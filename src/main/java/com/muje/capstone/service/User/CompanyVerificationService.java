package com.muje.capstone.service.User;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muje.capstone.dto.User.SingUp_In.CompanyVerificationCallbackRequest;
import com.muje.capstone.dto.User.SingUp_In.CompanyVerificationRequest;
import com.muje.capstone.dto.User.SingUp_In.CompanyVerificationResponse;
import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.codef.api.EasyCodefUtil.encryptRSA;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyVerificationService {

    private final EasyCodef codef;

    private String cachedToken;
    private long tokenExpiryMillis;

    private synchronized String getToken() {
        long now = System.currentTimeMillis();
        if (cachedToken != null && now < tokenExpiryMillis - TimeUnit.HOURS.toMillis(1)) {
            log.info("Codef Access Token re-used.");
            return cachedToken;
        }

        try {
            String token = codef.requestToken(EasyCodefServiceType.DEMO);

            if (token != null) {
                tokenExpiryMillis = now + TimeUnit.DAYS.toMillis(6);
                cachedToken = token;
                log.info("Codef Access Token newly issued.");
                return token;
            } else {
                log.error("Failed to get Codef Access Token. Token is null.");
                return null;
            }
        } catch (IOException e) {
            log.error("Exception occurred while requesting Codef token: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 1차 본인인증 요청 (EasyCodef.requestProduct 사용)
     * 응답으로 CF-03002를 받으면, 2차 인증을 위해 필요한 정보를 클라이언트에 반환합니다.
     */
    public CompanyVerificationResponse verifyCompany(CompanyVerificationRequest req) {
        String accessToken = getToken();
        if (accessToken == null) {
            return CompanyVerificationResponse.fail("CODEF_TOKEN_ERROR", "코드에프 Access Token 발급에 실패했습니다.");
        }

        String ID = "testID" + UUID.randomUUID();
        System.out.println(ID);

        HashMap<String, Object> params = new HashMap<>();
        params.put("organization", req.getOrganization());
        params.put("id", ID);
        params.put("loginType", req.getLoginType());
        params.put("loginTypeLevel", req.getLoginTypeLevel());
        params.put("userName", req.getUserName());
        params.put("telecom", req.getTelecom());
        params.put("phoneNo", req.getPhoneNo());
        params.put("identityEncYn", req.getIdentityEncYn());
        params.put("birthDate", req.getBirthDate());

        // 주민등록번호 뒷자리 암호화 로직
        if ("Y".equalsIgnoreCase(req.getIdentityEncYn()) && req.getIdentity() != null && !req.getIdentity().isEmpty()) {
            try {
                String encryptedIdentity = encryptRSA(req.getIdentity(), codef.getPublicKey());
                params.put("identity", encryptedIdentity);
                log.debug("Identity encrypted with custom RSA utility.");
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException |
                     IllegalBlockSizeException | BadPaddingException e) {
                log.error("Failed to encrypt identity with custom RSA utility: {}", e.getMessage(), e);
                return CompanyVerificationResponse.fail("RSA_ENCRYPTION_ERROR", "주민등록번호 암호화 중 오류가 발생했습니다.");
            }
        } else {
            params.put("identity", req.getIdentity());
        }

        String rawJson = null;
        try {
            // 요청 Endpoint는 "/v1/kr/public/pp/nps-minwon/member-join-list"
            String productRelativeUrl = "/v1/kr/public/pp/nps-minwon/member-join-list";
            log.info("Calling Codef API for initial verification with URL: {}", productRelativeUrl);
            log.debug("Initial request params sent to Codef: {}", params);

            // 1차 요청은 EasyCodef.requestProduct 사용
            rawJson = codef.requestProduct(productRelativeUrl, EasyCodefServiceType.DEMO, params);
            log.info("Codef API Raw Response for initial verification: {}", rawJson);

        } catch (InterruptedException | IOException e) {
            log.error("Exception occurred while calling Codef API for initial verification: {}", e.getMessage(), e);
            return CompanyVerificationResponse.fail("CODEF_API_CALL_ERROR", "코드에프 API 호출 중 오류가 발생했습니다.");
        }

        if (rawJson == null || rawJson.isEmpty()) {
            return CompanyVerificationResponse.fail("CODEF_NO_RESPONSE", "코드에프 API 응답이 없습니다.");
        }

        ObjectMapper om = new ObjectMapper();
        try {
            JsonNode root = om.readTree(rawJson);
            JsonNode result = root.path("result");
            JsonNode data = root.path("data"); // data 노드

            String resultCode = result.path("code").asText("UNKNOWN_CODE");
            String resultMessage = result.path("message").asText("UNKNOWN_MESSAGE");

            CompanyVerificationResponse.CompanyVerificationResponseBuilder builder =
                    CompanyVerificationResponse.builder()
                            .resultCode(resultCode)
                            .resultMessage(resultMessage)
                            .id(ID);

            if ("CF-03002".equals(resultCode)) {
                // 2-Way 인증 필요 응답: data 필드에서 직접 정보 추출
                builder.isVerified(false);
                builder.continue2Way(true);

                if (!data.isMissingNode() && !data.isNull()) {
                    builder.transactionId(data.path("jti").asText());
                    builder.jobIndex(data.path("jobIndex").asInt());
                    builder.threadIndex(data.path("threadIndex").asInt());
                    builder.twoWayTimestamp(data.path("twoWayTimestamp").asLong());
                    // organization과 id는 initial request에서 사용된 것을 그대로 넘겨줄 수 있습니다.
                    // 필요하다면 DTO에 추가하여 클라이언트가 다시 전달하도록 할 수 있습니다.
                    // 현재는 request2WayResult의 인자로 받도록 되어 있습니다.
                } else {
                    log.error("CF-03002 received but required 2-Way info fields are missing or null in data node.");
                    return CompanyVerificationResponse.fail("CODEF_2WAY_ERROR", "2-Way 인증 정보가 응답에 없습니다.");
                }


            } else if ("CF-00000".equals(resultCode)) {
                // 최종 성공 응답 (2-Way 인증이 완료된 후 또는 1차에서 바로 성공 시)
                builder.isVerified(true);
                builder.continue2Way(false);
                log.info("CF-00000 received. Company verification successful.");

                // 데이터 파싱 로직은 상품 응답 구조에 맞게
                if (data.isArray() && !data.isEmpty()) {
                    JsonNode first = data.get(0);
                    builder.resUserNm(first.path("resUserNm").asText(""));
                    builder.resCompanyNm(first.path("resCompanyNm").asText(""));
                    builder.resJoinUserType(first.path("resJoinUserType").asText(""));
                    builder.commStartDate(first.path("commStartDate").asText(""));
                    builder.commEndDate(first.path("commEndDate").asText(""));
                } else if (data.isObject()) {
                    builder.resUserNm(data.path("resUserNm").asText(""));
                    builder.resCompanyNm(data.path("resCompanyNm").asText(""));
                    builder.resJoinUserType(data.path("resJoinUserType").asText(""));
                    builder.commStartDate(data.path("commStartDate").asText(""));
                    builder.commEndDate(data.path("commEndDate").asText(""));
                } else {
                    log.warn("Codef API returned success code {} but no data found.", resultCode);
                    builder.isVerified(false);
                }
            } else {
                // 기타 에러
                builder.isVerified(false);
                builder.continue2Way(false);
                log.warn("Codef API returned error: [{}] {}", resultCode, resultMessage);
            }

            return builder.build();

        } catch (JsonProcessingException e) {
            log.error("JSON parsing error: {}", e.getMessage(), e);
            return CompanyVerificationResponse.fail("JSON_PARSE_ERROR", "코드에프 응답 파싱 중 오류가 발생했습니다.");
        }
    }

    /**
     * 2차 추가 인증 요청 (EasyCodef.requestCertification 사용)
     * 카카오톡 인증이 완료된 후, 1차 요청에서 받은 twoWayInfo와 함께 호출됩니다.
     */
    public CompanyVerificationResponse request2WayResult(CompanyVerificationCallbackRequest req) { // 단일 객체 파라미터로 변경
        String accessToken = getToken();
        if (accessToken == null) {
            return CompanyVerificationResponse.fail("CODEF_TOKEN_ERROR", "코드에프 Access Token 발급에 실패했습니다.");
        }

        HashMap<String, Object> params = new HashMap<>();
        params.put("organization", req.getOrganization()); // req에서 가져옴
        params.put("id", req.getId());

        // 간편인증 추가인증 입력부 (명세서 예시와 동일)
        params.put("simpleAuth", "1");
        params.put("is2Way", true);

        // identityEncYn 및 identity 추가
        params.put("identityEncYn", req.getIdentityEncYn()); // req에서 가져옴

        // 주민등록번호 뒷자리 암호화 로직
        if ("Y".equalsIgnoreCase(req.getIdentityEncYn()) && req.getIdentity() != null && !req.getIdentity().isEmpty()) {
            try {
                String encryptedIdentity = encryptRSA(req.getIdentity(), codef.getPublicKey());
                params.put("identity", encryptedIdentity);
                log.debug("Identity encrypted with custom RSA utility.");
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException |
                     IllegalBlockSizeException | BadPaddingException e) {
                log.error("Failed to encrypt identity with custom RSA utility: {}", e.getMessage(), e);
                return CompanyVerificationResponse.fail("RSA_ENCRYPTION_ERROR", "주민등록번호 암호화 중 오류가 발생했습니다.");
            }
        } else {
            params.put("identity", req.getIdentity());
        }

        // twoWayInfo 파라미터 설정 (명세서 예시와 동일)
        HashMap<String, Object> twoWayInfoMap = new HashMap<>();
        twoWayInfoMap.put("jobIndex", req.getJobIndex());         // req에서 가져옴
        twoWayInfoMap.put("threadIndex", req.getThreadIndex());   // req에서 가져옴
        twoWayInfoMap.put("jti", req.getTransactionId());         // req에서 가져옴
        twoWayInfoMap.put("twoWayTimestamp", req.getTwoWayTimestamp()); // req에서 가져옴
        params.put("twoWayInfo", twoWayInfoMap);

        String rawJson = null;
        try {
            String productRelativeUrl = "/v1/kr/public/pp/nps-minwon/member-join-list";
            log.info("Calling Codef API to check 2-Way result with URL: {}", productRelativeUrl);
            log.debug("Request params for 2-Way result: {}", params);

            rawJson = codef.requestCertification(productRelativeUrl, EasyCodefServiceType.DEMO, params);
            log.info("Codef API Raw Response for 2-Way result: {}", rawJson);

        } catch (InterruptedException | IOException e) {
            log.error("Exception occurred while calling Codef API for 2-Way result: {}", e.getMessage(), e);
            return CompanyVerificationResponse.fail("CODEF_API_CALL_ERROR", "코드에프 API 호출 중 오류가 발생했습니다.");
        }

        if (rawJson == null || rawJson.isEmpty()) {
            return CompanyVerificationResponse.fail("CODEF_NO_RESPONSE", "코드에프 API 응답이 없습니다.");
        }

        ObjectMapper om = new ObjectMapper();
        try {
            JsonNode root = om.readTree(rawJson);
            JsonNode result = root.path("result");
            JsonNode data = root.path("data");

            String resultCode = result.path("code").asText("UNKNOWN_CODE");
            String resultMessage = result.path("message").asText("UNKNOWN_MESSAGE");

            CompanyVerificationResponse.CompanyVerificationResponseBuilder builder =
                    CompanyVerificationResponse.builder()
                            .resultCode(resultCode)
                            .resultMessage(resultMessage);

            if ("CF-00000".equals(resultCode)) {
                builder.isVerified(true);
                builder.continue2Way(false);
                log.info("2-Way authentication completed successfully. Company verification successful.");

                if (data.isArray() && !data.isEmpty()) {
                    JsonNode first = data.get(0);
                    builder.resUserNm(first.path("resUserNm").asText(""));
                    builder.resCompanyNm(first.path("resCompanyNm").asText(""));
                    builder.resJoinUserType(first.path("resJoinUserType").asText(""));
                    builder.commStartDate(first.path("commStartDate").asText(""));
                    builder.commEndDate(first.path("commEndDate").asText(""));
                } else if (data.isObject()) {
                    builder.resUserNm(data.path("resUserNm").asText(""));
                    builder.resCompanyNm(data.path("resCompanyNm").asText(""));
                    builder.resJoinUserType(data.path("resJoinUserType").asText(""));
                    builder.commStartDate(data.path("commStartDate").asText(""));
                    builder.commEndDate(data.path("commEndDate").asText(""));
                } else {
                    log.warn("Codef API returned success code {} but no data found for 2-Way result.", resultCode);
                    builder.isVerified(false);
                }
            } else if ("CF-03003".equals(resultCode)) {
                builder.isVerified(false);
                builder.continue2Way(true);
                builder.transactionId(req.getTransactionId()); // req에서 가져옴
                builder.jobIndex(req.getJobIndex());           // req에서 가져옴
                builder.threadIndex(req.getThreadIndex());     // req에서 가져옴
                builder.twoWayTimestamp(req.getTwoWayTimestamp()); // req에서 가져옴
                log.warn("CF-03003 received. 2-Way authentication is still pending or failed. Retry.");
            } else {
                builder.isVerified(false);
                builder.continue2Way(false);
                log.warn("Codef API returned error for 2-Way result: [{}] {}", resultCode, resultMessage);
            }

            return builder.build();

        } catch (JsonProcessingException e) {
            log.error("JSON parsing error for 2-Way result: {}", e.getMessage(), e);
            return CompanyVerificationResponse.fail("JSON_PARSE_ERROR", "코드에프 응답 파싱 중 오류가 발생했습니다.");
        }
    }
}
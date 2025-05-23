package com.muje.capstone.service.User;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.muje.capstone.dto.User.SingUp_In.CompanyVerificationRequest;
import com.muje.capstone.dto.User.SingUp_In.CompanyVerificationResponse;
import io.codef.api.EasyCodef;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/*
@Service
@AllArgsConstructor
public class CompanyVerificationService {
    private final EasyCodef codef;

    // 토큰 재사용을 위한 내부 캐시
    private String cachedToken;
    private long tokenExpiryMillis;

    private synchronized String getToken() {
        long now = System.currentTimeMillis();
        if (cachedToken != null && now < tokenExpiryMillis) {
            // 재사용 가능한 토큰이 있으면 그대로 리턴
            return cachedToken;
        }
        // SANDBOX / DEMO / API 중 사용할 서비스 타입 지정
        String token = codef.requestToken(EasyCodefServiceType.DEMO);
        // Codef 토큰 만료 기본이 7일이므로, 6일 후 만료 처리
        tokenExpiryMillis = now + TimeUnit.DAYS.toMillis(6);
        cachedToken = token;
        return token;
    }

    public CompanyVerificationResponse verifyCompany(CompanyVerificationRequest req) {
        // 1) 토큰 얻기
        String accessToken = getToken();

        // 2) 호출 파라미터 맵 구성
        Map<String, Object> params = new HashMap<>();
        params.put("identityEncYn", req.getIdentityEncYn());
        params.put("birthDate",       req.getBirthDate());
        params.put("identity",        req.getIdentity());

        // 3) EasyCodef 로 API 호출
        //    - 첫번째 인자: 서비스 타입 (SANDBOX / DEMO / API)
        //    - 두번째: 그룹(pp), 세번째: API ID(nps-join-list)
        //    - 네번째: 헤더로 쓸 토큰
        //    - 다섯번째: 요청 파라미터
        String rawJson = codef.request(
                EasyCodefServiceType.SANDBOX,
                "pp",
                "nps-join-list",
                accessToken,
                params
        );

        // 4) JSON → DTO 변환
        ObjectMapper om = new ObjectMapper();
        JsonNode root = om.readTree(rawJson);
        JsonNode result = root.path("result");
        JsonNode data   = root.path("data");

        boolean isOk = "CF-00000".equals(result.path("code").asText());
        CompanyVerificationResponse resp = new CompanyVerificationResponse();
        resp.setVerified(isOk);
        if (isOk && !data.isMissingNode()) {
            resp.setResUserNm(     data.path("resUserNm").asText());
            resp.setResCompanyNm(  data.path("resCompanyNm").asText());
            resp.setResJoinUserType(data.path("resJoinUserType").asText());
            resp.setCommStartDate( data.path("commStartDate").asText());
            resp.setCommEndDate(   data.path("commEndDate").asText());
        }
        return resp;
    }
}
*/

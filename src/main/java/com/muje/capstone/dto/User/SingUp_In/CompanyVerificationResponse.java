package com.muje.capstone.dto.User.SingUp_In;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyVerificationResponse {
    private boolean isVerified;   // 회사 인증 성공 여부 (코드에프 API 호출 성공 여부)
    private String resultCode;    // 코드에프 응답 코드 (예: "CF-00000")
    private String resultMessage; // 코드에프 응답 메시지

    // 2-Way 인증 관련 필드 추가
    private String transactionId; // 2-Way 인증 진행 시 발급되는 트랜잭션 ID (jti)
    private boolean continue2Way; // 2-Way 인증이 필요한지 여부

    // twoWayInfo 내부 필드 추가 (클라이언트에게 전달 후 재활용)
    private Integer jobIndex;
    private Integer threadIndex;
    private Long twoWayTimestamp; // long 타입으로 받는 것이 안전합니다.

    private String id;
    private String resUserNm;       // 성명
    private String resCompanyNm;    // 상호(사업장명)
    private String resJoinUserType; // 가입자구분
    private String commEndDate;     // 자격유지종료일 (YYYYMMDD or "현재")
    private String commStartDate;   // 자격유지시작일 (YYYYMMDD)

    // 실패 응답을 쉽게 생성하기 위한 정적 팩토리 메서드
    public static CompanyVerificationResponse fail(String resultCode, String resultMessage) {
        return CompanyVerificationResponse.builder()
                .isVerified(false)
                .resultCode(resultCode)
                .resultMessage(resultMessage)
                .continue2Way(false) // 실패 시에는 2-Way 진행 안함
                .build();
    }
}
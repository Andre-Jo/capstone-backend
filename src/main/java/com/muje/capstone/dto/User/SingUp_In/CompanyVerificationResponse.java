package com.muje.capstone.dto.User.SingUp_In;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyVerificationResponse {
    private boolean isVerified; // 회사 인증 성공 여부
    private String resUserNm; // 성명
    private String resCompanyNm; // 상호(사업장명)
    private String resJoinUserType; // 가입자구분
    private String commEndDate; // 자격유지종료일 (YYYYMMDD or "현재")
    private String commStartDate; // 자격유지시작일 (YYYYMMDD)
}
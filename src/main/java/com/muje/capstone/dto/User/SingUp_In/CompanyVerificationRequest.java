package com.muje.capstone.dto.User.SingUp_In;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyVerificationRequest {
    private String identityEncYn = "Y"; // 주민등록번호 뒷자리 암호화 여부 ("Y"/"N"), 필요시 설정
    private String birthDate;           // 생년월일 (예: "850624" 형식)
    private String identity;            // 주민등록번호 뒤 7자리 (혹은 암호화된 값)
}
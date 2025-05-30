package com.muje.capstone.dto.User.SingUp_In;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyVerificationRequest {
    private String organization = "0001"; // 기관코드
    private String loginType = "5"; // 로그인 구분 (간편인증)
    private String loginTypeLevel = "1"; // 간편인증 로그인 구분 (카카오)
    private String identityEncYn = "Y"; // 주민등록번호 뒷자리 암호화 여부 ("Y"/"N"), 필요시 설정. 일반적으로 "Y" 고정.
    private String userName; // 사용자 이름
    private String telecom; // 통신사
    private String phoneNo; // 핸드폰 번호

    @NotBlank(message = "생년월일은 필수 입력 항목입니다.")
    @Pattern(regexp = "^\\d{6}$", message = "생년월일은 6자리 숫자(YYMMDD) 형식이어야 합니다.")
    private String birthDate; // 생년월일 (예: "850624" 형식)

    @NotBlank(message = "주민등록번호 뒷자리는 필수 입력 항목입니다.")
    // 암호화된 값을 받을 경우, 패턴은 달라질 수 있습니다.
    // 여기서는 암호화되지 않은 7자리 숫자를 가정합니다.
    @Pattern(regexp = "^\\d{7}$", message = "주민등록번호 뒤 7자리는 7자리 숫자 형식이어야 합니다.")
    private String identity; // 주민등록번호 뒤 7자리
}
package com.muje.capstone.dto.User.SingUp_In;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyVerificationCallbackRequest {
    @NotBlank(message = "트랜잭션 ID는 필수 입력 항목입니다.")
    private String transactionId; // jti 값

    @NotNull(message = "잡 인덱스는 필수 입력 항목입니다.")
    private Integer jobIndex;

    @NotNull(message = "쓰레드 인덱스는 필수 입력 항목입니다.")
    private Integer threadIndex;

    @NotNull(message = "타임스탬프는 필수 입력 항목입니다.")
    private Long twoWayTimestamp;

    private String organization = "0001"; // 1차 요청 시 사용된 기관코드

    @NotBlank(message = "식별 ID는 필수 입력 항목입니다.")
    private String id; // 1차 요청 시 사용된 식별 ID (ID)

    @NotBlank(message = "주민등록번호 암호화 여부는 필수 입력 항목입니다.")
    private String identityEncYn="Y"; // 1차 요청 시 보낸 'Y' 또는 'N'

    @NotBlank(message = "주민등록번호 뒷자리는 필수 입력 항목입니다.")
    private String identity; // 1차 요청 시 보낸 주민등록번호 뒷자리 (암호화 여부에 따라 평문/암호화된 값)
}
package com.muje.capstone.service;

import com.muje.capstone.dto.UniversityEmailVerificationRequest;
import com.muje.capstone.dto.UniversityValidationRequest;
import com.muje.capstone.dto.VerificationCodeValidationRequest;
import com.univcert.api.UnivCert;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class UnivService {

    @Value("${univcert.api-key}")
    private String apiKey;
    boolean univCheck = true;

    public boolean isUniversityValid(UniversityValidationRequest request) {
        String univName = request.getUnivName();

        try {
            Map<String, Object> result = UnivCert.check(univName);
            return (boolean) result.get("success");
        } catch (IOException e) {
            return false;
        }
    }

    public boolean isUniversityEmailValid(UniversityEmailVerificationRequest request) {
        String univName = request.getUnivName();
        String univEmail = request.getEmail();

        try {
            Map<String, Object> result = UnivCert.certify(apiKey, univEmail, univName, univCheck);
            return (boolean) result.get("success");
        } catch (IOException e) {
            return false;
        }
    }

    public boolean isUniversityEmailCodeValid(VerificationCodeValidationRequest request) {
        String univName = request.getUnivName();
        String univEmail = request.getEmail();
        int code = request.getCode();

        try {
            Map<String, Object> result = UnivCert.certifyCode(apiKey, univEmail, univName, code);
            return (boolean) result.get("success");
        } catch (IOException e) {
            return false;
        }
    }
}
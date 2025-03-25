package com.muje.capstone.service;

import com.univcert.api.UnivCert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class UnivService {

    public boolean isUniversityValid(String univName) {
        try {
            Map<String, Object> result = UnivCert.check(univName);
            return (boolean) result.get("success");
        } catch (IOException e) {
            // 예외 처리 로직
            return false;
        }
    }
}
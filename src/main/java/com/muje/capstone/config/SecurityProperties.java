package com.muje.capstone.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SecurityProperties {

    @Value("${security.permit-all-paths}")
    private String permitAllPaths;

    public List<String> getPermitAllPaths() {
        return Arrays.stream(permitAllPaths.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
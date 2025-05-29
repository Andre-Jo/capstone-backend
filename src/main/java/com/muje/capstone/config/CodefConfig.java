package com.muje.capstone.config;

import io.codef.api.EasyCodef;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class CodefConfig {

    // application.yml에서 정의한 정확한 경로로 변경합니다.
    @Value("${codef.client.id}")
    private String codefClientId;

    @Value("${codef.client.secret}")
    private String codefClientSecret;

    @Value("${codef.public.key}")
    private String codefPublicKey;

    @Value("${codef.api.base-url}")
    private String codefApiBaseUrl;

    @Bean
    public EasyCodef easyCodef() {
        EasyCodef codef = new EasyCodef();
        codef.setPublicKey(codefPublicKey);
        codef.setClientInfoForDemo(codefClientId, codefClientSecret);
        return codef;
    }
}
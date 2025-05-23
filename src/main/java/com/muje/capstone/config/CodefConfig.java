package com.muje.capstone.config;

import io.codef.api.EasyCodef;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class CodefConfig {

    @Value("${codef.client.id}")
    private String codefClientId;
    @Value("${codef.client.secret}")
    private String codefClientSecret;
    @Value("${codef.public.key}")
    private String codefPublicKey;

    @Bean
    public EasyCodef easyCodef() {
        EasyCodef codef = new EasyCodef();

        codef.setPublicKey(codefPublicKey);
        codef.setClientInfoForDemo(codefClientId, codefClientSecret);

        return codef;
    }
}
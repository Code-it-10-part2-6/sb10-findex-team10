package com.sb10findexteam6.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "open-api.fsc")
public class OpenApiProperties {

    /**
     * 공공데이터포털 금융위원회 지수시세정보 API Base URL
     */
    private String baseUrl;

    /**
     * 발급받은 서비스 인증키 (Decoding 키)
     */
    private String serviceKey;

}

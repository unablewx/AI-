package com.config;

import com.zhipu.oapi.ClientV4;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "zhipu")
@Data
public class AiConfig {

    @Value("apiKey")
    private String apiKey;

    @Bean
    public ClientV4 getClientV4() {
        return new ClientV4.Builder(apiKey).build();
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}

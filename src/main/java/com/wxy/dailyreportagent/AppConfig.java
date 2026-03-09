package com.wxy.dailyreportagent;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @Author: Wxy
 * @Date: 2026/3/9 16:37
 * @Description:
 */
@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 连接超时 10 秒（根据网络情况调整）
        factory.setConnectTimeout(10000);
        // 读取超时 60 秒（给模型更多时间处理）
        factory.setReadTimeout(60000);
        return new RestTemplate(factory);
    }
}

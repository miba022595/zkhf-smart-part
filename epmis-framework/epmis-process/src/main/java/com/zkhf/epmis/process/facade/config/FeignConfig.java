package com.zkhf.epmis.process.facade.config;

import com.zkhf.epmis.core.utils.ServletUtils;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
public class FeignConfig {

    // 令牌自定义标识
    @Value("${token.header}")
    private String header;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Content-Type", "application/json");
            // 可以添加多个header
            ServletRequestAttributes att = ServletUtils.getRequestAttributes();
            if (null == att) {
                log.error("非HTTP请求，无法获取header");
            } else {
                requestTemplate.header(header, att.getRequest().getHeader(header));
            }
        };
    }
}

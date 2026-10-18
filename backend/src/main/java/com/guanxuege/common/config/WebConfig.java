package com.guanxuege.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置：前端开发(5173) 与 生产 Nginx(8088) 均可访问 /api。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "http://localhost:5173", "http://127.0.0.1:5173",
                        "http://localhost:8088", "http://127.0.0.1:8088",
                        "https://guanxuege.cn", "https://www.guanxuege.cn",
                        "http://guanxuege.cn", "http://www.guanxuege.cn",
                        // 小程序 H5 预览（Trae 云端预览沙箱域，子域随机生成）与本地任意端口调试
                        "https://*.volcapp.com", "https://*.volceapi.com", "http://localhost:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    /**
     * 上传的图片静态映射：/api/images/** -> ./images 目录（容器内为 /app/images）
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/api/images/**")
                .addResourceLocations("file:./images/");
    }
}

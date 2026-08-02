package com.guanxuege;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 观学阁单体应用启动类（原 Spring Cloud 微服务已合并为单体）
 * - 包扫描：com.guanxuege（含 auth / learningplan / grade / community 子包）
 * - Mapper 扫描：仅扫描各模块的 mapper 包，避免把业务 Service 接口误注册为 MyBatis Mapper
 */
@SpringBootApplication(scanBasePackages = "com.guanxuege")
@MapperScan({
        "com.guanxuege.auth.mapper",
        "com.guanxuege.learningplan.mapper",
        "com.guanxuege.grade.mapper",
        "com.guanxuege.community.mapper"
})
public class SimpleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleApplication.class, args);
    }
}

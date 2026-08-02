package com.guanxuege.common.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局 Jackson 配置
 * <p>
 * 雪花主键为 19 位数字，超出 JS Number 安全整数范围（2^53），前端解析 JSON 数字会丢精度，
 * 导致更新/删除时主键匹配不上、关联字段写错值。这里将 Long 统一序列化为字符串，前端按
 * 字符串原样回传，后端 Long 反序列化不受影响。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> builder
                .serializerByType(Long.class, ToStringSerializer.instance)
                .serializerByType(Long.TYPE, ToStringSerializer.instance);
    }
}

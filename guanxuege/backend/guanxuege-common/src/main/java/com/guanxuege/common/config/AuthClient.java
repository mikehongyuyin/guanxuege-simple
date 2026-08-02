package com.guanxuege.common.config;

import com.guanxuege.common.R;
import com.guanxuege.common.dto.UserBrief;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 服务间调用：向 auth 服务查询用户简要信息
 * 由需要校验作者的业务服务（如 community）启用并注入
 */
@FeignClient(name = "guanxuege-auth", path = "/api/auth")
public interface AuthClient {

    @GetMapping("/internal/{userId}")
    R<UserBrief> getUserBrief(@PathVariable("userId") Long userId);
}

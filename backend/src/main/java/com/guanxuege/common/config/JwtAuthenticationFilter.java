package com.guanxuege.common.config;

import com.guanxuege.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 单体应用 JWT 鉴权过滤器（替代原 Gateway 的鉴权职责）：
 * - 放行：OPTIONS 预检、登录、注册
 * - 其余请求必须携带 Authorization: Bearer <token>
 * - 校验通过后，把 userId 注入到 X-User-Id 请求头，供 Controller 直接 @RequestHeader 读取
 */
@Component
@Order(1)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final List<String> WHITE_LIST = List.of("/api/auth/login", "/api/auth/register");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        // 放行：预检、登录/注册、上传的静态图片（评论图片需公开可见）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())
                || WHITE_LIST.contains(path)
                || path.startsWith("/api/images/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录或 token 缺失");
            return;
        }

        try {
            String token = auth.substring(7).trim();
            Long userId = JwtUtil.getUserId(token);
            MutableRequestWrapper wrapper = new MutableRequestWrapper(request);
            wrapper.putHeader("X-User-Id", String.valueOf(userId));
            filterChain.doFilter(wrapper, response);
        } catch (Exception e) {
            writeUnauthorized(response, "token 无效或已过期");
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
    }
}

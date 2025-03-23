package com.levelUp2.project_scaffolding_server.control;

import com.levelUp2.project_scaffolding_server.AuthenticateUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.lang.NonNull;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String accessToken = request.getHeader("Authorization");
        request.setAttribute("access_token", accessToken);

        if (accessToken == null || !AuthenticateUser.getUserInfo(accessToken.replaceFirst("Bearer", "").trim())) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return false;
        }
        return true;
    }
}
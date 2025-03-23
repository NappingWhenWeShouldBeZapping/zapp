package com.levelUp2.project_scaffolding_server.control;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.lang.NonNull;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String accessToken = request.getHeader("Authorization").replaceFirst("Bearer", "").trim();
        request.setAttribute("access_token", accessToken);

        if (!GoogleAuthValidator.isValidGoogleToken(accessToken)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return false;
        }
        return true;
    }
}
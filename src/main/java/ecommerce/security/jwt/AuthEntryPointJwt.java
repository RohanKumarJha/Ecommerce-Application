package ecommerce.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String customMessage = (String) request.getAttribute("exception");

        if ("USER_DELETED".equals(customMessage)) {
            customMessage = "User no longer exists. Please login again.";
        } else {
            customMessage = "Authentication failed. Invalid or expired token.";
        }

        Map<String, Object> body = new HashMap<>();
        body.put("status", 401);
        body.put("error", "UNAUTHORIZED");
        body.put("message", customMessage);
        body.put("path", request.getServletPath());
        body.put("timestamp", LocalDateTime.now().toString());

        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }
}
package ecommerce.auth.dto.response;

import java.util.Set;

public record LoginResponse(
        Long userId,
        String jwtToken,
        String userName,
        Set<String> roles
) {}
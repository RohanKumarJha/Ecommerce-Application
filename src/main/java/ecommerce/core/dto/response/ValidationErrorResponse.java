package ecommerce.core.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {

    private boolean success;
    private int status;
    private LocalDateTime timestamp;
    private Map<String, String> errors;

    public ValidationErrorResponse(Map<String, String> errors, int status) {
        this.success = false;
        this.status = status;
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
    }

}
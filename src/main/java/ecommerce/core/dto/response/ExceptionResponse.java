package ecommerce.core.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionResponse {

    private String message;
    private boolean success;
    private int status;
    private LocalDateTime timestamp;

    public ExceptionResponse(String message, boolean success, int status) {
        this.message = message;
        this.success = success;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
}
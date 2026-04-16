package ecommerce.core.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponse<T> {

    private List<T> content;

    private Integer pageNumber;
    private Integer pageSize;

    private Long totalElements;
    private Integer totalPages;

    private Boolean lastPage;
}
package cafe.product.server.presentation.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode {

    NOT_FOUND_CATEGORY(HttpStatus.NOT_FOUND, "카테고리가 존재하지 않습니다")
    ;

    private final HttpStatus status;
    private final String message;
}

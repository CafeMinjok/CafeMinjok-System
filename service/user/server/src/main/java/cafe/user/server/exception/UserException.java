package cafe.user.server.exception;

import cafe.domain.exception.BusinessException;
import lombok.Getter;

@Getter
public class UserException extends BusinessException {

    private final UserErrorCode errorCode;

    public UserException(UserErrorCode errorCode) {
        super(errorCode.getStatus().name(), errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

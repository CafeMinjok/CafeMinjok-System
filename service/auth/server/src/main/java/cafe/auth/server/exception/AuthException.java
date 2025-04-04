package cafe.auth.server.exception;

import cafe.domain.exception.BusinessException;
import lombok.Getter;

@Getter
public class AuthException extends BusinessException {

  AuthErrorCode errorCode;

  public AuthException(AuthErrorCode errorCode) {
    super(errorCode.getStatus().name(), errorCode.getMessage());
    this.errorCode = errorCode;
  }
}

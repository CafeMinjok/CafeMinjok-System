package cafe.product.server.presentation.exception;

import cafe.domain.exception.BusinessException;
import lombok.Getter;

@Getter
public class ProductServerException extends BusinessException {
  private final ProductErrorCode errorCode;

  public ProductServerException(ProductErrorCode errorCode) {
    super(errorCode.getStatus().name(), errorCode.getMessage());
    this.errorCode = errorCode;
  }
}

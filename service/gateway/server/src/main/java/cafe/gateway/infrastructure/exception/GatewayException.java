package cafe.gateway.infrastructure.exception;

import cafe.domain.exception.BusinessException;

public class GatewayException extends BusinessException {

    public GatewayException(GatewayErrorCode errorCode) {
        super(errorCode.getStatus().name(), errorCode.getMessage());
    }
}

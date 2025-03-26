package cafe.auth.server.infrastructure.feign;

import cafe.auth.server.exception.AuthErrorCode;
import cafe.auth.server.exception.AuthException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class UserErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        return new AuthException(AuthErrorCode.INTERNAL_SERVER_ERROR);
    }
}

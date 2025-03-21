package cafe.gateway.infrastructure.configuration;


import feign.Logger;
import feign.codec.Decoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;

public class AuthFeignConfig {

    // Feign에서 응답 데이터를 디코딩하는 방법을 설정
    @Bean
    public Decoder feignDecoder() {
        ObjectFactory<HttpMessageConverters> messageConverters = HttpMessageConverters::new;
        return new SpringDecoder(messageConverters);
    }

    // Feign 클라이언트의 로깅 레벨을 설정
    @Bean
    public Logger.Level feignLoggerLevel() { return Logger.Level.FULL; }
}

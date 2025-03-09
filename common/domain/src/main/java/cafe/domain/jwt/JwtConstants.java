package cafe.domain.jwt;

public enum JwtConstants {

    BEARER_PREFIX("Bearer "),
    AUTHORIZATION("Authorization"),
    X_USER_CLAIMS("x-user-claims");

    private final String value;

    JwtConstants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

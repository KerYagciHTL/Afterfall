package at.htl.afterfall.protocol.response;

public class RegisterResponse extends GameResponse {
    private static final long serialVersionUID = 1L;

    public boolean success;
    public String  message;

    public RegisterResponse() {}

    public RegisterResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}

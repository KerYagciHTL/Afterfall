package at.htl.afterfall.protocol.response;

import at.htl.afterfall.protocol.dto.GameConfigDto;

public class RegisterResponse extends GameResponse {
    private static final long serialVersionUID = 2L;

    public boolean       success;
    public String        message;
    public GameConfigDto config;

    public RegisterResponse() {}

    public RegisterResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public RegisterResponse(boolean success, String message, GameConfigDto config) {
        this.success = success;
        this.message = message;
        this.config  = config;
    }
}

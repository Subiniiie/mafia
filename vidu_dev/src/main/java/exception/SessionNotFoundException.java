package exception;

import lombok.Getter;

@Getter
public class SessionNotFoundException extends Exception {
    private final String sessionId;

    public SessionNotFoundException(String message, String sessionId) {
        super(message);
        this.sessionId = sessionId;
    }
}
package de.interaapps.pastely.exceptions;

public class AwaitingAccessException extends HTTPException {
    public AwaitingAccessException(String message) {
        super(403, message);
    }

    public AwaitingAccessException() {
        this("Awaiting access");
    }
}

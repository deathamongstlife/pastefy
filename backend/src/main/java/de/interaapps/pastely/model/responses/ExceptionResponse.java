package de.interaapps.pastely.model.responses;

import de.interaapps.pastely.exceptions.AuthenticationException;
import de.interaapps.pastely.exceptions.NotFoundException;

public class ExceptionResponse extends ActionResponse {

    public String exception;
    public boolean error = true;
    public boolean exists = false;

    public ExceptionResponse(Throwable throwable) {
        if (!(throwable instanceof AuthenticationException || throwable instanceof NotFoundException))
            throwable.printStackTrace();
        success = false;

        exception = throwable.getClass().getSimpleName();
    }
}

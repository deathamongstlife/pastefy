package cc.allyapps.pastely.model.responses;

import cc.allyapps.pastely.exceptions.AuthenticationException;
import cc.allyapps.pastely.exceptions.NotFoundException;

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

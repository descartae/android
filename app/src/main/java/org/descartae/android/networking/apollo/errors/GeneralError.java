package org.descartae.android.networking.apollo.errors;

/**
 * Created by lucasmontano on 14/02/2018.
 */
public class GeneralError {

    private String message;

    public GeneralError() {

    }

    public GeneralError(String message) {
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

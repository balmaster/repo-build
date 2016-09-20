package repo.build

import groovy.transform.CompileStatic;

@CompileStatic
class RepoBuildException extends RuntimeException {

    public RepoBuildException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepoBuildException(String message) {
        super(message);
    }

}

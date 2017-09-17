package repo.build

import groovy.transform.CompileStatic

@CompileStatic
class RepoBuildException extends RuntimeException {

    RepoBuildException(String message, Throwable cause) {
        super(message, cause)
    }

    RepoBuildException(String message) {
        super(message)
    }

}

package repo.build

class RepoBuildException extends RuntimeException {

    public RepoBuildException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepoBuildException(String message) {
        super(message);
    }

}

package buildlogic.exception

class InvalidExitCodeException extends RuntimeException {
    private final int exitCode;

    InvalidExitCodeException(int exitCode, String m) {
        super(m);
        this.exitCode = exitCode;
    }

    int getExitCode() { return exitCode; }
}

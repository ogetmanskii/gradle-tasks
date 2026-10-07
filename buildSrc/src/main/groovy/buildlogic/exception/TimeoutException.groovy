package buildlogic.exception

class TimeoutException extends RuntimeException {
    TimeoutException(String m) { super(m); }

    TimeoutException(String m, Throwable c) { super(m, c); }
}
package buildlogic.exception

class UnhealthyException extends RuntimeException {

    public UnhealthyException(String m) { super(m); }

    public UnhealthyException(String m, Throwable c) { super(m, c); }
}

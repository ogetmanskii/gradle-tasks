package gr.utils

class ClosureUtils {
    static <T> T applyClosure(Closure closure, T delegate) {
        closure.delegate = delegate
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call()
        return delegate
    }
}

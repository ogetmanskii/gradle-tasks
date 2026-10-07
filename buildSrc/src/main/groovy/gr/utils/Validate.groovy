package gr.utils

class Validate {

    static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message)
        }
    }

    static void directoryExists(String directory) {
        isTrue(directory != null, "directory must not be null")
        if (!(new File(directory).isDirectory())) {
            throw new IllegalArgumentException("Must be a directory: " + directory)
        }
    }
}

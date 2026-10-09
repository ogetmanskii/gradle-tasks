package buildlogic.utils

class Validate {

    static <T> T notNull(T object, String objectDescription) {
        if (object == null) {
            if (objectDescription == null) {
                objectDescription = "object"
            }
            throw new NullPointerException(objectDescription + " must not be null")
        }
        return object
    }

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

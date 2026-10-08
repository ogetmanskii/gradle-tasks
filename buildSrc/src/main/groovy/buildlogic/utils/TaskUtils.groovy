package buildlogic.utils

import org.gradle.api.provider.Provider

class TaskUtils {

    static String getString(Object object, String defaultValue) {
        if (object == null) {
            return defaultValue
        } else if (object instanceof String) {
            return object
        } else if (object instanceof Provider) {
            return getString(object.get(), defaultValue)
        } else if (object instanceof File) {
            return object.getPath()
        } else {
            return object.toString()
        }
    }

    static Integer getInteger(Object object, Integer defaultValue) {
        if (object == null) {
            return defaultValue
        } else if (object instanceof Integer) {
            return object
        } else if (object instanceof String) {
            return Integer.parseInt(object)
        } else if (object instanceof GString) {
            return Integer.parseInt(object.toString())
        } else if (object instanceof Provider) {
            return getInteger(object.get(), defaultValue)
        }
        throw new IllegalArgumentException("Unsupported integer type: " + object.getClass().getName())
    }

    static boolean getBoolean(Object object, boolean defaultValue) {
        if (object == null) {
            return defaultValue
        } else if (object instanceof Boolean) {
            return object
        } else if (object instanceof String) {
            return Boolean.parseBoolean(object)
        } else if (object instanceof GString) {
            return Boolean.parseBoolean(object.toString())
        } else if (object instanceof Provider) {
            return getBoolean(object.get(), defaultValue)
        }
        throw new IllegalArgumentException("Unsupported boolean type: " + object.getClass().getName())
    }
}

package gr.utils.spec

import gr.utils.Validate
import org.apache.commons.lang3.StringUtils

import javax.annotation.Nullable

class CommandSpec {

    // Shell команда для запуска
    String command

    void command(String v) {
        command = Objects.requireNonNull(v)
    }

    // Рабочая папка для запуска команды
    String workDir

    void workDir(String v) {
        workDir = Objects.requireNonNull(v)
    }

    // Переменные окружения
    @Nullable Map<String, String> env

    void env(Map<String, String> env) {
        this.env = env
    }

    void env(String key, String value) {
        if (env == null) {
            env = new HashMap<String, String>()
        }
        env.put(key, value)
    }

    // Если не null: отсоединиться от процесса через N секунд
    @Nullable Float detachAfterSeconds

    void detachAfterSeconds(Float v) {
        Validate.isTrue(v != null && v > 0f, "detachAfterSeconds must be positive value")
        detachAfterSeconds = v
    }

    // Если не null: отсоединиться от процесса после появления указанной фразы в stdout
    @Nullable String detachAfterPhrase

    void detachAfterPhrase(String v) {
        detachAfterPhrase = v
    }

    // Если не null: если команда не завершилась за N секунд, тогда выбросить исключение
    @Nullable Float timeoutSeconds

    void timeoutSeconds(Float v) {
        Validate.isTrue(v != null && v > 0f, "timeoutSeconds must be positive value")
        timeoutSeconds = v
    }

    // Если не null: указанные коды выхода не приводят к выбрасыванию исключения.
    // Если null: коды выхода, отличные от 0 - приводят к выбрасыванию исключения.
    @Nullable List<Integer> validExitCodes

    void validExitCodes(List<Integer> list) {
        validExitCodes = list
    }

    void validExitCode(int v) {
        if (validExitCodes == null) {
            validExitCodes = new ArrayList<>()
        }
        validExitCodes.add(v)
    }

    void validate() {
        Validate.isTrue(StringUtils.isNotBlank(command), "command must not be blank")
        Validate.directoryExists(workDir)
    }
}

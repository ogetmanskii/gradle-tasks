package buildlogic.spec

import buildlogic.utils.Validate
import org.apache.commons.lang3.StringUtils

import javax.annotation.Nullable

class CommandSpec {

    // Shell команда для запуска
    private String command

    // Рабочая папка для запуска команды
    private String workDir

    // Переменные окружения
    @Nullable
    private Map<String, String> env

    // Если не null: отсоединиться от процесса через N секунд
    @Nullable
    private Float detachAfterSeconds

    // Если не null: отсоединиться от процесса после появления указанной фразы в stdout
    @Nullable
    private String detachAfterPhrase

    // Если не null: указанные коды выхода не приводят к выбрасыванию исключения.
    // Если null: коды выхода, отличные от 0 - приводят к выбрасыванию исключения.
    @Nullable
    private List<Integer> validExitCodes

    // Если не null: если команда не завершилась за N секунд, тогда выбросить исключение
    @Nullable
    private Float timeoutSeconds

    void validate() {
        Validate.isTrue(StringUtils.isNotBlank(command), "command must not be blank")
        Validate.directoryExists(workDir)
    }

    void command(String v) {
        command = Objects.requireNonNull(v)
    }

    void workDir(String v) {
        workDir = Objects.requireNonNull(v)
    }

    void env(Map<String, String> env) {
        this.env = env
    }

    void env(String key, String value) {
        if (env == null) {
            env = new HashMap<String, String>()
        }
        env.put(key, value)
    }

    void detachAfterSeconds(Float v) {
        Validate.isTrue(v != null && v > 0f, "detachAfterSeconds must be positive value")
        detachAfterSeconds = v
    }

    void detachAfterPhrase(String v) {
        detachAfterPhrase = v
    }

    void timeoutSeconds(Float v) {
        Validate.isTrue(v != null && v > 0f, "timeoutSeconds must be positive value")
        timeoutSeconds = v
    }

    void validExitCodes(List<Integer> list) {
        validExitCodes = list
    }

    void validExitCode(int v) {
        if (validExitCodes == null) {
            validExitCodes = new ArrayList<>()
        }
        validExitCodes.add(v)
    }

    String getCommand() {
        return command
    }

    String getWorkDir() {
        return workDir
    }

    @Nullable
    Map<String, String> getEnv() {
        return env
    }

    @Nullable
    Float getDetachAfterSeconds() {
        return detachAfterSeconds
    }

    @Nullable
    String getDetachAfterPhrase() {
        return detachAfterPhrase
    }

    @Nullable
    List<Integer> getValidExitCodes() {
        return validExitCodes
    }

    @Nullable
    Float getTimeoutSeconds() {
        return timeoutSeconds
    }
}

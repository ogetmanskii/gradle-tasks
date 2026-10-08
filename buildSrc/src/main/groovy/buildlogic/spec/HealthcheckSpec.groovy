package buildlogic.spec

import buildlogic.utils.ClosureUtils
import buildlogic.utils.Validate

class HealthcheckSpec {

    // Команда
    private CommandSpec command

    // Интервал в секундах между запусками команды для проверки здоровья
    private Float intervalSeconds

    // Общий таймаут для проверок здоровья
    private Float timeoutSeconds

    void command(@DelegatesTo(CommandSpec) Closure c) {
        command = ClosureUtils.applyClosure(c, new CommandSpec())
        command.validate()
    }

    void intervalSeconds(Float v) {
        intervalSeconds = v
    }

    void timeoutSeconds(Float v) {
        timeoutSeconds = v
    }

    void validate() {
        Objects.requireNonNull(command, "command must not be null")
        Validate.isTrue(intervalSeconds != null && intervalSeconds > 0f, "intervalSeconds must be positive value")
        Validate.isTrue(timeoutSeconds != null && timeoutSeconds > 0f, "timeoutSeconds must be positive value")
        Validate.isTrue(timeoutSeconds > intervalSeconds, "timeoutSeconds must be higher than intervalSeconds")
    }

    CommandSpec getCommand() {
        return command
    }

    Float getIntervalSeconds() {
        return intervalSeconds
    }

    Float getTimeoutSeconds() {
        return timeoutSeconds
    }
}

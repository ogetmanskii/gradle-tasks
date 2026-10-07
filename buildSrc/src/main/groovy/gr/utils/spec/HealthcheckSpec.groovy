package gr.utils.spec

import gr.utils.ClosureUtils
import gr.utils.Validate

class HealthcheckSpec {

    // Команда
    CommandSpec command

    void command(@DelegatesTo(CommandSpec) Closure c) {
        command = ClosureUtils.applyClosure(c, new CommandSpec())
        command.validate()
    }

    // Интервал в секундах между запусками команды для проверки здоровья
    Float intervalSeconds

    void intervalSeconds(Float v) {
        intervalSeconds = v
    }

    // Общий таймаут для проверок здоровья
    Float timeoutSeconds

    void timeoutSeconds(Float v) {
        timeoutSeconds = v
    }

    void validate() {
        Objects.requireNonNull(command, "command must not be null")
        Validate.isTrue(intervalSeconds != null && intervalSeconds > 0f, "intervalSeconds must be positive value")
        Validate.isTrue(timeoutSeconds != null && timeoutSeconds > 0f, "timeoutSeconds must be positive value")
        Validate.isTrue(timeoutSeconds > intervalSeconds, "timeoutSeconds must be higher than intervalSeconds")
    }
}

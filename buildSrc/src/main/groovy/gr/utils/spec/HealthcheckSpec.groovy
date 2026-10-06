package gr.utils.spec

import gr.utils.ClosureUtils

class HealthcheckSpec {

    // Команда
    CommandSpec command

    void command(@DelegatesTo(CommandSpec) Closure c) {
        command = ClosureUtils.applyClosure(c, new CommandSpec())
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
}

package gr.utils.task

import gr.utils.ShellRunner
import gr.utils.shell.ShellRunnerImpl
import gr.utils.spec.ServiceSpec
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class HealthcheckServiceTask extends DefaultTask {

    private static ShellRunner shellRunner = ShellRunnerImpl.INSTANCE

    @Input
    abstract ServiceSpec service

    @TaskAction
    void execute() {
        if (service.healthcheck == null) {
            logger.lifecycle("${service.name} does not have healthcheck specification")
            return
        }
        shellRunner.healthcheck(logger, service.healthcheck)
        logger.lifecycle("${service.name} HEALTHY")
    }

}

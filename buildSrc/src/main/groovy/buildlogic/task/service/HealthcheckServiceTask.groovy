package buildlogic.task.service

import buildlogic.shell.ShellRunner
import buildlogic.shell.ShellRunnerImpl
import buildlogic.spec.ServiceSpec
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class HealthcheckServiceTask extends DefaultTask {

    private static final ShellRunner shellRunner = ShellRunnerImpl.INSTANCE

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

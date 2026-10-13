package buildlogic.task.service

import buildlogic.shell.ShellRunner
import buildlogic.shell.ShellRunnerImpl
import buildlogic.spec.ServiceSpec
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import java.nio.charset.Charset

abstract class HealthcheckServiceTask extends DefaultTask {

    @Input
    abstract ServiceSpec service

    @TaskAction
    void execute() {
        if (service.healthcheck == null) {
            logger.lifecycle("${service.name} does not have healthcheck specification")
            return
        }

        String charset = service.healthcheck.command.charset
        ShellRunner shellRunner = new ShellRunnerImpl(logger, charset == null ? null : Charset.forName(charset))
        shellRunner.healthcheck(service.healthcheck)
        logger.lifecycle("${service.name} HEALTHY")
    }

}

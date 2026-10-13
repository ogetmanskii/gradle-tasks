package buildlogic.task.service

import buildlogic.spec.CommandSpec
import buildlogic.utils.ProcessUtils
import buildlogic.shell.ShellRunner
import buildlogic.shell.ShellRunnerImpl
import buildlogic.spec.ServiceSpec
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import java.nio.charset.Charset

abstract class UpServiceTask extends DefaultTask {

    @Input
    abstract ServiceSpec service

    @TaskAction
    void execute() {
        if (service.imageFullPath != null) {
            if (ProcessUtils.hasActiveProcess(logger, service.imageFullPath)) {
                logger.lifecycle("${service.name} UP")
                return
            }
        }
        CommandSpec command = service.upCommand
        ShellRunner shellRunner = new ShellRunnerImpl(logger, command.charset == null ? null : Charset.forName(command.charset))
        shellRunner.run(command)
        logger.lifecycle("${service.name} UP")
    }

}

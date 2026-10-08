package buildlogic.task.service

import buildlogic.utils.ProcessUtils
import buildlogic.shell.ShellRunner
import buildlogic.shell.ShellRunnerImpl
import buildlogic.spec.ServiceSpec
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class UpServiceTask extends DefaultTask {

    private static ShellRunner shellRunner = ShellRunnerImpl.INSTANCE

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
        shellRunner.run(logger, service.upCommand)
        logger.lifecycle("${service.name} UP")
    }

}

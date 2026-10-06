package gr.utils.task

import gr.utils.ProcessUtils
import gr.utils.ShellRunner
import gr.utils.shell.ShellRunnerImpl
import gr.utils.spec.ServiceSpec
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class DownServiceTask extends DefaultTask {

    private static ShellRunner shellRunner = ShellRunnerImpl.INSTANCE

    @Input
    abstract ServiceSpec service

    @TaskAction
    void execute() {
        if (service.imageFullPath != null) {
            if (!ProcessUtils.hasActiveProcess(logger, service.imageFullPath)) {
                logger.lifecycle("${service.name} DOWN")
                return
            }
        }
        if (service.downCommand != null) {
            shellRunner.run(logger, service.downCommand)
            logger.lifecycle("${service.name} DOWN")
            return
        } else if (service.imageFullPath != null) {
            ProcessUtils.terminateProcess(logger, service.imageFullPath)
            logger.lifecycle("${service.name} DOWN")
            return
        }
        throw new IllegalStateException("Cannot down ${service.name}: the service does not have downCommand not imageFullPath")
    }

}

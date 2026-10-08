package buildlogic.task

import buildlogic.utils.OsUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.process.internal.ExecAction
import org.gradle.process.internal.ExecActionFactory

import javax.inject.Inject

abstract class ShellTask extends DefaultTask {

    @Inject
    protected abstract ExecActionFactory getExecActionFactory();

    @Input abstract String workDir
    @Input abstract Map<String, String> env = [:]
    @Input abstract String command
    @Input abstract boolean detach = false

    @TaskAction
    void execute() {
        if (detach) {
            List<String> commands = new ArrayList<>();
            if (OsUtils.IS_WINDOWS) {
                commands.add("powershell")
                commands.add("-Command")
            } else {
                commands.add("sh")
                commands.add("-c")
            }
            println("Run detached: ${commands}")
            commands.add(command)
            def pb = new ProcessBuilder()
                .command(commands)
                .directory(new File(workDir))
            pb.environment().putAll(env)
            pb.start()
        } else {
            ExecAction exec = getExecActionFactory().newExecAction()
            if (OsUtils.IS_WINDOWS) {
                exec.commandLine("powershell", "-Command", command)
            } else {
                exec.commandLine("sh", "-c", command)
            }
            exec.environment(env)
            exec.workingDir(workDir)
            exec.execute()
        }
    }
}

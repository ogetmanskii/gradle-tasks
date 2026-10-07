package gr.utils.task

import gr.utils.OsUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.process.internal.ExecAction
import org.gradle.process.internal.ExecActionFactory

import javax.inject.Inject

abstract class ShellTask extends DefaultTask {

    @Inject
    protected abstract ExecActionFactory getExecActionFactory();

    @Input abstract Object workDir
    @Input abstract List<Object> commands = []
    @Input abstract Object detach = false

    @TaskAction
    void execute() {
        boolean detached = TaskUtils.getBoolean(detach, false)
        for (def cmd in commands) {
            executeCommand(workDir, cmd, detached)
        }
    }

    private void executeCommand(Object workDir, Object cmd, boolean detached) {
        String cmdString = TaskUtils.getString(cmd, null)
        if (cmdString == null) {
            throw new IllegalStateException("cmd must not be null")
        }
        String workDirString = TaskUtils.getString(workDir, ".")

        if (detached) {
            List<String> command = new ArrayList<>();
            if (OsUtils.isWindows()) {
                command.add("powershell")
                command.add("-Command")
            } else {
                command.add("sh")
                command.add("-c")
            }
            println("Run detached: ${cmdString}")
            command.add(cmdString)
            new ProcessBuilder()
                .command(command)
                .directory(new File(workDirString))
                .start()
        } else {
            ExecAction exec = getExecActionFactory().newExecAction()
            if (OsUtils.isWindows()) {
                exec.commandLine("cmd", "/C", cmdString)
            } else {
                exec.commandLine("sh", "-c", cmdString)
            }
            exec.workingDir(workDirString)
            exec.execute()
        }
    }
}

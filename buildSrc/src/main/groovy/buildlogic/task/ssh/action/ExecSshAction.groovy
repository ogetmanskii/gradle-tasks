package buildlogic.task.ssh.action


import buildlogic.utils.Validate

class ExecSshAction implements SshAction {

    private final String command

    ExecSshAction(String command) {
        this.command = Validate.notNull(command, "command")
    }

    @Override
    SshActionType getType() {
        return SshActionType.EXEC
    }

    String getCommand() {
        return command
    }
}

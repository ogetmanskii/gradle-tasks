package buildlogic.task.ssh.action


import buildlogic.utils.Validate

class UploadSshAction implements SshAction {

    private final String localFile
    private final String remoteFile
    private final int permissions

    UploadSshAction(String localFile, String remoteFile, int permissions) {
        this.localFile = Validate.notNull(localFile, "localFile")
        this.remoteFile = Validate.notNull(remoteFile, "remoteFile")
        this.permissions = permissions
    }

    @Override
    SshActionType getType() {
        return SshActionType.TRANSFER
    }

    String getLocalFile() {
        return localFile
    }

    String getRemoteFile() {
        return remoteFile
    }

    int getPermissions() {
        return permissions
    }
}

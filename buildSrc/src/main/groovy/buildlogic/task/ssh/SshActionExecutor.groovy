package buildlogic.task.ssh

import buildlogic.task.ssh.action.ExecSshAction
import buildlogic.task.ssh.action.SshAction
import buildlogic.task.ssh.action.SshActionType
import buildlogic.task.ssh.action.UploadSshAction
import buildlogic.utils.Validate
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.Session
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger

class SshActionExecutor {

    private final Session session
    private final Logger logger

    SshActionExecutor(Session session, Logger logger) {
        this.session = Validate.notNull(session, "SSH session")
        this.logger = Validate.notNull(logger, "Task logger")
    }

    void executeSshAction(SshAction action) {
        if (action.getType() == SshActionType.EXEC && action instanceof ExecSshAction) {
            executeCommand(action.command)
        } else if (action.getType() == SshActionType.TRANSFER && action instanceof UploadSshAction) {
            uploadFile(action.localFile, action.remoteFile, action.permissions)
        } else {
            throw new IllegalArgumentException("Unsupported ssh action type: " + action.getType())
        }
    }

    private void executeCommand(String cmd) {
        ChannelExec channel = session.openChannel('exec') as ChannelExec
        channel.setCommand(cmd)
        channel.setInputStream(null)
        def errStream = channel.getErrStream()
        def out = channel.getInputStream()
        channel.connect()
        Thread outThread = Thread.start('ssh-stdout') {
            out.withStream { InputStream is ->
                logStream(is)
            }
        }
        Thread errThread = Thread.start('ssh-stderr') {
            errStream.withStream { InputStream is ->
                logStream(is)
            }
        }
        while (!channel.isClosed()) {
            Thread.sleep(50)
        }
        outThread.join()
        errThread.join()
        int exit = channel.getExitStatus()
        channel.disconnect()
        if (exit != 0) {
            throw new GradleException("Remote command failed (exit ${exit}): ${cmd}")
        }
    }

    private void logStream(InputStream is) {
        Reader reader = new InputStreamReader(is)
        String line
        while ((line = reader.readLine()) != null) {
            logger.lifecycle(line)
        }
    }

    private void uploadFile(String localFile, String remoteFile, int permissions) {
        File sourceFile = new File(localFile)
        Validate.isTrue(sourceFile.isFile(), "localFile must be a file")
        try (ChannelSftp channel = session.openChannel('sftp') as ChannelSftp) {
            channel.connect()
            logger.lifecycle("Upload: ${localFile} -> ${remoteFile}, permissions: ${permissions}")
            sourceFile.withInputStream {
                channel.put(it, remoteFile)
            }
            channel.chmod(permissions, remoteFile)
            channel.disconnect()
        }
    }
}

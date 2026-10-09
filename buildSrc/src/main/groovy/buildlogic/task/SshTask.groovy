package buildlogic.task

import buildlogic.spec.RemoteHostSpec
import buildlogic.task.ssh.action.ExecSshAction
import buildlogic.task.ssh.action.SshAction
import buildlogic.task.ssh.SshActionExecutor
import buildlogic.task.ssh.action.UploadSshAction
import buildlogic.utils.TaskUtils
import buildlogic.utils.Validate
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.apache.commons.lang3.StringUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import java.nio.charset.StandardCharsets

class SshTask extends DefaultTask {

    @Input RemoteHostSpec remote

    private List<SshAction> sshActions = []

    void exec(String command) {
        Validate.isTrue(StringUtils.isNotBlank(command), "command must not be blank")
        sshActions.add(new ExecSshAction(command))
    }

    void upload(String localFile, String remoteFile, int permissions) {
        Validate.isTrue(StringUtils.isNotBlank(localFile), "localFile must not be blank")
        Validate.isTrue(StringUtils.isNotBlank(remoteFile), "remoteFile must not be blank")
        sshActions.add(new UploadSshAction(localFile, remoteFile, permissions))
    }

    @TaskAction
    void execute() {
        def jsch = new JSch()
        Session session = null
        try {
            session = jsch.getSession(
                remote.getUser(),
                remote.getHost(),
                remote.getPort()
            )
            session.setPassword(TaskUtils.getString(remote.getPassword(), "").getBytes(StandardCharsets.UTF_8))
            session.setConfig('StrictHostKeyChecking', 'no')
            session.connect(10_000)
            SshActionExecutor executor = new SshActionExecutor(session, logger)
            for (SshAction action : sshActions) {
                executor.executeSshAction(action)
            }
        } finally {
            session?.disconnect()
        }
    }
}
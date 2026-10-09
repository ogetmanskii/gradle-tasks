package buildlogic.task

import buildlogic.spec.RemoteHostSpec
import buildlogic.utils.TaskUtils
import buildlogic.utils.Validate
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.apache.commons.lang3.StringUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import java.nio.charset.StandardCharsets
import java.util.function.Consumer

class SshTask extends DefaultTask {

    @Input RemoteHostSpec remote

    private List<Consumer<Session>> sessionActions = []

    void exec(String command) {
        Validate.isTrue(StringUtils.isNotBlank(command), "command must not be blank")
        def thisTask = this
        sessionActions.add({ session -> thisTask.runCommand(session, command) })
    }

    void upload(String localFile, String remoteFile, int permissions) {
        Validate.isTrue(StringUtils.isNotBlank(localFile), "localFile must not be blank")
        Validate.isTrue(StringUtils.isNotBlank(remoteFile), "remoteFile must not be blank")
        def thisTask = this
        sessionActions.add({ session -> thisTask.uploadFile(session, localFile, remoteFile, permissions) })
    }

    void doWithSession(Consumer<Session> sessionAction) {
        Validate.isTrue(sessionAction != null, "sessionAction must not be null")
        sessionActions.add(sessionAction)
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

            for (def action : sessionActions) {
                action(session)
            }
        } finally {
            session?.disconnect()
        }
    }

    private void runCommand(Session session, String cmd) {
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

    private void uploadFile(Session session, String localFile, String remoteFile, int permissions) {
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
package gr.utils.task

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import gr.utils.spec.RemoteHostSpec
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import java.nio.charset.StandardCharsets

abstract class SshTask extends DefaultTask {

    @Input RemoteHostSpec remote
    @Input List<Object> commands = []

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

            for (def cmd in commands) {
                runCommand(session, TaskUtils.getString(cmd, null))
            }
        } finally {
            session?.disconnect()
        }
    }

    private static void runCommand(Session session, String cmd) {
        ChannelExec channel = session.openChannel('exec') as ChannelExec
        channel.setCommand(cmd)
        channel.setInputStream(null)
        def errStream = channel.getErrStream()
        def out = channel.getInputStream()
        channel.connect()
        Thread outThread = Thread.start('ssh-stdout') {
            out.withStream { InputStream is ->
                copyTo(is, System.out)
            }
        }
        Thread errThread = Thread.start('ssh-stderr') {
            errStream.withStream { InputStream is ->
                copyTo(is, System.err)
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

    private static void copyTo(InputStream is, OutputStream os) {
        byte[] buf = new byte[4096]
        int n
        while ((n = is.read(buf)) != -1) {
            os.write(buf, 0, n)
            os.flush()
        }
    }
}
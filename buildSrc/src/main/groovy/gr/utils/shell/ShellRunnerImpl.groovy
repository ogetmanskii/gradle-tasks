package gr.utils.shell

import gr.utils.exception.InvalidExitCodeException
import gr.utils.exception.TimeoutException
import gr.utils.spec.CommandSpec
import gr.utils.ShellRunner
import gr.utils.exception.UnhealthyException
import gr.utils.spec.HealthcheckSpec
import org.gradle.api.logging.Logger

import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.time.Duration
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class ShellRunnerImpl implements ShellRunner {

    static final ShellRunner INSTANCE = new ShellRunnerImpl()

    // Сколько ждать после SIGTERM прежде чем слать SIGKILL.
    private static final Duration KILL_GRACE = Duration.ofSeconds(5)

    @Override
    Integer run(Logger log, CommandSpec spec) throws TimeoutException, InvalidExitCodeException {
        if (spec == null || spec.command == null || spec.command.isBlank()) {
            throw new IllegalArgumentException("command must not be empty")
        }

        ProcessBuilder pb = buildProcessBuilder(spec)

        Process process
        try {
            process = pb.start()
        } catch (IOException e) {
            throw new InvalidExitCodeException(-1, "Failed to start process: " + e.getMessage())
        }

        try {
            process.getOutputStream().close()
        } catch (IOException ignored) {

        }

        // Очереди строк из stdout/stderr. Кладём и «фрагменты» при переполнении строки.
        BlockingQueue<String> stdoutLines = new LinkedBlockingQueue<>()
        BlockingQueue<String> stderrLines = new LinkedBlockingQueue<>()

        Thread stdoutReader = startReader(log, process.getInputStream(), stdoutLines)
        Thread stderrReader = startReader(log, process.getErrorStream(), stderrLines)

        Integer exitCode = null
        try {
            exitCode = awaitProcess(process, spec, stdoutLines, stderrLines)
        } finally {
            stdoutReader.interrupt()
            stderrReader.interrupt()
        }

        if (exitCode == null) {
            // Отсоединились — процесс живёт дальше.
            return null
        }

        List<Integer> valid = spec.validExitCodes
        if (valid == null) {
            if (exitCode != 0) {
                throw new InvalidExitCodeException(exitCode, "Non-zero exit code: " + exitCode)
            }
        } else if (!valid.contains(exitCode)) {
            throw new InvalidExitCodeException(exitCode,
                "Exit code " + exitCode + " not in valid list " + valid)
        }
        return exitCode
    }

    @Override
    boolean healthcheck(Logger log, HealthcheckSpec spec) throws UnhealthyException {
        if (spec == null || spec.command == null)
            throw new IllegalArgumentException("commandSpec must not be null")
        if (spec.intervalSeconds == null || spec.intervalSeconds <= 0)
            throw new IllegalArgumentException("intervalSeconds must be > 0")
        if (spec.timeoutSeconds == null || spec.timeoutSeconds <= 0)
            throw new IllegalArgumentException("timeoutSeconds must be > 0")

        long deadline = System.nanoTime() + (long) (spec.timeoutSeconds * 1_000_000_000L)
        Exception lastError = null

        while (System.nanoTime() < deadline) {
            try {
                Integer code = run(log, spec.command)
                if (code == null || code == 0) return true
                lastError = new InvalidExitCodeException(code, "Healthcheck exit code: " + code)
            } catch (TimeoutException | InvalidExitCodeException e) {
                lastError = e
            }

            long remaining = deadline - System.nanoTime()
            if (remaining <= 0) break
            long sleepNanos = Math.min((long) (spec.intervalSeconds * 1_000_000_000L), remaining)
            try {
                TimeUnit.NANOSECONDS.sleep(sleepNanos)
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt()
                throw new UnhealthyException("Healthcheck interrupted", ie)
            }
        }
        throw new UnhealthyException(
            "Healthcheck failed within " + spec.timeoutSeconds + "s", lastError)
    }

    private static ProcessBuilder buildProcessBuilder(CommandSpec spec) {
        boolean isWindows = System.getProperty("os.name", "").toLowerCase().contains("win")
        List<String> cmd = new ArrayList<>()

        if (isWindows) {
            cmd.add("cmd")
            cmd.add("/C")
            cmd.add(spec.command)
        } else {
            // setsid делает дочерний процесс лидером новой process group,
            // что позволяет затем убивать всё дерево через kill -<pgid>.
            cmd.add("setsid")
            cmd.add("sh")
            cmd.add("-c")
            cmd.add(spec.command)
        }

        ProcessBuilder pb = new ProcessBuilder(cmd)

        if (spec.workDir != null && !spec.workDir.isBlank()) {
            pb.directory(Paths.get(spec.workDir).toFile())
        }
        if (spec.env != null && !spec.env.isEmpty()) {
            pb.environment().putAll(spec.env)
        }
        pb.redirectErrorStream(false)
        return pb
    }

    private static Integer awaitProcess(Process process,
                                        CommandSpec spec,
                                        BlockingQueue<String> stdoutLines,
                                        BlockingQueue<String> stderrLines) throws TimeoutException {

        long timeoutNanos = spec.timeoutSeconds != null
            ? (long) (spec.timeoutSeconds * 1_000_000_000L)
            : Long.MAX_VALUE
        long detachNanos = spec.detachAfterSeconds != null
            ? (long) (spec.detachAfterSeconds * 1_000_000_000L)
            : Long.MAX_VALUE

        // Ищем подстроку в обеих очередях. Строки уже разбиты, фраза
        // может пересекаться со склейкой фрагментов — см. класс PhraseMatcher.
        PhraseMatcher matcher = spec.detachAfterPhrase != null
            ? new PhraseMatcher(spec.detachAfterPhrase)
            : null

        long start = System.nanoTime()

        while (true) {
            if (!process.isAlive()) {
                try {
                    return process.waitFor()
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt()
                    killTree(process)
                    throw new TimeoutException("Interrupted while waiting", e)
                }
            }

            long elapsed = System.nanoTime() - start

            if (elapsed >= timeoutNanos) {
                killTree(process)
                throw new TimeoutException(
                    "Command timed out after " + spec.timeoutSeconds + "s: " + spec.command)
            }

            if (elapsed >= detachNanos) {
                // Detach по времени: процесс НЕ убиваем.
                return null
            }

            if (matcher != null) {
                boolean found = drainAndMatch(stdoutLines, matcher)
                    | drainAndMatch(stderrLines, matcher)
                if (found) {
                    // Detach по фразе: процесс НЕ убиваем.
                    return null
                }
            } else {
                // Просто сливаем очереди, чтобы продюсеры не блокировались.
                //noinspection GroovyEmptyStatementBody
                while (stdoutLines.poll() != null) {
                }
                //noinspection GroovyEmptyStatementBody
                while (stderrLines.poll() != null) {
                }
            }

            try {
                Thread.sleep(50)
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt()
                killTree(process)
                throw new TimeoutException("Interrupted", e)
            }
        }
    }

    private static boolean drainAndMatch(BlockingQueue<String> q, PhraseMatcher matcher) {
        String s
        boolean found = false
        while ((s = q.poll()) != null) {
            if (!found && matcher.feed(s)) found = true
        }
        return found
    }

    private static void killTree(Process process) {
        long pid = process.pid()
        boolean isWindows = System.getProperty("os.name", "").toLowerCase().contains("win")

        if (isWindows) {
            // taskkill /T — убить дерево, /F — force.
            runQuietly("taskkill", "/F", "/T", "/PID", Long.toString(pid))
            // На случай если taskkill не сработал:
            process.destroyForcibly()
            return
        }

        // Unix: пробуем мягко (SIGTERM) всей process group.
        // Благодаря setsid pgid == pid.
        runQuietly("kill", "-TERM", "-" + pid)

        // Ждём grace-период, потом добиваем SIGKILL.
        long deadline = System.nanoTime() + KILL_GRACE.toNanos()
        while (System.nanoTime() < deadline) {
            if (!process.isAlive()) return
            try {
                Thread.sleep(50)
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt()
                break
            }
        }

        runQuietly("kill", "-KILL", "-" + pid)
        process.destroyForcibly()
    }

    private static void runQuietly(String... args) {
        try {
            Process p = new ProcessBuilder(args)
                .redirectErrorStream(true)
                .start()
            // Слить вывод, чтобы не забивать буфер.
            try (InputStream is = p.getInputStream()) {
                byte[] buf = new byte[1024]
                //noinspection GroovyEmptyStatementBody
                while (is.read(buf) != -1) {
                }
            }
            p.waitFor(1, TimeUnit.SECONDS)
        } catch (IOException | InterruptedException ignored) {
            if (ignored instanceof InterruptedException) Thread.currentThread().interrupt()
        }
    }

    private static Thread startReader(
        Logger log,
        InputStream is,
        BlockingQueue<String> sink) {

        Thread t = new Thread(() -> {
            try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line
                while ((line = br.readLine()) != null) {
                    log.lifecycle(line)
                    sink.offer(line)
                }
            } catch (IOException ignored) {
            }
        }, "shellrunner-stdout")
        t.setDaemon(true);
        t.start()
        return t
    }

    /**
     * Ищет phrase в потоке строк. Т.к. длинная строка может быть разбита
     * на несколько сегментов, между соседними сегментами сохраняем
     * скользящий хвост длиной phrase.length()-1.
     */
    private static final class PhraseMatcher {
        private final String phrase
        private final char[] tail   // хвост предыдущего сегмента
        private int tailLen = 0

        PhraseMatcher(String phrase) {
            this.phrase = phrase
            this.tail = new char[Math.max(0, phrase.length() - 1)]
        }

        /** Возвращает true, если phrase найдена с учётом границы чанков. */
        boolean feed(String segment) {
            if (phrase.isEmpty()) return true

            // Ищем в tail+segment
            int totalLen = tailLen + segment.length()
            if (totalLen < phrase.length()) {
                // Мало данных — просто накапливаем хвост.
                updateTail(segment)
                return false
            }

            // Собираем временный буфер только когда это реально нужно.
            StringBuilder sb = new StringBuilder(totalLen)
            sb.append(tail, 0, tailLen).append(segment)
            int idx = sb.indexOf(phrase)
            updateTail(segment)
            return idx >= 0
        }

        private void updateTail(String segment) {
            if (tail.length == 0) return
            // Собираем «хвост»: последние tail.length символов из (tail + segment)
            StringBuilder sb = new StringBuilder(tailLen + segment.length())
            sb.append(tail, 0, tailLen).append(segment)
            int from = Math.max(0, sb.length() - tail.length)
            tailLen = sb.length() - from
            sb.getChars(from, sb.length(), tail, 0)
        }
    }
}

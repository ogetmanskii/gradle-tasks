package gr.utils

import org.gradle.api.logging.Logger

class ProcessUtils {
    static boolean hasActiveProcess(Logger log, String fullPath) {
        boolean result = ProcessHandle.allProcesses().anyMatch {
            fullPath == it.info().command().orElse(null)
        }
        if (result) {
            log.lifecycle("There is active process: ${fullPath}")
        } else {
            log.lifecycle("No active process: ${fullPath}")
        }
        return result
    }

    static void terminateProcess(Logger log, String fullPath) {
        boolean terminated = false
        for (def h in ProcessHandle.allProcesses()) {
            if (fullPath == h.info().command().orElse(null)) {
                log.lifecycle("Terminate PID ${h.pid()}")
                h.destroy()
                terminated = true
            }
        }
        if (!terminated) {
            log.lifecycle("No active process: ${fullPath}")
        }
    }
}

package gr.utils.spec

import gr.utils.ClosureUtils
import gr.utils.OsUtils
import org.apache.commons.lang3.StringUtils

class ServiceSpec {

    String name

    void name(String s) {
        name = s
    }

    CommandSpec upCommand

    void upCommand(@DelegatesTo(CommandSpec) Closure c) {
        upCommand = ClosureUtils.applyClosure(c, new CommandSpec())
    }

    CommandSpec downCommand

    void downCommand(@DelegatesTo(CommandSpec) Closure c) {
        downCommand = ClosureUtils.applyClosure(c, new CommandSpec())
    }

    String imageFullPath

    void imageFullPath(String s) {
        Objects.requireNonNull(s)
        if (OsUtils.IS_WINDOWS && s.contains("/")) {
            throw new IllegalArgumentException("imageFullPath must not contain / symbol for Windows environments!")
        }
        if (!(new File(s).exists())) {
            throw new IllegalArgumentException("File does not exist: " + s)
        }
        imageFullPath = s
    }

    HealthcheckSpec healthcheck

    void healthcheck(@DelegatesTo(HealthcheckSpec) Closure c) {
        healthcheck = ClosureUtils.applyClosure(c, new HealthcheckSpec())
    }

    void validate() {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name must not be blank")
        }
        if (name == "infra") {
            throw new IllegalArgumentException("name must not be \"infra\"")
        }
        Objects.requireNonNull(upCommand, "upCommand must not be null")
        if (downCommand != null) {
            downCommand.validate()
        }
        if (healthcheck != null) {
            healthcheck.validate()
        }
    }
}

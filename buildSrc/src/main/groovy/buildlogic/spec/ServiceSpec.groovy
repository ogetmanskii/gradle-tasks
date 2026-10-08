package buildlogic.spec

import buildlogic.utils.ClosureUtils
import buildlogic.utils.OsUtils
import org.apache.commons.lang3.StringUtils

class ServiceSpec {

    private String name
    private boolean required = true
    private CommandSpec upCommand
    private CommandSpec downCommand
    private String imageFullPath
    private HealthcheckSpec healthcheck

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

    void name(String s) {
        name = s
    }

    void upCommand(@DelegatesTo(CommandSpec) Closure c) {
        upCommand = ClosureUtils.applyClosure(c, new CommandSpec())
        upCommand.validate()
    }

    void downCommand(@DelegatesTo(CommandSpec) Closure c) {
        downCommand = ClosureUtils.applyClosure(c, new CommandSpec())
        downCommand.validate()
    }

    void required(boolean v) {
        required = v
    }

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

    String getName() {
        return name
    }

    CommandSpec getUpCommand() {
        return upCommand
    }

    CommandSpec getDownCommand() {
        return downCommand
    }

    String getImageFullPath() {
        return imageFullPath
    }

    HealthcheckSpec getHealthcheck() {
        return healthcheck
    }

    boolean isRequired() {
        return required
    }

    void healthcheck(@DelegatesTo(HealthcheckSpec) Closure c) {
        healthcheck = ClosureUtils.applyClosure(c, new HealthcheckSpec())
        healthcheck.validate()
    }
}

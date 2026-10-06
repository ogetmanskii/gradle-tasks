package gr.utils.spec

import gr.utils.ClosureUtils

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
        imageFullPath = s
    }

    HealthcheckSpec healthcheck

    void healthcheck(@DelegatesTo(HealthcheckSpec) Closure c) {
        healthcheck = ClosureUtils.applyClosure(c, new HealthcheckSpec())
    }
}

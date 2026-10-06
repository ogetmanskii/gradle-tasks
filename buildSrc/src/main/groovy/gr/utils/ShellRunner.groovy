package gr.utils

import gr.utils.exception.InvalidExitCodeException
import gr.utils.exception.TimeoutException
import gr.utils.exception.UnhealthyException
import gr.utils.spec.CommandSpec
import gr.utils.spec.HealthcheckSpec
import org.gradle.api.logging.Logger

import javax.annotation.Nullable

interface ShellRunner {

    @Nullable Integer run(Logger log, CommandSpec spec) throws TimeoutException, InvalidExitCodeException;

    @Nullable boolean healthcheck(Logger log, HealthcheckSpec spec) throws UnhealthyException;

}

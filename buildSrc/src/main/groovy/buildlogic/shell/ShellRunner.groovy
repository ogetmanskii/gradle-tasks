package buildlogic.shell

import buildlogic.exception.InvalidExitCodeException
import buildlogic.exception.TimeoutException
import buildlogic.exception.UnhealthyException
import buildlogic.spec.CommandSpec
import buildlogic.spec.HealthcheckSpec
import org.gradle.api.logging.Logger

import javax.annotation.Nullable

interface ShellRunner {

    @Nullable Integer run(Logger log, CommandSpec spec) throws TimeoutException, InvalidExitCodeException;

    @Nullable boolean healthcheck(Logger log, HealthcheckSpec spec) throws UnhealthyException;

}

package buildlogic.shell

import buildlogic.exception.InvalidExitCodeException
import buildlogic.exception.TimeoutException
import buildlogic.exception.UnhealthyException
import buildlogic.spec.CommandSpec
import buildlogic.spec.HealthcheckSpec

import javax.annotation.Nullable

interface ShellRunner {

    @Nullable
    Integer run(CommandSpec spec) throws TimeoutException, InvalidExitCodeException;

    @Nullable
    boolean healthcheck(HealthcheckSpec spec) throws UnhealthyException;

}

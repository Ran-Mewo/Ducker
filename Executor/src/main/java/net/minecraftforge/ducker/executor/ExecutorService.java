package net.minecraftforge.ducker.executor;

import net.minecraftforge.ducker.configuration.DuckerConfiguration;
import net.minecraftforge.ducker.mixin.DuckerExecutorMixinService;
import net.minecraftforge.ducker.mixin.DuckerMixinApplier;
import net.minecraftforge.ducker.mixin.DuckerMixinBootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Executes
 */
public final class ExecutorService
{

    private static final Logger LOGGER = LogManager.getLogger();
    private static final ExecutorService INSTANCE = new ExecutorService();

    public static ExecutorService getInstance()
    {
        return INSTANCE;
    }

    private ExecutorService()
    {
    }

    public void execute(final DuckerConfiguration duckerConfiguration)
    {
        LOGGER.info("Bootstrapping mixin runtime");
        DuckerExecutorMixinService duckerExecutorMixinService = DuckerMixinBootstrap.bootstrapMixin(duckerConfiguration);

        LOGGER.info("Applying mixins");
        DuckerMixinApplier.applyMixin(duckerConfiguration, duckerExecutorMixinService);
    }
}

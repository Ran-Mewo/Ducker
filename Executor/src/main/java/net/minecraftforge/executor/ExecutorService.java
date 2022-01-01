package net.minecraftforge.executor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.service.MixinService;

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

    public void execute()
    {
        LOGGER.info("Executing ducker runtime");

        MixinService.getService();
    }
}

package net.minecraftforge.ducker.executor;

import com.google.common.reflect.ClassPath;
import net.minecraftforge.ducker.mixin.DuckerExecutorMixinService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;

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

        IMixinService mixinService = MixinService.getService();
        if (!(mixinService instanceof DuckerExecutorMixinService duckerExecutorMixinService))
            throw new IllegalStateException("Mixin did not load the Ducker mixin executor.");

        duckerExecutorMixinService.onInit(new DuckerExecutor());

        ClassPath classPath = ClassPath.from(duckerExecutorMixinService.getClassPathProvider().getUrlClassLoader());
    }
}

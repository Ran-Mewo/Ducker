package net.minecraftforge.ducker.executor;

import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import net.minecraftforge.ducker.configuration.DuckerConfiguration;
import net.minecraftforge.ducker.mixin.DuckerExecutor;
import net.minecraftforge.ducker.mixin.DuckerExecutorMixinService;
import net.minecraftforge.ducker.mixin.DuckerMixinApplier;
import net.minecraftforge.ducker.mixin.DuckerMixinBootstrap;
import net.minecraftforge.ducker.mixin.classes.IClassProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.MixinService;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Objects;

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

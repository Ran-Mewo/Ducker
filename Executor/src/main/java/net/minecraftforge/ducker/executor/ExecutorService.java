package net.minecraftforge.ducker.executor;

import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import net.minecraftforge.ducker.configuration.DuckerConfiguration;
import net.minecraftforge.ducker.mixin.DuckerExecutorMixinService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.IClassProcessor;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.MixinService;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

    @SuppressWarnings("UnstableApiUsage")
    public void execute(final DuckerConfiguration duckerConfiguration)
    {
        LOGGER.info("Executing ducker runtime");

        IMixinService mixinService = MixinService.getService();
        if (!(mixinService instanceof DuckerExecutorMixinService duckerExecutorMixinService))
            throw new IllegalStateException("Mixin did not load the Ducker mixin executor.");

        duckerConfiguration.getMixinSourcesClasspath()
          .forEach(source -> duckerExecutorMixinService.getPrimaryContainer().addResource(
            source, new File(source).toPath()
          ));

        duckerExecutorMixinService.onInit(new DuckerExecutor());
        duckerExecutorMixinService.getClassPathProvider().setup(toFullUris(duckerConfiguration), toRuntimeUris(duckerConfiguration));

        MixinBootstrap.init();

        try
        {
            ClassPath classPath = ClassPath.from(duckerExecutorMixinService.getClassPathProvider().getRuntimeClassPathClassLoader());
            classPath.getTopLevelClassesRecursive(duckerConfiguration.getRootNamespace()).forEach(classInfo -> {
                try
                {
                    final ClassNode classNode = duckerExecutorMixinService.getBytecodeProvider().getClassNode(classInfo.getName(), true);
                    processClassNode(duckerExecutorMixinService, classNode);
                    LOGGER.info("Processed {}", classNode.name);
                    final File file = duckerConfiguration.getClassWriter().writeClass(classNode);
                    LOGGER.info("Written class {}, to {}", classNode.name, file.getAbsolutePath());
                    duckerConfiguration.getDecompiler().decompile(file);
                    LOGGER.info("Decompiled class {}.", classNode.name);
                }
                catch (ClassNotFoundException | IOException e)
                {
                    e.printStackTrace();
                }
            });
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private boolean processClassNode(final DuckerExecutorMixinService duckerExecutorMixinService, final ClassNode classNode) {
        boolean processed = false;


        for (IClassProcessor processor : duckerExecutorMixinService.getProcessors()) {
            processed |= processor.processClass(ILaunchPluginService.Phase.AFTER, classNode, Type.getObjectType(classNode.name), "classloading");
        }

        return processed;
    }

    private URL[] toFullUris(final DuckerConfiguration duckerConfiguration) {
        final List<URI> runtime = Lists.newArrayList();
        runtime.addAll(duckerConfiguration.getTargetRuntimeClasspath().stream().map(File::new).map(File::toURI).toList());
        runtime.add(new File(duckerConfiguration.getTargetJar()).toURI());
        runtime.addAll(duckerConfiguration.getMixinSourcesClasspath().stream().map(File::new).map(File::toURI).toList());

        return runtime.stream()
                 .map(uri -> {
                     try
                     {
                         return uri.normalize().toURL();
                     }
                     catch (MalformedURLException e)
                     {
                         e.printStackTrace();
                     }
                     return null;
                 })
                 .filter(Objects::nonNull)
                 .toArray(URL[]::new);
    }

    private URL[] toRuntimeUris(final DuckerConfiguration duckerConfiguration) {
        final List<URI> runtime = Lists.newArrayList();
        runtime.addAll(duckerConfiguration.getTargetRuntimeClasspath().stream().map(File::new).map(File::toURI).toList());
        runtime.add(new File(duckerConfiguration.getTargetJar()).toURI());

        return runtime.stream()
          .map(uri -> {
              try
              {
                  return uri.normalize().toURL();
              }
              catch (MalformedURLException e)
              {
                  e.printStackTrace();
              }
              return null;
          })
          .filter(Objects::nonNull)
          .toArray(URL[]::new);
    }
}

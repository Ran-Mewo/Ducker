package net.minecraftforge.ducker.mixin;

import com.google.common.collect.Lists;
import net.minecraftforge.ducker.configuration.DuckerConfiguration;
import net.minecraftforge.ducker.mixin.classes.IClassProcessor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.MixinService;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Objects;

public class DuckerMixinBootstrap
{

    private DuckerMixinBootstrap()
    {
        throw new IllegalStateException("Can not instantiate an instance of: DuckerMixinBootstrap. This is a utility class");
    }

    public static DuckerExecutorMixinService bootstrapMixin(final DuckerConfiguration duckerConfiguration)
    {
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
        return duckerExecutorMixinService;
    }

    private static URL[] toFullUris(final DuckerConfiguration duckerConfiguration) {
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

    private static URL[] toRuntimeUris(final DuckerConfiguration duckerConfiguration) {
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

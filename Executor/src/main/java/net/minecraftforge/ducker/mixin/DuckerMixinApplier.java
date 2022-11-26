package net.minecraftforge.ducker.mixin;

import com.google.common.reflect.ClassPath;
import net.minecraftforge.ducker.configuration.DuckerConfiguration;
import net.minecraftforge.ducker.mixin.classes.IClassProcessor;
import net.minecraftforge.ducker.writer.IClassWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;

public class DuckerMixinApplier
{

    private static final Logger LOGGER = LogManager.getLogger();
    private DuckerMixinApplier()
    {
        throw new IllegalStateException("Can not instantiate an instance of: DuckerMixinApplier. This is a utility class");
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void applyMixin(final DuckerConfiguration duckerConfiguration, final DuckerExecutorMixinService duckerExecutorMixinService)
    {
        try
        {
            ClassPath classPath = ClassPath.from(duckerExecutorMixinService.getClassPathProvider().getRuntimeClassPathClassLoader());
            classPath.getTopLevelClassesRecursive(duckerConfiguration.getRootNamespace()).forEach(classInfo -> {
                try
                {
                    final ClassNode classNode = duckerExecutorMixinService.getBytecodeProvider().getClassNode(classInfo.getName(), true);
                    processClassNode(duckerExecutorMixinService, classNode);

                    classNode.sourceDebug = null;

                    LOGGER.info("Processed {}", classNode.name);
                    final IClassWriter.ClassWriterResult writerResult = duckerConfiguration.getClassWriter().writeClass(duckerConfiguration, classNode);
                    LOGGER.info("Written class {}, to {}, with {} additional files", classNode.name, writerResult.file().getAbsolutePath(), writerResult.additionalFiles().size());
                    duckerConfiguration.getDecompiler().decompile(writerResult.file(), writerResult.additionalFiles());
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

    private static boolean processClassNode(final DuckerExecutorMixinService duckerExecutorMixinService, final ClassNode classNode) {
        boolean processed = false;


        for (IClassProcessor processor : duckerExecutorMixinService.getProcessors()) {
            processed |= processor.processClass(classNode, Type.getObjectType(classNode.name), "classloading");
        }

        return processed;
    }

}

package net.minecraftforge.ducker.mixin;

import com.google.common.reflect.ClassPath;
import net.minecraftforge.ducker.configuration.DuckerConfiguration;
import net.minecraftforge.ducker.mixin.classes.IClassProcessor;
import net.minecraftforge.ducker.writer.IClassWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;

import java.io.IOException;
import java.util.List;

public class DuckerMixinApplier
{

    private static final Logger LOGGER = LogManager.getLogger(DuckerMixinApplier.class);
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
            for (final String pkg : duckerConfiguration.getExtractionPackages()) {
                classPath.getTopLevelClassesRecursive(pkg).forEach(classInfo -> {
                    try
                    {
                        final ClassNode classNode = duckerExecutorMixinService.getBytecodeProvider().getClassNode(classInfo.getName(), true);
                        System.out.println("We're now at " + classNode.name);

                        for(InnerClassNode innerNode : classNode.innerClasses) {
                            System.out.println("Also processing inner class: " + innerNode.name);
                            final ClassNode innerNodeClazz = duckerExecutorMixinService.getBytecodeProvider().getClassNode(innerNode.name, true);

                            processClass(duckerConfiguration, duckerExecutorMixinService, classInfo, innerNodeClazz);
                        }

                        processClass(duckerConfiguration, duckerExecutorMixinService, classInfo, classNode);
                    }
                    catch (ClassNotFoundException | IOException e)
                    {
                        e.printStackTrace();
                    }
                });
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void processClass(DuckerConfiguration duckerConfiguration, DuckerExecutorMixinService duckerExecutorMixinService, ClassPath.ClassInfo classInfo, ClassNode classNode) {
        if (processClassNode(duckerExecutorMixinService, classNode)) {
            classNode.sourceDebug = null;

            System.out.println("Processed " + classNode.name);
            LOGGER.info("Processed {}", classNode.name);
            final IClassWriter.ClassWriterResult writerResult = duckerConfiguration.getClassWriter().writeClass(duckerConfiguration, classNode, duckerExecutorMixinService);
            System.out.println("Written class " + classInfo.getName());
            LOGGER.info("Written class {}, to {}, with {} additional files", classNode.name, writerResult.file().getAbsolutePath(), writerResult.additionalFiles().size());
            duckerConfiguration.getDecompiler().decompile(writerResult.file(), writerResult.additionalFiles());
            System.out.println("Decompiled " + classNode.name);
            LOGGER.info("Decompiled class {}.", classNode.name);
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

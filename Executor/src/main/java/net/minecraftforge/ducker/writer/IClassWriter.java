package net.minecraftforge.ducker.writer;

import net.minecraftforge.ducker.configuration.DuckerConfiguration;
import net.minecraftforge.ducker.mixin.DuckerExecutorMixinService;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.util.Set;

public interface IClassWriter
{
    record ClassWriterResult(File file, Set<File> additionalFiles) {}

    ClassWriterResult writeClass(final DuckerConfiguration configuration, final ClassNode classNode, DuckerExecutorMixinService service);
}

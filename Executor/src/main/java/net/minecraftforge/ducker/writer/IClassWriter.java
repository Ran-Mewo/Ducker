package net.minecraftforge.ducker.writer;

import net.minecraftforge.ducker.configuration.DuckerConfiguration;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;

public interface IClassWriter
{

    File writeClass(final DuckerConfiguration configuration, final ClassNode classNode);
}

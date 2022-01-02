package net.minecraftforge.ducker.writer;

import org.objectweb.asm.tree.ClassNode;

import java.io.File;

public interface IClassWriter
{

    File writeClass(final ClassNode classNode);
}

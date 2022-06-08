package net.minecraftforge.ducker.transformers;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

public interface IResultsTransformer
{
    ClassVisitor transform(ClassNode node, ClassVisitor previous);
}

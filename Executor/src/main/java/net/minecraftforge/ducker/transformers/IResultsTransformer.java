package net.minecraftforge.ducker.transformers;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

import java.util.Set;

public interface IResultsTransformer
{

    ClassVisitor transform(ClassNode node, ClassVisitor previous);
}

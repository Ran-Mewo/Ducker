package net.minecraftforge.ducker.transformers;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.util.asm.ASM;

import javax.naming.PartialResultException;

public class SourceMapStrippingTransformer implements IResultsTransformer
{
    @Override
    public ClassVisitor transform(ClassNode node, ClassVisitor previous)
    {
        return new SourceMapStrippingVisitor(previous);
    }

    private static final class SourceMapStrippingVisitor extends ClassVisitor
    {
        public SourceMapStrippingVisitor(final ClassVisitor cv)
        {
            super(ASM.API_VERSION, cv);
        }

        @Override
        public void visitAttribute(final Attribute attribute)
        {
            if (attribute.type.equals("SourceFile"))
            {
                return;
            }
            super.visitAttribute(attribute);
        }
    }

}

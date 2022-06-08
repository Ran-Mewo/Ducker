package net.minecraftforge.ducker.transformers;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.util.asm.ASM;

public class SourceMapStrippingTransformer implements IResultsTransformer
{
    @Override
    public ClassVisitor transform(final ClassNode node, final ClassVisitor previous)
    {
        return new SourceMapStrippingVisitor(previous);
    }

    private final class SourceMapStrippingVisitor extends ClassVisitor
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

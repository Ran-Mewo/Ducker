package net.minecraftforge.ducker.transformers;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.util.asm.ASM;

public class MixinAnnotationStripper implements IMixinMethodAwareTransformer
{
    @Override
    public ClassVisitor transform(ClassNode node, ClassVisitor previous)
    {
        return new MixinAnnotationStrippingClassVisitor(ASM.API_VERSION, previous);
    }

    private static final class MixinAnnotationStrippingClassVisitor extends ClassVisitor {

        public MixinAnnotationStrippingClassVisitor(final int api, final ClassVisitor classVisitor)
        {
            super(api, classVisitor);
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String[] exceptions)
        {
            final MethodVisitor innerVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
            return new MixinStrippingMethodVisitor(api, innerVisitor);
        }
    }

    private static final class MixinStrippingMethodVisitor extends MethodVisitor {

        public MixinStrippingMethodVisitor(final int api, final MethodVisitor methodVisitor)
        {
            super(api, methodVisitor);
        }

        @Override
        public AnnotationVisitor visitAnnotationDefault()
        {
            return super.visitAnnotationDefault();
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible)
        {
            if (descriptor.contains("MixinMerged"))
                return null;

            return super.visitAnnotation(descriptor, visible);
        }
    }
}

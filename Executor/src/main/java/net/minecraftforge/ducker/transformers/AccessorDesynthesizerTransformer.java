package net.minecraftforge.ducker.transformers;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.util.asm.ASM;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AccessorDesynthesizerTransformer implements IMixinMethodAwareTransformer
{
    @Override
    public ClassVisitor transform(ClassNode node, ClassVisitor previous)
    {
        final var toChange = node.methods.stream()
                .filter(AccessorDesynthesizerTransformer::isAccessor)
                .map(mn -> mn.name + mn.desc)
                .collect(Collectors.toSet());

        System.out.println("Methods to change: " + toChange);
        return new ClassVisitor(ASM.API_VERSION, previous) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                return super.visitMethod(toChange.contains(name + descriptor) ? (access & (~Opcodes.ACC_SYNTHETIC)) : access, name, descriptor, signature, exceptions);
            }
        };
    }

    public static boolean isAccessor(MethodNode methodNode) {
        if (methodNode.visibleAnnotations == null) return false;
        return methodNode.visibleAnnotations.stream()
                .anyMatch(a -> a.desc.equals("Lorg/spongepowered/asm/mixin/gen/Accessor;") || a.desc.equals("Lorg/spongepowered/asm/mixin/gen/Invoker;"));
    }

}

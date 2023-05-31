package net.minecraftforge.ducker.transformers;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.util.asm.ASM;

public class OverwriteFixerTransformer implements IResultsTransformer {
    @Override
    public ClassVisitor transform(ClassNode node, ClassVisitor previous) {
        return new ClassVisitor(ASM.API_VERSION, previous) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(ASM.API_VERSION, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                    private boolean isOverwritten;
                    private String mixinName;
                    @Override
                    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                        if (descriptor.equals("Lorg/spongepowered/asm/mixin/Overwrite;")) {
                            isOverwritten = true;
                        } else if (descriptor.equals("Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;")) {
                            return new AnnotationVisitor(ASM.API_VERSION, super.visitAnnotation(descriptor, visible)) {
                                @Override
                                public void visit(String name, Object value) {
                                    if (name.equals("mixin")) {
                                        mixinName = ((String) value).replace('.', '/');
                                    }
                                    super.visit(name, value);
                                }
                            };
                        }
                        return super.visitAnnotation(descriptor, visible);
                    }

                    @Override
                    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
                        if (isOverwritten) {
                            final Object[] newLocals = new Object[local.length];
                            for (int i = 0; i < local.length; i++) {
                                final Object l = local[i];
                                if (l instanceof String && l.equals(mixinName)) {
                                    newLocals[i] = node.name;
                                } else {
                                    newLocals[i] = l;
                                }
                            }

                            final Object[] newStack = new Object[stack.length];
                            for (int i = 0; i < stack.length; i++) {
                                final Object l = stack[i];
                                if (l instanceof String && l.equals(mixinName)) {
                                    newStack[i] = node.name;
                                } else {
                                    newStack[i] = l;
                                }
                            }

                            super.visitFrame(type, numLocal, newLocals, numStack, newStack);
                        } else {
                            super.visitFrame(type, numLocal, local, numStack, stack);
                        }
                    }
                };
            }
        };
    }
}

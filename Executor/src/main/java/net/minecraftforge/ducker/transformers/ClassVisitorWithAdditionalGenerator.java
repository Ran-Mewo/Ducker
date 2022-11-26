package net.minecraftforge.ducker.transformers;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.util.asm.ASM;

import java.util.Set;

public abstract class ClassVisitorWithAdditionalGenerator extends ClassVisitor {

    public ClassVisitorWithAdditionalGenerator(ClassVisitor classVisitor) {
        super(ASM.API_VERSION, classVisitor);
    }

    public abstract Set<ClassNode> getAdditionalClasses();
}

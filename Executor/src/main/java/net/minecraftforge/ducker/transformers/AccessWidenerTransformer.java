package net.minecraftforge.ducker.transformers;

import net.fabricmc.accesswidener.AccessWidener;
import net.fabricmc.accesswidener.AccessWidenerClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.util.asm.ASM;

/**
 * Transformer that applies access wideners to classes.
 */
public class AccessWidenerTransformer implements IResultsTransformer {
    
    private final AccessWidener accessWidener;
    
    public AccessWidenerTransformer(AccessWidener accessWidener) {
        this.accessWidener = accessWidener;
    }
    
    @Override
    public ClassVisitor transform(ClassNode node, ClassVisitor previous) {
        // Only apply the access widener if this class is a target
        if (accessWidener.getTargets().contains(node.name.replace('/', '.'))) {
            return AccessWidenerClassVisitor.createClassVisitor(ASM.API_VERSION, previous, accessWidener);
        }
        
        // Otherwise, just pass through
        return previous;
    }
}

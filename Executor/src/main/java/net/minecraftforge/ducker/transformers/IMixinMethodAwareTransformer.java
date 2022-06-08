package net.minecraftforge.ducker.transformers;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public interface IMixinMethodAwareTransformer extends IResultsTransformer
{
    default List<MethodNode> getMixinMethods(final ClassNode node) {
        return node.methods.stream()
          .filter(methodNode -> methodNode.getClass().getPackageName().contains("spongepowered")) //We need all those special mixin methods :D
          .toList();
    }
}

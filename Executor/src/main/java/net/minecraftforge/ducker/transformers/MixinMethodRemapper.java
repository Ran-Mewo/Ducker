package net.minecraftforge.ducker.transformers;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;

import java.util.Map;
import java.util.stream.Collectors;

public class MixinMethodRemapper implements IMixinMethodAwareTransformer
{
    @Override
    public ClassVisitor transform(ClassNode node, ClassVisitor previous)
    {
        final Map<String, String> methodRemapMap = getMixinMethods(node).stream()
          .collect(Collectors.toMap(
            methodNode -> node.name + "." + methodNode.name + methodNode.desc,
            methodNode -> "ducker$$" + methodNode.name
          ));

        final SimpleRemapper simpleRemapper = new SimpleRemapper(methodRemapMap);
        return new ClassRemapper(previous, simpleRemapper);
    }
}

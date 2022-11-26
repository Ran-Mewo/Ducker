package net.minecraftforge.ducker.transformers;

import com.google.common.collect.Maps;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MixinMethodRemapper implements IMixinMethodAwareTransformer
{
    @Override
    public ClassVisitor transform(ClassNode node, ClassVisitor previous)
    {
        record MethodIdentification(String name, String descriptor) {}
        final Map<MethodIdentification, String> nameByIdentification = node.methods.stream()
                .collect(Collectors.toMap(methodNode -> new MethodIdentification(methodNode.name, methodNode.desc), methodNode -> methodNode.name));

        final Map<String, AtomicInteger> offsetByMethodName = Maps.newHashMap();

        final Map<String, String> methodRemapMap = getMixinMethods(node).stream()
          .collect(Collectors.toMap(
            methodNode -> node.name + "." + methodNode.name + methodNode.desc,
            methodNode -> {
                final String mixinName = methodNode.name.substring(methodNode.name.lastIndexOf("$") + 1);
                final MethodIdentification methodIdentification = new MethodIdentification(mixinName, methodNode.desc);
                if (!nameByIdentification.containsKey(methodIdentification))
                    return mixinName;

                if (!offsetByMethodName.containsKey(mixinName))
                    offsetByMethodName.put(mixinName, new AtomicInteger(0));

                final int offset = offsetByMethodName.get(mixinName).getAndIncrement();
                return mixinName + "$" + offset;
            }
          ));

        final SimpleRemapper simpleRemapper = new SimpleRemapper(methodRemapMap);
        return new ClassRemapper(previous, simpleRemapper);
    }
}

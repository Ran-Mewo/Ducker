package net.minecraftforge.ducker.transformers;

import com.google.common.collect.Maps;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.util.asm.MethodNodeEx;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MixinMethodRemapperAndPrivatizer implements IMixinMethodAwareTransformer
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
                if (methodNode instanceof MethodNodeEx methodNodeEx) {
                    final String mixinName = methodNodeEx.getOriginalName();
                    if (Objects.equals(mixinName, methodNode.name)) {
                        return methodNode.name;
                    }

                    final MethodIdentification methodIdentification = new MethodIdentification(mixinName, methodNode.desc);
                    if (!nameByIdentification.containsKey(methodIdentification))
                        return mixinName;

                    final String prefixedMixinName = mixinName + "For" + methodNodeEx.getOwner().getName();

                    if (!offsetByMethodName.containsKey(prefixedMixinName))
                        offsetByMethodName.put(prefixedMixinName, new AtomicInteger(0));

                    final int offset = offsetByMethodName.get(prefixedMixinName).getAndIncrement();
                    if (offset == 0) {
                        return prefixedMixinName;
                    } else {
                        return prefixedMixinName + (offset - 1);
                    }
                }

                //Path will likely be never taken!
                return methodNode.name;
            }
          ));

        for (final var methodNode : getMixinMethods(node)) {
            if (methodNode instanceof MethodNodeEx methodNodeEx && !methodNode.name.equals(methodNodeEx.getOriginalName())) {
                methodNode.access &= ~Opcodes.ACC_PUBLIC; //Remove public access
            }
        }

        final SimpleRemapper simpleRemapper = new SimpleRemapper(methodRemapMap);
        return new ClassRemapper(previous, simpleRemapper);
    }
}

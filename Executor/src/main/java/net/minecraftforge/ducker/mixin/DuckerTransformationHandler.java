package net.minecraftforge.ducker.mixin;

import com.google.common.base.Preconditions;
import net.minecraftforge.ducker.mixin.classes.IClassProcessor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.MixinLaunchPluginLegacy;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.ISyntheticClassRegistry;

import java.lang.reflect.Field;

public class DuckerTransformationHandler implements IClassProcessor
{

    public static final String COMPUTING_FRAMES = "computing_frames";
    private IMixinTransformerFactory transformerFactory;
    private IMixinTransformer transformer;
    private ISyntheticClassRegistry registry;

    void offer(IMixinTransformerFactory transformerFactory) {
        Preconditions.checkNotNull(transformerFactory, "transformerFactory");
        this.transformerFactory = transformerFactory;
    }

    @Override
    public boolean generatesClass(Type classType) {
        return this.registry.findSyntheticClass(classType.getClassName()) != null;
    }

    @Override
    public synchronized boolean processClass(ClassNode classNode, Type classType, String reason) {
        if (this.transformer == null) {
            if (this.transformerFactory == null) {
                throw new IllegalStateException("processClass called before transformer factory offered to transformation handler");
            }
            this.transformer = this.transformerFactory.createTransformer();

            //We set the session ID to a static value.
            //Improves detection at runtime, and produces a guaranteed value during testing.
            try
            {
                final Field processorField = this.transformer.getClass().getDeclaredField("processor");
                processorField.setAccessible(true);
                final Class<?> processorClass = processorField.getType();
                final Field field = processorClass.getDeclaredField("sessionId");
                field.setAccessible(true);
                field.set(processorField.get(this.transformer), "Ducker Static Mixin Transformer");
            }
            catch (NoSuchFieldException | IllegalAccessException e)
            {
                e.printStackTrace();
            }

            this.registry = this.transformer.getExtensions().getSyntheticClassRegistry();
        }

        // Don't transform when the reason is mixin (side-loading in progress)
        if (MixinLaunchPluginLegacy.NAME.equals(reason)) {
            return false;
        }

        if (this.generatesClass(classType)) {
            return this.generateClass(classType, classNode);
        }

        MixinEnvironment environment = MixinEnvironment.getCurrentEnvironment();
        if (COMPUTING_FRAMES.equals(reason)) {
            return this.transformer.computeFramesForClass(environment, classType.getClassName(), classNode);
        }

        return this.transformer.transformClass(environment, classType.getClassName(), classNode);
    }

    @Override
    public boolean generateClass(Type classType, ClassNode classNode) {
        return this.transformer.generateClass(MixinEnvironment.getCurrentEnvironment(), classType.getClassName(), classNode);
    }
}

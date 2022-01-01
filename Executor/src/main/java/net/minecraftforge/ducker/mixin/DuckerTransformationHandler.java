package net.minecraftforge.ducker.mixin;

import com.google.common.base.Preconditions;
import cpw.mods.modlauncher.api.ITransformerActivity;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.IClassProcessor;
import org.spongepowered.asm.launch.MixinLaunchPluginLegacy;
import org.spongepowered.asm.launch.Phases;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.ISyntheticClassRegistry;

import java.util.EnumSet;

public class DuckerTransformationHandler implements IClassProcessor {

    /**
     * Mixin transformer factory, from service
     */
    private IMixinTransformerFactory transformerFactory;

    /**
     * Transformer pipeline instance
     */
    private IMixinTransformer transformer;

    /**
     * Synthetic class registry, used so the processor knows when to respond to
     * empty class population requests
     */
    private ISyntheticClassRegistry registry;

    void offer(IMixinTransformerFactory transformerFactory) {
        Preconditions.checkNotNull(transformerFactory, "transformerFactory");
        this.transformerFactory = transformerFactory;
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.launch.IClassProcessor#handlesClass(
     *      org.objectweb.asm.Type, boolean, java.lang.String)
     */
    @Override
    public EnumSet<ILaunchPluginService.Phase> handlesClass(Type classType, boolean isEmpty, String reason) {
        if (!isEmpty) {
            return Phases.AFTER_ONLY;
        }

        if (this.registry == null) {
            return null;
        }

        return this.generatesClass(classType) ? Phases.AFTER_ONLY : null;
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.launch.IClassProcessor#generatesClass(
     *      org.objectweb.asm.Type)
     */
    @Override
    public boolean generatesClass(Type classType) {
        return this.registry.findSyntheticClass(classType.getClassName()) != null;
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.launch.IClassProcessor#processClass(
     *      cpw.mods.modlauncher.serviceapi.ILaunchPluginService.Phase,
     *      org.objectweb.asm.tree.ClassNode, org.objectweb.asm.Type,
     *      java.lang.String)
     */
    @Override
    public synchronized boolean processClass(ILaunchPluginService.Phase phase, ClassNode classNode, Type classType, String reason) {
        if (phase == ILaunchPluginService.Phase.BEFORE) {
            return false;
        }

        if (this.transformer == null) {
            if (this.transformerFactory == null) {
                throw new IllegalStateException("processClass called before transformer factory offered to transformation handler");
            }
            this.transformer = this.transformerFactory.createTransformer();
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
        if (ITransformerActivity.COMPUTING_FRAMES_REASON.equals(reason)) {
            return this.transformer.computeFramesForClass(environment, classType.getClassName(), classNode);
        }

        return this.transformer.transformClass(environment, classType.getClassName(), classNode);
    }

    /* (non-Javadoc)
     * @see org.spongepowered.asm.launch.IClassProcessor#generateClass(
     *      org.objectweb.asm.Type, org.objectweb.asm.tree.ClassNode)
     */
    @Override
    public boolean generateClass(Type classType, ClassNode classNode) {
        return this.transformer.generateClass(MixinEnvironment.getCurrentEnvironment(), classType.getClassName(), classNode);
    }
}

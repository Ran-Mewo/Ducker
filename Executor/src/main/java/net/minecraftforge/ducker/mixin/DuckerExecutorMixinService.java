package net.minecraftforge.ducker.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.ducker.mixin.classes.IClassProcessor;
import net.minecraftforge.ducker.mixin.container.ContainerHandleDucker;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.*;
import org.spongepowered.asm.util.IConsumer;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;

public final class DuckerExecutorMixinService extends MixinServiceAbstract
{
    private static final String DUCKER_MIXIN_PACKAGE = "net.minecraftforge.ducker.mixin.";
    private static final String CONTAINER_PACKAGE = DUCKER_MIXIN_PACKAGE + "container.";
    private static final String ROOT_CONTAINER_CLASS = CONTAINER_PACKAGE + "ContainerHandleDucker";

    private DuckerClasspathClassProvider classProvider;
    private IClassBytecodeProvider bytecodeProvider;
    private DuckerTransformationHandler transformationHandler;
    private ContainerHandleDucker rootContainer;
    private final MixinEnvironment.CompatibilityLevel minCompatibilityLevel;

    public DuckerExecutorMixinService() {
        this.minCompatibilityLevel = MixinEnvironment.CompatibilityLevel.JAVA_16;
        this.createRootContainer();
    }

    public void onInit(IClassBytecodeProvider bytecodeProvider) {
        this.bytecodeProvider = bytecodeProvider;
    }

    private void createRootContainer() {
        try {
            Class<?> clRootContainer = this.getClassProvider().findClass(DuckerExecutorMixinService.ROOT_CONTAINER_CLASS);
            Constructor<?> ctor = clRootContainer.getDeclaredConstructor(String.class);
            this.rootContainer = (ContainerHandleDucker) ctor.newInstance(this.getName());
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void offer(IMixinInternal internal) {
        if (internal instanceof IMixinTransformerFactory) {
            this.getTransformationHandler().offer((IMixinTransformerFactory)internal);
        }
        super.offer(internal);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void wire(MixinEnvironment.Phase phase, IConsumer<MixinEnvironment.Phase> phaseConsumer) {
        super.wire(phase, phaseConsumer);
    }

    @Override
    public String getName() {
        return "Ducker";
    }

    @Override
    public MixinEnvironment.CompatibilityLevel getMinCompatibilityLevel() {
        return this.minCompatibilityLevel;
    }

    @Override
    protected ILogger createLogger(String name) {
        return new DuckerLoggerAdapter(name);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public IClassProvider getClassProvider() {
        return getClassPathProvider();
    }

    public DuckerClasspathClassProvider getClassPathProvider() {
        if (this.classProvider == null) {
            this.classProvider = new DuckerClasspathClassProvider();
        }
        return this.classProvider;
    }

    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
        if (this.bytecodeProvider == null) {
            throw new IllegalStateException("Service initialisation incomplete");
        }
        return this.bytecodeProvider;
    }

    @Override
    public ITransformerProvider getTransformerProvider() {
        return null;
    }

    @Override
    public IClassTracker getClassTracker() {
        return null;
    }

    @Override
    public IMixinAuditTrail getAuditTrail() {
        return null;
    }

    private DuckerTransformationHandler getTransformationHandler() {
        if (this.transformationHandler == null) {
            this.transformationHandler = new DuckerTransformationHandler();
        }
        return this.transformationHandler;
    }

    @Override
    public Collection<String> getPlatformAgents() {
        return ImmutableList.of(
          DUCKER_MIXIN_PACKAGE + "DuckerMixinPlatformAgent"
        );
    }

    @Override
    public ContainerHandleDucker getPrimaryContainer() {
        return this.rootContainer;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return getClassPathProvider().getFullClassLoader().getResourceAsStream(name);
    }

    public Collection<IClassProcessor> getProcessors() {
        return ImmutableList.of(
          this.getTransformationHandler()
        );
    }

    @Override
    public MixinEnvironment.Phase getInitialPhase()
    {
        return MixinEnvironment.Phase.DEFAULT;
    }
}

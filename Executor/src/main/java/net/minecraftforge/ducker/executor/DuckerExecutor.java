package net.minecraftforge.ducker.executor;

import com.google.common.io.Resources;
import net.minecraftforge.ducker.mixin.DuckerClasspathClassProvider;
import net.minecraftforge.ducker.mixin.DuckerExecutorMixinService;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.IClassProcessor;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.transformers.MixinClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class DuckerExecutor implements IClassBytecodeProvider {

    /**
     * Class processing components
     */
    private final Collection<IClassProcessor> processors = ((DuckerExecutorMixinService) MixinService.getService()).getProcessors();

    private byte[] getClassBytes(final String name) throws ClassNotFoundException {
        final DuckerExecutorMixinService duckerExecutorMixinService = (DuckerExecutorMixinService) MixinService.getService();
        final DuckerClasspathClassProvider classProvider = duckerExecutorMixinService.getClassPathProvider();

        String classAsPath = name.replace('.', '/') + ".class";
        InputStream stream = classProvider.getUrlClassLoader().getResourceAsStream(classAsPath);
        try {
            return IOUtils.toByteArray(Objects.requireNonNull(stream));
        } catch (IOException e) {
            throw new ClassNotFoundException("Could not get the byte data for the class: " + name);
        }
    }

    @Override
    public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
        return this.getClassNode(name, true);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
        if (!runTransformers) {
            throw new IllegalArgumentException("ModLauncher service does not currently support retrieval of untransformed bytecode");
        }

        String canonicalName = name.replace('/', '.');
        String internalName = name.replace('.', '/');

        byte[] classBytes;

        try {
            classBytes = getClassBytes(canonicalName);
        } catch (ClassNotFoundException ex) {
            URL url = Thread.currentThread().getContextClassLoader().getResource(internalName + ".class");
            if (url == null) {
                throw ex;
            }
            try {
                classBytes = Resources.asByteSource(url).read();
            } catch (IOException ioex) {
                throw ex;
            }
        }

        if (classBytes != null && classBytes.length != 0) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new MixinClassReader(classBytes, canonicalName);
            classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
            return classNode;
        }

        Type classType = Type.getObjectType(internalName);
        synchronized (this.processors) {
            for (IClassProcessor processor : this.processors) {
                if (!processor.generatesClass(classType)) {
                    continue;
                }

                ClassNode classNode = new ClassNode();
                if (processor.generateClass(classType, classNode)) {
                    return classNode;
                }
            }
        }

        throw new ClassNotFoundException(canonicalName);
    }
}

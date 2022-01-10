package net.minecraftforge.ducker.executor;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import net.minecraftforge.ducker.configuration.DuckerConfiguration;
import net.minecraftforge.ducker.decompile.IDecompiler;
import net.minecraftforge.ducker.writer.IClassWriter;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jetbrains.java.decompiler.util.InterpreterUtil;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.MethodRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.logging.Level;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.asm.ASM;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

class ExecutorServiceTest
{

    @Test
    void executeSimple()
    {
        final File output = new File("src/test/output/classes/simplesource");
        final File sources = new File("src/test/output/sources/simplesource");

        final DuckerConfiguration configuration = new DuckerConfiguration(
          Collections.emptyList(),
          "src/test/resources/runtimes/SimpleSource - Source/SimpleSource - Source-1.0.jar",
          Collections.singletonList("src/test/resources/runtimes/SimpleSource - Mixin/SimpleSource - Mixin-1.0.jar"),
          "net.minecraftforge.ducker.digger.simplesource", new TestClassWriter(output), new TestDecompiler(sources));

        ExecutorService.getInstance().execute(configuration);
    }

    @Test
    void executeNewInvokeSpecial()
    {
        final File output = new File("src/test/output/classes/newinvokespecial");
        final File sources = new File("src/test/output/sources/newinvokespecial");

        final DuckerConfiguration configuration = new DuckerConfiguration(
          Collections.emptyList(),
          "src/test/resources/runtimes/NewInvokeSpecial - Source/NewInvokeSpecial - Source-1.0.jar",
          Collections.singletonList("src/test/resources/runtimes/NewInvokeSpecial - Mixin/NewInvokeSpecial - Mixin-1.0.jar"),
          "net.minecraftforge.ducker.digger.newinvokespecial", new TestClassWriter(output), new TestDecompiler(sources));

        ExecutorService.getInstance().execute(configuration);
    }

    private final class TestClassWriter implements IClassWriter {

        private final File outputDirectory;

        private TestClassWriter(final File outputDirectory) {this.outputDirectory = outputDirectory;}

        @Override
        public File writeClass(final ClassNode classNode)
        {
            final List<MethodNode> mixinMethods = classNode.methods.stream()
                    .filter(methodNode -> methodNode.getClass().getPackageName().contains("spongepowered")) //We need all those special mixin methods :D
                    .toList();

            //We need to remove the mixin merged method annotations....
            mixinMethods.forEach(mixinMethodNode -> mixinMethodNode.visibleAnnotations.removeIf(annotationNode -> annotationNode.desc.contains("MixinMerged")));

            final Map<String, String> methodRemapMap = mixinMethods.stream()
                    .collect(Collectors.toMap(
                            methodNode -> classNode.name + "." + methodNode.name + methodNode.desc,
                            methodNode -> "ducker$$" + methodNode.name
                    ));

            final SimpleRemapper simpleRemapper = new SimpleRemapper(methodRemapMap);


            final ClassWriter classWriter = new ClassWriter(ASM.API_VERSION);
            final ClassRemapper classRemapper = new ClassRemapper(classWriter, simpleRemapper);
            classNode.accept(classRemapper);

            final File output = new File(outputDirectory, classNode.name + ".class");
            if (!output.exists())
                output.getParentFile().mkdirs();
            else
                output.delete();

            try
            {
                Files.write(output.toPath(), classWriter.toByteArray(), StandardOpenOption.CREATE);
            }
            catch (IOException e)
            {
                throw new IllegalStateException(e);
            }

            return output;
        }
    }

    private final class TestDecompiler extends IFernflowerLogger implements IDecompiler, IResultSaver
    {

        private static final Level[] SEVERITY_LEVELS = { Level.TRACE, Level.INFO, Level.WARN, Level.ERROR };

        private final Map<String, Object> options = ImmutableMap.<String, Object>builder()
          .put(IFernflowerPreferences.DECOMPILE_INNER,              "0")
          .put(IFernflowerPreferences.REMOVE_BRIDGE,                "0")
          .put(IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES, "1")
          .put(IFernflowerPreferences.ASCII_STRING_CHARACTERS,      "1")
          .put(IFernflowerPreferences.DECOMPILE_ENUM,               "1")
          .put(IFernflowerPreferences.HIDE_DEFAULT_CONSTRUCTOR,     "1")
          .put(IFernflowerPreferences.INDENT_STRING,                "    ")
          .build();

        private final File outputPath;

        protected final ILogger logger = MixinService.getService().getLogger("fernflower");

        public TestDecompiler(File outputPath) {
            this.outputPath = outputPath;
            if (this.outputPath.exists()) {
                try {
                    MoreFiles.deleteRecursively(this.outputPath.toPath(), RecursiveDeleteOption.ALLOW_INSECURE);
                } catch (IOException ex) {
                    this.logger.debug("Error cleaning output directory: {}", ex.getMessage());
                }
            }
        }

        @Override
        public String toString() {
            try {
                URL codeSource = Fernflower.class.getProtectionDomain().getCodeSource().getLocation();
                File file = org.spongepowered.asm.util.Files.toFile(codeSource);
                return file.getName();
            } catch (Exception ex) {
                return "unknown source (classpath)";
            }
        }

        @Override
        public void decompile(final File file) {
            try {
                Fernflower fernflower = new Fernflower(new IBytecodeProvider() {

                    private byte[] byteCode;

                    @Override
                    public byte[] getBytecode(String externalPath, String internalPath) throws IOException {
                        if (this.byteCode == null) {
                            this.byteCode = InterpreterUtil.getBytes(new File(externalPath));
                        }
                        return this.byteCode;
                    }

                }, this, this.options, this);

                try {
                    // New fernflower (including forgeflower)
                    Method mdAddSource = fernflower.getClass().getDeclaredMethod("addSource", File.class);
                    mdAddSource.invoke(fernflower, file);
                } catch (ReflectiveOperationException ex) {
                    // Old fernflower
                    throw new IllegalStateException("Fernflower is too old", ex);
                }

                fernflower.decompileContext();
            } catch (Throwable ex) {
                this.logger.warn("Decompilation error while processing {}", file.getName());
            }
        }

        @Override
        public void saveFolder(String path) {
        }

        @Override
        public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
            File file = new File(this.outputPath, qualifiedName + ".java");
            file.getParentFile().mkdirs();
            try {
                this.logger.info("Writing {}", file.getAbsolutePath());
                com.google.common.io.Files.write(content, file, Charsets.UTF_8);
            } catch (IOException ex) {
                this.writeMessage("Cannot write source file " + file, ex);
            }
        }

        @Override
        public void startReadingClass(String className) {
            this.logger.info("Decompiling {}", className);
        }

        @Override
        public void writeMessage(String message, Severity severity) {
            this.logger.log(SEVERITY_LEVELS[severity.ordinal()], message);
        }

        @Override
        public void writeMessage(String message, Throwable t) {
            this.logger.warn("{} {}: {}", message, t.getClass().getSimpleName(), t.getMessage());
        }

        @Override
        public void writeMessage(String message, Severity severity, Throwable t) {
            this.logger.log(SEVERITY_LEVELS[severity.ordinal()], message, severity == Severity.ERROR ? t : null);
        }

        @Override
        public void copyFile(String source, String path, String entryName) {
        }

        @Override
        public void createArchive(String path, String archiveName, Manifest manifest) {
        }

        @Override
        public void saveDirEntry(String path, String archiveName, String entryName) {
        }

        @Override
        public void copyEntry(String source, String path, String archiveName, String entry) {
        }

        @Override
        public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
        }

        @Override
        public void closeArchive(String path, String archiveName) {
        }

    }
}
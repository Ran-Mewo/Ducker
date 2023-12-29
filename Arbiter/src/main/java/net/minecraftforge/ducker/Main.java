package net.minecraftforge.ducker;

import com.google.common.collect.Lists;
import joptsimple.AbstractOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraftforge.ducker.configuration.DuckerConfiguration;
import net.minecraftforge.ducker.decompile.IDecompiler;
import net.minecraftforge.ducker.decompile.NoopDecompiler;
import net.minecraftforge.ducker.decompile.forgeflower.ForgeFlowerBasedDecompiler;
import net.minecraftforge.ducker.executor.ExecutorService;
import net.minecraftforge.ducker.processor.FFBasedLineNumberFixer;
import net.minecraftforge.ducker.transformers.AccessorDesynthesizerTransformer;
import net.minecraftforge.ducker.transformers.ArgsClassStripper;
import net.minecraftforge.ducker.transformers.IResultsTransformer;
import net.minecraftforge.ducker.transformers.MixinAnnotationStripper;
import net.minecraftforge.ducker.transformers.MixinMethodRemapperAndPrivatizer;
import net.minecraftforge.ducker.transformers.OverwriteFixerTransformer;
import net.minecraftforge.ducker.transformers.SourceMapStrippingTransformer;
import net.minecraftforge.ducker.writer.SimpleClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.util.asm.ASM;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.out.println("Property is now: " + System.getProperty("mixin.env.refMapRemappingFile"));
        LOGGER.warn("Property: {}", System.getProperty("mixin.env.refMapRemappingFile"));
        LOGGER.warn("Starting Ducker with args: {}", (Object)args);

        final OptionParser parser = new OptionParser();

        final AbstractOptionSpec<File> targetJarOption = parser.acceptsAll(
                        Lists.newArrayList("target", "t"),
                        "The jar file against which the mixins should be applied.")
                .withRequiredArg()
                .ofType(File.class);

        final AbstractOptionSpec<File> mixinJarOption = parser.acceptsAll(
                        Lists.newArrayList("mixin", "m"),
                        "The jar file from which the mixins should be applied.")
                .withRequiredArg()
                .ofType(File.class);

        final AbstractOptionSpec<File> outputDirectoryOption = parser.acceptsAll(
                        Lists.newArrayList("output", "o"),
                        "The directory into which the classes with the mixins applied are collected.")
                .withRequiredArg()
                .ofType(File.class);

        final AbstractOptionSpec<File> sourcesDirectoryOption = parser.acceptsAll(
                        Lists.newArrayList("sources", "s"),
                        "The directory into which the classes with the mixins applied are collected.")
                .withOptionalArg()
                .ofType(File.class);

        final AbstractOptionSpec<String> targetPackageOption = parser.acceptsAll(
                        Lists.newArrayList("package", "p"),
                        "The root packages of the classes to process.")
                .withRequiredArg();

        final AbstractOptionSpec<Transformer> transformersOption = parser.acceptsAll(
                List.of("transformer"),
                "Post-mixin transformers to run"
        ).withOptionalArg().ofType(Transformer.class);

        final AbstractOptionSpec<String> classpath = parser.acceptsAll(List.of("classpath")).withRequiredArg();

        final OptionSet parsed = parser.parse(args);

        final List<File> target;
        final List<String> rootPackages;
        final List<File> mixin;
        final File output;
        final Optional<File> sources;
        try {
            target = targetJarOption.values(parsed);
            rootPackages = targetPackageOption.values(parsed);
            mixin = parsed.valuesOf(mixinJarOption);

            output = outputDirectoryOption.value(parsed);
            sources = parsed.has(sourcesDirectoryOption) ? Optional.of(sourcesDirectoryOption.value(parsed)) : Optional.empty();

            if (target == null || rootPackages.isEmpty() || mixin.isEmpty() || output == null) {
                throw new IllegalArgumentException("Missing required arguments");
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to parse arguments");
            LOGGER.error("");
            LOGGER.error("Usage:");
            try {
                parser.printHelpOn(new Writer() {
                    @Override
                    public void write(char[] cbuf, int off, int len) {
                        final String content = new String(cbuf, off, len);
                        final List<String> lines = Lists.newArrayList(content.split("\n"));
                        lines.forEach(LOGGER::error);
                    }

                    @Override
                    public void flush() {

                    }

                    @Override
                    public void close() {

                    }
                });
            } catch (IOException e) {
                LOGGER.error("Failed to print help", e);
            }
            System.exit(1);
            throw new IllegalStateException("Failed to parse arguments", ex);
        }

        ASM.getApiVersionMajor();
        setValue(ASM.class, "majorVersion", 9);
        setValue(ASM.class, "minorVersion", 2);
        setValue(ASM.class, "implMinorVersion", 2);
        setValue(ASM.class, "patchVersion", 0);

        final DuckerConfiguration configuration = new DuckerConfiguration(
                classpath.values(parsed),
                target.stream().map(File::getAbsolutePath).toList(),
                mixin.stream().map(File::getAbsolutePath).toList(),
                rootPackages,
                new SimpleClassWriter(output),
                sources.map((Function<File, IDecompiler>) ForgeFlowerBasedDecompiler::new).orElse(NoopDecompiler.INSTANCE),
                parsed.valuesOf(transformersOption).stream().map(Transformer::newTransformer).collect(Collectors.toCollection(LinkedList::new)),
                Lists.newLinkedList());

        try {
            ExecutorService.getInstance().execute(configuration);
        } catch (Exception ex) {
            LOGGER.error("Failed to execute", ex);
            System.exit(1);
            throw new IllegalStateException("Failed to execute", ex);
        }
    }

    private static void setValue(Class<?> clazz, String field, int value) {
        try {
            final Field fl = clazz.getDeclaredField(field);
            fl.setAccessible(true);
            fl.setInt(null, value);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public enum Transformer {
        ARGS_CLASS_STRIPPER {
            @Override
            public IResultsTransformer newTransformer() {
                return new ArgsClassStripper();
            }
        },
        MIXIN_ANNOTATION_STRIPPER {
            @Override
            public IResultsTransformer newTransformer() {
                return new MixinAnnotationStripper();
            }
        },
        MIXIN_METHOD_REMAPPER_PRIVATIZER {
            @Override
            public IResultsTransformer newTransformer() {
                return new MixinMethodRemapperAndPrivatizer();
            }
        },
        SOURCE_MAP_STRIPPER {
            @Override
            public IResultsTransformer newTransformer() {
                return new SourceMapStrippingTransformer();
            }
        },
        ACCESSOR_DESYNTHESIZER {
            @Override
            public IResultsTransformer newTransformer() {
                return new AccessorDesynthesizerTransformer();
            }
        },
        OVERWRITE_FIXER {
            @Override
            public IResultsTransformer newTransformer() {
                return new OverwriteFixerTransformer();
            }
        };

        public abstract IResultsTransformer newTransformer();
    }
}

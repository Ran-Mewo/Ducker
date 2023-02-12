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
import net.minecraftforge.ducker.transformers.ArgsClassStripper;
import net.minecraftforge.ducker.transformers.MixinAnnotationStripper;
import net.minecraftforge.ducker.transformers.MixinMethodRemapperAndPrivatizer;
import net.minecraftforge.ducker.transformers.SourceMapStrippingTransformer;
import net.minecraftforge.ducker.writer.SimpleClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
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
                        "The root package of the classes to process.")
                .withRequiredArg();

        final OptionSet parsed = parser.parse(args);

        final File target;
        final String rootPackage;
        final List<File> mixin;
        final File output;
        final Optional<File> sources;
        try {
            target = targetJarOption.value(parsed);
            rootPackage = targetPackageOption.value(parsed);
            mixin = parsed.valuesOf(mixinJarOption);

            output = outputDirectoryOption.value(parsed);
            sources = parsed.has(sourcesDirectoryOption) ? Optional.of(sourcesDirectoryOption.value(parsed)) : Optional.empty();

            if (target == null || rootPackage == null || mixin.isEmpty() || output == null) {
                throw new IllegalArgumentException("Missing required arguments");
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to parse arguments", ex);
            LOGGER.error("");
            LOGGER.error("Usage:");
            try {
                parser.printHelpOn(new Writer() {
                    @Override
                    public void write(char[] cbuf, int off, int len) {
                        LOGGER.error(new String(cbuf, off, len));
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


        final DuckerConfiguration configuration = new DuckerConfiguration(
                Collections.emptyList(),
                target.getAbsolutePath(),
                mixin.stream().map(File::getAbsolutePath).toList(),
                rootPackage,
                new SimpleClassWriter(output),
                sources.map((Function<File, IDecompiler>) ForgeFlowerBasedDecompiler::new).orElse(NoopDecompiler.INSTANCE),
                Lists.newLinkedList(
                        Lists.newArrayList(
                                new ArgsClassStripper(),
                                new MixinAnnotationStripper(),
                                new MixinMethodRemapperAndPrivatizer(),
                                new SourceMapStrippingTransformer()
                        )
                ),
                Lists.newLinkedList(
                        Lists.newArrayList(
                                new FFBasedLineNumberFixer()
                        )));

        try {
            ExecutorService.getInstance().execute(configuration);
        } catch (Exception ex) {
            LOGGER.error("Failed to execute", ex);
            System.exit(1);
            throw new IllegalStateException("Failed to execute", ex);
        }
    }
}

package net.minecraftforge.ducker.executor;

import com.google.common.collect.Lists;
import net.minecraftforge.ducker.configuration.DuckerConfiguration;
import net.minecraftforge.ducker.decompile.forgeflower.ForgeFlowerBasedDecompiler;
import net.minecraftforge.ducker.processor.FFBasedLineNumberFixer;
import net.minecraftforge.ducker.transformers.AccessWidenerTransformer;
import net.minecraftforge.ducker.transformers.ArgsClassStripper;
import net.minecraftforge.ducker.transformers.MixinAnnotationStripper;
import net.minecraftforge.ducker.transformers.MixinMethodRemapperAndPrivatizer;
import net.minecraftforge.ducker.transformers.SourceMapStrippingTransformer;
import net.minecraftforge.ducker.util.AccessWidenerUtil;
import net.minecraftforge.ducker.writer.SimpleClassWriter;

import java.io.File;
import java.util.Collections;

import org.junit.jupiter.api.Test;

class ExecutorServiceTest
{
    void runTestFor(String name)
    {
        runTestFor(name, false);
    }

    void runTestFor(String name, boolean withAccessWidener)
    {
        final File output = new File("src/test/output/classes/" + name.toLowerCase());
        final File sources = new File("src/test/output/sources/" + name.toLowerCase());

        // Create a list of transformers
        var transformers = Lists.newArrayList(
                new ArgsClassStripper(),
                new MixinAnnotationStripper(),
                new MixinMethodRemapperAndPrivatizer(),
                new SourceMapStrippingTransformer()
        );

        // Add access widener transformer if requested
        if (withAccessWidener) {
            transformers.add(new AccessWidenerTransformer(
                    AccessWidenerUtil.createAccessWidener(
                            Collections.singletonList("src/test/resources/test.accesswidener")
                    )
            ));
        }

        final DuckerConfiguration configuration = new DuckerConfiguration(
                Collections.emptyList(),
                Collections.singletonList("src/test/resources/runtimes/" + name + " - Source/" + name + " - Source-1.0.jar"),
                Collections.singletonList("src/test/resources/runtimes/" + name + " - Mixin/" + name + " - Mixin-1.0.jar"),
                withAccessWidener ?
                        Collections.singletonList("src/test/resources/test.accesswidener") :
                        Collections.emptyList(),
                Collections.singletonList("net.minecraftforge.ducker.digger"),
                new SimpleClassWriter(output),
                new ForgeFlowerBasedDecompiler(sources),
                Lists.newLinkedList(transformers),
                Lists.newLinkedList(
                        Lists.newArrayList(
                                new FFBasedLineNumberFixer()
                        )));

        ExecutorService.getInstance().execute(configuration);
    }

    @Test
    void testWithAccessWidener() {
        // Run a test with access widener enabled
        runTestFor("InjectHead", true);
    }
}
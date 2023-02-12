package net.minecraftforge.ducker.executor;

import com.google.common.collect.Lists;
import net.minecraftforge.ducker.configuration.DuckerConfiguration;
import net.minecraftforge.ducker.decompile.forgeflower.ForgeFlowerBasedDecompiler;
import net.minecraftforge.ducker.processor.FFBasedLineNumberFixer;
import net.minecraftforge.ducker.transformers.ArgsClassStripper;
import net.minecraftforge.ducker.transformers.MixinAnnotationStripper;
import net.minecraftforge.ducker.transformers.MixinMethodRemapperAndPrivatizer;
import net.minecraftforge.ducker.transformers.SourceMapStrippingTransformer;
import net.minecraftforge.ducker.writer.SimpleClassWriter;

import java.io.File;
import java.util.Collections;

class ExecutorServiceTest
{
    void runTestFor(String name)
    {
        final File output = new File("src/test/output/classes/" + name.toLowerCase());
        final File sources = new File("src/test/output/sources/" + name.toLowerCase());

        final DuckerConfiguration configuration = new DuckerConfiguration(
                Collections.emptyList(),
                "src/test/resources/runtimes/" + name + " - Source/" + name + " - Source-1.0.jar",
                Collections.singletonList("src/test/resources/runtimes/" + name + " - Mixin/" + name + " - Mixin-1.0.jar"),
                "net.minecraftforge.ducker.digger",
                new SimpleClassWriter(output),
                new ForgeFlowerBasedDecompiler(sources),
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

        ExecutorService.getInstance().execute(configuration);
    }
}
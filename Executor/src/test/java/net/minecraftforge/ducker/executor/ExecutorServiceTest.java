package net.minecraftforge.ducker.executor;

import com.google.common.collect.Lists;
import net.minecraftforge.ducker.configuration.DuckerConfiguration;
import net.minecraftforge.ducker.decompile.forgeflower.ForgeFlowerBasedDecompiler;
import net.minecraftforge.ducker.processor.FFBasedLineNumberFixer;
import net.minecraftforge.ducker.transformers.MixinAnnotationStripper;
import net.minecraftforge.ducker.transformers.MixinMethodRemapper;
import net.minecraftforge.ducker.transformers.SourceMapStrippingTransformer;
import net.minecraftforge.ducker.writer.SimpleClassWriter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;

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
          "net.minecraftforge.ducker.digger.simplesource",
          new SimpleClassWriter(output),
          new ForgeFlowerBasedDecompiler(sources),
          Lists.newLinkedList(
            Lists.newArrayList(
              new MixinAnnotationStripper(),
              new MixinMethodRemapper(),
              new SourceMapStrippingTransformer()
            )
          ),
          Lists.newLinkedList(
            Lists.newArrayList(
              new FFBasedLineNumberFixer()
            )));

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
          "net.minecraftforge.ducker.digger.newinvokespecial",
          new SimpleClassWriter(output),
          new ForgeFlowerBasedDecompiler(sources),
          Lists.newLinkedList(
            Lists.newArrayList(
              new MixinAnnotationStripper(),
              new MixinMethodRemapper(),
              new SourceMapStrippingTransformer()
            )
          ),
          Lists.newLinkedList(
            Lists.newArrayList(
              new FFBasedLineNumberFixer()
            )));

        ExecutorService.getInstance().execute(configuration);
    }

    @Test
    void executeModifyVariable()
    {
        final File output = new File("src/test/output/classes/modifyvariable");
        final File sources = new File("src/test/output/sources/modifyvariable");

        final DuckerConfiguration configuration = new DuckerConfiguration(
          Collections.emptyList(),
          "src/test/resources/runtimes/ModifyVariable - Source/ModifyVariable - Source-1.0.jar",
          Collections.singletonList("src/test/resources/runtimes/ModifyVariable - Mixin/ModifyVariable - Mixin-1.0.jar"),
          "net.minecraftforge.ducker.digger.modifyvariable",
          new SimpleClassWriter(output),
          new ForgeFlowerBasedDecompiler(sources),
          Lists.newLinkedList(
            Lists.newArrayList(
              new MixinAnnotationStripper(),
              new MixinMethodRemapper(),
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
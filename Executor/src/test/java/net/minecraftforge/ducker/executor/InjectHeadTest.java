package net.minecraftforge.ducker.executor;

import com.google.common.collect.Lists;
import net.minecraftforge.ducker.configuration.DuckerConfiguration;
import net.minecraftforge.ducker.decompile.forgeflower.ForgeFlowerBasedDecompiler;
import net.minecraftforge.ducker.processor.FFBasedLineNumberFixer;
import net.minecraftforge.ducker.transformers.ArgsClassStripper;
import net.minecraftforge.ducker.transformers.MixinAnnotationStripper;
import net.minecraftforge.ducker.transformers.MixinMethodRemapper;
import net.minecraftforge.ducker.transformers.SourceMapStrippingTransformer;
import net.minecraftforge.ducker.writer.SimpleClassWriter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;

class InjectHeadTest extends ExecutorServiceTest
{
    @Test
    public void execute() {
        runTestFor("InjectHead");
    }
}
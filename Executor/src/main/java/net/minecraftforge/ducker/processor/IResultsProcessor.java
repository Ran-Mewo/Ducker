package net.minecraftforge.ducker.processor;

import net.minecraftforge.ducker.mixin.DuckerExecutorMixinService;

import java.io.File;
import java.util.Set;

public interface IResultsProcessor
{
    byte[] process(byte[] bytes, Set<File> additionalFiles, final DuckerExecutorMixinService service);
}

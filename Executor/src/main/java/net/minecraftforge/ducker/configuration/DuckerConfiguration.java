package net.minecraftforge.ducker.configuration;

import net.minecraftforge.ducker.decompile.IDecompiler;
import net.minecraftforge.ducker.processor.IResultsProcessor;
import net.minecraftforge.ducker.transformers.IResultsTransformer;
import net.minecraftforge.ducker.writer.IClassWriter;

import java.util.LinkedList;
import java.util.List;

public class DuckerConfiguration
{
    private final List<String> targetRuntimeClasspath;
    private final String targetJar;
    private final List<String> mixinSourcesClasspath;
    private final List<String> extractionPackages;
    private final IClassWriter classWriter;
    private final IDecompiler decompiler;
    private final LinkedList<IResultsTransformer> resultsTransformers;
    private final LinkedList<IResultsProcessor> resultsProcessors;

    public DuckerConfiguration(
      final List<String> targetRuntimeClasspath,
      final String targetJar,
      final List<String> mixinSourcesClasspath,
      final List<String> extractionPackages,
      final IClassWriter classWriter,
      final IDecompiler decompiler,
      final LinkedList<IResultsTransformer> resultsTransformers,
      final LinkedList<IResultsProcessor> resultsProcessors) {
        this.targetRuntimeClasspath = targetRuntimeClasspath;
        this.targetJar = targetJar;
        this.mixinSourcesClasspath = mixinSourcesClasspath;
        this.extractionPackages = extractionPackages;
        this.classWriter = classWriter;
        this.decompiler = decompiler;
        this.resultsTransformers = resultsTransformers;
        this.resultsProcessors = resultsProcessors;
    }

    public List<String> getTargetRuntimeClasspath()
    {
        return targetRuntimeClasspath;
    }

    public String getTargetJar()
    {
        return targetJar;
    }

    public List<String> getMixinSourcesClasspath()
    {
        return mixinSourcesClasspath;
    }

    public List<String> getExtractionPackages()
    {
        return extractionPackages;
    }

    public IClassWriter getClassWriter()
    {
        return classWriter;
    }

    public IDecompiler getDecompiler()
    {
        return decompiler;
    }

    public LinkedList<IResultsTransformer> getResultsTransformers()
    {
        return resultsTransformers;
    }

    public LinkedList<IResultsProcessor> getResultsProcessors()
    {
        return resultsProcessors;
    }
}

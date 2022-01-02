package net.minecraftforge.ducker.configuration;

import net.minecraftforge.ducker.decompile.IDecompiler;
import net.minecraftforge.ducker.writer.IClassWriter;

import java.util.List;

public class DuckerConfiguration
{
    private final List<String> targetRuntimeClasspath;
    private final String targetJar;
    private final List<String> mixinSourcesClasspath;
    private final String rootNamespace;
    private final IClassWriter classWriter;
    private final IDecompiler decompiler;

    public DuckerConfiguration(
      final List<String> targetRuntimeClasspath,
      final String targetJar,
      final List<String> mixinSourcesClasspath,
      final String rootNamespace,
      final IClassWriter classWriter, final IDecompiler decompiler) {
        this.targetRuntimeClasspath = targetRuntimeClasspath;
        this.targetJar = targetJar;
        this.mixinSourcesClasspath = mixinSourcesClasspath;
        this.rootNamespace = rootNamespace;
        this.classWriter = classWriter;
        this.decompiler = decompiler;
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

    public String getRootNamespace()
    {
        return rootNamespace;
    }

    public IClassWriter getClassWriter()
    {
        return classWriter;
    }

    public IDecompiler getDecompiler()
    {
        return decompiler;
    }
}

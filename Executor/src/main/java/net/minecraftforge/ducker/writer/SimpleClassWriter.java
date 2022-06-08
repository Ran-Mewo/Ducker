package net.minecraftforge.ducker.writer;

import net.minecraftforge.ducker.configuration.DuckerConfiguration;
import net.minecraftforge.ducker.processor.IResultsProcessor;
import net.minecraftforge.ducker.transformers.IResultsTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class SimpleClassWriter implements IClassWriter
{

    private final File outputDirectory;

    public SimpleClassWriter(final File outputDirectory)
    {
        this.outputDirectory = outputDirectory;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public File writeClass(final DuckerConfiguration configuration, final ClassNode classNode)
    {
        final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        ClassVisitor visitor = classWriter;
        for (final IResultsTransformer resultsTransformer : configuration.getResultsTransformers())
        {
            visitor = resultsTransformer.transform(classNode, visitor);
        }
        classNode.accept(visitor);

        final File output = new File(outputDirectory, classNode.name + ".class");
        if (!output.exists())
        {
            output.getParentFile().mkdirs();
        }
        else
        {
            output.delete();
        }

        byte[] processorOutput = classWriter.toByteArray();
        for (final IResultsProcessor resultsProcessor : configuration.getResultsProcessors())
        {
            processorOutput = resultsProcessor.process(processorOutput);
        }

        try
        {
            Files.write(output.toPath(), processorOutput, StandardOpenOption.CREATE);
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }

        return output;
    }
}

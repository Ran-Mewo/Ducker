package net.minecraftforge.ducker.writer;

import net.minecraftforge.ducker.configuration.DuckerConfiguration;
import net.minecraftforge.ducker.processor.IResultsProcessor;
import net.minecraftforge.ducker.transformers.ClassVisitorWithAdditionalGenerator;
import net.minecraftforge.ducker.transformers.IResultsTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

public class SimpleClassWriter implements IClassWriter
{

    private final File outputDirectory;

    public SimpleClassWriter(final File outputDirectory)
    {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public ClassWriterResult writeClass(final DuckerConfiguration configuration, final ClassNode classNode)
    {
        final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        ClassVisitor visitor = classWriter;
        final Set<ClassVisitorWithAdditionalGenerator> additionalGenerators = new HashSet<>();
        for (final IResultsTransformer resultsTransformer : configuration.getResultsTransformers())
        {
            visitor = resultsTransformer.transform(classNode, visitor);
            if (visitor instanceof ClassVisitorWithAdditionalGenerator)
            {
                additionalGenerators.add((ClassVisitorWithAdditionalGenerator) visitor);
            }
        }
        classNode.accept(visitor);

        final Set<File> additionalFiles = new HashSet<>();
        for (ClassVisitorWithAdditionalGenerator additionalGenerator : additionalGenerators) {
            for (ClassNode additionalClass : additionalGenerator.getAdditionalClasses()) {
                final ClassWriter additionalClassWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                additionalClass.accept(additionalClassWriter);
                additionalFiles.add(writeClass(configuration, additionalClass, additionalClassWriter.toByteArray()));
            }
        }

        byte[] processorOutput = classWriter.toByteArray();
        for (final IResultsProcessor resultsProcessor : configuration.getResultsProcessors())
        {
            processorOutput = resultsProcessor.process(processorOutput, additionalFiles);
        }

        return new ClassWriterResult(writeClass(configuration, classNode, classWriter.toByteArray()), additionalFiles);
    }

    private File writeClass(DuckerConfiguration configuration, ClassNode classNode, byte[] bytes) {
        final File output = new File(outputDirectory, classNode.name + ".class");
        if (!output.exists())
        {
            output.getParentFile().mkdirs();
        }
        else
        {
            output.delete();
        }

        try
        {
            Files.write(output.toPath(), bytes, StandardOpenOption.CREATE);
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }

        return output;
    }
}

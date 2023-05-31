package net.minecraftforge.ducker.processor;

import net.minecraftforge.ducker.decompile.forgeflower.AbstractResultSaver;
import net.minecraftforge.ducker.decompile.forgeflower.ForgeFlowerDecompilerBuilder;
import net.minecraftforge.ducker.mixin.DuckerExecutorMixinService;
import net.minecraftforge.ducker.util.DuckerClassWriter;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.objectweb.asm.*;
import org.spongepowered.asm.util.asm.ASM;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

public class FFBasedLineNumberFixer implements IResultsProcessor
{
    @Override
    public byte[] process(final byte[] bytes, Set<File> additionalFiles, final DuckerExecutorMixinService service)
    {
        final ClassReader classReader = new ClassReader(bytes);
        final ClassWriter classWriter = new DuckerClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES, service);

        classReader.accept(new LineNumberFixingClassVisitor(bytes, additionalFiles, classWriter), 0);

        return classWriter.toByteArray();
    }

    private static final class LineNumberFixingClassVisitor extends ClassVisitor {

        private final NavigableMap<Integer, Integer> lineNumberMap = new TreeMap<>();

        public LineNumberFixingClassVisitor(final byte[] originalData, Set<File> additionalFiles, final ClassVisitor classVisitor)
        {
            super(ASM.API_VERSION, classVisitor);

            try
            {
                final File tempTarget = File.createTempFile("ducker_line_fix_decompile", ".class");
                Files.write(tempTarget.toPath(), originalData, StandardOpenOption.CREATE);

                final Fernflower fernflower = ForgeFlowerDecompilerBuilder.getInstance().build(
                  "LineNumberFixingClassVisitor",
                  () -> null,
                  new AbstractResultSaver() {
                      @Override
                      public void saveClassFile(final String path, final String qualifiedName, final String entryName, final String content, final int[] mapping)
                      {
                          if (mapping != null) {
                              for (int i = 0; i < mapping.length; i += 2)
                              {
                                  lineNumberMap.put(mapping[i], mapping[i + 1]);
                              }
                          }
                      }
                  },
                  tempTarget,
                  additionalFiles
                );

                fernflower.decompileContext();

                tempTarget.deleteOnExit();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String[] exceptions) {
            MethodVisitor parent = super.visitMethod(access, name, descriptor, signature, exceptions);
            return new MethodVisitor(ASM.API_VERSION, parent) {
                @Override
                public void visitLineNumber(final int line, final Label start) {
                    Map.Entry<Integer, Integer> newLineData = lineNumberMap.higherEntry(line);
                    if (newLineData != null) {
                        super.visitLineNumber(newLineData.getValue(), start);
                    } else {
                        super.visitLineNumber(line, start);
                    }
                }

                @Override
                public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
                    super.visitFrame(type, numLocal, local, numStack, stack);
                }
            };
        }

    }
}

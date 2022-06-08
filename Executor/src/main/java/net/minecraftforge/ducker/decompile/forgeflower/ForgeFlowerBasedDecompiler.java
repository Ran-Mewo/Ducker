package net.minecraftforge.ducker.decompile.forgeflower;

import com.google.common.base.Charsets;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import net.minecraftforge.ducker.decompile.IDecompiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.util.InterpreterUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

@SuppressWarnings({"UnstableApiUsage", "ResultOfMethodCallIgnored", "deprecation"})
public class ForgeFlowerBasedDecompiler implements IDecompiler
{
    private final Logger logger = LogManager.getLogger();
    private final File   outputPath;

    public ForgeFlowerBasedDecompiler(File outputPath)
    {
        this.outputPath = outputPath;
        if (this.outputPath.exists())
        {
            try
            {
                MoreFiles.deleteRecursively(this.outputPath.toPath(), RecursiveDeleteOption.ALLOW_INSECURE);
            }
            catch (IOException ex)
            {
                this.logger.debug("Error cleaning output directory: {}", ex.getMessage());
            }
        }
    }

    @Override
    public void decompile(final File file)
    {
        try
        {
            final Fernflower fernflower = ForgeFlowerDecompilerBuilder.getInstance().build(
              "FileCompiler",
              () -> {
                  try
                  {
                      return InterpreterUtil.getBytes(file);
                  }
                  catch (IOException e)
                  {
                      throw new IllegalStateException("Could not read bytes from input file: " + file.getAbsolutePath(), e);
                  }
              },
              new AbstractResultSaver()
              {
                  @Override
                  public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping)
                  {
                      File file = new File(outputPath, qualifiedName + ".java");
                      file.getParentFile().mkdirs();
                      try
                      {
                          logger.info("Writing {}", file.getAbsolutePath());
                          com.google.common.io.Files.write(content, file, Charsets.UTF_8);
                      }
                      catch (IOException ex)
                      {
                          logger.error("Cannot write source file " + file, ex);
                      }
                  }
              },
              file
            );

            try
            {
                // New fernflower (including forgeflower)
                Method mdAddSource = fernflower.getClass().getDeclaredMethod("addSource", File.class);
                mdAddSource.invoke(fernflower, file);
            }
            catch (ReflectiveOperationException ex)
            {
                // Old fernflower
                throw new IllegalStateException("Fernflower is too old", ex);
            }

            fernflower.decompileContext();
        }
        catch (Throwable ex)
        {
            this.logger.warn("Decompilation error while processing {}", file.getName());
        }
    }
}

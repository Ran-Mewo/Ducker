package net.minecraftforge.ducker.decompile.forgeflower;

import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jetbrains.java.decompiler.util.InterpreterUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public final class ForgeFlowerDecompilerBuilder
{
    private static final ForgeFlowerDecompilerBuilder INSTANCE = new ForgeFlowerDecompilerBuilder();

    private static final Map<String, Object> DEFAULT_FORGE_FLOWER_OPTIONS = ImmutableMap.<String, Object>builder()
      .put(IFernflowerPreferences.DECOMPILE_INNER,              "0")
      .put(IFernflowerPreferences.REMOVE_BRIDGE,                "0")
      .put(IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES, "1")
      .put(IFernflowerPreferences.ASCII_STRING_CHARACTERS,      "1")
      .put(IFernflowerPreferences.DECOMPILE_ENUM,               "1")
      .put(IFernflowerPreferences.HIDE_DEFAULT_CONSTRUCTOR,     "1")
      .put(IFernflowerPreferences.INDENT_STRING,                "    ")
      .put(IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1")
      .build();

    public static ForgeFlowerDecompilerBuilder getInstance()
    {
        return INSTANCE;
    }

    private ForgeFlowerDecompilerBuilder()
    {
    }

    public Fernflower build(final String name, final Supplier<byte[]> classByteSupplier, final IResultSaver saver, final File decompileTarget, final Set<File> additionalFiles) {
        return build(name, classByteSupplier, saver, () -> DEFAULT_FORGE_FLOWER_OPTIONS, decompileTarget, additionalFiles);
    }

    public Fernflower build(final String name, final Supplier<byte[]> classByteSupplier, final IResultSaver saver, final Supplier<Map<String, Object>> optionsBuilder, final File decompileTarget, final Set<File> additionalFiles) {
        final Logger logger = LogManager.getLogger("Decompiler: " + name);

        final Fernflower fernflower = new Fernflower((externalPath, internalPath) -> {
            byte[] bytes = classByteSupplier.get();

            if (bytes == null)
            {
                bytes = InterpreterUtil.getBytes(new File(externalPath));
            }
            return bytes;
        }, saver, optionsBuilder.get(), new IFernflowerLogger() {
            @Override
            public void writeMessage(final String message, final Severity severity)
            {
                switch (severity) {
                    case TRACE -> logger.trace(message);
                    case INFO -> logger.info(message);
                    case WARN -> logger.warn(message);
                    case ERROR -> logger.error(message);
                }
            }

            @Override
            public void writeMessage(final String message, final Severity severity, final Throwable t)
            {
                switch (severity) {
                    case TRACE -> logger.trace(message, t);
                    case INFO -> logger.info(message, t);
                    case WARN -> logger.warn(message, t);
                    case ERROR -> logger.error(message, t);
                }
            }
        });

        try {
            // New fernflower (including forgeflower)
            Method mdAddSource = fernflower.getClass().getDeclaredMethod("addSource", File.class);
            mdAddSource.invoke(fernflower, decompileTarget);

            for (File additionalFile : additionalFiles) {
                mdAddSource.invoke(fernflower, additionalFile);
            }
        } catch (ReflectiveOperationException ex) {
            // Old fernflower
            throw new IllegalStateException("Fernflower is too old", ex);
        }

        return fernflower;
    }
}

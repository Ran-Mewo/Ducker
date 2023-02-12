package net.minecraftforge.ducker.decompile;

import java.io.File;
import java.util.Set;

public class NoopDecompiler implements IDecompiler {

    public static final NoopDecompiler INSTANCE = new NoopDecompiler();

    private NoopDecompiler() {
    }

    @Override
    public void decompile(File file, Set<File> additionalFiles) {
        // NOOP
    }
}

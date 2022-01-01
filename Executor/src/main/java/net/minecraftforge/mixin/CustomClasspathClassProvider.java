package net.minecraftforge.mixin;

import org.spongepowered.asm.service.IClassProvider;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class CustomClasspathClassProvider implements IClassProvider, Closeable
{

    private final URL[] classpathEntries;
    private final URLClassLoader urlClassLoader;

    public CustomClasspathClassProvider(final URL[] classpathEntries) {
        this.classpathEntries = classpathEntries;
        this.urlClassLoader = new URLClassLoader(classpathEntries);
    }

    @Override
    public URL[] getClassPath()
    {
        return classpathEntries;
    }

    @Override
    public Class<?> findClass(final String name) throws ClassNotFoundException
    {
        return Class.forName(name, false, this.urlClassLoader);
    }

    @Override
    public Class<?> findClass(final String name, final boolean initialize) throws ClassNotFoundException
    {
        return Class.forName(name, initialize, this.urlClassLoader);
    }

    @Override
    public Class<?> findAgentClass(final String name, final boolean initialize) throws ClassNotFoundException
    {
        return Class.forName(name, initialize, this.urlClassLoader);
    }

    @Override
    public void close() throws IOException
    {
        this.urlClassLoader.close();
    }
}

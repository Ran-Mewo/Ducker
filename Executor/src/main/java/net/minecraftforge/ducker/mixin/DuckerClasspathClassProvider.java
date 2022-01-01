package net.minecraftforge.ducker.mixin;

import org.spongepowered.asm.service.IClassProvider;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class DuckerClasspathClassProvider implements IClassProvider, Closeable
{
    private URL[] classpathEntries = new URL[0];
    private URLClassLoader urlClassLoader = new URLClassLoader("Ducker dummy", classpathEntries, this.getClassPath().getClass().getClassLoader());

    public DuckerClasspathClassProvider() {
    }

    public void setup(final URL[] classpathEntries) {
        this.classpathEntries = classpathEntries;
        this.urlClassLoader = new URLClassLoader("Ducker runtime", classpathEntries, this.getClass().getClassLoader());
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

    public URLClassLoader getUrlClassLoader() {
        return urlClassLoader;
    }

    @Override
    public void close() throws IOException
    {
        this.urlClassLoader.close();
    }
}

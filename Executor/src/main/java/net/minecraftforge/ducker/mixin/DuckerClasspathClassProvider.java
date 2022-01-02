package net.minecraftforge.ducker.mixin;

import org.spongepowered.asm.service.IClassProvider;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class DuckerClasspathClassProvider implements IClassProvider, Closeable
{
    private URL[]          fullClassPathEntries     = new URL[0];
    private URLClassLoader fullClassPathClassLoader = new URLClassLoader("Ducker dummy", fullClassPathEntries, this.getClass().getClassLoader());
    private URL[] runtimeClassPathEntries = new URL[0];
    private URLClassLoader runtimeClassPathClassLoader = new URLClassLoader("Ducker runtime dummy", runtimeClassPathEntries, this.getClass().getClassLoader());

    public DuckerClasspathClassProvider() {
    }

    public void setup(final URL[] classpathEntries, final URL[] runtimeClassPathEntries) {
        this.fullClassPathEntries = classpathEntries;
        this.fullClassPathClassLoader = new URLClassLoader("Ducker full runtime", classpathEntries, this.getClass().getClassLoader());

        this.runtimeClassPathEntries = runtimeClassPathEntries;
        this.runtimeClassPathClassLoader = new URLClassLoader("Ducker runtime", runtimeClassPathEntries, this.getClass().getClassLoader());

    }

    @Override
    public URL[] getClassPath()
    {
        return fullClassPathEntries;
    }

    @Override
    public Class<?> findClass(final String name) throws ClassNotFoundException
    {
        return Class.forName(name, false, this.fullClassPathClassLoader);
    }

    @Override
    public Class<?> findClass(final String name, final boolean initialize) throws ClassNotFoundException
    {
        return Class.forName(name, initialize, this.fullClassPathClassLoader);
    }

    @Override
    public Class<?> findAgentClass(final String name, final boolean initialize) throws ClassNotFoundException
    {
        return Class.forName(name, initialize, this.fullClassPathClassLoader);
    }

    public URLClassLoader getFullClassLoader() {
        return fullClassPathClassLoader;
    }

    public URLClassLoader getRuntimeClassPathClassLoader()
    {
        return runtimeClassPathClassLoader;
    }

    @Override
    public void close() throws IOException
    {
        this.fullClassPathClassLoader.close();
    }
}

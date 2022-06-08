package net.minecraftforge.ducker.decompile.forgeflower;

import org.jetbrains.java.decompiler.main.extern.IResultSaver;

import java.util.jar.Manifest;

public abstract class AbstractResultSaver implements IResultSaver
{
    @Override
    public void saveFolder(final String path)
    {

    }

    @Override
    public void copyFile(final String source, final String path, final String entryName)
    {

    }

    @Override
    public void saveClassFile(final String path, final String qualifiedName, final String entryName, final String content, final int[] mapping)
    {

    }

    @Override
    public void createArchive(final String path, final String archiveName, final Manifest manifest)
    {

    }

    @Override
    public void saveDirEntry(final String path, final String archiveName, final String entryName)
    {

    }

    @Override
    public void copyEntry(final String source, final String path, final String archiveName, final String entry)
    {

    }

    @Override
    public void closeArchive(final String path, final String archiveName)
    {

    }
}

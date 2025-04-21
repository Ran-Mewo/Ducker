package net.minecraftforge.ducker.util;

import net.fabricmc.accesswidener.AccessWidener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AccessWidenerUtilTest {

    @TempDir
    Path tempDir;

    @Test
    public void testCreateAccessWidenerWithValidFiles() throws IOException {
        // Create test access widener files
        File aw1 = tempDir.resolve("test1.accesswidener").toFile();
        try (FileWriter writer = new FileWriter(aw1)) {
            writer.write("accessWidener v1 named\n");
            writer.write("accessible class net/test/Class1\n");
            writer.write("accessible method net/test/Class1 method1 ()V\n");
        }

        File aw2 = tempDir.resolve("test2.accesswidener").toFile();
        try (FileWriter writer = new FileWriter(aw2)) {
            writer.write("accessWidener v1 named\n");
            writer.write("accessible class net/test/Class2\n");
            writer.write("accessible field net/test/Class2 field1 I\n");
        }

        // Test the utility
        AccessWidener accessWidener = AccessWidenerUtil.createAccessWidener(
                List.of(aw1.getAbsolutePath(), aw2.getAbsolutePath()));

        // Verify
        assertNotNull(accessWidener);
        assertTrue(accessWidener.getTargets().contains("net.test.Class1"));
        assertTrue(accessWidener.getTargets().contains("net.test.Class2"));
    }

    @Test
    public void testCreateAccessWidenerWithNonExistentFile() {
        // Test with a non-existent file
        AccessWidener accessWidener = AccessWidenerUtil.createAccessWidener(
                List.of(tempDir.resolve("nonexistent.accesswidener").toString()));

        // Verify - should still create an AccessWidener, just with no targets
        assertNotNull(accessWidener);
        assertTrue(accessWidener.getTargets().isEmpty());
    }

    @Test
    public void testCreateAccessWidenerWithEmptyList() {
        // Test with an empty list
        AccessWidener accessWidener = AccessWidenerUtil.createAccessWidener(List.of());

        // Verify
        assertNotNull(accessWidener);
        assertTrue(accessWidener.getTargets().isEmpty());
    }
}

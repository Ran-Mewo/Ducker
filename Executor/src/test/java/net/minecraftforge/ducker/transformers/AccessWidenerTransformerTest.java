package net.minecraftforge.ducker.transformers;

import net.fabricmc.accesswidener.AccessWidener;
import net.fabricmc.accesswidener.AccessWidenerReader;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class AccessWidenerTransformerTest {

    @Test
    public void testTransformWithTargetClass() throws Exception {
        // Setup
        AccessWidener accessWidener = new AccessWidener();
        AccessWidenerReader reader = new AccessWidenerReader(accessWidener);
        reader.read("accessWidener v1 named\naccessible class test/TestClass\n".getBytes(StandardCharsets.UTF_8));
        
        AccessWidenerTransformer transformer = new AccessWidenerTransformer(accessWidener);
        
        ClassNode node = new ClassNode();
        node.name = "test/TestClass";
        
        ClassVisitor previous = new ClassWriter(0);
        
        // Execute
        ClassVisitor result = transformer.transform(node, previous);
        
        // Verify
        assertNotNull(result);
        // The result should be an AccessWidenerClassVisitor wrapping the previous visitor
        // but we can't directly check the type due to package access restrictions
        // So we just verify it's not the same as the input visitor
        assertNotSame(previous, result);
    }
    
    @Test
    public void testTransformWithNonTargetClass() {
        // Setup
        AccessWidener accessWidener = new AccessWidener();
        AccessWidenerTransformer transformer = new AccessWidenerTransformer(accessWidener);
        
        ClassNode node = new ClassNode();
        node.name = "test/NonTargetClass";
        
        ClassVisitor previous = new ClassWriter(0);
        
        // Execute
        ClassVisitor result = transformer.transform(node, previous);
        
        // Verify
        assertNotNull(result);
        // For non-target classes, the transformer should just return the previous visitor
        assertSame(previous, result);
    }
    
    private void assertNotSame(Object expected, Object actual) {
        if (expected == actual) {
            throw new AssertionError("Expected objects to be different instances");
        }
    }
}

package net.minecraftforge.ducker.mixin.classes;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

/**
 * Interface for shunting classes through other parts of the Mixin subsystem
 * when they arrive at the plugin
 */
public interface IClassProcessor
{

    /**
     * Callback from the mixin plugin
     * 
     * @param classNode the classnode to process
     * @param classType the name of the class
     * @param reason the reason for processing
     * @return true if the class was processed
     */
    boolean processClass(ClassNode classNode, Type classType, String reason);
    
    /**
     * Returns whether this generator can generate the specified class
     * 
     * @param classType Class to generate
     * @return true if this generator can generate the class
     */
    boolean generatesClass(Type classType);
    
    /**
     * Generate the specified class
     * 
     * @param classType Class to generate
     * @param classNode ClassNode to populate with the new class data
     * @return true if the class was generated
     */
    boolean generateClass(Type classType, ClassNode classNode);

}

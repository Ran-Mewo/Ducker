package net.minecraftforge.ducker.tests.mixin;

import net.minecraftforge.ducker.tests.SomeClass;
import net.minecraftforge.ducker.tests.extensions.IForgeSomeClass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SomeClass.class)
public class SomeClassMixin implements IForgeSomeClass {
    @Unique
    private String myField;
    @Override
    public SomeClass self() {
        return (SomeClass) (Object) this;
    }
}

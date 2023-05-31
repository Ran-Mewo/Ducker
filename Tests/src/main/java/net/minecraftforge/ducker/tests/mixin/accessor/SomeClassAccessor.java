package net.minecraftforge.ducker.tests.mixin.accessor;

import net.minecraftforge.ducker.tests.SomeClass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SomeClass.class)
public interface SomeClassAccessor {
    @Accessor
    String getIt();

    @Invoker
    static String invokeMe() {
        throw new IllegalArgumentException();
    }
}

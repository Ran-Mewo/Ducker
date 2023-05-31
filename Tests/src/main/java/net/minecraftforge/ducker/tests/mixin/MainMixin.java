package net.minecraftforge.ducker.tests.mixin;

import net.minecraftforge.ducker.tests.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MainMixin {
    @Inject(at = @At(value = "INVOKE", target = "Ljava/io/PrintStream;(Ljava/lang/String;)V", ordinal = 0, shift = At.Shift.AFTER), method = "main")
    private static void whenInvoke(String[] args, CallbackInfo ci) {
        System.out.println("Hi again!");
    }
}

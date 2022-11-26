package net.minecraftforge.ducker.digger.mixins;

import net.minecraftforge.ducker.digger.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Main.class)
public class MainMixin {

    @ModifyArg(
      method = "echo(Ljava/lang/String;)V",
      at = @At(value = "INVOKE", target = "Ljava/io/PrintStream;println(Ljava/lang/String;)V", ordinal = 0),
      remap = false)
    private String modify(String str) {
        return "2";
    }
}
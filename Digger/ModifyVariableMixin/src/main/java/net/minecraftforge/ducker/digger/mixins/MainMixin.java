package net.minecraftforge.ducker.digger.mixins;

import net.minecraftforge.ducker.digger.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Main.class)
public class MainMixin {

    @ModifyVariable(
      method = "echo(Ljava/lang/String;)V",
      at = @At(value = "LOAD", ordinal = 0),
      remap = false,
      argsOnly = true)
    private String modify(String str) {
        return "2";
    }

}
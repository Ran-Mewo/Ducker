package net.minecraftforge.ducker.digger.modifyvariable.mixins;

import net.minecraftforge.ducker.digger.modifyvariable.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Main.class)
public class MainMixin {

    @ModifyVariable(
      method = "echo(Ljava/lang/String;)V",
      at = @At(value = "INVOKE", target = "Ljava/io/PrintStream;println(Ljava/lang/String;)V"),
      index = 1,
      remap = false,
      argsOnly = true)
    private String modify(String str) {
        return "2";
    }

}
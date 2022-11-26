package net.minecraftforge.ducker.digger.mixins;

import net.minecraftforge.ducker.digger.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Main.class)
public class MainMixin {

    @ModifyConstant(
      method = "main([Ljava/lang/String;)V",
      constant = @Constant(intValue = 6))
    private static int modify(int str) {
        return ++str;
    }
}
package net.minecraftforge.ducker.digger.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraftforge.ducker.digger.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Main.class)
public class MainMixin {


    @ModifyExpressionValue(
            method = "echo(Ljava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/ducker/digger/Main;shouldEcho()Z"
            )
    )
    private boolean additionalShouldEcho(boolean originalValue, String otherString) {
        return otherString.length() > 0;
    }

}
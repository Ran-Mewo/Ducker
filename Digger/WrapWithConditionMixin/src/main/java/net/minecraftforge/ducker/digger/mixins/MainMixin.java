package net.minecraftforge.ducker.digger.mixins;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraftforge.ducker.digger.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Main.class)
public class MainMixin {


    @WrapWithCondition(
            method = "Lnet/minecraftforge/ducker/digger/Main;echo(Ljava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/ducker/digger/Main;internalEcho()V"
            )
    )
    private boolean onlyInternalEchoIf(Main instance, String otherValue) {
        return otherValue.length() > 0;
    }

}
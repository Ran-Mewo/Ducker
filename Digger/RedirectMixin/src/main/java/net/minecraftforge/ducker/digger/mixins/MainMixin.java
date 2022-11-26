package net.minecraftforge.ducker.digger.mixins;

import net.minecraftforge.ducker.digger.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Main.class)
public class MainMixin {


    @Redirect(
            method = "Lnet/minecraftforge/ducker/digger/Main;echo(Ljava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/ducker/digger/Main;internalEcho()V"
            )
    )
    private void modify(Main instance) {
        System.out.println("Something from the mixin!");
    }

}
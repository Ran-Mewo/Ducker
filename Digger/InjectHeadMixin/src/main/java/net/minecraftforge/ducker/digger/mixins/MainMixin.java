package net.minecraftforge.ducker.digger.mixins;

import net.minecraftforge.ducker.digger.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public abstract class MainMixin
{
    @Inject(
      method = "main",
      at = @At(
        value = "HEAD"
      )
    )
    private static void onMain(final String[] args, final CallbackInfo ci)
    {
        System.out.println("Hello from mixin!");
    }
}

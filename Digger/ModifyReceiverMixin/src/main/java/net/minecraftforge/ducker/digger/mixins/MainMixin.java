package net.minecraftforge.ducker.digger.mixins;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.minecraftforge.ducker.digger.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public abstract class MainMixin
{
    @ModifyReceiver(
      method = "echo(Ljava/lang/String;)V",
      at = @At(
        value = "INVOKE", target = "Ljava/lang/String;substring(I)Ljava/lang/String;"
      )
    )
    private static String onEcho(final String input, final int substringIndexFrom)
    {
        return "New String that is definitely not the input! But this is the input: " + input;
    }
}

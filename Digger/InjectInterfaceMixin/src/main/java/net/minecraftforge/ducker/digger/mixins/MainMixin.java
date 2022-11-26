package net.minecraftforge.ducker.digger.mixins;

import net.minecraftforge.ducker.digger.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Main.class)
public class MainMixin implements IExtendedMain {


    @Redirect(
            method = "Lnet/minecraftforge/ducker/digger/Main;echo(Ljava/lang/String;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/ducker/digger/Main;internalEcho()V"
            )
    )
    private void modify(Main instance) {
        extendedEcho(instance.toString());
    }

    @Override
    public void extendedEcho(String otherValue) {
        System.out.println(otherValue);
        System.out.println("Hello from the interface!");
    }
}
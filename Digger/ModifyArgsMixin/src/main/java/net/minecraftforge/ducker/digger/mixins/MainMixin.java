package net.minecraftforge.ducker.digger.mixins;

import net.minecraftforge.ducker.digger.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.List;

@Mixin(Main.class)
public class MainMixin {

    @ModifyArgs(
      method = "echo(Ljava/lang/String;Ljava/lang/String;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraftforge/ducker/digger/Main;echoInternal(Ljava/lang/String;ILjava/util/List;)V", ordinal = 0),
      remap = false)
    private void modify(Args args) {
        args.set(0, "2");
        args.set(1, 4);
        args.set(2, List.of("3"));
    }
}
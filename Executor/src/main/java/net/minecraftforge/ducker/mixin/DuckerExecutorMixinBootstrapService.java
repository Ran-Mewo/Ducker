package net.minecraftforge.ducker.mixin;

import org.spongepowered.asm.service.IMixinServiceBootstrap;

public class DuckerExecutorMixinBootstrapService implements IMixinServiceBootstrap {
    @Override
    public String getName() {
        return "Ducker";
    }

    @Override
    public String getServiceClassName() {
        return "net.minecraftforge.ducker.mixin.DuckerExecutorMixinService";
    }

    @Override
    public void bootstrap() {
        //Noop
    }
}

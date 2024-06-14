package ladysnake.requiem.mixin.common;

import ladysnake.requiem.common.damage.DamageSourcesExt;
import ladysnake.requiem.common.damage.RequiemDamageSources;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.registry.DynamicRegistryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DamageSources.class)
public class DamageSourcesMixin implements DamageSourcesExt {
    @Unique
    private RequiemDamageSources requiemDamageSources;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(DynamicRegistryManager registryManager, CallbackInfo ci) {
        this.requiemDamageSources = new RequiemDamageSources((DamageSources) (Object) this);
    }

    @SuppressWarnings("AddedMixinMembersNamePattern") // It's fine, we have a custom type
    @Override
    public RequiemDamageSources requiemSources() {
        return this.requiemDamageSources;
    }
}

package ladysnake.requiem.common.damage;

import com.mojang.authlib.GameProfile;
import ladysnake.requiem.Requiem;
import ladysnake.requiem.common.remnant.MortalDysmorphiaDamageSource;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public final class RequiemDamageSources {
    public static final RegistryKey<DamageType> MORTAL_DYSMORPHIA = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Requiem.id("mortal_dysmorphia"));
    public static final RegistryKey<DamageType> ATTRITION_HARDCORE_DEATH = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Requiem.id("attrition_hardcore_death"));

    private final DamageSources damageSources;

    public RequiemDamageSources(DamageSources damageSources) {
        this.damageSources = damageSources;
    }

    public DamageSource mortalDysmorphia(GameProfile baseIdentity, GameProfile bodyIdentity) {
        return new MortalDysmorphiaDamageSource(this.damageSources.registry.getHolderOrThrow(MORTAL_DYSMORPHIA), Text.of(baseIdentity.getName()), Text.of(bodyIdentity.getName()));
    }

    public DamageSource attritionHardcoreDeath() {
        return this.damageSources.create(ATTRITION_HARDCORE_DEATH);
    }
}

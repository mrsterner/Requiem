package ladysnake.requiem.common.damage;

import net.minecraft.entity.damage.DamageSources;

/**
 * Allows access to {@link RequiemDamageSources} from {@link DamageSources}
 *
 * <p>Interface injected into {@link DamageSources}
 */
public interface DamageSourcesExt {
    default RequiemDamageSources requiemSources() {
        throw new IllegalStateException("Not transformed");
    }
}

package ladysnake.requiem.compat;

import ladysnake.requiem.api.v1.RequiemApi;
import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.RequiemPlugin;
import ladysnake.requiem.api.v1.annotation.CalledThroughReflection;
import ladysnake.requiem.api.v1.event.requiem.PossessionStartCallback;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import moriyashiine.bewitchment.api.BewitchmentAPI;
import moriyashiine.bewitchment.api.registry.RitualFunction;
import moriyashiine.bewitchment.common.item.TaglockItem;
import moriyashiine.bewitchment.common.registry.BWComponents;
import moriyashiine.bewitchment.common.registry.BWRegistries;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class BewitchmentCompat implements RequiemPlugin {

    public static final RitualFunction DECAY = new DecayRitualFunction();

    @CalledThroughReflection
    public static void init() {
        RequiemApi.registerPlugin(new BewitchmentCompat());
        Registry.register(BWRegistries.RITUAL_FUNCTION, new Identifier("dark_rites", "decay"), DECAY);
    }

    @Override
    public void onRequiemInitialize() {
        PossessionStartCallback.EVENT.register(new Identifier("dark_rites", "allow_familiars"), (target, possessor, simulate) -> {
            NbtCompound entityTag = new NbtCompound();
            target.saveSelfNbt(entityTag);

            if (entityTag.contains("Owner") && possessor.getUuid().equals(entityTag.getUuid("Owner"))) {
                if (BWComponents.FAMILIAR_COMPONENT.get(target).isFamiliar()) {
                    return PossessionStartCallback.Result.ALLOW;
                }
            }
            return PossessionStartCallback.Result.PASS;
        });
    }

    static class DecayRitualFunction extends RitualFunction {

        public DecayRitualFunction() {
            super(ParticleTypes.ASH, null);
        }

        @Override
        public void start(ServerWorld world, BlockPos glyphPos, BlockPos effectivePos, Inventory inventory, boolean catFamiliar) {
            ItemStack taglock = null;
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.getStack(i);
                if (inventory.getStack(i).getItem() instanceof TaglockItem) {
                    taglock = stack;
                    break;
                }
            }

            if (taglock != null) {
                LivingEntity livingEntity = BewitchmentAPI.getTaglockOwner(world, taglock);

                if (livingEntity instanceof RequiemPlayer player) {
                    RemnantComponent remnant = RemnantComponent.get((PlayerEntity) player);

                    if (remnant.getRemnantType().isDemon() && !player.asPossessor().isPossessionOngoing()) {
                        MobEntity body;

                        if (inventory.count(Items.BONE) > 0) {
                            body = EntityType.SKELETON.create(livingEntity.getWorld());
                        } else {
                            body = EntityType.ZOMBIE.create(livingEntity.getWorld());
                        }

                        if (body != null) {
                            body.copyPositionAndRotation(livingEntity);
                            livingEntity.getWorld().spawnEntity(body);

                            remnant.setVagrant(true);
                            player.asPossessor().startPossessing(body);
                        }
                    }
                }
            }

            super.start(world, glyphPos, effectivePos, inventory, catFamiliar);
        }
    }
}

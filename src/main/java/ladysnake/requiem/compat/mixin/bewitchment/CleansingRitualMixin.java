package ladysnake.requiem.compat.mixin.bewitchment;

import ladysnake.requiem.api.v1.RequiemPlayer;
import ladysnake.requiem.api.v1.remnant.RemnantComponent;
import moriyashiine.bewitchment.api.BewitchmentAPI;
import moriyashiine.bewitchment.common.item.TaglockItem;
import moriyashiine.bewitchment.common.ritualfunction.CleanseRitualFunction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CleanseRitualFunction.class, remap = false)
public class CleansingRitualMixin {
    @Inject(at = @At("HEAD"), method = "start")
    private void start(ServerWorld world, BlockPos glyphPos, BlockPos effectivePos, Inventory inventory, boolean catFamiliar, CallbackInfo callbackInfo) {
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

                if (player.asPossessor().isPossessionOngoing()) {
                    RemnantComponent remnant = RemnantComponent.get((PlayerEntity) player);
                    remnant.curePossessed(player.asPossessor().getPossessedEntity());
                }
            }
        }
    }
}

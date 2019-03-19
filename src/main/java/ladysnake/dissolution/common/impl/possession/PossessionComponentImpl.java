package ladysnake.dissolution.common.impl.possession;

import com.google.common.collect.MapMaker;
import ladysnake.dissolution.Dissolution;
import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.event.PossessionStartCallback;
import ladysnake.dissolution.api.v1.possession.Possessable;
import ladysnake.dissolution.api.v1.possession.PossessionComponent;
import ladysnake.dissolution.common.entity.ai.attribute.AttributeHelper;
import ladysnake.dissolution.common.entity.ai.attribute.PossessionDelegatingAttribute;
import ladysnake.dissolution.common.impl.movement.SerializableMovementConfig;
import ladysnake.dissolution.common.tag.DissolutionEntityTags;
import ladysnake.dissolution.common.util.InventoryHelper;
import ladysnake.dissolution.mixin.entity.LivingEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AbstractEntityAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static ladysnake.dissolution.common.network.DissolutionNetworking.createPossessionMessage;
import static ladysnake.dissolution.common.network.DissolutionNetworking.sendToAllTrackingIncluding;
import static net.minecraft.util.ActionResult.PASS;
import static net.minecraft.util.ActionResult.SUCCESS;

public final class PossessionComponentImpl implements PossessionComponent {
    // Identity weak map. Should probably be made into its own util class.
    private static final Set<PlayerEntity> attributeUpdated = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());

    private final PlayerEntity player;
    @Nullable private UUID possessedUuid;
    private int possessedNetworkId;

    public PossessionComponentImpl(PlayerEntity player) {
        this.player = player;
        this.possessedNetworkId = -1;
    }

    @Override
    public boolean canStartPossessing(final MobEntity mob) {
        if (DissolutionEntityTags.POSSESSION_BLACKLIST.contains(mob.getType())) {
            return false;
        }
        DissolutionPlayer dp = (DissolutionPlayer) this.player;
        return player.world.isClient || (!player.isSpectator() && dp.isRemnant() && dp.getRemnantState().isIncorporeal());
    }

    @Override
    public boolean startPossessing(final MobEntity host) {
        // 1- check that the player can initiate possession
        if (!canStartPossessing(host)) {
            return false;
        }

        ActionResult result = PossessionStartCallback.EVENT.invoker().onPossessionAttempted(host, this.player);
        if (result != PASS) {
            return result == SUCCESS;
        }

        Possessable possessable = (Possessable) host;
        // 2- check that the mob can be possessed
        if (!possessable.canBePossessedBy(player)) {
            return false;
        }
        // 3- transfer inventory
        if (DissolutionEntityTags.ITEM_USER.contains(host.getType())) {
            InventoryHelper.transferEquipment(host, player);
        }
        // 4- Actually set the possessed entity
        this.possessedUuid = host.getUuid();
        this.possessedNetworkId = host.getEntityId();
        possessable.setPossessor(this.player);
        this.syncPossessed();
        // 5- Update some attributes
        this.player.setPositionAndAngles(host);
        this.player.refreshSize(); // update size
        ((DissolutionPlayer)this.player).getMovementAlterer().setConfig(Dissolution.getMovementAltererManager().getEntityMovementConfig(host.getType()));
        if (!attributeUpdated.contains(this.player)) {
            this.swapAttributes(this.player);
            attributeUpdated.add(this.player);
        }

        // 6- Make the mob react a bit
        host.playAmbientSound();
        return true;
    }

    private void swapAttributes(PlayerEntity player) {
        AbstractEntityAttributeContainer attributeMap = player.getAttributeContainer();
        // Replace every registered attribute
        for (EntityAttributeInstance current: attributeMap.values()) {
            EntityAttributeInstance replacement = new PossessionDelegatingAttribute(attributeMap, current, this);
            AttributeHelper.substituteAttributeInstance(attributeMap, replacement);
        }
    }

    @Override
    public void stopPossessing() {
        Possessable possessed = this.getPossessedEntity();
        if (possessed != null) {
            this.possessedUuid = null;
            resetState();
            possessed.setPossessor(null);
            if (player instanceof ServerPlayerEntity && !player.isCreative()) {
                LivingEntity possessedEntity = (LivingEntity) possessed;
                if (DissolutionEntityTags.ITEM_USER.contains(possessedEntity.getType())) {
                    InventoryHelper.transferEquipment(player, (LivingEntity) possessed);
                }
                ((LivingEntityAccessor)player).invokeDropInventory();
            }
        }
    }

    private void syncPossessed() {
        if (!this.player.world.isClient) {
            sendToAllTrackingIncluding(this.player, createPossessionMessage(this.player.getUuid(), this.possessedNetworkId));
        }
    }

    @CheckForNull
    @Override
    public Possessable getPossessedEntity() {
        if (!isPossessing()) {
            return null;
        }
        // First attempt: use the network id (client & server)
        Entity host = this.player.world.getEntityById(this.possessedNetworkId);
        if (host == null) {
            if (this.player.world instanceof ServerWorld) {
                // Second attempt: use the UUID (server)
                // method_14190 == getEntityByUuid
                host = ((ServerWorld)this.player.world).getEntity(this.getPossessedEntityUuid());
            }
            // Set the possessed uuid to null to avoid infinite recursion
            this.possessedUuid = null;
            if (host instanceof MobEntity && host instanceof Possessable) {
                this.startPossessing((MobEntity) host);
            } else {
                if (host != null) {
                    Dissolution.LOGGER.warn("{}: this player's supposedly possessed entity ({}) cannot be possessed!", this.player, host);
                }
                Dissolution.LOGGER.debug("{}: this player's possessed entity is nowhere to be found", this);
                this.resetState();
                host = null;
            }
        }
        return (Possessable) host;
    }

    private void resetState() {
        this.possessedNetworkId = -1;
        ((DissolutionPlayer) this.player).getMovementAlterer().setConfig(SerializableMovementConfig.SOUL);
        this.player.refreshSize(); // update size
        this.player.setBreath(this.player.getMaxBreath());
        syncPossessed();
    }

    @Override
    public boolean isPossessing() {
        return this.possessedUuid != null;
    }

    @CheckForNull
    public UUID getPossessedEntityUuid() {
        return this.possessedUuid;
    }

}

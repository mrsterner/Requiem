package ladysnake.requiem.core.network;

import ladysnake.requiem.core.RequiemCore;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ObeliskPowerUpgradeS2CPayload implements CustomPayload {

    public static Id<ObeliskPowerUpgradeS2CPayload> ID = new Id<>(RequiemCore.id("obelisk_power_upgrade"));
    public static final PacketCodec<? super PacketByteBuf, ObeliskPowerUpgradeS2CPayload> STREAM_CODEC = CustomPayload.codecOf(ObeliskPowerUpgradeS2CPayload::write, ObeliskPowerUpgradeS2CPayload::new);
    public final BlockPos controllerPos;
    public final int coreWidth;
    public final int coreHeight;
    public final float powerRate;

    private void write(PacketByteBuf buf) {
        buf.writeBlockPos(controllerPos);
        buf.writeVarInt(coreWidth);
        buf.writeVarInt(coreHeight);
        buf.writeFloat(powerRate);
    }

    public ObeliskPowerUpgradeS2CPayload(PacketByteBuf buf) {
        controllerPos = buf.readBlockPos();
        coreWidth = buf.readVarInt();
        coreHeight = buf.readVarInt();
        powerRate = buf.readFloat();
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

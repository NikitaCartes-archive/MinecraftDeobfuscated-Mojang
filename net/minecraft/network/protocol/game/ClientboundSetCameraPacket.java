/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ClientboundSetCameraPacket
implements Packet<ClientGamePacketListener> {
    private final int cameraId;

    public ClientboundSetCameraPacket(Entity entity) {
        this.cameraId = entity.getId();
    }

    public ClientboundSetCameraPacket(FriendlyByteBuf friendlyByteBuf) {
        this.cameraId = friendlyByteBuf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.cameraId);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetCamera(this);
    }

    @Nullable
    @Environment(value=EnvType.CLIENT)
    public Entity getEntity(Level level) {
        return level.getEntity(this.cameraId);
    }
}


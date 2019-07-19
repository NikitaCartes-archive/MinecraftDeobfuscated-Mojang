/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;

public class ClientboundMoveVehiclePacket
implements Packet<ClientGamePacketListener> {
    private double x;
    private double y;
    private double z;
    private float yRot;
    private float xRot;

    public ClientboundMoveVehiclePacket() {
    }

    public ClientboundMoveVehiclePacket(Entity entity) {
        this.x = entity.x;
        this.y = entity.y;
        this.z = entity.z;
        this.yRot = entity.yRot;
        this.xRot = entity.xRot;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.x = friendlyByteBuf.readDouble();
        this.y = friendlyByteBuf.readDouble();
        this.z = friendlyByteBuf.readDouble();
        this.yRot = friendlyByteBuf.readFloat();
        this.xRot = friendlyByteBuf.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeDouble(this.x);
        friendlyByteBuf.writeDouble(this.y);
        friendlyByteBuf.writeDouble(this.z);
        friendlyByteBuf.writeFloat(this.yRot);
        friendlyByteBuf.writeFloat(this.xRot);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleMoveVehicle(this);
    }

    @Environment(value=EnvType.CLIENT)
    public double getX() {
        return this.x;
    }

    @Environment(value=EnvType.CLIENT)
    public double getY() {
        return this.y;
    }

    @Environment(value=EnvType.CLIENT)
    public double getZ() {
        return this.z;
    }

    @Environment(value=EnvType.CLIENT)
    public float getYRot() {
        return this.yRot;
    }

    @Environment(value=EnvType.CLIENT)
    public float getXRot() {
        return this.xRot;
    }
}


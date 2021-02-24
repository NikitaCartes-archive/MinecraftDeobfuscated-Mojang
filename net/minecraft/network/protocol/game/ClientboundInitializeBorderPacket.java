/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundInitializeBorderPacket
implements Packet<ClientGamePacketListener> {
    private final double newCenterX;
    private final double newCenterZ;
    private final double oldSize;
    private final double newSize;
    private final long lerpTime;
    private final int newAbsoluteMaxSize;
    private final int warningBlocks;
    private final int warningTime;

    public ClientboundInitializeBorderPacket(FriendlyByteBuf friendlyByteBuf) {
        this.newCenterX = friendlyByteBuf.readDouble();
        this.newCenterZ = friendlyByteBuf.readDouble();
        this.oldSize = friendlyByteBuf.readDouble();
        this.newSize = friendlyByteBuf.readDouble();
        this.lerpTime = friendlyByteBuf.readVarLong();
        this.newAbsoluteMaxSize = friendlyByteBuf.readVarInt();
        this.warningBlocks = friendlyByteBuf.readVarInt();
        this.warningTime = friendlyByteBuf.readVarInt();
    }

    public ClientboundInitializeBorderPacket(WorldBorder worldBorder) {
        this.newCenterX = worldBorder.getCenterX();
        this.newCenterZ = worldBorder.getCenterZ();
        this.oldSize = worldBorder.getSize();
        this.newSize = worldBorder.getLerpTarget();
        this.lerpTime = worldBorder.getLerpRemainingTime();
        this.newAbsoluteMaxSize = worldBorder.getAbsoluteMaxSize();
        this.warningBlocks = worldBorder.getWarningBlocks();
        this.warningTime = worldBorder.getWarningTime();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeDouble(this.newCenterX);
        friendlyByteBuf.writeDouble(this.newCenterZ);
        friendlyByteBuf.writeDouble(this.oldSize);
        friendlyByteBuf.writeDouble(this.newSize);
        friendlyByteBuf.writeVarLong(this.lerpTime);
        friendlyByteBuf.writeVarInt(this.newAbsoluteMaxSize);
        friendlyByteBuf.writeVarInt(this.warningBlocks);
        friendlyByteBuf.writeVarInt(this.warningTime);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleInitializeBorder(this);
    }

    @Environment(value=EnvType.CLIENT)
    public double getNewCenterX() {
        return this.newCenterX;
    }

    @Environment(value=EnvType.CLIENT)
    public double getNewCenterZ() {
        return this.newCenterZ;
    }

    @Environment(value=EnvType.CLIENT)
    public double getNewSize() {
        return this.newSize;
    }

    @Environment(value=EnvType.CLIENT)
    public double getOldSize() {
        return this.oldSize;
    }

    @Environment(value=EnvType.CLIENT)
    public long getLerpTime() {
        return this.lerpTime;
    }

    @Environment(value=EnvType.CLIENT)
    public int getNewAbsoluteMaxSize() {
        return this.newAbsoluteMaxSize;
    }

    @Environment(value=EnvType.CLIENT)
    public int getWarningTime() {
        return this.warningTime;
    }

    @Environment(value=EnvType.CLIENT)
    public int getWarningBlocks() {
        return this.warningBlocks;
    }
}


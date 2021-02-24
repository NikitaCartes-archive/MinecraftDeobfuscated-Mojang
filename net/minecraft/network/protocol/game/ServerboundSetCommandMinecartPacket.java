/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ServerboundSetCommandMinecartPacket
implements Packet<ServerGamePacketListener> {
    private final int entity;
    private final String command;
    private final boolean trackOutput;

    @Environment(value=EnvType.CLIENT)
    public ServerboundSetCommandMinecartPacket(int i, String string, boolean bl) {
        this.entity = i;
        this.command = string;
        this.trackOutput = bl;
    }

    public ServerboundSetCommandMinecartPacket(FriendlyByteBuf friendlyByteBuf) {
        this.entity = friendlyByteBuf.readVarInt();
        this.command = friendlyByteBuf.readUtf();
        this.trackOutput = friendlyByteBuf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.entity);
        friendlyByteBuf.writeUtf(this.command);
        friendlyByteBuf.writeBoolean(this.trackOutput);
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleSetCommandMinecart(this);
    }

    @Nullable
    public BaseCommandBlock getCommandBlock(Level level) {
        Entity entity = level.getEntity(this.entity);
        if (entity instanceof MinecartCommandBlock) {
            return ((MinecartCommandBlock)entity).getCommandBlock();
        }
        return null;
    }

    public String getCommand() {
        return this.command;
    }

    public boolean isTrackOutput() {
        return this.trackOutput;
    }
}


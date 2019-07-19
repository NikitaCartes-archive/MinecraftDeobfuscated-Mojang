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

public class ClientboundSetExperiencePacket
implements Packet<ClientGamePacketListener> {
    private float experienceProgress;
    private int totalExperience;
    private int experienceLevel;

    public ClientboundSetExperiencePacket() {
    }

    public ClientboundSetExperiencePacket(float f, int i, int j) {
        this.experienceProgress = f;
        this.totalExperience = i;
        this.experienceLevel = j;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.experienceProgress = friendlyByteBuf.readFloat();
        this.experienceLevel = friendlyByteBuf.readVarInt();
        this.totalExperience = friendlyByteBuf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeFloat(this.experienceProgress);
        friendlyByteBuf.writeVarInt(this.experienceLevel);
        friendlyByteBuf.writeVarInt(this.totalExperience);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetExperience(this);
    }

    @Environment(value=EnvType.CLIENT)
    public float getExperienceProgress() {
        return this.experienceProgress;
    }

    @Environment(value=EnvType.CLIENT)
    public int getTotalExperience() {
        return this.totalExperience;
    }

    @Environment(value=EnvType.CLIENT)
    public int getExperienceLevel() {
        return this.experienceLevel;
    }
}


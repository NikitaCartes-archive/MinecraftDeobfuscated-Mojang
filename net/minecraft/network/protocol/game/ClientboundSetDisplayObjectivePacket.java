/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.scores.Objective;
import org.jetbrains.annotations.Nullable;

public class ClientboundSetDisplayObjectivePacket
implements Packet<ClientGamePacketListener> {
    private int slot;
    private String objectiveName;

    public ClientboundSetDisplayObjectivePacket() {
    }

    public ClientboundSetDisplayObjectivePacket(int i, @Nullable Objective objective) {
        this.slot = i;
        this.objectiveName = objective == null ? "" : objective.getName();
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.slot = friendlyByteBuf.readByte();
        this.objectiveName = friendlyByteBuf.readUtf(16);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeByte(this.slot);
        friendlyByteBuf.writeUtf(this.objectiveName);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetDisplayObjective(this);
    }

    @Environment(value=EnvType.CLIENT)
    public int getSlot() {
        return this.slot;
    }

    @Nullable
    @Environment(value=EnvType.CLIENT)
    public String getObjectiveName() {
        return Objects.equals(this.objectiveName, "") ? null : this.objectiveName;
    }
}


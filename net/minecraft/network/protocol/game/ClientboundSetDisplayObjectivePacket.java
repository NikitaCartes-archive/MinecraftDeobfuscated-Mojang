/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.scores.Objective;
import org.jetbrains.annotations.Nullable;

public class ClientboundSetDisplayObjectivePacket
implements Packet<ClientGamePacketListener> {
    private final int slot;
    private final String objectiveName;

    public ClientboundSetDisplayObjectivePacket(int i, @Nullable Objective objective) {
        this.slot = i;
        this.objectiveName = objective == null ? "" : objective.getName();
    }

    public ClientboundSetDisplayObjectivePacket(FriendlyByteBuf friendlyByteBuf) {
        this.slot = friendlyByteBuf.readByte();
        this.objectiveName = friendlyByteBuf.readUtf(16);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByte(this.slot);
        friendlyByteBuf.writeUtf(this.objectiveName);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetDisplayObjective(this);
    }

    public int getSlot() {
        return this.slot;
    }

    @Nullable
    public String getObjectiveName() {
        return Objects.equals(this.objectiveName, "") ? null : this.objectiveName;
    }
}


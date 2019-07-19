/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundEntityTagQuery;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DebugQueryHandler {
    private final ClientPacketListener connection;
    private int transactionId = -1;
    @Nullable
    private Consumer<CompoundTag> callback;

    public DebugQueryHandler(ClientPacketListener clientPacketListener) {
        this.connection = clientPacketListener;
    }

    public boolean handleResponse(int i, @Nullable CompoundTag compoundTag) {
        if (this.transactionId == i && this.callback != null) {
            this.callback.accept(compoundTag);
            this.callback = null;
            return true;
        }
        return false;
    }

    private int startTransaction(Consumer<CompoundTag> consumer) {
        this.callback = consumer;
        return ++this.transactionId;
    }

    public void queryEntityTag(int i, Consumer<CompoundTag> consumer) {
        int j = this.startTransaction(consumer);
        this.connection.send(new ServerboundEntityTagQuery(j, i));
    }

    public void queryBlockEntityTag(BlockPos blockPos, Consumer<CompoundTag> consumer) {
        int i = this.startTransaction(consumer);
        this.connection.send(new ServerboundBlockEntityTagQuery(i, blockPos));
    }
}


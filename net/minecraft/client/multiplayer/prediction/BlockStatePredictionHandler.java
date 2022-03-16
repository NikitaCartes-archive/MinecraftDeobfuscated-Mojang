/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer.prediction;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class BlockStatePredictionHandler
implements AutoCloseable {
    private final Long2ObjectOpenHashMap<ServerVerifiedState> serverVerifiedStates = new Long2ObjectOpenHashMap();
    private int currentSequenceNr;
    private boolean isPredicting;

    public void retainKnownServerState(BlockPos blockPos, BlockState blockState, LocalPlayer localPlayer) {
        this.serverVerifiedStates.compute(blockPos.asLong(), (long_, serverVerifiedState) -> {
            if (serverVerifiedState != null) {
                return serverVerifiedState.setSequence(this.currentSequenceNr);
            }
            return new ServerVerifiedState(this.currentSequenceNr, blockState, localPlayer.position());
        });
    }

    public boolean updateKnownServerState(BlockPos blockPos, BlockState blockState) {
        ServerVerifiedState serverVerifiedState = this.serverVerifiedStates.get(blockPos.asLong());
        if (serverVerifiedState == null) {
            return false;
        }
        serverVerifiedState.setBlockState(blockState);
        return true;
    }

    public void endPredictionsUpTo(int i, ClientLevel clientLevel) {
        Iterator objectIterator = this.serverVerifiedStates.long2ObjectEntrySet().iterator();
        while (objectIterator.hasNext()) {
            Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)objectIterator.next();
            ServerVerifiedState serverVerifiedState = (ServerVerifiedState)entry.getValue();
            if (serverVerifiedState.sequence > i) continue;
            BlockPos blockPos = BlockPos.of(entry.getLongKey());
            objectIterator.remove();
            clientLevel.syncBlockState(blockPos, serverVerifiedState.blockState, serverVerifiedState.playerPos);
        }
    }

    public BlockStatePredictionHandler startPredicting() {
        ++this.currentSequenceNr;
        this.isPredicting = true;
        return this;
    }

    @Override
    public void close() {
        this.isPredicting = false;
    }

    public int currentSequence() {
        return this.currentSequenceNr;
    }

    public boolean isPredicting() {
        return this.isPredicting;
    }

    @Environment(value=EnvType.CLIENT)
    static class ServerVerifiedState {
        final Vec3 playerPos;
        int sequence;
        BlockState blockState;

        ServerVerifiedState(int i, BlockState blockState, Vec3 vec3) {
            this.sequence = i;
            this.blockState = blockState;
            this.playerPos = vec3;
        }

        ServerVerifiedState setSequence(int i) {
            this.sequence = i;
            return this;
        }

        void setBlockState(BlockState blockState) {
            this.blockState = blockState;
        }
    }
}


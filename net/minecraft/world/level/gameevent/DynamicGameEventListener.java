/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.gameevent;

import java.util.function.Consumer;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import net.minecraft.world.level.gameevent.GameEventListener;
import org.jetbrains.annotations.Nullable;

public class DynamicGameEventListener {
    private final GameEventListener listener;
    @Nullable
    private SectionPos lastSection;

    public DynamicGameEventListener(GameEventListener gameEventListener) {
        this.listener = gameEventListener;
    }

    public void add(ServerLevel serverLevel) {
        this.move(serverLevel);
    }

    public void remove(ServerLevel serverLevel) {
        DynamicGameEventListener.ifChunkExists(serverLevel, this.lastSection, gameEventDispatcher -> gameEventDispatcher.unregister(this.listener));
    }

    public void move(ServerLevel serverLevel) {
        this.listener.getListenerSource().getPosition(serverLevel).map(SectionPos::of).ifPresent(sectionPos -> {
            if (this.lastSection == null || !this.lastSection.equals(sectionPos)) {
                DynamicGameEventListener.ifChunkExists(serverLevel, this.lastSection, gameEventDispatcher -> gameEventDispatcher.unregister(this.listener));
                this.lastSection = sectionPos;
                DynamicGameEventListener.ifChunkExists(serverLevel, this.lastSection, gameEventDispatcher -> gameEventDispatcher.register(this.listener));
            }
        });
    }

    private static void ifChunkExists(LevelReader levelReader, @Nullable SectionPos sectionPos, Consumer<GameEventDispatcher> consumer) {
        if (sectionPos == null) {
            return;
        }
        ChunkAccess chunkAccess = levelReader.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.FULL, false);
        if (chunkAccess != null) {
            consumer.accept(chunkAccess.getEventDispatcher(sectionPos.y()));
        }
    }
}


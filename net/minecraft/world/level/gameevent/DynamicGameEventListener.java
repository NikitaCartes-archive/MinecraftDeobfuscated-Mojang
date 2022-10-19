/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.gameevent;

import java.util.function.Consumer;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
import org.jetbrains.annotations.Nullable;

public class DynamicGameEventListener<T extends GameEventListener> {
    private T listener;
    @Nullable
    private SectionPos lastSection;

    public DynamicGameEventListener(T gameEventListener) {
        this.listener = gameEventListener;
    }

    public void add(ServerLevel serverLevel) {
        this.move(serverLevel);
    }

    public void updateListener(T gameEventListener, @Nullable Level level) {
        Object gameEventListener2 = this.listener;
        if (gameEventListener2 == gameEventListener) {
            return;
        }
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            DynamicGameEventListener.ifChunkExists(serverLevel, this.lastSection, gameEventListenerRegistry -> gameEventListenerRegistry.unregister((GameEventListener)gameEventListener2));
            DynamicGameEventListener.ifChunkExists(serverLevel, this.lastSection, gameEventListenerRegistry -> gameEventListenerRegistry.register((GameEventListener)gameEventListener));
        }
        this.listener = gameEventListener;
    }

    public T getListener() {
        return this.listener;
    }

    public void remove(ServerLevel serverLevel) {
        DynamicGameEventListener.ifChunkExists(serverLevel, this.lastSection, gameEventListenerRegistry -> gameEventListenerRegistry.unregister((GameEventListener)this.listener));
    }

    public void move(ServerLevel serverLevel) {
        this.listener.getListenerSource().getPosition(serverLevel).map(SectionPos::of).ifPresent(sectionPos -> {
            if (this.lastSection == null || !this.lastSection.equals(sectionPos)) {
                DynamicGameEventListener.ifChunkExists(serverLevel, this.lastSection, gameEventListenerRegistry -> gameEventListenerRegistry.unregister((GameEventListener)this.listener));
                this.lastSection = sectionPos;
                DynamicGameEventListener.ifChunkExists(serverLevel, this.lastSection, gameEventListenerRegistry -> gameEventListenerRegistry.register((GameEventListener)this.listener));
            }
        });
    }

    private static void ifChunkExists(LevelReader levelReader, @Nullable SectionPos sectionPos, Consumer<GameEventListenerRegistry> consumer) {
        if (sectionPos == null) {
            return;
        }
        ChunkAccess chunkAccess = levelReader.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.FULL, false);
        if (chunkAccess != null) {
            consumer.accept(chunkAccess.getListenerRegistry(sectionPos.y()));
        }
    }
}


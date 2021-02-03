/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CommonLevelAccessor;
import net.minecraft.world.level.LevelTimeAccess;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.LevelData;
import org.jetbrains.annotations.Nullable;

public interface LevelAccessor
extends CommonLevelAccessor,
LevelTimeAccess {
    @Override
    default public long dayTime() {
        return this.getLevelData().getDayTime();
    }

    public TickList<Block> getBlockTicks();

    public TickList<Fluid> getLiquidTicks();

    public LevelData getLevelData();

    public DifficultyInstance getCurrentDifficultyAt(BlockPos var1);

    default public Difficulty getDifficulty() {
        return this.getLevelData().getDifficulty();
    }

    public ChunkSource getChunkSource();

    @Override
    default public boolean hasChunk(int i, int j) {
        return this.getChunkSource().hasChunk(i, j);
    }

    public Random getRandom();

    default public void blockUpdated(BlockPos blockPos, Block block) {
    }

    public void playSound(@Nullable Player var1, BlockPos var2, SoundEvent var3, SoundSource var4, float var5, float var6);

    public void addParticle(ParticleOptions var1, double var2, double var4, double var6, double var8, double var10, double var12);

    public void levelEvent(@Nullable Player var1, int var2, BlockPos var3, int var4);

    default public void levelEvent(int i, BlockPos blockPos, int j) {
        this.levelEvent(null, i, blockPos, j);
    }

    public void gameEvent(@Nullable Entity var1, GameEvent var2, BlockPos var3);

    default public void gameEvent(GameEvent gameEvent, BlockPos blockPos) {
        this.gameEvent(null, gameEvent, blockPos);
    }

    default public void gameEvent(GameEvent gameEvent, Entity entity) {
        this.gameEvent(null, gameEvent, entity.blockPosition());
    }

    default public void gameEvent(@Nullable Entity entity, GameEvent gameEvent, Entity entity2) {
        this.gameEvent(entity, gameEvent, entity2.blockPosition());
    }
}


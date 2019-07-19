/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import org.jetbrains.annotations.Nullable;

public abstract class BiomeSource {
    private static final List<Biome> PLAYER_SPAWN_BIOMES = Lists.newArrayList(Biomes.FOREST, Biomes.PLAINS, Biomes.TAIGA, Biomes.TAIGA_HILLS, Biomes.WOODED_HILLS, Biomes.JUNGLE, Biomes.JUNGLE_HILLS);
    protected final Map<StructureFeature<?>, Boolean> supportedStructures = Maps.newHashMap();
    protected final Set<BlockState> surfaceBlocks = Sets.newHashSet();

    protected BiomeSource() {
    }

    public List<Biome> getPlayerSpawnBiomes() {
        return PLAYER_SPAWN_BIOMES;
    }

    public Biome getBiome(BlockPos blockPos) {
        return this.getBiome(blockPos.getX(), blockPos.getZ());
    }

    public abstract Biome getBiome(int var1, int var2);

    public Biome getNoiseBiome(int i, int j) {
        return this.getBiome(i << 2, j << 2);
    }

    public Biome[] getBiomeBlock(int i, int j, int k, int l) {
        return this.getBiomeBlock(i, j, k, l, true);
    }

    public abstract Biome[] getBiomeBlock(int var1, int var2, int var3, int var4, boolean var5);

    public abstract Set<Biome> getBiomesWithin(int var1, int var2, int var3);

    @Nullable
    public abstract BlockPos findBiome(int var1, int var2, int var3, List<Biome> var4, Random var5);

    public float getHeightValue(int i, int j) {
        return 0.0f;
    }

    public abstract boolean canGenerateStructure(StructureFeature<?> var1);

    public abstract Set<BlockState> getSurfaceBlocks();
}


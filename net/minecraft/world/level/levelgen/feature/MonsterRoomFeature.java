/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.slf4j.Logger;

public class MonsterRoomFeature
extends Feature<NoneFeatureConfiguration> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final EntityType<?>[] MOBS = new EntityType[]{EntityType.SKELETON, EntityType.ZOMBIE, EntityType.ZOMBIE, EntityType.SPIDER};
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

    public MonsterRoomFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
        BlockPos blockPos2;
        int u;
        int t;
        int s;
        Predicate<BlockState> predicate = Feature.isReplaceable(BlockTags.FEATURES_CANNOT_REPLACE);
        BlockPos blockPos = featurePlaceContext.origin();
        RandomSource randomSource = featurePlaceContext.random();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        int i = 3;
        int j = randomSource.nextInt(2) + 2;
        int k = -j - 1;
        int l = j + 1;
        int m = -1;
        int n = 4;
        int o = randomSource.nextInt(2) + 2;
        int p = -o - 1;
        int q = o + 1;
        int r = 0;
        for (s = k; s <= l; ++s) {
            for (t = -1; t <= 4; ++t) {
                for (u = p; u <= q; ++u) {
                    blockPos2 = blockPos.offset(s, t, u);
                    Material material = worldGenLevel.getBlockState(blockPos2).getMaterial();
                    boolean bl = material.isSolid();
                    if (t == -1 && !bl) {
                        return false;
                    }
                    if (t == 4 && !bl) {
                        return false;
                    }
                    if (s != k && s != l && u != p && u != q || t != 0 || !worldGenLevel.isEmptyBlock(blockPos2) || !worldGenLevel.isEmptyBlock(blockPos2.above())) continue;
                    ++r;
                }
            }
        }
        if (r < 1 || r > 5) {
            return false;
        }
        for (s = k; s <= l; ++s) {
            for (t = 3; t >= -1; --t) {
                for (u = p; u <= q; ++u) {
                    blockPos2 = blockPos.offset(s, t, u);
                    BlockState blockState = worldGenLevel.getBlockState(blockPos2);
                    if (s == k || t == -1 || u == p || s == l || t == 4 || u == q) {
                        if (blockPos2.getY() >= worldGenLevel.getMinBuildHeight() && !worldGenLevel.getBlockState(blockPos2.below()).getMaterial().isSolid()) {
                            worldGenLevel.setBlock(blockPos2, AIR, 2);
                            continue;
                        }
                        if (!blockState.getMaterial().isSolid() || blockState.is(Blocks.CHEST)) continue;
                        if (t == -1 && randomSource.nextInt(4) != 0) {
                            this.safeSetBlock(worldGenLevel, blockPos2, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), predicate);
                            continue;
                        }
                        this.safeSetBlock(worldGenLevel, blockPos2, Blocks.COBBLESTONE.defaultBlockState(), predicate);
                        continue;
                    }
                    if (blockState.is(Blocks.CHEST) || blockState.is(Blocks.SPAWNER)) continue;
                    this.safeSetBlock(worldGenLevel, blockPos2, AIR, predicate);
                }
            }
        }
        block6: for (s = 0; s < 2; ++s) {
            for (t = 0; t < 3; ++t) {
                int w;
                int v;
                u = blockPos.getX() + randomSource.nextInt(j * 2 + 1) - j;
                BlockPos blockPos3 = new BlockPos(u, v = blockPos.getY(), w = blockPos.getZ() + randomSource.nextInt(o * 2 + 1) - o);
                if (!worldGenLevel.isEmptyBlock(blockPos3)) continue;
                int x = 0;
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    if (!worldGenLevel.getBlockState(blockPos3.relative(direction)).getMaterial().isSolid()) continue;
                    ++x;
                }
                if (x != 1) continue;
                this.safeSetBlock(worldGenLevel, blockPos3, StructurePiece.reorient(worldGenLevel, blockPos3, Blocks.CHEST.defaultBlockState()), predicate);
                RandomizableContainerBlockEntity.setLootTable(worldGenLevel, randomSource, blockPos3, BuiltInLootTables.SIMPLE_DUNGEON);
                continue block6;
            }
        }
        this.safeSetBlock(worldGenLevel, blockPos, Blocks.SPAWNER.defaultBlockState(), predicate);
        BlockEntity blockEntity = worldGenLevel.getBlockEntity(blockPos);
        if (blockEntity instanceof SpawnerBlockEntity) {
            SpawnerBlockEntity spawnerBlockEntity = (SpawnerBlockEntity)blockEntity;
            spawnerBlockEntity.setEntityId(this.randomEntityId(randomSource), randomSource);
        } else {
            LOGGER.error("Failed to fetch mob spawner entity at ({}, {}, {})", blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }
        return true;
    }

    private EntityType<?> randomEntityId(RandomSource randomSource) {
        return Util.getRandom(MOBS, randomSource);
    }
}


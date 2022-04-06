/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.phys.AABB;

public class SpikeFeature
extends Feature<SpikeConfiguration> {
    public static final int NUMBER_OF_SPIKES = 10;
    private static final int SPIKE_DISTANCE = 42;
    private static final LoadingCache<Long, List<EndSpike>> SPIKE_CACHE = CacheBuilder.newBuilder().expireAfterWrite(5L, TimeUnit.MINUTES).build(new SpikeCacheLoader());

    public SpikeFeature(Codec<SpikeConfiguration> codec) {
        super(codec);
    }

    public static List<EndSpike> getSpikesForLevel(WorldGenLevel worldGenLevel) {
        RandomSource randomSource = RandomSource.create(worldGenLevel.getSeed());
        long l = randomSource.nextLong() & 0xFFFFL;
        return SPIKE_CACHE.getUnchecked(l);
    }

    @Override
    public boolean place(FeaturePlaceContext<SpikeConfiguration> featurePlaceContext) {
        SpikeConfiguration spikeConfiguration = featurePlaceContext.config();
        WorldGenLevel worldGenLevel = featurePlaceContext.level();
        RandomSource randomSource = featurePlaceContext.random();
        BlockPos blockPos = featurePlaceContext.origin();
        List<EndSpike> list = spikeConfiguration.getSpikes();
        if (list.isEmpty()) {
            list = SpikeFeature.getSpikesForLevel(worldGenLevel);
        }
        for (EndSpike endSpike : list) {
            if (!endSpike.isCenterWithinChunk(blockPos)) continue;
            this.placeSpike(worldGenLevel, randomSource, spikeConfiguration, endSpike);
        }
        return true;
    }

    private void placeSpike(ServerLevelAccessor serverLevelAccessor, RandomSource randomSource, SpikeConfiguration spikeConfiguration, EndSpike endSpike) {
        int i = endSpike.getRadius();
        for (BlockPos blockPos : BlockPos.betweenClosed(new BlockPos(endSpike.getCenterX() - i, serverLevelAccessor.getMinBuildHeight(), endSpike.getCenterZ() - i), new BlockPos(endSpike.getCenterX() + i, endSpike.getHeight() + 10, endSpike.getCenterZ() + i))) {
            if (blockPos.distToLowCornerSqr(endSpike.getCenterX(), blockPos.getY(), endSpike.getCenterZ()) <= (double)(i * i + 1) && blockPos.getY() < endSpike.getHeight()) {
                this.setBlock(serverLevelAccessor, blockPos, Blocks.OBSIDIAN.defaultBlockState());
                continue;
            }
            if (blockPos.getY() <= 65) continue;
            this.setBlock(serverLevelAccessor, blockPos, Blocks.AIR.defaultBlockState());
        }
        if (endSpike.isGuarded()) {
            int j = -2;
            int k = 2;
            int l = 3;
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (int m = -2; m <= 2; ++m) {
                for (int n = -2; n <= 2; ++n) {
                    for (int o = 0; o <= 3; ++o) {
                        boolean bl3;
                        boolean bl = Mth.abs(m) == 2;
                        boolean bl2 = Mth.abs(n) == 2;
                        boolean bl4 = bl3 = o == 3;
                        if (!bl && !bl2 && !bl3) continue;
                        boolean bl42 = m == -2 || m == 2 || bl3;
                        boolean bl5 = n == -2 || n == 2 || bl3;
                        BlockState blockState = (BlockState)((BlockState)((BlockState)((BlockState)Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, bl42 && n != -2)).setValue(IronBarsBlock.SOUTH, bl42 && n != 2)).setValue(IronBarsBlock.WEST, bl5 && m != -2)).setValue(IronBarsBlock.EAST, bl5 && m != 2);
                        this.setBlock(serverLevelAccessor, mutableBlockPos.set(endSpike.getCenterX() + m, endSpike.getHeight() + o, endSpike.getCenterZ() + n), blockState);
                    }
                }
            }
        }
        EndCrystal endCrystal = EntityType.END_CRYSTAL.create(serverLevelAccessor.getLevel());
        endCrystal.setBeamTarget(spikeConfiguration.getCrystalBeamTarget());
        endCrystal.setInvulnerable(spikeConfiguration.isCrystalInvulnerable());
        endCrystal.moveTo((double)endSpike.getCenterX() + 0.5, endSpike.getHeight() + 1, (double)endSpike.getCenterZ() + 0.5, randomSource.nextFloat() * 360.0f, 0.0f);
        serverLevelAccessor.addFreshEntity(endCrystal);
        this.setBlock(serverLevelAccessor, new BlockPos(endSpike.getCenterX(), endSpike.getHeight(), endSpike.getCenterZ()), Blocks.BEDROCK.defaultBlockState());
    }

    public static class EndSpike {
        public static final Codec<EndSpike> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("centerX")).orElse(0).forGetter(endSpike -> endSpike.centerX), ((MapCodec)Codec.INT.fieldOf("centerZ")).orElse(0).forGetter(endSpike -> endSpike.centerZ), ((MapCodec)Codec.INT.fieldOf("radius")).orElse(0).forGetter(endSpike -> endSpike.radius), ((MapCodec)Codec.INT.fieldOf("height")).orElse(0).forGetter(endSpike -> endSpike.height), ((MapCodec)Codec.BOOL.fieldOf("guarded")).orElse(false).forGetter(endSpike -> endSpike.guarded)).apply((Applicative<EndSpike, ?>)instance, EndSpike::new));
        private final int centerX;
        private final int centerZ;
        private final int radius;
        private final int height;
        private final boolean guarded;
        private final AABB topBoundingBox;

        public EndSpike(int i, int j, int k, int l, boolean bl) {
            this.centerX = i;
            this.centerZ = j;
            this.radius = k;
            this.height = l;
            this.guarded = bl;
            this.topBoundingBox = new AABB(i - k, DimensionType.MIN_Y, j - k, i + k, DimensionType.MAX_Y, j + k);
        }

        public boolean isCenterWithinChunk(BlockPos blockPos) {
            return SectionPos.blockToSectionCoord(blockPos.getX()) == SectionPos.blockToSectionCoord(this.centerX) && SectionPos.blockToSectionCoord(blockPos.getZ()) == SectionPos.blockToSectionCoord(this.centerZ);
        }

        public int getCenterX() {
            return this.centerX;
        }

        public int getCenterZ() {
            return this.centerZ;
        }

        public int getRadius() {
            return this.radius;
        }

        public int getHeight() {
            return this.height;
        }

        public boolean isGuarded() {
            return this.guarded;
        }

        public AABB getTopBoundingBox() {
            return this.topBoundingBox;
        }
    }

    static class SpikeCacheLoader
    extends CacheLoader<Long, List<EndSpike>> {
        SpikeCacheLoader() {
        }

        @Override
        public List<EndSpike> load(Long long_) {
            List list = Util.shuffledCopy(IntStream.range(0, 10).boxed().collect(Collectors.toList()), RandomSource.create(long_));
            ArrayList<EndSpike> list2 = Lists.newArrayList();
            for (int i = 0; i < 10; ++i) {
                int j = Mth.floor(42.0 * Math.cos(2.0 * (-Math.PI + 0.3141592653589793 * (double)i)));
                int k = Mth.floor(42.0 * Math.sin(2.0 * (-Math.PI + 0.3141592653589793 * (double)i)));
                int l = (Integer)list.get(i);
                int m = 2 + l / 3;
                int n = 76 + l * 3;
                boolean bl = l == 1 || l == 2;
                list2.add(new EndSpike(j, k, m, n, bl));
            }
            return list2;
        }

        @Override
        public /* synthetic */ Object load(Object object) throws Exception {
            return this.load((Long)object);
        }
    }
}


/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.SimpleRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jetbrains.annotations.Nullable;

public class OreVeinifier {
    private static final float RARITY = 1.0f;
    private static final float RIDGE_NOISE_FREQUENCY = 4.0f;
    private static final float THICKNESS = 0.08f;
    private static final float VEININESS_THRESHOLD = 0.4f;
    private static final float VEIN_SOLIDNESS = 0.7f;
    private static final float MIN_RICHNESS = 0.1f;
    private static final float MAX_RICHNESS = 0.3f;
    private static final float MAX_RICHNESS_THRESHOLD = 0.6f;
    private static final float CHANCE_OF_RAW_ORE_BLOCK = 0.01f;
    private static final float SKIP_ORE_IF_GAP_NOISE_IS_BELOW = -0.3f;
    private final int veinMaxY;
    private final int veinMinY;
    private final BlockState normalBlock;
    private final NormalNoise veininessNoiseSource;
    private final NormalNoise veinANoiseSource;
    private final NormalNoise veinBNoiseSource;
    private final NormalNoise gapNoise;
    private final int cellWidth;
    private final int cellHeight;

    public OreVeinifier(long l, BlockState blockState, int i, int j, int k) {
        Random random = new Random(l);
        this.normalBlock = blockState;
        this.veininessNoiseSource = NormalNoise.create((RandomSource)new SimpleRandomSource(random.nextLong()), -8, 1.0);
        this.veinANoiseSource = NormalNoise.create((RandomSource)new SimpleRandomSource(random.nextLong()), -7, 1.0);
        this.veinBNoiseSource = NormalNoise.create((RandomSource)new SimpleRandomSource(random.nextLong()), -7, 1.0);
        this.gapNoise = NormalNoise.create((RandomSource)new SimpleRandomSource(0L), -5, 1.0);
        this.cellWidth = i;
        this.cellHeight = j;
        this.veinMaxY = Stream.of(VeinType.values()).mapToInt(veinType -> ((VeinType)veinType).maxY).max().orElse(k);
        this.veinMinY = Stream.of(VeinType.values()).mapToInt(veinType -> ((VeinType)veinType).minY).min().orElse(k);
    }

    public void fillVeininessNoiseColumn(double[] ds, int i, int j, int k, int l) {
        this.fillNoiseColumn(ds, i, j, this.veininessNoiseSource, 1.0, k, l);
    }

    public void fillNoiseColumnA(double[] ds, int i, int j, int k, int l) {
        this.fillNoiseColumn(ds, i, j, this.veinANoiseSource, 4.0, k, l);
    }

    public void fillNoiseColumnB(double[] ds, int i, int j, int k, int l) {
        this.fillNoiseColumn(ds, i, j, this.veinBNoiseSource, 4.0, k, l);
    }

    public void fillNoiseColumn(double[] ds, int i, int j, NormalNoise normalNoise, double d, int k, int l) {
        for (int m = 0; m < l; ++m) {
            int n = m + k;
            int o = i * this.cellWidth;
            int p = n * this.cellHeight;
            int q = j * this.cellWidth;
            double e = p >= this.veinMinY && p <= this.veinMaxY ? normalNoise.getValue((double)o * d, (double)p * d, (double)q * d) : 0.0;
            ds[m] = e;
        }
    }

    public BlockState oreVeinify(RandomSource randomSource, int i, int j, int k, double d, double e, double f) {
        BlockState blockState = this.normalBlock;
        VeinType veinType = this.getVeinType(d);
        if (veinType == null || j < veinType.minY || j > veinType.maxY) {
            return blockState;
        }
        if (randomSource.nextFloat() > 0.7f) {
            return blockState;
        }
        if (this.isVein(e, f)) {
            double g = Mth.clampedMap(Math.abs(d), 0.4f, 0.6f, 0.1f, 0.3f);
            if ((double)randomSource.nextFloat() < g && this.gapNoise.getValue(i, j, k) > (double)-0.3f) {
                return randomSource.nextFloat() < 0.01f ? veinType.rawOreBlock : veinType.ore;
            }
            return veinType.filler;
        }
        return blockState;
    }

    private boolean isVein(double d, double e) {
        double g;
        double f = Math.abs(1.0 * d) - (double)0.08f;
        return Math.max(f, g = Math.abs(1.0 * e) - (double)0.08f) < 0.0;
    }

    @Nullable
    private VeinType getVeinType(double d) {
        if (Math.abs(d) < (double)0.4f) {
            return null;
        }
        return d > 0.0 ? VeinType.COPPER : VeinType.IRON;
    }

    static enum VeinType {
        COPPER(Blocks.COPPER_ORE.defaultBlockState(), Blocks.RAW_COPPER_BLOCK.defaultBlockState(), Blocks.GRANITE.defaultBlockState(), 0, 50),
        IRON(Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(), Blocks.RAW_IRON_BLOCK.defaultBlockState(), Blocks.TUFF.defaultBlockState(), -60, -8);

        private final BlockState ore;
        private final BlockState rawOreBlock;
        private final BlockState filler;
        private final int minY;
        private final int maxY;

        private VeinType(BlockState blockState, BlockState blockState2, BlockState blockState3, int j, int k) {
            this.ore = blockState;
            this.rawOreBlock = blockState2;
            this.filler = blockState3;
            this.minY = j;
            this.maxY = k;
        }
    }
}


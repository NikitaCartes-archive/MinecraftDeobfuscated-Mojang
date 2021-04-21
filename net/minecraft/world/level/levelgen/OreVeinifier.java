/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.SimpleRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jetbrains.annotations.Nullable;

public class OreVeinifier {
    private static final double RARITY = 1.0;
    private static final double RIDGE_NOISE_FREQUENCY = 4.0;
    private static final double THICKNESS = 0.08;
    private static final double VEININESS_THRESHOLD = 0.3;
    private static final float VEIN_SOLIDNESS = 0.7f;
    private static final double ORE_PROPORTION_VS_BASESTONE = 0.3;
    private static final double VEININESS_FREQUENCY = 0.00390625;
    private final int veinMaxY;
    private final int veinMinY;
    private final BlockState normalBlock;
    private final NormalNoise veininessNoiseSource;
    private final NormalNoise veinANoiseSource;
    private final NormalNoise veinBNoiseSource;
    private final int cellWidth;
    private final int cellHeight;

    public OreVeinifier(long l, BlockState blockState, int i, int j, int k) {
        Random random = new Random(l);
        this.normalBlock = blockState;
        this.veininessNoiseSource = NormalNoise.create((RandomSource)new SimpleRandomSource(random.nextLong()), 0, 1.0);
        this.veinANoiseSource = NormalNoise.create((RandomSource)new SimpleRandomSource(random.nextLong()), -7, 1.0);
        this.veinBNoiseSource = NormalNoise.create((RandomSource)new SimpleRandomSource(random.nextLong()), -7, 1.0);
        this.cellWidth = i;
        this.cellHeight = j;
        this.veinMaxY = Stream.of(VeinType.values()).mapToInt(veinType -> ((VeinType)veinType).maxY).max().orElse(k);
        this.veinMinY = Stream.of(VeinType.values()).mapToInt(veinType -> ((VeinType)veinType).minY).min().orElse(k);
    }

    public void fillVeininessNoiseColumn(double[] ds, int i, int j, int k, int l) {
        this.fillNoiseColumn(ds, i, j, this.veininessNoiseSource, 0.00390625, k, l);
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

    public BlockState oreVeinify(RandomSource randomSource, int i, double d, double e, double f) {
        BlockState blockState = this.normalBlock;
        VeinType veinType = this.getVeinType(d);
        if (veinType == null || i < veinType.minY || i > veinType.maxY) {
            return blockState;
        }
        if (randomSource.nextFloat() > 0.7f) {
            return blockState;
        }
        if (this.isVein(e, f)) {
            return (double)randomSource.nextFloat() < 0.3 ? veinType.ore : veinType.filler;
        }
        return blockState;
    }

    private boolean isVein(double d, double e) {
        double g;
        double f = Math.abs(1.0 * d) - 0.08;
        return Math.max(f, g = Math.abs(1.0 * e) - 0.08) < 0.0;
    }

    @Nullable
    private VeinType getVeinType(double d) {
        if (Math.abs(d) < 0.3) {
            return null;
        }
        return d > 0.0 ? VeinType.COPPER : VeinType.IRON;
    }

    static enum VeinType {
        COPPER(Blocks.COPPER_ORE.defaultBlockState(), Blocks.GRANITE.defaultBlockState(), 0, 50),
        IRON(Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(), Blocks.TUFF.defaultBlockState(), -60, -8);

        private final BlockState ore;
        private final BlockState filler;
        private final int minY;
        private final int maxY;

        private VeinType(BlockState blockState, BlockState blockState2, int j, int k) {
            this.ore = blockState;
            this.filler = blockState2;
            this.minY = j;
            this.maxY = k;
        }
    }
}


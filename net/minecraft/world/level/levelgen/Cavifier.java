/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.SimpleRandomSource;
import net.minecraft.world.level.levelgen.synth.NoiseUtils;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class Cavifier {
    private final int minCellY;
    private final NormalNoise layerNoiseSource;
    private final NormalNoise pillarNoiseSource;
    private final NormalNoise pillarRarenessModulator;
    private final NormalNoise pillarThicknessModulator;
    private final NormalNoise spaghetti2dNoiseSource;
    private final NormalNoise spaghetti2dElevationModulator;
    private final NormalNoise spaghetti2dRarityModulator;
    private final NormalNoise spaghetti2dThicknessModulator;
    private final NormalNoise spaghetti3dNoiseSource1;
    private final NormalNoise spaghetti3dNoiseSource2;
    private final NormalNoise spaghetti3dRarityModulator;
    private final NormalNoise spaghetti3dThicknessModulator;
    private final NormalNoise spaghettiRoughnessNoise;
    private final NormalNoise spaghettiRoughnessModulator;
    private final NormalNoise caveEntranceNoiseSource;

    public Cavifier(RandomSource randomSource, int i) {
        this.minCellY = i;
        this.pillarNoiseSource = NormalNoise.create((RandomSource)new SimpleRandomSource(randomSource.nextLong()), -7, 1.0, 1.0);
        this.pillarRarenessModulator = NormalNoise.create((RandomSource)new SimpleRandomSource(randomSource.nextLong()), -8, 1.0);
        this.pillarThicknessModulator = NormalNoise.create((RandomSource)new SimpleRandomSource(randomSource.nextLong()), -8, 1.0);
        this.spaghetti2dNoiseSource = NormalNoise.create((RandomSource)new SimpleRandomSource(randomSource.nextLong()), -7, 1.0);
        this.spaghetti2dElevationModulator = NormalNoise.create((RandomSource)new SimpleRandomSource(randomSource.nextLong()), -8, 1.0);
        this.spaghetti2dRarityModulator = NormalNoise.create((RandomSource)new SimpleRandomSource(randomSource.nextLong()), -11, 1.0);
        this.spaghetti2dThicknessModulator = NormalNoise.create((RandomSource)new SimpleRandomSource(randomSource.nextLong()), -11, 1.0);
        this.spaghetti3dNoiseSource1 = NormalNoise.create((RandomSource)new SimpleRandomSource(randomSource.nextLong()), -7, 1.0);
        this.spaghetti3dNoiseSource2 = NormalNoise.create((RandomSource)new SimpleRandomSource(randomSource.nextLong()), -7, 1.0);
        this.spaghetti3dRarityModulator = NormalNoise.create((RandomSource)new SimpleRandomSource(randomSource.nextLong()), -11, 1.0);
        this.spaghetti3dThicknessModulator = NormalNoise.create((RandomSource)new SimpleRandomSource(randomSource.nextLong()), -8, 1.0);
        this.spaghettiRoughnessNoise = NormalNoise.create((RandomSource)new SimpleRandomSource(randomSource.nextLong()), -5, 1.0);
        this.spaghettiRoughnessModulator = NormalNoise.create((RandomSource)new SimpleRandomSource(randomSource.nextLong()), -8, 1.0);
        this.caveEntranceNoiseSource = NormalNoise.create((RandomSource)new SimpleRandomSource(randomSource.nextLong()), -8, 1.0, 1.0, 1.0);
        this.layerNoiseSource = NormalNoise.create((RandomSource)new SimpleRandomSource(randomSource.nextLong()), -8, 1.0);
    }

    public double cavify(int i, int j, int k, double d, double e) {
        boolean bl = e >= 375.0;
        double f = this.spaghettiRoughness(i, j, k);
        double g = this.getSpaghetti3d(i, j, k);
        if (bl) {
            double h = d / 128.0;
            double l = Mth.clamp(h + 0.35, -1.0, 1.0);
            double m = this.getLayerizedCaverns(i, j, k);
            double n = this.getSpaghetti2d(i, j, k);
            double o = l + m;
            double p = Math.min(o, Math.min(g, n) + f);
            double q = Math.max(p, this.getPillars(i, j, k));
            return 128.0 * Mth.clamp(q, -1.0, 1.0);
        }
        return Math.min(e, (g + f) * 128.0);
    }

    private double getPillars(int i, int j, int k) {
        double d = 0.0;
        double e = 2.0;
        double f = NoiseUtils.sampleNoiseAndMapToRange(this.pillarRarenessModulator, i, j, k, 0.0, 2.0);
        boolean l = false;
        boolean m = true;
        double g = NoiseUtils.sampleNoiseAndMapToRange(this.pillarThicknessModulator, i, j, k, 0.0, 1.0);
        g = Math.pow(g, 3.0);
        double h = 25.0;
        double n = 0.3;
        double o = this.pillarNoiseSource.getValue((double)i * 25.0, (double)j * 0.3, (double)k * 25.0);
        if ((o = g * (o * 2.0 - f)) > 0.02) {
            return o;
        }
        return Double.NEGATIVE_INFINITY;
    }

    private double getLayerizedCaverns(int i, int j, int k) {
        double d = this.layerNoiseSource.getValue(i, j * 8, k);
        return Mth.square(d) * 4.0;
    }

    private double getSpaghetti3d(int i, int j, int k) {
        double d = this.spaghetti3dRarityModulator.getValue(i * 2, j, k * 2);
        double e = QuantizedSpaghettiRarity.getSpaghettiRarity3D(d);
        double f = 0.065;
        double g = 0.085;
        double h = NoiseUtils.sampleNoiseAndMapToRange(this.spaghetti3dThicknessModulator, i, j, k, 0.065, 0.085);
        double l = Cavifier.sampleWithRarity(this.spaghetti3dNoiseSource1, i, j, k, e);
        double m = Math.abs(e * l) - h;
        double n = Cavifier.sampleWithRarity(this.spaghetti3dNoiseSource2, i, j, k, e);
        double o = Math.abs(e * n) - h;
        return Cavifier.clampToUnit(Math.max(m, o));
    }

    private double getSpaghetti2d(int i, int j, int k) {
        double d = this.spaghetti2dRarityModulator.getValue(i * 2, j, k * 2);
        double e = QuantizedSpaghettiRarity.getSphaghettiRarity2D(d);
        double f = 0.6;
        double g = 1.3;
        double h = NoiseUtils.sampleNoiseAndMapToRange(this.spaghetti2dThicknessModulator, i * 2, j, k * 2, 0.6, 1.3);
        double l = Cavifier.sampleWithRarity(this.spaghetti2dNoiseSource, i, j, k, e);
        double m = 0.083;
        double n = Math.abs(e * l) - 0.083 * h;
        int o = this.minCellY;
        int p = 8;
        double q = NoiseUtils.sampleNoiseAndMapToRange(this.spaghetti2dElevationModulator, i, 0.0, k, o, 8.0);
        double r = Math.abs(q - (double)j / 8.0) - 1.0 * h;
        r = r * r * r;
        return Cavifier.clampToUnit(Math.max(r, n));
    }

    private double spaghettiRoughness(int i, int j, int k) {
        double d = NoiseUtils.sampleNoiseAndMapToRange(this.spaghettiRoughnessModulator, i, j, k, 0.0, 0.1);
        return (0.4 - Math.abs(this.spaghettiRoughnessNoise.getValue(i, j, k))) * d;
    }

    private static double clampToUnit(double d) {
        return Mth.clamp(d, -1.0, 1.0);
    }

    private static double sampleWithRarity(NormalNoise normalNoise, double d, double e, double f, double g) {
        return normalNoise.getValue(d / g, e / g, f / g);
    }

    static final class QuantizedSpaghettiRarity {
        private static double getSphaghettiRarity2D(double d) {
            if (d < -0.75) {
                return 0.5;
            }
            if (d < -0.5) {
                return 0.75;
            }
            if (d < 0.5) {
                return 1.0;
            }
            if (d < 0.75) {
                return 2.0;
            }
            return 3.0;
        }

        private static double getSpaghettiRarity3D(double d) {
            if (d < -0.5) {
                return 0.75;
            }
            if (d < 0.0) {
                return 1.0;
            }
            if (d < 0.5) {
                return 2.0;
            }
            return 3.0;
        }
    }
}


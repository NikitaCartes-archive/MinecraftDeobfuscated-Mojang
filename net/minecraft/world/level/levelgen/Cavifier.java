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
    private final NormalNoise cheeseNoiseSource;

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
        this.cheeseNoiseSource = NormalNoise.create((RandomSource)new SimpleRandomSource(randomSource.nextLong()), -6, 1.0, 1.0, 0.0, 1.0, 0.0, 1.0, 1.0);
    }

    public double cavify(int i, int j, int k, double d) {
        boolean bl = d < 170.0;
        double e = this.spaghettiRoughness(i, j, k);
        double f = this.getSpaghetti3d(i, j, k);
        if (bl) {
            return Math.min(d, (f + e) * 128.0 * 5.0);
        }
        double g = this.cheeseNoiseSource.getValue(i, j, k);
        double h = Mth.clamp(g + 0.25, -1.0, 1.0);
        double l = (d - 170.0) / 100.0;
        double m = h + Mth.clampedLerp(0.5, 0.0, l);
        double n = this.getLayerizedCaverns(i, j, k);
        double o = this.getSpaghetti2d(i, j, k);
        double p = m + n;
        double q = Math.min(p, Math.min(f, o) + e);
        double r = Math.max(q, this.getPillars(i, j, k));
        return 128.0 * Mth.clamp(r, -1.0, 1.0);
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
        double g = 0.088;
        double h = NoiseUtils.sampleNoiseAndMapToRange(this.spaghetti3dThicknessModulator, i, j, k, 0.065, 0.088);
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
                return 1.5;
            }
            return 2.0;
        }
    }
}


/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.util.Mth;
import net.minecraft.util.ThreadingDetector;
import net.minecraft.world.level.levelgen.BitRandomSource;
import net.minecraft.world.level.levelgen.MarsagliaPolarGaussian;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomSource;

public class LegacyRandomSource
implements BitRandomSource {
    private static final int MODULUS_BITS = 48;
    private static final long MODULUS_MASK = 0xFFFFFFFFFFFFL;
    private static final long MULTIPLIER = 25214903917L;
    private static final long INCREMENT = 11L;
    private final AtomicLong seed = new AtomicLong();
    private final MarsagliaPolarGaussian gaussianSource = new MarsagliaPolarGaussian(this);

    public LegacyRandomSource(long l) {
        this.setSeed(l);
    }

    @Override
    public RandomSource fork() {
        return new LegacyRandomSource(this.nextLong());
    }

    @Override
    public PositionalRandomFactory forkPositional() {
        return new LegacyPositionalRandomFactory(this.nextLong());
    }

    @Override
    public void setSeed(long l) {
        if (!this.seed.compareAndSet(this.seed.get(), (l ^ 0x5DEECE66DL) & 0xFFFFFFFFFFFFL)) {
            throw ThreadingDetector.makeThreadingException("LegacyRandomSource", null);
        }
        this.gaussianSource.reset();
    }

    @Override
    public int next(int i) {
        long m;
        long l = this.seed.get();
        if (!this.seed.compareAndSet(l, m = l * 25214903917L + 11L & 0xFFFFFFFFFFFFL)) {
            throw ThreadingDetector.makeThreadingException("LegacyRandomSource", null);
        }
        return (int)(m >> 48 - i);
    }

    @Override
    public double nextGaussian() {
        return this.gaussianSource.nextGaussian();
    }

    public static class LegacyPositionalRandomFactory
    implements PositionalRandomFactory {
        private final long seed;

        public LegacyPositionalRandomFactory(long l) {
            this.seed = l;
        }

        @Override
        public RandomSource at(int i, int j, int k) {
            long l = Mth.getSeed(i, j, k);
            long m = l ^ this.seed;
            return new LegacyRandomSource(m);
        }

        @Override
        public RandomSource fromHashOf(String string) {
            int i = string.hashCode();
            return new LegacyRandomSource((long)i ^ this.seed);
        }

        @Override
        @VisibleForTesting
        public void parityConfigString(StringBuilder stringBuilder) {
            stringBuilder.append("LegacyPositionalRandomFactory{").append(this.seed).append("}");
        }
    }
}


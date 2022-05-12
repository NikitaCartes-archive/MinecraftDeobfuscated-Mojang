/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.atomic.AtomicLong;

public final class RandomSupport {
    public static final long GOLDEN_RATIO_64 = -7046029254386353131L;
    public static final long SILVER_RATIO_64 = 7640891576956012809L;
    private static final AtomicLong SEED_UNIQUIFIER = new AtomicLong(8682522807148012L);

    @VisibleForTesting
    public static long mixStafford13(long l) {
        l = (l ^ l >>> 30) * -4658895280553007687L;
        l = (l ^ l >>> 27) * -7723592293110705685L;
        return l ^ l >>> 31;
    }

    public static Seed128bit upgradeSeedTo128bit(long l) {
        long m = l ^ 0x6A09E667F3BCC909L;
        long n = m + -7046029254386353131L;
        return new Seed128bit(RandomSupport.mixStafford13(m), RandomSupport.mixStafford13(n));
    }

    public static long generateUniqueSeed() {
        return SEED_UNIQUIFIER.updateAndGet(l -> l * 1181783497276652981L) ^ System.nanoTime();
    }

    public record Seed128bit(long seedLo, long seedHi) {
    }
}


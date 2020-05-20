/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import java.util.function.IntConsumer;
import net.minecraft.Util;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

public class BitStorage {
    private static final int[] MAGIC = new int[]{-1, -1, 0, Integer.MIN_VALUE, 0, 0, 0x55555555, 0x55555555, 0, Integer.MIN_VALUE, 0, 1, 0x33333333, 0x33333333, 0, 0x2AAAAAAA, 0x2AAAAAAA, 0, 0x24924924, 0x24924924, 0, Integer.MIN_VALUE, 0, 2, 0x1C71C71C, 0x1C71C71C, 0, 0x19999999, 0x19999999, 0, 390451572, 390451572, 0, 0x15555555, 0x15555555, 0, 0x13B13B13, 0x13B13B13, 0, 306783378, 306783378, 0, 0x11111111, 0x11111111, 0, Integer.MIN_VALUE, 0, 3, 0xF0F0F0F, 0xF0F0F0F, 0, 0xE38E38E, 0xE38E38E, 0, 226050910, 226050910, 0, 0xCCCCCCC, 0xCCCCCCC, 0, 0xC30C30C, 0xC30C30C, 0, 195225786, 195225786, 0, 186737708, 186737708, 0, 0xAAAAAAA, 0xAAAAAAA, 0, 171798691, 171798691, 0, 0x9D89D89, 0x9D89D89, 0, 159072862, 159072862, 0, 0x9249249, 0x9249249, 0, 148102320, 148102320, 0, 0x8888888, 0x8888888, 0, 138547332, 138547332, 0, Integer.MIN_VALUE, 0, 4, 130150524, 130150524, 0, 0x7878787, 0x7878787, 0, 0x7507507, 0x7507507, 0, 0x71C71C7, 0x71C71C7, 0, 116080197, 116080197, 0, 113025455, 113025455, 0, 0x6906906, 0x6906906, 0, 0x6666666, 0x6666666, 0, 104755299, 104755299, 0, 0x6186186, 0x6186186, 0, 99882960, 99882960, 0, 97612893, 97612893, 0, 0x5B05B05, 0x5B05B05, 0, 93368854, 93368854, 0, 91382282, 91382282, 0, 0x5555555, 0x5555555, 0, 87652393, 87652393, 0, 85899345, 85899345, 0, 0x5050505, 0x5050505, 0, 0x4EC4EC4, 0x4EC4EC4, 0, 81037118, 81037118, 0, 79536431, 79536431, 0, 78090314, 78090314, 0, 0x4924924, 0x4924924, 0, 75350303, 75350303, 0, 74051160, 74051160, 0, 72796055, 72796055, 0, 0x4444444, 0x4444444, 0, 70409299, 70409299, 0, 69273666, 69273666, 0, 0x4104104, 0x4104104, 0, Integer.MIN_VALUE, 0, 5};
    private final long[] data;
    private final int bits;
    private final long mask;
    private final int size;
    private final int valuesPerLong;
    private final int divideMul;
    private final int divideAdd;
    private final int divideShift;

    public BitStorage(int i, int j) {
        this(i, j, null);
    }

    public BitStorage(int i, int j, @Nullable long[] ls) {
        Validate.inclusiveBetween(1L, 32L, i);
        this.size = j;
        this.bits = i;
        this.mask = (1L << i) - 1L;
        this.valuesPerLong = (char)(64 / i);
        int k = 3 * (this.valuesPerLong - 1);
        this.divideMul = MAGIC[k + 0];
        this.divideAdd = MAGIC[k + 1];
        this.divideShift = MAGIC[k + 2];
        int l = (j + this.valuesPerLong - 1) / this.valuesPerLong;
        if (ls != null) {
            if (ls.length != l) {
                throw Util.pauseInIde(new RuntimeException("Invalid length given for storage, got: " + ls.length + " but expected: " + l));
            }
            this.data = ls;
        } else {
            this.data = new long[l];
        }
    }

    private int cellIndex(int i) {
        long l = Integer.toUnsignedLong(this.divideMul);
        long m = Integer.toUnsignedLong(this.divideAdd);
        return (int)((long)i * l + m >> 32 >> this.divideShift);
    }

    public int getAndSet(int i, int j) {
        Validate.inclusiveBetween(0L, this.size - 1, i);
        Validate.inclusiveBetween(0L, this.mask, j);
        int k = this.cellIndex(i);
        long l = this.data[k];
        int m = (i - k * this.valuesPerLong) * this.bits;
        int n = (int)(l >> m & this.mask);
        this.data[k] = l & (this.mask << m ^ 0xFFFFFFFFFFFFFFFFL) | ((long)j & this.mask) << m;
        return n;
    }

    public void set(int i, int j) {
        Validate.inclusiveBetween(0L, this.size - 1, i);
        Validate.inclusiveBetween(0L, this.mask, j);
        int k = this.cellIndex(i);
        long l = this.data[k];
        int m = (i - k * this.valuesPerLong) * this.bits;
        this.data[k] = l & (this.mask << m ^ 0xFFFFFFFFFFFFFFFFL) | ((long)j & this.mask) << m;
    }

    public int get(int i) {
        Validate.inclusiveBetween(0L, this.size - 1, i);
        int j = this.cellIndex(i);
        long l = this.data[j];
        int k = (i - j * this.valuesPerLong) * this.bits;
        return (int)(l >> k & this.mask);
    }

    public long[] getRaw() {
        return this.data;
    }

    public int getSize() {
        return this.size;
    }

    public void getAll(IntConsumer intConsumer) {
        int i = 0;
        for (long l : this.data) {
            for (int j = 0; j < this.valuesPerLong; ++j) {
                intConsumer.accept((int)(l & this.mask));
                l >>= this.bits;
                if (++i < this.size) continue;
                return;
            }
        }
    }
}


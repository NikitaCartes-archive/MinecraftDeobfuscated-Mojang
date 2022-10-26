/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.math;

import java.util.Arrays;
import net.minecraft.Util;
import org.joml.Matrix3f;

public enum SymmetricGroup3 {
    P123(0, 1, 2),
    P213(1, 0, 2),
    P132(0, 2, 1),
    P231(1, 2, 0),
    P312(2, 0, 1),
    P321(2, 1, 0);

    private final int[] permutation;
    private final Matrix3f transformation;
    private static final int ORDER = 3;
    private static final SymmetricGroup3[][] cayleyTable;

    private SymmetricGroup3(int j, int k, int l) {
        this.permutation = new int[]{j, k, l};
        this.transformation = new Matrix3f();
        this.transformation.set(this.permutation(0), 0, 1.0f);
        this.transformation.set(this.permutation(1), 1, 1.0f);
        this.transformation.set(this.permutation(2), 2, 1.0f);
    }

    public SymmetricGroup3 compose(SymmetricGroup3 symmetricGroup3) {
        return cayleyTable[this.ordinal()][symmetricGroup3.ordinal()];
    }

    public int permutation(int i) {
        return this.permutation[i];
    }

    public Matrix3f transformation() {
        return this.transformation;
    }

    static {
        cayleyTable = Util.make(new SymmetricGroup3[SymmetricGroup3.values().length][SymmetricGroup3.values().length], symmetricGroup3s -> {
            for (SymmetricGroup3 symmetricGroup32 : SymmetricGroup3.values()) {
                for (SymmetricGroup3 symmetricGroup322 : SymmetricGroup3.values()) {
                    SymmetricGroup3 symmetricGroup33;
                    int[] is = new int[3];
                    for (int i = 0; i < 3; ++i) {
                        is[i] = symmetricGroup32.permutation[symmetricGroup322.permutation[i]];
                    }
                    symmetricGroup3s[symmetricGroup32.ordinal()][symmetricGroup322.ordinal()] = symmetricGroup33 = Arrays.stream(SymmetricGroup3.values()).filter(symmetricGroup3 -> Arrays.equals(symmetricGroup3.permutation, is)).findFirst().get();
                }
            }
        });
    }
}


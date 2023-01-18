/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity;

import java.util.EnumSet;
import java.util.Set;

public enum RelativeMovement {
    X(0),
    Y(1),
    Z(2),
    Y_ROT(3),
    X_ROT(4);

    public static final Set<RelativeMovement> ALL;
    public static final Set<RelativeMovement> ROTATION;
    private final int bit;

    private RelativeMovement(int j) {
        this.bit = j;
    }

    private int getMask() {
        return 1 << this.bit;
    }

    private boolean isSet(int i) {
        return (i & this.getMask()) == this.getMask();
    }

    public static Set<RelativeMovement> unpack(int i) {
        EnumSet<RelativeMovement> set = EnumSet.noneOf(RelativeMovement.class);
        for (RelativeMovement relativeMovement : RelativeMovement.values()) {
            if (!relativeMovement.isSet(i)) continue;
            set.add(relativeMovement);
        }
        return set;
    }

    public static int pack(Set<RelativeMovement> set) {
        int i = 0;
        for (RelativeMovement relativeMovement : set) {
            i |= relativeMovement.getMask();
        }
        return i;
    }

    static {
        ALL = Set.of(RelativeMovement.values());
        ROTATION = Set.of(X_ROT, Y_ROT);
    }
}


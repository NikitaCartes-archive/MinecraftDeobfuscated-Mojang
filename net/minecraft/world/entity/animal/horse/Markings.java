/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.animal.horse;

import java.util.function.IntFunction;
import net.minecraft.util.ByIdMap;

public enum Markings {
    NONE(0),
    WHITE(1),
    WHITE_FIELD(2),
    WHITE_DOTS(3),
    BLACK_DOTS(4);

    private static final IntFunction<Markings> BY_ID;
    private final int id;

    private Markings(int j) {
        this.id = j;
    }

    public int getId() {
        return this.id;
    }

    public static Markings byId(int i) {
        return BY_ID.apply(i);
    }

    static {
        BY_ID = ByIdMap.continuous(Markings::getId, Markings.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    }
}


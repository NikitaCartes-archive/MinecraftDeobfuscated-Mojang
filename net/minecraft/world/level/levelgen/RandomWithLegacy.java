/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomSource;

public record RandomWithLegacy(PositionalRandomFactory random, boolean useLegacyInit, long legacyLevelSeed) {
    public RandomSource newLegacyInstance(long l) {
        return new LegacyRandomSource(this.legacyLevelSeed + l);
    }
}


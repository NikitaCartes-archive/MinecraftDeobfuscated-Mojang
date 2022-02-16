/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import java.util.List;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

public record NoiseRouter(DensityFunction barrierNoise, DensityFunction fluidLevelFloodednessNoise, DensityFunction fluidLevelSpreadNoise, DensityFunction lavaNoise, PositionalRandomFactory aquiferPositionalRandomFactory, PositionalRandomFactory oreVeinsPositionalRandomFactory, DensityFunction temperature, DensityFunction humidity, DensityFunction continentalness, DensityFunction erosion, DensityFunction depth, DensityFunction weirdness, DensityFunction initialDensityNoJaggedness, DensityFunction fullNoise, DensityFunction veinToggle, DensityFunction veinRidged, DensityFunction veinGap, List<Climate.ParameterPoint> spawnTarget) {
}


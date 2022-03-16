/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.world.level.levelgen.DensityFunction;

public record NoiseRouter(DensityFunction barrierNoise, DensityFunction fluidLevelFloodednessNoise, DensityFunction fluidLevelSpreadNoise, DensityFunction lavaNoise, DensityFunction temperature, DensityFunction vegetation, DensityFunction continents, DensityFunction erosion, DensityFunction depth, DensityFunction ridges, DensityFunction initialDensityWithoutJaggedness, DensityFunction finalDensity, DensityFunction veinToggle, DensityFunction veinRidged, DensityFunction veinGap) {
    public static final Codec<NoiseRouter> CODEC = RecordCodecBuilder.create(instance -> instance.group(NoiseRouter.field("barrier", NoiseRouter::barrierNoise), NoiseRouter.field("fluid_level_floodedness", NoiseRouter::fluidLevelFloodednessNoise), NoiseRouter.field("fluid_level_spread", NoiseRouter::fluidLevelSpreadNoise), NoiseRouter.field("lava", NoiseRouter::lavaNoise), NoiseRouter.field("temperature", NoiseRouter::temperature), NoiseRouter.field("vegetation", NoiseRouter::vegetation), NoiseRouter.field("continents", NoiseRouter::continents), NoiseRouter.field("erosion", NoiseRouter::erosion), NoiseRouter.field("depth", NoiseRouter::depth), NoiseRouter.field("ridges", NoiseRouter::ridges), NoiseRouter.field("initial_density_without_jaggedness", NoiseRouter::initialDensityWithoutJaggedness), NoiseRouter.field("final_density", NoiseRouter::finalDensity), NoiseRouter.field("vein_toggle", NoiseRouter::veinToggle), NoiseRouter.field("vein_ridged", NoiseRouter::veinRidged), NoiseRouter.field("vein_gap", NoiseRouter::veinGap)).apply((Applicative<NoiseRouter, ?>)instance, NoiseRouter::new));

    private static RecordCodecBuilder<NoiseRouter, DensityFunction> field(String string, Function<NoiseRouter, DensityFunction> function) {
        return ((MapCodec)DensityFunction.HOLDER_HELPER_CODEC.fieldOf(string)).forGetter(function);
    }

    public NoiseRouter mapAll(DensityFunction.Visitor visitor) {
        return new NoiseRouter(this.barrierNoise.mapAll(visitor), this.fluidLevelFloodednessNoise.mapAll(visitor), this.fluidLevelSpreadNoise.mapAll(visitor), this.lavaNoise.mapAll(visitor), this.temperature.mapAll(visitor), this.vegetation.mapAll(visitor), this.continents.mapAll(visitor), this.erosion.mapAll(visitor), this.depth.mapAll(visitor), this.ridges.mapAll(visitor), this.initialDensityWithoutJaggedness.mapAll(visitor), this.finalDensity.mapAll(visitor), this.veinToggle.mapAll(visitor), this.veinRidged.mapAll(visitor), this.veinGap.mapAll(visitor));
    }
}


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

public record NoiseRouterWithOnlyNoises(DensityFunction barrierNoise, DensityFunction fluidLevelFloodednessNoise, DensityFunction fluidLevelSpreadNoise, DensityFunction lavaNoise, DensityFunction temperature, DensityFunction vegetation, DensityFunction continents, DensityFunction erosion, DensityFunction depth, DensityFunction ridges, DensityFunction initialDensityWithoutJaggedness, DensityFunction finalDensity, DensityFunction veinToggle, DensityFunction veinRidged, DensityFunction veinGap) {
    public static final Codec<NoiseRouterWithOnlyNoises> CODEC = RecordCodecBuilder.create(instance -> instance.group(NoiseRouterWithOnlyNoises.field("barrier", NoiseRouterWithOnlyNoises::barrierNoise), NoiseRouterWithOnlyNoises.field("fluid_level_floodedness", NoiseRouterWithOnlyNoises::fluidLevelFloodednessNoise), NoiseRouterWithOnlyNoises.field("fluid_level_spread", NoiseRouterWithOnlyNoises::fluidLevelSpreadNoise), NoiseRouterWithOnlyNoises.field("lava", NoiseRouterWithOnlyNoises::lavaNoise), NoiseRouterWithOnlyNoises.field("temperature", NoiseRouterWithOnlyNoises::temperature), NoiseRouterWithOnlyNoises.field("vegetation", NoiseRouterWithOnlyNoises::vegetation), NoiseRouterWithOnlyNoises.field("continents", NoiseRouterWithOnlyNoises::continents), NoiseRouterWithOnlyNoises.field("erosion", NoiseRouterWithOnlyNoises::erosion), NoiseRouterWithOnlyNoises.field("depth", NoiseRouterWithOnlyNoises::depth), NoiseRouterWithOnlyNoises.field("ridges", NoiseRouterWithOnlyNoises::ridges), NoiseRouterWithOnlyNoises.field("initial_density_without_jaggedness", NoiseRouterWithOnlyNoises::initialDensityWithoutJaggedness), NoiseRouterWithOnlyNoises.field("final_density", NoiseRouterWithOnlyNoises::finalDensity), NoiseRouterWithOnlyNoises.field("vein_toggle", NoiseRouterWithOnlyNoises::veinToggle), NoiseRouterWithOnlyNoises.field("vein_ridged", NoiseRouterWithOnlyNoises::veinRidged), NoiseRouterWithOnlyNoises.field("vein_gap", NoiseRouterWithOnlyNoises::veinGap)).apply((Applicative<NoiseRouterWithOnlyNoises, ?>)instance, NoiseRouterWithOnlyNoises::new));

    private static RecordCodecBuilder<NoiseRouterWithOnlyNoises, DensityFunction> field(String string, Function<NoiseRouterWithOnlyNoises, DensityFunction> function) {
        return ((MapCodec)DensityFunction.HOLDER_HELPER_CODEC.fieldOf(string)).forGetter(function);
    }

    public NoiseRouterWithOnlyNoises mapAll(DensityFunction.Visitor visitor) {
        return new NoiseRouterWithOnlyNoises(this.barrierNoise.mapAll(visitor), this.fluidLevelFloodednessNoise.mapAll(visitor), this.fluidLevelSpreadNoise.mapAll(visitor), this.lavaNoise.mapAll(visitor), this.temperature.mapAll(visitor), this.vegetation.mapAll(visitor), this.continents.mapAll(visitor), this.erosion.mapAll(visitor), this.depth.mapAll(visitor), this.ridges.mapAll(visitor), this.initialDensityWithoutJaggedness.mapAll(visitor), this.finalDensity.mapAll(visitor), this.veinToggle.mapAll(visitor), this.veinRidged.mapAll(visitor), this.veinGap.mapAll(visitor));
    }
}


package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class SpringConfiguration implements FeatureConfiguration {
	public final FluidState state;

	public SpringConfiguration(FluidState fluidState) {
		this.state = fluidState;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("state"), FluidState.serialize(dynamicOps, this.state).getValue()))
		);
	}

	public static <T> SpringConfiguration deserialize(Dynamic<T> dynamic) {
		FluidState fluidState = (FluidState)dynamic.get("state").map(FluidState::deserialize).orElse(Fluids.EMPTY.defaultFluidState());
		return new SpringConfiguration(fluidState);
	}
}

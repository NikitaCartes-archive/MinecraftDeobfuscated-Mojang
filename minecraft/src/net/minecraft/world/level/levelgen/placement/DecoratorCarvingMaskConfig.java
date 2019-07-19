package net.minecraft.world.level.levelgen.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.DecoratorConfiguration;

public class DecoratorCarvingMaskConfig implements DecoratorConfiguration {
	protected final GenerationStep.Carving step;
	protected final float probability;

	public DecoratorCarvingMaskConfig(GenerationStep.Carving carving, float f) {
		this.step = carving;
		this.probability = f;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("step"),
					dynamicOps.createString(this.step.toString()),
					dynamicOps.createString("probability"),
					dynamicOps.createFloat(this.probability)
				)
			)
		);
	}

	public static DecoratorCarvingMaskConfig deserialize(Dynamic<?> dynamic) {
		GenerationStep.Carving carving = GenerationStep.Carving.valueOf(dynamic.get("step").asString(""));
		float f = dynamic.get("probability").asFloat(0.0F);
		return new DecoratorCarvingMaskConfig(carving, f);
	}
}

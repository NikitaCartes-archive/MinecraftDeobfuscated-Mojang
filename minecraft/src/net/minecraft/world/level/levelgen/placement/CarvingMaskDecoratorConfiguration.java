package net.minecraft.world.level.levelgen.placement;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class CarvingMaskDecoratorConfiguration implements DecoratorConfiguration {
	protected final GenerationStep.Carving step;
	protected final float probability;

	public CarvingMaskDecoratorConfiguration(GenerationStep.Carving carving, float f) {
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

	public static CarvingMaskDecoratorConfiguration deserialize(Dynamic<?> dynamic) {
		GenerationStep.Carving carving = GenerationStep.Carving.valueOf(dynamic.get("step").asString(""));
		float f = dynamic.get("probability").asFloat(0.0F);
		return new CarvingMaskDecoratorConfiguration(carving, f);
	}

	public static CarvingMaskDecoratorConfiguration random(Random random) {
		return new CarvingMaskDecoratorConfiguration(Util.randomEnum(GenerationStep.Carving.class, random), random.nextFloat() / 2.0F);
	}
}

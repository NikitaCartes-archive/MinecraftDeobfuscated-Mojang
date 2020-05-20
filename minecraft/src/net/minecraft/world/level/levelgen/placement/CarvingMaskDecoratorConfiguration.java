package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class CarvingMaskDecoratorConfiguration implements DecoratorConfiguration {
	public static final Codec<CarvingMaskDecoratorConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					GenerationStep.Carving.CODEC.fieldOf("step").forGetter(carvingMaskDecoratorConfiguration -> carvingMaskDecoratorConfiguration.step),
					Codec.FLOAT.fieldOf("probability").forGetter(carvingMaskDecoratorConfiguration -> carvingMaskDecoratorConfiguration.probability)
				)
				.apply(instance, CarvingMaskDecoratorConfiguration::new)
	);
	protected final GenerationStep.Carving step;
	protected final float probability;

	public CarvingMaskDecoratorConfiguration(GenerationStep.Carving carving, float f) {
		this.step = carving;
		this.probability = f;
	}
}

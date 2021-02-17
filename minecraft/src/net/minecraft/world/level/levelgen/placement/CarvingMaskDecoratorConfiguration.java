package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class CarvingMaskDecoratorConfiguration implements DecoratorConfiguration {
	public static final Codec<CarvingMaskDecoratorConfiguration> CODEC = GenerationStep.Carving.CODEC
		.fieldOf("step")
		.<CarvingMaskDecoratorConfiguration>xmap(CarvingMaskDecoratorConfiguration::new, carvingMaskDecoratorConfiguration -> carvingMaskDecoratorConfiguration.step)
		.codec();
	protected final GenerationStep.Carving step;

	public CarvingMaskDecoratorConfiguration(GenerationStep.Carving carving) {
		this.step = carving;
	}
}

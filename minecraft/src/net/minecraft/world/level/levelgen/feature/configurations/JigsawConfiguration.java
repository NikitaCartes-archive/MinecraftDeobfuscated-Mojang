package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public class JigsawConfiguration implements FeatureConfiguration {
	public static final Codec<JigsawConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("start_pool").forGetter(JigsawConfiguration::getStartPool),
					Codec.INT.fieldOf("size").forGetter(JigsawConfiguration::getSize)
				)
				.apply(instance, JigsawConfiguration::new)
	);
	public final ResourceLocation startPool;
	public final int size;

	public JigsawConfiguration(ResourceLocation resourceLocation, int i) {
		this.startPool = resourceLocation;
		this.size = i;
	}

	public int getSize() {
		return this.size;
	}

	public ResourceLocation getStartPool() {
		return this.startPool;
	}
}

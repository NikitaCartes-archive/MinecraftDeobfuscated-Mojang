package net.minecraft.world.level.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkGenerator;

public record LevelStem(Holder<DimensionType> type, ChunkGenerator generator) {
	public static final Codec<LevelStem> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					DimensionType.CODEC.fieldOf("type").forGetter(LevelStem::type), ChunkGenerator.CODEC.fieldOf("generator").forGetter(LevelStem::generator)
				)
				.apply(instance, instance.stable(LevelStem::new))
	);
	public static final ResourceKey<LevelStem> OVERWORLD = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("overworld"));
	public static final ResourceKey<LevelStem> NETHER = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("the_nether"));
	public static final ResourceKey<LevelStem> END = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("the_end"));
}

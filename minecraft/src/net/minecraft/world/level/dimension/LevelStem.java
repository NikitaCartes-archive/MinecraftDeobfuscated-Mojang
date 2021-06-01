package net.minecraft.world.level.dimension;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public final class LevelStem {
	public static final Codec<LevelStem> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					DimensionType.CODEC.fieldOf("type").flatXmap(ExtraCodecs.nonNullSupplierCheck(), ExtraCodecs.nonNullSupplierCheck()).forGetter(LevelStem::typeSupplier),
					ChunkGenerator.CODEC.fieldOf("generator").forGetter(LevelStem::generator)
				)
				.apply(instance, instance.stable(LevelStem::new))
	);
	public static final ResourceKey<LevelStem> OVERWORLD = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("overworld"));
	public static final ResourceKey<LevelStem> NETHER = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("the_nether"));
	public static final ResourceKey<LevelStem> END = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("the_end"));
	private static final Set<ResourceKey<LevelStem>> BUILTIN_ORDER = Sets.<ResourceKey<LevelStem>>newLinkedHashSet(ImmutableList.of(OVERWORLD, NETHER, END));
	private final Supplier<DimensionType> type;
	private final ChunkGenerator generator;

	public LevelStem(Supplier<DimensionType> supplier, ChunkGenerator chunkGenerator) {
		this.type = supplier;
		this.generator = chunkGenerator;
	}

	public Supplier<DimensionType> typeSupplier() {
		return this.type;
	}

	public DimensionType type() {
		return (DimensionType)this.type.get();
	}

	public ChunkGenerator generator() {
		return this.generator;
	}

	public static MappedRegistry<LevelStem> sortMap(MappedRegistry<LevelStem> mappedRegistry) {
		MappedRegistry<LevelStem> mappedRegistry2 = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());

		for (ResourceKey<LevelStem> resourceKey : BUILTIN_ORDER) {
			LevelStem levelStem = mappedRegistry.get(resourceKey);
			if (levelStem != null) {
				mappedRegistry2.register(resourceKey, levelStem, mappedRegistry.lifecycle(levelStem));
			}
		}

		for (Entry<ResourceKey<LevelStem>, LevelStem> entry : mappedRegistry.entrySet()) {
			ResourceKey<LevelStem> resourceKey2 = (ResourceKey<LevelStem>)entry.getKey();
			if (!BUILTIN_ORDER.contains(resourceKey2)) {
				mappedRegistry2.register(resourceKey2, (LevelStem)entry.getValue(), mappedRegistry.lifecycle((LevelStem)entry.getValue()));
			}
		}

		return mappedRegistry2;
	}

	public static boolean stable(long l, MappedRegistry<LevelStem> mappedRegistry) {
		List<Entry<ResourceKey<LevelStem>, LevelStem>> list = Lists.<Entry<ResourceKey<LevelStem>, LevelStem>>newArrayList(mappedRegistry.entrySet());
		if (list.size() != BUILTIN_ORDER.size()) {
			return false;
		} else {
			Entry<ResourceKey<LevelStem>, LevelStem> entry = (Entry<ResourceKey<LevelStem>, LevelStem>)list.get(0);
			Entry<ResourceKey<LevelStem>, LevelStem> entry2 = (Entry<ResourceKey<LevelStem>, LevelStem>)list.get(1);
			Entry<ResourceKey<LevelStem>, LevelStem> entry3 = (Entry<ResourceKey<LevelStem>, LevelStem>)list.get(2);
			if (entry.getKey() != OVERWORLD || entry2.getKey() != NETHER || entry3.getKey() != END) {
				return false;
			} else if (!((LevelStem)entry.getValue()).type().equalTo(DimensionType.DEFAULT_OVERWORLD)
				&& ((LevelStem)entry.getValue()).type() != DimensionType.DEFAULT_OVERWORLD_CAVES) {
				return false;
			} else if (!((LevelStem)entry2.getValue()).type().equalTo(DimensionType.DEFAULT_NETHER)) {
				return false;
			} else if (!((LevelStem)entry3.getValue()).type().equalTo(DimensionType.DEFAULT_END)) {
				return false;
			} else if (((LevelStem)entry2.getValue()).generator() instanceof NoiseBasedChunkGenerator
				&& ((LevelStem)entry3.getValue()).generator() instanceof NoiseBasedChunkGenerator) {
				NoiseBasedChunkGenerator noiseBasedChunkGenerator = (NoiseBasedChunkGenerator)((LevelStem)entry2.getValue()).generator();
				NoiseBasedChunkGenerator noiseBasedChunkGenerator2 = (NoiseBasedChunkGenerator)((LevelStem)entry3.getValue()).generator();
				if (!noiseBasedChunkGenerator.stable(l, NoiseGeneratorSettings.NETHER)) {
					return false;
				} else if (!noiseBasedChunkGenerator2.stable(l, NoiseGeneratorSettings.END)) {
					return false;
				} else if (!(noiseBasedChunkGenerator.getBiomeSource() instanceof MultiNoiseBiomeSource)) {
					return false;
				} else {
					MultiNoiseBiomeSource multiNoiseBiomeSource = (MultiNoiseBiomeSource)noiseBasedChunkGenerator.getBiomeSource();
					if (!multiNoiseBiomeSource.stable(l)) {
						return false;
					} else if (!(noiseBasedChunkGenerator2.getBiomeSource() instanceof TheEndBiomeSource)) {
						return false;
					} else {
						TheEndBiomeSource theEndBiomeSource = (TheEndBiomeSource)noiseBasedChunkGenerator2.getBiomeSource();
						return theEndBiomeSource.stable(l);
					}
				}
			} else {
				return false;
			}
		}
	}
}

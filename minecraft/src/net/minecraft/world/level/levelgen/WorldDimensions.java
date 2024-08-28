package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.PrimaryLevelData;

public record WorldDimensions(Map<ResourceKey<LevelStem>, LevelStem> dimensions) {
	public static final MapCodec<WorldDimensions> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Codec.unboundedMap(ResourceKey.codec(Registries.LEVEL_STEM), LevelStem.CODEC).fieldOf("dimensions").forGetter(WorldDimensions::dimensions)
				)
				.apply(instance, instance.stable(WorldDimensions::new))
	);
	private static final Set<ResourceKey<LevelStem>> BUILTIN_ORDER = ImmutableSet.of(LevelStem.OVERWORLD, LevelStem.NETHER, LevelStem.END);
	private static final int VANILLA_DIMENSION_COUNT = BUILTIN_ORDER.size();

	public WorldDimensions(Map<ResourceKey<LevelStem>, LevelStem> dimensions) {
		LevelStem levelStem = (LevelStem)dimensions.get(LevelStem.OVERWORLD);
		if (levelStem == null) {
			throw new IllegalStateException("Overworld settings missing");
		} else {
			this.dimensions = dimensions;
		}
	}

	public WorldDimensions(Registry<LevelStem> registry) {
		this((Map<ResourceKey<LevelStem>, LevelStem>)registry.listElements().collect(Collectors.toMap(Holder.Reference::key, Holder.Reference::value)));
	}

	public static Stream<ResourceKey<LevelStem>> keysInOrder(Stream<ResourceKey<LevelStem>> stream) {
		return Stream.concat(BUILTIN_ORDER.stream(), stream.filter(resourceKey -> !BUILTIN_ORDER.contains(resourceKey)));
	}

	public WorldDimensions replaceOverworldGenerator(HolderLookup.Provider provider, ChunkGenerator chunkGenerator) {
		HolderLookup<DimensionType> holderLookup = provider.lookupOrThrow(Registries.DIMENSION_TYPE);
		Map<ResourceKey<LevelStem>, LevelStem> map = withOverworld(holderLookup, this.dimensions, chunkGenerator);
		return new WorldDimensions(map);
	}

	public static Map<ResourceKey<LevelStem>, LevelStem> withOverworld(
		HolderLookup<DimensionType> holderLookup, Map<ResourceKey<LevelStem>, LevelStem> map, ChunkGenerator chunkGenerator
	) {
		LevelStem levelStem = (LevelStem)map.get(LevelStem.OVERWORLD);
		Holder<DimensionType> holder = (Holder<DimensionType>)(levelStem == null ? holderLookup.getOrThrow(BuiltinDimensionTypes.OVERWORLD) : levelStem.type());
		return withOverworld(map, holder, chunkGenerator);
	}

	public static Map<ResourceKey<LevelStem>, LevelStem> withOverworld(
		Map<ResourceKey<LevelStem>, LevelStem> map, Holder<DimensionType> holder, ChunkGenerator chunkGenerator
	) {
		Builder<ResourceKey<LevelStem>, LevelStem> builder = ImmutableMap.builder();
		builder.putAll(map);
		builder.put(LevelStem.OVERWORLD, new LevelStem(holder, chunkGenerator));
		return builder.buildKeepingLast();
	}

	public ChunkGenerator overworld() {
		LevelStem levelStem = (LevelStem)this.dimensions.get(LevelStem.OVERWORLD);
		if (levelStem == null) {
			throw new IllegalStateException("Overworld settings missing");
		} else {
			return levelStem.generator();
		}
	}

	public Optional<LevelStem> get(ResourceKey<LevelStem> resourceKey) {
		return Optional.ofNullable((LevelStem)this.dimensions.get(resourceKey));
	}

	public ImmutableSet<ResourceKey<Level>> levels() {
		return (ImmutableSet<ResourceKey<Level>>)this.dimensions().keySet().stream().map(Registries::levelStemToLevel).collect(ImmutableSet.toImmutableSet());
	}

	public boolean isDebug() {
		return this.overworld() instanceof DebugLevelSource;
	}

	private static PrimaryLevelData.SpecialWorldProperty specialWorldProperty(Registry<LevelStem> registry) {
		return (PrimaryLevelData.SpecialWorldProperty)registry.getOptional(LevelStem.OVERWORLD).map(levelStem -> {
			ChunkGenerator chunkGenerator = levelStem.generator();
			if (chunkGenerator instanceof DebugLevelSource) {
				return PrimaryLevelData.SpecialWorldProperty.DEBUG;
			} else {
				return chunkGenerator instanceof FlatLevelSource ? PrimaryLevelData.SpecialWorldProperty.FLAT : PrimaryLevelData.SpecialWorldProperty.NONE;
			}
		}).orElse(PrimaryLevelData.SpecialWorldProperty.NONE);
	}

	static Lifecycle checkStability(ResourceKey<LevelStem> resourceKey, LevelStem levelStem) {
		return isVanillaLike(resourceKey, levelStem) ? Lifecycle.stable() : Lifecycle.experimental();
	}

	private static boolean isVanillaLike(ResourceKey<LevelStem> resourceKey, LevelStem levelStem) {
		if (resourceKey == LevelStem.OVERWORLD) {
			return isStableOverworld(levelStem);
		} else if (resourceKey == LevelStem.NETHER) {
			return isStableNether(levelStem);
		} else {
			return resourceKey == LevelStem.END ? isStableEnd(levelStem) : false;
		}
	}

	private static boolean isStableOverworld(LevelStem levelStem) {
		Holder<DimensionType> holder = levelStem.type();
		if (!holder.is(BuiltinDimensionTypes.OVERWORLD) && !holder.is(BuiltinDimensionTypes.OVERWORLD_CAVES)) {
			return false;
		} else {
			if (levelStem.generator().getBiomeSource() instanceof MultiNoiseBiomeSource multiNoiseBiomeSource
				&& !multiNoiseBiomeSource.stable(MultiNoiseBiomeSourceParameterLists.OVERWORLD)) {
				return false;
			}

			return true;
		}
	}

	private static boolean isStableNether(LevelStem levelStem) {
		return levelStem.type().is(BuiltinDimensionTypes.NETHER)
			&& levelStem.generator() instanceof NoiseBasedChunkGenerator noiseBasedChunkGenerator
			&& noiseBasedChunkGenerator.stable(NoiseGeneratorSettings.NETHER)
			&& noiseBasedChunkGenerator.getBiomeSource() instanceof MultiNoiseBiomeSource multiNoiseBiomeSource
			&& multiNoiseBiomeSource.stable(MultiNoiseBiomeSourceParameterLists.NETHER);
	}

	private static boolean isStableEnd(LevelStem levelStem) {
		return levelStem.type().is(BuiltinDimensionTypes.END)
			&& levelStem.generator() instanceof NoiseBasedChunkGenerator noiseBasedChunkGenerator
			&& noiseBasedChunkGenerator.stable(NoiseGeneratorSettings.END)
			&& noiseBasedChunkGenerator.getBiomeSource() instanceof TheEndBiomeSource;
	}

	public WorldDimensions.Complete bake(Registry<LevelStem> registry) {
		Stream<ResourceKey<LevelStem>> stream = Stream.concat(registry.registryKeySet().stream(), this.dimensions.keySet().stream()).distinct();

		record Entry(ResourceKey<LevelStem> key, LevelStem value) {

			RegistrationInfo registrationInfo() {
				return new RegistrationInfo(Optional.empty(), WorldDimensions.checkStability(this.key, this.value));
			}
		}

		List<Entry> list = new ArrayList();
		keysInOrder(stream)
			.forEach(
				resourceKey -> registry.getOptional(resourceKey)
						.or(() -> Optional.ofNullable((LevelStem)this.dimensions.get(resourceKey)))
						.ifPresent(levelStem -> list.add(new Entry(resourceKey, levelStem)))
			);
		Lifecycle lifecycle = list.size() == VANILLA_DIMENSION_COUNT ? Lifecycle.stable() : Lifecycle.experimental();
		WritableRegistry<LevelStem> writableRegistry = new MappedRegistry<>(Registries.LEVEL_STEM, lifecycle);
		list.forEach(arg -> writableRegistry.register(arg.key, arg.value, arg.registrationInfo()));
		Registry<LevelStem> registry2 = writableRegistry.freeze();
		PrimaryLevelData.SpecialWorldProperty specialWorldProperty = specialWorldProperty(registry2);
		return new WorldDimensions.Complete(registry2.freeze(), specialWorldProperty);
	}

	public static record Complete(Registry<LevelStem> dimensions, PrimaryLevelData.SpecialWorldProperty specialWorldProperty) {
		public Lifecycle lifecycle() {
			return this.dimensions.registryLifecycle();
		}

		public RegistryAccess.Frozen dimensionsRegistryAccess() {
			return new RegistryAccess.ImmutableRegistryAccess(List.of(this.dimensions)).freeze();
		}
	}
}

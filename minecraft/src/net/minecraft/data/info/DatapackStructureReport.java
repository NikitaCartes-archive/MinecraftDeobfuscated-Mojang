package net.minecraft.data.info;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

public class DatapackStructureReport implements DataProvider {
	private final PackOutput output;
	private static final DatapackStructureReport.Entry PSEUDO_REGISTRY = new DatapackStructureReport.Entry(true, false, true);
	private static final DatapackStructureReport.Entry STABLE_DYNAMIC_REGISTRY = new DatapackStructureReport.Entry(true, true, true);
	private static final DatapackStructureReport.Entry UNSTABLE_DYNAMIC_REGISTRY = new DatapackStructureReport.Entry(true, true, false);
	private static final DatapackStructureReport.Entry BUILT_IN_REGISTRY = new DatapackStructureReport.Entry(false, true, true);
	private static final Map<ResourceKey<? extends Registry<?>>, DatapackStructureReport.Entry> MANUAL_ENTRIES = Map.of(
		Registries.RECIPE,
		PSEUDO_REGISTRY,
		Registries.ADVANCEMENT,
		PSEUDO_REGISTRY,
		Registries.LOOT_TABLE,
		STABLE_DYNAMIC_REGISTRY,
		Registries.ITEM_MODIFIER,
		STABLE_DYNAMIC_REGISTRY,
		Registries.PREDICATE,
		STABLE_DYNAMIC_REGISTRY
	);
	private static final Map<String, DatapackStructureReport.CustomPackEntry> NON_REGISTRY_ENTRIES = Map.of(
		"structure",
		new DatapackStructureReport.CustomPackEntry(DatapackStructureReport.Format.STRUCTURE, new DatapackStructureReport.Entry(true, false, true)),
		"function",
		new DatapackStructureReport.CustomPackEntry(DatapackStructureReport.Format.MCFUNCTION, new DatapackStructureReport.Entry(true, true, true))
	);
	static final Codec<ResourceKey<? extends Registry<?>>> REGISTRY_KEY_CODEC = ResourceLocation.CODEC.xmap(ResourceKey::createRegistryKey, ResourceKey::location);

	public DatapackStructureReport(PackOutput packOutput) {
		this.output = packOutput;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		DatapackStructureReport.Report report = new DatapackStructureReport.Report(this.listRegistries(), NON_REGISTRY_ENTRIES);
		Path path = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("datapack.json");
		return DataProvider.saveStable(cachedOutput, DatapackStructureReport.Report.CODEC.encodeStart(JsonOps.INSTANCE, report).getOrThrow(), path);
	}

	@Override
	public String getName() {
		return "Datapack Structure";
	}

	private void putIfNotPresent(
		Map<ResourceKey<? extends Registry<?>>, DatapackStructureReport.Entry> map,
		ResourceKey<? extends Registry<?>> resourceKey,
		DatapackStructureReport.Entry entry
	) {
		DatapackStructureReport.Entry entry2 = (DatapackStructureReport.Entry)map.putIfAbsent(resourceKey, entry);
		if (entry2 != null) {
			throw new IllegalStateException("Duplicate entry for key " + resourceKey.location());
		}
	}

	private Map<ResourceKey<? extends Registry<?>>, DatapackStructureReport.Entry> listRegistries() {
		Map<ResourceKey<? extends Registry<?>>, DatapackStructureReport.Entry> map = new HashMap();
		BuiltInRegistries.REGISTRY.forEach(registry -> this.putIfNotPresent(map, registry.key(), BUILT_IN_REGISTRY));
		RegistryDataLoader.WORLDGEN_REGISTRIES.forEach(registryData -> this.putIfNotPresent(map, registryData.key(), UNSTABLE_DYNAMIC_REGISTRY));
		RegistryDataLoader.DIMENSION_REGISTRIES.forEach(registryData -> this.putIfNotPresent(map, registryData.key(), UNSTABLE_DYNAMIC_REGISTRY));
		MANUAL_ENTRIES.forEach((resourceKey, entry) -> this.putIfNotPresent(map, resourceKey, entry));
		return map;
	}

	static record CustomPackEntry(DatapackStructureReport.Format format, DatapackStructureReport.Entry entry) {
		public static final Codec<DatapackStructureReport.CustomPackEntry> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						DatapackStructureReport.Format.CODEC.fieldOf("format").forGetter(DatapackStructureReport.CustomPackEntry::format),
						DatapackStructureReport.Entry.MAP_CODEC.forGetter(DatapackStructureReport.CustomPackEntry::entry)
					)
					.apply(instance, DatapackStructureReport.CustomPackEntry::new)
		);
	}

	static record Entry(boolean elements, boolean tags, boolean stable) {
		public static final MapCodec<DatapackStructureReport.Entry> MAP_CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						Codec.BOOL.fieldOf("elements").forGetter(DatapackStructureReport.Entry::elements),
						Codec.BOOL.fieldOf("tags").forGetter(DatapackStructureReport.Entry::tags),
						Codec.BOOL.fieldOf("stable").forGetter(DatapackStructureReport.Entry::stable)
					)
					.apply(instance, DatapackStructureReport.Entry::new)
		);
		public static final Codec<DatapackStructureReport.Entry> CODEC = MAP_CODEC.codec();
	}

	static enum Format implements StringRepresentable {
		STRUCTURE("structure"),
		MCFUNCTION("mcfunction");

		public static final Codec<DatapackStructureReport.Format> CODEC = StringRepresentable.fromEnum(DatapackStructureReport.Format::values);
		private final String name;

		private Format(final String string2) {
			this.name = string2;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}

	static record Report(
		Map<ResourceKey<? extends Registry<?>>, DatapackStructureReport.Entry> registries, Map<String, DatapackStructureReport.CustomPackEntry> others
	) {
		public static final Codec<DatapackStructureReport.Report> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.unboundedMap(DatapackStructureReport.REGISTRY_KEY_CODEC, DatapackStructureReport.Entry.CODEC)
							.fieldOf("registries")
							.forGetter(DatapackStructureReport.Report::registries),
						Codec.unboundedMap(Codec.STRING, DatapackStructureReport.CustomPackEntry.CODEC).fieldOf("others").forGetter(DatapackStructureReport.Report::others)
					)
					.apply(instance, DatapackStructureReport.Report::new)
		);
	}
}

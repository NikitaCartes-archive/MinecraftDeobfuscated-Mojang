package net.minecraft.data.info;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import org.slf4j.Logger;

public class BiomeParametersDumpReport implements DataProvider {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Path topPath;
	private final CompletableFuture<HolderLookup.Provider> registries;
	private static final MapCodec<ResourceKey<Biome>> ENTRY_CODEC = ResourceKey.codec(Registries.BIOME).fieldOf("biome");
	private static final Codec<Climate.ParameterList<ResourceKey<Biome>>> CODEC = Climate.ParameterList.codec(ENTRY_CODEC).fieldOf("biomes").codec();

	public BiomeParametersDumpReport(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
		this.topPath = packOutput.getOutputFolder(PackOutput.Target.REPORTS).resolve("biome_parameters");
		this.registries = completableFuture;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		return this.registries
			.thenCompose(
				provider -> {
					DynamicOps<JsonElement> dynamicOps = provider.createSerializationContext(JsonOps.INSTANCE);
					List<CompletableFuture<?>> list = new ArrayList();
					MultiNoiseBiomeSourceParameterList.knownPresets()
						.forEach((preset, parameterList) -> list.add(dumpValue(this.createPath(preset.id()), cachedOutput, dynamicOps, CODEC, parameterList)));
					return CompletableFuture.allOf((CompletableFuture[])list.toArray(CompletableFuture[]::new));
				}
			);
	}

	private static <E> CompletableFuture<?> dumpValue(Path path, CachedOutput cachedOutput, DynamicOps<JsonElement> dynamicOps, Encoder<E> encoder, E object) {
		Optional<JsonElement> optional = encoder.encodeStart(dynamicOps, object)
			.resultOrPartial(string -> LOGGER.error("Couldn't serialize element {}: {}", path, string));
		return optional.isPresent() ? DataProvider.saveStable(cachedOutput, (JsonElement)optional.get(), path) : CompletableFuture.completedFuture(null);
	}

	private Path createPath(ResourceLocation resourceLocation) {
		return this.topPath.resolve(resourceLocation.getNamespace()).resolve(resourceLocation.getPath() + ".json");
	}

	@Override
	public final String getName() {
		return "Biome Parameters";
	}
}

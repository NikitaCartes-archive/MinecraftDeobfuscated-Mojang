package net.minecraft.data.info;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.slf4j.Logger;

public class BiomeParametersDumpReport implements DataProvider {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final DataGenerator generator;

	public BiomeParametersDumpReport(DataGenerator dataGenerator) {
		this.generator = dataGenerator;
	}

	@Override
	public void run(CachedOutput cachedOutput) {
		Path path = this.generator.getOutputFolder();
		RegistryAccess.Frozen frozen = (RegistryAccess.Frozen)RegistryAccess.BUILTIN.get();
		DynamicOps<JsonElement> dynamicOps = RegistryOps.create(JsonOps.INSTANCE, frozen);
		Registry<Biome> registry = frozen.registryOrThrow(Registry.BIOME_REGISTRY);
		MultiNoiseBiomeSource.Preset.getPresets().forEach(pair -> {
			MultiNoiseBiomeSource multiNoiseBiomeSource = ((MultiNoiseBiomeSource.Preset)pair.getSecond()).biomeSource(registry, false);
			dumpValue(createPath(path, (ResourceLocation)pair.getFirst()), cachedOutput, dynamicOps, MultiNoiseBiomeSource.CODEC, multiNoiseBiomeSource);
		});
	}

	private static <E> void dumpValue(Path path, CachedOutput cachedOutput, DynamicOps<JsonElement> dynamicOps, Encoder<E> encoder, E object) {
		try {
			Optional<JsonElement> optional = encoder.encodeStart(dynamicOps, object)
				.resultOrPartial(string -> LOGGER.error("Couldn't serialize element {}: {}", path, string));
			if (optional.isPresent()) {
				DataProvider.save(GSON, cachedOutput, (JsonElement)optional.get(), path);
			}
		} catch (IOException var6) {
			LOGGER.error("Couldn't save element {}", path, var6);
		}
	}

	private static Path createPath(Path path, ResourceLocation resourceLocation) {
		return resolveTopPath(path).resolve(resourceLocation.getNamespace()).resolve(resourceLocation.getPath() + ".json");
	}

	private static Path resolveTopPath(Path path) {
		return path.resolve("reports").resolve("biome_parameters");
	}

	@Override
	public String getName() {
		return "Biome Parameters";
	}
}

package net.minecraft.data.worldgen.biome;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiomeReport implements DataProvider {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final DataGenerator generator;

	public BiomeReport(DataGenerator dataGenerator) {
		this.generator = dataGenerator;
	}

	@Override
	public void run(HashCache hashCache) {
		Path path = this.generator.getOutputFolder();

		for (Entry<ResourceKey<Biome>, Biome> entry : BuiltinRegistries.BIOME.entrySet()) {
			Path path2 = createPath(path, ((ResourceKey)entry.getKey()).location());
			Biome biome = (Biome)entry.getValue();
			Function<Supplier<Biome>, DataResult<JsonElement>> function = JsonOps.INSTANCE.withEncoder(Biome.CODEC);

			try {
				Optional<JsonElement> optional = ((DataResult)function.apply((Supplier)() -> biome)).result();
				if (optional.isPresent()) {
					DataProvider.save(GSON, hashCache, (JsonElement)optional.get(), path2);
				} else {
					LOGGER.error("Couldn't serialize biome {}", path2);
				}
			} catch (IOException var9) {
				LOGGER.error("Couldn't save biome {}", path2, var9);
			}
		}
	}

	private static Path createPath(Path path, ResourceLocation resourceLocation) {
		return path.resolve("reports/biomes/" + resourceLocation.getPath() + ".json");
	}

	@Override
	public String getName() {
		return "Biomes";
	}
}

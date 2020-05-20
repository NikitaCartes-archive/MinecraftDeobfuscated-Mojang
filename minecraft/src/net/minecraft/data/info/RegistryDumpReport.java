package net.minecraft.data.info;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;

public class RegistryDumpReport implements DataProvider {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final DataGenerator generator;

	public RegistryDumpReport(DataGenerator dataGenerator) {
		this.generator = dataGenerator;
	}

	@Override
	public void run(HashCache hashCache) throws IOException {
		JsonObject jsonObject = new JsonObject();
		Registry.REGISTRY
			.keySet()
			.forEach(resourceLocation -> jsonObject.add(resourceLocation.toString(), dumpRegistry((Registry<?>)Registry.REGISTRY.get(resourceLocation))));
		Path path = this.generator.getOutputFolder().resolve("reports/registries.json");
		DataProvider.save(GSON, hashCache, jsonObject, path);
	}

	private static <T> JsonElement dumpRegistry(Registry<T> registry) {
		JsonObject jsonObject = new JsonObject();
		if (registry instanceof DefaultedRegistry) {
			ResourceLocation resourceLocation = ((DefaultedRegistry)registry).getDefaultKey();
			jsonObject.addProperty("default", resourceLocation.toString());
		}

		int i = Registry.REGISTRY.getId(registry);
		jsonObject.addProperty("protocol_id", i);
		JsonObject jsonObject2 = new JsonObject();

		for (ResourceLocation resourceLocation2 : registry.keySet()) {
			T object = registry.get(resourceLocation2);
			int j = registry.getId(object);
			JsonObject jsonObject3 = new JsonObject();
			jsonObject3.addProperty("protocol_id", j);
			jsonObject2.add(resourceLocation2.toString(), jsonObject3);
		}

		jsonObject.add("entries", jsonObject2);
		return jsonObject;
	}

	@Override
	public String getName() {
		return "Registry Dump";
	}
}

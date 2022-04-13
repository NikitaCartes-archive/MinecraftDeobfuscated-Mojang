package net.minecraft.data.info;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;

public class RegistryDumpReport implements DataProvider {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final DataGenerator generator;

	public RegistryDumpReport(DataGenerator dataGenerator) {
		this.generator = dataGenerator;
	}

	@Override
	public void run(CachedOutput cachedOutput) throws IOException {
		JsonObject jsonObject = new JsonObject();
		Registry.REGISTRY.holders().forEach(reference -> jsonObject.add(reference.key().location().toString(), dumpRegistry((Registry)reference.value())));
		Path path = this.generator.getOutputFolder().resolve("reports/registries.json");
		DataProvider.save(GSON, cachedOutput, jsonObject, path);
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
		registry.holders().forEach(reference -> {
			T object = (T)reference.value();
			int ix = registry.getId(object);
			JsonObject jsonObject2x = new JsonObject();
			jsonObject2x.addProperty("protocol_id", ix);
			jsonObject2.add(reference.key().location().toString(), jsonObject2x);
		});
		jsonObject.add("entries", jsonObject2);
		return jsonObject;
	}

	@Override
	public String getName() {
		return "Registry Dump";
	}
}

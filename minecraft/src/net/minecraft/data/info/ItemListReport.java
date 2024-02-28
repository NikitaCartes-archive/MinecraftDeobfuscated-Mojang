package net.minecraft.data.info;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ItemListReport implements DataProvider {
	private final PackOutput output;
	private final CompletableFuture<HolderLookup.Provider> registries;

	public ItemListReport(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
		this.output = packOutput;
		this.registries = completableFuture;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		Path path = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("items.json");
		return this.registries.thenCompose(provider -> {
			JsonObject jsonObject = new JsonObject();
			RegistryOps<JsonElement> registryOps = provider.createSerializationContext(JsonOps.INSTANCE);
			provider.lookupOrThrow(Registries.ITEM).listElements().forEach(reference -> {
				JsonObject jsonObject2 = new JsonObject();
				JsonArray jsonArray = new JsonArray();
				((Item)reference.value()).components().forEach(typedDataComponent -> jsonArray.add(dumpComponent(typedDataComponent, registryOps)));
				jsonObject2.add("components", jsonArray);
				jsonObject.add(reference.getRegisteredName(), jsonObject2);
			});
			return DataProvider.saveStable(cachedOutput, jsonObject, path);
		});
	}

	private static <T> JsonElement dumpComponent(TypedDataComponent<T> typedDataComponent, DynamicOps<JsonElement> dynamicOps) {
		ResourceLocation resourceLocation = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(typedDataComponent.type());
		JsonElement jsonElement = Util.getOrThrow(
			typedDataComponent.encodeValue(dynamicOps), string -> new IllegalStateException("Failed to serialize component " + resourceLocation + ": " + string)
		);
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("type", resourceLocation.toString());
		jsonObject.add("value", jsonElement);
		return jsonObject;
	}

	@Override
	public final String getName() {
		return "Item List";
	}
}

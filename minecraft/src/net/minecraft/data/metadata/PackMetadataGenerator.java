package net.minecraft.data.metadata;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.bridge.game.PackType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.DetectedVersion;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.world.flag.FeatureFlagSet;

public class PackMetadataGenerator implements DataProvider {
	private final String name;
	private final PackOutput output;
	private final Map<String, Supplier<JsonElement>> elements = new HashMap();

	public PackMetadataGenerator(PackOutput packOutput, String string) {
		this.output = packOutput;
		this.name = string;
	}

	public <T> PackMetadataGenerator add(MetadataSectionType<T> metadataSectionType, T object) {
		this.elements.put(metadataSectionType.getMetadataSectionName(), (Supplier)() -> metadataSectionType.toJson(object));
		return this;
	}

	@Override
	public void run(CachedOutput cachedOutput) throws IOException {
		JsonObject jsonObject = new JsonObject();
		this.elements.forEach((string, supplier) -> jsonObject.add(string, (JsonElement)supplier.get()));
		DataProvider.saveStable(cachedOutput, jsonObject, this.output.getOutputFolder().resolve("pack.mcmeta"));
	}

	@Override
	public String getName() {
		return this.name;
	}

	public static PackMetadataGenerator forFeaturePack(PackOutput packOutput, String string, Component component, FeatureFlagSet featureFlagSet) {
		return new PackMetadataGenerator(packOutput, "Pack metadata for " + string)
			.add(PackMetadataSection.TYPE, new PackMetadataSection(component, DetectedVersion.BUILT_IN.getPackVersion(PackType.DATA)))
			.add(FeatureFlagsMetadataSection.TYPE, new FeatureFlagsMetadataSection(featureFlagSet));
	}
}

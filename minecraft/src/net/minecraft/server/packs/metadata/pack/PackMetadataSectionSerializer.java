package net.minecraft.server.packs.metadata.pack;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.GsonHelper;

public class PackMetadataSectionSerializer implements MetadataSectionType<PackMetadataSection> {
	public PackMetadataSection fromJson(JsonObject jsonObject) {
		Component component = Component.Serializer.fromJson(jsonObject.get("description"));
		if (component == null) {
			throw new JsonParseException("Invalid/missing description!");
		} else {
			int i = GsonHelper.getAsInt(jsonObject, "pack_format");
			return new PackMetadataSection(component, i);
		}
	}

	public JsonObject toJson(PackMetadataSection packMetadataSection) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("description", Component.Serializer.toJsonTree(packMetadataSection.getDescription()));
		jsonObject.addProperty("pack_format", packMetadataSection.getPackFormat());
		return jsonObject;
	}

	@Override
	public String getMetadataSectionName() {
		return "pack";
	}
}

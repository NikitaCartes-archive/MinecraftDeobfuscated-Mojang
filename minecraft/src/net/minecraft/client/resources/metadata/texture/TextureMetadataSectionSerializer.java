package net.minecraft.client.resources.metadata.texture;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;

@Environment(EnvType.CLIENT)
public class TextureMetadataSectionSerializer implements MetadataSectionSerializer<TextureMetadataSection> {
	public TextureMetadataSection fromJson(JsonObject jsonObject) {
		boolean bl = GsonHelper.getAsBoolean(jsonObject, "blur", false);
		boolean bl2 = GsonHelper.getAsBoolean(jsonObject, "clamp", false);
		return new TextureMetadataSection(bl, bl2);
	}

	@Override
	public String getMetadataSectionName() {
		return "texture";
	}
}

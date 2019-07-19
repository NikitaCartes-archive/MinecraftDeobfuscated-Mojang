package net.minecraft.client.particle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

@Environment(EnvType.CLIENT)
public class ParticleDescription {
	@Nullable
	private final List<ResourceLocation> textures;

	private ParticleDescription(@Nullable List<ResourceLocation> list) {
		this.textures = list;
	}

	@Nullable
	public List<ResourceLocation> getTextures() {
		return this.textures;
	}

	public static ParticleDescription fromJson(JsonObject jsonObject) {
		JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "textures", null);
		List<ResourceLocation> list;
		if (jsonArray != null) {
			list = (List<ResourceLocation>)Streams.stream(jsonArray)
				.map(jsonElement -> GsonHelper.convertToString(jsonElement, "texture"))
				.map(ResourceLocation::new)
				.collect(ImmutableList.toImmutableList());
		} else {
			list = null;
		}

		return new ParticleDescription(list);
	}
}

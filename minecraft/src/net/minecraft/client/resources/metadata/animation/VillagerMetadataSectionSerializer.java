package net.minecraft.client.resources.metadata.animation;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;

@Environment(EnvType.CLIENT)
public class VillagerMetadataSectionSerializer implements MetadataSectionSerializer<VillagerMetaDataSection> {
	public VillagerMetaDataSection fromJson(JsonObject jsonObject) {
		return new VillagerMetaDataSection(VillagerMetaDataSection.Hat.getByName(GsonHelper.getAsString(jsonObject, "hat", "none")));
	}

	@Override
	public String getMetadataSectionName() {
		return "villager";
	}
}

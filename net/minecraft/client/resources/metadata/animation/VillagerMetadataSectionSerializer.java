/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.metadata.animation;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.metadata.animation.VillagerMetaDataSection;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;

@Environment(value=EnvType.CLIENT)
public class VillagerMetadataSectionSerializer
implements MetadataSectionSerializer<VillagerMetaDataSection> {
    @Override
    public VillagerMetaDataSection fromJson(JsonObject jsonObject) {
        return new VillagerMetaDataSection(VillagerMetaDataSection.Hat.getByName(GsonHelper.getAsString(jsonObject, "hat", "none")));
    }

    @Override
    public String getMetadataSectionName() {
        return "villager";
    }

    @Override
    public /* synthetic */ Object fromJson(JsonObject jsonObject) {
        return this.fromJson(jsonObject);
    }
}


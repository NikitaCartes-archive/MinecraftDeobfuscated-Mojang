/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.metadata.pack;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.GsonHelper;

public class PackMetadataSectionSerializer
implements MetadataSectionSerializer<PackMetadataSection> {
    @Override
    public PackMetadataSection fromJson(JsonObject jsonObject) {
        MutableComponent component = Component.Serializer.fromJson(jsonObject.get("description"));
        if (component == null) {
            throw new JsonParseException("Invalid/missing description!");
        }
        int i = GsonHelper.getAsInt(jsonObject, "pack_format");
        return new PackMetadataSection(component, i);
    }

    @Override
    public String getMetadataSectionName() {
        return "pack";
    }

    @Override
    public /* synthetic */ Object fromJson(JsonObject jsonObject) {
        return this.fromJson(jsonObject);
    }
}


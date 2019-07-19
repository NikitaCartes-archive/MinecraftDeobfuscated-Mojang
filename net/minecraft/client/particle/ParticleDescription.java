/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.particle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
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
        List list = jsonArray != null ? (List)Streams.stream(jsonArray).map(jsonElement -> GsonHelper.convertToString(jsonElement, "texture")).map(ResourceLocation::new).collect(ImmutableList.toImmutableList()) : null;
        return new ParticleDescription(list);
    }
}


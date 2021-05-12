/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.metadata.language;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.HashSet;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.metadata.language.LanguageMetadataSection;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;

@Environment(value=EnvType.CLIENT)
public class LanguageMetadataSectionSerializer
implements MetadataSectionSerializer<LanguageMetadataSection> {
    private static final int MAX_LANGUAGE_LENGTH = 16;

    @Override
    public LanguageMetadataSection fromJson(JsonObject jsonObject) {
        HashSet<LanguageInfo> set = Sets.newHashSet();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String string = entry.getKey();
            if (string.length() > 16) {
                throw new JsonParseException("Invalid language->'" + string + "': language code must not be more than 16 characters long");
            }
            JsonObject jsonObject2 = GsonHelper.convertToJsonObject(entry.getValue(), "language");
            String string2 = GsonHelper.getAsString(jsonObject2, "region");
            String string3 = GsonHelper.getAsString(jsonObject2, "name");
            boolean bl = GsonHelper.getAsBoolean(jsonObject2, "bidirectional", false);
            if (string2.isEmpty()) {
                throw new JsonParseException("Invalid language->'" + string + "'->region: empty value");
            }
            if (string3.isEmpty()) {
                throw new JsonParseException("Invalid language->'" + string + "'->name: empty value");
            }
            if (set.add(new LanguageInfo(string, string2, string3, bl))) continue;
            throw new JsonParseException("Duplicate language->'" + string + "' defined");
        }
        return new LanguageMetadataSection(set);
    }

    @Override
    public String getMetadataSectionName() {
        return "language";
    }

    @Override
    public /* synthetic */ Object fromJson(JsonObject jsonObject) {
        return this.fromJson(jsonObject);
    }
}


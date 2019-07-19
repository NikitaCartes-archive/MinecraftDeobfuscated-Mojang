/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.lang.reflect.Type;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.ConstantIntValue;
import net.minecraft.world.level.storage.loot.RandomIntGenerator;
import net.minecraft.world.level.storage.loot.RandomValueBounds;

public class RandomIntGenerators {
    private static final Map<ResourceLocation, Class<? extends RandomIntGenerator>> GENERATORS = Maps.newHashMap();

    public static RandomIntGenerator deserialize(JsonElement jsonElement, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement.isJsonPrimitive()) {
            return (RandomIntGenerator)jsonDeserializationContext.deserialize(jsonElement, (Type)((Object)ConstantIntValue.class));
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String string = GsonHelper.getAsString(jsonObject, "type", RandomIntGenerator.UNIFORM.toString());
        Class<? extends RandomIntGenerator> class_ = GENERATORS.get(new ResourceLocation(string));
        if (class_ == null) {
            throw new JsonParseException("Unknown generator: " + string);
        }
        return (RandomIntGenerator)jsonDeserializationContext.deserialize(jsonObject, class_);
    }

    public static JsonElement serialize(RandomIntGenerator randomIntGenerator, JsonSerializationContext jsonSerializationContext) {
        JsonElement jsonElement = jsonSerializationContext.serialize(randomIntGenerator);
        if (jsonElement.isJsonObject()) {
            jsonElement.getAsJsonObject().addProperty("type", randomIntGenerator.getType().toString());
        }
        return jsonElement;
    }

    static {
        GENERATORS.put(RandomIntGenerator.UNIFORM, RandomValueBounds.class);
        GENERATORS.put(RandomIntGenerator.BINOMIAL, BinomialDistributionGenerator.class);
        GENERATORS.put(RandomIntGenerator.CONSTANT, ConstantIntValue.class);
    }
}


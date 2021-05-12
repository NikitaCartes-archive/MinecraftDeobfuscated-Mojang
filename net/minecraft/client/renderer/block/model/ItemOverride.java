/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.block.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

@Environment(value=EnvType.CLIENT)
public class ItemOverride {
    private final ResourceLocation model;
    private final List<Predicate> predicates;

    public ItemOverride(ResourceLocation resourceLocation, List<Predicate> list) {
        this.model = resourceLocation;
        this.predicates = ImmutableList.copyOf(list);
    }

    public ResourceLocation getModel() {
        return this.model;
    }

    public Stream<Predicate> getPredicates() {
        return this.predicates.stream();
    }

    @Environment(value=EnvType.CLIENT)
    public static class Predicate {
        private final ResourceLocation property;
        private final float value;

        public Predicate(ResourceLocation resourceLocation, float f) {
            this.property = resourceLocation;
            this.value = f;
        }

        public ResourceLocation getProperty() {
            return this.property;
        }

        public float getValue() {
            return this.value;
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Deserializer
    implements JsonDeserializer<ItemOverride> {
        protected Deserializer() {
        }

        @Override
        public ItemOverride deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "model"));
            List<Predicate> list = this.getPredicates(jsonObject);
            return new ItemOverride(resourceLocation, list);
        }

        protected List<Predicate> getPredicates(JsonObject jsonObject) {
            LinkedHashMap<ResourceLocation, Float> map = Maps.newLinkedHashMap();
            JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "predicate");
            for (Map.Entry<String, JsonElement> entry2 : jsonObject2.entrySet()) {
                map.put(new ResourceLocation(entry2.getKey()), Float.valueOf(GsonHelper.convertToFloat(entry2.getValue(), entry2.getKey())));
            }
            return map.entrySet().stream().map(entry -> new Predicate((ResourceLocation)entry.getKey(), ((Float)entry.getValue()).floatValue())).collect(ImmutableList.toImmutableList());
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}


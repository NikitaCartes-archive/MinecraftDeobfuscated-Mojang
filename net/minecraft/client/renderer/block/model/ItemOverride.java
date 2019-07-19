/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemPropertyFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ItemOverride {
    private final ResourceLocation model;
    private final Map<ResourceLocation, Float> predicates;

    public ItemOverride(ResourceLocation resourceLocation, Map<ResourceLocation, Float> map) {
        this.model = resourceLocation;
        this.predicates = map;
    }

    public ResourceLocation getModel() {
        return this.model;
    }

    boolean test(ItemStack itemStack, @Nullable Level level, @Nullable LivingEntity livingEntity) {
        Item item = itemStack.getItem();
        for (Map.Entry<ResourceLocation, Float> entry : this.predicates.entrySet()) {
            ItemPropertyFunction itemPropertyFunction = item.getProperty(entry.getKey());
            if (itemPropertyFunction != null && !(itemPropertyFunction.call(itemStack, level, livingEntity) < entry.getValue().floatValue())) continue;
            return false;
        }
        return true;
    }

    @Environment(value=EnvType.CLIENT)
    public static class Deserializer
    implements JsonDeserializer<ItemOverride> {
        protected Deserializer() {
        }

        @Override
        public ItemOverride deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "model"));
            Map<ResourceLocation, Float> map = this.getPredicates(jsonObject);
            return new ItemOverride(resourceLocation, map);
        }

        protected Map<ResourceLocation, Float> getPredicates(JsonObject jsonObject) {
            LinkedHashMap<ResourceLocation, Float> map = Maps.newLinkedHashMap();
            JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "predicate");
            for (Map.Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
                map.put(new ResourceLocation(entry.getKey()), Float.valueOf(GsonHelper.convertToFloat(entry.getValue(), entry.getKey())));
            }
            return map;
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}


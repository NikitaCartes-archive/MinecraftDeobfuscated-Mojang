/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class MobEffectsPredicate {
    public static final MobEffectsPredicate ANY = new MobEffectsPredicate(Collections.emptyMap());
    private final Map<MobEffect, MobEffectInstancePredicate> effects;

    public MobEffectsPredicate(Map<MobEffect, MobEffectInstancePredicate> map) {
        this.effects = map;
    }

    public static MobEffectsPredicate effects() {
        return new MobEffectsPredicate(Maps.newLinkedHashMap());
    }

    public MobEffectsPredicate and(MobEffect mobEffect) {
        this.effects.put(mobEffect, new MobEffectInstancePredicate());
        return this;
    }

    public boolean matches(Entity entity) {
        if (this == ANY) {
            return true;
        }
        if (entity instanceof LivingEntity) {
            return this.matches(((LivingEntity)entity).getActiveEffectsMap());
        }
        return false;
    }

    public boolean matches(LivingEntity livingEntity) {
        if (this == ANY) {
            return true;
        }
        return this.matches(livingEntity.getActiveEffectsMap());
    }

    public boolean matches(Map<MobEffect, MobEffectInstance> map) {
        if (this == ANY) {
            return true;
        }
        for (Map.Entry<MobEffect, MobEffectInstancePredicate> entry : this.effects.entrySet()) {
            MobEffectInstance mobEffectInstance = map.get(entry.getKey());
            if (entry.getValue().matches(mobEffectInstance)) continue;
            return false;
        }
        return true;
    }

    public static MobEffectsPredicate fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "effects");
        LinkedHashMap<MobEffect, MobEffectInstancePredicate> map = Maps.newLinkedHashMap();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            ResourceLocation resourceLocation = new ResourceLocation(entry.getKey());
            MobEffect mobEffect = Registry.MOB_EFFECT.getOptional(resourceLocation).orElseThrow(() -> new JsonSyntaxException("Unknown effect '" + resourceLocation + "'"));
            MobEffectInstancePredicate mobEffectInstancePredicate = MobEffectInstancePredicate.fromJson(GsonHelper.convertToJsonObject(entry.getValue(), entry.getKey()));
            map.put(mobEffect, mobEffectInstancePredicate);
        }
        return new MobEffectsPredicate(map);
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<MobEffect, MobEffectInstancePredicate> entry : this.effects.entrySet()) {
            jsonObject.add(Registry.MOB_EFFECT.getKey(entry.getKey()).toString(), entry.getValue().serializeToJson());
        }
        return jsonObject;
    }

    public static class MobEffectInstancePredicate {
        private final MinMaxBounds.Ints amplifier;
        private final MinMaxBounds.Ints duration;
        @Nullable
        private final Boolean ambient;
        @Nullable
        private final Boolean visible;

        public MobEffectInstancePredicate(MinMaxBounds.Ints ints, MinMaxBounds.Ints ints2, @Nullable Boolean boolean_, @Nullable Boolean boolean2) {
            this.amplifier = ints;
            this.duration = ints2;
            this.ambient = boolean_;
            this.visible = boolean2;
        }

        public MobEffectInstancePredicate() {
            this(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, null, null);
        }

        public boolean matches(@Nullable MobEffectInstance mobEffectInstance) {
            if (mobEffectInstance == null) {
                return false;
            }
            if (!this.amplifier.matches(mobEffectInstance.getAmplifier())) {
                return false;
            }
            if (!this.duration.matches(mobEffectInstance.getDuration())) {
                return false;
            }
            if (this.ambient != null && this.ambient.booleanValue() != mobEffectInstance.isAmbient()) {
                return false;
            }
            return this.visible == null || this.visible.booleanValue() == mobEffectInstance.isVisible();
        }

        public JsonElement serializeToJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("amplifier", this.amplifier.serializeToJson());
            jsonObject.add("duration", this.duration.serializeToJson());
            jsonObject.addProperty("ambient", this.ambient);
            jsonObject.addProperty("visible", this.visible);
            return jsonObject;
        }

        public static MobEffectInstancePredicate fromJson(JsonObject jsonObject) {
            MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("amplifier"));
            MinMaxBounds.Ints ints2 = MinMaxBounds.Ints.fromJson(jsonObject.get("duration"));
            Boolean boolean_ = jsonObject.has("ambient") ? Boolean.valueOf(GsonHelper.getAsBoolean(jsonObject, "ambient")) : null;
            Boolean boolean2 = jsonObject.has("visible") ? Boolean.valueOf(GsonHelper.getAsBoolean(jsonObject, "visible")) : null;
            return new MobEffectInstancePredicate(ints, ints2, boolean_, boolean2);
        }
    }
}


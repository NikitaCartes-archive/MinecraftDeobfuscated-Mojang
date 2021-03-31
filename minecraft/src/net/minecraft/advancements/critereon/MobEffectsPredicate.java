package net.minecraft.advancements.critereon;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class MobEffectsPredicate {
	public static final MobEffectsPredicate ANY = new MobEffectsPredicate(Collections.emptyMap());
	private final Map<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> effects;

	public MobEffectsPredicate(Map<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> map) {
		this.effects = map;
	}

	public static MobEffectsPredicate effects() {
		return new MobEffectsPredicate(Maps.<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate>newLinkedHashMap());
	}

	public MobEffectsPredicate and(MobEffect mobEffect) {
		this.effects.put(mobEffect, new MobEffectsPredicate.MobEffectInstancePredicate());
		return this;
	}

	public MobEffectsPredicate and(MobEffect mobEffect, MobEffectsPredicate.MobEffectInstancePredicate mobEffectInstancePredicate) {
		this.effects.put(mobEffect, mobEffectInstancePredicate);
		return this;
	}

	public boolean matches(Entity entity) {
		if (this == ANY) {
			return true;
		} else {
			return entity instanceof LivingEntity ? this.matches(((LivingEntity)entity).getActiveEffectsMap()) : false;
		}
	}

	public boolean matches(LivingEntity livingEntity) {
		return this == ANY ? true : this.matches(livingEntity.getActiveEffectsMap());
	}

	public boolean matches(Map<MobEffect, MobEffectInstance> map) {
		if (this == ANY) {
			return true;
		} else {
			for (Entry<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> entry : this.effects.entrySet()) {
				MobEffectInstance mobEffectInstance = (MobEffectInstance)map.get(entry.getKey());
				if (!((MobEffectsPredicate.MobEffectInstancePredicate)entry.getValue()).matches(mobEffectInstance)) {
					return false;
				}
			}

			return true;
		}
	}

	public static MobEffectsPredicate fromJson(@Nullable JsonElement jsonElement) {
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "effects");
			Map<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> map = Maps.<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate>newLinkedHashMap();

			for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
				ResourceLocation resourceLocation = new ResourceLocation((String)entry.getKey());
				MobEffect mobEffect = (MobEffect)Registry.MOB_EFFECT
					.getOptional(resourceLocation)
					.orElseThrow(() -> new JsonSyntaxException("Unknown effect '" + resourceLocation + "'"));
				MobEffectsPredicate.MobEffectInstancePredicate mobEffectInstancePredicate = MobEffectsPredicate.MobEffectInstancePredicate.fromJson(
					GsonHelper.convertToJsonObject((JsonElement)entry.getValue(), (String)entry.getKey())
				);
				map.put(mobEffect, mobEffectInstancePredicate);
			}

			return new MobEffectsPredicate(map);
		} else {
			return ANY;
		}
	}

	public JsonElement serializeToJson() {
		if (this == ANY) {
			return JsonNull.INSTANCE;
		} else {
			JsonObject jsonObject = new JsonObject();

			for (Entry<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> entry : this.effects.entrySet()) {
				jsonObject.add(
					Registry.MOB_EFFECT.getKey((MobEffect)entry.getKey()).toString(), ((MobEffectsPredicate.MobEffectInstancePredicate)entry.getValue()).serializeToJson()
				);
			}

			return jsonObject;
		}
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
			} else if (!this.amplifier.matches(mobEffectInstance.getAmplifier())) {
				return false;
			} else if (!this.duration.matches(mobEffectInstance.getDuration())) {
				return false;
			} else {
				return this.ambient != null && this.ambient != mobEffectInstance.isAmbient()
					? false
					: this.visible == null || this.visible == mobEffectInstance.isVisible();
			}
		}

		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("amplifier", this.amplifier.serializeToJson());
			jsonObject.add("duration", this.duration.serializeToJson());
			jsonObject.addProperty("ambient", this.ambient);
			jsonObject.addProperty("visible", this.visible);
			return jsonObject;
		}

		public static MobEffectsPredicate.MobEffectInstancePredicate fromJson(JsonObject jsonObject) {
			MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("amplifier"));
			MinMaxBounds.Ints ints2 = MinMaxBounds.Ints.fromJson(jsonObject.get("duration"));
			Boolean boolean_ = jsonObject.has("ambient") ? GsonHelper.getAsBoolean(jsonObject, "ambient") : null;
			Boolean boolean2 = jsonObject.has("visible") ? GsonHelper.getAsBoolean(jsonObject, "visible") : null;
			return new MobEffectsPredicate.MobEffectInstancePredicate(ints, ints2, boolean_, boolean2);
		}
	}
}

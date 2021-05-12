package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class SetStewEffectFunction extends LootItemConditionalFunction {
	final Map<MobEffect, NumberProvider> effectDurationMap;

	SetStewEffectFunction(LootItemCondition[] lootItemConditions, Map<MobEffect, NumberProvider> map) {
		super(lootItemConditions);
		this.effectDurationMap = ImmutableMap.copyOf(map);
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_STEW_EFFECT;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return (Set<LootContextParam<?>>)this.effectDurationMap
			.values()
			.stream()
			.flatMap(numberProvider -> numberProvider.getReferencedContextParams().stream())
			.collect(ImmutableSet.toImmutableSet());
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		if (itemStack.is(Items.SUSPICIOUS_STEW) && !this.effectDurationMap.isEmpty()) {
			Random random = lootContext.getRandom();
			int i = random.nextInt(this.effectDurationMap.size());
			Entry<MobEffect, NumberProvider> entry = Iterables.get(this.effectDurationMap.entrySet(), i);
			MobEffect mobEffect = (MobEffect)entry.getKey();
			int j = ((NumberProvider)entry.getValue()).getInt(lootContext);
			if (!mobEffect.isInstantenous()) {
				j *= 20;
			}

			SuspiciousStewItem.saveMobEffect(itemStack, mobEffect, j);
			return itemStack;
		} else {
			return itemStack;
		}
	}

	public static SetStewEffectFunction.Builder stewEffect() {
		return new SetStewEffectFunction.Builder();
	}

	public static class Builder extends LootItemConditionalFunction.Builder<SetStewEffectFunction.Builder> {
		private final Map<MobEffect, NumberProvider> effectDurationMap = Maps.<MobEffect, NumberProvider>newHashMap();

		protected SetStewEffectFunction.Builder getThis() {
			return this;
		}

		public SetStewEffectFunction.Builder withEffect(MobEffect mobEffect, NumberProvider numberProvider) {
			this.effectDurationMap.put(mobEffect, numberProvider);
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new SetStewEffectFunction(this.getConditions(), this.effectDurationMap);
		}
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<SetStewEffectFunction> {
		public void serialize(JsonObject jsonObject, SetStewEffectFunction setStewEffectFunction, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, setStewEffectFunction, jsonSerializationContext);
			if (!setStewEffectFunction.effectDurationMap.isEmpty()) {
				JsonArray jsonArray = new JsonArray();

				for (MobEffect mobEffect : setStewEffectFunction.effectDurationMap.keySet()) {
					JsonObject jsonObject2 = new JsonObject();
					ResourceLocation resourceLocation = Registry.MOB_EFFECT.getKey(mobEffect);
					if (resourceLocation == null) {
						throw new IllegalArgumentException("Don't know how to serialize mob effect " + mobEffect);
					}

					jsonObject2.add("type", new JsonPrimitive(resourceLocation.toString()));
					jsonObject2.add("duration", jsonSerializationContext.serialize(setStewEffectFunction.effectDurationMap.get(mobEffect)));
					jsonArray.add(jsonObject2);
				}

				jsonObject.add("effects", jsonArray);
			}
		}

		public SetStewEffectFunction deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			Map<MobEffect, NumberProvider> map = Maps.<MobEffect, NumberProvider>newHashMap();
			if (jsonObject.has("effects")) {
				for (JsonElement jsonElement : GsonHelper.getAsJsonArray(jsonObject, "effects")) {
					String string = GsonHelper.getAsString(jsonElement.getAsJsonObject(), "type");
					MobEffect mobEffect = (MobEffect)Registry.MOB_EFFECT
						.getOptional(new ResourceLocation(string))
						.orElseThrow(() -> new JsonSyntaxException("Unknown mob effect '" + string + "'"));
					NumberProvider numberProvider = GsonHelper.getAsObject(jsonElement.getAsJsonObject(), "duration", jsonDeserializationContext, NumberProvider.class);
					map.put(mobEffect, numberProvider);
				}
			}

			return new SetStewEffectFunction(lootItemConditions, map);
		}
	}
}

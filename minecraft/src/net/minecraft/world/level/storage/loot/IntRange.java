package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class IntRange {
	@Nullable
	final NumberProvider min;
	@Nullable
	final NumberProvider max;
	private final IntRange.IntLimiter limiter;
	private final IntRange.IntChecker predicate;

	public Set<LootContextParam<?>> getReferencedContextParams() {
		Builder<LootContextParam<?>> builder = ImmutableSet.builder();
		if (this.min != null) {
			builder.addAll(this.min.getReferencedContextParams());
		}

		if (this.max != null) {
			builder.addAll(this.max.getReferencedContextParams());
		}

		return builder.build();
	}

	IntRange(@Nullable NumberProvider numberProvider, @Nullable NumberProvider numberProvider2) {
		this.min = numberProvider;
		this.max = numberProvider2;
		if (numberProvider == null) {
			if (numberProvider2 == null) {
				this.limiter = (lootContext, i) -> i;
				this.predicate = (lootContext, i) -> true;
			} else {
				this.limiter = (lootContext, i) -> Math.min(numberProvider2.getInt(lootContext), i);
				this.predicate = (lootContext, i) -> i <= numberProvider2.getInt(lootContext);
			}
		} else if (numberProvider2 == null) {
			this.limiter = (lootContext, i) -> Math.max(numberProvider.getInt(lootContext), i);
			this.predicate = (lootContext, i) -> i >= numberProvider.getInt(lootContext);
		} else {
			this.limiter = (lootContext, i) -> Mth.clamp(i, numberProvider.getInt(lootContext), numberProvider2.getInt(lootContext));
			this.predicate = (lootContext, i) -> i >= numberProvider.getInt(lootContext) && i <= numberProvider2.getInt(lootContext);
		}
	}

	public static IntRange exact(int i) {
		ConstantValue constantValue = ConstantValue.exactly((float)i);
		return new IntRange(constantValue, constantValue);
	}

	public static IntRange range(int i, int j) {
		return new IntRange(ConstantValue.exactly((float)i), ConstantValue.exactly((float)j));
	}

	public static IntRange lowerBound(int i) {
		return new IntRange(ConstantValue.exactly((float)i), null);
	}

	public static IntRange upperBound(int i) {
		return new IntRange(null, ConstantValue.exactly((float)i));
	}

	public int clamp(LootContext lootContext, int i) {
		return this.limiter.apply(lootContext, i);
	}

	public boolean test(LootContext lootContext, int i) {
		return this.predicate.test(lootContext, i);
	}

	@FunctionalInterface
	interface IntChecker {
		boolean test(LootContext lootContext, int i);
	}

	@FunctionalInterface
	interface IntLimiter {
		int apply(LootContext lootContext, int i);
	}

	public static class Serializer implements JsonDeserializer<IntRange>, JsonSerializer<IntRange> {
		public IntRange deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
			if (jsonElement.isJsonPrimitive()) {
				return IntRange.exact(jsonElement.getAsInt());
			} else {
				JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "value");
				NumberProvider numberProvider = jsonObject.has("min") ? GsonHelper.getAsObject(jsonObject, "min", jsonDeserializationContext, NumberProvider.class) : null;
				NumberProvider numberProvider2 = jsonObject.has("max") ? GsonHelper.getAsObject(jsonObject, "max", jsonDeserializationContext, NumberProvider.class) : null;
				return new IntRange(numberProvider, numberProvider2);
			}
		}

		public JsonElement serialize(IntRange intRange, Type type, JsonSerializationContext jsonSerializationContext) {
			JsonObject jsonObject = new JsonObject();
			if (Objects.equals(intRange.max, intRange.min)) {
				return jsonSerializationContext.serialize(intRange.min);
			} else {
				if (intRange.max != null) {
					jsonObject.add("max", jsonSerializationContext.serialize(intRange.max));
				}

				if (intRange.min != null) {
					jsonObject.add("min", jsonSerializationContext.serialize(intRange.min));
				}

				return jsonObject;
			}
		}
	}
}

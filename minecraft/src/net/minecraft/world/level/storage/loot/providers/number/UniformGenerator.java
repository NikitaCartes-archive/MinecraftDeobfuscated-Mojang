package net.minecraft.world.level.storage.loot.providers.number;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class UniformGenerator implements NumberProvider {
	final NumberProvider min;
	final NumberProvider max;

	UniformGenerator(NumberProvider numberProvider, NumberProvider numberProvider2) {
		this.min = numberProvider;
		this.max = numberProvider2;
	}

	@Override
	public LootNumberProviderType getType() {
		return NumberProviders.UNIFORM;
	}

	public static UniformGenerator between(float f, float g) {
		return new UniformGenerator(ConstantValue.exactly(f), ConstantValue.exactly(g));
	}

	@Override
	public int getInt(LootContext lootContext) {
		return Mth.nextInt(lootContext.getRandom(), this.min.getInt(lootContext), this.max.getInt(lootContext));
	}

	@Override
	public float getFloat(LootContext lootContext) {
		return Mth.nextFloat(lootContext.getRandom(), this.min.getFloat(lootContext), this.max.getFloat(lootContext));
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return Sets.<LootContextParam<?>>union(this.min.getReferencedContextParams(), this.max.getReferencedContextParams());
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<UniformGenerator> {
		public UniformGenerator deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			NumberProvider numberProvider = GsonHelper.getAsObject(jsonObject, "min", jsonDeserializationContext, NumberProvider.class);
			NumberProvider numberProvider2 = GsonHelper.getAsObject(jsonObject, "max", jsonDeserializationContext, NumberProvider.class);
			return new UniformGenerator(numberProvider, numberProvider2);
		}

		public void serialize(JsonObject jsonObject, UniformGenerator uniformGenerator, JsonSerializationContext jsonSerializationContext) {
			jsonObject.add("min", jsonSerializationContext.serialize(uniformGenerator.min));
			jsonObject.add("max", jsonSerializationContext.serialize(uniformGenerator.max));
		}
	}
}

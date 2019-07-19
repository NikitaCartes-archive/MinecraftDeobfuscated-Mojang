package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class LocationCheck implements LootItemCondition {
	private final LocationPredicate predicate;

	private LocationCheck(LocationPredicate locationPredicate) {
		this.predicate = locationPredicate;
	}

	public boolean test(LootContext lootContext) {
		BlockPos blockPos = lootContext.getParamOrNull(LootContextParams.BLOCK_POS);
		return blockPos != null && this.predicate.matches(lootContext.getLevel(), (float)blockPos.getX(), (float)blockPos.getY(), (float)blockPos.getZ());
	}

	public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder builder) {
		return () -> new LocationCheck(builder.build());
	}

	public static class Serializer extends LootItemCondition.Serializer<LocationCheck> {
		public Serializer() {
			super(new ResourceLocation("location_check"), LocationCheck.class);
		}

		public void serialize(JsonObject jsonObject, LocationCheck locationCheck, JsonSerializationContext jsonSerializationContext) {
			jsonObject.add("predicate", locationCheck.predicate.serializeToJson());
		}

		public LocationCheck deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject.get("predicate"));
			return new LocationCheck(locationPredicate);
		}
	}
}

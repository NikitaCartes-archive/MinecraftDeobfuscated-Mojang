package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class LocationCheck implements LootItemCondition {
	final LocationPredicate predicate;
	final BlockPos offset;

	LocationCheck(LocationPredicate locationPredicate, BlockPos blockPos) {
		this.predicate = locationPredicate;
		this.offset = blockPos;
	}

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.LOCATION_CHECK;
	}

	public boolean test(LootContext lootContext) {
		Vec3 vec3 = lootContext.getParamOrNull(LootContextParams.ORIGIN);
		return vec3 != null
			&& this.predicate
				.matches(lootContext.getLevel(), vec3.x() + (double)this.offset.getX(), vec3.y() + (double)this.offset.getY(), vec3.z() + (double)this.offset.getZ());
	}

	public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder builder) {
		return () -> new LocationCheck(builder.build(), BlockPos.ZERO);
	}

	public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder builder, BlockPos blockPos) {
		return () -> new LocationCheck(builder.build(), blockPos);
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LocationCheck> {
		public void serialize(JsonObject jsonObject, LocationCheck locationCheck, JsonSerializationContext jsonSerializationContext) {
			jsonObject.add("predicate", locationCheck.predicate.serializeToJson());
			if (locationCheck.offset.getX() != 0) {
				jsonObject.addProperty("offsetX", locationCheck.offset.getX());
			}

			if (locationCheck.offset.getY() != 0) {
				jsonObject.addProperty("offsetY", locationCheck.offset.getY());
			}

			if (locationCheck.offset.getZ() != 0) {
				jsonObject.addProperty("offsetZ", locationCheck.offset.getZ());
			}
		}

		public LocationCheck deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject.get("predicate"));
			int i = GsonHelper.getAsInt(jsonObject, "offsetX", 0);
			int j = GsonHelper.getAsInt(jsonObject, "offsetY", 0);
			int k = GsonHelper.getAsInt(jsonObject, "offsetZ", 0);
			return new LocationCheck(locationPredicate, new BlockPos(i, j, k));
		}
	}
}

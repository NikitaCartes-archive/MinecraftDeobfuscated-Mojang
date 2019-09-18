package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.dimension.DimensionType;

public class ChangeDimensionTrigger extends SimpleCriterionTrigger<ChangeDimensionTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("changed_dimension");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public ChangeDimensionTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		DimensionType dimensionType = jsonObject.has("from") ? DimensionType.getByName(new ResourceLocation(GsonHelper.getAsString(jsonObject, "from"))) : null;
		DimensionType dimensionType2 = jsonObject.has("to") ? DimensionType.getByName(new ResourceLocation(GsonHelper.getAsString(jsonObject, "to"))) : null;
		return new ChangeDimensionTrigger.TriggerInstance(dimensionType, dimensionType2);
	}

	public void trigger(ServerPlayer serverPlayer, DimensionType dimensionType, DimensionType dimensionType2) {
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(dimensionType, dimensionType2));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		@Nullable
		private final DimensionType from;
		@Nullable
		private final DimensionType to;

		public TriggerInstance(@Nullable DimensionType dimensionType, @Nullable DimensionType dimensionType2) {
			super(ChangeDimensionTrigger.ID);
			this.from = dimensionType;
			this.to = dimensionType2;
		}

		public static ChangeDimensionTrigger.TriggerInstance changedDimensionTo(DimensionType dimensionType) {
			return new ChangeDimensionTrigger.TriggerInstance(null, dimensionType);
		}

		public boolean matches(DimensionType dimensionType, DimensionType dimensionType2) {
			return this.from != null && this.from != dimensionType ? false : this.to == null || this.to == dimensionType2;
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			if (this.from != null) {
				jsonObject.addProperty("from", DimensionType.getName(this.from).toString());
			}

			if (this.to != null) {
				jsonObject.addProperty("to", DimensionType.getName(this.to).toString());
			}

			return jsonObject;
		}
	}
}

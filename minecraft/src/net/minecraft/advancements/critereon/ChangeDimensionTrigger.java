package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
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

	public ChangeDimensionTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		ResourceKey<DimensionType> resourceKey = jsonObject.has("from")
			? ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation(GsonHelper.getAsString(jsonObject, "from")))
			: null;
		ResourceKey<DimensionType> resourceKey2 = jsonObject.has("to")
			? ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation(GsonHelper.getAsString(jsonObject, "to")))
			: null;
		return new ChangeDimensionTrigger.TriggerInstance(composite, resourceKey, resourceKey2);
	}

	public void trigger(ServerPlayer serverPlayer, ResourceKey<DimensionType> resourceKey, ResourceKey<DimensionType> resourceKey2) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(resourceKey, resourceKey2));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		@Nullable
		private final ResourceKey<DimensionType> from;
		@Nullable
		private final ResourceKey<DimensionType> to;

		public TriggerInstance(
			EntityPredicate.Composite composite, @Nullable ResourceKey<DimensionType> resourceKey, @Nullable ResourceKey<DimensionType> resourceKey2
		) {
			super(ChangeDimensionTrigger.ID, composite);
			this.from = resourceKey;
			this.to = resourceKey2;
		}

		public static ChangeDimensionTrigger.TriggerInstance changedDimensionTo(ResourceKey<DimensionType> resourceKey) {
			return new ChangeDimensionTrigger.TriggerInstance(EntityPredicate.Composite.ANY, null, resourceKey);
		}

		public boolean matches(ResourceKey<DimensionType> resourceKey, ResourceKey<DimensionType> resourceKey2) {
			return this.from != null && this.from != resourceKey ? false : this.to == null || this.to == resourceKey2;
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			if (this.from != null) {
				jsonObject.addProperty("from", this.from.location().toString());
			}

			if (this.to != null) {
				jsonObject.addProperty("to", this.to.location().toString());
			}

			return jsonObject;
		}
	}
}

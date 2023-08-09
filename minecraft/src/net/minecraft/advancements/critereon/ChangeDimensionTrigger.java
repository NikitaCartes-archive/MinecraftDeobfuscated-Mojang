package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;

public class ChangeDimensionTrigger extends SimpleCriterionTrigger<ChangeDimensionTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("changed_dimension");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public ChangeDimensionTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		ResourceKey<Level> resourceKey = jsonObject.has("from")
			? ResourceKey.create(Registries.DIMENSION, new ResourceLocation(GsonHelper.getAsString(jsonObject, "from")))
			: null;
		ResourceKey<Level> resourceKey2 = jsonObject.has("to")
			? ResourceKey.create(Registries.DIMENSION, new ResourceLocation(GsonHelper.getAsString(jsonObject, "to")))
			: null;
		return new ChangeDimensionTrigger.TriggerInstance(optional, resourceKey, resourceKey2);
	}

	public void trigger(ServerPlayer serverPlayer, ResourceKey<Level> resourceKey, ResourceKey<Level> resourceKey2) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(resourceKey, resourceKey2));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		@Nullable
		private final ResourceKey<Level> from;
		@Nullable
		private final ResourceKey<Level> to;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, @Nullable ResourceKey<Level> resourceKey, @Nullable ResourceKey<Level> resourceKey2) {
			super(ChangeDimensionTrigger.ID, optional);
			this.from = resourceKey;
			this.to = resourceKey2;
		}

		public static ChangeDimensionTrigger.TriggerInstance changedDimension() {
			return new ChangeDimensionTrigger.TriggerInstance(Optional.empty(), null, null);
		}

		public static ChangeDimensionTrigger.TriggerInstance changedDimension(ResourceKey<Level> resourceKey, ResourceKey<Level> resourceKey2) {
			return new ChangeDimensionTrigger.TriggerInstance(Optional.empty(), resourceKey, resourceKey2);
		}

		public static ChangeDimensionTrigger.TriggerInstance changedDimensionTo(ResourceKey<Level> resourceKey) {
			return new ChangeDimensionTrigger.TriggerInstance(Optional.empty(), null, resourceKey);
		}

		public static ChangeDimensionTrigger.TriggerInstance changedDimensionFrom(ResourceKey<Level> resourceKey) {
			return new ChangeDimensionTrigger.TriggerInstance(Optional.empty(), resourceKey, null);
		}

		public boolean matches(ResourceKey<Level> resourceKey, ResourceKey<Level> resourceKey2) {
			return this.from != null && this.from != resourceKey ? false : this.to == null || this.to == resourceKey2;
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
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

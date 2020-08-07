package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class FilledBucketTrigger extends SimpleCriterionTrigger<FilledBucketTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("filled_bucket");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public FilledBucketTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		return new FilledBucketTrigger.TriggerInstance(composite, itemPredicate);
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ItemPredicate item;

		public TriggerInstance(EntityPredicate.Composite composite, ItemPredicate itemPredicate) {
			super(FilledBucketTrigger.ID, composite);
			this.item = itemPredicate;
		}

		public static FilledBucketTrigger.TriggerInstance filledBucket(ItemPredicate itemPredicate) {
			return new FilledBucketTrigger.TriggerInstance(EntityPredicate.Composite.ANY, itemPredicate);
		}

		public boolean matches(ItemStack itemStack) {
			return this.item.matches(itemStack);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("item", this.item.serializeToJson());
			return jsonObject;
		}
	}
}

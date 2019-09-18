package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
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

	public FilledBucketTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		return new FilledBucketTrigger.TriggerInstance(itemPredicate);
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack) {
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(itemStack));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ItemPredicate item;

		public TriggerInstance(ItemPredicate itemPredicate) {
			super(FilledBucketTrigger.ID);
			this.item = itemPredicate;
		}

		public static FilledBucketTrigger.TriggerInstance filledBucket(ItemPredicate itemPredicate) {
			return new FilledBucketTrigger.TriggerInstance(itemPredicate);
		}

		public boolean matches(ItemStack itemStack) {
			return this.item.matches(itemStack);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("item", this.item.serializeToJson());
			return jsonObject;
		}
	}
}

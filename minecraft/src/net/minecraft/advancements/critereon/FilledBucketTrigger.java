package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class FilledBucketTrigger extends SimpleCriterionTrigger<FilledBucketTrigger.TriggerInstance> {
	public FilledBucketTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		Optional<ItemPredicate> optional2 = ItemPredicate.fromJson(jsonObject.get("item"));
		return new FilledBucketTrigger.TriggerInstance(optional, optional2);
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Optional<ItemPredicate> item;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, Optional<ItemPredicate> optional2) {
			super(optional);
			this.item = optional2;
		}

		public static Criterion<FilledBucketTrigger.TriggerInstance> filledBucket(ItemPredicate.Builder builder) {
			return CriteriaTriggers.FILLED_BUCKET.createCriterion(new FilledBucketTrigger.TriggerInstance(Optional.empty(), Optional.of(builder.build())));
		}

		public boolean matches(ItemStack itemStack) {
			return !this.item.isPresent() || ((ItemPredicate)this.item.get()).matches(itemStack);
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			this.item.ifPresent(itemPredicate -> jsonObject.add("item", itemPredicate.serializeToJson()));
			return jsonObject;
		}
	}
}

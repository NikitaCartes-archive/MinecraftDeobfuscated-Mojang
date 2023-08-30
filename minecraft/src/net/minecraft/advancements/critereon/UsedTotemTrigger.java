package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class UsedTotemTrigger extends SimpleCriterionTrigger<UsedTotemTrigger.TriggerInstance> {
	public UsedTotemTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		Optional<ItemPredicate> optional2 = ItemPredicate.fromJson(jsonObject.get("item"));
		return new UsedTotemTrigger.TriggerInstance(optional, optional2);
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

		public static Criterion<UsedTotemTrigger.TriggerInstance> usedTotem(ItemPredicate itemPredicate) {
			return CriteriaTriggers.USED_TOTEM.createCriterion(new UsedTotemTrigger.TriggerInstance(Optional.empty(), Optional.of(itemPredicate)));
		}

		public static Criterion<UsedTotemTrigger.TriggerInstance> usedTotem(ItemLike itemLike) {
			return CriteriaTriggers.USED_TOTEM
				.createCriterion(new UsedTotemTrigger.TriggerInstance(Optional.empty(), Optional.of(ItemPredicate.Builder.item().of(itemLike).build())));
		}

		public boolean matches(ItemStack itemStack) {
			return this.item.isEmpty() || ((ItemPredicate)this.item.get()).matches(itemStack);
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			this.item.ifPresent(itemPredicate -> jsonObject.add("item", itemPredicate.serializeToJson()));
			return jsonObject;
		}
	}
}

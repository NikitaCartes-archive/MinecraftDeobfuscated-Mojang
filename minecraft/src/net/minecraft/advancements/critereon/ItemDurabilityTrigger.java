package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ItemDurabilityTrigger extends SimpleCriterionTrigger<ItemDurabilityTrigger.TriggerInstance> {
	public ItemDurabilityTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		Optional<ItemPredicate> optional2 = ItemPredicate.fromJson(jsonObject.get("item"));
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("durability"));
		MinMaxBounds.Ints ints2 = MinMaxBounds.Ints.fromJson(jsonObject.get("delta"));
		return new ItemDurabilityTrigger.TriggerInstance(optional, optional2, ints, ints2);
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack, int i) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack, i));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Optional<ItemPredicate> item;
		private final MinMaxBounds.Ints durability;
		private final MinMaxBounds.Ints delta;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, Optional<ItemPredicate> optional2, MinMaxBounds.Ints ints, MinMaxBounds.Ints ints2) {
			super(optional);
			this.item = optional2;
			this.durability = ints;
			this.delta = ints2;
		}

		public static Criterion<ItemDurabilityTrigger.TriggerInstance> changedDurability(Optional<ItemPredicate> optional, MinMaxBounds.Ints ints) {
			return changedDurability(Optional.empty(), optional, ints);
		}

		public static Criterion<ItemDurabilityTrigger.TriggerInstance> changedDurability(
			Optional<ContextAwarePredicate> optional, Optional<ItemPredicate> optional2, MinMaxBounds.Ints ints
		) {
			return CriteriaTriggers.ITEM_DURABILITY_CHANGED.createCriterion(new ItemDurabilityTrigger.TriggerInstance(optional, optional2, ints, MinMaxBounds.Ints.ANY));
		}

		public boolean matches(ItemStack itemStack, int i) {
			if (this.item.isPresent() && !((ItemPredicate)this.item.get()).matches(itemStack)) {
				return false;
			} else {
				return !this.durability.matches(itemStack.getMaxDamage() - i) ? false : this.delta.matches(itemStack.getDamageValue() - i);
			}
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			this.item.ifPresent(itemPredicate -> jsonObject.add("item", itemPredicate.serializeToJson()));
			jsonObject.add("durability", this.durability.serializeToJson());
			jsonObject.add("delta", this.delta.serializeToJson());
			return jsonObject;
		}
	}
}

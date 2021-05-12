package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ItemDurabilityTrigger extends SimpleCriterionTrigger<ItemDurabilityTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("item_durability_changed");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public ItemDurabilityTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("durability"));
		MinMaxBounds.Ints ints2 = MinMaxBounds.Ints.fromJson(jsonObject.get("delta"));
		return new ItemDurabilityTrigger.TriggerInstance(composite, itemPredicate, ints, ints2);
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack, int i) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack, i));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ItemPredicate item;
		private final MinMaxBounds.Ints durability;
		private final MinMaxBounds.Ints delta;

		public TriggerInstance(EntityPredicate.Composite composite, ItemPredicate itemPredicate, MinMaxBounds.Ints ints, MinMaxBounds.Ints ints2) {
			super(ItemDurabilityTrigger.ID, composite);
			this.item = itemPredicate;
			this.durability = ints;
			this.delta = ints2;
		}

		public static ItemDurabilityTrigger.TriggerInstance changedDurability(ItemPredicate itemPredicate, MinMaxBounds.Ints ints) {
			return changedDurability(EntityPredicate.Composite.ANY, itemPredicate, ints);
		}

		public static ItemDurabilityTrigger.TriggerInstance changedDurability(
			EntityPredicate.Composite composite, ItemPredicate itemPredicate, MinMaxBounds.Ints ints
		) {
			return new ItemDurabilityTrigger.TriggerInstance(composite, itemPredicate, ints, MinMaxBounds.Ints.ANY);
		}

		public boolean matches(ItemStack itemStack, int i) {
			if (!this.item.matches(itemStack)) {
				return false;
			} else {
				return !this.durability.matches(itemStack.getMaxDamage() - i) ? false : this.delta.matches(itemStack.getDamageValue() - i);
			}
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("item", this.item.serializeToJson());
			jsonObject.add("durability", this.durability.serializeToJson());
			jsonObject.add("delta", this.delta.serializeToJson());
			return jsonObject;
		}
	}
}

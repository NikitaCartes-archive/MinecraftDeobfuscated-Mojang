package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ConsumeItemTrigger extends SimpleCriterionTrigger<ConsumeItemTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("consume_item");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public ConsumeItemTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, ContextAwarePredicate contextAwarePredicate, DeserializationContext deserializationContext
	) {
		return new ConsumeItemTrigger.TriggerInstance(contextAwarePredicate, ItemPredicate.fromJson(jsonObject.get("item")));
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ItemPredicate item;

		public TriggerInstance(ContextAwarePredicate contextAwarePredicate, ItemPredicate itemPredicate) {
			super(ConsumeItemTrigger.ID, contextAwarePredicate);
			this.item = itemPredicate;
		}

		public static ConsumeItemTrigger.TriggerInstance usedItem() {
			return new ConsumeItemTrigger.TriggerInstance(ContextAwarePredicate.ANY, ItemPredicate.ANY);
		}

		public static ConsumeItemTrigger.TriggerInstance usedItem(ItemPredicate itemPredicate) {
			return new ConsumeItemTrigger.TriggerInstance(ContextAwarePredicate.ANY, itemPredicate);
		}

		public static ConsumeItemTrigger.TriggerInstance usedItem(ItemLike itemLike) {
			return new ConsumeItemTrigger.TriggerInstance(
				ContextAwarePredicate.ANY,
				new ItemPredicate(
					null,
					ImmutableSet.of(itemLike.asItem()),
					MinMaxBounds.Ints.ANY,
					MinMaxBounds.Ints.ANY,
					EnchantmentPredicate.NONE,
					EnchantmentPredicate.NONE,
					null,
					NbtPredicate.ANY
				)
			);
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

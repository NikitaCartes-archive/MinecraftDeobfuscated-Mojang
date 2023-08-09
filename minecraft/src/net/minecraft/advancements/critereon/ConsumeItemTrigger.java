package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
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
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		return new ConsumeItemTrigger.TriggerInstance(optional, ItemPredicate.fromJson(jsonObject.get("item")));
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Optional<ItemPredicate> item;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, Optional<ItemPredicate> optional2) {
			super(ConsumeItemTrigger.ID, optional);
			this.item = optional2;
		}

		public static ConsumeItemTrigger.TriggerInstance usedItem() {
			return new ConsumeItemTrigger.TriggerInstance(Optional.empty(), Optional.empty());
		}

		public static ConsumeItemTrigger.TriggerInstance usedItem(ItemPredicate itemPredicate) {
			return new ConsumeItemTrigger.TriggerInstance(Optional.empty(), Optional.of(itemPredicate));
		}

		public static ConsumeItemTrigger.TriggerInstance usedItem(ItemLike itemLike) {
			return new ConsumeItemTrigger.TriggerInstance(Optional.empty(), ItemPredicate.Builder.item().of(itemLike.asItem()).build());
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

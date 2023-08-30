package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ShotCrossbowTrigger extends SimpleCriterionTrigger<ShotCrossbowTrigger.TriggerInstance> {
	public ShotCrossbowTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		Optional<ItemPredicate> optional2 = ItemPredicate.fromJson(jsonObject.get("item"));
		return new ShotCrossbowTrigger.TriggerInstance(optional, optional2);
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

		public static Criterion<ShotCrossbowTrigger.TriggerInstance> shotCrossbow(Optional<ItemPredicate> optional) {
			return CriteriaTriggers.SHOT_CROSSBOW.createCriterion(new ShotCrossbowTrigger.TriggerInstance(Optional.empty(), optional));
		}

		public static Criterion<ShotCrossbowTrigger.TriggerInstance> shotCrossbow(ItemLike itemLike) {
			return CriteriaTriggers.SHOT_CROSSBOW
				.createCriterion(new ShotCrossbowTrigger.TriggerInstance(Optional.empty(), Optional.of(ItemPredicate.Builder.item().of(itemLike).build())));
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

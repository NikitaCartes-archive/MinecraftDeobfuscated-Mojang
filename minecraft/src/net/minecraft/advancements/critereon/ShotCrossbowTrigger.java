package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ShotCrossbowTrigger extends SimpleCriterionTrigger<ShotCrossbowTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("shot_crossbow");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public ShotCrossbowTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		return new ShotCrossbowTrigger.TriggerInstance(composite, itemPredicate);
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ItemPredicate item;

		public TriggerInstance(EntityPredicate.Composite composite, ItemPredicate itemPredicate) {
			super(ShotCrossbowTrigger.ID, composite);
			this.item = itemPredicate;
		}

		public static ShotCrossbowTrigger.TriggerInstance shotCrossbow(ItemPredicate itemPredicate) {
			return new ShotCrossbowTrigger.TriggerInstance(EntityPredicate.Composite.ANY, itemPredicate);
		}

		public static ShotCrossbowTrigger.TriggerInstance shotCrossbow(ItemLike itemLike) {
			return new ShotCrossbowTrigger.TriggerInstance(EntityPredicate.Composite.ANY, ItemPredicate.Builder.item().of(itemLike).build());
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

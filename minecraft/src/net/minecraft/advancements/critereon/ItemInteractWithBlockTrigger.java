package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ItemInteractWithBlockTrigger extends SimpleCriterionTrigger<ItemInteractWithBlockTrigger.TriggerInstance> {
	final ResourceLocation id;

	public ItemInteractWithBlockTrigger(ResourceLocation resourceLocation) {
		this.id = resourceLocation;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	public ItemInteractWithBlockTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject.get("location"));
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		return new ItemInteractWithBlockTrigger.TriggerInstance(this.id, composite, locationPredicate, itemPredicate);
	}

	public void trigger(ServerPlayer serverPlayer, BlockPos blockPos, ItemStack itemStack) {
		BlockState blockState = serverPlayer.getLevel().getBlockState(blockPos);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(blockState, serverPlayer.getLevel(), blockPos, itemStack));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final LocationPredicate location;
		private final ItemPredicate item;

		public TriggerInstance(
			ResourceLocation resourceLocation, EntityPredicate.Composite composite, LocationPredicate locationPredicate, ItemPredicate itemPredicate
		) {
			super(resourceLocation, composite);
			this.location = locationPredicate;
			this.item = itemPredicate;
		}

		public static ItemInteractWithBlockTrigger.TriggerInstance itemUsedOnBlock(LocationPredicate.Builder builder, ItemPredicate.Builder builder2) {
			return new ItemInteractWithBlockTrigger.TriggerInstance(
				CriteriaTriggers.ITEM_USED_ON_BLOCK.id, EntityPredicate.Composite.ANY, builder.build(), builder2.build()
			);
		}

		public static ItemInteractWithBlockTrigger.TriggerInstance allayDropItemOnBlock(LocationPredicate.Builder builder, ItemPredicate.Builder builder2) {
			return new ItemInteractWithBlockTrigger.TriggerInstance(
				CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK.id, EntityPredicate.Composite.ANY, builder.build(), builder2.build()
			);
		}

		public boolean matches(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
			return !this.location.matches(serverLevel, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5)
				? false
				: this.item.matches(itemStack);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("location", this.location.serializeToJson());
			jsonObject.add("item", this.item.serializeToJson());
			return jsonObject;
		}
	}
}

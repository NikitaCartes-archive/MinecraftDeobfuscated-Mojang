package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ItemUsedOnBlockTrigger extends SimpleCriterionTrigger<ItemUsedOnBlockTrigger.TriggerInstance> {
	private final ResourceLocation id;

	public ItemUsedOnBlockTrigger(ResourceLocation resourceLocation) {
		this.id = resourceLocation;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	public ItemUsedOnBlockTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		BlockPredicate blockPredicate = BlockPredicate.fromJson(jsonObject.get("block"));
		StatePropertiesPredicate statePropertiesPredicate = StatePropertiesPredicate.fromJson(jsonObject.get("state"));
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		return new ItemUsedOnBlockTrigger.TriggerInstance(this.id, composite, blockPredicate, statePropertiesPredicate, itemPredicate);
	}

	public void trigger(ServerPlayer serverPlayer, BlockPos blockPos, ItemStack itemStack) {
		BlockState blockState = serverPlayer.getLevel().getBlockState(blockPos);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(blockState, serverPlayer.getLevel(), blockPos, itemStack));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final BlockPredicate block;
		private final StatePropertiesPredicate state;
		private final ItemPredicate item;

		public TriggerInstance(
			ResourceLocation resourceLocation,
			EntityPredicate.Composite composite,
			BlockPredicate blockPredicate,
			StatePropertiesPredicate statePropertiesPredicate,
			ItemPredicate itemPredicate
		) {
			super(resourceLocation, composite);
			this.block = blockPredicate;
			this.state = statePropertiesPredicate;
			this.item = itemPredicate;
		}

		public static ItemUsedOnBlockTrigger.TriggerInstance safelyHarvestedHoney(BlockPredicate.Builder builder, ItemPredicate.Builder builder2) {
			return new ItemUsedOnBlockTrigger.TriggerInstance(
				CriteriaTriggers.SAFELY_HARVEST_HONEY.id, EntityPredicate.Composite.ANY, builder.build(), StatePropertiesPredicate.ANY, builder2.build()
			);
		}

		public boolean matches(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
			if (!this.block.matches(serverLevel, blockPos)) {
				return false;
			} else {
				return !this.state.matches(blockState) ? false : this.item.matches(itemStack);
			}
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("block", this.block.serializeToJson());
			jsonObject.add("state", this.state.serializeToJson());
			jsonObject.add("item", this.item.serializeToJson());
			return jsonObject;
		}
	}
}

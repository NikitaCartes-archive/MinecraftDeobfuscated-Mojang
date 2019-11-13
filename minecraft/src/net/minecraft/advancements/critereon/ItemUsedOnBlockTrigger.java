package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
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

	public ItemUsedOnBlockTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		BlockPredicate blockPredicate = BlockPredicate.fromJson(jsonObject.get("block"));
		StatePropertiesPredicate statePropertiesPredicate = StatePropertiesPredicate.fromJson(jsonObject.get("state"));
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		return new ItemUsedOnBlockTrigger.TriggerInstance(this.id, blockPredicate, statePropertiesPredicate, itemPredicate);
	}

	public void trigger(ServerPlayer serverPlayer, BlockPos blockPos, ItemStack itemStack) {
		BlockState blockState = serverPlayer.getLevel().getBlockState(blockPos);
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(blockState, serverPlayer.getLevel(), blockPos, itemStack));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final BlockPredicate block;
		private final StatePropertiesPredicate state;
		private final ItemPredicate item;

		public TriggerInstance(
			ResourceLocation resourceLocation, BlockPredicate blockPredicate, StatePropertiesPredicate statePropertiesPredicate, ItemPredicate itemPredicate
		) {
			super(resourceLocation);
			this.block = blockPredicate;
			this.state = statePropertiesPredicate;
			this.item = itemPredicate;
		}

		public static ItemUsedOnBlockTrigger.TriggerInstance safelyHarvestedHoney(BlockPredicate.Builder builder, ItemPredicate.Builder builder2) {
			return new ItemUsedOnBlockTrigger.TriggerInstance(CriteriaTriggers.SAFELY_HARVEST_HONEY.id, builder.build(), StatePropertiesPredicate.ANY, builder2.build());
		}

		public boolean matches(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
			if (!this.block.matches(serverLevel, blockPos)) {
				return false;
			} else {
				return !this.state.matches(blockState) ? false : this.item.matches(itemStack);
			}
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("block", this.block.serializeToJson());
			jsonObject.add("state", this.state.serializeToJson());
			jsonObject.add("item", this.item.serializeToJson());
			return jsonObject;
		}
	}
}

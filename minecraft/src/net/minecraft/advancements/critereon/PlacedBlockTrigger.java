package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class PlacedBlockTrigger extends SimpleCriterionTrigger<PlacedBlockTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("placed_block");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public PlacedBlockTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		Block block = deserializeBlock(jsonObject);
		StatePropertiesPredicate statePropertiesPredicate = StatePropertiesPredicate.fromJson(jsonObject.get("state"));
		if (block != null) {
			statePropertiesPredicate.checkState(block.getStateDefinition(), string -> {
				throw new JsonSyntaxException("Block " + block + " has no property " + string + ":");
			});
		}

		LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject.get("location"));
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		return new PlacedBlockTrigger.TriggerInstance(composite, block, statePropertiesPredicate, locationPredicate, itemPredicate);
	}

	@Nullable
	private static Block deserializeBlock(JsonObject jsonObject) {
		if (jsonObject.has("block")) {
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "block"));
			return (Block)BuiltInRegistries.BLOCK
				.getOptional(resourceLocation)
				.orElseThrow(() -> new JsonSyntaxException("Unknown block type '" + resourceLocation + "'"));
		} else {
			return null;
		}
	}

	public void trigger(ServerPlayer serverPlayer, BlockPos blockPos, ItemStack itemStack) {
		BlockState blockState = serverPlayer.getLevel().getBlockState(blockPos);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(blockState, blockPos, serverPlayer.getLevel(), itemStack));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		@Nullable
		private final Block block;
		private final StatePropertiesPredicate state;
		private final LocationPredicate location;
		private final ItemPredicate item;

		public TriggerInstance(
			EntityPredicate.Composite composite,
			@Nullable Block block,
			StatePropertiesPredicate statePropertiesPredicate,
			LocationPredicate locationPredicate,
			ItemPredicate itemPredicate
		) {
			super(PlacedBlockTrigger.ID, composite);
			this.block = block;
			this.state = statePropertiesPredicate;
			this.location = locationPredicate;
			this.item = itemPredicate;
		}

		public static PlacedBlockTrigger.TriggerInstance placedBlock(Block block) {
			return new PlacedBlockTrigger.TriggerInstance(EntityPredicate.Composite.ANY, block, StatePropertiesPredicate.ANY, LocationPredicate.ANY, ItemPredicate.ANY);
		}

		public boolean matches(BlockState blockState, BlockPos blockPos, ServerLevel serverLevel, ItemStack itemStack) {
			if (this.block != null && !blockState.is(this.block)) {
				return false;
			} else if (!this.state.matches(blockState)) {
				return false;
			} else {
				return !this.location.matches(serverLevel, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ())
					? false
					: this.item.matches(itemStack);
			}
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			if (this.block != null) {
				jsonObject.addProperty("block", BuiltInRegistries.BLOCK.getKey(this.block).toString());
			}

			jsonObject.add("state", this.state.serializeToJson());
			jsonObject.add("location", this.location.serializeToJson());
			jsonObject.add("item", this.item.serializeToJson());
			return jsonObject;
		}
	}
}

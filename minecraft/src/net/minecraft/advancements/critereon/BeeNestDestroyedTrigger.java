package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BeeNestDestroyedTrigger extends SimpleCriterionTrigger<BeeNestDestroyedTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("bee_nest_destroyed");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public BeeNestDestroyedTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		Block block = deserializeBlock(jsonObject);
		Optional<ItemPredicate> optional2 = ItemPredicate.fromJson(jsonObject.get("item"));
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("num_bees_inside"));
		return new BeeNestDestroyedTrigger.TriggerInstance(optional, block, optional2, ints);
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

	public void trigger(ServerPlayer serverPlayer, BlockState blockState, ItemStack itemStack, int i) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(blockState, itemStack, i));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		@Nullable
		private final Block block;
		private final Optional<ItemPredicate> item;
		private final MinMaxBounds.Ints numBees;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, @Nullable Block block, Optional<ItemPredicate> optional2, MinMaxBounds.Ints ints) {
			super(BeeNestDestroyedTrigger.ID, optional);
			this.block = block;
			this.item = optional2;
			this.numBees = ints;
		}

		public static BeeNestDestroyedTrigger.TriggerInstance destroyedBeeNest(Block block, ItemPredicate.Builder builder, MinMaxBounds.Ints ints) {
			return new BeeNestDestroyedTrigger.TriggerInstance(Optional.empty(), block, builder.build(), ints);
		}

		public boolean matches(BlockState blockState, ItemStack itemStack, int i) {
			if (this.block != null && !blockState.is(this.block)) {
				return false;
			} else {
				return this.item.isPresent() && !((ItemPredicate)this.item.get()).matches(itemStack) ? false : this.numBees.matches(i);
			}
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			if (this.block != null) {
				jsonObject.addProperty("block", BuiltInRegistries.BLOCK.getKey(this.block).toString());
			}

			this.item.ifPresent(itemPredicate -> jsonObject.add("item", itemPredicate.serializeToJson()));
			jsonObject.add("num_bees_inside", this.numBees.serializeToJson());
			return jsonObject;
		}
	}
}

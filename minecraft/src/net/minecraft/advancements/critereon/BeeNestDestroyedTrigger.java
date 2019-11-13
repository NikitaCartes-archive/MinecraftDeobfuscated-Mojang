package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class BeeNestDestroyedTrigger extends SimpleCriterionTrigger<BeeNestDestroyedTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("bee_nest_destroyed");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public BeeNestDestroyedTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		Block block = deserializeBlock(jsonObject);
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("num_bees_inside"));
		return new BeeNestDestroyedTrigger.TriggerInstance(block, itemPredicate, ints);
	}

	@Nullable
	private static Block deserializeBlock(JsonObject jsonObject) {
		if (jsonObject.has("block")) {
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "block"));
			return (Block)Registry.BLOCK.getOptional(resourceLocation).orElseThrow(() -> new JsonSyntaxException("Unknown block type '" + resourceLocation + "'"));
		} else {
			return null;
		}
	}

	public void trigger(ServerPlayer serverPlayer, Block block, ItemStack itemStack, int i) {
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(block, itemStack, i));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Block block;
		private final ItemPredicate item;
		private final MinMaxBounds.Ints numBees;

		public TriggerInstance(Block block, ItemPredicate itemPredicate, MinMaxBounds.Ints ints) {
			super(BeeNestDestroyedTrigger.ID);
			this.block = block;
			this.item = itemPredicate;
			this.numBees = ints;
		}

		public static BeeNestDestroyedTrigger.TriggerInstance destroyedBeeNest(Block block, ItemPredicate.Builder builder, MinMaxBounds.Ints ints) {
			return new BeeNestDestroyedTrigger.TriggerInstance(block, builder.build(), ints);
		}

		public boolean matches(Block block, ItemStack itemStack, int i) {
			if (this.block != null && block != this.block) {
				return false;
			} else {
				return !this.item.matches(itemStack) ? false : this.numBees.matches(i);
			}
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			if (this.block != null) {
				jsonObject.addProperty("block", Registry.BLOCK.getKey(this.block).toString());
			}

			jsonObject.add("item", this.item.serializeToJson());
			jsonObject.add("num_bees_inside", this.numBees.serializeToJson());
			return jsonObject;
		}
	}
}

package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class EnterBlockTrigger extends SimpleCriterionTrigger<EnterBlockTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("enter_block");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public EnterBlockTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		Block block = deserializeBlock(jsonObject);
		Optional<StatePropertiesPredicate> optional2 = StatePropertiesPredicate.fromJson(jsonObject.get("state"));
		if (block != null) {
			optional2.ifPresent(statePropertiesPredicate -> statePropertiesPredicate.checkState(block.getStateDefinition(), string -> {
					throw new JsonSyntaxException("Block " + block + " has no property " + string);
				}));
		}

		return new EnterBlockTrigger.TriggerInstance(optional, block, optional2);
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

	public void trigger(ServerPlayer serverPlayer, BlockState blockState) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(blockState));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		@Nullable
		private final Block block;
		private final Optional<StatePropertiesPredicate> state;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, @Nullable Block block, Optional<StatePropertiesPredicate> optional2) {
			super(EnterBlockTrigger.ID, optional);
			this.block = block;
			this.state = optional2;
		}

		public static EnterBlockTrigger.TriggerInstance entersBlock(Block block) {
			return new EnterBlockTrigger.TriggerInstance(Optional.empty(), block, Optional.empty());
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			if (this.block != null) {
				jsonObject.addProperty("block", BuiltInRegistries.BLOCK.getKey(this.block).toString());
			}

			this.state.ifPresent(statePropertiesPredicate -> jsonObject.add("state", statePropertiesPredicate.serializeToJson()));
			return jsonObject;
		}

		public boolean matches(BlockState blockState) {
			return this.block != null && !blockState.is(this.block)
				? false
				: !this.state.isPresent() || ((StatePropertiesPredicate)this.state.get()).matches(blockState);
		}
	}
}

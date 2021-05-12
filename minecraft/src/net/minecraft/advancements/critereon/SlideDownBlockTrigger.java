package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SlideDownBlockTrigger extends SimpleCriterionTrigger<SlideDownBlockTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("slide_down_block");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public SlideDownBlockTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		Block block = deserializeBlock(jsonObject);
		StatePropertiesPredicate statePropertiesPredicate = StatePropertiesPredicate.fromJson(jsonObject.get("state"));
		if (block != null) {
			statePropertiesPredicate.checkState(block.getStateDefinition(), string -> {
				throw new JsonSyntaxException("Block " + block + " has no property " + string);
			});
		}

		return new SlideDownBlockTrigger.TriggerInstance(composite, block, statePropertiesPredicate);
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

	public void trigger(ServerPlayer serverPlayer, BlockState blockState) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(blockState));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Block block;
		private final StatePropertiesPredicate state;

		public TriggerInstance(EntityPredicate.Composite composite, @Nullable Block block, StatePropertiesPredicate statePropertiesPredicate) {
			super(SlideDownBlockTrigger.ID, composite);
			this.block = block;
			this.state = statePropertiesPredicate;
		}

		public static SlideDownBlockTrigger.TriggerInstance slidesDownBlock(Block block) {
			return new SlideDownBlockTrigger.TriggerInstance(EntityPredicate.Composite.ANY, block, StatePropertiesPredicate.ANY);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			if (this.block != null) {
				jsonObject.addProperty("block", Registry.BLOCK.getKey(this.block).toString());
			}

			jsonObject.add("state", this.state.serializeToJson());
			return jsonObject;
		}

		public boolean matches(BlockState blockState) {
			return this.block != null && !blockState.is(this.block) ? false : this.state.matches(blockState);
		}
	}
}

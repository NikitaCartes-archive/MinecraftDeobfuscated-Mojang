package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Set;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class LootItemBlockStatePropertyCondition implements LootItemCondition {
	final Block block;
	final StatePropertiesPredicate properties;

	LootItemBlockStatePropertyCondition(Block block, StatePropertiesPredicate statePropertiesPredicate) {
		this.block = block;
		this.properties = statePropertiesPredicate;
	}

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.BLOCK_STATE_PROPERTY;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of(LootContextParams.BLOCK_STATE);
	}

	public boolean test(LootContext lootContext) {
		BlockState blockState = lootContext.getParamOrNull(LootContextParams.BLOCK_STATE);
		return blockState != null && blockState.is(this.block) && this.properties.matches(blockState);
	}

	public static LootItemBlockStatePropertyCondition.Builder hasBlockStateProperties(Block block) {
		return new LootItemBlockStatePropertyCondition.Builder(block);
	}

	public static class Builder implements LootItemCondition.Builder {
		private final Block block;
		private StatePropertiesPredicate properties = StatePropertiesPredicate.ANY;

		public Builder(Block block) {
			this.block = block;
		}

		public LootItemBlockStatePropertyCondition.Builder setProperties(StatePropertiesPredicate.Builder builder) {
			this.properties = builder.build();
			return this;
		}

		@Override
		public LootItemCondition build() {
			return new LootItemBlockStatePropertyCondition(this.block, this.properties);
		}
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootItemBlockStatePropertyCondition> {
		public void serialize(
			JsonObject jsonObject, LootItemBlockStatePropertyCondition lootItemBlockStatePropertyCondition, JsonSerializationContext jsonSerializationContext
		) {
			jsonObject.addProperty("block", Registry.BLOCK.getKey(lootItemBlockStatePropertyCondition.block).toString());
			jsonObject.add("properties", lootItemBlockStatePropertyCondition.properties.serializeToJson());
		}

		public LootItemBlockStatePropertyCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "block"));
			Block block = (Block)Registry.BLOCK.getOptional(resourceLocation).orElseThrow(() -> new IllegalArgumentException("Can't find block " + resourceLocation));
			StatePropertiesPredicate statePropertiesPredicate = StatePropertiesPredicate.fromJson(jsonObject.get("properties"));
			statePropertiesPredicate.checkState(block.getStateDefinition(), string -> {
				throw new JsonSyntaxException("Block " + block + " has no property " + string);
			});
			return new LootItemBlockStatePropertyCondition(block, statePropertiesPredicate);
		}
	}
}

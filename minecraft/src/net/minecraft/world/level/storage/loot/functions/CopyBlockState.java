package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyBlockState extends LootItemConditionalFunction {
	private final Block block;
	private final Set<Property<?>> properties;

	private CopyBlockState(LootItemCondition[] lootItemConditions, Block block, Set<Property<?>> set) {
		super(lootItemConditions);
		this.block = block;
		this.properties = set;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.COPY_STATE;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of(LootContextParams.BLOCK_STATE);
	}

	@Override
	protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
		BlockState blockState = lootContext.getParamOrNull(LootContextParams.BLOCK_STATE);
		if (blockState != null) {
			CompoundTag compoundTag = itemStack.getOrCreateTag();
			CompoundTag compoundTag2;
			if (compoundTag.contains("BlockStateTag", 10)) {
				compoundTag2 = compoundTag.getCompound("BlockStateTag");
			} else {
				compoundTag2 = new CompoundTag();
				compoundTag.put("BlockStateTag", compoundTag2);
			}

			this.properties.stream().filter(blockState::hasProperty).forEach(property -> compoundTag2.putString(property.getName(), serialize(blockState, property)));
		}

		return itemStack;
	}

	public static CopyBlockState.Builder copyState(Block block) {
		return new CopyBlockState.Builder(block);
	}

	private static <T extends Comparable<T>> String serialize(BlockState blockState, Property<T> property) {
		T comparable = blockState.getValue(property);
		return property.getName(comparable);
	}

	public static class Builder extends LootItemConditionalFunction.Builder<CopyBlockState.Builder> {
		private final Block block;
		private final Set<Property<?>> properties = Sets.<Property<?>>newHashSet();

		private Builder(Block block) {
			this.block = block;
		}

		public CopyBlockState.Builder copy(Property<?> property) {
			if (!this.block.getStateDefinition().getProperties().contains(property)) {
				throw new IllegalStateException("Property " + property + " is not present on block " + this.block);
			} else {
				this.properties.add(property);
				return this;
			}
		}

		protected CopyBlockState.Builder getThis() {
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new CopyBlockState(this.getConditions(), this.block, this.properties);
		}
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<CopyBlockState> {
		public void serialize(JsonObject jsonObject, CopyBlockState copyBlockState, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, copyBlockState, jsonSerializationContext);
			jsonObject.addProperty("block", Registry.BLOCK.getKey(copyBlockState.block).toString());
			JsonArray jsonArray = new JsonArray();
			copyBlockState.properties.forEach(property -> jsonArray.add(property.getName()));
			jsonObject.add("properties", jsonArray);
		}

		public CopyBlockState deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "block"));
			Block block = (Block)Registry.BLOCK.getOptional(resourceLocation).orElseThrow(() -> new IllegalArgumentException("Can't find block " + resourceLocation));
			StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
			Set<Property<?>> set = Sets.<Property<?>>newHashSet();
			JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "properties", null);
			if (jsonArray != null) {
				jsonArray.forEach(jsonElement -> set.add(stateDefinition.getProperty(GsonHelper.convertToString(jsonElement, "property"))));
			}

			return new CopyBlockState(lootItemConditions, block, set);
		}
	}
}

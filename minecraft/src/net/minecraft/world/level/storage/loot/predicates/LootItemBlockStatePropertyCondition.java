package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public record LootItemBlockStatePropertyCondition(Holder<Block> block, Optional<StatePropertiesPredicate> properties) implements LootItemCondition {
	public static final MapCodec<LootItemBlockStatePropertyCondition> CODEC = RecordCodecBuilder.<LootItemBlockStatePropertyCondition>mapCodec(
			instance -> instance.group(
						BuiltInRegistries.BLOCK.holderByNameCodec().fieldOf("block").forGetter(LootItemBlockStatePropertyCondition::block),
						StatePropertiesPredicate.CODEC.optionalFieldOf("properties").forGetter(LootItemBlockStatePropertyCondition::properties)
					)
					.apply(instance, LootItemBlockStatePropertyCondition::new)
		)
		.validate(LootItemBlockStatePropertyCondition::validate);

	private static DataResult<LootItemBlockStatePropertyCondition> validate(LootItemBlockStatePropertyCondition lootItemBlockStatePropertyCondition) {
		return (DataResult<LootItemBlockStatePropertyCondition>)lootItemBlockStatePropertyCondition.properties()
			.flatMap(statePropertiesPredicate -> statePropertiesPredicate.checkState(lootItemBlockStatePropertyCondition.block().value().getStateDefinition()))
			.map(string -> DataResult.error(() -> "Block " + lootItemBlockStatePropertyCondition.block() + " has no property" + string))
			.orElse(DataResult.success(lootItemBlockStatePropertyCondition));
	}

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.BLOCK_STATE_PROPERTY;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return Set.of(LootContextParams.BLOCK_STATE);
	}

	public boolean test(LootContext lootContext) {
		BlockState blockState = lootContext.getParamOrNull(LootContextParams.BLOCK_STATE);
		return blockState != null
			&& blockState.is(this.block)
			&& (this.properties.isEmpty() || ((StatePropertiesPredicate)this.properties.get()).matches(blockState));
	}

	public static LootItemBlockStatePropertyCondition.Builder hasBlockStateProperties(Block block) {
		return new LootItemBlockStatePropertyCondition.Builder(block);
	}

	public static class Builder implements LootItemCondition.Builder {
		private final Holder<Block> block;
		private Optional<StatePropertiesPredicate> properties = Optional.empty();

		public Builder(Block block) {
			this.block = block.builtInRegistryHolder();
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
}

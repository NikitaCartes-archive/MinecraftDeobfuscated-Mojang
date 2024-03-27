package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class EnterBlockTrigger extends SimpleCriterionTrigger<EnterBlockTrigger.TriggerInstance> {
	@Override
	public Codec<EnterBlockTrigger.TriggerInstance> codec() {
		return EnterBlockTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, BlockState blockState) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(blockState));
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<Holder<Block>> block, Optional<StatePropertiesPredicate> state)
		implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<EnterBlockTrigger.TriggerInstance> CODEC = RecordCodecBuilder.<EnterBlockTrigger.TriggerInstance>create(
				instance -> instance.group(
							EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(EnterBlockTrigger.TriggerInstance::player),
							BuiltInRegistries.BLOCK.holderByNameCodec().optionalFieldOf("block").forGetter(EnterBlockTrigger.TriggerInstance::block),
							StatePropertiesPredicate.CODEC.optionalFieldOf("state").forGetter(EnterBlockTrigger.TriggerInstance::state)
						)
						.apply(instance, EnterBlockTrigger.TriggerInstance::new)
			)
			.validate(EnterBlockTrigger.TriggerInstance::validate);

		private static DataResult<EnterBlockTrigger.TriggerInstance> validate(EnterBlockTrigger.TriggerInstance triggerInstance) {
			return (DataResult<EnterBlockTrigger.TriggerInstance>)triggerInstance.block
				.flatMap(
					holder -> triggerInstance.state
							.flatMap(statePropertiesPredicate -> statePropertiesPredicate.checkState(((Block)holder.value()).getStateDefinition()))
							.map(string -> DataResult.error(() -> "Block" + holder + " has no property " + string))
				)
				.orElseGet(() -> DataResult.success(triggerInstance));
		}

		public static Criterion<EnterBlockTrigger.TriggerInstance> entersBlock(Block block) {
			return CriteriaTriggers.ENTER_BLOCK
				.createCriterion(new EnterBlockTrigger.TriggerInstance(Optional.empty(), Optional.of(block.builtInRegistryHolder()), Optional.empty()));
		}

		public boolean matches(BlockState blockState) {
			return this.block.isPresent() && !blockState.is((Holder<Block>)this.block.get())
				? false
				: !this.state.isPresent() || ((StatePropertiesPredicate)this.state.get()).matches(blockState);
		}
	}
}

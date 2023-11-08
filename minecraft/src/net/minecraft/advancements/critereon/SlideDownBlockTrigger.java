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
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class SlideDownBlockTrigger extends SimpleCriterionTrigger<SlideDownBlockTrigger.TriggerInstance> {
	@Override
	public Codec<SlideDownBlockTrigger.TriggerInstance> codec() {
		return SlideDownBlockTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, BlockState blockState) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(blockState));
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<Holder<Block>> block, Optional<StatePropertiesPredicate> state)
		implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<SlideDownBlockTrigger.TriggerInstance> CODEC = ExtraCodecs.validate(
			RecordCodecBuilder.create(
				instance -> instance.group(
							ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(SlideDownBlockTrigger.TriggerInstance::player),
							ExtraCodecs.strictOptionalField(BuiltInRegistries.BLOCK.holderByNameCodec(), "block").forGetter(SlideDownBlockTrigger.TriggerInstance::block),
							ExtraCodecs.strictOptionalField(StatePropertiesPredicate.CODEC, "state").forGetter(SlideDownBlockTrigger.TriggerInstance::state)
						)
						.apply(instance, SlideDownBlockTrigger.TriggerInstance::new)
			),
			SlideDownBlockTrigger.TriggerInstance::validate
		);

		private static DataResult<SlideDownBlockTrigger.TriggerInstance> validate(SlideDownBlockTrigger.TriggerInstance triggerInstance) {
			return (DataResult<SlideDownBlockTrigger.TriggerInstance>)triggerInstance.block
				.flatMap(
					holder -> triggerInstance.state
							.flatMap(statePropertiesPredicate -> statePropertiesPredicate.checkState(((Block)holder.value()).getStateDefinition()))
							.map(string -> DataResult.error(() -> "Block" + holder + " has no property " + string))
				)
				.orElseGet(() -> DataResult.success(triggerInstance));
		}

		public static Criterion<SlideDownBlockTrigger.TriggerInstance> slidesDownBlock(Block block) {
			return CriteriaTriggers.HONEY_BLOCK_SLIDE
				.createCriterion(new SlideDownBlockTrigger.TriggerInstance(Optional.empty(), Optional.of(block.builtInRegistryHolder()), Optional.empty()));
		}

		public boolean matches(BlockState blockState) {
			return this.block.isPresent() && !blockState.is((Holder<Block>)this.block.get())
				? false
				: !this.state.isPresent() || ((StatePropertiesPredicate)this.state.get()).matches(blockState);
		}
	}
}

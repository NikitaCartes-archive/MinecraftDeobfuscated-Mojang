package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BeeNestDestroyedTrigger extends SimpleCriterionTrigger<BeeNestDestroyedTrigger.TriggerInstance> {
	@Override
	public Codec<BeeNestDestroyedTrigger.TriggerInstance> codec() {
		return BeeNestDestroyedTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, BlockState blockState, ItemStack itemStack, int i) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(blockState, itemStack, i));
	}

	public static record TriggerInstance(
		Optional<ContextAwarePredicate> player, Optional<Holder<Block>> block, Optional<ItemPredicate> item, MinMaxBounds.Ints beesInside
	) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<BeeNestDestroyedTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(BeeNestDestroyedTrigger.TriggerInstance::player),
						BuiltInRegistries.BLOCK.holderByNameCodec().optionalFieldOf("block").forGetter(BeeNestDestroyedTrigger.TriggerInstance::block),
						ItemPredicate.CODEC.optionalFieldOf("item").forGetter(BeeNestDestroyedTrigger.TriggerInstance::item),
						MinMaxBounds.Ints.CODEC.optionalFieldOf("num_bees_inside", MinMaxBounds.Ints.ANY).forGetter(BeeNestDestroyedTrigger.TriggerInstance::beesInside)
					)
					.apply(instance, BeeNestDestroyedTrigger.TriggerInstance::new)
		);

		public static Criterion<BeeNestDestroyedTrigger.TriggerInstance> destroyedBeeNest(Block block, ItemPredicate.Builder builder, MinMaxBounds.Ints ints) {
			return CriteriaTriggers.BEE_NEST_DESTROYED
				.createCriterion(
					new BeeNestDestroyedTrigger.TriggerInstance(Optional.empty(), Optional.of(block.builtInRegistryHolder()), Optional.of(builder.build()), ints)
				);
		}

		public boolean matches(BlockState blockState, ItemStack itemStack, int i) {
			if (this.block.isPresent() && !blockState.is((Holder<Block>)this.block.get())) {
				return false;
			} else {
				return this.item.isPresent() && !((ItemPredicate)this.item.get()).matches(itemStack) ? false : this.beesInside.matches(i);
			}
		}
	}
}

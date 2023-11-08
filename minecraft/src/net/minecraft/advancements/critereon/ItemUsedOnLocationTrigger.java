package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

public class ItemUsedOnLocationTrigger extends SimpleCriterionTrigger<ItemUsedOnLocationTrigger.TriggerInstance> {
	@Override
	public Codec<ItemUsedOnLocationTrigger.TriggerInstance> codec() {
		return ItemUsedOnLocationTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, BlockPos blockPos, ItemStack itemStack) {
		ServerLevel serverLevel = serverPlayer.serverLevel();
		BlockState blockState = serverLevel.getBlockState(blockPos);
		LootParams lootParams = new LootParams.Builder(serverLevel)
			.withParameter(LootContextParams.ORIGIN, blockPos.getCenter())
			.withParameter(LootContextParams.THIS_ENTITY, serverPlayer)
			.withParameter(LootContextParams.BLOCK_STATE, blockState)
			.withParameter(LootContextParams.TOOL, itemStack)
			.create(LootContextParamSets.ADVANCEMENT_LOCATION);
		LootContext lootContext = new LootContext.Builder(lootParams).create(Optional.empty());
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext));
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> location)
		implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<ItemUsedOnLocationTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(ItemUsedOnLocationTrigger.TriggerInstance::player),
						ExtraCodecs.strictOptionalField(ContextAwarePredicate.CODEC, "location").forGetter(ItemUsedOnLocationTrigger.TriggerInstance::location)
					)
					.apply(instance, ItemUsedOnLocationTrigger.TriggerInstance::new)
		);

		public static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placedBlock(Block block) {
			ContextAwarePredicate contextAwarePredicate = ContextAwarePredicate.create(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).build());
			return CriteriaTriggers.PLACED_BLOCK.createCriterion(new ItemUsedOnLocationTrigger.TriggerInstance(Optional.empty(), Optional.of(contextAwarePredicate)));
		}

		public static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placedBlock(LootItemCondition.Builder... builders) {
			ContextAwarePredicate contextAwarePredicate = ContextAwarePredicate.create(
				(LootItemCondition[])Arrays.stream(builders).map(LootItemCondition.Builder::build).toArray(LootItemCondition[]::new)
			);
			return CriteriaTriggers.PLACED_BLOCK.createCriterion(new ItemUsedOnLocationTrigger.TriggerInstance(Optional.empty(), Optional.of(contextAwarePredicate)));
		}

		private static ItemUsedOnLocationTrigger.TriggerInstance itemUsedOnLocation(LocationPredicate.Builder builder, ItemPredicate.Builder builder2) {
			ContextAwarePredicate contextAwarePredicate = ContextAwarePredicate.create(
				LocationCheck.checkLocation(builder).build(), MatchTool.toolMatches(builder2).build()
			);
			return new ItemUsedOnLocationTrigger.TriggerInstance(Optional.empty(), Optional.of(contextAwarePredicate));
		}

		public static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> itemUsedOnBlock(LocationPredicate.Builder builder, ItemPredicate.Builder builder2) {
			return CriteriaTriggers.ITEM_USED_ON_BLOCK.createCriterion(itemUsedOnLocation(builder, builder2));
		}

		public static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> allayDropItemOnBlock(LocationPredicate.Builder builder, ItemPredicate.Builder builder2) {
			return CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK.createCriterion(itemUsedOnLocation(builder, builder2));
		}

		public boolean matches(LootContext lootContext) {
			return this.location.isEmpty() || ((ContextAwarePredicate)this.location.get()).matches(lootContext);
		}

		@Override
		public void validate(CriterionValidator criterionValidator) {
			SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
			this.location.ifPresent(contextAwarePredicate -> criterionValidator.validate(contextAwarePredicate, LootContextParamSets.ADVANCEMENT_LOCATION, ".location"));
		}
	}
}

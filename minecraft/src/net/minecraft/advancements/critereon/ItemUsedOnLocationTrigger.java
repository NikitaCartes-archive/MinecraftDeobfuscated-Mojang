package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
	final ResourceLocation id;

	public ItemUsedOnLocationTrigger(ResourceLocation resourceLocation) {
		this.id = resourceLocation;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	public ItemUsedOnLocationTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		Optional<Optional<ContextAwarePredicate>> optional2 = ContextAwarePredicate.fromElement(
			"location", deserializationContext, jsonObject.get("location"), LootContextParamSets.ADVANCEMENT_LOCATION
		);
		if (optional2.isEmpty()) {
			throw new JsonParseException("Failed to parse 'location' field");
		} else {
			return new ItemUsedOnLocationTrigger.TriggerInstance(this.id, optional, (Optional<ContextAwarePredicate>)optional2.get());
		}
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

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Optional<ContextAwarePredicate> location;

		public TriggerInstance(ResourceLocation resourceLocation, Optional<ContextAwarePredicate> optional, Optional<ContextAwarePredicate> optional2) {
			super(resourceLocation, optional);
			this.location = optional2;
		}

		public static ItemUsedOnLocationTrigger.TriggerInstance placedBlock(Block block) {
			ContextAwarePredicate contextAwarePredicate = ContextAwarePredicate.create(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).build());
			return new ItemUsedOnLocationTrigger.TriggerInstance(CriteriaTriggers.PLACED_BLOCK.id, Optional.empty(), Optional.of(contextAwarePredicate));
		}

		public static ItemUsedOnLocationTrigger.TriggerInstance placedBlock(LootItemCondition.Builder... builders) {
			ContextAwarePredicate contextAwarePredicate = ContextAwarePredicate.create(
				(LootItemCondition[])Arrays.stream(builders).map(LootItemCondition.Builder::build).toArray(LootItemCondition[]::new)
			);
			return new ItemUsedOnLocationTrigger.TriggerInstance(CriteriaTriggers.PLACED_BLOCK.id, Optional.empty(), Optional.of(contextAwarePredicate));
		}

		private static ItemUsedOnLocationTrigger.TriggerInstance itemUsedOnLocation(
			LocationPredicate.Builder builder, ItemPredicate.Builder builder2, ResourceLocation resourceLocation
		) {
			ContextAwarePredicate contextAwarePredicate = ContextAwarePredicate.create(
				LocationCheck.checkLocation(builder).build(), MatchTool.toolMatches(builder2).build()
			);
			return new ItemUsedOnLocationTrigger.TriggerInstance(resourceLocation, Optional.empty(), Optional.of(contextAwarePredicate));
		}

		public static ItemUsedOnLocationTrigger.TriggerInstance itemUsedOnBlock(LocationPredicate.Builder builder, ItemPredicate.Builder builder2) {
			return itemUsedOnLocation(builder, builder2, CriteriaTriggers.ITEM_USED_ON_BLOCK.id);
		}

		public static ItemUsedOnLocationTrigger.TriggerInstance allayDropItemOnBlock(LocationPredicate.Builder builder, ItemPredicate.Builder builder2) {
			return itemUsedOnLocation(builder, builder2, CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK.id);
		}

		public boolean matches(LootContext lootContext) {
			return this.location.isEmpty() || ((ContextAwarePredicate)this.location.get()).matches(lootContext);
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			this.location.ifPresent(contextAwarePredicate -> jsonObject.add("location", contextAwarePredicate.toJson()));
			return jsonObject;
		}
	}
}

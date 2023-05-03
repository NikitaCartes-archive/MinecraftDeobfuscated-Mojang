package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class PlayerTrigger extends SimpleCriterionTrigger<PlayerTrigger.TriggerInstance> {
	final ResourceLocation id;

	public PlayerTrigger(ResourceLocation resourceLocation) {
		this.id = resourceLocation;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	public PlayerTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, ContextAwarePredicate contextAwarePredicate, DeserializationContext deserializationContext
	) {
		return new PlayerTrigger.TriggerInstance(this.id, contextAwarePredicate);
	}

	public void trigger(ServerPlayer serverPlayer) {
		this.trigger(serverPlayer, triggerInstance -> true);
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		public TriggerInstance(ResourceLocation resourceLocation, ContextAwarePredicate contextAwarePredicate) {
			super(resourceLocation, contextAwarePredicate);
		}

		public static PlayerTrigger.TriggerInstance located(LocationPredicate locationPredicate) {
			return new PlayerTrigger.TriggerInstance(
				CriteriaTriggers.LOCATION.id, EntityPredicate.wrap(EntityPredicate.Builder.entity().located(locationPredicate).build())
			);
		}

		public static PlayerTrigger.TriggerInstance located(EntityPredicate entityPredicate) {
			return new PlayerTrigger.TriggerInstance(CriteriaTriggers.LOCATION.id, EntityPredicate.wrap(entityPredicate));
		}

		public static PlayerTrigger.TriggerInstance sleptInBed() {
			return new PlayerTrigger.TriggerInstance(CriteriaTriggers.SLEPT_IN_BED.id, ContextAwarePredicate.ANY);
		}

		public static PlayerTrigger.TriggerInstance raidWon() {
			return new PlayerTrigger.TriggerInstance(CriteriaTriggers.RAID_WIN.id, ContextAwarePredicate.ANY);
		}

		public static PlayerTrigger.TriggerInstance avoidVibration() {
			return new PlayerTrigger.TriggerInstance(CriteriaTriggers.AVOID_VIBRATION.id, ContextAwarePredicate.ANY);
		}

		public static PlayerTrigger.TriggerInstance tick() {
			return new PlayerTrigger.TriggerInstance(CriteriaTriggers.TICK.id, ContextAwarePredicate.ANY);
		}

		public static PlayerTrigger.TriggerInstance walkOnBlockWithEquipment(Block block, Item item) {
			return located(
				EntityPredicate.Builder.entity()
					.equipment(EntityEquipmentPredicate.Builder.equipment().feet(ItemPredicate.Builder.item().of(item).build()).build())
					.steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(block).build()).build())
					.build()
			);
		}
	}
}

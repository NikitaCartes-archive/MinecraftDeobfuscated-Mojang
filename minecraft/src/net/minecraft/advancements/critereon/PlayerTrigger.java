package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
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
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		return new PlayerTrigger.TriggerInstance(this.id, optional);
	}

	public void trigger(ServerPlayer serverPlayer) {
		this.trigger(serverPlayer, triggerInstance -> true);
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		public TriggerInstance(ResourceLocation resourceLocation, Optional<ContextAwarePredicate> optional) {
			super(resourceLocation, optional);
		}

		public static PlayerTrigger.TriggerInstance located(LocationPredicate.Builder builder) {
			return new PlayerTrigger.TriggerInstance(CriteriaTriggers.LOCATION.id, EntityPredicate.wrap(EntityPredicate.Builder.entity().located(builder)));
		}

		public static PlayerTrigger.TriggerInstance located(Optional<EntityPredicate> optional) {
			return new PlayerTrigger.TriggerInstance(CriteriaTriggers.LOCATION.id, EntityPredicate.wrap(optional));
		}

		public static PlayerTrigger.TriggerInstance sleptInBed() {
			return new PlayerTrigger.TriggerInstance(CriteriaTriggers.SLEPT_IN_BED.id, Optional.empty());
		}

		public static PlayerTrigger.TriggerInstance raidWon() {
			return new PlayerTrigger.TriggerInstance(CriteriaTriggers.RAID_WIN.id, Optional.empty());
		}

		public static PlayerTrigger.TriggerInstance avoidVibration() {
			return new PlayerTrigger.TriggerInstance(CriteriaTriggers.AVOID_VIBRATION.id, Optional.empty());
		}

		public static PlayerTrigger.TriggerInstance tick() {
			return new PlayerTrigger.TriggerInstance(CriteriaTriggers.TICK.id, Optional.empty());
		}

		public static PlayerTrigger.TriggerInstance walkOnBlockWithEquipment(Block block, Item item) {
			return located(
				EntityPredicate.Builder.entity()
					.equipment(EntityEquipmentPredicate.Builder.equipment().feet(ItemPredicate.Builder.item().of(item)))
					.steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(block)))
					.build()
			);
		}
	}
}

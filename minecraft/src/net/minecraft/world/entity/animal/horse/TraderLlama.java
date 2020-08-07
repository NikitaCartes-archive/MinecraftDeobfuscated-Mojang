package net.minecraft.world.entity.animal.horse;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class TraderLlama extends Llama {
	private int despawnDelay = 47999;

	public TraderLlama(EntityType<? extends TraderLlama> entityType, Level level) {
		super(entityType, level);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean isTraderLlama() {
		return true;
	}

	@Override
	protected Llama makeBabyLlama() {
		return EntityType.TRADER_LLAMA.create(this.level);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("DespawnDelay", this.despawnDelay);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("DespawnDelay", 99)) {
			this.despawnDelay = compoundTag.getInt("DespawnDelay");
		}
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(1, new PanicGoal(this, 2.0));
		this.targetSelector.addGoal(1, new TraderLlama.TraderLlamaDefendWanderingTraderGoal(this));
	}

	@Override
	protected void doPlayerRide(Player player) {
		Entity entity = this.getLeashHolder();
		if (!(entity instanceof WanderingTrader)) {
			super.doPlayerRide(player);
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (!this.level.isClientSide) {
			this.maybeDespawn();
		}
	}

	private void maybeDespawn() {
		if (this.canDespawn()) {
			this.despawnDelay = this.isLeashedToWanderingTrader() ? ((WanderingTrader)this.getLeashHolder()).getDespawnDelay() - 1 : this.despawnDelay - 1;
			if (this.despawnDelay <= 0) {
				this.dropLeash(true, false);
				this.remove();
			}
		}
	}

	private boolean canDespawn() {
		return !this.isTamed() && !this.isLeashedToSomethingOtherThanTheWanderingTrader() && !this.hasOnePlayerPassenger();
	}

	private boolean isLeashedToWanderingTrader() {
		return this.getLeashHolder() instanceof WanderingTrader;
	}

	private boolean isLeashedToSomethingOtherThanTheWanderingTrader() {
		return this.isLeashed() && !this.isLeashedToWanderingTrader();
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
	) {
		if (mobSpawnType == MobSpawnType.EVENT) {
			this.setAge(0);
		}

		if (spawnGroupData == null) {
			spawnGroupData = new AgableMob.AgableMobGroupData(false);
		}

		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
	}

	public class TraderLlamaDefendWanderingTraderGoal extends TargetGoal {
		private final Llama llama;
		private LivingEntity ownerLastHurtBy;
		private int timestamp;

		public TraderLlamaDefendWanderingTraderGoal(Llama llama) {
			super(llama, false);
			this.llama = llama;
			this.setFlags(EnumSet.of(Goal.Flag.TARGET));
		}

		@Override
		public boolean canUse() {
			if (!this.llama.isLeashed()) {
				return false;
			} else {
				Entity entity = this.llama.getLeashHolder();
				if (!(entity instanceof WanderingTrader)) {
					return false;
				} else {
					WanderingTrader wanderingTrader = (WanderingTrader)entity;
					this.ownerLastHurtBy = wanderingTrader.getLastHurtByMob();
					int i = wanderingTrader.getLastHurtByMobTimestamp();
					return i != this.timestamp && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT);
				}
			}
		}

		@Override
		public void start() {
			this.mob.setTarget(this.ownerLastHurtBy);
			Entity entity = this.llama.getLeashHolder();
			if (entity instanceof WanderingTrader) {
				this.timestamp = ((WanderingTrader)entity).getLastHurtByMobTimestamp();
			}

			super.start();
		}
	}
}

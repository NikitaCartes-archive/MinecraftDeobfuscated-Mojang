package net.minecraft.world.entity;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Scoreboard;

public enum ConversionType {
	SINGLE(true) {
		@Override
		void convert(Mob mob, Mob mob2, ConversionParams conversionParams) {
			Entity entity = mob.getFirstPassenger();
			if (entity != null) {
				entity.stopRiding();
				entity.boardingCooldown = 0;
				entity.startRiding(mob2);
			}

			if (conversionParams.keepEquipment()) {
				for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
					ItemStack itemStack = mob.getItemBySlot(equipmentSlot);
					if (!itemStack.isEmpty()) {
						mob2.setItemSlot(equipmentSlot, itemStack.copyAndClear());
						mob2.setDropChance(equipmentSlot, mob.getEquipmentDropChance(equipmentSlot));
					}
				}
			}

			mob2.getAttributes().assignAllValues(mob.getAttributes());
			mob2.fallDistance = mob.fallDistance;
			mob2.setSharedFlag(7, mob.isFallFlying());
			float f = mob.getHealth() / mob.getMaxHealth();
			mob2.setHealth(mob2.getMaxHealth() * f);
			mob2.lastHurtByPlayerTime = mob.lastHurtByPlayerTime;
			mob2.hurtTime = mob.hurtTime;
			mob2.yBodyRot = mob.yBodyRot;
			mob2.copyPosition(mob);
			mob2.setDeltaMovement(mob.getDeltaMovement());
			mob2.setOnGround(mob.onGround());
			mob.getSleepingPos().ifPresent(mob2::setSleepingPos);
			Entity entity2 = mob.getLeashHolder();
			if (entity2 != null) {
				mob2.setLeashedTo(entity2, true);
			}

			this.convertCommon(mob, mob2, conversionParams);
		}
	},
	SPLIT_ON_DEATH(false) {
		@Override
		void convert(Mob mob, Mob mob2, ConversionParams conversionParams) {
			Entity entity = mob.getFirstPassenger();
			if (entity != null) {
				entity.stopRiding();
			}

			Entity entity2 = mob.getLeashHolder();
			if (entity2 != null) {
				mob.dropLeash(true, true);
			}

			this.convertCommon(mob, mob2, conversionParams);
		}
	};

	private final boolean discardAfterConversion;

	ConversionType(final boolean bl) {
		this.discardAfterConversion = bl;
	}

	public boolean shouldDiscardAfterConversion() {
		return this.discardAfterConversion;
	}

	abstract void convert(Mob mob, Mob mob2, ConversionParams conversionParams);

	void convertCommon(Mob mob, Mob mob2, ConversionParams conversionParams) {
		mob2.setAbsorptionAmount(mob.getAbsorptionAmount());

		for (MobEffectInstance mobEffectInstance : mob.getActiveEffects()) {
			mob2.addEffect(mobEffectInstance);
		}

		if (mob.isBaby()) {
			mob2.setBaby(true);
		}

		if (mob instanceof AgeableMob ageableMob && mob2 instanceof AgeableMob ageableMob2) {
			ageableMob2.setAge(ageableMob.getAge());
			ageableMob2.forcedAge = ageableMob.forcedAge;
			ageableMob2.forcedAgeTimer = ageableMob.forcedAgeTimer;
		}

		Brain<?> brain = mob.getBrain();
		Brain<?> brain2 = mob2.getBrain();
		if (brain.checkMemory(MemoryModuleType.ANGRY_AT, MemoryStatus.REGISTERED) && brain.hasMemoryValue(MemoryModuleType.ANGRY_AT)) {
			brain2.setMemory(MemoryModuleType.ANGRY_AT, brain.getMemory(MemoryModuleType.ANGRY_AT));
		}

		if (conversionParams.preserveCanPickUpLoot()) {
			mob2.setCanPickUpLoot(mob.canPickUpLoot());
		}

		mob2.setLeftHanded(mob.isLeftHanded());
		mob2.setNoAi(mob.isNoAi());
		if (mob.isPersistenceRequired()) {
			mob2.setPersistenceRequired();
		}

		mob2.setLootTable(mob.getLootTable());
		mob2.setLootTableSeed(mob.getLootTableSeed());
		if (mob.hasCustomName()) {
			mob2.setCustomName(mob.getCustomName());
			mob2.setCustomNameVisible(mob.isCustomNameVisible());
		}

		mob2.setSharedFlagOnFire(mob.isOnFire());
		mob2.setInvulnerable(mob.isInvulnerable());
		mob2.setNoGravity(mob.isNoGravity());
		mob2.setPortalCooldown(mob.getPortalCooldown());
		mob2.setSilent(mob.isSilent());
		mob.getTags().forEach(mob2::addTag);
		if (conversionParams.team() != null) {
			Scoreboard scoreboard = mob2.level().getScoreboard();
			scoreboard.addPlayerToTeam(mob2.getStringUUID(), conversionParams.team());
			if (mob.getTeam() != null && mob.getTeam() == conversionParams.team()) {
				scoreboard.removePlayerFromTeam(mob.getStringUUID(), mob.getTeam());
			}
		}
	}
}

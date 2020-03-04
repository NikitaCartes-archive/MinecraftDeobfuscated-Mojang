package net.minecraft.world.entity.animal.horse;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.global.LightningBolt;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class SkeletonTrapGoal extends Goal {
	private final SkeletonHorse horse;

	public SkeletonTrapGoal(SkeletonHorse skeletonHorse) {
		this.horse = skeletonHorse;
	}

	@Override
	public boolean canUse() {
		return this.horse.level.hasNearbyAlivePlayer(this.horse.getX(), this.horse.getY(), this.horse.getZ(), 10.0);
	}

	@Override
	public void tick() {
		DifficultyInstance difficultyInstance = this.horse.level.getCurrentDifficultyAt(this.horse.blockPosition());
		this.horse.setTrap(false);
		this.horse.setTamed(true);
		this.horse.setAge(0);
		((ServerLevel)this.horse.level).addGlobalEntity(new LightningBolt(this.horse.level, this.horse.getX(), this.horse.getY(), this.horse.getZ(), true));
		Skeleton skeleton = this.createSkeleton(difficultyInstance, this.horse);
		skeleton.startRiding(this.horse);

		for (int i = 0; i < 3; i++) {
			AbstractHorse abstractHorse = this.createHorse(difficultyInstance);
			Skeleton skeleton2 = this.createSkeleton(difficultyInstance, abstractHorse);
			skeleton2.startRiding(abstractHorse);
			abstractHorse.push(this.horse.getRandom().nextGaussian() * 0.5, 0.0, this.horse.getRandom().nextGaussian() * 0.5);
		}
	}

	private AbstractHorse createHorse(DifficultyInstance difficultyInstance) {
		SkeletonHorse skeletonHorse = EntityType.SKELETON_HORSE.create(this.horse.level);
		skeletonHorse.finalizeSpawn(this.horse.level, difficultyInstance, MobSpawnType.TRIGGERED, null, null);
		skeletonHorse.setPos(this.horse.getX(), this.horse.getY(), this.horse.getZ());
		skeletonHorse.invulnerableTime = 60;
		skeletonHorse.setPersistenceRequired();
		skeletonHorse.setTamed(true);
		skeletonHorse.setAge(0);
		skeletonHorse.level.addFreshEntity(skeletonHorse);
		return skeletonHorse;
	}

	private Skeleton createSkeleton(DifficultyInstance difficultyInstance, AbstractHorse abstractHorse) {
		Skeleton skeleton = EntityType.SKELETON.create(abstractHorse.level);
		skeleton.finalizeSpawn(abstractHorse.level, difficultyInstance, MobSpawnType.TRIGGERED, null, null);
		skeleton.setPos(abstractHorse.getX(), abstractHorse.getY(), abstractHorse.getZ());
		skeleton.invulnerableTime = 60;
		skeleton.setPersistenceRequired();
		if (skeleton.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
			skeleton.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
		}

		skeleton.setItemSlot(
			EquipmentSlot.MAINHAND,
			EnchantmentHelper.enchantItem(
				skeleton.getRandom(), skeleton.getMainHandItem(), (int)(5.0F + difficultyInstance.getSpecialMultiplier() * (float)skeleton.getRandom().nextInt(18)), false
			)
		);
		skeleton.setItemSlot(
			EquipmentSlot.HEAD,
			EnchantmentHelper.enchantItem(
				skeleton.getRandom(),
				skeleton.getItemBySlot(EquipmentSlot.HEAD),
				(int)(5.0F + difficultyInstance.getSpecialMultiplier() * (float)skeleton.getRandom().nextInt(18)),
				false
			)
		);
		skeleton.level.addFreshEntity(skeleton);
		return skeleton;
	}
}

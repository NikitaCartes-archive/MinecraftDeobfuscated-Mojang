package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.Goal;
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
		return this.horse.level().hasNearbyAlivePlayer(this.horse.getX(), this.horse.getY(), this.horse.getZ(), 10.0);
	}

	@Override
	public void tick() {
		ServerLevel serverLevel = (ServerLevel)this.horse.level();
		DifficultyInstance difficultyInstance = serverLevel.getCurrentDifficultyAt(this.horse.blockPosition());
		this.horse.setTrap(false);
		this.horse.setTamed(true);
		this.horse.setAge(0);
		LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
		if (lightningBolt != null) {
			lightningBolt.moveTo(this.horse.getX(), this.horse.getY(), this.horse.getZ());
			lightningBolt.setVisualOnly(true);
			serverLevel.addFreshEntity(lightningBolt);
			Skeleton skeleton = this.createSkeleton(difficultyInstance, this.horse);
			if (skeleton != null) {
				skeleton.startRiding(this.horse);
				serverLevel.addFreshEntityWithPassengers(skeleton);

				for (int i = 0; i < 3; i++) {
					AbstractHorse abstractHorse = this.createHorse(difficultyInstance);
					if (abstractHorse != null) {
						Skeleton skeleton2 = this.createSkeleton(difficultyInstance, abstractHorse);
						if (skeleton2 != null) {
							skeleton2.startRiding(abstractHorse);
							abstractHorse.push(this.horse.getRandom().triangle(0.0, 1.1485), 0.0, this.horse.getRandom().triangle(0.0, 1.1485));
							serverLevel.addFreshEntityWithPassengers(abstractHorse);
						}
					}
				}
			}
		}
	}

	@Nullable
	private AbstractHorse createHorse(DifficultyInstance difficultyInstance) {
		SkeletonHorse skeletonHorse = EntityType.SKELETON_HORSE.create(this.horse.level());
		if (skeletonHorse != null) {
			skeletonHorse.finalizeSpawn((ServerLevel)this.horse.level(), difficultyInstance, MobSpawnType.TRIGGERED, null);
			skeletonHorse.setPos(this.horse.getX(), this.horse.getY(), this.horse.getZ());
			skeletonHorse.invulnerableTime = 60;
			skeletonHorse.setPersistenceRequired();
			skeletonHorse.setTamed(true);
			skeletonHorse.setAge(0);
		}

		return skeletonHorse;
	}

	@Nullable
	private Skeleton createSkeleton(DifficultyInstance difficultyInstance, AbstractHorse abstractHorse) {
		Skeleton skeleton = EntityType.SKELETON.create(abstractHorse.level());
		if (skeleton != null) {
			skeleton.finalizeSpawn((ServerLevel)abstractHorse.level(), difficultyInstance, MobSpawnType.TRIGGERED, null);
			skeleton.setPos(abstractHorse.getX(), abstractHorse.getY(), abstractHorse.getZ());
			skeleton.invulnerableTime = 60;
			skeleton.setPersistenceRequired();
			if (skeleton.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
				skeleton.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
			}

			skeleton.setItemSlot(
				EquipmentSlot.MAINHAND,
				EnchantmentHelper.enchantItem(
					skeleton.getRandom(),
					this.disenchant(skeleton.getMainHandItem()),
					(int)(5.0F + difficultyInstance.getSpecialMultiplier() * (float)skeleton.getRandom().nextInt(18)),
					false
				)
			);
			skeleton.setItemSlot(
				EquipmentSlot.HEAD,
				EnchantmentHelper.enchantItem(
					skeleton.getRandom(),
					this.disenchant(skeleton.getItemBySlot(EquipmentSlot.HEAD)),
					(int)(5.0F + difficultyInstance.getSpecialMultiplier() * (float)skeleton.getRandom().nextInt(18)),
					false
				)
			);
		}

		return skeleton;
	}

	private ItemStack disenchant(ItemStack itemStack) {
		itemStack.removeTagKey("Enchantments");
		return itemStack;
	}
}

package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;

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
		LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(serverLevel, EntitySpawnReason.TRIGGERED);
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
		SkeletonHorse skeletonHorse = EntityType.SKELETON_HORSE.create(this.horse.level(), EntitySpawnReason.TRIGGERED);
		if (skeletonHorse != null) {
			skeletonHorse.finalizeSpawn((ServerLevel)this.horse.level(), difficultyInstance, EntitySpawnReason.TRIGGERED, null);
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
		Skeleton skeleton = EntityType.SKELETON.create(abstractHorse.level(), EntitySpawnReason.TRIGGERED);
		if (skeleton != null) {
			skeleton.finalizeSpawn((ServerLevel)abstractHorse.level(), difficultyInstance, EntitySpawnReason.TRIGGERED, null);
			skeleton.setPos(abstractHorse.getX(), abstractHorse.getY(), abstractHorse.getZ());
			skeleton.invulnerableTime = 60;
			skeleton.setPersistenceRequired();
			if (skeleton.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
				skeleton.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
			}

			this.enchant(skeleton, EquipmentSlot.MAINHAND, difficultyInstance);
			this.enchant(skeleton, EquipmentSlot.HEAD, difficultyInstance);
		}

		return skeleton;
	}

	private void enchant(Skeleton skeleton, EquipmentSlot equipmentSlot, DifficultyInstance difficultyInstance) {
		ItemStack itemStack = skeleton.getItemBySlot(equipmentSlot);
		itemStack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
		EnchantmentHelper.enchantItemFromProvider(
			itemStack, skeleton.level().registryAccess(), VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, difficultyInstance, skeleton.getRandom()
		);
		skeleton.setItemSlot(equipmentSlot, itemStack);
	}
}

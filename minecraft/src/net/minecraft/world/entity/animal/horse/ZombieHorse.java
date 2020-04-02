package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;

public class ZombieHorse extends AbstractHorse {
	public ZombieHorse(EntityType<? extends ZombieHorse> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return createBaseHorseAttributes().add(Attributes.MAX_HEALTH, 15.0).add(Attributes.MOVEMENT_SPEED, 0.2F);
	}

	@Override
	protected void randomizeAttributes() {
		this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(this.generateRandomJumpStrength());
	}

	@Override
	public MobType getMobType() {
		return MobType.UNDEAD;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		super.getAmbientSound();
		return SoundEvents.ZOMBIE_HORSE_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		super.getDeathSound();
		return SoundEvents.ZOMBIE_HORSE_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		super.getHurtSound(damageSource);
		return SoundEvents.ZOMBIE_HORSE_HURT;
	}

	@Nullable
	@Override
	public AgableMob getBreedOffspring(AgableMob agableMob) {
		return EntityType.ZOMBIE_HORSE.create(this.level);
	}

	@Override
	public boolean mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (itemStack.getItem() instanceof SpawnEggItem) {
			return super.mobInteract(player, interactionHand);
		} else if (!this.isTamed()) {
			return false;
		} else if (this.isBaby()) {
			return super.mobInteract(player, interactionHand);
		} else if (player.isSecondaryUseActive()) {
			this.openInventory(player);
			return true;
		} else if (this.isVehicle()) {
			return super.mobInteract(player, interactionHand);
		} else {
			if (!itemStack.isEmpty()) {
				if (!this.isSaddled() && itemStack.getItem() == Items.SADDLE) {
					this.openInventory(player);
					return true;
				}

				if (itemStack.interactEnemy(player, this, interactionHand)) {
					return true;
				}
			}

			this.doPlayerRide(player);
			return true;
		}
	}

	@Override
	protected void addBehaviourGoals() {
	}
}

package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
	public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (!this.isTamed()) {
			return InteractionResult.PASS;
		} else if (this.isBaby()) {
			return super.mobInteract(player, interactionHand);
		} else if (player.isSecondaryUseActive()) {
			this.openInventory(player);
			return InteractionResult.sidedSuccess(this.level.isClientSide);
		} else if (this.isVehicle()) {
			return super.mobInteract(player, interactionHand);
		} else {
			if (!itemStack.isEmpty()) {
				if (itemStack.getItem() == Items.SADDLE && !this.isSaddled()) {
					this.openInventory(player);
					return InteractionResult.sidedSuccess(this.level.isClientSide);
				}

				InteractionResult interactionResult = itemStack.interactLivingEntity(player, this, interactionHand);
				if (interactionResult.consumesAction()) {
					return interactionResult;
				}
			}

			this.doPlayerRide(player);
			return InteractionResult.sidedSuccess(this.level.isClientSide);
		}
	}

	@Override
	protected void addBehaviourGoals() {
	}
}

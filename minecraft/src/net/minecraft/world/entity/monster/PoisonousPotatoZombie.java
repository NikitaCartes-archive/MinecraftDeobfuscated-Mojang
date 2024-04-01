package net.minecraft.world.entity.monster;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class PoisonousPotatoZombie extends Zombie {
	public PoisonousPotatoZombie(EntityType<? extends PoisonousPotatoZombie> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Zombie.createAttributes().add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.FOLLOW_RANGE, 8.0);
	}

	@Override
	protected void registerGoals() {
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Zombie.class, true, livingEntity -> !(livingEntity instanceof PoisonousPotatoZombie)));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true, livingEntity -> !this.isUsingZombieHat((Player)livingEntity)));
		this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
		this.addBehaviourGoals();
	}

	@Override
	protected void addBehaviourGoals() {
		this.goalSelector.addGoal(2, new ZombieAttackGoal<>(this, 1.0, false));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0, true, 4, this::canBreakDoors));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
	}

	private boolean isUsingZombieHat(Player player) {
		ItemStack itemStack = player.getInventory().armor.get(3);
		return itemStack.is(Blocks.POTATO_ZOMBIE_HEAD_HAT.asItem());
	}

	@Override
	public boolean killedEntity(ServerLevel serverLevel, LivingEntity livingEntity) {
		boolean bl = super.killedEntity(serverLevel, livingEntity);
		if (livingEntity instanceof Zombie zombie) {
			PoisonousPotatoZombie poisonousPotatoZombie = zombie.convertTo(EntityType.POISONOUS_POTATO_ZOMBIE, false);
			if (poisonousPotatoZombie != null) {
				poisonousPotatoZombie.finalizeSpawn(
					serverLevel, serverLevel.getCurrentDifficultyAt(poisonousPotatoZombie.blockPosition()), MobSpawnType.CONVERSION, new Zombie.ZombieGroupData(false, true)
				);
				if (!this.isSilent()) {
					serverLevel.levelEvent(null, 1051, this.blockPosition(), 0);
				}

				bl = false;
			}
		}

		return bl;
	}

	@Override
	public boolean isPotato() {
		return true;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.POTATO_ZOMBIE_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.POTATO_ZOMBIE_DEATH;
	}

	@Override
	protected SoundEvent getStepSound() {
		return SoundEvents.POTATO_ZOMBIE_STEP;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.POTATO_ZOMBIE_HURT;
	}

	@Override
	protected boolean convertsInWater() {
		return false;
	}
}

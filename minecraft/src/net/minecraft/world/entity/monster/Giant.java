package net.minecraft.world.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

public class Giant extends Monster {
	public Giant(EntityType<? extends Giant> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 100.0)
			.add(Attributes.MOVEMENT_SPEED, 0.5)
			.add(Attributes.ATTACK_DAMAGE, 20.0)
			.add(Attributes.ATTACK_KNOCKBACK, 50.0);
	}

	@Override
	public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
		return levelReader.getPathfindingCostFromLightLevels(blockPos);
	}

	@Override
	public boolean hasPotatoVariant() {
		return true;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(2, new ZombieAttackGoal<>(this, 1.0, false));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(ZombifiedPiglin.class));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, AbstractVillager.class, false));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, true));
		this.targetSelector.addGoal(5, new NearestAttackableTargetGoal(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		boolean bl = super.doHurtTarget(entity);
		if (bl) {
			float f = this.level().getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
			if (this.getMainHandItem().isEmpty() && this.isOnFire() && this.random.nextFloat() < f * 0.3F) {
				entity.igniteForSeconds(2 * (int)f);
			}
		}

		return bl;
	}
}

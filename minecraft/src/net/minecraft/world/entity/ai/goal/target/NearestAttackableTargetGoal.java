package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class NearestAttackableTargetGoal<T extends LivingEntity> extends TargetGoal {
	protected final Class<T> targetType;
	protected final int randomInterval;
	protected LivingEntity target;
	protected TargetingConditions targetConditions;

	public NearestAttackableTargetGoal(Mob mob, Class<T> class_, boolean bl) {
		this(mob, class_, bl, false);
	}

	public NearestAttackableTargetGoal(Mob mob, Class<T> class_, boolean bl, boolean bl2) {
		this(mob, class_, 10, bl, bl2, null);
	}

	public NearestAttackableTargetGoal(Mob mob, Class<T> class_, int i, boolean bl, boolean bl2, @Nullable Predicate<LivingEntity> predicate) {
		super(mob, bl, bl2);
		this.targetType = class_;
		this.randomInterval = i;
		this.setFlags(EnumSet.of(Goal.Flag.TARGET));
		this.targetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(predicate);
	}

	@Override
	public boolean canUse() {
		if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
			return false;
		} else {
			this.findTarget();
			return this.target != null;
		}
	}

	protected AABB getTargetSearchArea(double d) {
		return this.mob.getBoundingBox().inflate(d, 4.0, d);
	}

	protected void findTarget() {
		if (this.targetType != Player.class && this.targetType != ServerPlayer.class) {
			this.target = this.mob
				.level
				.getNearestEntity(
					this.mob.level.getEntitiesOfClass(this.targetType, this.getTargetSearchArea(this.getFollowDistance()), livingEntity -> true),
					this.targetConditions,
					this.mob,
					this.mob.getX(),
					this.mob.getEyeY(),
					this.mob.getZ()
				);
		} else {
			this.target = this.mob.level.getNearestPlayer(this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
		}
	}

	@Override
	public void start() {
		this.mob.setTarget(this.target);
		super.start();
	}

	public void setTarget(@Nullable LivingEntity livingEntity) {
		this.target = livingEntity;
	}
}

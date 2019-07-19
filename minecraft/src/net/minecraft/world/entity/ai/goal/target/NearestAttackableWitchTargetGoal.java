package net.minecraft.world.entity.ai.goal.target;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raider;

public class NearestAttackableWitchTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
	private boolean canAttack = true;

	public NearestAttackableWitchTargetGoal(Raider raider, Class<T> class_, int i, boolean bl, boolean bl2, @Nullable Predicate<LivingEntity> predicate) {
		super(raider, class_, i, bl, bl2, predicate);
	}

	public void setCanAttack(boolean bl) {
		this.canAttack = bl;
	}

	@Override
	public boolean canUse() {
		return this.canAttack && super.canUse();
	}
}

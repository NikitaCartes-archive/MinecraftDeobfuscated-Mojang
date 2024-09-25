package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

public class DragonSittingAttackingPhase extends AbstractDragonSittingPhase {
	private static final int ROAR_DURATION = 40;
	private int attackingTicks;

	public DragonSittingAttackingPhase(EnderDragon enderDragon) {
		super(enderDragon);
	}

	@Override
	public void doClientTick() {
		this.dragon
			.level()
			.playLocalSound(
				this.dragon.getX(),
				this.dragon.getY(),
				this.dragon.getZ(),
				SoundEvents.ENDER_DRAGON_GROWL,
				this.dragon.getSoundSource(),
				2.5F,
				0.8F + this.dragon.getRandom().nextFloat() * 0.3F,
				false
			);
	}

	@Override
	public void doServerTick(ServerLevel serverLevel) {
		if (this.attackingTicks++ >= 40) {
			this.dragon.getPhaseManager().setPhase(EnderDragonPhase.SITTING_FLAMING);
		}
	}

	@Override
	public void begin() {
		this.attackingTicks = 0;
	}

	@Override
	public EnderDragonPhase<DragonSittingAttackingPhase> getPhase() {
		return EnderDragonPhase.SITTING_ATTACKING;
	}
}

package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.phys.Vec3;

public class DragonSittingScanningPhase extends AbstractDragonSittingPhase {
	private static final int SITTING_SCANNING_IDLE_TICKS = 100;
	private static final int SITTING_ATTACK_Y_VIEW_RANGE = 10;
	private static final int SITTING_ATTACK_VIEW_RANGE = 20;
	private static final int SITTING_CHARGE_VIEW_RANGE = 150;
	private static final TargetingConditions CHARGE_TARGETING = TargetingConditions.forCombat().range(150.0);
	private final TargetingConditions scanTargeting;
	private int scanningTime;

	public DragonSittingScanningPhase(EnderDragon enderDragon) {
		super(enderDragon);
		this.scanTargeting = TargetingConditions.forCombat().range(20.0).selector(livingEntity -> Math.abs(livingEntity.getY() - enderDragon.getY()) <= 10.0);
	}

	@Override
	public void doServerTick() {
		this.scanningTime++;
		LivingEntity livingEntity = this.dragon.level.getNearestPlayer(this.scanTargeting, this.dragon, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
		if (livingEntity != null) {
			if (this.scanningTime > 25) {
				this.dragon.getPhaseManager().setPhase(EnderDragonPhase.SITTING_ATTACKING);
			} else {
				Vec3 vec3 = new Vec3(livingEntity.getX() - this.dragon.getX(), 0.0, livingEntity.getZ() - this.dragon.getZ()).normalize();
				Vec3 vec32 = new Vec3(
						(double)Mth.sin(this.dragon.getYRot() * (float) (Math.PI / 180.0)), 0.0, (double)(-Mth.cos(this.dragon.getYRot() * (float) (Math.PI / 180.0)))
					)
					.normalize();
				float f = (float)vec32.dot(vec3);
				float g = (float)(Math.acos((double)f) * 180.0F / (float)Math.PI) + 0.5F;
				if (g < 0.0F || g > 10.0F) {
					double d = livingEntity.getX() - this.dragon.head.getX();
					double e = livingEntity.getZ() - this.dragon.head.getZ();
					double h = Mth.clamp(Mth.wrapDegrees(180.0 - Mth.atan2(d, e) * 180.0F / (float)Math.PI - (double)this.dragon.getYRot()), -100.0, 100.0);
					this.dragon.yRotA *= 0.8F;
					float i = (float)Math.sqrt(d * d + e * e) + 1.0F;
					float j = i;
					if (i > 40.0F) {
						i = 40.0F;
					}

					this.dragon.yRotA = (float)((double)this.dragon.yRotA + h * (double)(0.7F / i / j));
					this.dragon.setYRot(this.dragon.getYRot() + this.dragon.yRotA);
				}
			}
		} else if (this.scanningTime >= 100) {
			livingEntity = this.dragon.level.getNearestPlayer(CHARGE_TARGETING, this.dragon, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
			this.dragon.getPhaseManager().setPhase(EnderDragonPhase.TAKEOFF);
			if (livingEntity != null) {
				this.dragon.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
				this.dragon.getPhaseManager().getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(new Vec3(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ()));
			}
		}
	}

	@Override
	public void begin() {
		this.scanningTime = 0;
	}

	@Override
	public EnderDragonPhase<DragonSittingScanningPhase> getPhase() {
		return EnderDragonPhase.SITTING_SCANNING;
	}
}

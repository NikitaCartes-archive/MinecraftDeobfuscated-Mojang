package net.minecraft.world.entity.boss.enderdragon.phases;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.phys.Vec3;

public class DragonSittingFlamingPhase extends AbstractDragonSittingPhase {
	private int flameTicks;
	private int flameCount;
	private AreaEffectCloud flame;

	public DragonSittingFlamingPhase(EnderDragon enderDragon) {
		super(enderDragon);
	}

	@Override
	public void doClientTick() {
		this.flameTicks++;
		if (this.flameTicks % 2 == 0 && this.flameTicks < 10) {
			Vec3 vec3 = this.dragon.getHeadLookVector(1.0F).normalize();
			vec3.yRot((float) (-Math.PI / 4));
			double d = this.dragon.head.getX();
			double e = this.dragon.head.getY(0.5);
			double f = this.dragon.head.getZ();

			for (int i = 0; i < 8; i++) {
				double g = d + this.dragon.getRandom().nextGaussian() / 2.0;
				double h = e + this.dragon.getRandom().nextGaussian() / 2.0;
				double j = f + this.dragon.getRandom().nextGaussian() / 2.0;

				for (int k = 0; k < 6; k++) {
					this.dragon.level.addParticle(ParticleTypes.DRAGON_BREATH, g, h, j, -vec3.x * 0.08F * (double)k, -vec3.y * 0.6F, -vec3.z * 0.08F * (double)k);
				}

				vec3.yRot((float) (Math.PI / 16));
			}
		}
	}

	@Override
	public void doServerTick() {
		this.flameTicks++;
		if (this.flameTicks >= 200) {
			if (this.flameCount >= 4) {
				this.dragon.getPhaseManager().setPhase(EnderDragonPhase.TAKEOFF);
			} else {
				this.dragon.getPhaseManager().setPhase(EnderDragonPhase.SITTING_SCANNING);
			}
		} else if (this.flameTicks == 10) {
			Vec3 vec3 = new Vec3(this.dragon.head.getX() - this.dragon.getX(), 0.0, this.dragon.head.getZ() - this.dragon.getZ()).normalize();
			float f = 5.0F;
			double d = this.dragon.head.getX() + vec3.x * 5.0 / 2.0;
			double e = this.dragon.head.getZ() + vec3.z * 5.0 / 2.0;
			double g = this.dragon.head.getY(0.5);
			double h = g;
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(d, g, e);

			while (this.dragon.level.isEmptyBlock(mutableBlockPos)) {
				if (--h < 0.0) {
					h = g;
					break;
				}

				mutableBlockPos.set(d, h, e);
			}

			h = (double)(Mth.floor(h) + 1);
			this.flame = new AreaEffectCloud(this.dragon.level, d, h, e);
			this.flame.setOwner(this.dragon);
			this.flame.setRadius(5.0F);
			this.flame.setDuration(200);
			this.flame.setParticle(ParticleTypes.DRAGON_BREATH);
			this.flame.addEffect(new MobEffectInstance(MobEffects.HARM));
			this.dragon.level.addFreshEntity(this.flame);
		}
	}

	@Override
	public void begin() {
		this.flameTicks = 0;
		this.flameCount++;
	}

	@Override
	public void end() {
		if (this.flame != null) {
			this.flame.remove();
			this.flame = null;
		}
	}

	@Override
	public EnderDragonPhase<DragonSittingFlamingPhase> getPhase() {
		return EnderDragonPhase.SITTING_FLAMING;
	}

	public void resetFlameCount() {
		this.flameCount = 0;
	}
}

package net.minecraft.client.renderer.item;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class CompassItemPropertyFunction implements ClampedItemPropertyFunction {
	public static final int DEFAULT_ROTATION = 0;
	private final CompassItemPropertyFunction.CompassWobble wobble = new CompassItemPropertyFunction.CompassWobble();
	private final CompassItemPropertyFunction.CompassWobble wobbleRandom = new CompassItemPropertyFunction.CompassWobble();
	public final CompassItemPropertyFunction.CompassTarget compassTarget;

	public CompassItemPropertyFunction(CompassItemPropertyFunction.CompassTarget compassTarget) {
		this.compassTarget = compassTarget;
	}

	@Override
	public float unclampedCall(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i) {
		Entity entity = (Entity)(livingEntity != null ? livingEntity : itemStack.getEntityRepresentation());
		if (entity == null) {
			return 0.0F;
		} else {
			clientLevel = this.tryFetchLevelIfMissing(entity, clientLevel);
			return clientLevel == null ? 0.0F : this.getCompassRotation(itemStack, clientLevel, i, entity);
		}
	}

	private float getCompassRotation(ItemStack itemStack, ClientLevel clientLevel, int i, Entity entity) {
		GlobalPos globalPos = this.compassTarget.getPos(clientLevel, itemStack, entity);
		long l = clientLevel.getGameTime();
		return !this.isValidCompassTargetPos(entity, globalPos)
			? this.getRandomlySpinningRotation(i, l)
			: this.getRotationTowardsCompassTarget(entity, l, globalPos.pos());
	}

	private float getRandomlySpinningRotation(int i, long l) {
		if (this.wobbleRandom.shouldUpdate(l)) {
			this.wobbleRandom.update(l, Math.random());
		}

		double d = this.wobbleRandom.rotation + (double)((float)this.hash(i) / 2.1474836E9F);
		return Mth.positiveModulo((float)d, 1.0F);
	}

	private float getRotationTowardsCompassTarget(Entity entity, long l, BlockPos blockPos) {
		double d = this.getAngleFromEntityToPos(entity, blockPos);
		double e = this.getWrappedVisualRotationY(entity);
		if (entity instanceof Player player && player.isLocalPlayer()) {
			if (this.wobble.shouldUpdate(l)) {
				this.wobble.update(l, 0.5 - (e - 0.25));
			}

			double f = d + this.wobble.rotation;
			return Mth.positiveModulo((float)f, 1.0F);
		}

		double f = 0.5 - (e - 0.25 - d);
		return Mth.positiveModulo((float)f, 1.0F);
	}

	@Nullable
	private ClientLevel tryFetchLevelIfMissing(Entity entity, @Nullable ClientLevel clientLevel) {
		return clientLevel == null && entity.level() instanceof ClientLevel ? (ClientLevel)entity.level() : clientLevel;
	}

	private boolean isValidCompassTargetPos(Entity entity, @Nullable GlobalPos globalPos) {
		return globalPos != null && globalPos.dimension() == entity.level().dimension() && !(globalPos.pos().distToCenterSqr(entity.position()) < 1.0E-5F);
	}

	private double getAngleFromEntityToPos(Entity entity, BlockPos blockPos) {
		Vec3 vec3 = Vec3.atCenterOf(blockPos);
		return Math.atan2(vec3.z() - entity.getZ(), vec3.x() - entity.getX()) / (float) (Math.PI * 2);
	}

	private double getWrappedVisualRotationY(Entity entity) {
		return Mth.positiveModulo((double)(entity.getVisualRotationYInDegrees() / 360.0F), 1.0);
	}

	private int hash(int i) {
		return i * 1327217883;
	}

	@Environment(EnvType.CLIENT)
	public interface CompassTarget {
		@Nullable
		GlobalPos getPos(ClientLevel clientLevel, ItemStack itemStack, Entity entity);
	}

	@Environment(EnvType.CLIENT)
	static class CompassWobble {
		double rotation;
		private double deltaRotation;
		private long lastUpdateTick;

		boolean shouldUpdate(long l) {
			return this.lastUpdateTick != l;
		}

		void update(long l, double d) {
			this.lastUpdateTick = l;
			double e = d - this.rotation;
			e = Mth.positiveModulo(e + 0.5, 1.0) - 0.5;
			this.deltaRotation += e * 0.1;
			this.deltaRotation *= 0.8;
			this.rotation = Mth.positiveModulo(this.rotation + this.deltaRotation, 1.0);
		}
	}
}

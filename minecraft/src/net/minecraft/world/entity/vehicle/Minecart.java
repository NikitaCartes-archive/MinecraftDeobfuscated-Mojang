package net.minecraft.world.entity.vehicle;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Minecart extends AbstractMinecart {
	private float rotationOffset;
	private float playerRotationOffset;

	public Minecart(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	public Minecart(Level level, double d, double e, double f) {
		super(EntityType.MINECART, level, d, e, f);
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand interactionHand) {
		if (!player.isSecondaryUseActive() && !this.isVehicle() && (this.level().isClientSide || player.startRiding(this))) {
			this.playerRotationOffset = this.rotationOffset;
			if (!this.level().isClientSide) {
				return (InteractionResult)(player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS);
			} else {
				return InteractionResult.SUCCESS;
			}
		} else {
			return InteractionResult.PASS;
		}
	}

	@Override
	protected Item getDropItem() {
		return Items.MINECART;
	}

	@Override
	public void activateMinecart(int i, int j, int k, boolean bl) {
		if (bl) {
			if (this.isVehicle()) {
				this.ejectPassengers();
			}

			if (this.getHurtTime() == 0) {
				this.setHurtDir(-this.getHurtDir());
				this.setHurtTime(10);
				this.setDamage(50.0F);
				this.markHurt();
			}
		}
	}

	@Override
	public AbstractMinecart.Type getMinecartType() {
		return AbstractMinecart.Type.RIDEABLE;
	}

	@Override
	public void tick() {
		double d = (double)this.getYRot();
		Vec3 vec3 = this.position();
		super.tick();
		double e = ((double)this.getYRot() - d) % 360.0;
		if (this.level().isClientSide && vec3.distanceTo(this.position()) > 0.01) {
			this.rotationOffset += (float)e;
			this.rotationOffset %= 360.0F;
		}
	}

	@Override
	protected void positionRider(Entity entity, Entity.MoveFunction moveFunction) {
		super.positionRider(entity, moveFunction);
		if (this.level().isClientSide && entity instanceof Player player && player.shouldRotateWithMinecart() && useExperimentalMovement(this.level())) {
			float f = (float)Mth.rotLerp(0.5, (double)this.playerRotationOffset, (double)this.rotationOffset);
			player.setYRot(player.getYRot() - (f - this.playerRotationOffset));
			this.playerRotationOffset = f;
		}
	}
}

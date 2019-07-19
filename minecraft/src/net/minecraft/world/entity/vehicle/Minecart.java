package net.minecraft.world.entity.vehicle;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class Minecart extends AbstractMinecart {
	public Minecart(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	public Minecart(Level level, double d, double e, double f) {
		super(EntityType.MINECART, level, d, e, f);
	}

	@Override
	public boolean interact(Player player, InteractionHand interactionHand) {
		if (player.isSneaking()) {
			return false;
		} else if (this.isVehicle()) {
			return true;
		} else {
			if (!this.level.isClientSide) {
				player.startRiding(this);
			}

			return true;
		}
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
}

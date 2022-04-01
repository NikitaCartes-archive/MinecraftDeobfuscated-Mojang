package net.minecraft.world.entity.vehicle;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Minecart extends AbstractMinecart {
	public Minecart(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	public Minecart(Level level, double d, double e, double f) {
		super(EntityType.MINECART, level, d, e, f);
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand interactionHand) {
		if (player.isSecondaryUseActive()) {
			return InteractionResult.PASS;
		} else {
			BlockState blockState = player.getCarriedBlock();
			BlockState blockState2 = this.getDisplayBlockState();
			if (blockState2.isAir() && blockState != null) {
				player.setCarriedBlock(null);
				this.setDisplayBlockState(blockState);
				return InteractionResult.SUCCESS;
			} else if (!blockState2.isAir() && blockState == null) {
				player.setCarriedBlock(blockState2);
				this.setDisplayBlockState(Blocks.AIR.defaultBlockState());
				this.setCustomDisplay(false);
				return InteractionResult.SUCCESS;
			} else if (this.isVehicle()) {
				return InteractionResult.PASS;
			} else if (!this.level.isClientSide) {
				return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
			} else {
				return InteractionResult.SUCCESS;
			}
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

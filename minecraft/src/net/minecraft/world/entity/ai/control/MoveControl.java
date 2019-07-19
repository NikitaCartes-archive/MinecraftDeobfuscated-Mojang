package net.minecraft.world.entity.ai.control;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MoveControl {
	protected final Mob mob;
	protected double wantedX;
	protected double wantedY;
	protected double wantedZ;
	protected double speedModifier;
	protected float strafeForwards;
	protected float strafeRight;
	protected MoveControl.Operation operation = MoveControl.Operation.WAIT;

	public MoveControl(Mob mob) {
		this.mob = mob;
	}

	public boolean hasWanted() {
		return this.operation == MoveControl.Operation.MOVE_TO;
	}

	public double getSpeedModifier() {
		return this.speedModifier;
	}

	public void setWantedPosition(double d, double e, double f, double g) {
		this.wantedX = d;
		this.wantedY = e;
		this.wantedZ = f;
		this.speedModifier = g;
		if (this.operation != MoveControl.Operation.JUMPING) {
			this.operation = MoveControl.Operation.MOVE_TO;
		}
	}

	public void strafe(float f, float g) {
		this.operation = MoveControl.Operation.STRAFE;
		this.strafeForwards = f;
		this.strafeRight = g;
		this.speedModifier = 0.25;
	}

	public void tick() {
		if (this.operation == MoveControl.Operation.STRAFE) {
			float f = (float)this.mob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue();
			float g = (float)this.speedModifier * f;
			float h = this.strafeForwards;
			float i = this.strafeRight;
			float j = Mth.sqrt(h * h + i * i);
			if (j < 1.0F) {
				j = 1.0F;
			}

			j = g / j;
			h *= j;
			i *= j;
			float k = Mth.sin(this.mob.yRot * (float) (Math.PI / 180.0));
			float l = Mth.cos(this.mob.yRot * (float) (Math.PI / 180.0));
			float m = h * l - i * k;
			float n = i * l + h * k;
			PathNavigation pathNavigation = this.mob.getNavigation();
			if (pathNavigation != null) {
				NodeEvaluator nodeEvaluator = pathNavigation.getNodeEvaluator();
				if (nodeEvaluator != null
					&& nodeEvaluator.getBlockPathType(this.mob.level, Mth.floor(this.mob.x + (double)m), Mth.floor(this.mob.y), Mth.floor(this.mob.z + (double)n))
						!= BlockPathTypes.WALKABLE) {
					this.strafeForwards = 1.0F;
					this.strafeRight = 0.0F;
					g = f;
				}
			}

			this.mob.setSpeed(g);
			this.mob.setZza(this.strafeForwards);
			this.mob.setXxa(this.strafeRight);
			this.operation = MoveControl.Operation.WAIT;
		} else if (this.operation == MoveControl.Operation.MOVE_TO) {
			this.operation = MoveControl.Operation.WAIT;
			double d = this.wantedX - this.mob.x;
			double e = this.wantedZ - this.mob.z;
			double o = this.wantedY - this.mob.y;
			double p = d * d + o * o + e * e;
			if (p < 2.5000003E-7F) {
				this.mob.setZza(0.0F);
				return;
			}

			float n = (float)(Mth.atan2(e, d) * 180.0F / (float)Math.PI) - 90.0F;
			this.mob.yRot = this.rotlerp(this.mob.yRot, n, 90.0F);
			this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue()));
			BlockPos blockPos = new BlockPos(this.mob);
			BlockState blockState = this.mob.level.getBlockState(blockPos);
			Block block = blockState.getBlock();
			VoxelShape voxelShape = blockState.getCollisionShape(this.mob.level, blockPos);
			if (o > (double)this.mob.maxUpStep && d * d + e * e < (double)Math.max(1.0F, this.mob.getBbWidth())
				|| !voxelShape.isEmpty()
					&& this.mob.y < voxelShape.max(Direction.Axis.Y) + (double)blockPos.getY()
					&& !block.is(BlockTags.DOORS)
					&& !block.is(BlockTags.FENCES)) {
				this.mob.getJumpControl().jump();
				this.operation = MoveControl.Operation.JUMPING;
			}
		} else if (this.operation == MoveControl.Operation.JUMPING) {
			this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue()));
			if (this.mob.onGround) {
				this.operation = MoveControl.Operation.WAIT;
			}
		} else {
			this.mob.setZza(0.0F);
		}
	}

	protected float rotlerp(float f, float g, float h) {
		float i = Mth.wrapDegrees(g - f);
		if (i > h) {
			i = h;
		}

		if (i < -h) {
			i = -h;
		}

		float j = f + i;
		if (j < 0.0F) {
			j += 360.0F;
		} else if (j > 360.0F) {
			j -= 360.0F;
		}

		return j;
	}

	public double getWantedX() {
		return this.wantedX;
	}

	public double getWantedY() {
		return this.wantedY;
	}

	public double getWantedZ() {
		return this.wantedZ;
	}

	public static enum Operation {
		WAIT,
		MOVE_TO,
		STRAFE,
		JUMPING;
	}
}

package net.minecraft.world.entity.vehicle;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class NewMinecartBehavior extends MinecartBehavior {
	public static final int POS_ROT_LERP_TICKS = 3;
	public static final double ON_RAIL_Y_OFFSET = 0.1;
	public static final double OPPOSING_SLOPES_REST_AT_SPEED_THRESHOLD = 0.005;
	@Nullable
	private NewMinecartBehavior.StepPartialTicks cacheIndexAlpha;
	private int cachedLerpDelay;
	private float cachedPartialTick;
	private int lerpDelay = 0;
	public final List<NewMinecartBehavior.MinecartStep> lerpSteps = new LinkedList();
	public final List<NewMinecartBehavior.MinecartStep> currentLerpSteps = new LinkedList();
	public double currentLerpStepsTotalWeight = 0.0;
	public NewMinecartBehavior.MinecartStep oldLerp = NewMinecartBehavior.MinecartStep.ZERO;

	public NewMinecartBehavior(AbstractMinecart abstractMinecart) {
		super(abstractMinecart);
	}

	@Override
	public void tick() {
		if (this.level().isClientSide) {
			this.lerpClientPositionAndRotation();
			boolean bl = BaseRailBlock.isRail(this.level().getBlockState(this.minecart.getCurrentBlockPosOrRailBelow()));
			this.minecart.setOnRails(bl);
		} else {
			BlockPos blockPos = this.minecart.getCurrentBlockPosOrRailBelow();
			BlockState blockState = this.level().getBlockState(blockPos);
			if (this.minecart.isFirstTick()) {
				this.minecart.setOnRails(BaseRailBlock.isRail(blockState));
				this.adjustToRails(blockPos, blockState, true);
			}

			this.minecart.applyGravity();
			this.minecart.moveAlongTrack();
		}
	}

	private void lerpClientPositionAndRotation() {
		if (--this.lerpDelay <= 0) {
			this.setOldLerpValues();
			this.currentLerpSteps.clear();
			if (!this.lerpSteps.isEmpty()) {
				this.currentLerpSteps.addAll(this.lerpSteps);
				this.lerpSteps.clear();
				this.currentLerpStepsTotalWeight = 0.0;

				for (NewMinecartBehavior.MinecartStep minecartStep : this.currentLerpSteps) {
					this.currentLerpStepsTotalWeight = this.currentLerpStepsTotalWeight + (double)minecartStep.weight;
				}

				this.lerpDelay = this.currentLerpStepsTotalWeight == 0.0 ? 0 : 3;
			}
		}

		if (this.cartHasPosRotLerp()) {
			this.setPos(this.getCartLerpPosition(1.0F));
			this.setDeltaMovement(this.getCartLerpMovements(1.0F));
			this.setXRot(this.getCartLerpXRot(1.0F));
			this.setYRot(this.getCartLerpYRot(1.0F));
		}
	}

	public void setOldLerpValues() {
		this.oldLerp = new NewMinecartBehavior.MinecartStep(this.position(), this.getDeltaMovement(), this.getYRot(), this.getXRot(), 0.0F);
	}

	public boolean cartHasPosRotLerp() {
		return !this.currentLerpSteps.isEmpty();
	}

	public float getCartLerpXRot(float f) {
		NewMinecartBehavior.StepPartialTicks stepPartialTicks = this.getCurrentLerpStep(f);
		return Mth.rotLerp(stepPartialTicks.partialTicksInStep, stepPartialTicks.previousStep.xRot, stepPartialTicks.currentStep.xRot);
	}

	public float getCartLerpYRot(float f) {
		NewMinecartBehavior.StepPartialTicks stepPartialTicks = this.getCurrentLerpStep(f);
		return Mth.rotLerp(stepPartialTicks.partialTicksInStep, stepPartialTicks.previousStep.yRot, stepPartialTicks.currentStep.yRot);
	}

	public Vec3 getCartLerpPosition(float f) {
		NewMinecartBehavior.StepPartialTicks stepPartialTicks = this.getCurrentLerpStep(f);
		return Mth.lerp((double)stepPartialTicks.partialTicksInStep, stepPartialTicks.previousStep.position, stepPartialTicks.currentStep.position);
	}

	public Vec3 getCartLerpMovements(float f) {
		NewMinecartBehavior.StepPartialTicks stepPartialTicks = this.getCurrentLerpStep(f);
		return Mth.lerp((double)stepPartialTicks.partialTicksInStep, stepPartialTicks.previousStep.movement, stepPartialTicks.currentStep.movement);
	}

	private NewMinecartBehavior.StepPartialTicks getCurrentLerpStep(float f) {
		if (f == this.cachedPartialTick && this.lerpDelay == this.cachedLerpDelay && this.cacheIndexAlpha != null) {
			return this.cacheIndexAlpha;
		} else {
			float g = ((float)(3 - this.lerpDelay) + f) / 3.0F;
			float h = 0.0F;
			float i = 1.0F;
			boolean bl = false;

			int j;
			for (j = 0; j < this.currentLerpSteps.size(); j++) {
				float k = ((NewMinecartBehavior.MinecartStep)this.currentLerpSteps.get(j)).weight;
				if (!(k <= 0.0F)) {
					h += k;
					if ((double)h >= this.currentLerpStepsTotalWeight * (double)g) {
						float l = h - k;
						i = (float)(((double)g * this.currentLerpStepsTotalWeight - (double)l) / (double)k);
						bl = true;
						break;
					}
				}
			}

			if (!bl) {
				j = this.currentLerpSteps.size() - 1;
			}

			NewMinecartBehavior.MinecartStep minecartStep = (NewMinecartBehavior.MinecartStep)this.currentLerpSteps.get(j);
			NewMinecartBehavior.MinecartStep minecartStep2 = j > 0 ? (NewMinecartBehavior.MinecartStep)this.currentLerpSteps.get(j - 1) : this.oldLerp;
			this.cacheIndexAlpha = new NewMinecartBehavior.StepPartialTicks(i, minecartStep, minecartStep2);
			this.cachedLerpDelay = this.lerpDelay;
			this.cachedPartialTick = f;
			return this.cacheIndexAlpha;
		}
	}

	public void adjustToRails(BlockPos blockPos, BlockState blockState, boolean bl) {
		if (BaseRailBlock.isRail(blockState)) {
			RailShape railShape = blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty());
			Pair<Vec3i, Vec3i> pair = AbstractMinecart.exits(railShape);
			Vec3 vec3 = new Vec3(pair.getFirst()).scale(0.5);
			Vec3 vec32 = new Vec3(pair.getSecond()).scale(0.5);
			Vec3 vec33 = vec3.horizontal();
			Vec3 vec34 = vec32.horizontal();
			if (this.getDeltaMovement().length() > 1.0E-5F && this.getDeltaMovement().dot(vec33) < this.getDeltaMovement().dot(vec34)
				|| this.isDecending(vec34, railShape)) {
				Vec3 vec35 = vec33;
				vec33 = vec34;
				vec34 = vec35;
			}

			float f = 180.0F - (float)(Math.atan2(vec33.z, vec33.x) * 180.0 / Math.PI);
			f += this.minecart.isFlipped() ? 180.0F : 0.0F;
			Vec3 vec36 = this.position();
			boolean bl2 = vec3.x() != vec32.x() && vec3.z() != vec32.z();
			Vec3 vec310;
			if (bl2) {
				Vec3 vec37 = vec32.subtract(vec3);
				Vec3 vec38 = vec36.subtract(blockPos.getBottomCenter()).subtract(vec3);
				Vec3 vec39 = vec37.scale(vec37.dot(vec38) / vec37.dot(vec37));
				vec310 = blockPos.getBottomCenter().add(vec3).add(vec39);
				f = 180.0F - (float)(Math.atan2(vec39.z, vec39.x) * 180.0 / Math.PI);
				f += this.minecart.isFlipped() ? 180.0F : 0.0F;
			} else {
				boolean bl3 = vec3.subtract(vec32).x != 0.0;
				boolean bl4 = vec3.subtract(vec32).z != 0.0;
				vec310 = new Vec3(bl4 ? blockPos.getCenter().x : vec36.x, (double)blockPos.getY(), bl3 ? blockPos.getCenter().z : vec36.z);
			}

			Vec3 vec37 = vec310.subtract(vec36);
			this.setPos(vec36.add(vec37));
			float g = 0.0F;
			boolean bl5 = vec3.y() != vec32.y();
			if (bl5) {
				Vec3 vec311 = blockPos.getBottomCenter().add(vec34);
				double d = vec311.distanceTo(this.position());
				this.setPos(this.position().add(0.0, d + 0.1, 0.0));
				g = this.minecart.isFlipped() ? 45.0F : -45.0F;
			} else {
				this.setPos(this.position().add(0.0, 0.1, 0.0));
			}

			this.setRotation(f, g);
			double e = vec36.distanceTo(this.position());
			if (e > 0.0) {
				this.lerpSteps.add(new NewMinecartBehavior.MinecartStep(this.position(), this.getDeltaMovement(), this.getYRot(), this.getXRot(), bl ? 0.0F : (float)e));
			}
		}
	}

	private void setRotation(float f, float g) {
		double d = (double)Math.abs(f - this.getYRot());
		if (d >= 175.0 && d <= 185.0) {
			this.minecart.setFlipped(!this.minecart.isFlipped());
			f -= 180.0F;
			g *= -1.0F;
		}

		g = Math.clamp(g, -45.0F, 45.0F);
		this.setXRot(g % 360.0F);
		this.setYRot(f % 360.0F);
	}

	@Override
	public void moveAlongTrack() {
		for (NewMinecartBehavior.TrackIteration trackIteration = new NewMinecartBehavior.TrackIteration();
			trackIteration.shouldIterate();
			trackIteration.firstIteration = false
		) {
			Vec3 vec3 = this.getDeltaMovement();
			BlockPos blockPos = this.minecart.getCurrentBlockPosOrRailBelow();
			BlockState blockState = this.level().getBlockState(blockPos);
			boolean bl = BaseRailBlock.isRail(blockState);
			if (this.minecart.isOnRails() != bl) {
				this.minecart.setOnRails(bl);
				this.adjustToRails(blockPos, blockState, false);
			}

			if (bl) {
				this.minecart.resetFallDistance();
				this.minecart.setOldPosAndRot();
				if (blockState.is(Blocks.ACTIVATOR_RAIL)) {
					this.minecart.activateMinecart(blockPos.getX(), blockPos.getY(), blockPos.getZ(), (Boolean)blockState.getValue(PoweredRailBlock.POWERED));
				}

				RailShape railShape = blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty());
				Vec3 vec32 = this.calculateTrackSpeed(vec3.horizontal(), trackIteration, blockPos, blockState, railShape);
				if (trackIteration.firstIteration) {
					trackIteration.movementLeft = vec32.horizontalDistance();
				} else {
					trackIteration.movementLeft = trackIteration.movementLeft + (vec32.horizontalDistance() - vec3.horizontalDistance());
				}

				this.setDeltaMovement(vec32);
				trackIteration.movementLeft = this.minecart.makeStepAlongTrack(blockPos, railShape, trackIteration.movementLeft);
			} else {
				this.minecart.comeOffTrack();
				trackIteration.movementLeft = 0.0;
			}

			Vec3 vec33 = this.position();
			Vec3 vec32 = vec33.subtract(this.minecart.oldPosition());
			double d = vec32.length();
			if (d > 1.0E-5F) {
				if (!(vec32.horizontalDistanceSqr() > 1.0E-5F)) {
					if (!this.minecart.isOnRails()) {
						this.setXRot(this.minecart.onGround() ? 0.0F : Mth.rotLerp(0.2F, this.getXRot(), 0.0F));
					}
				} else {
					float f = 180.0F - (float)(Math.atan2(vec32.z, vec32.x) * 180.0 / Math.PI);
					float g = this.minecart.onGround() && !this.minecart.isOnRails()
						? 0.0F
						: 90.0F - (float)(Math.atan2(vec32.horizontalDistance(), vec32.y) * 180.0 / Math.PI);
					f += this.minecart.isFlipped() ? 180.0F : 0.0F;
					g *= this.minecart.isFlipped() ? -1.0F : 1.0F;
					this.setRotation(f, g);
				}

				this.lerpSteps.add(new NewMinecartBehavior.MinecartStep(vec33, this.getDeltaMovement(), this.getYRot(), this.getXRot(), (float)d));
			} else if (vec3.horizontalDistanceSqr() > 0.0) {
				this.lerpSteps.add(new NewMinecartBehavior.MinecartStep(vec33, this.getDeltaMovement(), this.getYRot(), this.getXRot(), (float)vec3.length()));
			}

			if (d > 1.0E-5F || trackIteration.firstIteration) {
				this.minecart.applyEffectsFromBlocks();
			}
		}
	}

	private Vec3 calculateTrackSpeed(Vec3 vec3, NewMinecartBehavior.TrackIteration trackIteration, BlockPos blockPos, BlockState blockState, RailShape railShape) {
		Vec3 vec32 = vec3;
		if (!trackIteration.hasGainedSlopeSpeed) {
			Vec3 vec33 = this.calculateSlopeSpeed(vec3, railShape);
			if (vec33.horizontalDistanceSqr() != vec3.horizontalDistanceSqr()) {
				trackIteration.hasGainedSlopeSpeed = true;
				vec32 = vec33;
			}
		}

		if (trackIteration.firstIteration) {
			Vec3 vec33 = this.calculatePlayerInputSpeed(vec32);
			if (vec33.horizontalDistanceSqr() != vec32.horizontalDistanceSqr()) {
				trackIteration.hasHalted = true;
				vec32 = vec33;
			}
		}

		if (!trackIteration.hasHalted) {
			Vec3 vec33 = this.calculateHaltTrackSpeed(vec32, blockState);
			if (vec33.horizontalDistanceSqr() != vec32.horizontalDistanceSqr()) {
				trackIteration.hasHalted = true;
				vec32 = vec33;
			}
		}

		if (trackIteration.firstIteration) {
			vec32 = this.minecart.applyNaturalSlowdown(vec32);
			if (vec32.lengthSqr() > 0.0) {
				double d = Math.min(vec32.length(), this.minecart.getMaxSpeed());
				vec32 = vec32.normalize().scale(d);
			}
		}

		if (!trackIteration.hasBoosted) {
			Vec3 vec33 = this.calculateBoostTrackSpeed(vec32, blockPos, blockState);
			if (vec33.horizontalDistanceSqr() != vec32.horizontalDistanceSqr()) {
				trackIteration.hasBoosted = true;
				vec32 = vec33;
			}
		}

		return vec32;
	}

	private Vec3 calculateSlopeSpeed(Vec3 vec3, RailShape railShape) {
		double d = Math.max(0.0078125, vec3.horizontalDistance() * 0.02);
		if (this.minecart.isInWater()) {
			d *= 0.2;
		}
		return switch (railShape) {
			case ASCENDING_EAST -> vec3.add(-d, 0.0, 0.0);
			case ASCENDING_WEST -> vec3.add(d, 0.0, 0.0);
			case ASCENDING_NORTH -> vec3.add(0.0, 0.0, d);
			case ASCENDING_SOUTH -> vec3.add(0.0, 0.0, -d);
			default -> vec3;
		};
	}

	private Vec3 calculatePlayerInputSpeed(Vec3 vec3) {
		if (this.minecart.getFirstPassenger() instanceof ServerPlayer serverPlayer) {
			Vec3 vec32 = serverPlayer.getLastClientMoveIntent();
			if (vec32.lengthSqr() > 0.0) {
				Vec3 vec33 = vec32.normalize();
				double d = vec3.horizontalDistanceSqr();
				if (vec33.lengthSqr() > 0.0 && d < 0.01) {
					return vec3.add(new Vec3(vec33.x, 0.0, vec33.z).normalize().scale(0.001));
				}
			}

			return vec3;
		} else {
			return vec3;
		}
	}

	private Vec3 calculateHaltTrackSpeed(Vec3 vec3, BlockState blockState) {
		if (blockState.is(Blocks.POWERED_RAIL) && !(Boolean)blockState.getValue(PoweredRailBlock.POWERED)) {
			return vec3.length() < 0.03 ? Vec3.ZERO : vec3.scale(0.5);
		} else {
			return vec3;
		}
	}

	private Vec3 calculateBoostTrackSpeed(Vec3 vec3, BlockPos blockPos, BlockState blockState) {
		if (blockState.is(Blocks.POWERED_RAIL) && (Boolean)blockState.getValue(PoweredRailBlock.POWERED)) {
			if (vec3.length() > 0.01) {
				return vec3.normalize().scale(vec3.length() + 0.06);
			} else {
				Vec3 vec32 = this.minecart.getRedstoneDirection(blockPos);
				return vec32.lengthSqr() <= 0.0 ? vec3 : vec32.scale(vec3.length() + 0.2);
			}
		} else {
			return vec3;
		}
	}

	@Override
	public double stepAlongTrack(BlockPos blockPos, RailShape railShape, double d) {
		if (d < 1.0E-5F) {
			return 0.0;
		} else {
			Vec3 vec3 = this.position();
			Pair<Vec3i, Vec3i> pair = AbstractMinecart.exits(railShape);
			Vec3i vec3i = pair.getFirst();
			Vec3i vec3i2 = pair.getSecond();
			Vec3 vec32 = this.getDeltaMovement().horizontal();
			if (vec32.length() < 1.0E-5F) {
				this.setDeltaMovement(Vec3.ZERO);
				return 0.0;
			} else {
				boolean bl = vec3i.getY() != vec3i2.getY();
				Vec3 vec33 = new Vec3(vec3i2).scale(0.5).horizontal();
				Vec3 vec34 = new Vec3(vec3i).scale(0.5).horizontal();
				if (vec32.dot(vec34) < vec32.dot(vec33)) {
					vec34 = vec33;
				}

				Vec3 vec35 = blockPos.getBottomCenter().add(vec34).add(0.0, 0.1, 0.0).add(vec34.normalize().scale(1.0E-5F));
				if (bl && !this.isDecending(vec32, railShape)) {
					vec35 = vec35.add(0.0, 1.0, 0.0);
				}

				Vec3 vec36 = vec35.subtract(this.position()).normalize();
				vec32 = vec36.scale(vec32.length() / vec36.horizontalDistance());
				Vec3 vec37 = vec3.add(vec32.normalize().scale(d * (double)(bl ? Mth.SQRT_OF_TWO : 1.0F)));
				if (vec3.distanceToSqr(vec35) <= vec3.distanceToSqr(vec37)) {
					d = vec35.subtract(vec37).horizontalDistance();
					vec37 = vec35;
				} else {
					d = 0.0;
				}

				this.minecart.move(MoverType.SELF, vec37.subtract(vec3));
				BlockState blockState = this.level().getBlockState(BlockPos.containing(vec37));
				if (bl) {
					if (BaseRailBlock.isRail(blockState)) {
						RailShape railShape2 = blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty());
						if (this.restAtVShape(railShape, railShape2)) {
							return 0.0;
						}
					}

					double e = vec35.horizontal().distanceTo(this.position().horizontal());
					double f = vec35.y + (this.isDecending(vec32, railShape) ? e : -e);
					if (this.position().y < f) {
						this.setPos(this.position().x, f, this.position().z);
					}
				}

				if (this.position().distanceTo(vec3) < 1.0E-5F && vec37.distanceTo(vec3) > 1.0E-5F) {
					this.setDeltaMovement(Vec3.ZERO);
					return 0.0;
				} else {
					this.setDeltaMovement(vec32);
					return d;
				}
			}
		}
	}

	private boolean restAtVShape(RailShape railShape, RailShape railShape2) {
		if (this.getDeltaMovement().lengthSqr() < 0.005
			&& railShape2.isSlope()
			&& this.isDecending(this.getDeltaMovement(), railShape)
			&& !this.isDecending(this.getDeltaMovement(), railShape2)) {
			this.setDeltaMovement(Vec3.ZERO);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public double getMaxSpeed() {
		return (double)this.level().getGameRules().getInt(GameRules.RULE_MINECART_MAX_SPEED) * (this.minecart.isInWater() ? 0.5 : 1.0) / 20.0;
	}

	private boolean isDecending(Vec3 vec3, RailShape railShape) {
		return switch (railShape) {
			case ASCENDING_EAST -> vec3.x < 0.0;
			case ASCENDING_WEST -> vec3.x > 0.0;
			case ASCENDING_NORTH -> vec3.z > 0.0;
			case ASCENDING_SOUTH -> vec3.z < 0.0;
			default -> false;
		};
	}

	@Override
	public double getSlowdownFactor() {
		return this.minecart.isVehicle() ? 0.997 : 0.975;
	}

	@Override
	public boolean pushAndPickupEntities() {
		boolean bl = this.pickupEntities(this.minecart.getBoundingBox().inflate(0.2, 0.0, 0.2));
		if (!this.minecart.horizontalCollision && !this.minecart.verticalCollision) {
			return false;
		} else {
			boolean bl2 = this.pushEntities(this.minecart.getBoundingBox().inflate(1.0E-7));
			return bl && !bl2;
		}
	}

	public boolean pickupEntities(AABB aABB) {
		if (this.minecart.getMinecartType() == AbstractMinecart.Type.RIDEABLE && !this.minecart.isVehicle()) {
			List<Entity> list = this.level().getEntities(this.minecart, aABB, EntitySelector.pushableBy(this.minecart));
			if (!list.isEmpty()) {
				for (Entity entity : list) {
					if (!(entity instanceof Player)
						&& !(entity instanceof IronGolem)
						&& !(entity instanceof AbstractMinecart)
						&& !this.minecart.isVehicle()
						&& !entity.isPassenger()) {
						boolean bl = entity.startRiding(this.minecart);
						if (bl) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public boolean pushEntities(AABB aABB) {
		boolean bl = false;
		if (this.minecart.getMinecartType() == AbstractMinecart.Type.RIDEABLE) {
			List<Entity> list = this.level().getEntities(this.minecart, aABB, EntitySelector.pushableBy(this.minecart));
			if (!list.isEmpty()) {
				for (Entity entity : list) {
					if (entity instanceof Player || entity instanceof IronGolem || entity instanceof AbstractMinecart || this.minecart.isVehicle() || entity.isPassenger()) {
						entity.push(this.minecart);
						bl = true;
					}
				}
			}
		} else {
			for (Entity entity2 : this.level().getEntities(this.minecart, aABB)) {
				if (!this.minecart.hasPassenger(entity2) && entity2.isPushable() && entity2 instanceof AbstractMinecart) {
					entity2.push(this.minecart);
					bl = true;
				}
			}
		}

		return bl;
	}

	public static record MinecartStep(Vec3 position, Vec3 movement, float yRot, float xRot, float weight) {
		public static final StreamCodec<ByteBuf, NewMinecartBehavior.MinecartStep> STREAM_CODEC = StreamCodec.composite(
			Vec3.STREAM_CODEC,
			NewMinecartBehavior.MinecartStep::position,
			Vec3.STREAM_CODEC,
			NewMinecartBehavior.MinecartStep::movement,
			ByteBufCodecs.ROTATION_BYTE,
			NewMinecartBehavior.MinecartStep::yRot,
			ByteBufCodecs.ROTATION_BYTE,
			NewMinecartBehavior.MinecartStep::xRot,
			ByteBufCodecs.FLOAT,
			NewMinecartBehavior.MinecartStep::weight,
			NewMinecartBehavior.MinecartStep::new
		);
		public static NewMinecartBehavior.MinecartStep ZERO = new NewMinecartBehavior.MinecartStep(Vec3.ZERO, Vec3.ZERO, 0.0F, 0.0F, 0.0F);
	}

	static record StepPartialTicks(float partialTicksInStep, NewMinecartBehavior.MinecartStep currentStep, NewMinecartBehavior.MinecartStep previousStep) {
	}

	static class TrackIteration {
		double movementLeft = 0.0;
		boolean firstIteration = true;
		boolean hasGainedSlopeSpeed = false;
		boolean hasHalted = false;
		boolean hasBoosted = false;

		public boolean shouldIterate() {
			return this.firstIteration || this.movementLeft > 1.0E-5F;
		}
	}
}

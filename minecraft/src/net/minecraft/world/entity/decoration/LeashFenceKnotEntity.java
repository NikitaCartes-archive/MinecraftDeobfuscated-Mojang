package net.minecraft.world.entity.decoration;

import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class LeashFenceKnotEntity extends HangingEntity {
	public LeashFenceKnotEntity(EntityType<? extends LeashFenceKnotEntity> entityType, Level level) {
		super(entityType, level);
	}

	public LeashFenceKnotEntity(Level level, BlockPos blockPos) {
		super(EntityType.LEASH_KNOT, level, blockPos);
		this.setPos((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5);
		float f = 0.125F;
		float g = 0.1875F;
		float h = 0.25F;
		this.setBoundingBox(new AABB(this.x - 0.1875, this.y - 0.25 + 0.125, this.z - 0.1875, this.x + 0.1875, this.y + 0.25 + 0.125, this.z + 0.1875));
		this.forcedLoading = true;
	}

	@Override
	public void setPos(double d, double e, double f) {
		super.setPos((double)Mth.floor(d) + 0.5, (double)Mth.floor(e) + 0.5, (double)Mth.floor(f) + 0.5);
	}

	@Override
	protected void recalculateBoundingBox() {
		this.x = (double)this.pos.getX() + 0.5;
		this.y = (double)this.pos.getY() + 0.5;
		this.z = (double)this.pos.getZ() + 0.5;
	}

	@Override
	public void setDirection(Direction direction) {
	}

	@Override
	public int getWidth() {
		return 9;
	}

	@Override
	public int getHeight() {
		return 9;
	}

	@Override
	protected float getEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return -0.0625F;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		return d < 1024.0;
	}

	@Override
	public void dropItem(@Nullable Entity entity) {
		this.playSound(SoundEvents.LEASH_KNOT_BREAK, 1.0F, 1.0F);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
	}

	@Override
	public boolean interact(Player player, InteractionHand interactionHand) {
		if (this.level.isClientSide) {
			return true;
		} else {
			boolean bl = false;
			double d = 7.0;
			List<Mob> list = this.level.getEntitiesOfClass(Mob.class, new AABB(this.x - 7.0, this.y - 7.0, this.z - 7.0, this.x + 7.0, this.y + 7.0, this.z + 7.0));

			for (Mob mob : list) {
				if (mob.getLeashHolder() == player) {
					mob.setLeashedTo(this, true);
					bl = true;
				}
			}

			if (!bl) {
				this.remove();
				if (player.abilities.instabuild) {
					for (Mob mobx : list) {
						if (mobx.isLeashed() && mobx.getLeashHolder() == this) {
							mobx.dropLeash(true, false);
						}
					}
				}
			}

			return true;
		}
	}

	@Override
	public boolean survives() {
		return this.level.getBlockState(this.pos).getBlock().is(BlockTags.FENCES);
	}

	public static LeashFenceKnotEntity getOrCreateKnot(Level level, BlockPos blockPos) {
		int i = blockPos.getX();
		int j = blockPos.getY();
		int k = blockPos.getZ();

		for (LeashFenceKnotEntity leashFenceKnotEntity : level.getEntitiesOfClass(
			LeashFenceKnotEntity.class, new AABB((double)i - 1.0, (double)j - 1.0, (double)k - 1.0, (double)i + 1.0, (double)j + 1.0, (double)k + 1.0)
		)) {
			if (leashFenceKnotEntity.getPos().equals(blockPos)) {
				return leashFenceKnotEntity;
			}
		}

		LeashFenceKnotEntity leashFenceKnotEntity2 = new LeashFenceKnotEntity(level, blockPos);
		level.addFreshEntity(leashFenceKnotEntity2);
		leashFenceKnotEntity2.playPlacementSound();
		return leashFenceKnotEntity2;
	}

	@Override
	public void playPlacementSound() {
		this.playSound(SoundEvents.LEASH_KNOT_PLACE, 1.0F, 1.0F);
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this, this.getType(), 0, this.getPos());
	}
}

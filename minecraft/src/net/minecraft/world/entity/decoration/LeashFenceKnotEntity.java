package net.minecraft.world.entity.decoration;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LeashFenceKnotEntity extends BlockAttachedEntity {
	public static final double OFFSET_Y = 0.375;

	public LeashFenceKnotEntity(EntityType<? extends LeashFenceKnotEntity> entityType, Level level) {
		super(entityType, level);
	}

	public LeashFenceKnotEntity(Level level, BlockPos blockPos) {
		super(EntityType.LEASH_KNOT, level, blockPos);
		this.setPos((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ());
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	protected void recalculateBoundingBox() {
		this.setPosRaw((double)this.pos.getX() + 0.5, (double)this.pos.getY() + 0.375, (double)this.pos.getZ() + 0.5);
		double d = (double)this.getType().getWidth() / 2.0;
		double e = (double)this.getType().getHeight();
		this.setBoundingBox(new AABB(this.getX() - d, this.getY(), this.getZ() - d, this.getX() + d, this.getY() + e, this.getZ() + d));
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		return d < 1024.0;
	}

	@Override
	public void dropItem(ServerLevel serverLevel, @Nullable Entity entity) {
		this.playSound(SoundEvents.LEASH_KNOT_BREAK, 1.0F, 1.0F);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand interactionHand) {
		if (this.level().isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			boolean bl = false;
			List<Leashable> list = LeadItem.leashableInArea(this.level(), this.getPos(), leashablex -> {
				Entity entity = leashablex.getLeashHolder();
				return entity == player || entity == this;
			});

			for (Leashable leashable : list) {
				if (leashable.getLeashHolder() == player) {
					leashable.setLeashedTo(this, true);
					bl = true;
				}
			}

			boolean bl2 = false;
			if (!bl) {
				this.discard();
				if (player.getAbilities().instabuild) {
					for (Leashable leashable2 : list) {
						if (leashable2.isLeashed() && leashable2.getLeashHolder() == this) {
							leashable2.dropLeash(true, false);
							bl2 = true;
						}
					}
				}
			}

			if (bl || bl2) {
				this.gameEvent(GameEvent.BLOCK_ATTACH, player);
			}

			return InteractionResult.SUCCESS;
		}
	}

	@Override
	public boolean survives() {
		return this.level().getBlockState(this.pos).is(BlockTags.FENCES);
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
		return leashFenceKnotEntity2;
	}

	public void playPlacementSound() {
		this.playSound(SoundEvents.LEASH_KNOT_PLACE, 1.0F, 1.0F);
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
		return new ClientboundAddEntityPacket(this, 0, this.getPos());
	}

	@Override
	public Vec3 getRopeHoldPosition(float f) {
		return this.getPosition(f).add(0.0, 0.2, 0.0);
	}

	@Override
	public ItemStack getPickResult() {
		return new ItemStack(Items.LEAD);
	}
}

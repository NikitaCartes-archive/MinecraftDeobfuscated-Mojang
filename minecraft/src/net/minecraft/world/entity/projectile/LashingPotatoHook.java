package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LashingPotatoHook extends Projectile {
	public static final EntityDataAccessor<Boolean> IN_BLOCK = SynchedEntityData.defineId(LashingPotatoHook.class, EntityDataSerializers.BOOLEAN);
	public static final EntityDataAccessor<Float> LENGTH = SynchedEntityData.defineId(LashingPotatoHook.class, EntityDataSerializers.FLOAT);
	private static final float MAX_RANGE = 100.0F;
	private static final double SPEED = 5.0;

	public LashingPotatoHook(EntityType<? extends LashingPotatoHook> entityType, Level level) {
		super(entityType, level);
		this.noCulling = true;
	}

	public LashingPotatoHook(Level level, Player player) {
		this(EntityType.LASHING_POTATO_HOOK, level);
		this.setOwner(player);
		this.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
		this.setDeltaMovement(player.getViewVector(1.0F).scale(5.0));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(IN_BLOCK, false);
		builder.define(LENGTH, 0.0F);
	}

	@Override
	public boolean shouldRenderAtSqrDistance(double d) {
		return true;
	}

	@Override
	public void lerpTo(double d, double e, double f, float g, float h, int i) {
	}

	@Override
	public void tick() {
		super.tick();
		Player player = this.getPlayerOwner();
		if (player != null && (this.level().isClientSide() || !this.shouldRetract(player))) {
			HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
			if (hitResult.getType() != HitResult.Type.MISS) {
				this.onHit(hitResult);
			}

			this.setPos(hitResult.getLocation());
			this.checkInsideBlocks();
		} else {
			this.discard();
		}
	}

	private boolean shouldRetract(Player player) {
		if (!player.isRemoved() && player.isAlive() && player.isHolding(Items.LASHING_POTATO) && !(this.distanceToSqr(player) > 10000.0)) {
			return false;
		} else {
			this.discard();
			return true;
		}
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		return false;
	}

	@Override
	protected void onHitBlock(BlockHitResult blockHitResult) {
		super.onHitBlock(blockHitResult);
		this.setDeltaMovement(Vec3.ZERO);
		this.setInBlock(true);
		Player player = this.getPlayerOwner();
		if (player != null) {
			double d = player.getEyePosition().subtract(blockHitResult.getLocation()).length();
			this.setLength(Math.max((float)d * 0.5F - 3.0F, 1.5F));
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		compoundTag.putBoolean("in_block", this.inBlock());
		compoundTag.putFloat("length", this.length());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		this.setInBlock(compoundTag.getBoolean("in_block"));
		this.setLength(compoundTag.getFloat("length"));
	}

	private void setInBlock(boolean bl) {
		this.getEntityData().set(IN_BLOCK, bl);
	}

	private void setLength(float f) {
		this.getEntityData().set(LENGTH, f);
	}

	public boolean inBlock() {
		return this.getEntityData().get(IN_BLOCK);
	}

	public float length() {
		return this.getEntityData().get(LENGTH);
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.NONE;
	}

	@Override
	public void remove(Entity.RemovalReason removalReason) {
		this.updateOwnerInfo(null);
		super.remove(removalReason);
	}

	@Override
	public void onClientRemoval() {
		this.updateOwnerInfo(null);
	}

	@Override
	public void setOwner(@Nullable Entity entity) {
		super.setOwner(entity);
		this.updateOwnerInfo(this);
	}

	private void updateOwnerInfo(@Nullable LashingPotatoHook lashingPotatoHook) {
		Player player = this.getPlayerOwner();
		if (player != null) {
			player.grappling = lashingPotatoHook;
		}
	}

	@Nullable
	public Player getPlayerOwner() {
		Entity entity = this.getOwner();
		return entity instanceof Player ? (Player)entity : null;
	}

	@Override
	public boolean canChangeDimensions() {
		return false;
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		Entity entity = this.getOwner();
		return new ClientboundAddEntityPacket(this, entity == null ? this.getId() : entity.getId());
	}

	@Override
	public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
		super.recreateFromPacket(clientboundAddEntityPacket);
		if (this.getPlayerOwner() == null) {
			this.kill();
		}
	}
}

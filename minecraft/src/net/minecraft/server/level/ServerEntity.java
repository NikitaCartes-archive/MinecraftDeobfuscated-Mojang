package net.minecraft.server.level;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ServerEntity {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int TOLERANCE_LEVEL_ROTATION = 1;
	private final ServerLevel level;
	private final Entity entity;
	private final int updateInterval;
	private final boolean trackDelta;
	private final Consumer<Packet<?>> broadcast;
	private final VecDeltaCodec positionCodec = new VecDeltaCodec();
	private int yRotp;
	private int xRotp;
	private int yHeadRotp;
	private Vec3 ap = Vec3.ZERO;
	private int tickCount;
	private int teleportDelay;
	private List<Entity> lastPassengers = Collections.emptyList();
	private boolean wasRiding;
	private boolean wasOnGround;
	@Nullable
	private List<SynchedEntityData.DataValue<?>> trackedDataValues;

	public ServerEntity(ServerLevel serverLevel, Entity entity, int i, boolean bl, Consumer<Packet<?>> consumer) {
		this.level = serverLevel;
		this.broadcast = consumer;
		this.entity = entity;
		this.updateInterval = i;
		this.trackDelta = bl;
		this.positionCodec.setBase(entity.trackingPosition());
		this.yRotp = Mth.floor(entity.getYRot() * 256.0F / 360.0F);
		this.xRotp = Mth.floor(entity.getXRot() * 256.0F / 360.0F);
		this.yHeadRotp = Mth.floor(entity.getYHeadRot() * 256.0F / 360.0F);
		this.wasOnGround = entity.isOnGround();
		this.trackedDataValues = entity.getEntityData().getNonDefaultValues();
	}

	public void sendChanges() {
		List<Entity> list = this.entity.getPassengers();
		if (!list.equals(this.lastPassengers)) {
			this.lastPassengers = list;
			this.broadcast.accept(new ClientboundSetPassengersPacket(this.entity));
		}

		if (this.entity instanceof ItemFrame itemFrame && this.tickCount % 10 == 0) {
			ItemStack itemStack = itemFrame.getItem();
			if (itemStack.getItem() instanceof MapItem) {
				Integer integer = MapItem.getMapId(itemStack);
				MapItemSavedData mapItemSavedData = MapItem.getSavedData(integer, this.level);
				if (mapItemSavedData != null) {
					for (ServerPlayer serverPlayer : this.level.players()) {
						mapItemSavedData.tickCarriedBy(serverPlayer, itemStack);
						Packet<?> packet = mapItemSavedData.getUpdatePacket(integer, serverPlayer);
						if (packet != null) {
							serverPlayer.connection.send(packet);
						}
					}
				}
			}

			this.sendDirtyEntityData();
		}

		if (this.tickCount % this.updateInterval == 0 || this.entity.hasImpulse || this.entity.getEntityData().isDirty()) {
			if (this.entity.isPassenger()) {
				int i = Mth.floor(this.entity.getYRot() * 256.0F / 360.0F);
				int j = Mth.floor(this.entity.getXRot() * 256.0F / 360.0F);
				boolean bl = Math.abs(i - this.yRotp) >= 1 || Math.abs(j - this.xRotp) >= 1;
				if (bl) {
					this.broadcast.accept(new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte)i, (byte)j, this.entity.isOnGround()));
					this.yRotp = i;
					this.xRotp = j;
				}

				this.positionCodec.setBase(this.entity.trackingPosition());
				this.sendDirtyEntityData();
				this.wasRiding = true;
			} else {
				this.teleportDelay++;
				int i = Mth.floor(this.entity.getYRot() * 256.0F / 360.0F);
				int j = Mth.floor(this.entity.getXRot() * 256.0F / 360.0F);
				Vec3 vec3 = this.entity.trackingPosition();
				boolean bl2 = this.positionCodec.delta(vec3).lengthSqr() >= 7.6293945E-6F;
				Packet<?> packet2 = null;
				boolean bl3 = bl2 || this.tickCount % 60 == 0;
				boolean bl4 = Math.abs(i - this.yRotp) >= 1 || Math.abs(j - this.xRotp) >= 1;
				boolean bl5 = false;
				boolean bl6 = false;
				if (this.tickCount > 0 || this.entity instanceof AbstractArrow) {
					long l = this.positionCodec.encodeX(vec3);
					long m = this.positionCodec.encodeY(vec3);
					long n = this.positionCodec.encodeZ(vec3);
					boolean bl7 = l < -32768L || l > 32767L || m < -32768L || m > 32767L || n < -32768L || n > 32767L;
					if (bl7 || this.teleportDelay > 400 || this.wasRiding || this.wasOnGround != this.entity.isOnGround()) {
						this.wasOnGround = this.entity.isOnGround();
						this.teleportDelay = 0;
						packet2 = new ClientboundTeleportEntityPacket(this.entity);
						bl5 = true;
						bl6 = true;
					} else if ((!bl3 || !bl4) && !(this.entity instanceof AbstractArrow)) {
						if (bl3) {
							packet2 = new ClientboundMoveEntityPacket.Pos(this.entity.getId(), (short)((int)l), (short)((int)m), (short)((int)n), this.entity.isOnGround());
							bl5 = true;
						} else if (bl4) {
							packet2 = new ClientboundMoveEntityPacket.Rot(this.entity.getId(), (byte)i, (byte)j, this.entity.isOnGround());
							bl6 = true;
						}
					} else {
						packet2 = new ClientboundMoveEntityPacket.PosRot(
							this.entity.getId(), (short)((int)l), (short)((int)m), (short)((int)n), (byte)i, (byte)j, this.entity.isOnGround()
						);
						bl5 = true;
						bl6 = true;
					}
				}

				if ((this.trackDelta || this.entity.hasImpulse || this.entity instanceof LivingEntity && ((LivingEntity)this.entity).isFallFlying()) && this.tickCount > 0) {
					Vec3 vec32 = this.entity.getDeltaMovement();
					double d = vec32.distanceToSqr(this.ap);
					if (d > 1.0E-7 || d > 0.0 && vec32.lengthSqr() == 0.0) {
						this.ap = vec32;
						this.broadcast.accept(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.ap));
					}
				}

				if (packet2 != null) {
					this.broadcast.accept(packet2);
				}

				this.sendDirtyEntityData();
				if (bl5) {
					this.positionCodec.setBase(vec3);
				}

				if (bl6) {
					this.yRotp = i;
					this.xRotp = j;
				}

				this.wasRiding = false;
			}

			int ix = Mth.floor(this.entity.getYHeadRot() * 256.0F / 360.0F);
			if (Math.abs(ix - this.yHeadRotp) >= 1) {
				this.broadcast.accept(new ClientboundRotateHeadPacket(this.entity, (byte)ix));
				this.yHeadRotp = ix;
			}

			this.entity.hasImpulse = false;
		}

		this.tickCount++;
		if (this.entity.hurtMarked) {
			this.broadcastAndSend(new ClientboundSetEntityMotionPacket(this.entity));
			this.entity.hurtMarked = false;
		}
	}

	public void removePairing(ServerPlayer serverPlayer) {
		this.entity.stopSeenByPlayer(serverPlayer);
		serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(this.entity.getId()));
	}

	public void addPairing(ServerPlayer serverPlayer) {
		List<Packet<ClientGamePacketListener>> list = new ArrayList();
		this.sendPairingData(list::add);
		serverPlayer.connection.send(new ClientboundBundlePacket(list));
		this.entity.startSeenByPlayer(serverPlayer);
	}

	public void sendPairingData(Consumer<Packet<ClientGamePacketListener>> consumer) {
		if (this.entity.isRemoved()) {
			LOGGER.warn("Fetching packet for removed entity {}", this.entity);
		}

		Packet<ClientGamePacketListener> packet = this.entity.getAddEntityPacket();
		this.yHeadRotp = Mth.floor(this.entity.getYHeadRot() * 256.0F / 360.0F);
		consumer.accept(packet);
		if (this.trackedDataValues != null) {
			consumer.accept(new ClientboundSetEntityDataPacket(this.entity.getId(), this.trackedDataValues));
		}

		boolean bl = this.trackDelta;
		if (this.entity instanceof LivingEntity) {
			Collection<AttributeInstance> collection = ((LivingEntity)this.entity).getAttributes().getSyncableAttributes();
			if (!collection.isEmpty()) {
				consumer.accept(new ClientboundUpdateAttributesPacket(this.entity.getId(), collection));
			}

			if (((LivingEntity)this.entity).isFallFlying()) {
				bl = true;
			}
		}

		this.ap = this.entity.getDeltaMovement();
		if (bl && !(this.entity instanceof LivingEntity)) {
			consumer.accept(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.ap));
		}

		if (this.entity instanceof LivingEntity) {
			List<Pair<EquipmentSlot, ItemStack>> list = Lists.<Pair<EquipmentSlot, ItemStack>>newArrayList();

			for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
				ItemStack itemStack = ((LivingEntity)this.entity).getItemBySlot(equipmentSlot);
				if (!itemStack.isEmpty()) {
					list.add(Pair.of(equipmentSlot, itemStack.copy()));
				}
			}

			if (!list.isEmpty()) {
				consumer.accept(new ClientboundSetEquipmentPacket(this.entity.getId(), list));
			}
		}

		if (this.entity instanceof LivingEntity livingEntity) {
			for (MobEffectInstance mobEffectInstance : livingEntity.getActiveEffects()) {
				consumer.accept(new ClientboundUpdateMobEffectPacket(this.entity.getId(), mobEffectInstance));
			}
		}

		if (!this.entity.getPassengers().isEmpty()) {
			consumer.accept(new ClientboundSetPassengersPacket(this.entity));
		}

		if (this.entity.isPassenger()) {
			consumer.accept(new ClientboundSetPassengersPacket(this.entity.getVehicle()));
		}

		if (this.entity instanceof Mob mob && mob.isLeashed()) {
			consumer.accept(new ClientboundSetEntityLinkPacket(mob, mob.getLeashHolder()));
		}
	}

	private void sendDirtyEntityData() {
		SynchedEntityData synchedEntityData = this.entity.getEntityData();
		List<SynchedEntityData.DataValue<?>> list = synchedEntityData.packDirty();
		if (list != null) {
			this.trackedDataValues = synchedEntityData.getNonDefaultValues();
			this.broadcastAndSend(new ClientboundSetEntityDataPacket(this.entity.getId(), list));
		}

		if (this.entity instanceof LivingEntity) {
			Set<AttributeInstance> set = ((LivingEntity)this.entity).getAttributes().getDirtyAttributes();
			if (!set.isEmpty()) {
				this.broadcastAndSend(new ClientboundUpdateAttributesPacket(this.entity.getId(), set));
			}

			set.clear();
		}
	}

	private void broadcastAndSend(Packet<?> packet) {
		this.broadcast.accept(packet);
		if (this.entity instanceof ServerPlayer) {
			((ServerPlayer)this.entity).connection.send(packet);
		}
	}
}

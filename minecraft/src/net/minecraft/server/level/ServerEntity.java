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
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveMinecartPacket;
import net.minecraft.network.protocol.game.ClientboundProjectilePowerPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ServerEntity {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int TOLERANCE_LEVEL_ROTATION = 1;
	private static final double TOLERANCE_LEVEL_POSITION = 7.6293945E-6F;
	public static final int FORCED_POS_UPDATE_PERIOD = 60;
	private static final int FORCED_TELEPORT_PERIOD = 400;
	private final ServerLevel level;
	private final Entity entity;
	private final int updateInterval;
	private final boolean trackDelta;
	private final Consumer<Packet<?>> broadcast;
	private final VecDeltaCodec positionCodec = new VecDeltaCodec();
	private byte lastSentYRot;
	private byte lastSentXRot;
	private byte lastSentYHeadRot;
	private Vec3 lastSentMovement;
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
		this.lastSentMovement = entity.getDeltaMovement();
		this.lastSentYRot = Mth.packDegrees(entity.getYRot());
		this.lastSentXRot = Mth.packDegrees(entity.getXRot());
		this.lastSentYHeadRot = Mth.packDegrees(entity.getYHeadRot());
		this.wasOnGround = entity.onGround();
		this.trackedDataValues = entity.getEntityData().getNonDefaultValues();
	}

	public void sendChanges() {
		List<Entity> list = this.entity.getPassengers();
		if (!list.equals(this.lastPassengers)) {
			this.broadcast.accept(new ClientboundSetPassengersPacket(this.entity));
			removedPassengers(list, this.lastPassengers).forEach(entity -> {
				if (entity instanceof ServerPlayer serverPlayer) {
					serverPlayer.connection.teleport(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), serverPlayer.getYRot(), serverPlayer.getXRot());
				}
			});
			this.lastPassengers = list;
		}

		if (this.entity instanceof ItemFrame itemFrame && this.tickCount % 10 == 0) {
			ItemStack itemStack = itemFrame.getItem();
			if (itemStack.getItem() instanceof MapItem) {
				MapId mapId = itemStack.get(DataComponents.MAP_ID);
				MapItemSavedData mapItemSavedData = MapItem.getSavedData(mapId, this.level);
				if (mapItemSavedData != null) {
					for (ServerPlayer serverPlayer : this.level.players()) {
						mapItemSavedData.tickCarriedBy(serverPlayer, itemStack);
						Packet<?> packet = mapItemSavedData.getUpdatePacket(mapId, serverPlayer);
						if (packet != null) {
							serverPlayer.connection.send(packet);
						}
					}
				}
			}

			this.sendDirtyEntityData();
		}

		if (this.tickCount % this.updateInterval == 0 || this.entity.hasImpulse || this.entity.getEntityData().isDirty()) {
			byte b = Mth.packDegrees(this.entity.getYRot());
			byte c = Mth.packDegrees(this.entity.getXRot());
			boolean bl = Math.abs(b - this.lastSentYRot) >= 1 || Math.abs(c - this.lastSentXRot) >= 1;
			if (this.entity.isPassenger()) {
				if (bl) {
					this.broadcast.accept(new ClientboundMoveEntityPacket.Rot(this.entity.getId(), b, c, this.entity.onGround()));
					this.lastSentYRot = b;
					this.lastSentXRot = c;
				}

				this.positionCodec.setBase(this.entity.trackingPosition());
				this.sendDirtyEntityData();
				this.wasRiding = true;
			} else {
				label194: {
					if (this.entity instanceof AbstractMinecart abstractMinecart && abstractMinecart.getBehavior() instanceof NewMinecartBehavior newMinecartBehavior) {
						this.handleMinecartPosRot(newMinecartBehavior, b, c, bl);
						break label194;
					}

					this.teleportDelay++;
					Vec3 vec3 = this.entity.trackingPosition();
					boolean bl2 = this.positionCodec.delta(vec3).lengthSqr() >= 7.6293945E-6F;
					Packet<?> packet2 = null;
					boolean bl3 = bl2 || this.tickCount % 60 == 0;
					boolean bl4 = false;
					boolean bl5 = false;
					long l = this.positionCodec.encodeX(vec3);
					long m = this.positionCodec.encodeY(vec3);
					long n = this.positionCodec.encodeZ(vec3);
					boolean bl6 = l < -32768L || l > 32767L || m < -32768L || m > 32767L || n < -32768L || n > 32767L;
					if (bl6 || this.teleportDelay > 400 || this.wasRiding || this.wasOnGround != this.entity.onGround()) {
						this.wasOnGround = this.entity.onGround();
						this.teleportDelay = 0;
						packet2 = ClientboundEntityPositionSyncPacket.of(this.entity);
						bl4 = true;
						bl5 = true;
					} else if ((!bl3 || !bl) && !(this.entity instanceof AbstractArrow)) {
						if (bl3) {
							packet2 = new ClientboundMoveEntityPacket.Pos(this.entity.getId(), (short)((int)l), (short)((int)m), (short)((int)n), this.entity.onGround());
							bl4 = true;
						} else if (bl) {
							packet2 = new ClientboundMoveEntityPacket.Rot(this.entity.getId(), b, c, this.entity.onGround());
							bl5 = true;
						}
					} else {
						packet2 = new ClientboundMoveEntityPacket.PosRot(this.entity.getId(), (short)((int)l), (short)((int)m), (short)((int)n), b, c, this.entity.onGround());
						bl4 = true;
						bl5 = true;
					}

					if ((this.trackDelta || this.entity.hasImpulse || this.entity instanceof LivingEntity && ((LivingEntity)this.entity).isFallFlying()) && this.tickCount > 0
						)
					 {
						Vec3 vec32 = this.entity.getDeltaMovement();
						double d = vec32.distanceToSqr(this.lastSentMovement);
						if (d > 1.0E-7 || d > 0.0 && vec32.lengthSqr() == 0.0) {
							this.lastSentMovement = vec32;
							if (this.entity instanceof AbstractHurtingProjectile abstractHurtingProjectile) {
								this.broadcast
									.accept(
										new ClientboundBundlePacket(
											List.of(
												new ClientboundSetEntityMotionPacket(this.entity.getId(), this.lastSentMovement),
												new ClientboundProjectilePowerPacket(abstractHurtingProjectile.getId(), abstractHurtingProjectile.accelerationPower)
											)
										)
									);
							} else {
								this.broadcast.accept(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.lastSentMovement));
							}
						}
					}

					if (packet2 != null) {
						this.broadcast.accept(packet2);
					}

					this.sendDirtyEntityData();
					if (bl4) {
						this.positionCodec.setBase(vec3);
					}

					if (bl5) {
						this.lastSentYRot = b;
						this.lastSentXRot = c;
					}

					this.wasRiding = false;
				}
			}

			byte e = Mth.packDegrees(this.entity.getYHeadRot());
			if (Math.abs(e - this.lastSentYHeadRot) >= 1) {
				this.broadcast.accept(new ClientboundRotateHeadPacket(this.entity, e));
				this.lastSentYHeadRot = e;
			}

			this.entity.hasImpulse = false;
		}

		this.tickCount++;
		if (this.entity.hurtMarked) {
			this.entity.hurtMarked = false;
			this.broadcastAndSend(new ClientboundSetEntityMotionPacket(this.entity));
		}
	}

	private void handleMinecartPosRot(NewMinecartBehavior newMinecartBehavior, byte b, byte c, boolean bl) {
		this.sendDirtyEntityData();
		if (newMinecartBehavior.lerpSteps.isEmpty()) {
			Vec3 vec3 = this.entity.getDeltaMovement();
			double d = vec3.distanceToSqr(this.lastSentMovement);
			Vec3 vec32 = this.entity.trackingPosition();
			boolean bl2 = this.positionCodec.delta(vec32).lengthSqr() >= 7.6293945E-6F;
			boolean bl3 = bl2 || this.tickCount % 60 == 0;
			if (bl3 || bl || d > 1.0E-7) {
				this.broadcast
					.accept(
						new ClientboundMoveMinecartPacket(
							this.entity.getId(),
							List.of(new NewMinecartBehavior.MinecartStep(this.entity.position(), this.entity.getDeltaMovement(), this.entity.getYRot(), this.entity.getXRot(), 1.0F))
						)
					);
			}
		} else {
			this.broadcast.accept(new ClientboundMoveMinecartPacket(this.entity.getId(), List.copyOf(newMinecartBehavior.lerpSteps)));
			newMinecartBehavior.lerpSteps.clear();
		}

		this.lastSentYRot = b;
		this.lastSentXRot = c;
		this.positionCodec.setBase(this.entity.position());
	}

	private static Stream<Entity> removedPassengers(List<Entity> list, List<Entity> list2) {
		return list2.stream().filter(entity -> !list.contains(entity));
	}

	public void removePairing(ServerPlayer serverPlayer) {
		this.entity.stopSeenByPlayer(serverPlayer);
		serverPlayer.connection.send(new ClientboundRemoveEntitiesPacket(this.entity.getId()));
	}

	public void addPairing(ServerPlayer serverPlayer) {
		List<Packet<? super ClientGamePacketListener>> list = new ArrayList();
		this.sendPairingData(serverPlayer, list::add);
		serverPlayer.connection.send(new ClientboundBundlePacket(list));
		this.entity.startSeenByPlayer(serverPlayer);
	}

	public void sendPairingData(ServerPlayer serverPlayer, Consumer<Packet<ClientGamePacketListener>> consumer) {
		if (this.entity.isRemoved()) {
			LOGGER.warn("Fetching packet for removed entity {}", this.entity);
		}

		Packet<ClientGamePacketListener> packet = this.entity.getAddEntityPacket(this);
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

		if (bl && !(this.entity instanceof LivingEntity)) {
			consumer.accept(new ClientboundSetEntityMotionPacket(this.entity.getId(), this.lastSentMovement));
		}

		if (this.entity instanceof LivingEntity livingEntity) {
			List<Pair<EquipmentSlot, ItemStack>> list = Lists.<Pair<EquipmentSlot, ItemStack>>newArrayList();

			for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
				ItemStack itemStack = livingEntity.getItemBySlot(equipmentSlot);
				if (!itemStack.isEmpty()) {
					list.add(Pair.of(equipmentSlot, itemStack.copy()));
				}
			}

			if (!list.isEmpty()) {
				consumer.accept(new ClientboundSetEquipmentPacket(this.entity.getId(), list));
			}
		}

		if (!this.entity.getPassengers().isEmpty()) {
			consumer.accept(new ClientboundSetPassengersPacket(this.entity));
		}

		if (this.entity.isPassenger()) {
			consumer.accept(new ClientboundSetPassengersPacket(this.entity.getVehicle()));
		}

		if (this.entity instanceof Leashable leashable && leashable.isLeashed()) {
			consumer.accept(new ClientboundSetEntityLinkPacket(this.entity, leashable.getLeashHolder()));
		}
	}

	public Vec3 getPositionBase() {
		return this.positionCodec.getBase();
	}

	public Vec3 getLastSentMovement() {
		return this.lastSentMovement;
	}

	public float getLastSentXRot() {
		return Mth.unpackDegrees(this.lastSentXRot);
	}

	public float getLastSentYRot() {
		return Mth.unpackDegrees(this.lastSentYRot);
	}

	public float getLastSentYHeadRot() {
		return Mth.unpackDegrees(this.lastSentYHeadRot);
	}

	private void sendDirtyEntityData() {
		SynchedEntityData synchedEntityData = this.entity.getEntityData();
		List<SynchedEntityData.DataValue<?>> list = synchedEntityData.packDirty();
		if (list != null) {
			this.trackedDataValues = synchedEntityData.getNonDefaultValues();
			this.broadcastAndSend(new ClientboundSetEntityDataPacket(this.entity.getId(), list));
		}

		if (this.entity instanceof LivingEntity) {
			Set<AttributeInstance> set = ((LivingEntity)this.entity).getAttributes().getAttributesToSync();
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

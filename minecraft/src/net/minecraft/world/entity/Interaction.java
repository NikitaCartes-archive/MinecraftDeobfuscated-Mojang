package net.minecraft.world.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class Interaction extends Entity implements Attackable, Targeting {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final EntityDataAccessor<Float> DATA_WIDTH_ID = SynchedEntityData.defineId(Interaction.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_HEIGHT_ID = SynchedEntityData.defineId(Interaction.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Boolean> DATA_RESPONSE_ID = SynchedEntityData.defineId(Interaction.class, EntityDataSerializers.BOOLEAN);
	private static final String TAG_WIDTH = "width";
	private static final String TAG_HEIGHT = "height";
	private static final String TAG_ATTACK = "attack";
	private static final String TAG_INTERACTION = "interaction";
	private static final String TAG_RESPONSE = "response";
	@Nullable
	private Interaction.PlayerAction attack;
	@Nullable
	private Interaction.PlayerAction interaction;

	public Interaction(EntityType<?> entityType, Level level) {
		super(entityType, level);
		this.noPhysics = true;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_WIDTH_ID, 1.0F);
		builder.define(DATA_HEIGHT_ID, 1.0F);
		builder.define(DATA_RESPONSE_ID, false);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compoundTag) {
		if (compoundTag.contains("width", 99)) {
			this.setWidth(compoundTag.getFloat("width"));
		}

		if (compoundTag.contains("height", 99)) {
			this.setHeight(compoundTag.getFloat("height"));
		}

		if (compoundTag.contains("attack")) {
			Interaction.PlayerAction.CODEC
				.decode(NbtOps.INSTANCE, compoundTag.get("attack"))
				.resultOrPartial(Util.prefix("Interaction entity", LOGGER::error))
				.ifPresent(pair -> this.attack = (Interaction.PlayerAction)pair.getFirst());
		} else {
			this.attack = null;
		}

		if (compoundTag.contains("interaction")) {
			Interaction.PlayerAction.CODEC
				.decode(NbtOps.INSTANCE, compoundTag.get("interaction"))
				.resultOrPartial(Util.prefix("Interaction entity", LOGGER::error))
				.ifPresent(pair -> this.interaction = (Interaction.PlayerAction)pair.getFirst());
		} else {
			this.interaction = null;
		}

		this.setResponse(compoundTag.getBoolean("response"));
		this.setBoundingBox(this.makeBoundingBox());
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		compoundTag.putFloat("width", this.getWidth());
		compoundTag.putFloat("height", this.getHeight());
		if (this.attack != null) {
			Interaction.PlayerAction.CODEC.encodeStart(NbtOps.INSTANCE, this.attack).ifSuccess(tag -> compoundTag.put("attack", tag));
		}

		if (this.interaction != null) {
			Interaction.PlayerAction.CODEC.encodeStart(NbtOps.INSTANCE, this.interaction).ifSuccess(tag -> compoundTag.put("interaction", tag));
		}

		compoundTag.putBoolean("response", this.getResponse());
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		super.onSyncedDataUpdated(entityDataAccessor);
		if (DATA_HEIGHT_ID.equals(entityDataAccessor) || DATA_WIDTH_ID.equals(entityDataAccessor)) {
			this.refreshDimensions();
		}
	}

	@Override
	public boolean canBeHitByProjectile() {
		return false;
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public PushReaction getPistonPushReaction() {
		return PushReaction.IGNORE;
	}

	@Override
	public boolean isIgnoringBlockTriggers() {
		return true;
	}

	@Override
	public boolean skipAttackInteraction(Entity entity) {
		if (entity instanceof Player player) {
			this.attack = new Interaction.PlayerAction(player.getUUID(), this.level().getGameTime());
			if (player instanceof ServerPlayer serverPlayer) {
				CriteriaTriggers.PLAYER_HURT_ENTITY.trigger(serverPlayer, this, player.damageSources().generic(), 1.0F, 1.0F, false);
			}

			return !this.getResponse();
		} else {
			return false;
		}
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand interactionHand) {
		if (this.level().isClientSide) {
			return this.getResponse() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
		} else {
			this.interaction = new Interaction.PlayerAction(player.getUUID(), this.level().getGameTime());
			return InteractionResult.CONSUME;
		}
	}

	@Override
	public void tick() {
	}

	@Nullable
	@Override
	public LivingEntity getLastAttacker() {
		return this.attack != null ? this.level().getPlayerByUUID(this.attack.player()) : null;
	}

	@Nullable
	@Override
	public LivingEntity getTarget() {
		return this.interaction != null ? this.level().getPlayerByUUID(this.interaction.player()) : null;
	}

	private void setWidth(float f) {
		this.entityData.set(DATA_WIDTH_ID, f);
	}

	private float getWidth() {
		return this.entityData.get(DATA_WIDTH_ID);
	}

	private void setHeight(float f) {
		this.entityData.set(DATA_HEIGHT_ID, f);
	}

	private float getHeight() {
		return this.entityData.get(DATA_HEIGHT_ID);
	}

	private void setResponse(boolean bl) {
		this.entityData.set(DATA_RESPONSE_ID, bl);
	}

	private boolean getResponse() {
		return this.entityData.get(DATA_RESPONSE_ID);
	}

	private EntityDimensions getDimensions() {
		return EntityDimensions.scalable(this.getWidth(), this.getHeight());
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return this.getDimensions();
	}

	@Override
	protected AABB makeBoundingBox() {
		return this.getDimensions().makeBoundingBox(this.position());
	}

	static record PlayerAction(UUID player, long timestamp) {
		public static final Codec<Interaction.PlayerAction> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						UUIDUtil.CODEC.fieldOf("player").forGetter(Interaction.PlayerAction::player),
						Codec.LONG.fieldOf("timestamp").forGetter(Interaction.PlayerAction::timestamp)
					)
					.apply(instance, Interaction.PlayerAction::new)
		);
	}
}

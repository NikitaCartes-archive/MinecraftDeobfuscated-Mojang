package net.minecraft.client.player;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.JigsawBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.MinecartCommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.resources.sounds.AmbientSoundHandler;
import net.minecraft.client.resources.sounds.BiomeAmbientSoundsHandler;
import net.minecraft.client.resources.sounds.BubbleColumnAmbientSoundHandler;
import net.minecraft.client.resources.sounds.ElytraOnPlayerSoundInstance;
import net.minecraft.client.resources.sounds.RidingMinecartSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.UnderwaterAmbientSoundHandler;
import net.minecraft.client.resources.sounds.UnderwaterAmbientSoundInstances;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.StatsCounter;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@Environment(EnvType.CLIENT)
public class LocalPlayer extends AbstractClientPlayer {
	private static final int POSITION_REMINDER_INTERVAL = 20;
	private static final int WATER_VISION_MAX_TIME = 600;
	private static final int WATER_VISION_QUICK_TIME = 100;
	private static final float WATER_VISION_QUICK_PERCENT = 0.6F;
	private static final double SUFFOCATING_COLLISION_CHECK_SCALE = 0.35;
	private static final double MINOR_COLLISION_ANGLE_THRESHOLD_RADIAN = 0.13962634F;
	public final ClientPacketListener connection;
	private final StatsCounter stats;
	private final ClientRecipeBook recipeBook;
	private final List<AmbientSoundHandler> ambientSoundHandlers = Lists.<AmbientSoundHandler>newArrayList();
	private int permissionLevel = 0;
	private double xLast;
	private double yLast1;
	private double zLast;
	private float yRotLast;
	private float xRotLast;
	private boolean lastOnGround;
	private boolean crouching;
	private boolean wasShiftKeyDown;
	private boolean wasSprinting;
	private int positionReminder;
	private boolean flashOnSetHealth;
	@Nullable
	private String serverBrand;
	public Input input;
	protected final Minecraft minecraft;
	protected int sprintTriggerTime;
	public int sprintTime;
	public float yBob;
	public float xBob;
	public float yBobO;
	public float xBobO;
	private int jumpRidingTicks;
	private float jumpRidingScale;
	public float portalTime;
	public float oPortalTime;
	private boolean startedUsingItem;
	@Nullable
	private InteractionHand usingItemHand;
	private boolean handsBusy;
	private boolean autoJumpEnabled = true;
	private int autoJumpTime;
	private boolean wasFallFlying;
	private int waterVisionTime;
	private boolean showDeathScreen = true;

	public LocalPlayer(
		Minecraft minecraft,
		ClientLevel clientLevel,
		ClientPacketListener clientPacketListener,
		StatsCounter statsCounter,
		ClientRecipeBook clientRecipeBook,
		boolean bl,
		boolean bl2
	) {
		super(clientLevel, clientPacketListener.getLocalGameProfile());
		this.minecraft = minecraft;
		this.connection = clientPacketListener;
		this.stats = statsCounter;
		this.recipeBook = clientRecipeBook;
		this.wasShiftKeyDown = bl;
		this.wasSprinting = bl2;
		this.ambientSoundHandlers.add(new UnderwaterAmbientSoundHandler(this, minecraft.getSoundManager()));
		this.ambientSoundHandlers.add(new BubbleColumnAmbientSoundHandler(this));
		this.ambientSoundHandlers.add(new BiomeAmbientSoundsHandler(this, minecraft.getSoundManager(), clientLevel.getBiomeManager()));
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		return false;
	}

	@Override
	public void heal(float f) {
	}

	@Override
	public boolean startRiding(Entity entity, boolean bl) {
		if (!super.startRiding(entity, bl)) {
			return false;
		} else {
			if (entity instanceof AbstractMinecart) {
				this.minecraft.getSoundManager().play(new RidingMinecartSoundInstance(this, (AbstractMinecart)entity, true));
				this.minecraft.getSoundManager().play(new RidingMinecartSoundInstance(this, (AbstractMinecart)entity, false));
			}

			return true;
		}
	}

	@Override
	public void removeVehicle() {
		super.removeVehicle();
		this.handsBusy = false;
	}

	@Override
	public float getViewXRot(float f) {
		return this.getXRot();
	}

	@Override
	public float getViewYRot(float f) {
		return this.isPassenger() ? super.getViewYRot(f) : this.getYRot();
	}

	@Override
	public void tick() {
		if (this.level.hasChunkAt(this.getBlockX(), this.getBlockZ())) {
			super.tick();
			if (this.isPassenger()) {
				this.connection.send(new ServerboundMovePlayerPacket.Rot(this.getYRot(), this.getXRot(), this.onGround));
				this.connection.send(new ServerboundPlayerInputPacket(this.xxa, this.zza, this.input.jumping, this.input.shiftKeyDown));
				Entity entity = this.getRootVehicle();
				if (entity != this && entity.isControlledByLocalInstance()) {
					this.connection.send(new ServerboundMoveVehiclePacket(entity));
				}
			} else {
				this.sendPosition();
			}

			for (AmbientSoundHandler ambientSoundHandler : this.ambientSoundHandlers) {
				ambientSoundHandler.tick();
			}
		}
	}

	public float getCurrentMood() {
		for (AmbientSoundHandler ambientSoundHandler : this.ambientSoundHandlers) {
			if (ambientSoundHandler instanceof BiomeAmbientSoundsHandler) {
				return ((BiomeAmbientSoundsHandler)ambientSoundHandler).getMoodiness();
			}
		}

		return 0.0F;
	}

	private void sendPosition() {
		boolean bl = this.isSprinting();
		if (bl != this.wasSprinting) {
			ServerboundPlayerCommandPacket.Action action = bl
				? ServerboundPlayerCommandPacket.Action.START_SPRINTING
				: ServerboundPlayerCommandPacket.Action.STOP_SPRINTING;
			this.connection.send(new ServerboundPlayerCommandPacket(this, action));
			this.wasSprinting = bl;
		}

		boolean bl2 = this.isShiftKeyDown();
		if (bl2 != this.wasShiftKeyDown) {
			ServerboundPlayerCommandPacket.Action action2 = bl2
				? ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY
				: ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY;
			this.connection.send(new ServerboundPlayerCommandPacket(this, action2));
			this.wasShiftKeyDown = bl2;
		}

		if (this.isControlledCamera()) {
			double d = this.getX() - this.xLast;
			double e = this.getY() - this.yLast1;
			double f = this.getZ() - this.zLast;
			double g = (double)(this.getYRot() - this.yRotLast);
			double h = (double)(this.getXRot() - this.xRotLast);
			this.positionReminder++;
			boolean bl3 = Mth.lengthSquared(d, e, f) > Mth.square(2.0E-4) || this.positionReminder >= 20;
			boolean bl4 = g != 0.0 || h != 0.0;
			if (this.isPassenger()) {
				Vec3 vec3 = this.getDeltaMovement();
				this.connection.send(new ServerboundMovePlayerPacket.PosRot(vec3.x, -999.0, vec3.z, this.getYRot(), this.getXRot(), this.onGround));
				bl3 = false;
			} else if (bl3 && bl4) {
				this.connection.send(new ServerboundMovePlayerPacket.PosRot(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot(), this.onGround));
			} else if (bl3) {
				this.connection.send(new ServerboundMovePlayerPacket.Pos(this.getX(), this.getY(), this.getZ(), this.onGround));
			} else if (bl4) {
				this.connection.send(new ServerboundMovePlayerPacket.Rot(this.getYRot(), this.getXRot(), this.onGround));
			} else if (this.lastOnGround != this.onGround) {
				this.connection.send(new ServerboundMovePlayerPacket.StatusOnly(this.onGround));
			}

			if (bl3) {
				this.xLast = this.getX();
				this.yLast1 = this.getY();
				this.zLast = this.getZ();
				this.positionReminder = 0;
			}

			if (bl4) {
				this.yRotLast = this.getYRot();
				this.xRotLast = this.getXRot();
			}

			this.lastOnGround = this.onGround;
			this.autoJumpEnabled = this.minecraft.options.autoJump;
		}
	}

	public boolean drop(boolean bl) {
		ServerboundPlayerActionPacket.Action action = bl ? ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS : ServerboundPlayerActionPacket.Action.DROP_ITEM;
		ItemStack itemStack = this.getInventory().removeFromSelected(bl);
		this.connection.send(new ServerboundPlayerActionPacket(action, BlockPos.ZERO, Direction.DOWN));
		return !itemStack.isEmpty();
	}

	public void chat(String string) {
		this.connection.send(new ServerboundChatPacket(string));
	}

	@Override
	public void swing(InteractionHand interactionHand) {
		super.swing(interactionHand);
		this.connection.send(new ServerboundSwingPacket(interactionHand));
	}

	@Override
	public void respawn() {
		this.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
	}

	@Override
	protected void actuallyHurt(DamageSource damageSource, float f) {
		if (!this.isInvulnerableTo(damageSource)) {
			this.setHealth(this.getHealth() - f);
		}
	}

	@Override
	public void closeContainer() {
		this.connection.send(new ServerboundContainerClosePacket(this.containerMenu.containerId));
		this.clientSideCloseContainer();
	}

	public void clientSideCloseContainer() {
		super.closeContainer();
		this.minecraft.setScreen(null);
	}

	public void hurtTo(float f) {
		if (this.flashOnSetHealth) {
			float g = this.getHealth() - f;
			if (g <= 0.0F) {
				this.setHealth(f);
				if (g < 0.0F) {
					this.invulnerableTime = 10;
				}
			} else {
				this.lastHurt = g;
				this.invulnerableTime = 20;
				this.setHealth(f);
				this.hurtDuration = 10;
				this.hurtTime = this.hurtDuration;
			}
		} else {
			this.setHealth(f);
			this.flashOnSetHealth = true;
		}
	}

	@Override
	public void onUpdateAbilities() {
		this.connection.send(new ServerboundPlayerAbilitiesPacket(this.getAbilities()));
	}

	@Override
	public boolean isLocalPlayer() {
		return true;
	}

	@Override
	public boolean isSuppressingSlidingDownLadder() {
		return !this.getAbilities().flying && super.isSuppressingSlidingDownLadder();
	}

	@Override
	public boolean canSpawnSprintParticle() {
		return !this.getAbilities().flying && super.canSpawnSprintParticle();
	}

	@Override
	public boolean canSpawnSoulSpeedParticle() {
		return !this.getAbilities().flying && super.canSpawnSoulSpeedParticle();
	}

	protected void sendRidingJump() {
		this.connection
			.send(new ServerboundPlayerCommandPacket(this, ServerboundPlayerCommandPacket.Action.START_RIDING_JUMP, Mth.floor(this.getJumpRidingScale() * 100.0F)));
	}

	public void sendOpenInventory() {
		this.connection.send(new ServerboundPlayerCommandPacket(this, ServerboundPlayerCommandPacket.Action.OPEN_INVENTORY));
	}

	public void setServerBrand(@Nullable String string) {
		this.serverBrand = string;
	}

	@Nullable
	public String getServerBrand() {
		return this.serverBrand;
	}

	public StatsCounter getStats() {
		return this.stats;
	}

	public ClientRecipeBook getRecipeBook() {
		return this.recipeBook;
	}

	public void removeRecipeHighlight(Recipe<?> recipe) {
		if (this.recipeBook.willHighlight(recipe)) {
			this.recipeBook.removeHighlight(recipe);
			this.connection.send(new ServerboundRecipeBookSeenRecipePacket(recipe));
		}
	}

	@Override
	protected int getPermissionLevel() {
		return this.permissionLevel;
	}

	public void setPermissionLevel(int i) {
		this.permissionLevel = i;
	}

	@Override
	public void displayClientMessage(Component component, boolean bl) {
		if (bl) {
			this.minecraft.gui.setOverlayMessage(component, false);
		} else {
			this.minecraft.gui.getChat().addMessage(component);
		}
	}

	private void moveTowardsClosestSpace(double d, double e) {
		BlockPos blockPos = new BlockPos(d, this.getY(), e);
		if (this.suffocatesAt(blockPos)) {
			double f = d - (double)blockPos.getX();
			double g = e - (double)blockPos.getZ();
			Direction direction = null;
			double h = Double.MAX_VALUE;
			Direction[] directions = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};

			for (Direction direction2 : directions) {
				double i = direction2.getAxis().choose(f, 0.0, g);
				double j = direction2.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0 - i : i;
				if (j < h && !this.suffocatesAt(blockPos.relative(direction2))) {
					h = j;
					direction = direction2;
				}
			}

			if (direction != null) {
				Vec3 vec3 = this.getDeltaMovement();
				if (direction.getAxis() == Direction.Axis.X) {
					this.setDeltaMovement(0.1 * (double)direction.getStepX(), vec3.y, vec3.z);
				} else {
					this.setDeltaMovement(vec3.x, vec3.y, 0.1 * (double)direction.getStepZ());
				}
			}
		}
	}

	private boolean suffocatesAt(BlockPos blockPos) {
		AABB aABB = this.getBoundingBox();
		AABB aABB2 = new AABB((double)blockPos.getX(), aABB.minY, (double)blockPos.getZ(), (double)blockPos.getX() + 1.0, aABB.maxY, (double)blockPos.getZ() + 1.0)
			.deflate(1.0E-7);
		return this.level.collidesWithSuffocatingBlock(this, aABB2);
	}

	@Override
	public void setSprinting(boolean bl) {
		super.setSprinting(bl);
		this.sprintTime = 0;
	}

	public void setExperienceValues(float f, int i, int j) {
		this.experienceProgress = f;
		this.totalExperience = i;
		this.experienceLevel = j;
	}

	@Override
	public void sendMessage(Component component, UUID uUID) {
		this.minecraft.gui.getChat().addMessage(component);
	}

	@Override
	public void handleEntityEvent(byte b) {
		if (b >= 24 && b <= 28) {
			this.setPermissionLevel(b - 24);
		} else {
			super.handleEntityEvent(b);
		}
	}

	public void setShowDeathScreen(boolean bl) {
		this.showDeathScreen = bl;
	}

	public boolean shouldShowDeathScreen() {
		return this.showDeathScreen;
	}

	@Override
	public void playSound(SoundEvent soundEvent, float f, float g) {
		this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundSource(), f, g, false);
	}

	@Override
	public void playNotifySound(SoundEvent soundEvent, SoundSource soundSource, float f, float g) {
		this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), soundEvent, soundSource, f, g, false);
	}

	@Override
	public boolean isEffectiveAi() {
		return true;
	}

	@Override
	public void startUsingItem(InteractionHand interactionHand) {
		ItemStack itemStack = this.getItemInHand(interactionHand);
		if (!itemStack.isEmpty() && !this.isUsingItem()) {
			super.startUsingItem(interactionHand);
			this.startedUsingItem = true;
			this.usingItemHand = interactionHand;
		}
	}

	@Override
	public boolean isUsingItem() {
		return this.startedUsingItem;
	}

	@Override
	public void stopUsingItem() {
		super.stopUsingItem();
		this.startedUsingItem = false;
	}

	@Override
	public InteractionHand getUsedItemHand() {
		return (InteractionHand)Objects.requireNonNullElse(this.usingItemHand, InteractionHand.MAIN_HAND);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		super.onSyncedDataUpdated(entityDataAccessor);
		if (DATA_LIVING_ENTITY_FLAGS.equals(entityDataAccessor)) {
			boolean bl = (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 1) > 0;
			InteractionHand interactionHand = (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 2) > 0 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
			if (bl && !this.startedUsingItem) {
				this.startUsingItem(interactionHand);
			} else if (!bl && this.startedUsingItem) {
				this.stopUsingItem();
			}
		}

		if (DATA_SHARED_FLAGS_ID.equals(entityDataAccessor) && this.isFallFlying() && !this.wasFallFlying) {
			this.minecraft.getSoundManager().play(new ElytraOnPlayerSoundInstance(this));
		}
	}

	public boolean isRidingJumpable() {
		Entity entity = this.getVehicle();
		return this.isPassenger() && entity instanceof PlayerRideableJumping && ((PlayerRideableJumping)entity).canJump();
	}

	public float getJumpRidingScale() {
		return this.jumpRidingScale;
	}

	@Override
	public void openTextEdit(SignBlockEntity signBlockEntity) {
		this.minecraft.setScreen(new SignEditScreen(signBlockEntity, this.minecraft.isTextFilteringEnabled()));
	}

	@Override
	public void openMinecartCommandBlock(BaseCommandBlock baseCommandBlock) {
		this.minecraft.setScreen(new MinecartCommandBlockEditScreen(baseCommandBlock));
	}

	@Override
	public void openCommandBlock(CommandBlockEntity commandBlockEntity) {
		this.minecraft.setScreen(new CommandBlockEditScreen(commandBlockEntity));
	}

	@Override
	public void openStructureBlock(StructureBlockEntity structureBlockEntity) {
		this.minecraft.setScreen(new StructureBlockEditScreen(structureBlockEntity));
	}

	@Override
	public void openJigsawBlock(JigsawBlockEntity jigsawBlockEntity) {
		this.minecraft.setScreen(new JigsawBlockEditScreen(jigsawBlockEntity));
	}

	@Override
	public void openItemGui(ItemStack itemStack, InteractionHand interactionHand) {
		if (itemStack.is(Items.WRITABLE_BOOK)) {
			this.minecraft.setScreen(new BookEditScreen(this, itemStack, interactionHand));
		}
	}

	@Override
	public void crit(Entity entity) {
		this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.CRIT);
	}

	@Override
	public void magicCrit(Entity entity) {
		this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.ENCHANTED_HIT);
	}

	@Override
	public boolean isShiftKeyDown() {
		return this.input != null && this.input.shiftKeyDown;
	}

	@Override
	public boolean isCrouching() {
		return this.crouching;
	}

	public boolean isMovingSlowly() {
		return this.isCrouching() || this.isVisuallyCrawling();
	}

	@Override
	public void serverAiStep() {
		super.serverAiStep();
		if (this.isControlledCamera()) {
			this.xxa = this.input.leftImpulse;
			this.zza = this.input.forwardImpulse;
			this.jumping = this.input.jumping;
			this.yBobO = this.yBob;
			this.xBobO = this.xBob;
			this.xBob = this.xBob + (this.getXRot() - this.xBob) * 0.5F;
			this.yBob = this.yBob + (this.getYRot() - this.yBob) * 0.5F;
		}
	}

	protected boolean isControlledCamera() {
		return this.minecraft.getCameraEntity() == this;
	}

	public void resetPos() {
		this.setPose(Pose.STANDING);
		if (this.level != null) {
			for (double d = this.getY(); d > (double)this.level.getMinBuildHeight() && d < (double)this.level.getMaxBuildHeight(); d++) {
				this.setPos(this.getX(), d, this.getZ());
				if (this.level.noCollision(this)) {
					break;
				}
			}

			this.setDeltaMovement(Vec3.ZERO);
			this.setXRot(0.0F);
		}

		this.setHealth(this.getMaxHealth());
		this.deathTime = 0;
	}

	@Override
	public void aiStep() {
		this.sprintTime++;
		if (this.sprintTriggerTime > 0) {
			this.sprintTriggerTime--;
		}

		this.handleNetherPortalClient();
		boolean bl = this.input.jumping;
		boolean bl2 = this.input.shiftKeyDown;
		boolean bl3 = this.hasEnoughImpulseToStartSprinting();
		this.crouching = !this.getAbilities().flying
			&& !this.isSwimming()
			&& this.canEnterPose(Pose.CROUCHING)
			&& (this.isShiftKeyDown() || !this.isSleeping() && !this.canEnterPose(Pose.STANDING));
		this.input.tick(this.isMovingSlowly());
		this.minecraft.getTutorial().onInput(this.input);
		if (this.isUsingItem() && !this.isPassenger()) {
			this.input.leftImpulse *= 0.2F;
			this.input.forwardImpulse *= 0.2F;
			this.sprintTriggerTime = 0;
		}

		boolean bl4 = false;
		if (this.autoJumpTime > 0) {
			this.autoJumpTime--;
			bl4 = true;
			this.input.jumping = true;
		}

		if (!this.noPhysics) {
			this.moveTowardsClosestSpace(this.getX() - (double)this.getBbWidth() * 0.35, this.getZ() + (double)this.getBbWidth() * 0.35);
			this.moveTowardsClosestSpace(this.getX() - (double)this.getBbWidth() * 0.35, this.getZ() - (double)this.getBbWidth() * 0.35);
			this.moveTowardsClosestSpace(this.getX() + (double)this.getBbWidth() * 0.35, this.getZ() - (double)this.getBbWidth() * 0.35);
			this.moveTowardsClosestSpace(this.getX() + (double)this.getBbWidth() * 0.35, this.getZ() + (double)this.getBbWidth() * 0.35);
		}

		if (bl2) {
			this.sprintTriggerTime = 0;
		}

		boolean bl5 = (float)this.getFoodData().getFoodLevel() > 6.0F || this.getAbilities().mayfly;
		if ((this.onGround || this.isUnderWater())
			&& !bl2
			&& !bl3
			&& this.hasEnoughImpulseToStartSprinting()
			&& !this.isSprinting()
			&& bl5
			&& !this.isUsingItem()
			&& !this.hasEffect(MobEffects.BLINDNESS)) {
			if (this.sprintTriggerTime <= 0 && !this.minecraft.options.keySprint.isDown()) {
				this.sprintTriggerTime = 7;
			} else {
				this.setSprinting(true);
			}
		}

		if (!this.isSprinting()
			&& (!this.isInWater() || this.isUnderWater())
			&& this.hasEnoughImpulseToStartSprinting()
			&& bl5
			&& !this.isUsingItem()
			&& !this.hasEffect(MobEffects.BLINDNESS)
			&& this.minecraft.options.keySprint.isDown()) {
			this.setSprinting(true);
		}

		if (this.isSprinting()) {
			boolean bl6 = !this.input.hasForwardImpulse() || !bl5;
			boolean bl7 = bl6 || this.horizontalCollision && !this.minorHorizontalCollision || this.isInWater() && !this.isUnderWater();
			if (this.isSwimming()) {
				if (!this.onGround && !this.input.shiftKeyDown && bl6 || !this.isInWater()) {
					this.setSprinting(false);
				}
			} else if (bl7) {
				this.setSprinting(false);
			}
		}

		boolean bl6 = false;
		if (this.getAbilities().mayfly) {
			if (this.minecraft.gameMode.isAlwaysFlying()) {
				if (!this.getAbilities().flying) {
					this.getAbilities().flying = true;
					bl6 = true;
					this.onUpdateAbilities();
				}
			} else if (!bl && this.input.jumping && !bl4) {
				if (this.jumpTriggerTime == 0) {
					this.jumpTriggerTime = 7;
				} else if (!this.isSwimming()) {
					this.getAbilities().flying = !this.getAbilities().flying;
					bl6 = true;
					this.onUpdateAbilities();
					this.jumpTriggerTime = 0;
				}
			}
		}

		if (this.input.jumping && !bl6 && !bl && !this.getAbilities().flying && !this.isPassenger() && !this.onClimbable()) {
			ItemStack itemStack = this.getItemBySlot(EquipmentSlot.CHEST);
			if (itemStack.is(Items.ELYTRA) && ElytraItem.isFlyEnabled(itemStack) && this.tryToStartFallFlying()) {
				this.connection.send(new ServerboundPlayerCommandPacket(this, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
			}
		}

		this.wasFallFlying = this.isFallFlying();
		if (this.isInWater() && this.input.shiftKeyDown && this.isAffectedByFluids()) {
			this.goDownInWater();
		}

		if (this.isEyeInFluid(FluidTags.WATER)) {
			int i = this.isSpectator() ? 10 : 1;
			this.waterVisionTime = Mth.clamp(this.waterVisionTime + i, 0, 600);
		} else if (this.waterVisionTime > 0) {
			this.isEyeInFluid(FluidTags.WATER);
			this.waterVisionTime = Mth.clamp(this.waterVisionTime - 10, 0, 600);
		}

		if (this.getAbilities().flying && this.isControlledCamera()) {
			int i = 0;
			if (this.input.shiftKeyDown) {
				i--;
			}

			if (this.input.jumping) {
				i++;
			}

			if (i != 0) {
				this.setDeltaMovement(this.getDeltaMovement().add(0.0, (double)((float)i * this.getAbilities().getFlyingSpeed() * 3.0F), 0.0));
			}
		}

		if (this.isRidingJumpable()) {
			PlayerRideableJumping playerRideableJumping = (PlayerRideableJumping)this.getVehicle();
			if (this.jumpRidingTicks < 0) {
				this.jumpRidingTicks++;
				if (this.jumpRidingTicks == 0) {
					this.jumpRidingScale = 0.0F;
				}
			}

			if (bl && !this.input.jumping) {
				this.jumpRidingTicks = -10;
				playerRideableJumping.onPlayerJump(Mth.floor(this.getJumpRidingScale() * 100.0F));
				this.sendRidingJump();
			} else if (!bl && this.input.jumping) {
				this.jumpRidingTicks = 0;
				this.jumpRidingScale = 0.0F;
			} else if (bl) {
				this.jumpRidingTicks++;
				if (this.jumpRidingTicks < 10) {
					this.jumpRidingScale = (float)this.jumpRidingTicks * 0.1F;
				} else {
					this.jumpRidingScale = 0.8F + 2.0F / (float)(this.jumpRidingTicks - 9) * 0.1F;
				}
			}
		} else {
			this.jumpRidingScale = 0.0F;
		}

		super.aiStep();
		if (this.onGround && this.getAbilities().flying && !this.minecraft.gameMode.isAlwaysFlying()) {
			this.getAbilities().flying = false;
			this.onUpdateAbilities();
		}
	}

	@Override
	protected void tickDeath() {
		this.deathTime++;
		if (this.deathTime == 20) {
			this.remove(Entity.RemovalReason.KILLED);
		}
	}

	private void handleNetherPortalClient() {
		this.oPortalTime = this.portalTime;
		if (this.isInsidePortal) {
			if (this.minecraft.screen != null
				&& !this.minecraft.screen.isPauseScreen()
				&& !(this.minecraft.screen instanceof DeathScreen)
				&& !(this.minecraft.screen instanceof ReceivingLevelScreen)) {
				if (this.minecraft.screen instanceof AbstractContainerScreen) {
					this.closeContainer();
				}

				this.minecraft.setScreen(null);
			}

			if (this.portalTime == 0.0F) {
				this.minecraft.getSoundManager().play(SimpleSoundInstance.forLocalAmbience(SoundEvents.PORTAL_TRIGGER, this.random.nextFloat() * 0.4F + 0.8F, 0.25F));
			}

			this.portalTime += 0.0125F;
			if (this.portalTime >= 1.0F) {
				this.portalTime = 1.0F;
			}

			this.isInsidePortal = false;
		} else if (this.hasEffect(MobEffects.CONFUSION) && this.getEffect(MobEffects.CONFUSION).getDuration() > 60) {
			this.portalTime += 0.006666667F;
			if (this.portalTime > 1.0F) {
				this.portalTime = 1.0F;
			}
		} else {
			if (this.portalTime > 0.0F) {
				this.portalTime -= 0.05F;
			}

			if (this.portalTime < 0.0F) {
				this.portalTime = 0.0F;
			}
		}

		this.processPortalCooldown();
	}

	@Override
	public void rideTick() {
		super.rideTick();
		this.handsBusy = false;
		if (this.getVehicle() instanceof Boat) {
			Boat boat = (Boat)this.getVehicle();
			boat.setInput(this.input.left, this.input.right, this.input.up, this.input.down);
			this.handsBusy = this.handsBusy | (this.input.left || this.input.right || this.input.up || this.input.down);
		}
	}

	public boolean isHandsBusy() {
		return this.handsBusy;
	}

	@Nullable
	@Override
	public MobEffectInstance removeEffectNoUpdate(@Nullable MobEffect mobEffect) {
		if (mobEffect == MobEffects.CONFUSION) {
			this.oPortalTime = 0.0F;
			this.portalTime = 0.0F;
		}

		return super.removeEffectNoUpdate(mobEffect);
	}

	@Override
	public void move(MoverType moverType, Vec3 vec3) {
		double d = this.getX();
		double e = this.getZ();
		super.move(moverType, vec3);
		this.updateAutoJump((float)(this.getX() - d), (float)(this.getZ() - e));
	}

	public boolean isAutoJumpEnabled() {
		return this.autoJumpEnabled;
	}

	protected void updateAutoJump(float f, float g) {
		if (this.canAutoJump()) {
			Vec3 vec3 = this.position();
			Vec3 vec32 = vec3.add((double)f, 0.0, (double)g);
			Vec3 vec33 = new Vec3((double)f, 0.0, (double)g);
			float h = this.getSpeed();
			float i = (float)vec33.lengthSqr();
			if (i <= 0.001F) {
				Vec2 vec2 = this.input.getMoveVector();
				float j = h * vec2.x;
				float k = h * vec2.y;
				float l = Mth.sin(this.getYRot() * (float) (Math.PI / 180.0));
				float m = Mth.cos(this.getYRot() * (float) (Math.PI / 180.0));
				vec33 = new Vec3((double)(j * m - k * l), vec33.y, (double)(k * m + j * l));
				i = (float)vec33.lengthSqr();
				if (i <= 0.001F) {
					return;
				}
			}

			float n = Mth.fastInvSqrt(i);
			Vec3 vec34 = vec33.scale((double)n);
			Vec3 vec35 = this.getForward();
			float l = (float)(vec35.x * vec34.x + vec35.z * vec34.z);
			if (!(l < -0.15F)) {
				CollisionContext collisionContext = CollisionContext.of(this);
				BlockPos blockPos = new BlockPos(this.getX(), this.getBoundingBox().maxY, this.getZ());
				BlockState blockState = this.level.getBlockState(blockPos);
				if (blockState.getCollisionShape(this.level, blockPos, collisionContext).isEmpty()) {
					blockPos = blockPos.above();
					BlockState blockState2 = this.level.getBlockState(blockPos);
					if (blockState2.getCollisionShape(this.level, blockPos, collisionContext).isEmpty()) {
						float o = 7.0F;
						float p = 1.2F;
						if (this.hasEffect(MobEffects.JUMP)) {
							p += (float)(this.getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.75F;
						}

						float q = Math.max(h * 7.0F, 1.0F / n);
						Vec3 vec37 = vec32.add(vec34.scale((double)q));
						float r = this.getBbWidth();
						float s = this.getBbHeight();
						AABB aABB = new AABB(vec3, vec37.add(0.0, (double)s, 0.0)).inflate((double)r, 0.0, (double)r);
						Vec3 vec36 = vec3.add(0.0, 0.51F, 0.0);
						vec37 = vec37.add(0.0, 0.51F, 0.0);
						Vec3 vec38 = vec34.cross(new Vec3(0.0, 1.0, 0.0));
						Vec3 vec39 = vec38.scale((double)(r * 0.5F));
						Vec3 vec310 = vec36.subtract(vec39);
						Vec3 vec311 = vec37.subtract(vec39);
						Vec3 vec312 = vec36.add(vec39);
						Vec3 vec313 = vec37.add(vec39);
						Iterable<VoxelShape> iterable = this.level.getCollisions(this, aABB);
						Iterator<AABB> iterator = StreamSupport.stream(iterable.spliterator(), false).flatMap(voxelShapex -> voxelShapex.toAabbs().stream()).iterator();
						float t = Float.MIN_VALUE;

						while (iterator.hasNext()) {
							AABB aABB2 = (AABB)iterator.next();
							if (aABB2.intersects(vec310, vec311) || aABB2.intersects(vec312, vec313)) {
								t = (float)aABB2.maxY;
								Vec3 vec314 = aABB2.getCenter();
								BlockPos blockPos2 = new BlockPos(vec314);

								for (int u = 1; (float)u < p; u++) {
									BlockPos blockPos3 = blockPos2.above(u);
									BlockState blockState3 = this.level.getBlockState(blockPos3);
									VoxelShape voxelShape;
									if (!(voxelShape = blockState3.getCollisionShape(this.level, blockPos3, collisionContext)).isEmpty()) {
										t = (float)voxelShape.max(Direction.Axis.Y) + (float)blockPos3.getY();
										if ((double)t - this.getY() > (double)p) {
											return;
										}
									}

									if (u > 1) {
										blockPos = blockPos.above();
										BlockState blockState4 = this.level.getBlockState(blockPos);
										if (!blockState4.getCollisionShape(this.level, blockPos, collisionContext).isEmpty()) {
											return;
										}
									}
								}
								break;
							}
						}

						if (t != Float.MIN_VALUE) {
							float v = (float)((double)t - this.getY());
							if (!(v <= 0.5F) && !(v > p)) {
								this.autoJumpTime = 1;
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected boolean isHorizontalCollisionMinor(Vec3 vec3) {
		float f = this.getYRot() * (float) (Math.PI / 180.0);
		double d = (double)Mth.sin(f);
		double e = (double)Mth.cos(f);
		double g = (double)this.xxa * e - (double)this.zza * d;
		double h = (double)this.zza * e + (double)this.xxa * d;
		double i = Mth.square(g) + Mth.square(h);
		double j = Mth.square(vec3.x) + Mth.square(vec3.z);
		if (!(i < 1.0E-5F) && !(j < 1.0E-5F)) {
			double k = g * vec3.x + h * vec3.z;
			double l = Math.acos(k / Math.sqrt(i * j));
			return l < 0.13962634F;
		} else {
			return false;
		}
	}

	private boolean canAutoJump() {
		return this.isAutoJumpEnabled()
			&& this.autoJumpTime <= 0
			&& this.onGround
			&& !this.isStayingOnGroundSurface()
			&& !this.isPassenger()
			&& this.isMoving()
			&& (double)this.getBlockJumpFactor() >= 1.0;
	}

	private boolean isMoving() {
		Vec2 vec2 = this.input.getMoveVector();
		return vec2.x != 0.0F || vec2.y != 0.0F;
	}

	private boolean hasEnoughImpulseToStartSprinting() {
		double d = 0.8;
		return this.isUnderWater() ? this.input.hasForwardImpulse() : (double)this.input.forwardImpulse >= 0.8;
	}

	public float getWaterVision() {
		if (!this.isEyeInFluid(FluidTags.WATER)) {
			return 0.0F;
		} else {
			float f = 600.0F;
			float g = 100.0F;
			if ((float)this.waterVisionTime >= 600.0F) {
				return 1.0F;
			} else {
				float h = Mth.clamp((float)this.waterVisionTime / 100.0F, 0.0F, 1.0F);
				float i = (float)this.waterVisionTime < 100.0F ? 0.0F : Mth.clamp(((float)this.waterVisionTime - 100.0F) / 500.0F, 0.0F, 1.0F);
				return h * 0.6F + i * 0.39999998F;
			}
		}
	}

	@Override
	public boolean isUnderWater() {
		return this.wasUnderwater;
	}

	@Override
	protected boolean updateIsUnderwater() {
		boolean bl = this.wasUnderwater;
		boolean bl2 = super.updateIsUnderwater();
		if (this.isSpectator()) {
			return this.wasUnderwater;
		} else {
			if (!bl && bl2) {
				this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundSource.AMBIENT, 1.0F, 1.0F, false);
				this.minecraft.getSoundManager().play(new UnderwaterAmbientSoundInstances.UnderwaterAmbientSoundInstance(this));
			}

			if (bl && !bl2) {
				this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.AMBIENT_UNDERWATER_EXIT, SoundSource.AMBIENT, 1.0F, 1.0F, false);
			}

			return this.wasUnderwater;
		}
	}

	@Override
	public Vec3 getRopeHoldPosition(float f) {
		if (this.minecraft.options.getCameraType().isFirstPerson()) {
			float g = Mth.lerp(f * 0.5F, this.getYRot(), this.yRotO) * (float) (Math.PI / 180.0);
			float h = Mth.lerp(f * 0.5F, this.getXRot(), this.xRotO) * (float) (Math.PI / 180.0);
			double d = this.getMainArm() == HumanoidArm.RIGHT ? -1.0 : 1.0;
			Vec3 vec3 = new Vec3(0.39 * d, -0.6, 0.3);
			return vec3.xRot(-h).yRot(-g).add(this.getEyePosition(f));
		} else {
			return super.getRopeHoldPosition(f);
		}
	}

	@Override
	public void updateTutorialInventoryAction(ItemStack itemStack, ItemStack itemStack2, ClickAction clickAction) {
		this.minecraft.getTutorial().onInventoryAction(itemStack, itemStack2, clickAction);
	}
}

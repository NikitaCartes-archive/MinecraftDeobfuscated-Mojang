package net.minecraft.server.level;

import com.google.common.net.InetAddresses;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEndPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEnterPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerRotationPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundServerDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetCursorItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.ServerItemCooldowns;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.slf4j.Logger;

public class ServerPlayer extends Player {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int NEUTRAL_MOB_DEATH_NOTIFICATION_RADII_XZ = 32;
	private static final int NEUTRAL_MOB_DEATH_NOTIFICATION_RADII_Y = 10;
	private static final int FLY_STAT_RECORDING_SPEED = 25;
	public static final double BLOCK_INTERACTION_DISTANCE_VERIFICATION_BUFFER = 1.0;
	public static final double ENTITY_INTERACTION_DISTANCE_VERIFICATION_BUFFER = 3.0;
	public static final int ENDER_PEARL_TICKET_RADIUS = 2;
	public static final String ENDER_PEARLS_TAG = "ender_pearls";
	public static final String ENDER_PEARL_DIMENSION_TAG = "ender_pearl_dimension";
	private static final AttributeModifier CREATIVE_BLOCK_INTERACTION_RANGE_MODIFIER = new AttributeModifier(
		ResourceLocation.withDefaultNamespace("creative_mode_block_range"), 0.5, AttributeModifier.Operation.ADD_VALUE
	);
	private static final AttributeModifier CREATIVE_ENTITY_INTERACTION_RANGE_MODIFIER = new AttributeModifier(
		ResourceLocation.withDefaultNamespace("creative_mode_entity_range"), 2.0, AttributeModifier.Operation.ADD_VALUE
	);
	public ServerGamePacketListenerImpl connection;
	public final MinecraftServer server;
	public final ServerPlayerGameMode gameMode;
	private final PlayerAdvancements advancements;
	private final ServerStatsCounter stats;
	private float lastRecordedHealthAndAbsorption = Float.MIN_VALUE;
	private int lastRecordedFoodLevel = Integer.MIN_VALUE;
	private int lastRecordedAirLevel = Integer.MIN_VALUE;
	private int lastRecordedArmor = Integer.MIN_VALUE;
	private int lastRecordedLevel = Integer.MIN_VALUE;
	private int lastRecordedExperience = Integer.MIN_VALUE;
	private float lastSentHealth = -1.0E8F;
	private int lastSentFood = -99999999;
	private boolean lastFoodSaturationZero = true;
	private int lastSentExp = -99999999;
	private int spawnInvulnerableTime = 60;
	private ChatVisiblity chatVisibility = ChatVisiblity.FULL;
	private ParticleStatus particleStatus = ParticleStatus.ALL;
	private boolean canChatColor = true;
	private long lastActionTime = Util.getMillis();
	@Nullable
	private Entity camera;
	private boolean isChangingDimension;
	public boolean seenCredits;
	private final ServerRecipeBook recipeBook;
	@Nullable
	private Vec3 levitationStartPos;
	private int levitationStartTime;
	private boolean disconnected;
	private int requestedViewDistance = 2;
	private String language = "en_us";
	@Nullable
	private Vec3 startingToFallPosition;
	@Nullable
	private Vec3 enteredNetherPosition;
	@Nullable
	private Vec3 enteredLavaOnVehiclePosition;
	private SectionPos lastSectionPos = SectionPos.of(0, 0, 0);
	private ChunkTrackingView chunkTrackingView = ChunkTrackingView.EMPTY;
	private ResourceKey<Level> respawnDimension = Level.OVERWORLD;
	@Nullable
	private BlockPos respawnPosition;
	private boolean respawnForced;
	private float respawnAngle;
	private final TextFilter textFilter;
	private boolean textFilteringEnabled;
	private boolean allowsListing;
	private boolean spawnExtraParticlesOnFall;
	private WardenSpawnTracker wardenSpawnTracker = new WardenSpawnTracker(0, 0, 0);
	@Nullable
	private BlockPos raidOmenPosition;
	private Vec3 lastKnownClientMovement = Vec3.ZERO;
	private Input lastClientInput = Input.EMPTY;
	private final Set<ThrownEnderpearl> enderPearls = new HashSet();
	private final ContainerSynchronizer containerSynchronizer = new ContainerSynchronizer() {
		@Override
		public void sendInitialData(AbstractContainerMenu abstractContainerMenu, NonNullList<ItemStack> nonNullList, ItemStack itemStack, int[] is) {
			ServerPlayer.this.connection
				.send(new ClientboundContainerSetContentPacket(abstractContainerMenu.containerId, abstractContainerMenu.incrementStateId(), nonNullList, itemStack));

			for (int i = 0; i < is.length; i++) {
				this.broadcastDataValue(abstractContainerMenu, i, is[i]);
			}
		}

		@Override
		public void sendSlotChange(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
			ServerPlayer.this.connection
				.send(new ClientboundContainerSetSlotPacket(abstractContainerMenu.containerId, abstractContainerMenu.incrementStateId(), i, itemStack));
		}

		@Override
		public void sendCarriedChange(AbstractContainerMenu abstractContainerMenu, ItemStack itemStack) {
			ServerPlayer.this.connection.send(new ClientboundSetCursorItemPacket(itemStack.copy()));
		}

		@Override
		public void sendDataChange(AbstractContainerMenu abstractContainerMenu, int i, int j) {
			this.broadcastDataValue(abstractContainerMenu, i, j);
		}

		private void broadcastDataValue(AbstractContainerMenu abstractContainerMenu, int i, int j) {
			ServerPlayer.this.connection.send(new ClientboundContainerSetDataPacket(abstractContainerMenu.containerId, i, j));
		}
	};
	private final ContainerListener containerListener = new ContainerListener() {
		@Override
		public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
			Slot slot = abstractContainerMenu.getSlot(i);
			if (!(slot instanceof ResultSlot)) {
				if (slot.container == ServerPlayer.this.getInventory()) {
					CriteriaTriggers.INVENTORY_CHANGED.trigger(ServerPlayer.this, ServerPlayer.this.getInventory(), itemStack);
				}
			}
		}

		@Override
		public void dataChanged(AbstractContainerMenu abstractContainerMenu, int i, int j) {
		}
	};
	@Nullable
	private RemoteChatSession chatSession;
	@Nullable
	public final Object object;
	private final CommandSource commandSource = new CommandSource() {
		@Override
		public boolean acceptsSuccess() {
			return ServerPlayer.this.serverLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK);
		}

		@Override
		public boolean acceptsFailure() {
			return true;
		}

		@Override
		public boolean shouldInformAdmins() {
			return true;
		}

		@Override
		public void sendSystemMessage(Component component) {
			ServerPlayer.this.sendSystemMessage(component);
		}
	};
	private int containerCounter;
	public boolean wonGame;

	public ServerPlayer(MinecraftServer minecraftServer, ServerLevel serverLevel, GameProfile gameProfile, ClientInformation clientInformation) {
		super(serverLevel, serverLevel.getSharedSpawnPos(), serverLevel.getSharedSpawnAngle(), gameProfile);
		this.textFilter = minecraftServer.createTextFilterForPlayer(this);
		this.gameMode = minecraftServer.createGameModeForPlayer(this);
		this.recipeBook = new ServerRecipeBook((resourceKey, consumer) -> minecraftServer.getRecipeManager().listDisplaysForRecipe(resourceKey, consumer));
		this.server = minecraftServer;
		this.stats = minecraftServer.getPlayerList().getPlayerStats(this);
		this.advancements = minecraftServer.getPlayerList().getPlayerAdvancements(this);
		this.moveTo(this.adjustSpawnLocation(serverLevel, serverLevel.getSharedSpawnPos()).getBottomCenter(), 0.0F, 0.0F);
		this.updateOptions(clientInformation);
		this.object = null;
	}

	@Override
	public BlockPos adjustSpawnLocation(ServerLevel serverLevel, BlockPos blockPos) {
		AABB aABB = this.getDimensions(Pose.STANDING).makeBoundingBox(Vec3.ZERO);
		BlockPos blockPos2 = blockPos;
		if (serverLevel.dimensionType().hasSkyLight() && serverLevel.getServer().getWorldData().getGameType() != GameType.ADVENTURE) {
			int i = Math.max(0, this.server.getSpawnRadius(serverLevel));
			int j = Mth.floor(serverLevel.getWorldBorder().getDistanceToBorder((double)blockPos.getX(), (double)blockPos.getZ()));
			if (j < i) {
				i = j;
			}

			if (j <= 1) {
				i = 1;
			}

			long l = (long)(i * 2 + 1);
			long m = l * l;
			int k = m > 2147483647L ? Integer.MAX_VALUE : (int)m;
			int n = this.getCoprime(k);
			int o = RandomSource.create().nextInt(k);

			for (int p = 0; p < k; p++) {
				int q = (o + n * p) % k;
				int r = q % (i * 2 + 1);
				int s = q / (i * 2 + 1);
				int t = blockPos.getX() + r - i;
				int u = blockPos.getZ() + s - i;

				try {
					blockPos2 = PlayerRespawnLogic.getOverworldRespawnPos(serverLevel, t, u);
					if (blockPos2 != null && this.noCollisionNoLiquid(serverLevel, aABB.move(blockPos2.getBottomCenter()))) {
						return blockPos2;
					}
				} catch (Exception var25) {
					int v = p;
					int w = i;
					CrashReport crashReport = CrashReport.forThrowable(var25, "Searching for spawn");
					CrashReportCategory crashReportCategory = crashReport.addCategory("Spawn Lookup");
					crashReportCategory.setDetail("Origin", blockPos::toString);
					crashReportCategory.setDetail("Radius", (CrashReportDetail<String>)(() -> Integer.toString(w)));
					crashReportCategory.setDetail("Candidate", (CrashReportDetail<String>)(() -> "[" + t + "," + u + "]"));
					crashReportCategory.setDetail("Progress", (CrashReportDetail<String>)(() -> v + " out of " + k));
					throw new ReportedException(crashReport);
				}
			}

			blockPos2 = blockPos;
		}

		while (!this.noCollisionNoLiquid(serverLevel, aABB.move(blockPos2.getBottomCenter())) && blockPos2.getY() < serverLevel.getMaxY()) {
			blockPos2 = blockPos2.above();
		}

		while (this.noCollisionNoLiquid(serverLevel, aABB.move(blockPos2.below().getBottomCenter())) && blockPos2.getY() > serverLevel.getMinY() + 1) {
			blockPos2 = blockPos2.below();
		}

		return blockPos2;
	}

	private boolean noCollisionNoLiquid(ServerLevel serverLevel, AABB aABB) {
		return serverLevel.noCollision(this, aABB, true);
	}

	private int getCoprime(int i) {
		return i <= 16 ? i - 1 : 17;
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("warden_spawn_tracker", 10)) {
			WardenSpawnTracker.CODEC
				.parse(new Dynamic<>(NbtOps.INSTANCE, compoundTag.get("warden_spawn_tracker")))
				.resultOrPartial(LOGGER::error)
				.ifPresent(wardenSpawnTracker -> this.wardenSpawnTracker = wardenSpawnTracker);
		}

		if (compoundTag.contains("enteredNetherPosition", 10)) {
			CompoundTag compoundTag2 = compoundTag.getCompound("enteredNetherPosition");
			this.enteredNetherPosition = new Vec3(compoundTag2.getDouble("x"), compoundTag2.getDouble("y"), compoundTag2.getDouble("z"));
		}

		this.seenCredits = compoundTag.getBoolean("seenCredits");
		if (compoundTag.contains("recipeBook", 10)) {
			this.recipeBook.fromNbt(compoundTag.getCompound("recipeBook"), resourceKey -> this.server.getRecipeManager().byKey(resourceKey).isPresent());
		}

		if (this.isSleeping()) {
			this.stopSleeping();
		}

		if (compoundTag.contains("SpawnX", 99) && compoundTag.contains("SpawnY", 99) && compoundTag.contains("SpawnZ", 99)) {
			this.respawnPosition = new BlockPos(compoundTag.getInt("SpawnX"), compoundTag.getInt("SpawnY"), compoundTag.getInt("SpawnZ"));
			this.respawnForced = compoundTag.getBoolean("SpawnForced");
			this.respawnAngle = compoundTag.getFloat("SpawnAngle");
			if (compoundTag.contains("SpawnDimension")) {
				this.respawnDimension = (ResourceKey<Level>)Level.RESOURCE_KEY_CODEC
					.parse(NbtOps.INSTANCE, compoundTag.get("SpawnDimension"))
					.resultOrPartial(LOGGER::error)
					.orElse(Level.OVERWORLD);
			}
		}

		this.spawnExtraParticlesOnFall = compoundTag.getBoolean("spawn_extra_particles_on_fall");
		Tag tag = compoundTag.get("raid_omen_position");
		if (tag != null) {
			BlockPos.CODEC.parse(NbtOps.INSTANCE, tag).resultOrPartial(LOGGER::error).ifPresent(blockPos -> this.raidOmenPosition = blockPos);
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		WardenSpawnTracker.CODEC
			.encodeStart(NbtOps.INSTANCE, this.wardenSpawnTracker)
			.resultOrPartial(LOGGER::error)
			.ifPresent(tag -> compoundTag.put("warden_spawn_tracker", tag));
		this.storeGameTypes(compoundTag);
		compoundTag.putBoolean("seenCredits", this.seenCredits);
		if (this.enteredNetherPosition != null) {
			CompoundTag compoundTag2 = new CompoundTag();
			compoundTag2.putDouble("x", this.enteredNetherPosition.x);
			compoundTag2.putDouble("y", this.enteredNetherPosition.y);
			compoundTag2.putDouble("z", this.enteredNetherPosition.z);
			compoundTag.put("enteredNetherPosition", compoundTag2);
		}

		this.saveParentVehicle(compoundTag);
		compoundTag.put("recipeBook", this.recipeBook.toNbt());
		compoundTag.putString("Dimension", this.level().dimension().location().toString());
		if (this.respawnPosition != null) {
			compoundTag.putInt("SpawnX", this.respawnPosition.getX());
			compoundTag.putInt("SpawnY", this.respawnPosition.getY());
			compoundTag.putInt("SpawnZ", this.respawnPosition.getZ());
			compoundTag.putBoolean("SpawnForced", this.respawnForced);
			compoundTag.putFloat("SpawnAngle", this.respawnAngle);
			ResourceLocation.CODEC
				.encodeStart(NbtOps.INSTANCE, this.respawnDimension.location())
				.resultOrPartial(LOGGER::error)
				.ifPresent(tag -> compoundTag.put("SpawnDimension", tag));
		}

		compoundTag.putBoolean("spawn_extra_particles_on_fall", this.spawnExtraParticlesOnFall);
		if (this.raidOmenPosition != null) {
			BlockPos.CODEC
				.encodeStart(NbtOps.INSTANCE, this.raidOmenPosition)
				.resultOrPartial(LOGGER::error)
				.ifPresent(tag -> compoundTag.put("raid_omen_position", tag));
		}

		this.saveEnderPearls(compoundTag);
	}

	private void saveParentVehicle(CompoundTag compoundTag) {
		Entity entity = this.getRootVehicle();
		Entity entity2 = this.getVehicle();
		if (entity2 != null && entity != this && entity.hasExactlyOnePlayerPassenger()) {
			CompoundTag compoundTag2 = new CompoundTag();
			CompoundTag compoundTag3 = new CompoundTag();
			entity.save(compoundTag3);
			compoundTag2.putUUID("Attach", entity2.getUUID());
			compoundTag2.put("Entity", compoundTag3);
			compoundTag.put("RootVehicle", compoundTag2);
		}
	}

	public void loadAndSpawnParentVehicle(Optional<CompoundTag> optional) {
		if (optional.isPresent() && ((CompoundTag)optional.get()).contains("RootVehicle", 10) && this.level() instanceof ServerLevel serverLevel) {
			CompoundTag compoundTag = ((CompoundTag)optional.get()).getCompound("RootVehicle");
			Entity entity = EntityType.loadEntityRecursive(
				compoundTag.getCompound("Entity"), serverLevel, EntitySpawnReason.LOAD, entityx -> !serverLevel.addWithUUID(entityx) ? null : entityx
			);
			if (entity == null) {
				return;
			}

			UUID uUID;
			if (compoundTag.hasUUID("Attach")) {
				uUID = compoundTag.getUUID("Attach");
			} else {
				uUID = null;
			}

			if (entity.getUUID().equals(uUID)) {
				this.startRiding(entity, true);
			} else {
				for (Entity entity2 : entity.getIndirectPassengers()) {
					if (entity2.getUUID().equals(uUID)) {
						this.startRiding(entity2, true);
						break;
					}
				}
			}

			if (!this.isPassenger()) {
				LOGGER.warn("Couldn't reattach entity to player");
				entity.discard();

				for (Entity entity2x : entity.getIndirectPassengers()) {
					entity2x.discard();
				}
			}
		}
	}

	private void saveEnderPearls(CompoundTag compoundTag) {
		if (!this.enderPearls.isEmpty()) {
			ListTag listTag = new ListTag();

			for (ThrownEnderpearl thrownEnderpearl : this.enderPearls) {
				if (thrownEnderpearl.isRemoved()) {
					LOGGER.warn("Trying to save removed ender pearl, skipping");
				} else {
					CompoundTag compoundTag2 = new CompoundTag();
					thrownEnderpearl.save(compoundTag2);
					ResourceLocation.CODEC
						.encodeStart(NbtOps.INSTANCE, thrownEnderpearl.level().dimension().location())
						.resultOrPartial(LOGGER::error)
						.ifPresent(tag -> compoundTag2.put("ender_pearl_dimension", tag));
					listTag.add(compoundTag2);
				}
			}

			compoundTag.put("ender_pearls", listTag);
		}
	}

	public void loadAndSpawnEnderpearls(Optional<CompoundTag> optional) {
		if (optional.isPresent()
			&& ((CompoundTag)optional.get()).contains("ender_pearls", 9)
			&& ((CompoundTag)optional.get()).get("ender_pearls") instanceof ListTag listTag) {
			listTag.forEach(
				tag -> {
					if (tag instanceof CompoundTag compoundTag && compoundTag.contains("ender_pearl_dimension")) {
						Optional<ResourceKey<Level>> optionalx = Level.RESOURCE_KEY_CODEC
							.parse(NbtOps.INSTANCE, compoundTag.get("ender_pearl_dimension"))
							.resultOrPartial(LOGGER::error);
						if (optionalx.isEmpty()) {
							LOGGER.warn("No dimension defined for ender pearl, skipping");
							return;
						}

						ServerLevel serverLevel = this.level().getServer().getLevel((ResourceKey<Level>)optionalx.get());
						if (serverLevel != null) {
							Entity entity = EntityType.loadEntityRecursive(
								compoundTag, serverLevel, EntitySpawnReason.LOAD, entityx -> !serverLevel.addWithUUID(entityx) ? null : entityx
							);
							if (entity != null) {
								placeEnderPearlTicket(serverLevel, entity.chunkPosition());
							} else {
								LOGGER.warn("Failed to spawn player ender pearl in level ({}), skipping", optionalx.get());
							}
						} else {
							LOGGER.warn("Trying to load ender pearl without level ({}) being loaded, skipping", optionalx.get());
						}
					}
				}
			);
		}
	}

	public void setExperiencePoints(int i) {
		float f = (float)this.getXpNeededForNextLevel();
		float g = (f - 1.0F) / f;
		this.experienceProgress = Mth.clamp((float)i / f, 0.0F, g);
		this.lastSentExp = -1;
	}

	public void setExperienceLevels(int i) {
		this.experienceLevel = i;
		this.lastSentExp = -1;
	}

	@Override
	public void giveExperienceLevels(int i) {
		super.giveExperienceLevels(i);
		this.lastSentExp = -1;
	}

	@Override
	public void onEnchantmentPerformed(ItemStack itemStack, int i) {
		super.onEnchantmentPerformed(itemStack, i);
		this.lastSentExp = -1;
	}

	private void initMenu(AbstractContainerMenu abstractContainerMenu) {
		abstractContainerMenu.addSlotListener(this.containerListener);
		abstractContainerMenu.setSynchronizer(this.containerSynchronizer);
	}

	public void initInventoryMenu() {
		this.initMenu(this.inventoryMenu);
	}

	@Override
	public void onEnterCombat() {
		super.onEnterCombat();
		this.connection.send(ClientboundPlayerCombatEnterPacket.INSTANCE);
	}

	@Override
	public void onLeaveCombat() {
		super.onLeaveCombat();
		this.connection.send(new ClientboundPlayerCombatEndPacket(this.getCombatTracker()));
	}

	@Override
	public void onInsideBlock(BlockState blockState) {
		CriteriaTriggers.ENTER_BLOCK.trigger(this, blockState);
	}

	@Override
	protected ItemCooldowns createItemCooldowns() {
		return new ServerItemCooldowns(this);
	}

	@Override
	public void tick() {
		this.gameMode.tick();
		this.wardenSpawnTracker.tick();
		this.spawnInvulnerableTime--;
		if (this.invulnerableTime > 0) {
			this.invulnerableTime--;
		}

		this.containerMenu.broadcastChanges();
		if (!this.level().isClientSide && !this.containerMenu.stillValid(this)) {
			this.closeContainer();
			this.containerMenu = this.inventoryMenu;
		}

		Entity entity = this.getCamera();
		if (entity != this) {
			if (entity.isAlive()) {
				this.absMoveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
				this.serverLevel().getChunkSource().move(this);
				if (this.wantsToStopRiding()) {
					this.setCamera(this);
				}
			} else {
				this.setCamera(this);
			}
		}

		CriteriaTriggers.TICK.trigger(this);
		if (this.levitationStartPos != null) {
			CriteriaTriggers.LEVITATION.trigger(this, this.levitationStartPos, this.tickCount - this.levitationStartTime);
		}

		this.trackStartFallingPosition();
		this.trackEnteredOrExitedLavaOnVehicle();
		this.updatePlayerAttributes();
		this.advancements.flushDirty(this);
	}

	private void updatePlayerAttributes() {
		AttributeInstance attributeInstance = this.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
		if (attributeInstance != null) {
			if (this.isCreative()) {
				attributeInstance.addOrUpdateTransientModifier(CREATIVE_BLOCK_INTERACTION_RANGE_MODIFIER);
			} else {
				attributeInstance.removeModifier(CREATIVE_BLOCK_INTERACTION_RANGE_MODIFIER);
			}
		}

		AttributeInstance attributeInstance2 = this.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
		if (attributeInstance2 != null) {
			if (this.isCreative()) {
				attributeInstance2.addOrUpdateTransientModifier(CREATIVE_ENTITY_INTERACTION_RANGE_MODIFIER);
			} else {
				attributeInstance2.removeModifier(CREATIVE_ENTITY_INTERACTION_RANGE_MODIFIER);
			}
		}
	}

	public void doTick() {
		try {
			if (!this.isSpectator() || !this.touchingUnloadedChunk()) {
				super.tick();
			}

			for (int i = 0; i < this.getInventory().getContainerSize(); i++) {
				ItemStack itemStack = this.getInventory().getItem(i);
				if (!itemStack.isEmpty()) {
					this.synchronizeSpecialItemUpdates(itemStack);
				}
			}

			if (this.getHealth() != this.lastSentHealth
				|| this.lastSentFood != this.foodData.getFoodLevel()
				|| this.foodData.getSaturationLevel() == 0.0F != this.lastFoodSaturationZero) {
				this.connection.send(new ClientboundSetHealthPacket(this.getHealth(), this.foodData.getFoodLevel(), this.foodData.getSaturationLevel()));
				this.lastSentHealth = this.getHealth();
				this.lastSentFood = this.foodData.getFoodLevel();
				this.lastFoodSaturationZero = this.foodData.getSaturationLevel() == 0.0F;
			}

			if (this.getHealth() + this.getAbsorptionAmount() != this.lastRecordedHealthAndAbsorption) {
				this.lastRecordedHealthAndAbsorption = this.getHealth() + this.getAbsorptionAmount();
				this.updateScoreForCriteria(ObjectiveCriteria.HEALTH, Mth.ceil(this.lastRecordedHealthAndAbsorption));
			}

			if (this.foodData.getFoodLevel() != this.lastRecordedFoodLevel) {
				this.lastRecordedFoodLevel = this.foodData.getFoodLevel();
				this.updateScoreForCriteria(ObjectiveCriteria.FOOD, Mth.ceil((float)this.lastRecordedFoodLevel));
			}

			if (this.getAirSupply() != this.lastRecordedAirLevel) {
				this.lastRecordedAirLevel = this.getAirSupply();
				this.updateScoreForCriteria(ObjectiveCriteria.AIR, Mth.ceil((float)this.lastRecordedAirLevel));
			}

			if (this.getArmorValue() != this.lastRecordedArmor) {
				this.lastRecordedArmor = this.getArmorValue();
				this.updateScoreForCriteria(ObjectiveCriteria.ARMOR, Mth.ceil((float)this.lastRecordedArmor));
			}

			if (this.totalExperience != this.lastRecordedExperience) {
				this.lastRecordedExperience = this.totalExperience;
				this.updateScoreForCriteria(ObjectiveCriteria.EXPERIENCE, Mth.ceil((float)this.lastRecordedExperience));
			}

			if (this.experienceLevel != this.lastRecordedLevel) {
				this.lastRecordedLevel = this.experienceLevel;
				this.updateScoreForCriteria(ObjectiveCriteria.LEVEL, Mth.ceil((float)this.lastRecordedLevel));
			}

			if (this.totalExperience != this.lastSentExp) {
				this.lastSentExp = this.totalExperience;
				this.connection.send(new ClientboundSetExperiencePacket(this.experienceProgress, this.totalExperience, this.experienceLevel));
			}

			if (this.tickCount % 20 == 0) {
				CriteriaTriggers.LOCATION.trigger(this);
			}
		} catch (Throwable var4) {
			CrashReport crashReport = CrashReport.forThrowable(var4, "Ticking player");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Player being ticked");
			this.fillCrashReportCategory(crashReportCategory);
			throw new ReportedException(crashReport);
		}
	}

	private void synchronizeSpecialItemUpdates(ItemStack itemStack) {
		MapId mapId = itemStack.get(DataComponents.MAP_ID);
		MapItemSavedData mapItemSavedData = MapItem.getSavedData(mapId, this.level());
		if (mapItemSavedData != null) {
			Packet<?> packet = mapItemSavedData.getUpdatePacket(mapId, this);
			if (packet != null) {
				this.connection.send(packet);
			}
		}
	}

	@Override
	protected void tickRegeneration() {
		if (this.level().getDifficulty() == Difficulty.PEACEFUL && this.serverLevel().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
			if (this.tickCount % 20 == 0) {
				if (this.getHealth() < this.getMaxHealth()) {
					this.heal(1.0F);
				}

				float f = this.foodData.getSaturationLevel();
				if (f < 20.0F) {
					this.foodData.setSaturation(f + 1.0F);
				}
			}

			if (this.tickCount % 10 == 0 && this.foodData.needsFood()) {
				this.foodData.setFoodLevel(this.foodData.getFoodLevel() + 1);
			}
		}
	}

	@Override
	public void resetFallDistance() {
		if (this.getHealth() > 0.0F && this.startingToFallPosition != null) {
			CriteriaTriggers.FALL_FROM_HEIGHT.trigger(this, this.startingToFallPosition);
		}

		this.startingToFallPosition = null;
		super.resetFallDistance();
	}

	public void trackStartFallingPosition() {
		if (this.fallDistance > 0.0F && this.startingToFallPosition == null) {
			this.startingToFallPosition = this.position();
			if (this.currentImpulseImpactPos != null && this.currentImpulseImpactPos.y <= this.startingToFallPosition.y) {
				CriteriaTriggers.FALL_AFTER_EXPLOSION.trigger(this, this.currentImpulseImpactPos, this.currentExplosionCause);
			}
		}
	}

	public void trackEnteredOrExitedLavaOnVehicle() {
		if (this.getVehicle() != null && this.getVehicle().isInLava()) {
			if (this.enteredLavaOnVehiclePosition == null) {
				this.enteredLavaOnVehiclePosition = this.position();
			} else {
				CriteriaTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER.trigger(this, this.enteredLavaOnVehiclePosition);
			}
		}

		if (this.enteredLavaOnVehiclePosition != null && (this.getVehicle() == null || !this.getVehicle().isInLava())) {
			this.enteredLavaOnVehiclePosition = null;
		}
	}

	private void updateScoreForCriteria(ObjectiveCriteria objectiveCriteria, int i) {
		this.getScoreboard().forAllObjectives(objectiveCriteria, this, scoreAccess -> scoreAccess.set(i));
	}

	@Override
	public void die(DamageSource damageSource) {
		this.gameEvent(GameEvent.ENTITY_DIE);
		boolean bl = this.serverLevel().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES);
		if (bl) {
			Component component = this.getCombatTracker().getDeathMessage();
			this.connection
				.send(
					new ClientboundPlayerCombatKillPacket(this.getId(), component),
					PacketSendListener.exceptionallySend(
						() -> {
							int i = 256;
							String string = component.getString(256);
							Component component2 = Component.translatable("death.attack.message_too_long", Component.literal(string).withStyle(ChatFormatting.YELLOW));
							Component component3 = Component.translatable("death.attack.even_more_magic", this.getDisplayName())
								.withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component2)));
							return new ClientboundPlayerCombatKillPacket(this.getId(), component3);
						}
					)
				);
			Team team = this.getTeam();
			if (team == null || team.getDeathMessageVisibility() == Team.Visibility.ALWAYS) {
				this.server.getPlayerList().broadcastSystemMessage(component, false);
			} else if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OTHER_TEAMS) {
				this.server.getPlayerList().broadcastSystemToTeam(this, component);
			} else if (team.getDeathMessageVisibility() == Team.Visibility.HIDE_FOR_OWN_TEAM) {
				this.server.getPlayerList().broadcastSystemToAllExceptTeam(this, component);
			}
		} else {
			this.connection.send(new ClientboundPlayerCombatKillPacket(this.getId(), CommonComponents.EMPTY));
		}

		this.removeEntitiesOnShoulder();
		if (this.serverLevel().getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
			this.tellNeutralMobsThatIDied();
		}

		if (!this.isSpectator()) {
			this.dropAllDeathLoot(this.serverLevel(), damageSource);
		}

		this.getScoreboard().forAllObjectives(ObjectiveCriteria.DEATH_COUNT, this, ScoreAccess::increment);
		LivingEntity livingEntity = this.getKillCredit();
		if (livingEntity != null) {
			this.awardStat(Stats.ENTITY_KILLED_BY.get(livingEntity.getType()));
			livingEntity.awardKillScore(this, this.deathScore, damageSource);
			this.createWitherRose(livingEntity);
		}

		this.level().broadcastEntityEvent(this, (byte)3);
		this.awardStat(Stats.DEATHS);
		this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
		this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
		this.clearFire();
		this.setTicksFrozen(0);
		this.setSharedFlagOnFire(false);
		this.getCombatTracker().recheckStatus();
		this.setLastDeathLocation(Optional.of(GlobalPos.of(this.level().dimension(), this.blockPosition())));
	}

	private void tellNeutralMobsThatIDied() {
		AABB aABB = new AABB(this.blockPosition()).inflate(32.0, 10.0, 32.0);
		this.level()
			.getEntitiesOfClass(Mob.class, aABB, EntitySelector.NO_SPECTATORS)
			.stream()
			.filter(mob -> mob instanceof NeutralMob)
			.forEach(mob -> ((NeutralMob)mob).playerDied(this.serverLevel(), this));
	}

	@Override
	public void awardKillScore(Entity entity, int i, DamageSource damageSource) {
		if (entity != this) {
			super.awardKillScore(entity, i, damageSource);
			this.increaseScore(i);
			this.getScoreboard().forAllObjectives(ObjectiveCriteria.KILL_COUNT_ALL, this, ScoreAccess::increment);
			if (entity instanceof Player) {
				this.awardStat(Stats.PLAYER_KILLS);
				this.getScoreboard().forAllObjectives(ObjectiveCriteria.KILL_COUNT_PLAYERS, this, ScoreAccess::increment);
			} else {
				this.awardStat(Stats.MOB_KILLS);
			}

			this.handleTeamKill(this, entity, ObjectiveCriteria.TEAM_KILL);
			this.handleTeamKill(entity, this, ObjectiveCriteria.KILLED_BY_TEAM);
			CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(this, entity, damageSource);
		}
	}

	private void handleTeamKill(ScoreHolder scoreHolder, ScoreHolder scoreHolder2, ObjectiveCriteria[] objectiveCriterias) {
		PlayerTeam playerTeam = this.getScoreboard().getPlayersTeam(scoreHolder2.getScoreboardName());
		if (playerTeam != null) {
			int i = playerTeam.getColor().getId();
			if (i >= 0 && i < objectiveCriterias.length) {
				this.getScoreboard().forAllObjectives(objectiveCriterias[i], scoreHolder, ScoreAccess::increment);
			}
		}
	}

	@Override
	public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
		if (this.isInvulnerableTo(serverLevel, damageSource)) {
			return false;
		} else {
			boolean bl = this.server.isDedicatedServer() && this.isPvpAllowed() && damageSource.is(DamageTypeTags.IS_FALL);
			if (!bl && this.spawnInvulnerableTime > 0 && !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
				return false;
			} else {
				Entity entity = damageSource.getEntity();
				if (entity instanceof Player player && !this.canHarmPlayer(player)) {
					return false;
				}

				if (entity instanceof AbstractArrow abstractArrow && abstractArrow.getOwner() instanceof Player player2 && !this.canHarmPlayer(player2)) {
					return false;
				}

				return super.hurtServer(serverLevel, damageSource, f);
			}
		}
	}

	@Override
	public boolean canHarmPlayer(Player player) {
		return !this.isPvpAllowed() ? false : super.canHarmPlayer(player);
	}

	private boolean isPvpAllowed() {
		return this.server.isPvpAllowed();
	}

	public TeleportTransition findRespawnPositionAndUseSpawnBlock(boolean bl, TeleportTransition.PostTeleportTransition postTeleportTransition) {
		BlockPos blockPos = this.getRespawnPosition();
		float f = this.getRespawnAngle();
		boolean bl2 = this.isRespawnForced();
		ServerLevel serverLevel = this.server.getLevel(this.getRespawnDimension());
		if (serverLevel != null && blockPos != null) {
			Optional<ServerPlayer.RespawnPosAngle> optional = findRespawnAndUseSpawnBlock(serverLevel, blockPos, f, bl2, bl);
			if (optional.isPresent()) {
				ServerPlayer.RespawnPosAngle respawnPosAngle = (ServerPlayer.RespawnPosAngle)optional.get();
				return new TeleportTransition(serverLevel, respawnPosAngle.position(), Vec3.ZERO, respawnPosAngle.yaw(), 0.0F, postTeleportTransition);
			} else {
				return TeleportTransition.missingRespawnBlock(this.server.overworld(), this, postTeleportTransition);
			}
		} else {
			return new TeleportTransition(this.server.overworld(), this, postTeleportTransition);
		}
	}

	private static Optional<ServerPlayer.RespawnPosAngle> findRespawnAndUseSpawnBlock(ServerLevel serverLevel, BlockPos blockPos, float f, boolean bl, boolean bl2) {
		BlockState blockState = serverLevel.getBlockState(blockPos);
		Block block = blockState.getBlock();
		if (block instanceof RespawnAnchorBlock && (bl || (Integer)blockState.getValue(RespawnAnchorBlock.CHARGE) > 0) && RespawnAnchorBlock.canSetSpawn(serverLevel)
			)
		 {
			Optional<Vec3> optional = RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, serverLevel, blockPos);
			if (!bl && bl2 && optional.isPresent()) {
				serverLevel.setBlock(
					blockPos, blockState.setValue(RespawnAnchorBlock.CHARGE, Integer.valueOf((Integer)blockState.getValue(RespawnAnchorBlock.CHARGE) - 1)), 3
				);
			}

			return optional.map(vec3 -> ServerPlayer.RespawnPosAngle.of(vec3, blockPos));
		} else if (block instanceof BedBlock && BedBlock.canSetSpawn(serverLevel)) {
			return BedBlock.findStandUpPosition(EntityType.PLAYER, serverLevel, blockPos, blockState.getValue(BedBlock.FACING), f)
				.map(vec3 -> ServerPlayer.RespawnPosAngle.of(vec3, blockPos));
		} else if (!bl) {
			return Optional.empty();
		} else {
			boolean bl3 = block.isPossibleToRespawnInThis(blockState);
			BlockState blockState2 = serverLevel.getBlockState(blockPos.above());
			boolean bl4 = blockState2.getBlock().isPossibleToRespawnInThis(blockState2);
			return bl3 && bl4
				? Optional.of(new ServerPlayer.RespawnPosAngle(new Vec3((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.1, (double)blockPos.getZ() + 0.5), f))
				: Optional.empty();
		}
	}

	public void showEndCredits() {
		this.unRide();
		this.serverLevel().removePlayerImmediately(this, Entity.RemovalReason.CHANGED_DIMENSION);
		if (!this.wonGame) {
			this.wonGame = true;
			this.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, 0.0F));
			this.seenCredits = true;
		}
	}

	@Nullable
	public ServerPlayer teleport(TeleportTransition teleportTransition) {
		if (this.isRemoved()) {
			return null;
		} else {
			if (teleportTransition.missingRespawnBlock()) {
				this.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
			}

			ServerLevel serverLevel = teleportTransition.newLevel();
			ServerLevel serverLevel2 = this.serverLevel();
			ResourceKey<Level> resourceKey = serverLevel2.dimension();
			if (!teleportTransition.asPassenger()) {
				this.stopRiding();
			}

			if (serverLevel.dimension() == resourceKey) {
				this.connection.teleport(PositionMoveRotation.of(teleportTransition), teleportTransition.relatives());
				this.connection.resetPosition();
				teleportTransition.postTeleportTransition().onTransition(this);
				return this;
			} else {
				this.isChangingDimension = true;
				LevelData levelData = serverLevel.getLevelData();
				this.connection.send(new ClientboundRespawnPacket(this.createCommonSpawnInfo(serverLevel), (byte)3));
				this.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
				PlayerList playerList = this.server.getPlayerList();
				playerList.sendPlayerPermissionLevel(this);
				serverLevel2.removePlayerImmediately(this, Entity.RemovalReason.CHANGED_DIMENSION);
				this.unsetRemoved();
				ProfilerFiller profilerFiller = Profiler.get();
				profilerFiller.push("moving");
				if (resourceKey == Level.OVERWORLD && serverLevel.dimension() == Level.NETHER) {
					this.enteredNetherPosition = this.position();
				}

				profilerFiller.pop();
				profilerFiller.push("placing");
				this.setServerLevel(serverLevel);
				this.connection.teleport(PositionMoveRotation.of(teleportTransition), teleportTransition.relatives());
				this.connection.resetPosition();
				serverLevel.addDuringTeleport(this);
				profilerFiller.pop();
				this.triggerDimensionChangeTriggers(serverLevel2);
				this.stopUsingItem();
				this.connection.send(new ClientboundPlayerAbilitiesPacket(this.getAbilities()));
				playerList.sendLevelInfo(this, serverLevel);
				playerList.sendAllPlayerInfo(this);
				playerList.sendActivePlayerEffects(this);
				teleportTransition.postTeleportTransition().onTransition(this);
				this.lastSentExp = -1;
				this.lastSentHealth = -1.0F;
				this.lastSentFood = -1;
				return this;
			}
		}
	}

	@Override
	public void forceSetRotation(float f, float g) {
		this.connection.send(new ClientboundPlayerRotationPacket(f, g));
	}

	private void triggerDimensionChangeTriggers(ServerLevel serverLevel) {
		ResourceKey<Level> resourceKey = serverLevel.dimension();
		ResourceKey<Level> resourceKey2 = this.level().dimension();
		CriteriaTriggers.CHANGED_DIMENSION.trigger(this, resourceKey, resourceKey2);
		if (resourceKey == Level.NETHER && resourceKey2 == Level.OVERWORLD && this.enteredNetherPosition != null) {
			CriteriaTriggers.NETHER_TRAVEL.trigger(this, this.enteredNetherPosition);
		}

		if (resourceKey2 != Level.NETHER) {
			this.enteredNetherPosition = null;
		}
	}

	@Override
	public boolean broadcastToPlayer(ServerPlayer serverPlayer) {
		if (serverPlayer.isSpectator()) {
			return this.getCamera() == this;
		} else {
			return this.isSpectator() ? false : super.broadcastToPlayer(serverPlayer);
		}
	}

	@Override
	public void take(Entity entity, int i) {
		super.take(entity, i);
		this.containerMenu.broadcastChanges();
	}

	@Override
	public Either<Player.BedSleepingProblem, Unit> startSleepInBed(BlockPos blockPos) {
		Direction direction = this.level().getBlockState(blockPos).getValue(HorizontalDirectionalBlock.FACING);
		if (this.isSleeping() || !this.isAlive()) {
			return Either.left(Player.BedSleepingProblem.OTHER_PROBLEM);
		} else if (!this.level().dimensionType().natural()) {
			return Either.left(Player.BedSleepingProblem.NOT_POSSIBLE_HERE);
		} else if (!this.bedInRange(blockPos, direction)) {
			return Either.left(Player.BedSleepingProblem.TOO_FAR_AWAY);
		} else if (this.bedBlocked(blockPos, direction)) {
			return Either.left(Player.BedSleepingProblem.OBSTRUCTED);
		} else {
			this.setRespawnPosition(this.level().dimension(), blockPos, this.getYRot(), false, true);
			if (this.level().isDay()) {
				return Either.left(Player.BedSleepingProblem.NOT_POSSIBLE_NOW);
			} else {
				if (!this.isCreative()) {
					double d = 8.0;
					double e = 5.0;
					Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);
					List<Monster> list = this.level()
						.getEntitiesOfClass(
							Monster.class,
							new AABB(vec3.x() - 8.0, vec3.y() - 5.0, vec3.z() - 8.0, vec3.x() + 8.0, vec3.y() + 5.0, vec3.z() + 8.0),
							monster -> monster.isPreventingPlayerRest(this.serverLevel(), this)
						);
					if (!list.isEmpty()) {
						return Either.left(Player.BedSleepingProblem.NOT_SAFE);
					}
				}

				Either<Player.BedSleepingProblem, Unit> either = super.startSleepInBed(blockPos).ifRight(unit -> {
					this.awardStat(Stats.SLEEP_IN_BED);
					CriteriaTriggers.SLEPT_IN_BED.trigger(this);
				});
				if (!this.serverLevel().canSleepThroughNights()) {
					this.displayClientMessage(Component.translatable("sleep.not_possible"), true);
				}

				((ServerLevel)this.level()).updateSleepingPlayerList();
				return either;
			}
		}
	}

	@Override
	public void startSleeping(BlockPos blockPos) {
		this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
		super.startSleeping(blockPos);
	}

	private boolean bedInRange(BlockPos blockPos, Direction direction) {
		return this.isReachableBedBlock(blockPos) || this.isReachableBedBlock(blockPos.relative(direction.getOpposite()));
	}

	private boolean isReachableBedBlock(BlockPos blockPos) {
		Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);
		return Math.abs(this.getX() - vec3.x()) <= 3.0 && Math.abs(this.getY() - vec3.y()) <= 2.0 && Math.abs(this.getZ() - vec3.z()) <= 3.0;
	}

	private boolean bedBlocked(BlockPos blockPos, Direction direction) {
		BlockPos blockPos2 = blockPos.above();
		return !this.freeAt(blockPos2) || !this.freeAt(blockPos2.relative(direction.getOpposite()));
	}

	@Override
	public void stopSleepInBed(boolean bl, boolean bl2) {
		if (this.isSleeping()) {
			this.serverLevel().getChunkSource().broadcastAndSend(this, new ClientboundAnimatePacket(this, 2));
		}

		super.stopSleepInBed(bl, bl2);
		if (this.connection != null) {
			this.connection.teleport(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
		}
	}

	@Override
	public void dismountTo(double d, double e, double f) {
		this.removeVehicle();
		this.setPos(d, e, f);
	}

	@Override
	public boolean isInvulnerableTo(ServerLevel serverLevel, DamageSource damageSource) {
		return super.isInvulnerableTo(serverLevel, damageSource) || this.isChangingDimension() && !damageSource.is(DamageTypes.ENDER_PEARL);
	}

	@Override
	protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
	}

	@Override
	protected void onChangedBlock(ServerLevel serverLevel, BlockPos blockPos) {
		if (!this.isSpectator()) {
			super.onChangedBlock(serverLevel, blockPos);
		}
	}

	public void doCheckFallDamage(double d, double e, double f, boolean bl) {
		if (!this.touchingUnloadedChunk()) {
			this.checkSupportingBlock(bl, new Vec3(d, e, f));
			BlockPos blockPos = this.getOnPosLegacy();
			BlockState blockState = this.level().getBlockState(blockPos);
			if (this.spawnExtraParticlesOnFall && bl && this.fallDistance > 0.0F) {
				Vec3 vec3 = blockPos.getCenter().add(0.0, 0.5, 0.0);
				int i = (int)Mth.clamp(50.0F * this.fallDistance, 0.0F, 200.0F);
				this.serverLevel().sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockState), vec3.x, vec3.y, vec3.z, i, 0.3F, 0.3F, 0.3F, 0.15F);
				this.spawnExtraParticlesOnFall = false;
			}

			super.checkFallDamage(e, bl, blockState, blockPos);
		}
	}

	@Override
	public void onExplosionHit(@Nullable Entity entity) {
		super.onExplosionHit(entity);
		this.currentImpulseImpactPos = this.position();
		this.currentExplosionCause = entity;
		this.setIgnoreFallDamageFromCurrentImpulse(entity != null && entity.getType() == EntityType.WIND_CHARGE);
	}

	@Override
	protected void pushEntities() {
		if (this.level().tickRateManager().runsNormally()) {
			super.pushEntities();
		}
	}

	@Override
	public void openTextEdit(SignBlockEntity signBlockEntity, boolean bl) {
		this.connection.send(new ClientboundBlockUpdatePacket(this.level(), signBlockEntity.getBlockPos()));
		this.connection.send(new ClientboundOpenSignEditorPacket(signBlockEntity.getBlockPos(), bl));
	}

	private void nextContainerCounter() {
		this.containerCounter = this.containerCounter % 100 + 1;
	}

	@Override
	public OptionalInt openMenu(@Nullable MenuProvider menuProvider) {
		if (menuProvider == null) {
			return OptionalInt.empty();
		} else {
			if (this.containerMenu != this.inventoryMenu) {
				this.closeContainer();
			}

			this.nextContainerCounter();
			AbstractContainerMenu abstractContainerMenu = menuProvider.createMenu(this.containerCounter, this.getInventory(), this);
			if (abstractContainerMenu == null) {
				if (this.isSpectator()) {
					this.displayClientMessage(Component.translatable("container.spectatorCantOpen").withStyle(ChatFormatting.RED), true);
				}

				return OptionalInt.empty();
			} else {
				this.connection.send(new ClientboundOpenScreenPacket(abstractContainerMenu.containerId, abstractContainerMenu.getType(), menuProvider.getDisplayName()));
				this.initMenu(abstractContainerMenu);
				this.containerMenu = abstractContainerMenu;
				return OptionalInt.of(this.containerCounter);
			}
		}
	}

	@Override
	public void sendMerchantOffers(int i, MerchantOffers merchantOffers, int j, int k, boolean bl, boolean bl2) {
		this.connection.send(new ClientboundMerchantOffersPacket(i, merchantOffers, j, k, bl, bl2));
	}

	@Override
	public void openHorseInventory(AbstractHorse abstractHorse, Container container) {
		if (this.containerMenu != this.inventoryMenu) {
			this.closeContainer();
		}

		this.nextContainerCounter();
		int i = abstractHorse.getInventoryColumns();
		this.connection.send(new ClientboundHorseScreenOpenPacket(this.containerCounter, i, abstractHorse.getId()));
		this.containerMenu = new HorseInventoryMenu(this.containerCounter, this.getInventory(), container, abstractHorse, i);
		this.initMenu(this.containerMenu);
	}

	@Override
	public void openItemGui(ItemStack itemStack, InteractionHand interactionHand) {
		if (itemStack.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
			if (WrittenBookItem.resolveBookComponents(itemStack, this.createCommandSourceStack(), this)) {
				this.containerMenu.broadcastChanges();
			}

			this.connection.send(new ClientboundOpenBookPacket(interactionHand));
		}
	}

	@Override
	public void openCommandBlock(CommandBlockEntity commandBlockEntity) {
		this.connection.send(ClientboundBlockEntityDataPacket.create(commandBlockEntity, BlockEntity::saveCustomOnly));
	}

	@Override
	public void closeContainer() {
		this.connection.send(new ClientboundContainerClosePacket(this.containerMenu.containerId));
		this.doCloseContainer();
	}

	@Override
	public void doCloseContainer() {
		this.containerMenu.removed(this);
		this.inventoryMenu.transferState(this.containerMenu);
		this.containerMenu = this.inventoryMenu;
	}

	@Override
	public void rideTick() {
		double d = this.getX();
		double e = this.getY();
		double f = this.getZ();
		super.rideTick();
		this.checkRidingStatistics(this.getX() - d, this.getY() - e, this.getZ() - f);
	}

	public void checkMovementStatistics(double d, double e, double f) {
		if (!this.isPassenger() && !didNotMove(d, e, f)) {
			if (this.isSwimming()) {
				int i = Math.round((float)Math.sqrt(d * d + e * e + f * f) * 100.0F);
				if (i > 0) {
					this.awardStat(Stats.SWIM_ONE_CM, i);
					this.causeFoodExhaustion(0.01F * (float)i * 0.01F);
				}
			} else if (this.isEyeInFluid(FluidTags.WATER)) {
				int i = Math.round((float)Math.sqrt(d * d + e * e + f * f) * 100.0F);
				if (i > 0) {
					this.awardStat(Stats.WALK_UNDER_WATER_ONE_CM, i);
					this.causeFoodExhaustion(0.01F * (float)i * 0.01F);
				}
			} else if (this.isInWater()) {
				int i = Math.round((float)Math.sqrt(d * d + f * f) * 100.0F);
				if (i > 0) {
					this.awardStat(Stats.WALK_ON_WATER_ONE_CM, i);
					this.causeFoodExhaustion(0.01F * (float)i * 0.01F);
				}
			} else if (this.onClimbable()) {
				if (e > 0.0) {
					this.awardStat(Stats.CLIMB_ONE_CM, (int)Math.round(e * 100.0));
				}
			} else if (this.onGround()) {
				int i = Math.round((float)Math.sqrt(d * d + f * f) * 100.0F);
				if (i > 0) {
					if (this.isSprinting()) {
						this.awardStat(Stats.SPRINT_ONE_CM, i);
						this.causeFoodExhaustion(0.1F * (float)i * 0.01F);
					} else if (this.isCrouching()) {
						this.awardStat(Stats.CROUCH_ONE_CM, i);
						this.causeFoodExhaustion(0.0F * (float)i * 0.01F);
					} else {
						this.awardStat(Stats.WALK_ONE_CM, i);
						this.causeFoodExhaustion(0.0F * (float)i * 0.01F);
					}
				}
			} else if (this.isFallFlying()) {
				int i = Math.round((float)Math.sqrt(d * d + e * e + f * f) * 100.0F);
				this.awardStat(Stats.AVIATE_ONE_CM, i);
			} else {
				int i = Math.round((float)Math.sqrt(d * d + f * f) * 100.0F);
				if (i > 25) {
					this.awardStat(Stats.FLY_ONE_CM, i);
				}
			}
		}
	}

	private void checkRidingStatistics(double d, double e, double f) {
		if (this.isPassenger() && !didNotMove(d, e, f)) {
			int i = Math.round((float)Math.sqrt(d * d + e * e + f * f) * 100.0F);
			Entity entity = this.getVehicle();
			if (entity instanceof AbstractMinecart) {
				this.awardStat(Stats.MINECART_ONE_CM, i);
			} else if (entity instanceof AbstractBoat) {
				this.awardStat(Stats.BOAT_ONE_CM, i);
			} else if (entity instanceof Pig) {
				this.awardStat(Stats.PIG_ONE_CM, i);
			} else if (entity instanceof AbstractHorse) {
				this.awardStat(Stats.HORSE_ONE_CM, i);
			} else if (entity instanceof Strider) {
				this.awardStat(Stats.STRIDER_ONE_CM, i);
			}
		}
	}

	private static boolean didNotMove(double d, double e, double f) {
		return d == 0.0 && e == 0.0 && f == 0.0;
	}

	@Override
	public void awardStat(Stat<?> stat, int i) {
		this.stats.increment(this, stat, i);
		this.getScoreboard().forAllObjectives(stat, this, scoreAccess -> scoreAccess.add(i));
	}

	@Override
	public void resetStat(Stat<?> stat) {
		this.stats.setValue(this, stat, 0);
		this.getScoreboard().forAllObjectives(stat, this, ScoreAccess::reset);
	}

	@Override
	public int awardRecipes(Collection<RecipeHolder<?>> collection) {
		return this.recipeBook.addRecipes(collection, this);
	}

	@Override
	public void triggerRecipeCrafted(RecipeHolder<?> recipeHolder, List<ItemStack> list) {
		CriteriaTriggers.RECIPE_CRAFTED.trigger(this, recipeHolder.id(), list);
	}

	@Override
	public void awardRecipesByKey(List<ResourceKey<Recipe<?>>> list) {
		List<RecipeHolder<?>> list2 = (List<RecipeHolder<?>>)list.stream()
			.flatMap(resourceKey -> this.server.getRecipeManager().byKey(resourceKey).stream())
			.collect(Collectors.toList());
		this.awardRecipes(list2);
	}

	@Override
	public int resetRecipes(Collection<RecipeHolder<?>> collection) {
		return this.recipeBook.removeRecipes(collection, this);
	}

	@Override
	public void jumpFromGround() {
		super.jumpFromGround();
		this.awardStat(Stats.JUMP);
		if (this.isSprinting()) {
			this.causeFoodExhaustion(0.2F);
		} else {
			this.causeFoodExhaustion(0.05F);
		}
	}

	@Override
	public void giveExperiencePoints(int i) {
		super.giveExperiencePoints(i);
		this.lastSentExp = -1;
	}

	public void disconnect() {
		this.disconnected = true;
		this.ejectPassengers();
		if (this.isSleeping()) {
			this.stopSleepInBed(true, false);
		}
	}

	public boolean hasDisconnected() {
		return this.disconnected;
	}

	public void resetSentInfo() {
		this.lastSentHealth = -1.0E8F;
	}

	@Override
	public void displayClientMessage(Component component, boolean bl) {
		this.sendSystemMessage(component, bl);
	}

	@Override
	protected void completeUsingItem() {
		if (!this.useItem.isEmpty() && this.isUsingItem()) {
			this.connection.send(new ClientboundEntityEventPacket(this, (byte)9));
			super.completeUsingItem();
		}
	}

	@Override
	public void lookAt(EntityAnchorArgument.Anchor anchor, Vec3 vec3) {
		super.lookAt(anchor, vec3);
		this.connection.send(new ClientboundPlayerLookAtPacket(anchor, vec3.x, vec3.y, vec3.z));
	}

	public void lookAt(EntityAnchorArgument.Anchor anchor, Entity entity, EntityAnchorArgument.Anchor anchor2) {
		Vec3 vec3 = anchor2.apply(entity);
		super.lookAt(anchor, vec3);
		this.connection.send(new ClientboundPlayerLookAtPacket(anchor, entity, anchor2));
	}

	public void restoreFrom(ServerPlayer serverPlayer, boolean bl) {
		this.wardenSpawnTracker = serverPlayer.wardenSpawnTracker;
		this.chatSession = serverPlayer.chatSession;
		this.gameMode.setGameModeForPlayer(serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer.gameMode.getPreviousGameModeForPlayer());
		this.onUpdateAbilities();
		if (bl) {
			this.getAttributes().assignBaseValues(serverPlayer.getAttributes());
			this.getAttributes().assignPermanentModifiers(serverPlayer.getAttributes());
			this.setHealth(serverPlayer.getHealth());
			this.foodData = serverPlayer.foodData;

			for (MobEffectInstance mobEffectInstance : serverPlayer.getActiveEffects()) {
				this.addEffect(new MobEffectInstance(mobEffectInstance));
			}

			this.getInventory().replaceWith(serverPlayer.getInventory());
			this.experienceLevel = serverPlayer.experienceLevel;
			this.totalExperience = serverPlayer.totalExperience;
			this.experienceProgress = serverPlayer.experienceProgress;
			this.setScore(serverPlayer.getScore());
			this.portalProcess = serverPlayer.portalProcess;
		} else {
			this.getAttributes().assignBaseValues(serverPlayer.getAttributes());
			this.setHealth(this.getMaxHealth());
			if (this.serverLevel().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) || serverPlayer.isSpectator()) {
				this.getInventory().replaceWith(serverPlayer.getInventory());
				this.experienceLevel = serverPlayer.experienceLevel;
				this.totalExperience = serverPlayer.totalExperience;
				this.experienceProgress = serverPlayer.experienceProgress;
				this.setScore(serverPlayer.getScore());
			}
		}

		this.enchantmentSeed = serverPlayer.enchantmentSeed;
		this.enderChestInventory = serverPlayer.enderChestInventory;
		this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, serverPlayer.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION));
		this.lastSentExp = -1;
		this.lastSentHealth = -1.0F;
		this.lastSentFood = -1;
		this.recipeBook.copyOverData(serverPlayer.recipeBook);
		this.seenCredits = serverPlayer.seenCredits;
		this.enteredNetherPosition = serverPlayer.enteredNetherPosition;
		this.chunkTrackingView = serverPlayer.chunkTrackingView;
		this.setShoulderEntityLeft(serverPlayer.getShoulderEntityLeft());
		this.setShoulderEntityRight(serverPlayer.getShoulderEntityRight());
		this.setLastDeathLocation(serverPlayer.getLastDeathLocation());
	}

	@Override
	protected void onEffectAdded(MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
		super.onEffectAdded(mobEffectInstance, entity);
		this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), mobEffectInstance, true));
		if (mobEffectInstance.is(MobEffects.LEVITATION)) {
			this.levitationStartTime = this.tickCount;
			this.levitationStartPos = this.position();
		}

		CriteriaTriggers.EFFECTS_CHANGED.trigger(this, entity);
	}

	@Override
	protected void onEffectUpdated(MobEffectInstance mobEffectInstance, boolean bl, @Nullable Entity entity) {
		super.onEffectUpdated(mobEffectInstance, bl, entity);
		this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), mobEffectInstance, false));
		CriteriaTriggers.EFFECTS_CHANGED.trigger(this, entity);
	}

	@Override
	protected void onEffectsRemoved(Collection<MobEffectInstance> collection) {
		super.onEffectsRemoved(collection);

		for (MobEffectInstance mobEffectInstance : collection) {
			this.connection.send(new ClientboundRemoveMobEffectPacket(this.getId(), mobEffectInstance.getEffect()));
			if (mobEffectInstance.is(MobEffects.LEVITATION)) {
				this.levitationStartPos = null;
			}
		}

		CriteriaTriggers.EFFECTS_CHANGED.trigger(this, null);
	}

	@Override
	public void teleportTo(double d, double e, double f) {
		this.connection.teleport(new PositionMoveRotation(new Vec3(d, e, f), Vec3.ZERO, 0.0F, 0.0F), Relative.union(Relative.DELTA, Relative.ROTATION));
	}

	@Override
	public void teleportRelative(double d, double e, double f) {
		this.connection.teleport(new PositionMoveRotation(new Vec3(d, e, f), Vec3.ZERO, 0.0F, 0.0F), Relative.ALL);
	}

	@Override
	public boolean teleportTo(ServerLevel serverLevel, double d, double e, double f, Set<Relative> set, float g, float h, boolean bl) {
		ChunkPos chunkPos = new ChunkPos(BlockPos.containing(d, e, f));
		serverLevel.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkPos, 1, this.getId());
		if (this.isSleeping()) {
			this.stopSleepInBed(true, true);
		}

		if (bl) {
			this.setCamera(this);
		}

		boolean bl2 = super.teleportTo(serverLevel, d, e, f, set, g, h, bl);
		if (bl2) {
			this.setYHeadRot(set.contains(Relative.Y_ROT) ? this.getYHeadRot() + g : g);
		}

		return bl2;
	}

	@Override
	public void moveTo(double d, double e, double f) {
		super.moveTo(d, e, f);
		this.connection.resetPosition();
	}

	@Override
	public void crit(Entity entity) {
		this.serverLevel().getChunkSource().broadcastAndSend(this, new ClientboundAnimatePacket(entity, 4));
	}

	@Override
	public void magicCrit(Entity entity) {
		this.serverLevel().getChunkSource().broadcastAndSend(this, new ClientboundAnimatePacket(entity, 5));
	}

	@Override
	public void onUpdateAbilities() {
		if (this.connection != null) {
			this.connection.send(new ClientboundPlayerAbilitiesPacket(this.getAbilities()));
			this.updateInvisibilityStatus();
		}
	}

	public ServerLevel serverLevel() {
		return (ServerLevel)this.level();
	}

	public boolean setGameMode(GameType gameType) {
		boolean bl = this.isSpectator();
		if (!this.gameMode.changeGameModeForPlayer(gameType)) {
			return false;
		} else {
			this.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, (float)gameType.getId()));
			if (gameType == GameType.SPECTATOR) {
				this.removeEntitiesOnShoulder();
				this.stopRiding();
				EnchantmentHelper.stopLocationBasedEffects(this);
			} else {
				this.setCamera(this);
				if (bl) {
					EnchantmentHelper.runLocationChangedEffects(this.serverLevel(), this);
				}
			}

			this.onUpdateAbilities();
			this.updateEffectVisibility();
			return true;
		}
	}

	@Override
	public boolean isSpectator() {
		return this.gameMode.getGameModeForPlayer() == GameType.SPECTATOR;
	}

	@Override
	public boolean isCreative() {
		return this.gameMode.getGameModeForPlayer() == GameType.CREATIVE;
	}

	public CommandSource commandSource() {
		return this.commandSource;
	}

	public CommandSourceStack createCommandSourceStack() {
		return new CommandSourceStack(
			this.commandSource(),
			this.position(),
			this.getRotationVector(),
			this.serverLevel(),
			this.getPermissionLevel(),
			this.getName().getString(),
			this.getDisplayName(),
			this.server,
			this
		);
	}

	public void sendSystemMessage(Component component) {
		this.sendSystemMessage(component, false);
	}

	public void sendSystemMessage(Component component, boolean bl) {
		if (this.acceptsSystemMessages(bl)) {
			this.connection.send(new ClientboundSystemChatPacket(component, bl), PacketSendListener.exceptionallySend(() -> {
				if (this.acceptsSystemMessages(false)) {
					int i = 256;
					String string = component.getString(256);
					Component component2 = Component.literal(string).withStyle(ChatFormatting.YELLOW);
					return new ClientboundSystemChatPacket(Component.translatable("multiplayer.message_not_delivered", component2).withStyle(ChatFormatting.RED), false);
				} else {
					return null;
				}
			}));
		}
	}

	public void sendChatMessage(OutgoingChatMessage outgoingChatMessage, boolean bl, ChatType.Bound bound) {
		if (this.acceptsChatMessages()) {
			outgoingChatMessage.sendToPlayer(this, bl, bound);
		}
	}

	public String getIpAddress() {
		return this.connection.getRemoteAddress() instanceof InetSocketAddress inetSocketAddress
			? InetAddresses.toAddrString(inetSocketAddress.getAddress())
			: "<unknown>";
	}

	public void updateOptions(ClientInformation clientInformation) {
		this.language = clientInformation.language();
		this.requestedViewDistance = clientInformation.viewDistance();
		this.chatVisibility = clientInformation.chatVisibility();
		this.canChatColor = clientInformation.chatColors();
		this.textFilteringEnabled = clientInformation.textFilteringEnabled();
		this.allowsListing = clientInformation.allowsListing();
		this.particleStatus = clientInformation.particleStatus();
		this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, (byte)clientInformation.modelCustomisation());
		this.getEntityData().set(DATA_PLAYER_MAIN_HAND, (byte)clientInformation.mainHand().getId());
	}

	public ClientInformation clientInformation() {
		int i = this.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION);
		HumanoidArm humanoidArm = (HumanoidArm)HumanoidArm.BY_ID.apply(this.getEntityData().get(DATA_PLAYER_MAIN_HAND));
		return new ClientInformation(
			this.language,
			this.requestedViewDistance,
			this.chatVisibility,
			this.canChatColor,
			i,
			humanoidArm,
			this.textFilteringEnabled,
			this.allowsListing,
			this.particleStatus
		);
	}

	public boolean canChatInColor() {
		return this.canChatColor;
	}

	public ChatVisiblity getChatVisibility() {
		return this.chatVisibility;
	}

	private boolean acceptsSystemMessages(boolean bl) {
		return this.chatVisibility == ChatVisiblity.HIDDEN ? bl : true;
	}

	private boolean acceptsChatMessages() {
		return this.chatVisibility == ChatVisiblity.FULL;
	}

	public int requestedViewDistance() {
		return this.requestedViewDistance;
	}

	public void sendServerStatus(ServerStatus serverStatus) {
		this.connection.send(new ClientboundServerDataPacket(serverStatus.description(), serverStatus.favicon().map(ServerStatus.Favicon::iconBytes)));
	}

	@Override
	protected int getPermissionLevel() {
		return this.server.getProfilePermissions(this.getGameProfile());
	}

	public void resetLastActionTime() {
		this.lastActionTime = Util.getMillis();
	}

	public ServerStatsCounter getStats() {
		return this.stats;
	}

	public ServerRecipeBook getRecipeBook() {
		return this.recipeBook;
	}

	@Override
	protected void updateInvisibilityStatus() {
		if (this.isSpectator()) {
			this.removeEffectParticles();
			this.setInvisible(true);
		} else {
			super.updateInvisibilityStatus();
		}
	}

	public Entity getCamera() {
		return (Entity)(this.camera == null ? this : this.camera);
	}

	public void setCamera(@Nullable Entity entity) {
		Entity entity2 = this.getCamera();
		this.camera = (Entity)(entity == null ? this : entity);
		if (entity2 != this.camera) {
			if (this.camera.level() instanceof ServerLevel serverLevel) {
				this.teleportTo(serverLevel, this.camera.getX(), this.camera.getY(), this.camera.getZ(), Set.of(), this.getYRot(), this.getXRot(), false);
			}

			if (entity != null) {
				this.serverLevel().getChunkSource().move(this);
			}

			this.connection.send(new ClientboundSetCameraPacket(this.camera));
			this.connection.resetPosition();
		}
	}

	@Override
	protected void processPortalCooldown() {
		if (!this.isChangingDimension) {
			super.processPortalCooldown();
		}
	}

	@Override
	public void attack(Entity entity) {
		if (this.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
			this.setCamera(entity);
		} else {
			super.attack(entity);
		}
	}

	public long getLastActionTime() {
		return this.lastActionTime;
	}

	@Nullable
	public Component getTabListDisplayName() {
		return null;
	}

	public int getTabListOrder() {
		return 0;
	}

	@Override
	public void swing(InteractionHand interactionHand) {
		super.swing(interactionHand);
		this.resetAttackStrengthTicker();
	}

	public boolean isChangingDimension() {
		return this.isChangingDimension;
	}

	public void hasChangedDimension() {
		this.isChangingDimension = false;
	}

	public PlayerAdvancements getAdvancements() {
		return this.advancements;
	}

	@Nullable
	public BlockPos getRespawnPosition() {
		return this.respawnPosition;
	}

	public float getRespawnAngle() {
		return this.respawnAngle;
	}

	public ResourceKey<Level> getRespawnDimension() {
		return this.respawnDimension;
	}

	public boolean isRespawnForced() {
		return this.respawnForced;
	}

	public void copyRespawnPosition(ServerPlayer serverPlayer) {
		this.setRespawnPosition(
			serverPlayer.getRespawnDimension(), serverPlayer.getRespawnPosition(), serverPlayer.getRespawnAngle(), serverPlayer.isRespawnForced(), false
		);
	}

	public void setRespawnPosition(ResourceKey<Level> resourceKey, @Nullable BlockPos blockPos, float f, boolean bl, boolean bl2) {
		if (blockPos != null) {
			boolean bl3 = blockPos.equals(this.respawnPosition) && resourceKey.equals(this.respawnDimension);
			if (bl2 && !bl3) {
				this.sendSystemMessage(Component.translatable("block.minecraft.set_spawn"));
			}

			this.respawnPosition = blockPos;
			this.respawnDimension = resourceKey;
			this.respawnAngle = f;
			this.respawnForced = bl;
		} else {
			this.respawnPosition = null;
			this.respawnDimension = Level.OVERWORLD;
			this.respawnAngle = 0.0F;
			this.respawnForced = false;
		}
	}

	public SectionPos getLastSectionPos() {
		return this.lastSectionPos;
	}

	public void setLastSectionPos(SectionPos sectionPos) {
		this.lastSectionPos = sectionPos;
	}

	public ChunkTrackingView getChunkTrackingView() {
		return this.chunkTrackingView;
	}

	public void setChunkTrackingView(ChunkTrackingView chunkTrackingView) {
		this.chunkTrackingView = chunkTrackingView;
	}

	@Override
	public void playNotifySound(SoundEvent soundEvent, SoundSource soundSource, float f, float g) {
		this.connection
			.send(
				new ClientboundSoundPacket(
					BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent), soundSource, this.getX(), this.getY(), this.getZ(), f, g, this.random.nextLong()
				)
			);
	}

	@Override
	public ItemEntity drop(ItemStack itemStack, boolean bl, boolean bl2) {
		ItemEntity itemEntity = this.createItemStackToDrop(itemStack, bl, bl2);
		if (itemEntity == null) {
			return null;
		} else {
			this.level().addFreshEntity(itemEntity);
			ItemStack itemStack2 = itemEntity.getItem();
			if (bl2) {
				if (!itemStack2.isEmpty()) {
					this.awardStat(Stats.ITEM_DROPPED.get(itemStack2.getItem()), itemStack.getCount());
				}

				this.awardStat(Stats.DROP);
			}

			return itemEntity;
		}
	}

	@Nullable
	private ItemEntity createItemStackToDrop(ItemStack itemStack, boolean bl, boolean bl2) {
		if (itemStack.isEmpty()) {
			return null;
		} else {
			double d = this.getEyeY() - 0.3F;
			ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), d, this.getZ(), itemStack);
			itemEntity.setPickUpDelay(40);
			if (bl2) {
				itemEntity.setThrower(this);
			}

			if (bl) {
				float f = this.random.nextFloat() * 0.5F;
				float g = this.random.nextFloat() * (float) (Math.PI * 2);
				itemEntity.setDeltaMovement((double)(-Mth.sin(g) * f), 0.2F, (double)(Mth.cos(g) * f));
			} else {
				float f = 0.3F;
				float g = Mth.sin(this.getXRot() * (float) (Math.PI / 180.0));
				float h = Mth.cos(this.getXRot() * (float) (Math.PI / 180.0));
				float i = Mth.sin(this.getYRot() * (float) (Math.PI / 180.0));
				float j = Mth.cos(this.getYRot() * (float) (Math.PI / 180.0));
				float k = this.random.nextFloat() * (float) (Math.PI * 2);
				float l = 0.02F * this.random.nextFloat();
				itemEntity.setDeltaMovement(
					(double)(-i * h * 0.3F) + Math.cos((double)k) * (double)l,
					(double)(-g * 0.3F + 0.1F + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F),
					(double)(j * h * 0.3F) + Math.sin((double)k) * (double)l
				);
			}

			return itemEntity;
		}
	}

	public TextFilter getTextFilter() {
		return this.textFilter;
	}

	public void setServerLevel(ServerLevel serverLevel) {
		this.setLevel(serverLevel);
		this.gameMode.setLevel(serverLevel);
	}

	@Nullable
	private static GameType readPlayerMode(@Nullable CompoundTag compoundTag, String string) {
		return compoundTag != null && compoundTag.contains(string, 99) ? GameType.byId(compoundTag.getInt(string)) : null;
	}

	private GameType calculateGameModeForNewPlayer(@Nullable GameType gameType) {
		GameType gameType2 = this.server.getForcedGameType();
		if (gameType2 != null) {
			return gameType2;
		} else {
			return gameType != null ? gameType : this.server.getDefaultGameType();
		}
	}

	public void loadGameTypes(@Nullable CompoundTag compoundTag) {
		this.gameMode
			.setGameModeForPlayer(
				this.calculateGameModeForNewPlayer(readPlayerMode(compoundTag, "playerGameType")), readPlayerMode(compoundTag, "previousPlayerGameType")
			);
	}

	private void storeGameTypes(CompoundTag compoundTag) {
		compoundTag.putInt("playerGameType", this.gameMode.getGameModeForPlayer().getId());
		GameType gameType = this.gameMode.getPreviousGameModeForPlayer();
		if (gameType != null) {
			compoundTag.putInt("previousPlayerGameType", gameType.getId());
		}
	}

	@Override
	public boolean isTextFilteringEnabled() {
		return this.textFilteringEnabled;
	}

	public boolean shouldFilterMessageTo(ServerPlayer serverPlayer) {
		return serverPlayer == this ? false : this.textFilteringEnabled || serverPlayer.textFilteringEnabled;
	}

	@Override
	public boolean mayInteract(ServerLevel serverLevel, BlockPos blockPos) {
		return super.mayInteract(serverLevel, blockPos) && serverLevel.mayInteract(this, blockPos);
	}

	@Override
	protected void updateUsingItem(ItemStack itemStack) {
		CriteriaTriggers.USING_ITEM.trigger(this, itemStack);
		super.updateUsingItem(itemStack);
	}

	public boolean drop(boolean bl) {
		Inventory inventory = this.getInventory();
		ItemStack itemStack = inventory.removeFromSelected(bl);
		this.containerMenu.findSlot(inventory, inventory.selected).ifPresent(i -> this.containerMenu.setRemoteSlot(i, inventory.getSelected()));
		return this.drop(itemStack, false, true) != null;
	}

	@Override
	public void handleExtraItemsCreatedOnUse(ItemStack itemStack) {
		if (!this.getInventory().add(itemStack)) {
			this.drop(itemStack, false);
		}
	}

	public boolean allowsListing() {
		return this.allowsListing;
	}

	@Override
	public Optional<WardenSpawnTracker> getWardenSpawnTracker() {
		return Optional.of(this.wardenSpawnTracker);
	}

	public void setSpawnExtraParticlesOnFall(boolean bl) {
		this.spawnExtraParticlesOnFall = bl;
	}

	@Override
	public void onItemPickup(ItemEntity itemEntity) {
		super.onItemPickup(itemEntity);
		Entity entity = itemEntity.getOwner();
		if (entity != null) {
			CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_PLAYER.trigger(this, itemEntity.getItem(), entity);
		}
	}

	public void setChatSession(RemoteChatSession remoteChatSession) {
		this.chatSession = remoteChatSession;
	}

	@Nullable
	public RemoteChatSession getChatSession() {
		return this.chatSession != null && this.chatSession.hasExpired() ? null : this.chatSession;
	}

	@Override
	public void indicateDamage(double d, double e) {
		this.hurtDir = (float)(Mth.atan2(e, d) * 180.0F / (float)Math.PI - (double)this.getYRot());
		this.connection.send(new ClientboundHurtAnimationPacket(this));
	}

	@Override
	public boolean startRiding(Entity entity, boolean bl) {
		if (super.startRiding(entity, bl)) {
			entity.positionRider(this);
			this.connection.teleport(new PositionMoveRotation(this.position(), Vec3.ZERO, 0.0F, 0.0F), Relative.ROTATION);
			if (entity instanceof LivingEntity livingEntity) {
				this.server.getPlayerList().sendActiveEffects(livingEntity, this.connection);
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public void stopRiding() {
		Entity entity = this.getVehicle();
		super.stopRiding();
		if (entity instanceof LivingEntity livingEntity) {
			for (MobEffectInstance mobEffectInstance : livingEntity.getActiveEffects()) {
				this.connection.send(new ClientboundRemoveMobEffectPacket(entity.getId(), mobEffectInstance.getEffect()));
			}
		}
	}

	public CommonPlayerSpawnInfo createCommonSpawnInfo(ServerLevel serverLevel) {
		return new CommonPlayerSpawnInfo(
			serverLevel.dimensionTypeRegistration(),
			serverLevel.dimension(),
			BiomeManager.obfuscateSeed(serverLevel.getSeed()),
			this.gameMode.getGameModeForPlayer(),
			this.gameMode.getPreviousGameModeForPlayer(),
			serverLevel.isDebug(),
			serverLevel.isFlat(),
			this.getLastDeathLocation(),
			this.getPortalCooldown(),
			serverLevel.getSeaLevel()
		);
	}

	public void setRaidOmenPosition(BlockPos blockPos) {
		this.raidOmenPosition = blockPos;
	}

	public void clearRaidOmenPosition() {
		this.raidOmenPosition = null;
	}

	@Nullable
	public BlockPos getRaidOmenPosition() {
		return this.raidOmenPosition;
	}

	@Override
	public Vec3 getKnownMovement() {
		Entity entity = this.getVehicle();
		return entity != null && entity.getControllingPassenger() != this ? entity.getKnownMovement() : this.lastKnownClientMovement;
	}

	public void setKnownMovement(Vec3 vec3) {
		this.lastKnownClientMovement = vec3;
	}

	@Override
	protected float getEnchantedDamage(Entity entity, float f, DamageSource damageSource) {
		return EnchantmentHelper.modifyDamage(this.serverLevel(), this.getWeaponItem(), entity, damageSource, f);
	}

	@Override
	public void onEquippedItemBroken(Item item, EquipmentSlot equipmentSlot) {
		super.onEquippedItemBroken(item, equipmentSlot);
		this.awardStat(Stats.ITEM_BROKEN.get(item));
	}

	public Input getLastClientInput() {
		return this.lastClientInput;
	}

	public void setLastClientInput(Input input) {
		this.lastClientInput = input;
	}

	public Vec3 getLastClientMoveIntent() {
		float f = this.lastClientInput.left() == this.lastClientInput.right() ? 0.0F : (this.lastClientInput.left() ? 1.0F : -1.0F);
		float g = this.lastClientInput.forward() == this.lastClientInput.backward() ? 0.0F : (this.lastClientInput.forward() ? 1.0F : -1.0F);
		return getInputVector(new Vec3((double)f, 0.0, (double)g), 1.0F, this.getYRot());
	}

	public void registerEnderPearl(ThrownEnderpearl thrownEnderpearl) {
		this.enderPearls.add(thrownEnderpearl);
	}

	public void deregisterEnderPearl(ThrownEnderpearl thrownEnderpearl) {
		this.enderPearls.remove(thrownEnderpearl);
	}

	public Set<ThrownEnderpearl> getEnderPearls() {
		return this.enderPearls;
	}

	public long registerAndUpdateEnderPearlTicket(ThrownEnderpearl thrownEnderpearl) {
		if (thrownEnderpearl.level() instanceof ServerLevel serverLevel) {
			ChunkPos chunkPos = thrownEnderpearl.chunkPosition();
			this.registerEnderPearl(thrownEnderpearl);
			serverLevel.resetEmptyTime();
			return placeEnderPearlTicket(serverLevel, chunkPos) - 1L;
		} else {
			return 0L;
		}
	}

	public static long placeEnderPearlTicket(ServerLevel serverLevel, ChunkPos chunkPos) {
		serverLevel.getChunkSource().addRegionTicket(TicketType.ENDER_PEARL, chunkPos, 2, chunkPos);
		return TicketType.ENDER_PEARL.timeout();
	}

	static record RespawnPosAngle(Vec3 position, float yaw) {
		public static ServerPlayer.RespawnPosAngle of(Vec3 vec3, BlockPos blockPos) {
			return new ServerPlayer.RespawnPosAngle(vec3, calculateLookAtYaw(vec3, blockPos));
		}

		private static float calculateLookAtYaw(Vec3 vec3, BlockPos blockPos) {
			Vec3 vec32 = Vec3.atBottomCenterOf(blockPos).subtract(vec3).normalize();
			return (float)Mth.wrapDegrees(Mth.atan2(vec32.z, vec32.x) * 180.0F / (float)Math.PI - 90.0);
		}
	}
}

package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.logging.LogUtils;
import io.netty.buffer.Unpooled;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.time.Instant;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.ClientTelemetryManager;
import net.minecraft.client.DebugQueryHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.DemoIntroScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.achievement.StatsUpdateListener;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.debug.BeeDebugRenderer;
import net.minecraft.client.renderer.debug.BrainDebugRenderer;
import net.minecraft.client.renderer.debug.GoalSelectorDebugRenderer;
import net.minecraft.client.renderer.debug.NeighborsUpdateRenderer;
import net.minecraft.client.renderer.debug.WorldGenAttemptRenderer;
import net.minecraft.client.resources.sounds.BeeAggressiveSoundInstance;
import net.minecraft.client.resources.sounds.BeeFlyingSoundInstance;
import net.minecraft.client.resources.sounds.BeeSoundInstance;
import net.minecraft.client.resources.sounds.GuardianAttackSoundInstance;
import net.minecraft.client.resources.sounds.MinecartSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Position;
import net.minecraft.core.PositionImpl;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.chat.LocalChatSession;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSignatureCache;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.chat.SignedMessageLink;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.network.protocol.game.ClientboundDeleteChatPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPingPacket;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEndPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEnterPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundServerDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateEnabledFeaturesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.util.Crypt;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientPacketListener implements ClientGamePacketListener {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Component GENERIC_DISCONNECT_MESSAGE = Component.translatable("disconnect.lost");
	private static final Component UNSECURE_SERVER_TOAST_TITLE = Component.translatable("multiplayer.unsecureserver.toast.title");
	private static final Component UNSERURE_SERVER_TOAST = Component.translatable("multiplayer.unsecureserver.toast");
	private static final Component INVALID_PACKET = Component.translatable("multiplayer.disconnect.invalid_packet");
	private static final Component CHAT_VALIDATION_FAILED_ERROR = Component.translatable("multiplayer.disconnect.chat_validation_failed");
	private static final int PENDING_OFFSET_THRESHOLD = 64;
	private final Connection connection;
	@Nullable
	private final ServerData serverData;
	private final GameProfile localGameProfile;
	private final Screen callbackScreen;
	private final Minecraft minecraft;
	private ClientLevel level;
	private ClientLevel.ClientLevelData levelData;
	private final Map<UUID, PlayerInfo> playerInfoMap = Maps.<UUID, PlayerInfo>newHashMap();
	private final SortedArraySet<PlayerInfo> listedPlayers = SortedArraySet.create(
		Comparator.comparing(playerInfo -> playerInfo.getProfile().getName(), String::compareToIgnoreCase)
	);
	private final ClientAdvancements advancements;
	private final ClientSuggestionProvider suggestionsProvider;
	private final DebugQueryHandler debugQueryHandler = new DebugQueryHandler(this);
	private int serverChunkRadius = 3;
	private int serverSimulationDistance = 3;
	private final RandomSource random = RandomSource.createThreadSafe();
	private CommandDispatcher<SharedSuggestionProvider> commands = new CommandDispatcher<>();
	private final RecipeManager recipeManager = new RecipeManager();
	private final UUID id = UUID.randomUUID();
	private Set<ResourceKey<Level>> levels;
	private LayeredRegistryAccess<ClientRegistryLayer> registryAccess = ClientRegistryLayer.createRegistryAccess();
	private FeatureFlagSet enabledFeatures = FeatureFlags.DEFAULT_FLAGS;
	private final ClientTelemetryManager telemetryManager;
	private final SignedMessageChain.Encoder signedMessageEncoder;
	private final LastSeenMessagesTracker lastSeenMessages = new LastSeenMessagesTracker(20);
	private final MessageSignatureCache messageSignatureCache = MessageSignatureCache.createDefault();
	private final MessageSignature.Unpacker messageSignatureUnpacker = this.messageSignatureCache.unpacker();

	public ClientPacketListener(
		Minecraft minecraft,
		Screen screen,
		Connection connection,
		LocalChatSession localChatSession,
		@Nullable ServerData serverData,
		GameProfile gameProfile,
		ClientTelemetryManager clientTelemetryManager
	) {
		this.minecraft = minecraft;
		this.callbackScreen = screen;
		this.connection = connection;
		this.serverData = serverData;
		this.localGameProfile = gameProfile;
		this.advancements = new ClientAdvancements(minecraft);
		this.suggestionsProvider = new ClientSuggestionProvider(this, minecraft);
		this.telemetryManager = clientTelemetryManager;
		this.signedMessageEncoder = localChatSession.createMessageEncoder(gameProfile.getId());
	}

	public ClientSuggestionProvider getSuggestionsProvider() {
		return this.suggestionsProvider;
	}

	public void cleanup() {
		this.level = null;
	}

	public RecipeManager getRecipeManager() {
		return this.recipeManager;
	}

	@Override
	public void handleLogin(ClientboundLoginPacket clientboundLoginPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundLoginPacket, this, this.minecraft);
		this.minecraft.gameMode = new MultiPlayerGameMode(this.minecraft, this);
		this.registryAccess = this.registryAccess.replaceFrom(ClientRegistryLayer.REMOTE, clientboundLoginPacket.registryHolder());
		if (!this.connection.isMemoryConnection()) {
			this.registryAccess.compositeAccess().registries().forEach(registryEntry -> registryEntry.value().resetTags());
		}

		List<ResourceKey<Level>> list = Lists.<ResourceKey<Level>>newArrayList(clientboundLoginPacket.levels());
		Collections.shuffle(list);
		this.levels = Sets.<ResourceKey<Level>>newLinkedHashSet(list);
		ResourceKey<Level> resourceKey = clientboundLoginPacket.dimension();
		Holder<DimensionType> holder = this.registryAccess
			.compositeAccess()
			.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY)
			.getHolderOrThrow(clientboundLoginPacket.dimensionType());
		this.serverChunkRadius = clientboundLoginPacket.chunkRadius();
		this.serverSimulationDistance = clientboundLoginPacket.simulationDistance();
		boolean bl = clientboundLoginPacket.isDebug();
		boolean bl2 = clientboundLoginPacket.isFlat();
		ClientLevel.ClientLevelData clientLevelData = new ClientLevel.ClientLevelData(Difficulty.NORMAL, clientboundLoginPacket.hardcore(), bl2);
		this.levelData = clientLevelData;
		this.level = new ClientLevel(
			this,
			clientLevelData,
			resourceKey,
			holder,
			this.serverChunkRadius,
			this.serverSimulationDistance,
			this.minecraft::getProfiler,
			this.minecraft.levelRenderer,
			bl,
			clientboundLoginPacket.seed()
		);
		this.minecraft.setLevel(this.level);
		if (this.minecraft.player == null) {
			this.minecraft.player = this.minecraft.gameMode.createPlayer(this.level, new StatsCounter(), new ClientRecipeBook());
			this.minecraft.player.setYRot(-180.0F);
			if (this.minecraft.getSingleplayerServer() != null) {
				this.minecraft.getSingleplayerServer().setUUID(this.minecraft.player.getUUID());
			}
		}

		this.minecraft.debugRenderer.clear();
		this.minecraft.player.resetPos();
		int i = clientboundLoginPacket.playerId();
		this.minecraft.player.setId(i);
		this.level.addPlayer(i, this.minecraft.player);
		this.minecraft.player.input = new KeyboardInput(this.minecraft.options);
		this.minecraft.gameMode.adjustPlayer(this.minecraft.player);
		this.minecraft.cameraEntity = this.minecraft.player;
		this.minecraft.setScreen(new ReceivingLevelScreen());
		this.minecraft.player.setReducedDebugInfo(clientboundLoginPacket.reducedDebugInfo());
		this.minecraft.player.setShowDeathScreen(clientboundLoginPacket.showDeathScreen());
		this.minecraft.player.setLastDeathLocation(clientboundLoginPacket.lastDeathLocation());
		this.minecraft.gameMode.setLocalMode(clientboundLoginPacket.gameType(), clientboundLoginPacket.previousGameType());
		this.minecraft.options.setServerRenderDistance(clientboundLoginPacket.chunkRadius());
		this.minecraft.options.broadcastOptions();
		this.connection
			.send(
				new ServerboundCustomPayloadPacket(
					ServerboundCustomPayloadPacket.BRAND, new FriendlyByteBuf(Unpooled.buffer()).writeUtf(ClientBrandRetriever.getClientModName())
				)
			);
		this.minecraft.getGame().onStartGameSession();
		this.telemetryManager.onPlayerInfoReceived(clientboundLoginPacket.gameType(), clientboundLoginPacket.hardcore());
	}

	@Override
	public void handleAddEntity(ClientboundAddEntityPacket clientboundAddEntityPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundAddEntityPacket, this, this.minecraft);
		EntityType<?> entityType = clientboundAddEntityPacket.getType();
		Entity entity = entityType.create(this.level);
		if (entity != null) {
			entity.recreateFromPacket(clientboundAddEntityPacket);
			int i = clientboundAddEntityPacket.getId();
			this.level.putNonPlayerEntity(i, entity);
			this.postAddEntitySoundInstance(entity);
		} else {
			LOGGER.warn("Skipping Entity with id {}", entityType);
		}
	}

	private void postAddEntitySoundInstance(Entity entity) {
		if (entity instanceof AbstractMinecart) {
			this.minecraft.getSoundManager().play(new MinecartSoundInstance((AbstractMinecart)entity));
		} else if (entity instanceof Bee) {
			boolean bl = ((Bee)entity).isAngry();
			BeeSoundInstance beeSoundInstance;
			if (bl) {
				beeSoundInstance = new BeeAggressiveSoundInstance((Bee)entity);
			} else {
				beeSoundInstance = new BeeFlyingSoundInstance((Bee)entity);
			}

			this.minecraft.getSoundManager().queueTickingSound(beeSoundInstance);
		}
	}

	@Override
	public void handleAddExperienceOrb(ClientboundAddExperienceOrbPacket clientboundAddExperienceOrbPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundAddExperienceOrbPacket, this, this.minecraft);
		double d = clientboundAddExperienceOrbPacket.getX();
		double e = clientboundAddExperienceOrbPacket.getY();
		double f = clientboundAddExperienceOrbPacket.getZ();
		Entity entity = new ExperienceOrb(this.level, d, e, f, clientboundAddExperienceOrbPacket.getValue());
		entity.syncPacketPositionCodec(d, e, f);
		entity.setYRot(0.0F);
		entity.setXRot(0.0F);
		entity.setId(clientboundAddExperienceOrbPacket.getId());
		this.level.putNonPlayerEntity(clientboundAddExperienceOrbPacket.getId(), entity);
	}

	@Override
	public void handleSetEntityMotion(ClientboundSetEntityMotionPacket clientboundSetEntityMotionPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetEntityMotionPacket, this, this.minecraft);
		Entity entity = this.level.getEntity(clientboundSetEntityMotionPacket.getId());
		if (entity != null) {
			entity.lerpMotion(
				(double)clientboundSetEntityMotionPacket.getXa() / 8000.0,
				(double)clientboundSetEntityMotionPacket.getYa() / 8000.0,
				(double)clientboundSetEntityMotionPacket.getZa() / 8000.0
			);
		}
	}

	@Override
	public void handleSetEntityData(ClientboundSetEntityDataPacket clientboundSetEntityDataPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetEntityDataPacket, this, this.minecraft);
		Entity entity = this.level.getEntity(clientboundSetEntityDataPacket.getId());
		if (entity != null && clientboundSetEntityDataPacket.getUnpackedData() != null) {
			entity.getEntityData().assignValues(clientboundSetEntityDataPacket.getUnpackedData());
		}
	}

	@Override
	public void handleAddPlayer(ClientboundAddPlayerPacket clientboundAddPlayerPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundAddPlayerPacket, this, this.minecraft);
		PlayerInfo playerInfo = this.getPlayerInfo(clientboundAddPlayerPacket.getPlayerId());
		if (playerInfo == null) {
			LOGGER.warn("Server attempted to add player prior to sending player info (Player id {})", clientboundAddPlayerPacket.getPlayerId());
		} else {
			double d = clientboundAddPlayerPacket.getX();
			double e = clientboundAddPlayerPacket.getY();
			double f = clientboundAddPlayerPacket.getZ();
			float g = (float)(clientboundAddPlayerPacket.getyRot() * 360) / 256.0F;
			float h = (float)(clientboundAddPlayerPacket.getxRot() * 360) / 256.0F;
			int i = clientboundAddPlayerPacket.getEntityId();
			RemotePlayer remotePlayer = new RemotePlayer(this.minecraft.level, playerInfo.getProfile());
			remotePlayer.setId(i);
			remotePlayer.syncPacketPositionCodec(d, e, f);
			remotePlayer.absMoveTo(d, e, f, g, h);
			remotePlayer.setOldPosAndRot();
			this.level.addPlayer(i, remotePlayer);
		}
	}

	@Override
	public void handleTeleportEntity(ClientboundTeleportEntityPacket clientboundTeleportEntityPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundTeleportEntityPacket, this, this.minecraft);
		Entity entity = this.level.getEntity(clientboundTeleportEntityPacket.getId());
		if (entity != null) {
			double d = clientboundTeleportEntityPacket.getX();
			double e = clientboundTeleportEntityPacket.getY();
			double f = clientboundTeleportEntityPacket.getZ();
			entity.syncPacketPositionCodec(d, e, f);
			if (!entity.isControlledByLocalInstance()) {
				float g = (float)(clientboundTeleportEntityPacket.getyRot() * 360) / 256.0F;
				float h = (float)(clientboundTeleportEntityPacket.getxRot() * 360) / 256.0F;
				entity.lerpTo(d, e, f, g, h, 3, true);
				entity.setOnGround(clientboundTeleportEntityPacket.isOnGround());
			}
		}
	}

	@Override
	public void handleSetCarriedItem(ClientboundSetCarriedItemPacket clientboundSetCarriedItemPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetCarriedItemPacket, this, this.minecraft);
		if (Inventory.isHotbarSlot(clientboundSetCarriedItemPacket.getSlot())) {
			this.minecraft.player.getInventory().selected = clientboundSetCarriedItemPacket.getSlot();
		}
	}

	@Override
	public void handleMoveEntity(ClientboundMoveEntityPacket clientboundMoveEntityPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundMoveEntityPacket, this, this.minecraft);
		Entity entity = clientboundMoveEntityPacket.getEntity(this.level);
		if (entity != null) {
			if (!entity.isControlledByLocalInstance()) {
				if (clientboundMoveEntityPacket.hasPosition()) {
					VecDeltaCodec vecDeltaCodec = entity.getPositionCodec();
					Vec3 vec3 = vecDeltaCodec.decode(
						(long)clientboundMoveEntityPacket.getXa(), (long)clientboundMoveEntityPacket.getYa(), (long)clientboundMoveEntityPacket.getZa()
					);
					vecDeltaCodec.setBase(vec3);
					float f = clientboundMoveEntityPacket.hasRotation() ? (float)(clientboundMoveEntityPacket.getyRot() * 360) / 256.0F : entity.getYRot();
					float g = clientboundMoveEntityPacket.hasRotation() ? (float)(clientboundMoveEntityPacket.getxRot() * 360) / 256.0F : entity.getXRot();
					entity.lerpTo(vec3.x(), vec3.y(), vec3.z(), f, g, 3, false);
				} else if (clientboundMoveEntityPacket.hasRotation()) {
					float h = (float)(clientboundMoveEntityPacket.getyRot() * 360) / 256.0F;
					float i = (float)(clientboundMoveEntityPacket.getxRot() * 360) / 256.0F;
					entity.lerpTo(entity.getX(), entity.getY(), entity.getZ(), h, i, 3, false);
				}

				entity.setOnGround(clientboundMoveEntityPacket.isOnGround());
			}
		}
	}

	@Override
	public void handleRotateMob(ClientboundRotateHeadPacket clientboundRotateHeadPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundRotateHeadPacket, this, this.minecraft);
		Entity entity = clientboundRotateHeadPacket.getEntity(this.level);
		if (entity != null) {
			float f = (float)(clientboundRotateHeadPacket.getYHeadRot() * 360) / 256.0F;
			entity.lerpHeadTo(f, 3);
		}
	}

	@Override
	public void handleRemoveEntities(ClientboundRemoveEntitiesPacket clientboundRemoveEntitiesPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundRemoveEntitiesPacket, this, this.minecraft);
		clientboundRemoveEntitiesPacket.getEntityIds().forEach(i -> this.level.removeEntity(i, Entity.RemovalReason.DISCARDED));
	}

	@Override
	public void handleMovePlayer(ClientboundPlayerPositionPacket clientboundPlayerPositionPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundPlayerPositionPacket, this, this.minecraft);
		Player player = this.minecraft.player;
		if (clientboundPlayerPositionPacket.requestDismountVehicle()) {
			player.removeVehicle();
		}

		Vec3 vec3 = player.getDeltaMovement();
		boolean bl = clientboundPlayerPositionPacket.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.X);
		boolean bl2 = clientboundPlayerPositionPacket.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.Y);
		boolean bl3 = clientboundPlayerPositionPacket.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.Z);
		double d;
		double e;
		if (bl) {
			d = vec3.x();
			e = player.getX() + clientboundPlayerPositionPacket.getX();
			player.xOld = player.xOld + clientboundPlayerPositionPacket.getX();
			player.xo = player.xo + clientboundPlayerPositionPacket.getX();
		} else {
			d = 0.0;
			e = clientboundPlayerPositionPacket.getX();
			player.xOld = e;
			player.xo = e;
		}

		double f;
		double g;
		if (bl2) {
			f = vec3.y();
			g = player.getY() + clientboundPlayerPositionPacket.getY();
			player.yOld = player.yOld + clientboundPlayerPositionPacket.getY();
			player.yo = player.yo + clientboundPlayerPositionPacket.getY();
		} else {
			f = 0.0;
			g = clientboundPlayerPositionPacket.getY();
			player.yOld = g;
			player.yo = g;
		}

		double h;
		double i;
		if (bl3) {
			h = vec3.z();
			i = player.getZ() + clientboundPlayerPositionPacket.getZ();
			player.zOld = player.zOld + clientboundPlayerPositionPacket.getZ();
			player.zo = player.zo + clientboundPlayerPositionPacket.getZ();
		} else {
			h = 0.0;
			i = clientboundPlayerPositionPacket.getZ();
			player.zOld = i;
			player.zo = i;
		}

		player.setPos(e, g, i);
		player.setDeltaMovement(d, f, h);
		float j = clientboundPlayerPositionPacket.getYRot();
		float k = clientboundPlayerPositionPacket.getXRot();
		if (clientboundPlayerPositionPacket.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.X_ROT)) {
			player.setXRot(player.getXRot() + k);
			player.xRotO += k;
		} else {
			player.setXRot(k);
			player.xRotO = k;
		}

		if (clientboundPlayerPositionPacket.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.Y_ROT)) {
			player.setYRot(player.getYRot() + j);
			player.yRotO += j;
		} else {
			player.setYRot(j);
			player.yRotO = j;
		}

		this.connection.send(new ServerboundAcceptTeleportationPacket(clientboundPlayerPositionPacket.getId()));
		this.connection.send(new ServerboundMovePlayerPacket.PosRot(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), false));
	}

	@Override
	public void handleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket clientboundSectionBlocksUpdatePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSectionBlocksUpdatePacket, this, this.minecraft);
		int i = 19 | (clientboundSectionBlocksUpdatePacket.shouldSuppressLightUpdates() ? 128 : 0);
		clientboundSectionBlocksUpdatePacket.runUpdates((blockPos, blockState) -> this.level.setServerVerifiedBlockState(blockPos, blockState, i));
	}

	@Override
	public void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket clientboundLevelChunkWithLightPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundLevelChunkWithLightPacket, this, this.minecraft);
		this.updateLevelChunk(
			clientboundLevelChunkWithLightPacket.getX(), clientboundLevelChunkWithLightPacket.getZ(), clientboundLevelChunkWithLightPacket.getChunkData()
		);
		this.queueLightUpdate(
			clientboundLevelChunkWithLightPacket.getX(), clientboundLevelChunkWithLightPacket.getZ(), clientboundLevelChunkWithLightPacket.getLightData()
		);
	}

	private void updateLevelChunk(int i, int j, ClientboundLevelChunkPacketData clientboundLevelChunkPacketData) {
		this.level
			.getChunkSource()
			.replaceWithPacketData(
				i,
				j,
				clientboundLevelChunkPacketData.getReadBuffer(),
				clientboundLevelChunkPacketData.getHeightmaps(),
				clientboundLevelChunkPacketData.getBlockEntitiesTagsConsumer(i, j)
			);
	}

	private void queueLightUpdate(int i, int j, ClientboundLightUpdatePacketData clientboundLightUpdatePacketData) {
		this.level.queueLightUpdate(() -> {
			this.applyLightData(i, j, clientboundLightUpdatePacketData);
			LevelChunk levelChunk = this.level.getChunkSource().getChunk(i, j, false);
			if (levelChunk != null) {
				this.enableChunkLight(levelChunk, i, j);
			}
		});
	}

	private void enableChunkLight(LevelChunk levelChunk, int i, int j) {
		LevelLightEngine levelLightEngine = this.level.getChunkSource().getLightEngine();
		LevelChunkSection[] levelChunkSections = levelChunk.getSections();
		ChunkPos chunkPos = levelChunk.getPos();
		levelLightEngine.enableLightSources(chunkPos, true);

		for (int k = 0; k < levelChunkSections.length; k++) {
			LevelChunkSection levelChunkSection = levelChunkSections[k];
			int l = this.level.getSectionYFromSectionIndex(k);
			levelLightEngine.updateSectionStatus(SectionPos.of(chunkPos, l), levelChunkSection.hasOnlyAir());
			this.level.setSectionDirtyWithNeighbors(i, l, j);
		}

		this.level.setLightReady(i, j);
	}

	@Override
	public void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket clientboundForgetLevelChunkPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundForgetLevelChunkPacket, this, this.minecraft);
		int i = clientboundForgetLevelChunkPacket.getX();
		int j = clientboundForgetLevelChunkPacket.getZ();
		ClientChunkCache clientChunkCache = this.level.getChunkSource();
		clientChunkCache.drop(i, j);
		this.queueLightUpdate(clientboundForgetLevelChunkPacket);
	}

	private void queueLightUpdate(ClientboundForgetLevelChunkPacket clientboundForgetLevelChunkPacket) {
		this.level.queueLightUpdate(() -> {
			LevelLightEngine levelLightEngine = this.level.getLightEngine();

			for (int i = this.level.getMinSection(); i < this.level.getMaxSection(); i++) {
				levelLightEngine.updateSectionStatus(SectionPos.of(clientboundForgetLevelChunkPacket.getX(), i, clientboundForgetLevelChunkPacket.getZ()), true);
			}

			levelLightEngine.enableLightSources(new ChunkPos(clientboundForgetLevelChunkPacket.getX(), clientboundForgetLevelChunkPacket.getZ()), false);
			this.level.setLightReady(clientboundForgetLevelChunkPacket.getX(), clientboundForgetLevelChunkPacket.getZ());
		});
	}

	@Override
	public void handleBlockUpdate(ClientboundBlockUpdatePacket clientboundBlockUpdatePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundBlockUpdatePacket, this, this.minecraft);
		this.level.setServerVerifiedBlockState(clientboundBlockUpdatePacket.getPos(), clientboundBlockUpdatePacket.getBlockState(), 19);
	}

	@Override
	public void handleDisconnect(ClientboundDisconnectPacket clientboundDisconnectPacket) {
		this.connection.disconnect(clientboundDisconnectPacket.getReason());
	}

	@Override
	public void onDisconnect(Component component) {
		this.minecraft.clearLevel();
		this.telemetryManager.onDisconnect();
		if (this.callbackScreen != null) {
			if (this.callbackScreen instanceof RealmsScreen) {
				this.minecraft.setScreen(new DisconnectedRealmsScreen(this.callbackScreen, GENERIC_DISCONNECT_MESSAGE, component));
			} else {
				this.minecraft.setScreen(new DisconnectedScreen(this.callbackScreen, GENERIC_DISCONNECT_MESSAGE, component));
			}
		} else {
			this.minecraft.setScreen(new DisconnectedScreen(new JoinMultiplayerScreen(new TitleScreen()), GENERIC_DISCONNECT_MESSAGE, component));
		}
	}

	public void send(Packet<?> packet) {
		this.connection.send(packet);
	}

	@Override
	public void handleTakeItemEntity(ClientboundTakeItemEntityPacket clientboundTakeItemEntityPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundTakeItemEntityPacket, this, this.minecraft);
		Entity entity = this.level.getEntity(clientboundTakeItemEntityPacket.getItemId());
		LivingEntity livingEntity = (LivingEntity)this.level.getEntity(clientboundTakeItemEntityPacket.getPlayerId());
		if (livingEntity == null) {
			livingEntity = this.minecraft.player;
		}

		if (entity != null) {
			if (entity instanceof ExperienceOrb) {
				this.level
					.playLocalSound(
						entity.getX(),
						entity.getY(),
						entity.getZ(),
						SoundEvents.EXPERIENCE_ORB_PICKUP,
						SoundSource.PLAYERS,
						0.1F,
						(this.random.nextFloat() - this.random.nextFloat()) * 0.35F + 0.9F,
						false
					);
			} else {
				this.level
					.playLocalSound(
						entity.getX(),
						entity.getY(),
						entity.getZ(),
						SoundEvents.ITEM_PICKUP,
						SoundSource.PLAYERS,
						0.2F,
						(this.random.nextFloat() - this.random.nextFloat()) * 1.4F + 2.0F,
						false
					);
			}

			this.minecraft
				.particleEngine
				.add(new ItemPickupParticle(this.minecraft.getEntityRenderDispatcher(), this.minecraft.renderBuffers(), this.level, entity, livingEntity));
			if (entity instanceof ItemEntity itemEntity) {
				ItemStack itemStack = itemEntity.getItem();
				itemStack.shrink(clientboundTakeItemEntityPacket.getAmount());
				if (itemStack.isEmpty()) {
					this.level.removeEntity(clientboundTakeItemEntityPacket.getItemId(), Entity.RemovalReason.DISCARDED);
				}
			} else if (!(entity instanceof ExperienceOrb)) {
				this.level.removeEntity(clientboundTakeItemEntityPacket.getItemId(), Entity.RemovalReason.DISCARDED);
			}
		}
	}

	@Override
	public void handleSystemChat(ClientboundSystemChatPacket clientboundSystemChatPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSystemChatPacket, this, this.minecraft);
		this.minecraft.getChatListener().handleSystemMessage(clientboundSystemChatPacket.content(), clientboundSystemChatPacket.overlay());
	}

	@Override
	public void handlePlayerChat(ClientboundPlayerChatPacket clientboundPlayerChatPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundPlayerChatPacket, this, this.minecraft);
		Optional<SignedMessageBody> optional = clientboundPlayerChatPacket.body().unpack(this.messageSignatureUnpacker);
		Optional<ChatType.Bound> optional2 = clientboundPlayerChatPacket.chatType().resolve(this.registryAccess.compositeAccess());
		if (!optional.isEmpty() && !optional2.isEmpty()) {
			UUID uUID = clientboundPlayerChatPacket.sender();
			PlayerInfo playerInfo = this.getPlayerInfo(uUID);
			if (playerInfo != null && playerInfo.getChatSession() != null) {
				RemoteChatSession remoteChatSession = playerInfo.getChatSession();
				SignedMessageLink signedMessageLink = new SignedMessageLink(clientboundPlayerChatPacket.index(), uUID, remoteChatSession.sessionId());
				PlayerChatMessage playerChatMessage = new PlayerChatMessage(
					signedMessageLink,
					clientboundPlayerChatPacket.signature(),
					(SignedMessageBody)optional.get(),
					clientboundPlayerChatPacket.unsignedContent(),
					clientboundPlayerChatPacket.filterMask()
				);
				if (!playerInfo.getMessageValidator().updateAndValidate(playerChatMessage)) {
					this.connection.disconnect(CHAT_VALIDATION_FAILED_ERROR);
				} else {
					this.minecraft.getChatListener().handlePlayerChatMessage(playerChatMessage, playerInfo.getProfile(), (ChatType.Bound)optional2.get());
					this.messageSignatureCache.push(playerChatMessage);
				}
			} else {
				this.connection.disconnect(CHAT_VALIDATION_FAILED_ERROR);
			}
		} else {
			this.connection.disconnect(INVALID_PACKET);
		}
	}

	@Override
	public void handleDisguisedChat(ClientboundDisguisedChatPacket clientboundDisguisedChatPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundDisguisedChatPacket, this, this.minecraft);
		Optional<ChatType.Bound> optional = clientboundDisguisedChatPacket.chatType().resolve(this.registryAccess.compositeAccess());
		if (optional.isEmpty()) {
			this.connection.disconnect(INVALID_PACKET);
		} else {
			this.minecraft.getChatListener().handleDisguisedChatMessage(clientboundDisguisedChatPacket.message(), (ChatType.Bound)optional.get());
		}
	}

	@Override
	public void handleDeleteChat(ClientboundDeleteChatPacket clientboundDeleteChatPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundDeleteChatPacket, this, this.minecraft);
		Optional<MessageSignature> optional = clientboundDeleteChatPacket.messageSignature().unpack(this.messageSignatureUnpacker);
		if (optional.isEmpty()) {
			this.connection.disconnect(INVALID_PACKET);
		} else {
			this.lastSeenMessages.ignorePending((MessageSignature)optional.get());
			if (!this.minecraft.getChatListener().removeFromDelayedMessageQueue((MessageSignature)optional.get())) {
				this.minecraft.gui.getChat().deleteMessage((MessageSignature)optional.get());
			}
		}
	}

	@Override
	public void handleAnimate(ClientboundAnimatePacket clientboundAnimatePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundAnimatePacket, this, this.minecraft);
		Entity entity = this.level.getEntity(clientboundAnimatePacket.getId());
		if (entity != null) {
			if (clientboundAnimatePacket.getAction() == 0) {
				LivingEntity livingEntity = (LivingEntity)entity;
				livingEntity.swing(InteractionHand.MAIN_HAND);
			} else if (clientboundAnimatePacket.getAction() == 3) {
				LivingEntity livingEntity = (LivingEntity)entity;
				livingEntity.swing(InteractionHand.OFF_HAND);
			} else if (clientboundAnimatePacket.getAction() == 1) {
				entity.animateHurt();
			} else if (clientboundAnimatePacket.getAction() == 2) {
				Player player = (Player)entity;
				player.stopSleepInBed(false, false);
			} else if (clientboundAnimatePacket.getAction() == 4) {
				this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.CRIT);
			} else if (clientboundAnimatePacket.getAction() == 5) {
				this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.ENCHANTED_HIT);
			}
		}
	}

	@Override
	public void handleSetTime(ClientboundSetTimePacket clientboundSetTimePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetTimePacket, this, this.minecraft);
		this.minecraft.level.setGameTime(clientboundSetTimePacket.getGameTime());
		this.minecraft.level.setDayTime(clientboundSetTimePacket.getDayTime());
	}

	@Override
	public void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket clientboundSetDefaultSpawnPositionPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetDefaultSpawnPositionPacket, this, this.minecraft);
		this.minecraft.level.setDefaultSpawnPos(clientboundSetDefaultSpawnPositionPacket.getPos(), clientboundSetDefaultSpawnPositionPacket.getAngle());
		if (this.minecraft.screen instanceof ReceivingLevelScreen receivingLevelScreen) {
			receivingLevelScreen.loadingPacketsReceived();
		}
	}

	@Override
	public void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket clientboundSetPassengersPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetPassengersPacket, this, this.minecraft);
		Entity entity = this.level.getEntity(clientboundSetPassengersPacket.getVehicle());
		if (entity == null) {
			LOGGER.warn("Received passengers for unknown entity");
		} else {
			boolean bl = entity.hasIndirectPassenger(this.minecraft.player);
			entity.ejectPassengers();

			for (int i : clientboundSetPassengersPacket.getPassengers()) {
				Entity entity2 = this.level.getEntity(i);
				if (entity2 != null) {
					entity2.startRiding(entity, true);
					if (entity2 == this.minecraft.player && !bl) {
						if (entity instanceof Boat) {
							this.minecraft.player.yRotO = entity.getYRot();
							this.minecraft.player.setYRot(entity.getYRot());
							this.minecraft.player.setYHeadRot(entity.getYRot());
						}

						Component component = Component.translatable("mount.onboard", this.minecraft.options.keyShift.getTranslatedKeyMessage());
						this.minecraft.gui.setOverlayMessage(component, false);
						this.minecraft.getNarrator().sayNow(component);
					}
				}
			}
		}
	}

	@Override
	public void handleEntityLinkPacket(ClientboundSetEntityLinkPacket clientboundSetEntityLinkPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetEntityLinkPacket, this, this.minecraft);
		Entity entity = this.level.getEntity(clientboundSetEntityLinkPacket.getSourceId());
		if (entity instanceof Mob) {
			((Mob)entity).setDelayedLeashHolderId(clientboundSetEntityLinkPacket.getDestId());
		}
	}

	private static ItemStack findTotem(Player player) {
		for (InteractionHand interactionHand : InteractionHand.values()) {
			ItemStack itemStack = player.getItemInHand(interactionHand);
			if (itemStack.is(Items.TOTEM_OF_UNDYING)) {
				return itemStack;
			}
		}

		return new ItemStack(Items.TOTEM_OF_UNDYING);
	}

	@Override
	public void handleEntityEvent(ClientboundEntityEventPacket clientboundEntityEventPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundEntityEventPacket, this, this.minecraft);
		Entity entity = clientboundEntityEventPacket.getEntity(this.level);
		if (entity != null) {
			if (clientboundEntityEventPacket.getEventId() == 21) {
				this.minecraft.getSoundManager().play(new GuardianAttackSoundInstance((Guardian)entity));
			} else if (clientboundEntityEventPacket.getEventId() == 35) {
				int i = 40;
				this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
				this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0F, 1.0F, false);
				if (entity == this.minecraft.player) {
					this.minecraft.gameRenderer.displayItemActivation(findTotem(this.minecraft.player));
				}
			} else {
				entity.handleEntityEvent(clientboundEntityEventPacket.getEventId());
			}
		}
	}

	@Override
	public void handleSetHealth(ClientboundSetHealthPacket clientboundSetHealthPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetHealthPacket, this, this.minecraft);
		this.minecraft.player.hurtTo(clientboundSetHealthPacket.getHealth());
		this.minecraft.player.getFoodData().setFoodLevel(clientboundSetHealthPacket.getFood());
		this.minecraft.player.getFoodData().setSaturation(clientboundSetHealthPacket.getSaturation());
	}

	@Override
	public void handleSetExperience(ClientboundSetExperiencePacket clientboundSetExperiencePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetExperiencePacket, this, this.minecraft);
		this.minecraft
			.player
			.setExperienceValues(
				clientboundSetExperiencePacket.getExperienceProgress(),
				clientboundSetExperiencePacket.getTotalExperience(),
				clientboundSetExperiencePacket.getExperienceLevel()
			);
	}

	@Override
	public void handleRespawn(ClientboundRespawnPacket clientboundRespawnPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundRespawnPacket, this, this.minecraft);
		ResourceKey<Level> resourceKey = clientboundRespawnPacket.getDimension();
		Holder<DimensionType> holder = this.registryAccess
			.compositeAccess()
			.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY)
			.getHolderOrThrow(clientboundRespawnPacket.getDimensionType());
		LocalPlayer localPlayer = this.minecraft.player;
		int i = localPlayer.getId();
		if (resourceKey != localPlayer.level.dimension()) {
			Scoreboard scoreboard = this.level.getScoreboard();
			Map<String, MapItemSavedData> map = this.level.getAllMapData();
			boolean bl = clientboundRespawnPacket.isDebug();
			boolean bl2 = clientboundRespawnPacket.isFlat();
			ClientLevel.ClientLevelData clientLevelData = new ClientLevel.ClientLevelData(this.levelData.getDifficulty(), this.levelData.isHardcore(), bl2);
			this.levelData = clientLevelData;
			this.level = new ClientLevel(
				this,
				clientLevelData,
				resourceKey,
				holder,
				this.serverChunkRadius,
				this.serverSimulationDistance,
				this.minecraft::getProfiler,
				this.minecraft.levelRenderer,
				bl,
				clientboundRespawnPacket.getSeed()
			);
			this.level.setScoreboard(scoreboard);
			this.level.addMapData(map);
			this.minecraft.setLevel(this.level);
			this.minecraft.setScreen(new ReceivingLevelScreen());
		}

		String string = localPlayer.getServerBrand();
		this.minecraft.cameraEntity = null;
		if (localPlayer.hasContainerOpen()) {
			localPlayer.closeContainer();
		}

		LocalPlayer localPlayer2 = this.minecraft
			.gameMode
			.createPlayer(this.level, localPlayer.getStats(), localPlayer.getRecipeBook(), localPlayer.isShiftKeyDown(), localPlayer.isSprinting());
		localPlayer2.setId(i);
		this.minecraft.player = localPlayer2;
		if (resourceKey != localPlayer.level.dimension()) {
			this.minecraft.getMusicManager().stopPlaying();
		}

		this.minecraft.cameraEntity = localPlayer2;
		localPlayer2.getEntityData().assignValues(localPlayer.getEntityData().getAll());
		if (clientboundRespawnPacket.shouldKeepAllPlayerData()) {
			localPlayer2.getAttributes().assignValues(localPlayer.getAttributes());
		}

		localPlayer2.resetPos();
		localPlayer2.setServerBrand(string);
		this.level.addPlayer(i, localPlayer2);
		localPlayer2.setYRot(-180.0F);
		localPlayer2.input = new KeyboardInput(this.minecraft.options);
		this.minecraft.gameMode.adjustPlayer(localPlayer2);
		localPlayer2.setReducedDebugInfo(localPlayer.isReducedDebugInfo());
		localPlayer2.setShowDeathScreen(localPlayer.shouldShowDeathScreen());
		localPlayer2.setLastDeathLocation(clientboundRespawnPacket.getLastDeathLocation());
		if (this.minecraft.screen instanceof DeathScreen) {
			this.minecraft.setScreen(null);
		}

		this.minecraft.gameMode.setLocalMode(clientboundRespawnPacket.getPlayerGameType(), clientboundRespawnPacket.getPreviousPlayerGameType());
	}

	@Override
	public void handleExplosion(ClientboundExplodePacket clientboundExplodePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundExplodePacket, this, this.minecraft);
		Explosion explosion = new Explosion(
			this.minecraft.level,
			null,
			clientboundExplodePacket.getX(),
			clientboundExplodePacket.getY(),
			clientboundExplodePacket.getZ(),
			clientboundExplodePacket.getPower(),
			clientboundExplodePacket.getToBlow()
		);
		explosion.finalizeExplosion(true);
		this.minecraft
			.player
			.setDeltaMovement(
				this.minecraft
					.player
					.getDeltaMovement()
					.add((double)clientboundExplodePacket.getKnockbackX(), (double)clientboundExplodePacket.getKnockbackY(), (double)clientboundExplodePacket.getKnockbackZ())
			);
	}

	@Override
	public void handleHorseScreenOpen(ClientboundHorseScreenOpenPacket clientboundHorseScreenOpenPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundHorseScreenOpenPacket, this, this.minecraft);
		Entity entity = this.level.getEntity(clientboundHorseScreenOpenPacket.getEntityId());
		if (entity instanceof AbstractHorse) {
			LocalPlayer localPlayer = this.minecraft.player;
			AbstractHorse abstractHorse = (AbstractHorse)entity;
			SimpleContainer simpleContainer = new SimpleContainer(clientboundHorseScreenOpenPacket.getSize());
			HorseInventoryMenu horseInventoryMenu = new HorseInventoryMenu(
				clientboundHorseScreenOpenPacket.getContainerId(), localPlayer.getInventory(), simpleContainer, abstractHorse
			);
			localPlayer.containerMenu = horseInventoryMenu;
			this.minecraft.setScreen(new HorseInventoryScreen(horseInventoryMenu, localPlayer.getInventory(), abstractHorse));
		}
	}

	@Override
	public void handleOpenScreen(ClientboundOpenScreenPacket clientboundOpenScreenPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundOpenScreenPacket, this, this.minecraft);
		MenuScreens.create(
			clientboundOpenScreenPacket.getType(), this.minecraft, clientboundOpenScreenPacket.getContainerId(), clientboundOpenScreenPacket.getTitle()
		);
	}

	@Override
	public void handleContainerSetSlot(ClientboundContainerSetSlotPacket clientboundContainerSetSlotPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundContainerSetSlotPacket, this, this.minecraft);
		Player player = this.minecraft.player;
		ItemStack itemStack = clientboundContainerSetSlotPacket.getItem();
		int i = clientboundContainerSetSlotPacket.getSlot();
		this.minecraft.getTutorial().onGetItem(itemStack);
		if (clientboundContainerSetSlotPacket.getContainerId() == -1) {
			if (!(this.minecraft.screen instanceof CreativeModeInventoryScreen)) {
				player.containerMenu.setCarried(itemStack);
			}
		} else if (clientboundContainerSetSlotPacket.getContainerId() == -2) {
			player.getInventory().setItem(i, itemStack);
		} else {
			boolean bl = false;
			if (this.minecraft.screen instanceof CreativeModeInventoryScreen creativeModeInventoryScreen) {
				bl = creativeModeInventoryScreen.getSelectedTab() != CreativeModeTabs.TAB_INVENTORY.getId();
			}

			if (clientboundContainerSetSlotPacket.getContainerId() == 0 && InventoryMenu.isHotbarSlot(i)) {
				if (!itemStack.isEmpty()) {
					ItemStack itemStack2 = player.inventoryMenu.getSlot(i).getItem();
					if (itemStack2.isEmpty() || itemStack2.getCount() < itemStack.getCount()) {
						itemStack.setPopTime(5);
					}
				}

				player.inventoryMenu.setItem(i, clientboundContainerSetSlotPacket.getStateId(), itemStack);
			} else if (clientboundContainerSetSlotPacket.getContainerId() == player.containerMenu.containerId
				&& (clientboundContainerSetSlotPacket.getContainerId() != 0 || !bl)) {
				player.containerMenu.setItem(i, clientboundContainerSetSlotPacket.getStateId(), itemStack);
			}
		}
	}

	@Override
	public void handleContainerContent(ClientboundContainerSetContentPacket clientboundContainerSetContentPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundContainerSetContentPacket, this, this.minecraft);
		Player player = this.minecraft.player;
		if (clientboundContainerSetContentPacket.getContainerId() == 0) {
			player.inventoryMenu
				.initializeContents(
					clientboundContainerSetContentPacket.getStateId(), clientboundContainerSetContentPacket.getItems(), clientboundContainerSetContentPacket.getCarriedItem()
				);
		} else if (clientboundContainerSetContentPacket.getContainerId() == player.containerMenu.containerId) {
			player.containerMenu
				.initializeContents(
					clientboundContainerSetContentPacket.getStateId(), clientboundContainerSetContentPacket.getItems(), clientboundContainerSetContentPacket.getCarriedItem()
				);
		}
	}

	@Override
	public void handleOpenSignEditor(ClientboundOpenSignEditorPacket clientboundOpenSignEditorPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundOpenSignEditorPacket, this, this.minecraft);
		BlockPos blockPos = clientboundOpenSignEditorPacket.getPos();
		BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
		if (!(blockEntity instanceof SignBlockEntity)) {
			BlockState blockState = this.level.getBlockState(blockPos);
			blockEntity = new SignBlockEntity(blockPos, blockState);
			blockEntity.setLevel(this.level);
		}

		this.minecraft.player.openTextEdit((SignBlockEntity)blockEntity);
	}

	@Override
	public void handleBlockEntityData(ClientboundBlockEntityDataPacket clientboundBlockEntityDataPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundBlockEntityDataPacket, this, this.minecraft);
		BlockPos blockPos = clientboundBlockEntityDataPacket.getPos();
		this.minecraft.level.getBlockEntity(blockPos, clientboundBlockEntityDataPacket.getType()).ifPresent(blockEntity -> {
			CompoundTag compoundTag = clientboundBlockEntityDataPacket.getTag();
			if (compoundTag != null) {
				blockEntity.load(compoundTag);
			}

			if (blockEntity instanceof CommandBlockEntity && this.minecraft.screen instanceof CommandBlockEditScreen) {
				((CommandBlockEditScreen)this.minecraft.screen).updateGui();
			}
		});
	}

	@Override
	public void handleContainerSetData(ClientboundContainerSetDataPacket clientboundContainerSetDataPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundContainerSetDataPacket, this, this.minecraft);
		Player player = this.minecraft.player;
		if (player.containerMenu != null && player.containerMenu.containerId == clientboundContainerSetDataPacket.getContainerId()) {
			player.containerMenu.setData(clientboundContainerSetDataPacket.getId(), clientboundContainerSetDataPacket.getValue());
		}
	}

	@Override
	public void handleSetEquipment(ClientboundSetEquipmentPacket clientboundSetEquipmentPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetEquipmentPacket, this, this.minecraft);
		Entity entity = this.level.getEntity(clientboundSetEquipmentPacket.getEntity());
		if (entity != null) {
			clientboundSetEquipmentPacket.getSlots().forEach(pair -> entity.setItemSlot((EquipmentSlot)pair.getFirst(), (ItemStack)pair.getSecond()));
		}
	}

	@Override
	public void handleContainerClose(ClientboundContainerClosePacket clientboundContainerClosePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundContainerClosePacket, this, this.minecraft);
		this.minecraft.player.clientSideCloseContainer();
	}

	@Override
	public void handleBlockEvent(ClientboundBlockEventPacket clientboundBlockEventPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundBlockEventPacket, this, this.minecraft);
		this.minecraft
			.level
			.blockEvent(
				clientboundBlockEventPacket.getPos(), clientboundBlockEventPacket.getBlock(), clientboundBlockEventPacket.getB0(), clientboundBlockEventPacket.getB1()
			);
	}

	@Override
	public void handleBlockDestruction(ClientboundBlockDestructionPacket clientboundBlockDestructionPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundBlockDestructionPacket, this, this.minecraft);
		this.minecraft
			.level
			.destroyBlockProgress(clientboundBlockDestructionPacket.getId(), clientboundBlockDestructionPacket.getPos(), clientboundBlockDestructionPacket.getProgress());
	}

	@Override
	public void handleGameEvent(ClientboundGameEventPacket clientboundGameEventPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundGameEventPacket, this, this.minecraft);
		Player player = this.minecraft.player;
		ClientboundGameEventPacket.Type type = clientboundGameEventPacket.getEvent();
		float f = clientboundGameEventPacket.getParam();
		int i = Mth.floor(f + 0.5F);
		if (type == ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE) {
			player.displayClientMessage(Component.translatable("block.minecraft.spawn.not_valid"), false);
		} else if (type == ClientboundGameEventPacket.START_RAINING) {
			this.level.getLevelData().setRaining(true);
			this.level.setRainLevel(0.0F);
		} else if (type == ClientboundGameEventPacket.STOP_RAINING) {
			this.level.getLevelData().setRaining(false);
			this.level.setRainLevel(1.0F);
		} else if (type == ClientboundGameEventPacket.CHANGE_GAME_MODE) {
			this.minecraft.gameMode.setLocalMode(GameType.byId(i));
		} else if (type == ClientboundGameEventPacket.WIN_GAME) {
			if (i == 0) {
				this.minecraft.player.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
				this.minecraft.setScreen(new ReceivingLevelScreen());
			} else if (i == 1) {
				this.minecraft
					.setScreen(
						new WinScreen(
							true, () -> this.minecraft.player.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN))
						)
					);
			}
		} else if (type == ClientboundGameEventPacket.DEMO_EVENT) {
			Options options = this.minecraft.options;
			if (f == 0.0F) {
				this.minecraft.setScreen(new DemoIntroScreen());
			} else if (f == 101.0F) {
				this.minecraft
					.gui
					.getChat()
					.addMessage(
						Component.translatable(
							"demo.help.movement",
							options.keyUp.getTranslatedKeyMessage(),
							options.keyLeft.getTranslatedKeyMessage(),
							options.keyDown.getTranslatedKeyMessage(),
							options.keyRight.getTranslatedKeyMessage()
						)
					);
			} else if (f == 102.0F) {
				this.minecraft.gui.getChat().addMessage(Component.translatable("demo.help.jump", options.keyJump.getTranslatedKeyMessage()));
			} else if (f == 103.0F) {
				this.minecraft.gui.getChat().addMessage(Component.translatable("demo.help.inventory", options.keyInventory.getTranslatedKeyMessage()));
			} else if (f == 104.0F) {
				this.minecraft.gui.getChat().addMessage(Component.translatable("demo.day.6", options.keyScreenshot.getTranslatedKeyMessage()));
			}
		} else if (type == ClientboundGameEventPacket.ARROW_HIT_PLAYER) {
			this.level.playSound(player, player.getX(), player.getEyeY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.18F, 0.45F);
		} else if (type == ClientboundGameEventPacket.RAIN_LEVEL_CHANGE) {
			this.level.setRainLevel(f);
		} else if (type == ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE) {
			this.level.setThunderLevel(f);
		} else if (type == ClientboundGameEventPacket.PUFFER_FISH_STING) {
			this.level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.PUFFER_FISH_STING, SoundSource.NEUTRAL, 1.0F, 1.0F);
		} else if (type == ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT) {
			this.level.addParticle(ParticleTypes.ELDER_GUARDIAN, player.getX(), player.getY(), player.getZ(), 0.0, 0.0, 0.0);
			if (i == 1) {
				this.level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.0F, 1.0F);
			}
		} else if (type == ClientboundGameEventPacket.IMMEDIATE_RESPAWN) {
			this.minecraft.player.setShowDeathScreen(f == 0.0F);
		}
	}

	@Override
	public void handleMapItemData(ClientboundMapItemDataPacket clientboundMapItemDataPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundMapItemDataPacket, this, this.minecraft);
		MapRenderer mapRenderer = this.minecraft.gameRenderer.getMapRenderer();
		int i = clientboundMapItemDataPacket.getMapId();
		String string = MapItem.makeKey(i);
		MapItemSavedData mapItemSavedData = this.minecraft.level.getMapData(string);
		if (mapItemSavedData == null) {
			mapItemSavedData = MapItemSavedData.createForClient(
				clientboundMapItemDataPacket.getScale(), clientboundMapItemDataPacket.isLocked(), this.minecraft.level.dimension()
			);
			this.minecraft.level.setMapData(string, mapItemSavedData);
		}

		clientboundMapItemDataPacket.applyToMap(mapItemSavedData);
		mapRenderer.update(i, mapItemSavedData);
	}

	@Override
	public void handleLevelEvent(ClientboundLevelEventPacket clientboundLevelEventPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundLevelEventPacket, this, this.minecraft);
		if (clientboundLevelEventPacket.isGlobalEvent()) {
			this.minecraft.level.globalLevelEvent(clientboundLevelEventPacket.getType(), clientboundLevelEventPacket.getPos(), clientboundLevelEventPacket.getData());
		} else {
			this.minecraft.level.levelEvent(clientboundLevelEventPacket.getType(), clientboundLevelEventPacket.getPos(), clientboundLevelEventPacket.getData());
		}
	}

	@Override
	public void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket clientboundUpdateAdvancementsPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundUpdateAdvancementsPacket, this, this.minecraft);
		this.advancements.update(clientboundUpdateAdvancementsPacket);
	}

	@Override
	public void handleSelectAdvancementsTab(ClientboundSelectAdvancementsTabPacket clientboundSelectAdvancementsTabPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSelectAdvancementsTabPacket, this, this.minecraft);
		ResourceLocation resourceLocation = clientboundSelectAdvancementsTabPacket.getTab();
		if (resourceLocation == null) {
			this.advancements.setSelectedTab(null, false);
		} else {
			Advancement advancement = this.advancements.getAdvancements().get(resourceLocation);
			this.advancements.setSelectedTab(advancement, false);
		}
	}

	@Override
	public void handleCommands(ClientboundCommandsPacket clientboundCommandsPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundCommandsPacket, this, this.minecraft);
		this.commands = new CommandDispatcher<>(
			clientboundCommandsPacket.getRoot(new CommandBuildContext(this.registryAccess.compositeAccess(), this.enabledFeatures))
		);
	}

	@Override
	public void handleStopSoundEvent(ClientboundStopSoundPacket clientboundStopSoundPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundStopSoundPacket, this, this.minecraft);
		this.minecraft.getSoundManager().stop(clientboundStopSoundPacket.getName(), clientboundStopSoundPacket.getSource());
	}

	@Override
	public void handleCommandSuggestions(ClientboundCommandSuggestionsPacket clientboundCommandSuggestionsPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundCommandSuggestionsPacket, this, this.minecraft);
		this.suggestionsProvider.completeCustomSuggestions(clientboundCommandSuggestionsPacket.getId(), clientboundCommandSuggestionsPacket.getSuggestions());
	}

	@Override
	public void handleUpdateRecipes(ClientboundUpdateRecipesPacket clientboundUpdateRecipesPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundUpdateRecipesPacket, this, this.minecraft);
		this.recipeManager.replaceRecipes(clientboundUpdateRecipesPacket.getRecipes());
		ClientRecipeBook clientRecipeBook = this.minecraft.player.getRecipeBook();
		clientRecipeBook.setupCollections(this.recipeManager.getRecipes());
		this.minecraft.populateSearchTree(SearchRegistry.RECIPE_COLLECTIONS, clientRecipeBook.getCollections());
	}

	@Override
	public void handleLookAt(ClientboundPlayerLookAtPacket clientboundPlayerLookAtPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundPlayerLookAtPacket, this, this.minecraft);
		Vec3 vec3 = clientboundPlayerLookAtPacket.getPosition(this.level);
		if (vec3 != null) {
			this.minecraft.player.lookAt(clientboundPlayerLookAtPacket.getFromAnchor(), vec3);
		}
	}

	@Override
	public void handleTagQueryPacket(ClientboundTagQueryPacket clientboundTagQueryPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundTagQueryPacket, this, this.minecraft);
		if (!this.debugQueryHandler.handleResponse(clientboundTagQueryPacket.getTransactionId(), clientboundTagQueryPacket.getTag())) {
			LOGGER.debug("Got unhandled response to tag query {}", clientboundTagQueryPacket.getTransactionId());
		}
	}

	@Override
	public void handleAwardStats(ClientboundAwardStatsPacket clientboundAwardStatsPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundAwardStatsPacket, this, this.minecraft);

		for (Entry<Stat<?>, Integer> entry : clientboundAwardStatsPacket.getStats().entrySet()) {
			Stat<?> stat = (Stat<?>)entry.getKey();
			int i = (Integer)entry.getValue();
			this.minecraft.player.getStats().setValue(this.minecraft.player, stat, i);
		}

		if (this.minecraft.screen instanceof StatsUpdateListener) {
			((StatsUpdateListener)this.minecraft.screen).onStatsUpdated();
		}
	}

	@Override
	public void handleAddOrRemoveRecipes(ClientboundRecipePacket clientboundRecipePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundRecipePacket, this, this.minecraft);
		ClientRecipeBook clientRecipeBook = this.minecraft.player.getRecipeBook();
		clientRecipeBook.setBookSettings(clientboundRecipePacket.getBookSettings());
		ClientboundRecipePacket.State state = clientboundRecipePacket.getState();
		switch (state) {
			case REMOVE:
				for (ResourceLocation resourceLocation : clientboundRecipePacket.getRecipes()) {
					this.recipeManager.byKey(resourceLocation).ifPresent(clientRecipeBook::remove);
				}
				break;
			case INIT:
				for (ResourceLocation resourceLocation : clientboundRecipePacket.getRecipes()) {
					this.recipeManager.byKey(resourceLocation).ifPresent(clientRecipeBook::add);
				}

				for (ResourceLocation resourceLocation : clientboundRecipePacket.getHighlights()) {
					this.recipeManager.byKey(resourceLocation).ifPresent(clientRecipeBook::addHighlight);
				}
				break;
			case ADD:
				for (ResourceLocation resourceLocation : clientboundRecipePacket.getRecipes()) {
					this.recipeManager.byKey(resourceLocation).ifPresent(recipe -> {
						clientRecipeBook.add(recipe);
						clientRecipeBook.addHighlight(recipe);
						RecipeToast.addOrUpdate(this.minecraft.getToasts(), recipe);
					});
				}
		}

		clientRecipeBook.getCollections().forEach(recipeCollection -> recipeCollection.updateKnownRecipes(clientRecipeBook));
		if (this.minecraft.screen instanceof RecipeUpdateListener) {
			((RecipeUpdateListener)this.minecraft.screen).recipesUpdated();
		}
	}

	@Override
	public void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket clientboundUpdateMobEffectPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundUpdateMobEffectPacket, this, this.minecraft);
		Entity entity = this.level.getEntity(clientboundUpdateMobEffectPacket.getEntityId());
		if (entity instanceof LivingEntity) {
			MobEffect mobEffect = clientboundUpdateMobEffectPacket.getEffect();
			if (mobEffect != null) {
				MobEffectInstance mobEffectInstance = new MobEffectInstance(
					mobEffect,
					clientboundUpdateMobEffectPacket.getEffectDurationTicks(),
					clientboundUpdateMobEffectPacket.getEffectAmplifier(),
					clientboundUpdateMobEffectPacket.isEffectAmbient(),
					clientboundUpdateMobEffectPacket.isEffectVisible(),
					clientboundUpdateMobEffectPacket.effectShowsIcon(),
					null,
					Optional.ofNullable(clientboundUpdateMobEffectPacket.getFactorData())
				);
				mobEffectInstance.setNoCounter(clientboundUpdateMobEffectPacket.isSuperLongDuration());
				((LivingEntity)entity).forceAddEffect(mobEffectInstance, null);
			}
		}
	}

	@Override
	public void handleUpdateTags(ClientboundUpdateTagsPacket clientboundUpdateTagsPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundUpdateTagsPacket, this, this.minecraft);
		clientboundUpdateTagsPacket.getTags().forEach(this::updateTagsForRegistry);
		if (!this.connection.isMemoryConnection()) {
			Blocks.rebuildCache();
		}

		this.rebuildCreativeScreenSearchData();
	}

	@Override
	public void handleEnabledFeatures(ClientboundUpdateEnabledFeaturesPacket clientboundUpdateEnabledFeaturesPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundUpdateEnabledFeaturesPacket, this, this.minecraft);
		this.enabledFeatures = FeatureFlags.REGISTRY.fromNames(clientboundUpdateEnabledFeaturesPacket.features());
		this.rebuildCreativeScreenSearchData();
	}

	private void rebuildCreativeScreenSearchData() {
		for (CreativeModeTab creativeModeTab : CreativeModeTabs.TABS) {
			creativeModeTab.invalidateDisplayListCache();
		}

		NonNullList<ItemStack> nonNullList = NonNullList.create();
		nonNullList.addAll(CreativeModeTabs.TAB_SEARCH.getDisplayItems(this.enabledFeatures));
		this.minecraft.populateSearchTree(SearchRegistry.CREATIVE_NAMES, nonNullList);
		this.minecraft.populateSearchTree(SearchRegistry.CREATIVE_TAGS, nonNullList);
	}

	private <T> void updateTagsForRegistry(ResourceKey<? extends Registry<? extends T>> resourceKey, TagNetworkSerialization.NetworkPayload networkPayload) {
		if (!networkPayload.isEmpty()) {
			Registry<T> registry = (Registry<T>)this.registryAccess
				.compositeAccess()
				.registry(resourceKey)
				.orElseThrow(() -> new IllegalStateException("Unknown registry " + resourceKey));
			Map<TagKey<T>, List<Holder<T>>> map = new HashMap();
			TagNetworkSerialization.deserializeTagsFromNetwork(resourceKey, registry, networkPayload, map::put);
			registry.bindTags(map);
		}
	}

	@Override
	public void handlePlayerCombatEnd(ClientboundPlayerCombatEndPacket clientboundPlayerCombatEndPacket) {
	}

	@Override
	public void handlePlayerCombatEnter(ClientboundPlayerCombatEnterPacket clientboundPlayerCombatEnterPacket) {
	}

	@Override
	public void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket clientboundPlayerCombatKillPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundPlayerCombatKillPacket, this, this.minecraft);
		Entity entity = this.level.getEntity(clientboundPlayerCombatKillPacket.getPlayerId());
		if (entity == this.minecraft.player) {
			if (this.minecraft.player.shouldShowDeathScreen()) {
				this.minecraft.setScreen(new DeathScreen(clientboundPlayerCombatKillPacket.getMessage(), this.level.getLevelData().isHardcore()));
			} else {
				this.minecraft.player.respawn();
			}
		}
	}

	@Override
	public void handleChangeDifficulty(ClientboundChangeDifficultyPacket clientboundChangeDifficultyPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundChangeDifficultyPacket, this, this.minecraft);
		this.levelData.setDifficulty(clientboundChangeDifficultyPacket.getDifficulty());
		this.levelData.setDifficultyLocked(clientboundChangeDifficultyPacket.isLocked());
	}

	@Override
	public void handleSetCamera(ClientboundSetCameraPacket clientboundSetCameraPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetCameraPacket, this, this.minecraft);
		Entity entity = clientboundSetCameraPacket.getEntity(this.level);
		if (entity != null) {
			this.minecraft.setCameraEntity(entity);
		}
	}

	@Override
	public void handleInitializeBorder(ClientboundInitializeBorderPacket clientboundInitializeBorderPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundInitializeBorderPacket, this, this.minecraft);
		WorldBorder worldBorder = this.level.getWorldBorder();
		worldBorder.setCenter(clientboundInitializeBorderPacket.getNewCenterX(), clientboundInitializeBorderPacket.getNewCenterZ());
		long l = clientboundInitializeBorderPacket.getLerpTime();
		if (l > 0L) {
			worldBorder.lerpSizeBetween(clientboundInitializeBorderPacket.getOldSize(), clientboundInitializeBorderPacket.getNewSize(), l);
		} else {
			worldBorder.setSize(clientboundInitializeBorderPacket.getNewSize());
		}

		worldBorder.setAbsoluteMaxSize(clientboundInitializeBorderPacket.getNewAbsoluteMaxSize());
		worldBorder.setWarningBlocks(clientboundInitializeBorderPacket.getWarningBlocks());
		worldBorder.setWarningTime(clientboundInitializeBorderPacket.getWarningTime());
	}

	@Override
	public void handleSetBorderCenter(ClientboundSetBorderCenterPacket clientboundSetBorderCenterPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetBorderCenterPacket, this, this.minecraft);
		this.level.getWorldBorder().setCenter(clientboundSetBorderCenterPacket.getNewCenterX(), clientboundSetBorderCenterPacket.getNewCenterZ());
	}

	@Override
	public void handleSetBorderLerpSize(ClientboundSetBorderLerpSizePacket clientboundSetBorderLerpSizePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetBorderLerpSizePacket, this, this.minecraft);
		this.level
			.getWorldBorder()
			.lerpSizeBetween(
				clientboundSetBorderLerpSizePacket.getOldSize(), clientboundSetBorderLerpSizePacket.getNewSize(), clientboundSetBorderLerpSizePacket.getLerpTime()
			);
	}

	@Override
	public void handleSetBorderSize(ClientboundSetBorderSizePacket clientboundSetBorderSizePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetBorderSizePacket, this, this.minecraft);
		this.level.getWorldBorder().setSize(clientboundSetBorderSizePacket.getSize());
	}

	@Override
	public void handleSetBorderWarningDistance(ClientboundSetBorderWarningDistancePacket clientboundSetBorderWarningDistancePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetBorderWarningDistancePacket, this, this.minecraft);
		this.level.getWorldBorder().setWarningBlocks(clientboundSetBorderWarningDistancePacket.getWarningBlocks());
	}

	@Override
	public void handleSetBorderWarningDelay(ClientboundSetBorderWarningDelayPacket clientboundSetBorderWarningDelayPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetBorderWarningDelayPacket, this, this.minecraft);
		this.level.getWorldBorder().setWarningTime(clientboundSetBorderWarningDelayPacket.getWarningDelay());
	}

	@Override
	public void handleTitlesClear(ClientboundClearTitlesPacket clientboundClearTitlesPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundClearTitlesPacket, this, this.minecraft);
		this.minecraft.gui.clear();
		if (clientboundClearTitlesPacket.shouldResetTimes()) {
			this.minecraft.gui.resetTitleTimes();
		}
	}

	@Override
	public void handleServerData(ClientboundServerDataPacket clientboundServerDataPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundServerDataPacket, this, this.minecraft);
		if (this.serverData != null) {
			clientboundServerDataPacket.getMotd().ifPresent(component -> this.serverData.motd = component);
			clientboundServerDataPacket.getIconBase64().ifPresent(string -> {
				try {
					this.serverData.setIconB64(ServerData.parseFavicon(string));
				} catch (ParseException var3) {
					LOGGER.error("Invalid server icon", (Throwable)var3);
				}
			});
			this.serverData.setEnforcesSecureChat(clientboundServerDataPacket.enforcesSecureChat());
			ServerList.saveSingleServer(this.serverData);
			if (!clientboundServerDataPacket.enforcesSecureChat()) {
				SystemToast systemToast = SystemToast.multiline(
					this.minecraft, SystemToast.SystemToastIds.UNSECURE_SERVER_WARNING, UNSECURE_SERVER_TOAST_TITLE, UNSERURE_SERVER_TOAST
				);
				this.minecraft.getToasts().addToast(systemToast);
			}
		}
	}

	@Override
	public void handleCustomChatCompletions(ClientboundCustomChatCompletionsPacket clientboundCustomChatCompletionsPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundCustomChatCompletionsPacket, this, this.minecraft);
		this.suggestionsProvider.modifyCustomCompletions(clientboundCustomChatCompletionsPacket.action(), clientboundCustomChatCompletionsPacket.entries());
	}

	@Override
	public void setActionBarText(ClientboundSetActionBarTextPacket clientboundSetActionBarTextPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetActionBarTextPacket, this, this.minecraft);
		this.minecraft.gui.setOverlayMessage(clientboundSetActionBarTextPacket.getText(), false);
	}

	@Override
	public void setTitleText(ClientboundSetTitleTextPacket clientboundSetTitleTextPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetTitleTextPacket, this, this.minecraft);
		this.minecraft.gui.setTitle(clientboundSetTitleTextPacket.getText());
	}

	@Override
	public void setSubtitleText(ClientboundSetSubtitleTextPacket clientboundSetSubtitleTextPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetSubtitleTextPacket, this, this.minecraft);
		this.minecraft.gui.setSubtitle(clientboundSetSubtitleTextPacket.getText());
	}

	@Override
	public void setTitlesAnimation(ClientboundSetTitlesAnimationPacket clientboundSetTitlesAnimationPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetTitlesAnimationPacket, this, this.minecraft);
		this.minecraft
			.gui
			.setTimes(clientboundSetTitlesAnimationPacket.getFadeIn(), clientboundSetTitlesAnimationPacket.getStay(), clientboundSetTitlesAnimationPacket.getFadeOut());
	}

	@Override
	public void handleTabListCustomisation(ClientboundTabListPacket clientboundTabListPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundTabListPacket, this, this.minecraft);
		this.minecraft.gui.getTabList().setHeader(clientboundTabListPacket.getHeader().getString().isEmpty() ? null : clientboundTabListPacket.getHeader());
		this.minecraft.gui.getTabList().setFooter(clientboundTabListPacket.getFooter().getString().isEmpty() ? null : clientboundTabListPacket.getFooter());
	}

	@Override
	public void handleRemoveMobEffect(ClientboundRemoveMobEffectPacket clientboundRemoveMobEffectPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundRemoveMobEffectPacket, this, this.minecraft);
		Entity entity = clientboundRemoveMobEffectPacket.getEntity(this.level);
		if (entity instanceof LivingEntity) {
			((LivingEntity)entity).removeEffectNoUpdate(clientboundRemoveMobEffectPacket.getEffect());
		}
	}

	@Override
	public void handlePlayerInfoRemove(ClientboundPlayerInfoRemovePacket clientboundPlayerInfoRemovePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundPlayerInfoRemovePacket, this, this.minecraft);

		for (UUID uUID : clientboundPlayerInfoRemovePacket.profileIds()) {
			this.minecraft.getPlayerSocialManager().removePlayer(uUID);
			PlayerInfo playerInfo = (PlayerInfo)this.playerInfoMap.remove(uUID);
			if (playerInfo != null) {
				this.listedPlayers.remove(playerInfo);
			}
		}
	}

	@Override
	public void handlePlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket clientboundPlayerInfoUpdatePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundPlayerInfoUpdatePacket, this, this.minecraft);

		for (ClientboundPlayerInfoUpdatePacket.Entry entry : clientboundPlayerInfoUpdatePacket.newEntries()) {
			PlayerInfo playerInfo = new PlayerInfo(entry.profile());
			if (this.playerInfoMap.putIfAbsent(entry.profileId(), playerInfo) == null) {
				this.minecraft.getPlayerSocialManager().addPlayer(playerInfo);
			}
		}

		for (ClientboundPlayerInfoUpdatePacket.Entry entryx : clientboundPlayerInfoUpdatePacket.entries()) {
			PlayerInfo playerInfo = (PlayerInfo)this.playerInfoMap.get(entryx.profileId());
			if (playerInfo == null) {
				LOGGER.warn("Ignoring player info update for unknown player {}", entryx.profileId());
			} else {
				for (ClientboundPlayerInfoUpdatePacket.Action action : clientboundPlayerInfoUpdatePacket.actions()) {
					this.applyPlayerInfoUpdate(action, entryx, playerInfo);
				}
			}
		}
	}

	private void applyPlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket.Action action, ClientboundPlayerInfoUpdatePacket.Entry entry, PlayerInfo playerInfo) {
		switch (action) {
			case INITIALIZE_CHAT:
				this.initializeChatSession(entry, playerInfo);
				break;
			case UPDATE_GAME_MODE:
				playerInfo.setGameMode(entry.gameMode());
				break;
			case UPDATE_LISTED:
				if (entry.listed()) {
					this.listedPlayers.add(playerInfo);
				} else {
					this.listedPlayers.remove(playerInfo);
				}
				break;
			case UPDATE_LATENCY:
				playerInfo.setLatency(entry.latency());
				break;
			case UPDATE_DISPLAY_NAME:
				playerInfo.setTabListDisplayName(entry.displayName());
		}
	}

	private void initializeChatSession(ClientboundPlayerInfoUpdatePacket.Entry entry, PlayerInfo playerInfo) {
		RemoteChatSession remoteChatSession = this.validateChatSession(entry.chatSession(), playerInfo.getProfile());
		boolean bl = this.serverData != null && this.serverData.enforcesSecureChat();
		if (bl && !remoteChatSession.verifiable()) {
			LOGGER.error("Received unverifiable chat session for player '{}', but Secure Chat is enforced", playerInfo.getProfile().getName());
			this.connection.disconnect(INVALID_PACKET);
		} else {
			playerInfo.setChatSession(remoteChatSession);
		}
	}

	private RemoteChatSession validateChatSession(RemoteChatSession.Data data, GameProfile gameProfile) {
		try {
			return data.validate(gameProfile, this.minecraft.getServiceSignatureValidator(), ProfilePublicKey.EXPIRY_GRACE_PERIOD);
		} catch (ProfilePublicKey.ValidationException var4) {
			LOGGER.warn("Failed to validate profile key for player: '{}'", gameProfile.getName(), var4);
			return RemoteChatSession.UNVERIFIED;
		}
	}

	@Override
	public void handleKeepAlive(ClientboundKeepAlivePacket clientboundKeepAlivePacket) {
		this.send(new ServerboundKeepAlivePacket(clientboundKeepAlivePacket.getId()));
	}

	@Override
	public void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket clientboundPlayerAbilitiesPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundPlayerAbilitiesPacket, this, this.minecraft);
		Player player = this.minecraft.player;
		player.getAbilities().flying = clientboundPlayerAbilitiesPacket.isFlying();
		player.getAbilities().instabuild = clientboundPlayerAbilitiesPacket.canInstabuild();
		player.getAbilities().invulnerable = clientboundPlayerAbilitiesPacket.isInvulnerable();
		player.getAbilities().mayfly = clientboundPlayerAbilitiesPacket.canFly();
		player.getAbilities().setFlyingSpeed(clientboundPlayerAbilitiesPacket.getFlyingSpeed());
		player.getAbilities().setWalkingSpeed(clientboundPlayerAbilitiesPacket.getWalkingSpeed());
	}

	@Override
	public void handleSoundEvent(ClientboundSoundPacket clientboundSoundPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSoundPacket, this, this.minecraft);
		this.minecraft
			.level
			.playSeededSound(
				this.minecraft.player,
				clientboundSoundPacket.getX(),
				clientboundSoundPacket.getY(),
				clientboundSoundPacket.getZ(),
				clientboundSoundPacket.getSound(),
				clientboundSoundPacket.getSource(),
				clientboundSoundPacket.getVolume(),
				clientboundSoundPacket.getPitch(),
				clientboundSoundPacket.getSeed()
			);
	}

	@Override
	public void handleSoundEntityEvent(ClientboundSoundEntityPacket clientboundSoundEntityPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSoundEntityPacket, this, this.minecraft);
		Entity entity = this.level.getEntity(clientboundSoundEntityPacket.getId());
		if (entity != null) {
			this.minecraft
				.level
				.playSeededSound(
					this.minecraft.player,
					entity,
					clientboundSoundEntityPacket.getSound(),
					clientboundSoundEntityPacket.getSource(),
					clientboundSoundEntityPacket.getVolume(),
					clientboundSoundEntityPacket.getPitch(),
					clientboundSoundEntityPacket.getSeed()
				);
		}
	}

	@Override
	public void handleCustomSoundEvent(ClientboundCustomSoundPacket clientboundCustomSoundPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundCustomSoundPacket, this, this.minecraft);
		this.minecraft
			.getSoundManager()
			.play(
				new SimpleSoundInstance(
					clientboundCustomSoundPacket.getName(),
					clientboundCustomSoundPacket.getSource(),
					clientboundCustomSoundPacket.getVolume(),
					clientboundCustomSoundPacket.getPitch(),
					RandomSource.create(clientboundCustomSoundPacket.getSeed()),
					false,
					0,
					SoundInstance.Attenuation.LINEAR,
					clientboundCustomSoundPacket.getX(),
					clientboundCustomSoundPacket.getY(),
					clientboundCustomSoundPacket.getZ(),
					false
				)
			);
	}

	@Override
	public void handleResourcePack(ClientboundResourcePackPacket clientboundResourcePackPacket) {
		URL uRL = parseResourcePackUrl(clientboundResourcePackPacket.getUrl());
		if (uRL == null) {
			this.send(ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD);
		} else {
			String string = clientboundResourcePackPacket.getHash();
			boolean bl = clientboundResourcePackPacket.isRequired();
			if (this.serverData != null && this.serverData.getResourcePackStatus() == ServerData.ServerPackStatus.ENABLED) {
				this.send(ServerboundResourcePackPacket.Action.ACCEPTED);
				this.downloadCallback(this.minecraft.getDownloadedPackSource().downloadAndSelectResourcePack(uRL, string, true));
			} else if (this.serverData != null
				&& this.serverData.getResourcePackStatus() != ServerData.ServerPackStatus.PROMPT
				&& (!bl || this.serverData.getResourcePackStatus() != ServerData.ServerPackStatus.DISABLED)) {
				this.send(ServerboundResourcePackPacket.Action.DECLINED);
				if (bl) {
					this.connection.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
				}
			} else {
				this.minecraft
					.execute(
						() -> this.minecraft
								.setScreen(
									new ConfirmScreen(
										bl2 -> {
											this.minecraft.setScreen(null);
											if (bl2) {
												if (this.serverData != null) {
													this.serverData.setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);
												}

												this.send(ServerboundResourcePackPacket.Action.ACCEPTED);
												this.downloadCallback(this.minecraft.getDownloadedPackSource().downloadAndSelectResourcePack(uRL, string, true));
											} else {
												this.send(ServerboundResourcePackPacket.Action.DECLINED);
												if (bl) {
													this.connection.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
												} else if (this.serverData != null) {
													this.serverData.setResourcePackStatus(ServerData.ServerPackStatus.DISABLED);
												}
											}

											if (this.serverData != null) {
												ServerList.saveSingleServer(this.serverData);
											}
										},
										bl ? Component.translatable("multiplayer.requiredTexturePrompt.line1") : Component.translatable("multiplayer.texturePrompt.line1"),
										preparePackPrompt(
											bl
												? Component.translatable("multiplayer.requiredTexturePrompt.line2").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
												: Component.translatable("multiplayer.texturePrompt.line2"),
											clientboundResourcePackPacket.getPrompt()
										),
										bl ? CommonComponents.GUI_PROCEED : CommonComponents.GUI_YES,
										(Component)(bl ? Component.translatable("menu.disconnect") : CommonComponents.GUI_NO)
									)
								)
					);
			}
		}
	}

	private static Component preparePackPrompt(Component component, @Nullable Component component2) {
		return (Component)(component2 == null ? component : Component.translatable("multiplayer.texturePrompt.serverPrompt", component, component2));
	}

	@Nullable
	private static URL parseResourcePackUrl(String string) {
		try {
			URL uRL = new URL(string);
			String string2 = uRL.getProtocol();
			return !"http".equals(string2) && !"https".equals(string2) ? null : uRL;
		} catch (MalformedURLException var3) {
			return null;
		}
	}

	private void downloadCallback(CompletableFuture<?> completableFuture) {
		completableFuture.thenRun(() -> this.send(ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED)).exceptionally(throwable -> {
			this.send(ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD);
			return null;
		});
	}

	private void send(ServerboundResourcePackPacket.Action action) {
		this.connection.send(new ServerboundResourcePackPacket(action));
	}

	@Override
	public void handleBossUpdate(ClientboundBossEventPacket clientboundBossEventPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundBossEventPacket, this, this.minecraft);
		this.minecraft.gui.getBossOverlay().update(clientboundBossEventPacket);
	}

	@Override
	public void handleItemCooldown(ClientboundCooldownPacket clientboundCooldownPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundCooldownPacket, this, this.minecraft);
		if (clientboundCooldownPacket.getDuration() == 0) {
			this.minecraft.player.getCooldowns().removeCooldown(clientboundCooldownPacket.getItem());
		} else {
			this.minecraft.player.getCooldowns().addCooldown(clientboundCooldownPacket.getItem(), clientboundCooldownPacket.getDuration());
		}
	}

	@Override
	public void handleMoveVehicle(ClientboundMoveVehiclePacket clientboundMoveVehiclePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundMoveVehiclePacket, this, this.minecraft);
		Entity entity = this.minecraft.player.getRootVehicle();
		if (entity != this.minecraft.player && entity.isControlledByLocalInstance()) {
			entity.absMoveTo(
				clientboundMoveVehiclePacket.getX(),
				clientboundMoveVehiclePacket.getY(),
				clientboundMoveVehiclePacket.getZ(),
				clientboundMoveVehiclePacket.getYRot(),
				clientboundMoveVehiclePacket.getXRot()
			);
			this.connection.send(new ServerboundMoveVehiclePacket(entity));
		}
	}

	@Override
	public void handleOpenBook(ClientboundOpenBookPacket clientboundOpenBookPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundOpenBookPacket, this, this.minecraft);
		ItemStack itemStack = this.minecraft.player.getItemInHand(clientboundOpenBookPacket.getHand());
		if (itemStack.is(Items.WRITTEN_BOOK)) {
			this.minecraft.setScreen(new BookViewScreen(new BookViewScreen.WrittenBookAccess(itemStack)));
		}
	}

	@Override
	public void handleCustomPayload(ClientboundCustomPayloadPacket clientboundCustomPayloadPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundCustomPayloadPacket, this, this.minecraft);
		ResourceLocation resourceLocation = clientboundCustomPayloadPacket.getIdentifier();
		FriendlyByteBuf friendlyByteBuf = null;

		try {
			friendlyByteBuf = clientboundCustomPayloadPacket.getData();
			if (ClientboundCustomPayloadPacket.BRAND.equals(resourceLocation)) {
				String string = friendlyByteBuf.readUtf();
				this.minecraft.player.setServerBrand(string);
				this.telemetryManager.onServerBrandReceived(string);
			} else if (ClientboundCustomPayloadPacket.DEBUG_PATHFINDING_PACKET.equals(resourceLocation)) {
				int i = friendlyByteBuf.readInt();
				float f = friendlyByteBuf.readFloat();
				Path path = Path.createFromStream(friendlyByteBuf);
				this.minecraft.debugRenderer.pathfindingRenderer.addPath(i, path, f);
			} else if (ClientboundCustomPayloadPacket.DEBUG_NEIGHBORSUPDATE_PACKET.equals(resourceLocation)) {
				long l = friendlyByteBuf.readVarLong();
				BlockPos blockPos = friendlyByteBuf.readBlockPos();
				((NeighborsUpdateRenderer)this.minecraft.debugRenderer.neighborsUpdateRenderer).addUpdate(l, blockPos);
			} else if (ClientboundCustomPayloadPacket.DEBUG_STRUCTURES_PACKET.equals(resourceLocation)) {
				DimensionType dimensionType = this.registryAccess
					.compositeAccess()
					.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY)
					.get(friendlyByteBuf.readResourceLocation());
				BoundingBox boundingBox = new BoundingBox(
					friendlyByteBuf.readInt(),
					friendlyByteBuf.readInt(),
					friendlyByteBuf.readInt(),
					friendlyByteBuf.readInt(),
					friendlyByteBuf.readInt(),
					friendlyByteBuf.readInt()
				);
				int j = friendlyByteBuf.readInt();
				List<BoundingBox> list = Lists.<BoundingBox>newArrayList();
				List<Boolean> list2 = Lists.<Boolean>newArrayList();

				for (int k = 0; k < j; k++) {
					list.add(
						new BoundingBox(
							friendlyByteBuf.readInt(),
							friendlyByteBuf.readInt(),
							friendlyByteBuf.readInt(),
							friendlyByteBuf.readInt(),
							friendlyByteBuf.readInt(),
							friendlyByteBuf.readInt()
						)
					);
					list2.add(friendlyByteBuf.readBoolean());
				}

				this.minecraft.debugRenderer.structureRenderer.addBoundingBox(boundingBox, list, list2, dimensionType);
			} else if (ClientboundCustomPayloadPacket.DEBUG_WORLDGENATTEMPT_PACKET.equals(resourceLocation)) {
				((WorldGenAttemptRenderer)this.minecraft.debugRenderer.worldGenAttemptRenderer)
					.addPos(
						friendlyByteBuf.readBlockPos(),
						friendlyByteBuf.readFloat(),
						friendlyByteBuf.readFloat(),
						friendlyByteBuf.readFloat(),
						friendlyByteBuf.readFloat(),
						friendlyByteBuf.readFloat()
					);
			} else if (ClientboundCustomPayloadPacket.DEBUG_VILLAGE_SECTIONS.equals(resourceLocation)) {
				int i = friendlyByteBuf.readInt();

				for (int m = 0; m < i; m++) {
					this.minecraft.debugRenderer.villageSectionsDebugRenderer.setVillageSection(friendlyByteBuf.readSectionPos());
				}

				int m = friendlyByteBuf.readInt();

				for (int j = 0; j < m; j++) {
					this.minecraft.debugRenderer.villageSectionsDebugRenderer.setNotVillageSection(friendlyByteBuf.readSectionPos());
				}
			} else if (ClientboundCustomPayloadPacket.DEBUG_POI_ADDED_PACKET.equals(resourceLocation)) {
				BlockPos blockPos2 = friendlyByteBuf.readBlockPos();
				String string2 = friendlyByteBuf.readUtf();
				int j = friendlyByteBuf.readInt();
				BrainDebugRenderer.PoiInfo poiInfo = new BrainDebugRenderer.PoiInfo(blockPos2, string2, j);
				this.minecraft.debugRenderer.brainDebugRenderer.addPoi(poiInfo);
			} else if (ClientboundCustomPayloadPacket.DEBUG_POI_REMOVED_PACKET.equals(resourceLocation)) {
				BlockPos blockPos2 = friendlyByteBuf.readBlockPos();
				this.minecraft.debugRenderer.brainDebugRenderer.removePoi(blockPos2);
			} else if (ClientboundCustomPayloadPacket.DEBUG_POI_TICKET_COUNT_PACKET.equals(resourceLocation)) {
				BlockPos blockPos2 = friendlyByteBuf.readBlockPos();
				int m = friendlyByteBuf.readInt();
				this.minecraft.debugRenderer.brainDebugRenderer.setFreeTicketCount(blockPos2, m);
			} else if (ClientboundCustomPayloadPacket.DEBUG_GOAL_SELECTOR.equals(resourceLocation)) {
				BlockPos blockPos2 = friendlyByteBuf.readBlockPos();
				int m = friendlyByteBuf.readInt();
				int j = friendlyByteBuf.readInt();
				List<GoalSelectorDebugRenderer.DebugGoal> list = Lists.<GoalSelectorDebugRenderer.DebugGoal>newArrayList();

				for (int n = 0; n < j; n++) {
					int k = friendlyByteBuf.readInt();
					boolean bl = friendlyByteBuf.readBoolean();
					String string3 = friendlyByteBuf.readUtf(255);
					list.add(new GoalSelectorDebugRenderer.DebugGoal(blockPos2, k, string3, bl));
				}

				this.minecraft.debugRenderer.goalSelectorRenderer.addGoalSelector(m, list);
			} else if (ClientboundCustomPayloadPacket.DEBUG_RAIDS.equals(resourceLocation)) {
				int i = friendlyByteBuf.readInt();
				Collection<BlockPos> collection = Lists.<BlockPos>newArrayList();

				for (int j = 0; j < i; j++) {
					collection.add(friendlyByteBuf.readBlockPos());
				}

				this.minecraft.debugRenderer.raidDebugRenderer.setRaidCenters(collection);
			} else if (ClientboundCustomPayloadPacket.DEBUG_BRAIN.equals(resourceLocation)) {
				double d = friendlyByteBuf.readDouble();
				double e = friendlyByteBuf.readDouble();
				double g = friendlyByteBuf.readDouble();
				Position position = new PositionImpl(d, e, g);
				UUID uUID = friendlyByteBuf.readUUID();
				int o = friendlyByteBuf.readInt();
				String string4 = friendlyByteBuf.readUtf();
				String string5 = friendlyByteBuf.readUtf();
				int p = friendlyByteBuf.readInt();
				float h = friendlyByteBuf.readFloat();
				float q = friendlyByteBuf.readFloat();
				String string6 = friendlyByteBuf.readUtf();
				Path path2 = friendlyByteBuf.readNullable(Path::createFromStream);
				boolean bl2 = friendlyByteBuf.readBoolean();
				int r = friendlyByteBuf.readInt();
				BrainDebugRenderer.BrainDump brainDump = new BrainDebugRenderer.BrainDump(uUID, o, string4, string5, p, h, q, position, string6, path2, bl2, r);
				int s = friendlyByteBuf.readVarInt();

				for (int t = 0; t < s; t++) {
					String string7 = friendlyByteBuf.readUtf();
					brainDump.activities.add(string7);
				}

				int t = friendlyByteBuf.readVarInt();

				for (int u = 0; u < t; u++) {
					String string8 = friendlyByteBuf.readUtf();
					brainDump.behaviors.add(string8);
				}

				int u = friendlyByteBuf.readVarInt();

				for (int v = 0; v < u; v++) {
					String string9 = friendlyByteBuf.readUtf();
					brainDump.memories.add(string9);
				}

				int v = friendlyByteBuf.readVarInt();

				for (int w = 0; w < v; w++) {
					BlockPos blockPos3 = friendlyByteBuf.readBlockPos();
					brainDump.pois.add(blockPos3);
				}

				int w = friendlyByteBuf.readVarInt();

				for (int x = 0; x < w; x++) {
					BlockPos blockPos4 = friendlyByteBuf.readBlockPos();
					brainDump.potentialPois.add(blockPos4);
				}

				int x = friendlyByteBuf.readVarInt();

				for (int y = 0; y < x; y++) {
					String string10 = friendlyByteBuf.readUtf();
					brainDump.gossips.add(string10);
				}

				this.minecraft.debugRenderer.brainDebugRenderer.addOrUpdateBrainDump(brainDump);
			} else if (ClientboundCustomPayloadPacket.DEBUG_BEE.equals(resourceLocation)) {
				double d = friendlyByteBuf.readDouble();
				double e = friendlyByteBuf.readDouble();
				double g = friendlyByteBuf.readDouble();
				Position position = new PositionImpl(d, e, g);
				UUID uUID = friendlyByteBuf.readUUID();
				int o = friendlyByteBuf.readInt();
				BlockPos blockPos5 = friendlyByteBuf.readNullable(FriendlyByteBuf::readBlockPos);
				BlockPos blockPos6 = friendlyByteBuf.readNullable(FriendlyByteBuf::readBlockPos);
				int p = friendlyByteBuf.readInt();
				Path path3 = friendlyByteBuf.readNullable(Path::createFromStream);
				BeeDebugRenderer.BeeInfo beeInfo = new BeeDebugRenderer.BeeInfo(uUID, o, position, path3, blockPos5, blockPos6, p);
				int z = friendlyByteBuf.readVarInt();

				for (int aa = 0; aa < z; aa++) {
					String string11 = friendlyByteBuf.readUtf();
					beeInfo.goals.add(string11);
				}

				int aa = friendlyByteBuf.readVarInt();

				for (int ab = 0; ab < aa; ab++) {
					BlockPos blockPos7 = friendlyByteBuf.readBlockPos();
					beeInfo.blacklistedHives.add(blockPos7);
				}

				this.minecraft.debugRenderer.beeDebugRenderer.addOrUpdateBeeInfo(beeInfo);
			} else if (ClientboundCustomPayloadPacket.DEBUG_HIVE.equals(resourceLocation)) {
				BlockPos blockPos2 = friendlyByteBuf.readBlockPos();
				String string2 = friendlyByteBuf.readUtf();
				int j = friendlyByteBuf.readInt();
				int ac = friendlyByteBuf.readInt();
				boolean bl3 = friendlyByteBuf.readBoolean();
				BeeDebugRenderer.HiveInfo hiveInfo = new BeeDebugRenderer.HiveInfo(blockPos2, string2, j, ac, bl3, this.level.getGameTime());
				this.minecraft.debugRenderer.beeDebugRenderer.addOrUpdateHiveInfo(hiveInfo);
			} else if (ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_CLEAR.equals(resourceLocation)) {
				this.minecraft.debugRenderer.gameTestDebugRenderer.clear();
			} else if (ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_ADD_MARKER.equals(resourceLocation)) {
				BlockPos blockPos2 = friendlyByteBuf.readBlockPos();
				int m = friendlyByteBuf.readInt();
				String string12 = friendlyByteBuf.readUtf();
				int ac = friendlyByteBuf.readInt();
				this.minecraft.debugRenderer.gameTestDebugRenderer.addMarker(blockPos2, m, string12, ac);
			} else if (ClientboundCustomPayloadPacket.DEBUG_GAME_EVENT.equals(resourceLocation)) {
				GameEvent gameEvent = Registry.GAME_EVENT.get(new ResourceLocation(friendlyByteBuf.readUtf()));
				Vec3 vec3 = new Vec3(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble());
				this.minecraft.debugRenderer.gameEventListenerRenderer.trackGameEvent(gameEvent, vec3);
			} else if (ClientboundCustomPayloadPacket.DEBUG_GAME_EVENT_LISTENER.equals(resourceLocation)) {
				ResourceLocation resourceLocation2 = friendlyByteBuf.readResourceLocation();
				PositionSource positionSource = ((PositionSourceType)Registry.POSITION_SOURCE_TYPE
						.getOptional(resourceLocation2)
						.orElseThrow(() -> new IllegalArgumentException("Unknown position source type " + resourceLocation2)))
					.read(friendlyByteBuf);
				int j = friendlyByteBuf.readVarInt();
				this.minecraft.debugRenderer.gameEventListenerRenderer.trackListener(positionSource, j);
			} else {
				LOGGER.warn("Unknown custom packed identifier: {}", resourceLocation);
			}
		} finally {
			if (friendlyByteBuf != null) {
				friendlyByteBuf.release();
			}
		}
	}

	@Override
	public void handleAddObjective(ClientboundSetObjectivePacket clientboundSetObjectivePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetObjectivePacket, this, this.minecraft);
		Scoreboard scoreboard = this.level.getScoreboard();
		String string = clientboundSetObjectivePacket.getObjectiveName();
		if (clientboundSetObjectivePacket.getMethod() == 0) {
			scoreboard.addObjective(string, ObjectiveCriteria.DUMMY, clientboundSetObjectivePacket.getDisplayName(), clientboundSetObjectivePacket.getRenderType());
		} else if (scoreboard.hasObjective(string)) {
			Objective objective = scoreboard.getObjective(string);
			if (clientboundSetObjectivePacket.getMethod() == 1) {
				scoreboard.removeObjective(objective);
			} else if (clientboundSetObjectivePacket.getMethod() == 2) {
				objective.setRenderType(clientboundSetObjectivePacket.getRenderType());
				objective.setDisplayName(clientboundSetObjectivePacket.getDisplayName());
			}
		}
	}

	@Override
	public void handleSetScore(ClientboundSetScorePacket clientboundSetScorePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetScorePacket, this, this.minecraft);
		Scoreboard scoreboard = this.level.getScoreboard();
		String string = clientboundSetScorePacket.getObjectiveName();
		switch (clientboundSetScorePacket.getMethod()) {
			case CHANGE:
				Objective objective = scoreboard.getOrCreateObjective(string);
				Score score = scoreboard.getOrCreatePlayerScore(clientboundSetScorePacket.getOwner(), objective);
				score.setScore(clientboundSetScorePacket.getScore());
				break;
			case REMOVE:
				scoreboard.resetPlayerScore(clientboundSetScorePacket.getOwner(), scoreboard.getObjective(string));
		}
	}

	@Override
	public void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket clientboundSetDisplayObjectivePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetDisplayObjectivePacket, this, this.minecraft);
		Scoreboard scoreboard = this.level.getScoreboard();
		String string = clientboundSetDisplayObjectivePacket.getObjectiveName();
		Objective objective = string == null ? null : scoreboard.getOrCreateObjective(string);
		scoreboard.setDisplayObjective(clientboundSetDisplayObjectivePacket.getSlot(), objective);
	}

	@Override
	public void handleSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket clientboundSetPlayerTeamPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetPlayerTeamPacket, this, this.minecraft);
		Scoreboard scoreboard = this.level.getScoreboard();
		ClientboundSetPlayerTeamPacket.Action action = clientboundSetPlayerTeamPacket.getTeamAction();
		PlayerTeam playerTeam;
		if (action == ClientboundSetPlayerTeamPacket.Action.ADD) {
			playerTeam = scoreboard.addPlayerTeam(clientboundSetPlayerTeamPacket.getName());
		} else {
			playerTeam = scoreboard.getPlayerTeam(clientboundSetPlayerTeamPacket.getName());
			if (playerTeam == null) {
				LOGGER.warn(
					"Received packet for unknown team {}: team action: {}, player action: {}",
					clientboundSetPlayerTeamPacket.getName(),
					clientboundSetPlayerTeamPacket.getTeamAction(),
					clientboundSetPlayerTeamPacket.getPlayerAction()
				);
				return;
			}
		}

		Optional<ClientboundSetPlayerTeamPacket.Parameters> optional = clientboundSetPlayerTeamPacket.getParameters();
		optional.ifPresent(parameters -> {
			playerTeam.setDisplayName(parameters.getDisplayName());
			playerTeam.setColor(parameters.getColor());
			playerTeam.unpackOptions(parameters.getOptions());
			Team.Visibility visibility = Team.Visibility.byName(parameters.getNametagVisibility());
			if (visibility != null) {
				playerTeam.setNameTagVisibility(visibility);
			}

			Team.CollisionRule collisionRule = Team.CollisionRule.byName(parameters.getCollisionRule());
			if (collisionRule != null) {
				playerTeam.setCollisionRule(collisionRule);
			}

			playerTeam.setPlayerPrefix(parameters.getPlayerPrefix());
			playerTeam.setPlayerSuffix(parameters.getPlayerSuffix());
		});
		ClientboundSetPlayerTeamPacket.Action action2 = clientboundSetPlayerTeamPacket.getPlayerAction();
		if (action2 == ClientboundSetPlayerTeamPacket.Action.ADD) {
			for (String string : clientboundSetPlayerTeamPacket.getPlayers()) {
				scoreboard.addPlayerToTeam(string, playerTeam);
			}
		} else if (action2 == ClientboundSetPlayerTeamPacket.Action.REMOVE) {
			for (String string : clientboundSetPlayerTeamPacket.getPlayers()) {
				scoreboard.removePlayerFromTeam(string, playerTeam);
			}
		}

		if (action == ClientboundSetPlayerTeamPacket.Action.REMOVE) {
			scoreboard.removePlayerTeam(playerTeam);
		}
	}

	@Override
	public void handleParticleEvent(ClientboundLevelParticlesPacket clientboundLevelParticlesPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundLevelParticlesPacket, this, this.minecraft);
		if (clientboundLevelParticlesPacket.getCount() == 0) {
			double d = (double)(clientboundLevelParticlesPacket.getMaxSpeed() * clientboundLevelParticlesPacket.getXDist());
			double e = (double)(clientboundLevelParticlesPacket.getMaxSpeed() * clientboundLevelParticlesPacket.getYDist());
			double f = (double)(clientboundLevelParticlesPacket.getMaxSpeed() * clientboundLevelParticlesPacket.getZDist());

			try {
				this.level
					.addParticle(
						clientboundLevelParticlesPacket.getParticle(),
						clientboundLevelParticlesPacket.isOverrideLimiter(),
						clientboundLevelParticlesPacket.getX(),
						clientboundLevelParticlesPacket.getY(),
						clientboundLevelParticlesPacket.getZ(),
						d,
						e,
						f
					);
			} catch (Throwable var17) {
				LOGGER.warn("Could not spawn particle effect {}", clientboundLevelParticlesPacket.getParticle());
			}
		} else {
			for (int i = 0; i < clientboundLevelParticlesPacket.getCount(); i++) {
				double g = this.random.nextGaussian() * (double)clientboundLevelParticlesPacket.getXDist();
				double h = this.random.nextGaussian() * (double)clientboundLevelParticlesPacket.getYDist();
				double j = this.random.nextGaussian() * (double)clientboundLevelParticlesPacket.getZDist();
				double k = this.random.nextGaussian() * (double)clientboundLevelParticlesPacket.getMaxSpeed();
				double l = this.random.nextGaussian() * (double)clientboundLevelParticlesPacket.getMaxSpeed();
				double m = this.random.nextGaussian() * (double)clientboundLevelParticlesPacket.getMaxSpeed();

				try {
					this.level
						.addParticle(
							clientboundLevelParticlesPacket.getParticle(),
							clientboundLevelParticlesPacket.isOverrideLimiter(),
							clientboundLevelParticlesPacket.getX() + g,
							clientboundLevelParticlesPacket.getY() + h,
							clientboundLevelParticlesPacket.getZ() + j,
							k,
							l,
							m
						);
				} catch (Throwable var16) {
					LOGGER.warn("Could not spawn particle effect {}", clientboundLevelParticlesPacket.getParticle());
					return;
				}
			}
		}
	}

	@Override
	public void handlePing(ClientboundPingPacket clientboundPingPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundPingPacket, this, this.minecraft);
		this.send(new ServerboundPongPacket(clientboundPingPacket.getId()));
	}

	@Override
	public void handleUpdateAttributes(ClientboundUpdateAttributesPacket clientboundUpdateAttributesPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundUpdateAttributesPacket, this, this.minecraft);
		Entity entity = this.level.getEntity(clientboundUpdateAttributesPacket.getEntityId());
		if (entity != null) {
			if (!(entity instanceof LivingEntity)) {
				throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + entity + ")");
			} else {
				AttributeMap attributeMap = ((LivingEntity)entity).getAttributes();

				for (ClientboundUpdateAttributesPacket.AttributeSnapshot attributeSnapshot : clientboundUpdateAttributesPacket.getValues()) {
					AttributeInstance attributeInstance = attributeMap.getInstance(attributeSnapshot.getAttribute());
					if (attributeInstance == null) {
						LOGGER.warn("Entity {} does not have attribute {}", entity, Registry.ATTRIBUTE.getKey(attributeSnapshot.getAttribute()));
					} else {
						attributeInstance.setBaseValue(attributeSnapshot.getBase());
						attributeInstance.removeModifiers();

						for (AttributeModifier attributeModifier : attributeSnapshot.getModifiers()) {
							attributeInstance.addTransientModifier(attributeModifier);
						}
					}
				}
			}
		}
	}

	@Override
	public void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket clientboundPlaceGhostRecipePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundPlaceGhostRecipePacket, this, this.minecraft);
		AbstractContainerMenu abstractContainerMenu = this.minecraft.player.containerMenu;
		if (abstractContainerMenu.containerId == clientboundPlaceGhostRecipePacket.getContainerId()) {
			this.recipeManager.byKey(clientboundPlaceGhostRecipePacket.getRecipe()).ifPresent(recipe -> {
				if (this.minecraft.screen instanceof RecipeUpdateListener) {
					RecipeBookComponent recipeBookComponent = ((RecipeUpdateListener)this.minecraft.screen).getRecipeBookComponent();
					recipeBookComponent.setupGhostRecipe(recipe, abstractContainerMenu.slots);
				}
			});
		}
	}

	@Override
	public void handleLightUpdatePacket(ClientboundLightUpdatePacket clientboundLightUpdatePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundLightUpdatePacket, this, this.minecraft);
		int i = clientboundLightUpdatePacket.getX();
		int j = clientboundLightUpdatePacket.getZ();
		ClientboundLightUpdatePacketData clientboundLightUpdatePacketData = clientboundLightUpdatePacket.getLightData();
		this.level.queueLightUpdate(() -> this.applyLightData(i, j, clientboundLightUpdatePacketData));
	}

	private void applyLightData(int i, int j, ClientboundLightUpdatePacketData clientboundLightUpdatePacketData) {
		LevelLightEngine levelLightEngine = this.level.getChunkSource().getLightEngine();
		BitSet bitSet = clientboundLightUpdatePacketData.getSkyYMask();
		BitSet bitSet2 = clientboundLightUpdatePacketData.getEmptySkyYMask();
		Iterator<byte[]> iterator = clientboundLightUpdatePacketData.getSkyUpdates().iterator();
		this.readSectionList(i, j, levelLightEngine, LightLayer.SKY, bitSet, bitSet2, iterator, clientboundLightUpdatePacketData.getTrustEdges());
		BitSet bitSet3 = clientboundLightUpdatePacketData.getBlockYMask();
		BitSet bitSet4 = clientboundLightUpdatePacketData.getEmptyBlockYMask();
		Iterator<byte[]> iterator2 = clientboundLightUpdatePacketData.getBlockUpdates().iterator();
		this.readSectionList(i, j, levelLightEngine, LightLayer.BLOCK, bitSet3, bitSet4, iterator2, clientboundLightUpdatePacketData.getTrustEdges());
		this.level.setLightReady(i, j);
	}

	@Override
	public void handleMerchantOffers(ClientboundMerchantOffersPacket clientboundMerchantOffersPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundMerchantOffersPacket, this, this.minecraft);
		AbstractContainerMenu abstractContainerMenu = this.minecraft.player.containerMenu;
		if (clientboundMerchantOffersPacket.getContainerId() == abstractContainerMenu.containerId && abstractContainerMenu instanceof MerchantMenu merchantMenu) {
			merchantMenu.setOffers(new MerchantOffers(clientboundMerchantOffersPacket.getOffers().createTag()));
			merchantMenu.setXp(clientboundMerchantOffersPacket.getVillagerXp());
			merchantMenu.setMerchantLevel(clientboundMerchantOffersPacket.getVillagerLevel());
			merchantMenu.setShowProgressBar(clientboundMerchantOffersPacket.showProgress());
			merchantMenu.setCanRestock(clientboundMerchantOffersPacket.canRestock());
		}
	}

	@Override
	public void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket clientboundSetChunkCacheRadiusPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetChunkCacheRadiusPacket, this, this.minecraft);
		this.serverChunkRadius = clientboundSetChunkCacheRadiusPacket.getRadius();
		this.minecraft.options.setServerRenderDistance(this.serverChunkRadius);
		this.level.getChunkSource().updateViewRadius(clientboundSetChunkCacheRadiusPacket.getRadius());
	}

	@Override
	public void handleSetSimulationDistance(ClientboundSetSimulationDistancePacket clientboundSetSimulationDistancePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetSimulationDistancePacket, this, this.minecraft);
		this.serverSimulationDistance = clientboundSetSimulationDistancePacket.simulationDistance();
		this.level.setServerSimulationDistance(this.serverSimulationDistance);
	}

	@Override
	public void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket clientboundSetChunkCacheCenterPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetChunkCacheCenterPacket, this, this.minecraft);
		this.level.getChunkSource().updateViewCenter(clientboundSetChunkCacheCenterPacket.getX(), clientboundSetChunkCacheCenterPacket.getZ());
	}

	@Override
	public void handleBlockChangedAck(ClientboundBlockChangedAckPacket clientboundBlockChangedAckPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundBlockChangedAckPacket, this, this.minecraft);
		this.level.handleBlockChangedAck(clientboundBlockChangedAckPacket.sequence());
	}

	private void readSectionList(
		int i, int j, LevelLightEngine levelLightEngine, LightLayer lightLayer, BitSet bitSet, BitSet bitSet2, Iterator<byte[]> iterator, boolean bl
	) {
		for (int k = 0; k < levelLightEngine.getLightSectionCount(); k++) {
			int l = levelLightEngine.getMinLightSection() + k;
			boolean bl2 = bitSet.get(k);
			boolean bl3 = bitSet2.get(k);
			if (bl2 || bl3) {
				levelLightEngine.queueSectionData(lightLayer, SectionPos.of(i, l, j), bl2 ? new DataLayer((byte[])((byte[])iterator.next()).clone()) : new DataLayer(), bl);
				this.level.setSectionDirtyWithNeighbors(i, l, j);
			}
		}
	}

	@Override
	public Connection getConnection() {
		return this.connection;
	}

	public Collection<PlayerInfo> getListedOnlinePlayers() {
		return this.listedPlayers;
	}

	public Collection<PlayerInfo> getOnlinePlayers() {
		return this.playerInfoMap.values();
	}

	public Collection<UUID> getOnlinePlayerIds() {
		return this.playerInfoMap.keySet();
	}

	@Nullable
	public PlayerInfo getPlayerInfo(UUID uUID) {
		return (PlayerInfo)this.playerInfoMap.get(uUID);
	}

	@Nullable
	public PlayerInfo getPlayerInfo(String string) {
		for (PlayerInfo playerInfo : this.playerInfoMap.values()) {
			if (playerInfo.getProfile().getName().equals(string)) {
				return playerInfo;
			}
		}

		return null;
	}

	public GameProfile getLocalGameProfile() {
		return this.localGameProfile;
	}

	public ClientAdvancements getAdvancements() {
		return this.advancements;
	}

	public CommandDispatcher<SharedSuggestionProvider> getCommands() {
		return this.commands;
	}

	public ClientLevel getLevel() {
		return this.level;
	}

	public DebugQueryHandler getDebugQueryHandler() {
		return this.debugQueryHandler;
	}

	public UUID getId() {
		return this.id;
	}

	public Set<ResourceKey<Level>> levels() {
		return this.levels;
	}

	public RegistryAccess registryAccess() {
		return this.registryAccess.compositeAccess();
	}

	public void markMessageAsProcessed(PlayerChatMessage playerChatMessage, boolean bl) {
		MessageSignature messageSignature = playerChatMessage.signature();
		if (messageSignature != null && this.lastSeenMessages.addPending(messageSignature, bl) && this.lastSeenMessages.offset() > 64) {
			this.sendChatAcknowledgement();
		}
	}

	private void sendChatAcknowledgement() {
		int i = this.lastSeenMessages.getAndClearOffset();
		if (i > 0) {
			this.send(new ServerboundChatAckPacket(i));
		}
	}

	public void sendChat(String string) {
		Instant instant = Instant.now();
		long l = Crypt.SaltSupplier.getLong();
		LastSeenMessagesTracker.Update update = this.lastSeenMessages.generateAndApplyUpdate();
		MessageSignature messageSignature = this.signedMessageEncoder.pack(new SignedMessageBody(string, instant, l, update.lastSeen()));
		this.send(new ServerboundChatPacket(string, instant, l, messageSignature, update.update()));
	}

	public void sendCommand(String string) {
		Instant instant = Instant.now();
		long l = Crypt.SaltSupplier.getLong();
		LastSeenMessagesTracker.Update update = this.lastSeenMessages.generateAndApplyUpdate();
		ArgumentSignatures argumentSignatures = ArgumentSignatures.signCommand(SignableCommand.of(this.parseCommand(string)), stringx -> {
			SignedMessageBody signedMessageBody = new SignedMessageBody(stringx, instant, l, update.lastSeen());
			return this.signedMessageEncoder.pack(signedMessageBody);
		});
		this.send(new ServerboundChatCommandPacket(string, instant, l, argumentSignatures, update.update()));
	}

	public boolean sendUnsignedCommand(String string) {
		if (SignableCommand.of(this.parseCommand(string)).arguments().isEmpty()) {
			LastSeenMessagesTracker.Update update = this.lastSeenMessages.generateAndApplyUpdate();
			this.send(new ServerboundChatCommandPacket(string, Instant.now(), 0L, ArgumentSignatures.EMPTY, update.update()));
			return true;
		} else {
			return false;
		}
	}

	private ParseResults<SharedSuggestionProvider> parseCommand(String string) {
		return this.commands.parse(string, this.suggestionsProvider);
	}

	@Nullable
	public ServerData getServerData() {
		return this.serverData;
	}

	public FeatureFlagSet enabledFeatures() {
		return this.enabledFeatures;
	}

	public boolean isFeatureEnabled(FeatureFlagSet featureFlagSet) {
		return featureFlagSet.isSubsetOf(this.enabledFeatures());
	}
}

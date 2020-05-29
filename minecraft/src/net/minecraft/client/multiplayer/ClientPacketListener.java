package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.DebugQueryHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.gui.components.toasts.RecipeToast;
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
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
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
import net.minecraft.client.searchtree.MutableSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.PositionImpl;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.network.protocol.game.ClientboundAddGlobalEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddMobPacket;
import net.minecraft.network.protocol.game.ClientboundAddPaintingPacket;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.network.protocol.game.ClientboundBlockBreakAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerAckPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquippedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundContainerAckPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagManager;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.global.LightningBolt;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.entity.vehicle.MinecartHopper;
import net.minecraft.world.entity.vehicle.MinecartSpawner;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.CreativeModeTab;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientPacketListener implements ClientGamePacketListener {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Connection connection;
	private final GameProfile localGameProfile;
	private final Screen callbackScreen;
	private Minecraft minecraft;
	private ClientLevel level;
	private ClientLevel.ClientLevelData levelData;
	private boolean started;
	private final Map<UUID, PlayerInfo> playerInfoMap = Maps.<UUID, PlayerInfo>newHashMap();
	private final ClientAdvancements advancements;
	private final ClientSuggestionProvider suggestionsProvider;
	private TagManager tags = new TagManager();
	private final DebugQueryHandler debugQueryHandler = new DebugQueryHandler(this);
	private int serverChunkRadius = 3;
	private final Random random = new Random();
	private CommandDispatcher<SharedSuggestionProvider> commands = new CommandDispatcher<>();
	private final RecipeManager recipeManager = new RecipeManager();
	private final UUID id = UUID.randomUUID();
	private Set<ResourceKey<Level>> levels;
	private RegistryAccess registryAccess = RegistryAccess.builtin();

	public ClientPacketListener(Minecraft minecraft, Screen screen, Connection connection, GameProfile gameProfile) {
		this.minecraft = minecraft;
		this.callbackScreen = screen;
		this.connection = connection;
		this.localGameProfile = gameProfile;
		this.advancements = new ClientAdvancements(minecraft);
		this.suggestionsProvider = new ClientSuggestionProvider(this, minecraft);
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
		if (!this.connection.isMemoryConnection()) {
			BlockTags.resetToEmpty();
			ItemTags.resetToEmpty();
			FluidTags.resetToEmpty();
			EntityTypeTags.resetToEmpty();
		}

		ArrayList<ResourceKey<Level>> arrayList = Lists.newArrayList(clientboundLoginPacket.levels());
		Collections.shuffle(arrayList);
		this.levels = Sets.<ResourceKey<Level>>newLinkedHashSet(arrayList);
		this.registryAccess = clientboundLoginPacket.registryAccess();
		ResourceKey<DimensionType> resourceKey = clientboundLoginPacket.getDimensionType();
		ResourceKey<Level> resourceKey2 = clientboundLoginPacket.getDimension();
		DimensionType dimensionType = this.registryAccess.dimensionTypes().get(resourceKey);
		this.serverChunkRadius = clientboundLoginPacket.getChunkRadius();
		boolean bl = clientboundLoginPacket.isDebug();
		boolean bl2 = clientboundLoginPacket.isFlat();
		ClientLevel.ClientLevelData clientLevelData = new ClientLevel.ClientLevelData(Difficulty.NORMAL, clientboundLoginPacket.isHardcore(), bl2);
		this.levelData = clientLevelData;
		this.level = new ClientLevel(
			this,
			clientLevelData,
			resourceKey2,
			resourceKey,
			dimensionType,
			this.serverChunkRadius,
			this.minecraft::getProfiler,
			this.minecraft.levelRenderer,
			bl,
			clientboundLoginPacket.getSeed()
		);
		this.minecraft.setLevel(this.level);
		if (this.minecraft.player == null) {
			this.minecraft.player = this.minecraft.gameMode.createPlayer(this.level, new StatsCounter(), new ClientRecipeBook(this.level.getRecipeManager()));
			this.minecraft.player.yRot = -180.0F;
			if (this.minecraft.getSingleplayerServer() != null) {
				this.minecraft.getSingleplayerServer().setUUID(this.minecraft.player.getUUID());
			}
		}

		this.minecraft.debugRenderer.clear();
		this.minecraft.player.resetPos();
		int i = clientboundLoginPacket.getPlayerId();
		this.level.addPlayer(i, this.minecraft.player);
		this.minecraft.player.input = new KeyboardInput(this.minecraft.options);
		this.minecraft.gameMode.adjustPlayer(this.minecraft.player);
		this.minecraft.cameraEntity = this.minecraft.player;
		this.minecraft.setScreen(new ReceivingLevelScreen());
		this.minecraft.player.setId(i);
		this.minecraft.player.setReducedDebugInfo(clientboundLoginPacket.isReducedDebugInfo());
		this.minecraft.player.setShowDeathScreen(clientboundLoginPacket.shouldShowDeathScreen());
		this.minecraft.gameMode.setLocalMode(clientboundLoginPacket.getGameType());
		this.minecraft.options.broadcastOptions();
		this.connection
			.send(
				new ServerboundCustomPayloadPacket(
					ServerboundCustomPayloadPacket.BRAND, new FriendlyByteBuf(Unpooled.buffer()).writeUtf(ClientBrandRetriever.getClientModName())
				)
			);
		this.minecraft.getGame().onStartGameSession();
	}

	@Override
	public void handleAddEntity(ClientboundAddEntityPacket clientboundAddEntityPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundAddEntityPacket, this, this.minecraft);
		double d = clientboundAddEntityPacket.getX();
		double e = clientboundAddEntityPacket.getY();
		double f = clientboundAddEntityPacket.getZ();
		EntityType<?> entityType = clientboundAddEntityPacket.getType();
		Entity entity;
		if (entityType == EntityType.CHEST_MINECART) {
			entity = new MinecartChest(this.level, d, e, f);
		} else if (entityType == EntityType.FURNACE_MINECART) {
			entity = new MinecartFurnace(this.level, d, e, f);
		} else if (entityType == EntityType.TNT_MINECART) {
			entity = new MinecartTNT(this.level, d, e, f);
		} else if (entityType == EntityType.SPAWNER_MINECART) {
			entity = new MinecartSpawner(this.level, d, e, f);
		} else if (entityType == EntityType.HOPPER_MINECART) {
			entity = new MinecartHopper(this.level, d, e, f);
		} else if (entityType == EntityType.COMMAND_BLOCK_MINECART) {
			entity = new MinecartCommandBlock(this.level, d, e, f);
		} else if (entityType == EntityType.MINECART) {
			entity = new Minecart(this.level, d, e, f);
		} else if (entityType == EntityType.FISHING_BOBBER) {
			Entity entity2 = this.level.getEntity(clientboundAddEntityPacket.getData());
			if (entity2 instanceof Player) {
				entity = new FishingHook(this.level, (Player)entity2, d, e, f);
			} else {
				entity = null;
			}
		} else if (entityType == EntityType.ARROW) {
			entity = new Arrow(this.level, d, e, f);
			Entity entity2 = this.level.getEntity(clientboundAddEntityPacket.getData());
			if (entity2 != null) {
				((AbstractArrow)entity).setOwner(entity2);
			}
		} else if (entityType == EntityType.SPECTRAL_ARROW) {
			entity = new SpectralArrow(this.level, d, e, f);
			Entity entity2 = this.level.getEntity(clientboundAddEntityPacket.getData());
			if (entity2 != null) {
				((AbstractArrow)entity).setOwner(entity2);
			}
		} else if (entityType == EntityType.TRIDENT) {
			entity = new ThrownTrident(this.level, d, e, f);
			Entity entity2 = this.level.getEntity(clientboundAddEntityPacket.getData());
			if (entity2 != null) {
				((AbstractArrow)entity).setOwner(entity2);
			}
		} else if (entityType == EntityType.SNOWBALL) {
			entity = new Snowball(this.level, d, e, f);
		} else if (entityType == EntityType.LLAMA_SPIT) {
			entity = new LlamaSpit(this.level, d, e, f, clientboundAddEntityPacket.getXa(), clientboundAddEntityPacket.getYa(), clientboundAddEntityPacket.getZa());
		} else if (entityType == EntityType.ITEM_FRAME) {
			entity = new ItemFrame(this.level, new BlockPos(d, e, f), Direction.from3DDataValue(clientboundAddEntityPacket.getData()));
		} else if (entityType == EntityType.LEASH_KNOT) {
			entity = new LeashFenceKnotEntity(this.level, new BlockPos(d, e, f));
		} else if (entityType == EntityType.ENDER_PEARL) {
			entity = new ThrownEnderpearl(this.level, d, e, f);
		} else if (entityType == EntityType.EYE_OF_ENDER) {
			entity = new EyeOfEnder(this.level, d, e, f);
		} else if (entityType == EntityType.FIREWORK_ROCKET) {
			entity = new FireworkRocketEntity(this.level, d, e, f, ItemStack.EMPTY);
		} else if (entityType == EntityType.FIREBALL) {
			entity = new LargeFireball(this.level, d, e, f, clientboundAddEntityPacket.getXa(), clientboundAddEntityPacket.getYa(), clientboundAddEntityPacket.getZa());
		} else if (entityType == EntityType.DRAGON_FIREBALL) {
			entity = new DragonFireball(this.level, d, e, f, clientboundAddEntityPacket.getXa(), clientboundAddEntityPacket.getYa(), clientboundAddEntityPacket.getZa());
		} else if (entityType == EntityType.SMALL_FIREBALL) {
			entity = new SmallFireball(this.level, d, e, f, clientboundAddEntityPacket.getXa(), clientboundAddEntityPacket.getYa(), clientboundAddEntityPacket.getZa());
		} else if (entityType == EntityType.WITHER_SKULL) {
			entity = new WitherSkull(this.level, d, e, f, clientboundAddEntityPacket.getXa(), clientboundAddEntityPacket.getYa(), clientboundAddEntityPacket.getZa());
		} else if (entityType == EntityType.SHULKER_BULLET) {
			entity = new ShulkerBullet(this.level, d, e, f, clientboundAddEntityPacket.getXa(), clientboundAddEntityPacket.getYa(), clientboundAddEntityPacket.getZa());
		} else if (entityType == EntityType.EGG) {
			entity = new ThrownEgg(this.level, d, e, f);
		} else if (entityType == EntityType.EVOKER_FANGS) {
			entity = new EvokerFangs(this.level, d, e, f, 0.0F, 0, null);
		} else if (entityType == EntityType.POTION) {
			entity = new ThrownPotion(this.level, d, e, f);
		} else if (entityType == EntityType.EXPERIENCE_BOTTLE) {
			entity = new ThrownExperienceBottle(this.level, d, e, f);
		} else if (entityType == EntityType.BOAT) {
			entity = new Boat(this.level, d, e, f);
		} else if (entityType == EntityType.TNT) {
			entity = new PrimedTnt(this.level, d, e, f, null);
		} else if (entityType == EntityType.ARMOR_STAND) {
			entity = new ArmorStand(this.level, d, e, f);
		} else if (entityType == EntityType.END_CRYSTAL) {
			entity = new EndCrystal(this.level, d, e, f);
		} else if (entityType == EntityType.ITEM) {
			entity = new ItemEntity(this.level, d, e, f);
		} else if (entityType == EntityType.FALLING_BLOCK) {
			entity = new FallingBlockEntity(this.level, d, e, f, Block.stateById(clientboundAddEntityPacket.getData()));
		} else if (entityType == EntityType.AREA_EFFECT_CLOUD) {
			entity = new AreaEffectCloud(this.level, d, e, f);
		} else {
			entity = null;
		}

		if (entity != null) {
			int i = clientboundAddEntityPacket.getId();
			entity.setPacketCoordinates(d, e, f);
			entity.xRot = (float)(clientboundAddEntityPacket.getxRot() * 360) / 256.0F;
			entity.yRot = (float)(clientboundAddEntityPacket.getyRot() * 360) / 256.0F;
			entity.setId(i);
			entity.setUUID(clientboundAddEntityPacket.getUUID());
			this.level.putNonPlayerEntity(i, entity);
			if (entity instanceof AbstractMinecart) {
				this.minecraft.getSoundManager().play(new MinecartSoundInstance((AbstractMinecart)entity));
			}
		}
	}

	@Override
	public void handleAddExperienceOrb(ClientboundAddExperienceOrbPacket clientboundAddExperienceOrbPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundAddExperienceOrbPacket, this, this.minecraft);
		double d = clientboundAddExperienceOrbPacket.getX();
		double e = clientboundAddExperienceOrbPacket.getY();
		double f = clientboundAddExperienceOrbPacket.getZ();
		Entity entity = new ExperienceOrb(this.level, d, e, f, clientboundAddExperienceOrbPacket.getValue());
		entity.setPacketCoordinates(d, e, f);
		entity.yRot = 0.0F;
		entity.xRot = 0.0F;
		entity.setId(clientboundAddExperienceOrbPacket.getId());
		this.level.putNonPlayerEntity(clientboundAddExperienceOrbPacket.getId(), entity);
	}

	@Override
	public void handleAddGlobalEntity(ClientboundAddGlobalEntityPacket clientboundAddGlobalEntityPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundAddGlobalEntityPacket, this, this.minecraft);
		double d = clientboundAddGlobalEntityPacket.getX();
		double e = clientboundAddGlobalEntityPacket.getY();
		double f = clientboundAddGlobalEntityPacket.getZ();
		if (clientboundAddGlobalEntityPacket.getType() == 1) {
			LightningBolt lightningBolt = new LightningBolt(this.level, d, e, f, false);
			lightningBolt.setPacketCoordinates(d, e, f);
			lightningBolt.yRot = 0.0F;
			lightningBolt.xRot = 0.0F;
			lightningBolt.setId(clientboundAddGlobalEntityPacket.getId());
			this.level.addLightning(lightningBolt);
		}
	}

	@Override
	public void handleAddPainting(ClientboundAddPaintingPacket clientboundAddPaintingPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundAddPaintingPacket, this, this.minecraft);
		Painting painting = new Painting(
			this.level, clientboundAddPaintingPacket.getPos(), clientboundAddPaintingPacket.getDirection(), clientboundAddPaintingPacket.getMotive()
		);
		painting.setId(clientboundAddPaintingPacket.getId());
		painting.setUUID(clientboundAddPaintingPacket.getUUID());
		this.level.putNonPlayerEntity(clientboundAddPaintingPacket.getId(), painting);
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
		double d = clientboundAddPlayerPacket.getX();
		double e = clientboundAddPlayerPacket.getY();
		double f = clientboundAddPlayerPacket.getZ();
		float g = (float)(clientboundAddPlayerPacket.getyRot() * 360) / 256.0F;
		float h = (float)(clientboundAddPlayerPacket.getxRot() * 360) / 256.0F;
		int i = clientboundAddPlayerPacket.getEntityId();
		RemotePlayer remotePlayer = new RemotePlayer(this.minecraft.level, this.getPlayerInfo(clientboundAddPlayerPacket.getPlayerId()).getProfile());
		remotePlayer.setId(i);
		remotePlayer.setPosAndOldPos(d, e, f);
		remotePlayer.setPacketCoordinates(d, e, f);
		remotePlayer.absMoveTo(d, e, f, g, h);
		this.level.addPlayer(i, remotePlayer);
	}

	@Override
	public void handleTeleportEntity(ClientboundTeleportEntityPacket clientboundTeleportEntityPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundTeleportEntityPacket, this, this.minecraft);
		Entity entity = this.level.getEntity(clientboundTeleportEntityPacket.getId());
		if (entity != null) {
			double d = clientboundTeleportEntityPacket.getX();
			double e = clientboundTeleportEntityPacket.getY();
			double f = clientboundTeleportEntityPacket.getZ();
			entity.setPacketCoordinates(d, e, f);
			if (!entity.isControlledByLocalInstance()) {
				float g = (float)(clientboundTeleportEntityPacket.getyRot() * 360) / 256.0F;
				float h = (float)(clientboundTeleportEntityPacket.getxRot() * 360) / 256.0F;
				if (!(Math.abs(entity.getX() - d) >= 0.03125) && !(Math.abs(entity.getY() - e) >= 0.015625) && !(Math.abs(entity.getZ() - f) >= 0.03125)) {
					entity.lerpTo(entity.getX(), entity.getY(), entity.getZ(), g, h, 3, true);
				} else {
					entity.lerpTo(d, e, f, g, h, 3, true);
				}

				entity.setOnGround(clientboundTeleportEntityPacket.isOnGround());
			}
		}
	}

	@Override
	public void handleSetCarriedItem(ClientboundSetCarriedItemPacket clientboundSetCarriedItemPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetCarriedItemPacket, this, this.minecraft);
		if (Inventory.isHotbarSlot(clientboundSetCarriedItemPacket.getSlot())) {
			this.minecraft.player.inventory.selected = clientboundSetCarriedItemPacket.getSlot();
		}
	}

	@Override
	public void handleMoveEntity(ClientboundMoveEntityPacket clientboundMoveEntityPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundMoveEntityPacket, this, this.minecraft);
		Entity entity = clientboundMoveEntityPacket.getEntity(this.level);
		if (entity != null) {
			if (!entity.isControlledByLocalInstance()) {
				if (clientboundMoveEntityPacket.hasPosition()) {
					entity.xp = entity.xp + (long)clientboundMoveEntityPacket.getXa();
					entity.yp = entity.yp + (long)clientboundMoveEntityPacket.getYa();
					entity.zp = entity.zp + (long)clientboundMoveEntityPacket.getZa();
					Vec3 vec3 = ClientboundMoveEntityPacket.packetToEntity(entity.xp, entity.yp, entity.zp);
					float f = clientboundMoveEntityPacket.hasRotation() ? (float)(clientboundMoveEntityPacket.getyRot() * 360) / 256.0F : entity.yRot;
					float g = clientboundMoveEntityPacket.hasRotation() ? (float)(clientboundMoveEntityPacket.getxRot() * 360) / 256.0F : entity.xRot;
					entity.lerpTo(vec3.x, vec3.y, vec3.z, f, g, 3, false);
				} else if (clientboundMoveEntityPacket.hasRotation()) {
					float h = (float)(clientboundMoveEntityPacket.getyRot() * 360) / 256.0F;
					float f = (float)(clientboundMoveEntityPacket.getxRot() * 360) / 256.0F;
					entity.lerpTo(entity.getX(), entity.getY(), entity.getZ(), h, f, 3, false);
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
	public void handleRemoveEntity(ClientboundRemoveEntitiesPacket clientboundRemoveEntitiesPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundRemoveEntitiesPacket, this, this.minecraft);

		for (int i = 0; i < clientboundRemoveEntitiesPacket.getEntityIds().length; i++) {
			int j = clientboundRemoveEntitiesPacket.getEntityIds()[i];
			this.level.removeEntity(j);
		}
	}

	@Override
	public void handleMovePlayer(ClientboundPlayerPositionPacket clientboundPlayerPositionPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundPlayerPositionPacket, this, this.minecraft);
		Player player = this.minecraft.player;
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
		} else {
			d = 0.0;
			e = clientboundPlayerPositionPacket.getX();
			player.xOld = e;
		}

		double f;
		double g;
		if (bl2) {
			f = vec3.y();
			g = player.getY() + clientboundPlayerPositionPacket.getY();
			player.yOld = player.yOld + clientboundPlayerPositionPacket.getY();
		} else {
			f = 0.0;
			g = clientboundPlayerPositionPacket.getY();
			player.yOld = g;
		}

		double h;
		double i;
		if (bl3) {
			h = vec3.z();
			i = player.getZ() + clientboundPlayerPositionPacket.getZ();
			player.zOld = player.zOld + clientboundPlayerPositionPacket.getZ();
		} else {
			h = 0.0;
			i = clientboundPlayerPositionPacket.getZ();
			player.zOld = i;
		}

		if (player.tickCount > 0 && player.getVehicle() != null) {
			player.removeVehicle();
		}

		player.setPosRaw(e, g, i);
		player.xo = e;
		player.yo = g;
		player.zo = i;
		player.setDeltaMovement(d, f, h);
		float j = clientboundPlayerPositionPacket.getYRot();
		float k = clientboundPlayerPositionPacket.getXRot();
		if (clientboundPlayerPositionPacket.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.X_ROT)) {
			k += player.xRot;
		}

		if (clientboundPlayerPositionPacket.getRelativeArguments().contains(ClientboundPlayerPositionPacket.RelativeArgument.Y_ROT)) {
			j += player.yRot;
		}

		player.absMoveTo(e, g, i, j, k);
		this.connection.send(new ServerboundAcceptTeleportationPacket(clientboundPlayerPositionPacket.getId()));
		this.connection.send(new ServerboundMovePlayerPacket.PosRot(player.getX(), player.getY(), player.getZ(), player.yRot, player.xRot, false));
		if (!this.started) {
			this.started = true;
			this.minecraft.setScreen(null);
		}
	}

	@Override
	public void handleChunkBlocksUpdate(ClientboundChunkBlocksUpdatePacket clientboundChunkBlocksUpdatePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundChunkBlocksUpdatePacket, this, this.minecraft);

		for (ClientboundChunkBlocksUpdatePacket.BlockUpdate blockUpdate : clientboundChunkBlocksUpdatePacket.getUpdates()) {
			this.level.setKnownState(blockUpdate.getPos(), blockUpdate.getBlock());
		}
	}

	@Override
	public void handleLevelChunk(ClientboundLevelChunkPacket clientboundLevelChunkPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundLevelChunkPacket, this, this.minecraft);
		int i = clientboundLevelChunkPacket.getX();
		int j = clientboundLevelChunkPacket.getZ();
		LevelChunk levelChunk = this.level
			.getChunkSource()
			.replaceWithPacketData(
				i,
				j,
				clientboundLevelChunkPacket.getBiomes(),
				clientboundLevelChunkPacket.getReadBuffer(),
				clientboundLevelChunkPacket.getHeightmaps(),
				clientboundLevelChunkPacket.getAvailableSections(),
				clientboundLevelChunkPacket.isFullChunk()
			);
		if (levelChunk != null && clientboundLevelChunkPacket.isFullChunk()) {
			this.level.reAddEntitiesToChunk(levelChunk);
		}

		for (int k = 0; k < 16; k++) {
			this.level.setSectionDirtyWithNeighbors(i, k, j);
		}

		for (CompoundTag compoundTag : clientboundLevelChunkPacket.getBlockEntitiesTags()) {
			BlockPos blockPos = new BlockPos(compoundTag.getInt("x"), compoundTag.getInt("y"), compoundTag.getInt("z"));
			BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
			if (blockEntity != null) {
				blockEntity.load(this.level.getBlockState(blockPos), compoundTag);
			}
		}
	}

	@Override
	public void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket clientboundForgetLevelChunkPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundForgetLevelChunkPacket, this, this.minecraft);
		int i = clientboundForgetLevelChunkPacket.getX();
		int j = clientboundForgetLevelChunkPacket.getZ();
		ClientChunkCache clientChunkCache = this.level.getChunkSource();
		clientChunkCache.drop(i, j);
		LevelLightEngine levelLightEngine = clientChunkCache.getLightEngine();

		for (int k = 0; k < 16; k++) {
			this.level.setSectionDirtyWithNeighbors(i, k, j);
			levelLightEngine.updateSectionStatus(SectionPos.of(i, k, j), true);
		}

		levelLightEngine.enableLightSources(new ChunkPos(i, j), false);
	}

	@Override
	public void handleBlockUpdate(ClientboundBlockUpdatePacket clientboundBlockUpdatePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundBlockUpdatePacket, this, this.minecraft);
		this.level.setKnownState(clientboundBlockUpdatePacket.getPos(), clientboundBlockUpdatePacket.getBlockState());
	}

	@Override
	public void handleDisconnect(ClientboundDisconnectPacket clientboundDisconnectPacket) {
		this.connection.disconnect(clientboundDisconnectPacket.getReason());
	}

	@Override
	public void onDisconnect(Component component) {
		this.minecraft.clearLevel();
		if (this.callbackScreen != null) {
			if (this.callbackScreen instanceof RealmsScreen) {
				this.minecraft.setScreen(new DisconnectedRealmsScreen(this.callbackScreen, "disconnect.lost", component));
			} else {
				this.minecraft.setScreen(new DisconnectedScreen(this.callbackScreen, "disconnect.lost", component));
			}
		} else {
			this.minecraft.setScreen(new DisconnectedScreen(new JoinMultiplayerScreen(new TitleScreen()), "disconnect.lost", component));
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
			if (entity instanceof ItemEntity) {
				ItemEntity itemEntity = (ItemEntity)entity;
				ItemStack itemStack = itemEntity.getItem();
				itemStack.shrink(clientboundTakeItemEntityPacket.getAmount());
				if (itemStack.isEmpty()) {
					this.level.removeEntity(clientboundTakeItemEntityPacket.getItemId());
				}
			} else {
				this.level.removeEntity(clientboundTakeItemEntityPacket.getItemId());
			}
		}
	}

	@Override
	public void handleChat(ClientboundChatPacket clientboundChatPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundChatPacket, this, this.minecraft);
		this.minecraft.gui.handleChat(clientboundChatPacket.getType(), clientboundChatPacket.getMessage(), clientboundChatPacket.getSender());
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
	public void handleAddMob(ClientboundAddMobPacket clientboundAddMobPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundAddMobPacket, this, this.minecraft);
		double d = clientboundAddMobPacket.getX();
		double e = clientboundAddMobPacket.getY();
		double f = clientboundAddMobPacket.getZ();
		float g = (float)(clientboundAddMobPacket.getyRot() * 360) / 256.0F;
		float h = (float)(clientboundAddMobPacket.getxRot() * 360) / 256.0F;
		LivingEntity livingEntity = (LivingEntity)EntityType.create(clientboundAddMobPacket.getType(), this.minecraft.level);
		if (livingEntity != null) {
			livingEntity.setPacketCoordinates(d, e, f);
			livingEntity.yBodyRot = (float)(clientboundAddMobPacket.getyHeadRot() * 360) / 256.0F;
			livingEntity.yHeadRot = (float)(clientboundAddMobPacket.getyHeadRot() * 360) / 256.0F;
			if (livingEntity instanceof EnderDragon) {
				EnderDragonPart[] enderDragonParts = ((EnderDragon)livingEntity).getSubEntities();

				for (int i = 0; i < enderDragonParts.length; i++) {
					enderDragonParts[i].setId(i + clientboundAddMobPacket.getId());
				}
			}

			livingEntity.setId(clientboundAddMobPacket.getId());
			livingEntity.setUUID(clientboundAddMobPacket.getUUID());
			livingEntity.absMoveTo(d, e, f, g, h);
			livingEntity.setDeltaMovement(
				(double)((float)clientboundAddMobPacket.getXd() / 8000.0F),
				(double)((float)clientboundAddMobPacket.getYd() / 8000.0F),
				(double)((float)clientboundAddMobPacket.getZd() / 8000.0F)
			);
			this.level.putNonPlayerEntity(clientboundAddMobPacket.getId(), livingEntity);
			if (livingEntity instanceof Bee) {
				boolean bl = ((Bee)livingEntity).isAngry();
				BeeSoundInstance beeSoundInstance;
				if (bl) {
					beeSoundInstance = new BeeAggressiveSoundInstance((Bee)livingEntity);
				} else {
					beeSoundInstance = new BeeFlyingSoundInstance((Bee)livingEntity);
				}

				this.minecraft.getSoundManager().queueTickingSound(beeSoundInstance);
			}
		} else {
			LOGGER.warn("Skipping Entity with id {}", clientboundAddMobPacket.getType());
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
		this.minecraft.level.setDefaultSpawnPos(clientboundSetDefaultSpawnPositionPacket.getPos());
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
						this.minecraft.gui.setOverlayMessage(new TranslatableComponent("mount.onboard", this.minecraft.options.keyShift.getTranslatedKeyMessage()), false);
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
			if (itemStack.getItem() == Items.TOTEM_OF_UNDYING) {
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
		ResourceKey<DimensionType> resourceKey = clientboundRespawnPacket.getDimensionType();
		ResourceKey<Level> resourceKey2 = clientboundRespawnPacket.getDimension();
		DimensionType dimensionType = this.registryAccess.dimensionTypes().get(resourceKey);
		LocalPlayer localPlayer = this.minecraft.player;
		int i = localPlayer.getId();
		this.started = false;
		if (resourceKey2 != localPlayer.level.dimension()) {
			Scoreboard scoreboard = this.level.getScoreboard();
			boolean bl = clientboundRespawnPacket.isDebug();
			boolean bl2 = clientboundRespawnPacket.isFlat();
			ClientLevel.ClientLevelData clientLevelData = new ClientLevel.ClientLevelData(this.levelData.getDifficulty(), this.levelData.isHardcore(), bl2);
			this.levelData = clientLevelData;
			this.level = new ClientLevel(
				this,
				clientLevelData,
				resourceKey2,
				resourceKey,
				dimensionType,
				this.serverChunkRadius,
				this.minecraft::getProfiler,
				this.minecraft.levelRenderer,
				bl,
				clientboundRespawnPacket.getSeed()
			);
			this.level.setScoreboard(scoreboard);
			this.minecraft.setLevel(this.level);
			this.minecraft.setScreen(new ReceivingLevelScreen());
		}

		this.level.removeAllPendingEntityRemovals();
		String string = localPlayer.getServerBrand();
		this.minecraft.cameraEntity = null;
		LocalPlayer localPlayer2 = this.minecraft
			.gameMode
			.createPlayer(this.level, localPlayer.getStats(), localPlayer.getRecipeBook(), localPlayer.isShiftKeyDown(), localPlayer.isSprinting());
		localPlayer2.setId(i);
		this.minecraft.player = localPlayer2;
		this.minecraft.cameraEntity = localPlayer2;
		localPlayer2.getEntityData().assignValues(localPlayer.getEntityData().getAll());
		if (clientboundRespawnPacket.shouldKeepAllPlayerData()) {
			localPlayer2.getAttributes().assignValues(localPlayer.getAttributes());
		}

		localPlayer2.resetPos();
		localPlayer2.setServerBrand(string);
		this.level.addPlayer(i, localPlayer2);
		localPlayer2.yRot = -180.0F;
		localPlayer2.input = new KeyboardInput(this.minecraft.options);
		this.minecraft.gameMode.adjustPlayer(localPlayer2);
		localPlayer2.setReducedDebugInfo(localPlayer.isReducedDebugInfo());
		localPlayer2.setShowDeathScreen(localPlayer.shouldShowDeathScreen());
		if (this.minecraft.screen instanceof DeathScreen) {
			this.minecraft.setScreen(null);
		}

		this.minecraft.gameMode.setLocalMode(clientboundRespawnPacket.getPlayerGameType());
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
				clientboundHorseScreenOpenPacket.getContainerId(), localPlayer.inventory, simpleContainer, abstractHorse
			);
			localPlayer.containerMenu = horseInventoryMenu;
			this.minecraft.setScreen(new HorseInventoryScreen(horseInventoryMenu, localPlayer.inventory, abstractHorse));
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
				player.inventory.setCarried(itemStack);
			}
		} else if (clientboundContainerSetSlotPacket.getContainerId() == -2) {
			player.inventory.setItem(i, itemStack);
		} else {
			boolean bl = false;
			if (this.minecraft.screen instanceof CreativeModeInventoryScreen) {
				CreativeModeInventoryScreen creativeModeInventoryScreen = (CreativeModeInventoryScreen)this.minecraft.screen;
				bl = creativeModeInventoryScreen.getSelectedTab() != CreativeModeTab.TAB_INVENTORY.getId();
			}

			if (clientboundContainerSetSlotPacket.getContainerId() == 0 && clientboundContainerSetSlotPacket.getSlot() >= 36 && i < 45) {
				if (!itemStack.isEmpty()) {
					ItemStack itemStack2 = player.inventoryMenu.getSlot(i).getItem();
					if (itemStack2.isEmpty() || itemStack2.getCount() < itemStack.getCount()) {
						itemStack.setPopTime(5);
					}
				}

				player.inventoryMenu.setItem(i, itemStack);
			} else if (clientboundContainerSetSlotPacket.getContainerId() == player.containerMenu.containerId
				&& (clientboundContainerSetSlotPacket.getContainerId() != 0 || !bl)) {
				player.containerMenu.setItem(i, itemStack);
			}
		}
	}

	@Override
	public void handleContainerAck(ClientboundContainerAckPacket clientboundContainerAckPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundContainerAckPacket, this, this.minecraft);
		AbstractContainerMenu abstractContainerMenu = null;
		Player player = this.minecraft.player;
		if (clientboundContainerAckPacket.getContainerId() == 0) {
			abstractContainerMenu = player.inventoryMenu;
		} else if (clientboundContainerAckPacket.getContainerId() == player.containerMenu.containerId) {
			abstractContainerMenu = player.containerMenu;
		}

		if (abstractContainerMenu != null && !clientboundContainerAckPacket.isAccepted()) {
			this.send(new ServerboundContainerAckPacket(clientboundContainerAckPacket.getContainerId(), clientboundContainerAckPacket.getUid(), true));
		}
	}

	@Override
	public void handleContainerContent(ClientboundContainerSetContentPacket clientboundContainerSetContentPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundContainerSetContentPacket, this, this.minecraft);
		Player player = this.minecraft.player;
		if (clientboundContainerSetContentPacket.getContainerId() == 0) {
			player.inventoryMenu.setAll(clientboundContainerSetContentPacket.getItems());
		} else if (clientboundContainerSetContentPacket.getContainerId() == player.containerMenu.containerId) {
			player.containerMenu.setAll(clientboundContainerSetContentPacket.getItems());
		}
	}

	@Override
	public void handleOpenSignEditor(ClientboundOpenSignEditorPacket clientboundOpenSignEditorPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundOpenSignEditorPacket, this, this.minecraft);
		BlockEntity blockEntity = this.level.getBlockEntity(clientboundOpenSignEditorPacket.getPos());
		if (!(blockEntity instanceof SignBlockEntity)) {
			blockEntity = new SignBlockEntity();
			blockEntity.setLevelAndPosition(this.level, clientboundOpenSignEditorPacket.getPos());
		}

		this.minecraft.player.openTextEdit((SignBlockEntity)blockEntity);
	}

	@Override
	public void handleBlockEntityData(ClientboundBlockEntityDataPacket clientboundBlockEntityDataPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundBlockEntityDataPacket, this, this.minecraft);
		BlockPos blockPos = clientboundBlockEntityDataPacket.getPos();
		BlockEntity blockEntity = this.minecraft.level.getBlockEntity(blockPos);
		int i = clientboundBlockEntityDataPacket.getType();
		boolean bl = i == 2 && blockEntity instanceof CommandBlockEntity;
		if (i == 1 && blockEntity instanceof SpawnerBlockEntity
			|| bl
			|| i == 3 && blockEntity instanceof BeaconBlockEntity
			|| i == 4 && blockEntity instanceof SkullBlockEntity
			|| i == 6 && blockEntity instanceof BannerBlockEntity
			|| i == 7 && blockEntity instanceof StructureBlockEntity
			|| i == 8 && blockEntity instanceof TheEndGatewayBlockEntity
			|| i == 9 && blockEntity instanceof SignBlockEntity
			|| i == 11 && blockEntity instanceof BedBlockEntity
			|| i == 5 && blockEntity instanceof ConduitBlockEntity
			|| i == 12 && blockEntity instanceof JigsawBlockEntity
			|| i == 13 && blockEntity instanceof CampfireBlockEntity
			|| i == 14 && blockEntity instanceof BeehiveBlockEntity) {
			blockEntity.load(this.minecraft.level.getBlockState(blockPos), clientboundBlockEntityDataPacket.getTag());
		}

		if (bl && this.minecraft.screen instanceof CommandBlockEditScreen) {
			((CommandBlockEditScreen)this.minecraft.screen).updateGui();
		}
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
	public void handleSetEquippedItem(ClientboundSetEquippedItemPacket clientboundSetEquippedItemPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetEquippedItemPacket, this, this.minecraft);
		Entity entity = this.level.getEntity(clientboundSetEquippedItemPacket.getEntity());
		if (entity != null) {
			entity.setItemSlot(clientboundSetEquippedItemPacket.getSlot(), clientboundSetEquippedItemPacket.getItem());
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
		int i = clientboundGameEventPacket.getEvent();
		float f = clientboundGameEventPacket.getParam();
		int j = Mth.floor(f + 0.5F);
		if (i >= 0 && i < ClientboundGameEventPacket.EVENT_LANGUAGE_ID.length && ClientboundGameEventPacket.EVENT_LANGUAGE_ID[i] != null) {
			player.displayClientMessage(new TranslatableComponent(ClientboundGameEventPacket.EVENT_LANGUAGE_ID[i]), false);
		}

		if (i == 1) {
			this.level.getLevelData().setRaining(true);
			this.level.setRainLevel(0.0F);
		} else if (i == 2) {
			this.level.getLevelData().setRaining(false);
			this.level.setRainLevel(1.0F);
		} else if (i == 3) {
			this.minecraft.gameMode.setLocalMode(GameType.byId(j));
		} else if (i == 4) {
			if (j == 0) {
				this.minecraft.player.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
				this.minecraft.setScreen(new ReceivingLevelScreen());
			} else if (j == 1) {
				this.minecraft
					.setScreen(
						new WinScreen(
							true, () -> this.minecraft.player.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN))
						)
					);
			}
		} else if (i == 5) {
			Options options = this.minecraft.options;
			if (f == 0.0F) {
				this.minecraft.setScreen(new DemoIntroScreen());
			} else if (f == 101.0F) {
				this.minecraft
					.gui
					.getChat()
					.addMessage(
						new TranslatableComponent(
							"demo.help.movement",
							options.keyUp.getTranslatedKeyMessage(),
							options.keyLeft.getTranslatedKeyMessage(),
							options.keyDown.getTranslatedKeyMessage(),
							options.keyRight.getTranslatedKeyMessage()
						)
					);
			} else if (f == 102.0F) {
				this.minecraft.gui.getChat().addMessage(new TranslatableComponent("demo.help.jump", options.keyJump.getTranslatedKeyMessage()));
			} else if (f == 103.0F) {
				this.minecraft.gui.getChat().addMessage(new TranslatableComponent("demo.help.inventory", options.keyInventory.getTranslatedKeyMessage()));
			} else if (f == 104.0F) {
				this.minecraft.gui.getChat().addMessage(new TranslatableComponent("demo.day.6", options.keyScreenshot.getTranslatedKeyMessage()));
			}
		} else if (i == 6) {
			this.level.playSound(player, player.getX(), player.getEyeY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.18F, 0.45F);
		} else if (i == 7) {
			this.level.setRainLevel(f);
		} else if (i == 8) {
			this.level.setThunderLevel(f);
		} else if (i == 9) {
			this.level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.PUFFER_FISH_STING, SoundSource.NEUTRAL, 1.0F, 1.0F);
		} else if (i == 10) {
			this.level.addParticle(ParticleTypes.ELDER_GUARDIAN, player.getX(), player.getY(), player.getZ(), 0.0, 0.0, 0.0);
			if (j == 1) {
				this.level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.0F, 1.0F);
			}
		} else if (i == 11) {
			this.minecraft.player.setShowDeathScreen(f == 0.0F);
		}
	}

	@Override
	public void handleMapItemData(ClientboundMapItemDataPacket clientboundMapItemDataPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundMapItemDataPacket, this, this.minecraft);
		MapRenderer mapRenderer = this.minecraft.gameRenderer.getMapRenderer();
		String string = MapItem.makeKey(clientboundMapItemDataPacket.getMapId());
		MapItemSavedData mapItemSavedData = this.minecraft.level.getMapData(string);
		if (mapItemSavedData == null) {
			mapItemSavedData = new MapItemSavedData(string);
			if (mapRenderer.getMapInstanceIfExists(string) != null) {
				MapItemSavedData mapItemSavedData2 = mapRenderer.getData(mapRenderer.getMapInstanceIfExists(string));
				if (mapItemSavedData2 != null) {
					mapItemSavedData = mapItemSavedData2;
				}
			}

			this.minecraft.level.setMapData(mapItemSavedData);
		}

		clientboundMapItemDataPacket.applyToMap(mapItemSavedData);
		mapRenderer.update(mapItemSavedData);
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
		this.commands = new CommandDispatcher<>(clientboundCommandsPacket.getRoot());
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
		MutableSearchTree<RecipeCollection> mutableSearchTree = this.minecraft.getSearchTree(SearchRegistry.RECIPE_COLLECTIONS);
		mutableSearchTree.clear();
		ClientRecipeBook clientRecipeBook = this.minecraft.player.getRecipeBook();
		clientRecipeBook.setupCollections();
		clientRecipeBook.getCollections().forEach(mutableSearchTree::add);
		mutableSearchTree.refresh();
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
		clientRecipeBook.setGuiOpen(clientboundRecipePacket.isGuiOpen());
		clientRecipeBook.setFilteringCraftable(clientboundRecipePacket.isFilteringCraftable());
		clientRecipeBook.setFurnaceGuiOpen(clientboundRecipePacket.isFurnaceGuiOpen());
		clientRecipeBook.setFurnaceFilteringCraftable(clientboundRecipePacket.isFurnaceFilteringCraftable());
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
			MobEffect mobEffect = MobEffect.byId(clientboundUpdateMobEffectPacket.getEffectId());
			if (mobEffect != null) {
				MobEffectInstance mobEffectInstance = new MobEffectInstance(
					mobEffect,
					clientboundUpdateMobEffectPacket.getEffectDurationTicks(),
					clientboundUpdateMobEffectPacket.getEffectAmplifier(),
					clientboundUpdateMobEffectPacket.isEffectAmbient(),
					clientboundUpdateMobEffectPacket.isEffectVisible(),
					clientboundUpdateMobEffectPacket.effectShowsIcon()
				);
				mobEffectInstance.setNoCounter(clientboundUpdateMobEffectPacket.isSuperLongDuration());
				((LivingEntity)entity).forceAddEffect(mobEffectInstance);
			}
		}
	}

	@Override
	public void handleUpdateTags(ClientboundUpdateTagsPacket clientboundUpdateTagsPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundUpdateTagsPacket, this, this.minecraft);
		this.tags = clientboundUpdateTagsPacket.getTags();
		if (!this.connection.isMemoryConnection()) {
			this.tags.bindToGlobal();
		}

		this.minecraft.getSearchTree(SearchRegistry.CREATIVE_TAGS).refresh();
	}

	@Override
	public void handlePlayerCombat(ClientboundPlayerCombatPacket clientboundPlayerCombatPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundPlayerCombatPacket, this, this.minecraft);
		if (clientboundPlayerCombatPacket.event == ClientboundPlayerCombatPacket.Event.ENTITY_DIED) {
			Entity entity = this.level.getEntity(clientboundPlayerCombatPacket.playerId);
			if (entity == this.minecraft.player) {
				if (this.minecraft.player.shouldShowDeathScreen()) {
					this.minecraft.setScreen(new DeathScreen(clientboundPlayerCombatPacket.message, this.level.getLevelData().isHardcore()));
				} else {
					this.minecraft.player.respawn();
				}
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
	public void handleSetBorder(ClientboundSetBorderPacket clientboundSetBorderPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetBorderPacket, this, this.minecraft);
		clientboundSetBorderPacket.applyChanges(this.level.getWorldBorder());
	}

	@Override
	public void handleSetTitles(ClientboundSetTitlesPacket clientboundSetTitlesPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetTitlesPacket, this, this.minecraft);
		ClientboundSetTitlesPacket.Type type = clientboundSetTitlesPacket.getType();
		Component component = null;
		Component component2 = null;
		Component component3 = clientboundSetTitlesPacket.getText() != null ? clientboundSetTitlesPacket.getText() : TextComponent.EMPTY;
		switch (type) {
			case TITLE:
				component = component3;
				break;
			case SUBTITLE:
				component2 = component3;
				break;
			case ACTIONBAR:
				this.minecraft.gui.setOverlayMessage(component3, false);
				return;
			case RESET:
				this.minecraft.gui.setTitles(null, null, -1, -1, -1);
				this.minecraft.gui.resetTitleTimes();
				return;
		}

		this.minecraft
			.gui
			.setTitles(
				component, component2, clientboundSetTitlesPacket.getFadeInTime(), clientboundSetTitlesPacket.getStayTime(), clientboundSetTitlesPacket.getFadeOutTime()
			);
	}

	@Override
	public void handleTabListCustomisation(ClientboundTabListPacket clientboundTabListPacket) {
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
	public void handlePlayerInfo(ClientboundPlayerInfoPacket clientboundPlayerInfoPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundPlayerInfoPacket, this, this.minecraft);

		for (ClientboundPlayerInfoPacket.PlayerUpdate playerUpdate : clientboundPlayerInfoPacket.getEntries()) {
			if (clientboundPlayerInfoPacket.getAction() == ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER) {
				this.playerInfoMap.remove(playerUpdate.getProfile().getId());
			} else {
				PlayerInfo playerInfo = (PlayerInfo)this.playerInfoMap.get(playerUpdate.getProfile().getId());
				if (clientboundPlayerInfoPacket.getAction() == ClientboundPlayerInfoPacket.Action.ADD_PLAYER) {
					playerInfo = new PlayerInfo(playerUpdate);
					this.playerInfoMap.put(playerInfo.getProfile().getId(), playerInfo);
				}

				if (playerInfo != null) {
					switch (clientboundPlayerInfoPacket.getAction()) {
						case ADD_PLAYER:
							playerInfo.setGameMode(playerUpdate.getGameMode());
							playerInfo.setLatency(playerUpdate.getLatency());
							playerInfo.setTabListDisplayName(playerUpdate.getDisplayName());
							break;
						case UPDATE_GAME_MODE:
							playerInfo.setGameMode(playerUpdate.getGameMode());
							break;
						case UPDATE_LATENCY:
							playerInfo.setLatency(playerUpdate.getLatency());
							break;
						case UPDATE_DISPLAY_NAME:
							playerInfo.setTabListDisplayName(playerUpdate.getDisplayName());
					}
				}
			}
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
		player.abilities.flying = clientboundPlayerAbilitiesPacket.isFlying();
		player.abilities.instabuild = clientboundPlayerAbilitiesPacket.canInstabuild();
		player.abilities.invulnerable = clientboundPlayerAbilitiesPacket.isInvulnerable();
		player.abilities.mayfly = clientboundPlayerAbilitiesPacket.canFly();
		player.abilities.setFlyingSpeed(clientboundPlayerAbilitiesPacket.getFlyingSpeed());
		player.abilities.setWalkingSpeed(clientboundPlayerAbilitiesPacket.getWalkingSpeed());
	}

	@Override
	public void handleSoundEvent(ClientboundSoundPacket clientboundSoundPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSoundPacket, this, this.minecraft);
		this.minecraft
			.level
			.playSound(
				this.minecraft.player,
				clientboundSoundPacket.getX(),
				clientboundSoundPacket.getY(),
				clientboundSoundPacket.getZ(),
				clientboundSoundPacket.getSound(),
				clientboundSoundPacket.getSource(),
				clientboundSoundPacket.getVolume(),
				clientboundSoundPacket.getPitch()
			);
	}

	@Override
	public void handleSoundEntityEvent(ClientboundSoundEntityPacket clientboundSoundEntityPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSoundEntityPacket, this, this.minecraft);
		Entity entity = this.level.getEntity(clientboundSoundEntityPacket.getId());
		if (entity != null) {
			this.minecraft
				.level
				.playSound(
					this.minecraft.player,
					entity,
					clientboundSoundEntityPacket.getSound(),
					clientboundSoundEntityPacket.getSource(),
					clientboundSoundEntityPacket.getVolume(),
					clientboundSoundEntityPacket.getPitch()
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
					false,
					0,
					SoundInstance.Attenuation.LINEAR,
					(float)clientboundCustomSoundPacket.getX(),
					(float)clientboundCustomSoundPacket.getY(),
					(float)clientboundCustomSoundPacket.getZ(),
					false
				)
			);
	}

	@Override
	public void handleResourcePack(ClientboundResourcePackPacket clientboundResourcePackPacket) {
		String string = clientboundResourcePackPacket.getUrl();
		String string2 = clientboundResourcePackPacket.getHash();
		if (this.validateResourcePackUrl(string)) {
			if (string.startsWith("level://")) {
				try {
					String string3 = URLDecoder.decode(string.substring("level://".length()), StandardCharsets.UTF_8.toString());
					File file = new File(this.minecraft.gameDirectory, "saves");
					File file2 = new File(file, string3);
					if (file2.isFile()) {
						this.send(ServerboundResourcePackPacket.Action.ACCEPTED);
						CompletableFuture<?> completableFuture = this.minecraft.getClientPackSource().setServerPack(file2);
						this.downloadCallback(completableFuture);
						return;
					}
				} catch (UnsupportedEncodingException var8) {
				}

				this.send(ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD);
			} else {
				ServerData serverData = this.minecraft.getCurrentServer();
				if (serverData != null && serverData.getResourcePackStatus() == ServerData.ServerPackStatus.ENABLED) {
					this.send(ServerboundResourcePackPacket.Action.ACCEPTED);
					this.downloadCallback(this.minecraft.getClientPackSource().downloadAndSelectResourcePack(string, string2));
				} else if (serverData != null && serverData.getResourcePackStatus() != ServerData.ServerPackStatus.PROMPT) {
					this.send(ServerboundResourcePackPacket.Action.DECLINED);
				} else {
					this.minecraft.execute(() -> this.minecraft.setScreen(new ConfirmScreen(bl -> {
							this.minecraft = Minecraft.getInstance();
							ServerData serverDatax = this.minecraft.getCurrentServer();
							if (bl) {
								if (serverDatax != null) {
									serverDatax.setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);
								}

								this.send(ServerboundResourcePackPacket.Action.ACCEPTED);
								this.downloadCallback(this.minecraft.getClientPackSource().downloadAndSelectResourcePack(string, string2));
							} else {
								if (serverDatax != null) {
									serverDatax.setResourcePackStatus(ServerData.ServerPackStatus.DISABLED);
								}

								this.send(ServerboundResourcePackPacket.Action.DECLINED);
							}

							ServerList.saveSingleServer(serverDatax);
							this.minecraft.setScreen(null);
						}, new TranslatableComponent("multiplayer.texturePrompt.line1"), new TranslatableComponent("multiplayer.texturePrompt.line2"))));
				}
			}
		}
	}

	private boolean validateResourcePackUrl(String string) {
		try {
			URI uRI = new URI(string);
			String string2 = uRI.getScheme();
			boolean bl = "level".equals(string2);
			if (!"http".equals(string2) && !"https".equals(string2) && !bl) {
				throw new URISyntaxException(string, "Wrong protocol");
			} else if (!bl || !string.contains("..") && string.endsWith("/resources.zip")) {
				return true;
			} else {
				throw new URISyntaxException(string, "Invalid levelstorage resourcepack path");
			}
		} catch (URISyntaxException var5) {
			this.send(ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD);
			return false;
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
		if (itemStack.getItem() == Items.WRITTEN_BOOK) {
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
				this.minecraft.player.setServerBrand(friendlyByteBuf.readUtf(32767));
			} else if (ClientboundCustomPayloadPacket.DEBUG_PATHFINDING_PACKET.equals(resourceLocation)) {
				int i = friendlyByteBuf.readInt();
				float f = friendlyByteBuf.readFloat();
				Path path = Path.createFromStream(friendlyByteBuf);
				this.minecraft.debugRenderer.pathfindingRenderer.addPath(i, path, f);
			} else if (ClientboundCustomPayloadPacket.DEBUG_NEIGHBORSUPDATE_PACKET.equals(resourceLocation)) {
				long l = friendlyByteBuf.readVarLong();
				BlockPos blockPos = friendlyByteBuf.readBlockPos();
				((NeighborsUpdateRenderer)this.minecraft.debugRenderer.neighborsUpdateRenderer).addUpdate(l, blockPos);
			} else if (ClientboundCustomPayloadPacket.DEBUG_CAVES_PACKET.equals(resourceLocation)) {
				BlockPos blockPos2 = friendlyByteBuf.readBlockPos();
				int j = friendlyByteBuf.readInt();
				List<BlockPos> list = Lists.<BlockPos>newArrayList();
				List<Float> list2 = Lists.<Float>newArrayList();

				for (int k = 0; k < j; k++) {
					list.add(friendlyByteBuf.readBlockPos());
					list2.add(friendlyByteBuf.readFloat());
				}

				this.minecraft.debugRenderer.caveRenderer.addTunnel(blockPos2, list, list2);
			} else if (ClientboundCustomPayloadPacket.DEBUG_STRUCTURES_PACKET.equals(resourceLocation)) {
				DimensionType dimensionType = this.registryAccess.dimensionTypes().get(friendlyByteBuf.readResourceLocation());
				BoundingBox boundingBox = new BoundingBox(
					friendlyByteBuf.readInt(),
					friendlyByteBuf.readInt(),
					friendlyByteBuf.readInt(),
					friendlyByteBuf.readInt(),
					friendlyByteBuf.readInt(),
					friendlyByteBuf.readInt()
				);
				int m = friendlyByteBuf.readInt();
				List<BoundingBox> list2 = Lists.<BoundingBox>newArrayList();
				List<Boolean> list3 = Lists.<Boolean>newArrayList();

				for (int n = 0; n < m; n++) {
					list2.add(
						new BoundingBox(
							friendlyByteBuf.readInt(),
							friendlyByteBuf.readInt(),
							friendlyByteBuf.readInt(),
							friendlyByteBuf.readInt(),
							friendlyByteBuf.readInt(),
							friendlyByteBuf.readInt()
						)
					);
					list3.add(friendlyByteBuf.readBoolean());
				}

				this.minecraft.debugRenderer.structureRenderer.addBoundingBox(boundingBox, list2, list3, dimensionType);
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

				for (int j = 0; j < i; j++) {
					this.minecraft.debugRenderer.villageSectionsDebugRenderer.setVillageSection(friendlyByteBuf.readSectionPos());
				}

				int j = friendlyByteBuf.readInt();

				for (int m = 0; m < j; m++) {
					this.minecraft.debugRenderer.villageSectionsDebugRenderer.setNotVillageSection(friendlyByteBuf.readSectionPos());
				}
			} else if (ClientboundCustomPayloadPacket.DEBUG_POI_ADDED_PACKET.equals(resourceLocation)) {
				BlockPos blockPos2 = friendlyByteBuf.readBlockPos();
				String string = friendlyByteBuf.readUtf();
				int m = friendlyByteBuf.readInt();
				BrainDebugRenderer.PoiInfo poiInfo = new BrainDebugRenderer.PoiInfo(blockPos2, string, m);
				this.minecraft.debugRenderer.brainDebugRenderer.addPoi(poiInfo);
			} else if (ClientboundCustomPayloadPacket.DEBUG_POI_REMOVED_PACKET.equals(resourceLocation)) {
				BlockPos blockPos2 = friendlyByteBuf.readBlockPos();
				this.minecraft.debugRenderer.brainDebugRenderer.removePoi(blockPos2);
			} else if (ClientboundCustomPayloadPacket.DEBUG_POI_TICKET_COUNT_PACKET.equals(resourceLocation)) {
				BlockPos blockPos2 = friendlyByteBuf.readBlockPos();
				int j = friendlyByteBuf.readInt();
				this.minecraft.debugRenderer.brainDebugRenderer.setFreeTicketCount(blockPos2, j);
			} else if (ClientboundCustomPayloadPacket.DEBUG_GOAL_SELECTOR.equals(resourceLocation)) {
				BlockPos blockPos2 = friendlyByteBuf.readBlockPos();
				int j = friendlyByteBuf.readInt();
				int m = friendlyByteBuf.readInt();
				List<GoalSelectorDebugRenderer.DebugGoal> list2 = Lists.<GoalSelectorDebugRenderer.DebugGoal>newArrayList();

				for (int k = 0; k < m; k++) {
					int n = friendlyByteBuf.readInt();
					boolean bl = friendlyByteBuf.readBoolean();
					String string2 = friendlyByteBuf.readUtf(255);
					list2.add(new GoalSelectorDebugRenderer.DebugGoal(blockPos2, n, string2, bl));
				}

				this.minecraft.debugRenderer.goalSelectorRenderer.addGoalSelector(j, list2);
			} else if (ClientboundCustomPayloadPacket.DEBUG_RAIDS.equals(resourceLocation)) {
				int i = friendlyByteBuf.readInt();
				Collection<BlockPos> collection = Lists.<BlockPos>newArrayList();

				for (int m = 0; m < i; m++) {
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
				String string3 = friendlyByteBuf.readUtf();
				String string4 = friendlyByteBuf.readUtf();
				int p = friendlyByteBuf.readInt();
				float h = friendlyByteBuf.readFloat();
				float q = friendlyByteBuf.readFloat();
				String string5 = friendlyByteBuf.readUtf();
				boolean bl2 = friendlyByteBuf.readBoolean();
				Path path2;
				if (bl2) {
					path2 = Path.createFromStream(friendlyByteBuf);
				} else {
					path2 = null;
				}

				boolean bl3 = friendlyByteBuf.readBoolean();
				BrainDebugRenderer.BrainDump brainDump = new BrainDebugRenderer.BrainDump(uUID, o, string3, string4, p, h, q, position, string5, path2, bl3);
				int r = friendlyByteBuf.readInt();

				for (int s = 0; s < r; s++) {
					String string6 = friendlyByteBuf.readUtf();
					brainDump.activities.add(string6);
				}

				int s = friendlyByteBuf.readInt();

				for (int t = 0; t < s; t++) {
					String string7 = friendlyByteBuf.readUtf();
					brainDump.behaviors.add(string7);
				}

				int t = friendlyByteBuf.readInt();

				for (int u = 0; u < t; u++) {
					String string8 = friendlyByteBuf.readUtf();
					brainDump.memories.add(string8);
				}

				int u = friendlyByteBuf.readInt();

				for (int v = 0; v < u; v++) {
					BlockPos blockPos3 = friendlyByteBuf.readBlockPos();
					brainDump.pois.add(blockPos3);
				}

				int v = friendlyByteBuf.readInt();

				for (int w = 0; w < v; w++) {
					BlockPos blockPos4 = friendlyByteBuf.readBlockPos();
					brainDump.potentialPois.add(blockPos4);
				}

				int w = friendlyByteBuf.readInt();

				for (int x = 0; x < w; x++) {
					String string9 = friendlyByteBuf.readUtf();
					brainDump.gossips.add(string9);
				}

				this.minecraft.debugRenderer.brainDebugRenderer.addOrUpdateBrainDump(brainDump);
			} else if (ClientboundCustomPayloadPacket.DEBUG_BEE.equals(resourceLocation)) {
				double d = friendlyByteBuf.readDouble();
				double e = friendlyByteBuf.readDouble();
				double g = friendlyByteBuf.readDouble();
				Position position = new PositionImpl(d, e, g);
				UUID uUID = friendlyByteBuf.readUUID();
				int o = friendlyByteBuf.readInt();
				boolean bl4 = friendlyByteBuf.readBoolean();
				BlockPos blockPos5 = null;
				if (bl4) {
					blockPos5 = friendlyByteBuf.readBlockPos();
				}

				boolean bl5 = friendlyByteBuf.readBoolean();
				BlockPos blockPos6 = null;
				if (bl5) {
					blockPos6 = friendlyByteBuf.readBlockPos();
				}

				int y = friendlyByteBuf.readInt();
				boolean bl6 = friendlyByteBuf.readBoolean();
				Path path3 = null;
				if (bl6) {
					path3 = Path.createFromStream(friendlyByteBuf);
				}

				BeeDebugRenderer.BeeInfo beeInfo = new BeeDebugRenderer.BeeInfo(uUID, o, position, path3, blockPos5, blockPos6, y);
				int z = friendlyByteBuf.readInt();

				for (int aa = 0; aa < z; aa++) {
					String string10 = friendlyByteBuf.readUtf();
					beeInfo.goals.add(string10);
				}

				int aa = friendlyByteBuf.readInt();

				for (int r = 0; r < aa; r++) {
					BlockPos blockPos7 = friendlyByteBuf.readBlockPos();
					beeInfo.blacklistedHives.add(blockPos7);
				}

				this.minecraft.debugRenderer.beeDebugRenderer.addOrUpdateBeeInfo(beeInfo);
			} else if (ClientboundCustomPayloadPacket.DEBUG_HIVE.equals(resourceLocation)) {
				BlockPos blockPos2 = friendlyByteBuf.readBlockPos();
				String string = friendlyByteBuf.readUtf();
				int m = friendlyByteBuf.readInt();
				int ab = friendlyByteBuf.readInt();
				boolean bl7 = friendlyByteBuf.readBoolean();
				BeeDebugRenderer.HiveInfo hiveInfo = new BeeDebugRenderer.HiveInfo(blockPos2, string, m, ab, bl7, this.level.getGameTime());
				this.minecraft.debugRenderer.beeDebugRenderer.addOrUpdateHiveInfo(hiveInfo);
			} else if (ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_CLEAR.equals(resourceLocation)) {
				this.minecraft.debugRenderer.gameTestDebugRenderer.clear();
			} else if (ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_ADD_MARKER.equals(resourceLocation)) {
				BlockPos blockPos2 = friendlyByteBuf.readBlockPos();
				int j = friendlyByteBuf.readInt();
				String string11 = friendlyByteBuf.readUtf();
				int ab = friendlyByteBuf.readInt();
				this.minecraft.debugRenderer.gameTestDebugRenderer.addMarker(blockPos2, j, string11, ab);
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
		PlayerTeam playerTeam;
		if (clientboundSetPlayerTeamPacket.getMethod() == 0) {
			playerTeam = scoreboard.addPlayerTeam(clientboundSetPlayerTeamPacket.getName());
		} else {
			playerTeam = scoreboard.getPlayerTeam(clientboundSetPlayerTeamPacket.getName());
		}

		if (clientboundSetPlayerTeamPacket.getMethod() == 0 || clientboundSetPlayerTeamPacket.getMethod() == 2) {
			playerTeam.setDisplayName(clientboundSetPlayerTeamPacket.getDisplayName());
			playerTeam.setColor(clientboundSetPlayerTeamPacket.getColor());
			playerTeam.unpackOptions(clientboundSetPlayerTeamPacket.getOptions());
			Team.Visibility visibility = Team.Visibility.byName(clientboundSetPlayerTeamPacket.getNametagVisibility());
			if (visibility != null) {
				playerTeam.setNameTagVisibility(visibility);
			}

			Team.CollisionRule collisionRule = Team.CollisionRule.byName(clientboundSetPlayerTeamPacket.getCollisionRule());
			if (collisionRule != null) {
				playerTeam.setCollisionRule(collisionRule);
			}

			playerTeam.setPlayerPrefix(clientboundSetPlayerTeamPacket.getPlayerPrefix());
			playerTeam.setPlayerSuffix(clientboundSetPlayerTeamPacket.getPlayerSuffix());
		}

		if (clientboundSetPlayerTeamPacket.getMethod() == 0 || clientboundSetPlayerTeamPacket.getMethod() == 3) {
			for (String string : clientboundSetPlayerTeamPacket.getPlayers()) {
				scoreboard.addPlayerToTeam(string, playerTeam);
			}
		}

		if (clientboundSetPlayerTeamPacket.getMethod() == 4) {
			for (String string : clientboundSetPlayerTeamPacket.getPlayers()) {
				scoreboard.removePlayerFromTeam(string, playerTeam);
			}
		}

		if (clientboundSetPlayerTeamPacket.getMethod() == 1) {
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
						LOGGER.warn("Entity {} does not have attribute {}", entity, Registry.ATTRIBUTES.getKey(attributeSnapshot.getAttribute()));
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
		if (abstractContainerMenu.containerId == clientboundPlaceGhostRecipePacket.getContainerId() && abstractContainerMenu.isSynched(this.minecraft.player)) {
			this.recipeManager.byKey(clientboundPlaceGhostRecipePacket.getRecipe()).ifPresent(recipe -> {
				if (this.minecraft.screen instanceof RecipeUpdateListener) {
					RecipeBookComponent recipeBookComponent = ((RecipeUpdateListener)this.minecraft.screen).getRecipeBookComponent();
					recipeBookComponent.setupGhostRecipe(recipe, abstractContainerMenu.slots);
				}
			});
		}
	}

	@Override
	public void handleLightUpdatePacked(ClientboundLightUpdatePacket clientboundLightUpdatePacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundLightUpdatePacket, this, this.minecraft);
		int i = clientboundLightUpdatePacket.getX();
		int j = clientboundLightUpdatePacket.getZ();
		LevelLightEngine levelLightEngine = this.level.getChunkSource().getLightEngine();
		int k = clientboundLightUpdatePacket.getSkyYMask();
		int l = clientboundLightUpdatePacket.getEmptySkyYMask();
		Iterator<byte[]> iterator = clientboundLightUpdatePacket.getSkyUpdates().iterator();
		this.readSectionList(i, j, levelLightEngine, LightLayer.SKY, k, l, iterator);
		int m = clientboundLightUpdatePacket.getBlockYMask();
		int n = clientboundLightUpdatePacket.getEmptyBlockYMask();
		Iterator<byte[]> iterator2 = clientboundLightUpdatePacket.getBlockUpdates().iterator();
		this.readSectionList(i, j, levelLightEngine, LightLayer.BLOCK, m, n, iterator2);
	}

	@Override
	public void handleMerchantOffers(ClientboundMerchantOffersPacket clientboundMerchantOffersPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundMerchantOffersPacket, this, this.minecraft);
		AbstractContainerMenu abstractContainerMenu = this.minecraft.player.containerMenu;
		if (clientboundMerchantOffersPacket.getContainerId() == abstractContainerMenu.containerId && abstractContainerMenu instanceof MerchantMenu) {
			((MerchantMenu)abstractContainerMenu).setOffers(new MerchantOffers(clientboundMerchantOffersPacket.getOffers().createTag()));
			((MerchantMenu)abstractContainerMenu).setXp(clientboundMerchantOffersPacket.getVillagerXp());
			((MerchantMenu)abstractContainerMenu).setMerchantLevel(clientboundMerchantOffersPacket.getVillagerLevel());
			((MerchantMenu)abstractContainerMenu).setShowProgressBar(clientboundMerchantOffersPacket.showProgress());
			((MerchantMenu)abstractContainerMenu).setCanRestock(clientboundMerchantOffersPacket.canRestock());
		}
	}

	@Override
	public void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket clientboundSetChunkCacheRadiusPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetChunkCacheRadiusPacket, this, this.minecraft);
		this.serverChunkRadius = clientboundSetChunkCacheRadiusPacket.getRadius();
		this.level.getChunkSource().updateViewRadius(clientboundSetChunkCacheRadiusPacket.getRadius());
	}

	@Override
	public void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket clientboundSetChunkCacheCenterPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundSetChunkCacheCenterPacket, this, this.minecraft);
		this.level.getChunkSource().updateViewCenter(clientboundSetChunkCacheCenterPacket.getX(), clientboundSetChunkCacheCenterPacket.getZ());
	}

	@Override
	public void handleBlockBreakAck(ClientboundBlockBreakAckPacket clientboundBlockBreakAckPacket) {
		PacketUtils.ensureRunningOnSameThread(clientboundBlockBreakAckPacket, this, this.minecraft);
		this.minecraft
			.gameMode
			.handleBlockBreakAck(
				this.level,
				clientboundBlockBreakAckPacket.getPos(),
				clientboundBlockBreakAckPacket.getState(),
				clientboundBlockBreakAckPacket.action(),
				clientboundBlockBreakAckPacket.allGood()
			);
	}

	private void readSectionList(int i, int j, LevelLightEngine levelLightEngine, LightLayer lightLayer, int k, int l, Iterator<byte[]> iterator) {
		for (int m = 0; m < 18; m++) {
			int n = -1 + m;
			boolean bl = (k & 1 << m) != 0;
			boolean bl2 = (l & 1 << m) != 0;
			if (bl || bl2) {
				levelLightEngine.queueSectionData(lightLayer, SectionPos.of(i, n, j), bl ? new DataLayer((byte[])((byte[])iterator.next()).clone()) : new DataLayer());
				this.level.setSectionDirtyWithNeighbors(i, n, j);
			}
		}
	}

	@Override
	public Connection getConnection() {
		return this.connection;
	}

	public Collection<PlayerInfo> getOnlinePlayers() {
		return this.playerInfoMap.values();
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

	public TagManager getTags() {
		return this.tags;
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
		return this.registryAccess;
	}
}

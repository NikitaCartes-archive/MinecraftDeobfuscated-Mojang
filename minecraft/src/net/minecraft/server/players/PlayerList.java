package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.DemoMode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class PlayerList {
	public static final File USERBANLIST_FILE = new File("banned-players.json");
	public static final File IPBANLIST_FILE = new File("banned-ips.json");
	public static final File OPLIST_FILE = new File("ops.json");
	public static final File WHITELIST_FILE = new File("whitelist.json");
	private static final Logger LOGGER = LogManager.getLogger();
	private static final SimpleDateFormat BAN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
	private final MinecraftServer server;
	private final List<ServerPlayer> players = Lists.<ServerPlayer>newArrayList();
	private final Map<UUID, ServerPlayer> playersByUUID = Maps.<UUID, ServerPlayer>newHashMap();
	private final UserBanList bans = new UserBanList(USERBANLIST_FILE);
	private final IpBanList ipBans = new IpBanList(IPBANLIST_FILE);
	private final ServerOpList ops = new ServerOpList(OPLIST_FILE);
	private final UserWhiteList whitelist = new UserWhiteList(WHITELIST_FILE);
	private final Map<UUID, ServerStatsCounter> stats = Maps.<UUID, ServerStatsCounter>newHashMap();
	private final Map<UUID, PlayerAdvancements> advancements = Maps.<UUID, PlayerAdvancements>newHashMap();
	private final PlayerDataStorage playerIo;
	private boolean doWhiteList;
	private final RegistryAccess.RegistryHolder registryHolder;
	protected final int maxPlayers;
	private int viewDistance;
	private GameType overrideGameMode;
	private boolean allowCheatsForAllPlayers;
	private int sendAllPlayerInfoIn;

	public PlayerList(MinecraftServer minecraftServer, RegistryAccess.RegistryHolder registryHolder, PlayerDataStorage playerDataStorage, int i) {
		this.server = minecraftServer;
		this.registryHolder = registryHolder;
		this.maxPlayers = i;
		this.playerIo = playerDataStorage;
		this.getBans().setEnabled(true);
		this.getIpBans().setEnabled(true);
	}

	public void placeNewPlayer(Connection connection, ServerPlayer serverPlayer) {
		GameProfile gameProfile = serverPlayer.getGameProfile();
		GameProfileCache gameProfileCache = this.server.getProfileCache();
		GameProfile gameProfile2 = gameProfileCache.get(gameProfile.getId());
		String string = gameProfile2 == null ? gameProfile.getName() : gameProfile2.getName();
		gameProfileCache.add(gameProfile);
		CompoundTag compoundTag = this.load(serverPlayer);
		ResourceKey<DimensionType> resourceKey = compoundTag != null
			? (ResourceKey)DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, compoundTag.get("Dimension")))
				.resultOrPartial(LOGGER::error)
				.orElse(DimensionType.OVERWORLD_LOCATION)
			: DimensionType.OVERWORLD_LOCATION;
		ServerLevel serverLevel = this.server.getLevel(resourceKey);
		serverPlayer.setLevel(serverLevel);
		serverPlayer.gameMode.setLevel((ServerLevel)serverPlayer.level);
		String string2 = "local";
		if (connection.getRemoteAddress() != null) {
			string2 = connection.getRemoteAddress().toString();
		}

		LOGGER.info(
			"{}[{}] logged in with entity id {} at ({}, {}, {})",
			serverPlayer.getName().getString(),
			string2,
			serverPlayer.getId(),
			serverPlayer.getX(),
			serverPlayer.getY(),
			serverPlayer.getZ()
		);
		LevelData levelData = serverLevel.getLevelData();
		this.updatePlayerGameMode(serverPlayer, null, serverLevel);
		ServerGamePacketListenerImpl serverGamePacketListenerImpl = new ServerGamePacketListenerImpl(this.server, connection, serverPlayer);
		GameRules gameRules = serverLevel.getGameRules();
		boolean bl = gameRules.getBoolean(GameRules.RULE_DO_IMMEDIATE_RESPAWN);
		boolean bl2 = gameRules.getBoolean(GameRules.RULE_REDUCEDDEBUGINFO);
		serverGamePacketListenerImpl.send(
			new ClientboundLoginPacket(
				serverPlayer.getId(),
				serverPlayer.gameMode.getGameModeForPlayer(),
				BiomeManager.obfuscateSeed(serverLevel.getSeed()),
				levelData.isHardcore(),
				this.registryHolder,
				serverLevel.dimension().location(),
				this.getMaxPlayers(),
				this.viewDistance,
				bl2,
				!bl,
				serverLevel.isDebug(),
				serverLevel.isFlat()
			)
		);
		serverGamePacketListenerImpl.send(
			new ClientboundCustomPayloadPacket(
				ClientboundCustomPayloadPacket.BRAND, new FriendlyByteBuf(Unpooled.buffer()).writeUtf(this.getServer().getServerModName())
			)
		);
		serverGamePacketListenerImpl.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
		serverGamePacketListenerImpl.send(new ClientboundPlayerAbilitiesPacket(serverPlayer.abilities));
		serverGamePacketListenerImpl.send(new ClientboundSetCarriedItemPacket(serverPlayer.inventory.selected));
		serverGamePacketListenerImpl.send(new ClientboundUpdateRecipesPacket(this.server.getRecipeManager().getRecipes()));
		serverGamePacketListenerImpl.send(new ClientboundUpdateTagsPacket(this.server.getTags()));
		this.sendPlayerPermissionLevel(serverPlayer);
		serverPlayer.getStats().markAllDirty();
		serverPlayer.getRecipeBook().sendInitialRecipeBook(serverPlayer);
		this.updateEntireScoreboard(serverLevel.getScoreboard(), serverPlayer);
		this.server.invalidateStatus();
		MutableComponent mutableComponent;
		if (serverPlayer.getGameProfile().getName().equalsIgnoreCase(string)) {
			mutableComponent = new TranslatableComponent("multiplayer.player.joined", serverPlayer.getDisplayName());
		} else {
			mutableComponent = new TranslatableComponent("multiplayer.player.joined.renamed", serverPlayer.getDisplayName(), string);
		}

		this.broadcastMessage(mutableComponent.withStyle(ChatFormatting.YELLOW), ChatType.SYSTEM, Util.NIL_UUID);
		serverGamePacketListenerImpl.teleport(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), serverPlayer.yRot, serverPlayer.xRot);
		this.players.add(serverPlayer);
		this.playersByUUID.put(serverPlayer.getUUID(), serverPlayer);
		this.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, serverPlayer));

		for (int i = 0; i < this.players.size(); i++) {
			serverPlayer.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, (ServerPlayer)this.players.get(i)));
		}

		serverLevel.addNewPlayer(serverPlayer);
		this.server.getCustomBossEvents().onPlayerConnect(serverPlayer);
		this.sendLevelInfo(serverPlayer, serverLevel);
		if (!this.server.getResourcePack().isEmpty()) {
			serverPlayer.sendTexturePack(this.server.getResourcePack(), this.server.getResourcePackHash());
		}

		for (MobEffectInstance mobEffectInstance : serverPlayer.getActiveEffects()) {
			serverGamePacketListenerImpl.send(new ClientboundUpdateMobEffectPacket(serverPlayer.getId(), mobEffectInstance));
		}

		if (compoundTag != null && compoundTag.contains("RootVehicle", 10)) {
			CompoundTag compoundTag2 = compoundTag.getCompound("RootVehicle");
			Entity entity = EntityType.loadEntityRecursive(
				compoundTag2.getCompound("Entity"), serverLevel, entityx -> !serverLevel.addWithUUID(entityx) ? null : entityx
			);
			if (entity != null) {
				UUID uUID;
				if (compoundTag2.hasUUID("Attach")) {
					uUID = compoundTag2.getUUID("Attach");
				} else {
					uUID = null;
				}

				if (entity.getUUID().equals(uUID)) {
					serverPlayer.startRiding(entity, true);
				} else {
					for (Entity entity2 : entity.getIndirectPassengers()) {
						if (entity2.getUUID().equals(uUID)) {
							serverPlayer.startRiding(entity2, true);
							break;
						}
					}
				}

				if (!serverPlayer.isPassenger()) {
					LOGGER.warn("Couldn't reattach entity to player");
					serverLevel.despawn(entity);

					for (Entity entity2x : entity.getIndirectPassengers()) {
						serverLevel.despawn(entity2x);
					}
				}
			}
		}

		serverPlayer.initMenu();
	}

	protected void updateEntireScoreboard(ServerScoreboard serverScoreboard, ServerPlayer serverPlayer) {
		Set<Objective> set = Sets.<Objective>newHashSet();

		for (PlayerTeam playerTeam : serverScoreboard.getPlayerTeams()) {
			serverPlayer.connection.send(new ClientboundSetPlayerTeamPacket(playerTeam, 0));
		}

		for (int i = 0; i < 19; i++) {
			Objective objective = serverScoreboard.getDisplayObjective(i);
			if (objective != null && !set.contains(objective)) {
				for (Packet<?> packet : serverScoreboard.getStartTrackingPackets(objective)) {
					serverPlayer.connection.send(packet);
				}

				set.add(objective);
			}
		}
	}

	public void setLevel(ServerLevel serverLevel) {
		serverLevel.getWorldBorder().addListener(new BorderChangeListener() {
			@Override
			public void onBorderSizeSet(WorldBorder worldBorder, double d) {
				PlayerList.this.broadcastAll(new ClientboundSetBorderPacket(worldBorder, ClientboundSetBorderPacket.Type.SET_SIZE));
			}

			@Override
			public void onBorderSizeLerping(WorldBorder worldBorder, double d, double e, long l) {
				PlayerList.this.broadcastAll(new ClientboundSetBorderPacket(worldBorder, ClientboundSetBorderPacket.Type.LERP_SIZE));
			}

			@Override
			public void onBorderCenterSet(WorldBorder worldBorder, double d, double e) {
				PlayerList.this.broadcastAll(new ClientboundSetBorderPacket(worldBorder, ClientboundSetBorderPacket.Type.SET_CENTER));
			}

			@Override
			public void onBorderSetWarningTime(WorldBorder worldBorder, int i) {
				PlayerList.this.broadcastAll(new ClientboundSetBorderPacket(worldBorder, ClientboundSetBorderPacket.Type.SET_WARNING_TIME));
			}

			@Override
			public void onBorderSetWarningBlocks(WorldBorder worldBorder, int i) {
				PlayerList.this.broadcastAll(new ClientboundSetBorderPacket(worldBorder, ClientboundSetBorderPacket.Type.SET_WARNING_BLOCKS));
			}

			@Override
			public void onBorderSetDamagePerBlock(WorldBorder worldBorder, double d) {
			}

			@Override
			public void onBorderSetDamageSafeZOne(WorldBorder worldBorder, double d) {
			}
		});
	}

	@Nullable
	public CompoundTag load(ServerPlayer serverPlayer) {
		CompoundTag compoundTag = this.server.getWorldData().getLoadedPlayerTag();
		CompoundTag compoundTag2;
		if (serverPlayer.getName().getString().equals(this.server.getSingleplayerName()) && compoundTag != null) {
			compoundTag2 = compoundTag;
			serverPlayer.load(compoundTag);
			LOGGER.debug("loading single player");
		} else {
			compoundTag2 = this.playerIo.load(serverPlayer);
		}

		return compoundTag2;
	}

	protected void save(ServerPlayer serverPlayer) {
		this.playerIo.save(serverPlayer);
		ServerStatsCounter serverStatsCounter = (ServerStatsCounter)this.stats.get(serverPlayer.getUUID());
		if (serverStatsCounter != null) {
			serverStatsCounter.save();
		}

		PlayerAdvancements playerAdvancements = (PlayerAdvancements)this.advancements.get(serverPlayer.getUUID());
		if (playerAdvancements != null) {
			playerAdvancements.save();
		}
	}

	public void remove(ServerPlayer serverPlayer) {
		ServerLevel serverLevel = serverPlayer.getLevel();
		serverPlayer.awardStat(Stats.LEAVE_GAME);
		this.save(serverPlayer);
		if (serverPlayer.isPassenger()) {
			Entity entity = serverPlayer.getRootVehicle();
			if (entity.hasOnePlayerPassenger()) {
				LOGGER.debug("Removing player mount");
				serverPlayer.stopRiding();
				serverLevel.despawn(entity);
				entity.removed = true;

				for (Entity entity2 : entity.getIndirectPassengers()) {
					serverLevel.despawn(entity2);
					entity2.removed = true;
				}

				serverLevel.getChunk(serverPlayer.xChunk, serverPlayer.zChunk).markUnsaved();
			}
		}

		serverPlayer.unRide();
		serverLevel.removePlayerImmediately(serverPlayer);
		serverPlayer.getAdvancements().stopListening();
		this.players.remove(serverPlayer);
		this.server.getCustomBossEvents().onPlayerDisconnect(serverPlayer);
		UUID uUID = serverPlayer.getUUID();
		ServerPlayer serverPlayer2 = (ServerPlayer)this.playersByUUID.get(uUID);
		if (serverPlayer2 == serverPlayer) {
			this.playersByUUID.remove(uUID);
			this.stats.remove(uUID);
			this.advancements.remove(uUID);
		}

		this.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, serverPlayer));
	}

	@Nullable
	public Component canPlayerLogin(SocketAddress socketAddress, GameProfile gameProfile) {
		if (this.bans.isBanned(gameProfile)) {
			UserBanListEntry userBanListEntry = this.bans.get(gameProfile);
			MutableComponent mutableComponent = new TranslatableComponent("multiplayer.disconnect.banned.reason", userBanListEntry.getReason());
			if (userBanListEntry.getExpires() != null) {
				mutableComponent.append(new TranslatableComponent("multiplayer.disconnect.banned.expiration", BAN_DATE_FORMAT.format(userBanListEntry.getExpires())));
			}

			return mutableComponent;
		} else if (!this.isWhiteListed(gameProfile)) {
			return new TranslatableComponent("multiplayer.disconnect.not_whitelisted");
		} else if (this.ipBans.isBanned(socketAddress)) {
			IpBanListEntry ipBanListEntry = this.ipBans.get(socketAddress);
			MutableComponent mutableComponent = new TranslatableComponent("multiplayer.disconnect.banned_ip.reason", ipBanListEntry.getReason());
			if (ipBanListEntry.getExpires() != null) {
				mutableComponent.append(new TranslatableComponent("multiplayer.disconnect.banned_ip.expiration", BAN_DATE_FORMAT.format(ipBanListEntry.getExpires())));
			}

			return mutableComponent;
		} else {
			return this.players.size() >= this.maxPlayers && !this.canBypassPlayerLimit(gameProfile)
				? new TranslatableComponent("multiplayer.disconnect.server_full")
				: null;
		}
	}

	public ServerPlayer getPlayerForLogin(GameProfile gameProfile) {
		UUID uUID = Player.createPlayerUUID(gameProfile);
		List<ServerPlayer> list = Lists.<ServerPlayer>newArrayList();

		for (int i = 0; i < this.players.size(); i++) {
			ServerPlayer serverPlayer = (ServerPlayer)this.players.get(i);
			if (serverPlayer.getUUID().equals(uUID)) {
				list.add(serverPlayer);
			}
		}

		ServerPlayer serverPlayer2 = (ServerPlayer)this.playersByUUID.get(gameProfile.getId());
		if (serverPlayer2 != null && !list.contains(serverPlayer2)) {
			list.add(serverPlayer2);
		}

		for (ServerPlayer serverPlayer3 : list) {
			serverPlayer3.connection.disconnect(new TranslatableComponent("multiplayer.disconnect.duplicate_login"));
		}

		ServerPlayerGameMode serverPlayerGameMode;
		if (this.server.isDemo()) {
			serverPlayerGameMode = new DemoMode(this.server.getLevel(DimensionType.OVERWORLD_LOCATION));
		} else {
			serverPlayerGameMode = new ServerPlayerGameMode(this.server.getLevel(DimensionType.OVERWORLD_LOCATION));
		}

		return new ServerPlayer(this.server, this.server.getLevel(DimensionType.OVERWORLD_LOCATION), gameProfile, serverPlayerGameMode);
	}

	public ServerPlayer respawn(ServerPlayer serverPlayer, boolean bl) {
		this.players.remove(serverPlayer);
		serverPlayer.getLevel().removePlayerImmediately(serverPlayer);
		BlockPos blockPos = serverPlayer.getRespawnPosition();
		boolean bl2 = serverPlayer.isRespawnForced();
		Optional<Vec3> optional;
		if (blockPos != null) {
			optional = Player.findRespawnPositionAndUseSpawnBlock(this.server.getLevel(serverPlayer.getRespawnDimension()), blockPos, bl2, bl);
		} else {
			optional = Optional.empty();
		}

		ResourceKey<DimensionType> resourceKey = optional.isPresent() ? serverPlayer.getRespawnDimension() : DimensionType.OVERWORLD_LOCATION;
		ServerLevel serverLevel = this.server.getLevel(resourceKey);
		ServerPlayerGameMode serverPlayerGameMode;
		if (this.server.isDemo()) {
			serverPlayerGameMode = new DemoMode(serverLevel);
		} else {
			serverPlayerGameMode = new ServerPlayerGameMode(serverLevel);
		}

		ServerPlayer serverPlayer2 = new ServerPlayer(this.server, serverLevel, serverPlayer.getGameProfile(), serverPlayerGameMode);
		serverPlayer2.connection = serverPlayer.connection;
		serverPlayer2.restoreFrom(serverPlayer, bl);
		serverPlayer2.setId(serverPlayer.getId());
		serverPlayer2.setMainArm(serverPlayer.getMainArm());

		for (String string : serverPlayer.getTags()) {
			serverPlayer2.addTag(string);
		}

		this.updatePlayerGameMode(serverPlayer2, serverPlayer, serverLevel);
		boolean bl3 = false;
		if (optional.isPresent()) {
			Vec3 vec3 = (Vec3)optional.get();
			serverPlayer2.moveTo(vec3.x, vec3.y, vec3.z, 0.0F, 0.0F);
			serverPlayer2.setRespawnPosition(resourceKey, blockPos, bl2, false);
			bl3 = !bl && serverLevel.getBlockState(blockPos).getBlock() instanceof RespawnAnchorBlock;
		} else if (blockPos != null) {
			serverPlayer2.connection.send(new ClientboundGameEventPacket(0, 0.0F));
		}

		while (!serverLevel.noCollision(serverPlayer2) && serverPlayer2.getY() < 256.0) {
			serverPlayer2.setPos(serverPlayer2.getX(), serverPlayer2.getY() + 1.0, serverPlayer2.getZ());
		}

		LevelData levelData = serverPlayer2.level.getLevelData();
		serverPlayer2.connection
			.send(
				new ClientboundRespawnPacket(
					serverPlayer2.level.dimension().location(),
					BiomeManager.obfuscateSeed(serverPlayer2.getLevel().getSeed()),
					serverPlayer2.gameMode.getGameModeForPlayer(),
					serverPlayer2.getLevel().isDebug(),
					serverPlayer2.getLevel().isFlat(),
					bl
				)
			);
		serverPlayer2.connection.teleport(serverPlayer2.getX(), serverPlayer2.getY(), serverPlayer2.getZ(), serverPlayer2.yRot, serverPlayer2.xRot);
		serverPlayer2.connection.send(new ClientboundSetDefaultSpawnPositionPacket(serverLevel.getSharedSpawnPos()));
		serverPlayer2.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
		serverPlayer2.connection
			.send(new ClientboundSetExperiencePacket(serverPlayer2.experienceProgress, serverPlayer2.totalExperience, serverPlayer2.experienceLevel));
		this.sendLevelInfo(serverPlayer2, serverLevel);
		this.sendPlayerPermissionLevel(serverPlayer2);
		serverLevel.addRespawnedPlayer(serverPlayer2);
		this.players.add(serverPlayer2);
		this.playersByUUID.put(serverPlayer2.getUUID(), serverPlayer2);
		serverPlayer2.initMenu();
		serverPlayer2.setHealth(serverPlayer2.getHealth());
		if (bl3) {
			serverPlayer2.connection
				.send(
					new ClientboundSoundPacket(
						SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, (double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), 1.0F, 1.0F
					)
				);
		}

		return serverPlayer2;
	}

	public void sendPlayerPermissionLevel(ServerPlayer serverPlayer) {
		GameProfile gameProfile = serverPlayer.getGameProfile();
		int i = this.server.getProfilePermissions(gameProfile);
		this.sendPlayerPermissionLevel(serverPlayer, i);
	}

	public void tick() {
		if (++this.sendAllPlayerInfoIn > 600) {
			this.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.UPDATE_LATENCY, this.players));
			this.sendAllPlayerInfoIn = 0;
		}
	}

	public void broadcastAll(Packet<?> packet) {
		for (int i = 0; i < this.players.size(); i++) {
			((ServerPlayer)this.players.get(i)).connection.send(packet);
		}
	}

	public void broadcastAll(Packet<?> packet, ResourceKey<DimensionType> resourceKey) {
		for (int i = 0; i < this.players.size(); i++) {
			ServerPlayer serverPlayer = (ServerPlayer)this.players.get(i);
			if (serverPlayer.level.dimension() == resourceKey) {
				serverPlayer.connection.send(packet);
			}
		}
	}

	public void broadcastToTeam(Player player, Component component) {
		Team team = player.getTeam();
		if (team != null) {
			for (String string : team.getPlayers()) {
				ServerPlayer serverPlayer = this.getPlayerByName(string);
				if (serverPlayer != null && serverPlayer != player) {
					serverPlayer.sendMessage(component, player.getUUID());
				}
			}
		}
	}

	public void broadcastToAllExceptTeam(Player player, Component component) {
		Team team = player.getTeam();
		if (team == null) {
			this.broadcastMessage(component, ChatType.SYSTEM, player.getUUID());
		} else {
			for (int i = 0; i < this.players.size(); i++) {
				ServerPlayer serverPlayer = (ServerPlayer)this.players.get(i);
				if (serverPlayer.getTeam() != team) {
					serverPlayer.sendMessage(component, player.getUUID());
				}
			}
		}
	}

	public String[] getPlayerNamesArray() {
		String[] strings = new String[this.players.size()];

		for (int i = 0; i < this.players.size(); i++) {
			strings[i] = ((ServerPlayer)this.players.get(i)).getGameProfile().getName();
		}

		return strings;
	}

	public UserBanList getBans() {
		return this.bans;
	}

	public IpBanList getIpBans() {
		return this.ipBans;
	}

	public void op(GameProfile gameProfile) {
		this.ops.add(new ServerOpListEntry(gameProfile, this.server.getOperatorUserPermissionLevel(), this.ops.canBypassPlayerLimit(gameProfile)));
		ServerPlayer serverPlayer = this.getPlayer(gameProfile.getId());
		if (serverPlayer != null) {
			this.sendPlayerPermissionLevel(serverPlayer);
		}
	}

	public void deop(GameProfile gameProfile) {
		this.ops.remove(gameProfile);
		ServerPlayer serverPlayer = this.getPlayer(gameProfile.getId());
		if (serverPlayer != null) {
			this.sendPlayerPermissionLevel(serverPlayer);
		}
	}

	private void sendPlayerPermissionLevel(ServerPlayer serverPlayer, int i) {
		if (serverPlayer.connection != null) {
			byte b;
			if (i <= 0) {
				b = 24;
			} else if (i >= 4) {
				b = 28;
			} else {
				b = (byte)(24 + i);
			}

			serverPlayer.connection.send(new ClientboundEntityEventPacket(serverPlayer, b));
		}

		this.server.getCommands().sendCommands(serverPlayer);
	}

	public boolean isWhiteListed(GameProfile gameProfile) {
		return !this.doWhiteList || this.ops.contains(gameProfile) || this.whitelist.contains(gameProfile);
	}

	public boolean isOp(GameProfile gameProfile) {
		return this.ops.contains(gameProfile)
			|| this.server.isSingleplayerOwner(gameProfile) && this.server.getWorldData().getAllowCommands()
			|| this.allowCheatsForAllPlayers;
	}

	@Nullable
	public ServerPlayer getPlayerByName(String string) {
		for (ServerPlayer serverPlayer : this.players) {
			if (serverPlayer.getGameProfile().getName().equalsIgnoreCase(string)) {
				return serverPlayer;
			}
		}

		return null;
	}

	public void broadcast(@Nullable Player player, double d, double e, double f, double g, ResourceKey<DimensionType> resourceKey, Packet<?> packet) {
		for (int i = 0; i < this.players.size(); i++) {
			ServerPlayer serverPlayer = (ServerPlayer)this.players.get(i);
			if (serverPlayer != player && serverPlayer.level.dimension() == resourceKey) {
				double h = d - serverPlayer.getX();
				double j = e - serverPlayer.getY();
				double k = f - serverPlayer.getZ();
				if (h * h + j * j + k * k < g * g) {
					serverPlayer.connection.send(packet);
				}
			}
		}
	}

	public void saveAll() {
		for (int i = 0; i < this.players.size(); i++) {
			this.save((ServerPlayer)this.players.get(i));
		}
	}

	public UserWhiteList getWhiteList() {
		return this.whitelist;
	}

	public String[] getWhiteListNames() {
		return this.whitelist.getUserList();
	}

	public ServerOpList getOps() {
		return this.ops;
	}

	public String[] getOpNames() {
		return this.ops.getUserList();
	}

	public void reloadWhiteList() {
	}

	public void sendLevelInfo(ServerPlayer serverPlayer, ServerLevel serverLevel) {
		WorldBorder worldBorder = this.server.getLevel(DimensionType.OVERWORLD_LOCATION).getWorldBorder();
		serverPlayer.connection.send(new ClientboundSetBorderPacket(worldBorder, ClientboundSetBorderPacket.Type.INITIALIZE));
		serverPlayer.connection
			.send(new ClientboundSetTimePacket(serverLevel.getGameTime(), serverLevel.getDayTime(), serverLevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)));
		serverPlayer.connection.send(new ClientboundSetDefaultSpawnPositionPacket(serverLevel.getSharedSpawnPos()));
		if (serverLevel.isRaining()) {
			serverPlayer.connection.send(new ClientboundGameEventPacket(1, 0.0F));
			serverPlayer.connection.send(new ClientboundGameEventPacket(7, serverLevel.getRainLevel(1.0F)));
			serverPlayer.connection.send(new ClientboundGameEventPacket(8, serverLevel.getThunderLevel(1.0F)));
		}
	}

	public void sendAllPlayerInfo(ServerPlayer serverPlayer) {
		serverPlayer.refreshContainer(serverPlayer.inventoryMenu);
		serverPlayer.resetSentInfo();
		serverPlayer.connection.send(new ClientboundSetCarriedItemPacket(serverPlayer.inventory.selected));
	}

	public int getPlayerCount() {
		return this.players.size();
	}

	public int getMaxPlayers() {
		return this.maxPlayers;
	}

	public boolean isUsingWhitelist() {
		return this.doWhiteList;
	}

	public void setUsingWhiteList(boolean bl) {
		this.doWhiteList = bl;
	}

	public List<ServerPlayer> getPlayersWithAddress(String string) {
		List<ServerPlayer> list = Lists.<ServerPlayer>newArrayList();

		for (ServerPlayer serverPlayer : this.players) {
			if (serverPlayer.getIpAddress().equals(string)) {
				list.add(serverPlayer);
			}
		}

		return list;
	}

	public int getViewDistance() {
		return this.viewDistance;
	}

	public MinecraftServer getServer() {
		return this.server;
	}

	public CompoundTag getSingleplayerData() {
		return null;
	}

	@Environment(EnvType.CLIENT)
	public void setOverrideGameMode(GameType gameType) {
		this.overrideGameMode = gameType;
	}

	private void updatePlayerGameMode(ServerPlayer serverPlayer, ServerPlayer serverPlayer2, ServerLevel serverLevel) {
		if (serverPlayer2 != null) {
			serverPlayer.gameMode.setGameModeForPlayer(serverPlayer2.gameMode.getGameModeForPlayer());
		} else if (this.overrideGameMode != null) {
			serverPlayer.gameMode.setGameModeForPlayer(this.overrideGameMode);
		}

		serverPlayer.gameMode.updateGameMode(serverLevel.getServer().getWorldData().getGameType());
	}

	@Environment(EnvType.CLIENT)
	public void setAllowCheatsForAllPlayers(boolean bl) {
		this.allowCheatsForAllPlayers = bl;
	}

	public void removeAll() {
		for (int i = 0; i < this.players.size(); i++) {
			((ServerPlayer)this.players.get(i)).connection.disconnect(new TranslatableComponent("multiplayer.disconnect.server_shutdown"));
		}
	}

	public void broadcastMessage(Component component, ChatType chatType, UUID uUID) {
		this.server.sendMessage(component, uUID);
		this.broadcastAll(new ClientboundChatPacket(component, chatType, uUID));
	}

	public ServerStatsCounter getPlayerStats(Player player) {
		UUID uUID = player.getUUID();
		ServerStatsCounter serverStatsCounter = uUID == null ? null : (ServerStatsCounter)this.stats.get(uUID);
		if (serverStatsCounter == null) {
			File file = this.server.getWorldPath(LevelResource.PLAYER_STATS_DIR).toFile();
			File file2 = new File(file, uUID + ".json");
			if (!file2.exists()) {
				File file3 = new File(file, player.getName().getString() + ".json");
				if (file3.exists() && file3.isFile()) {
					file3.renameTo(file2);
				}
			}

			serverStatsCounter = new ServerStatsCounter(this.server, file2);
			this.stats.put(uUID, serverStatsCounter);
		}

		return serverStatsCounter;
	}

	public PlayerAdvancements getPlayerAdvancements(ServerPlayer serverPlayer) {
		UUID uUID = serverPlayer.getUUID();
		PlayerAdvancements playerAdvancements = (PlayerAdvancements)this.advancements.get(uUID);
		if (playerAdvancements == null) {
			File file = this.server.getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR).toFile();
			File file2 = new File(file, uUID + ".json");
			playerAdvancements = new PlayerAdvancements(this.server, file2, serverPlayer);
			this.advancements.put(uUID, playerAdvancements);
		}

		playerAdvancements.setPlayer(serverPlayer);
		return playerAdvancements;
	}

	public void setViewDistance(int i) {
		this.viewDistance = i;
		this.broadcastAll(new ClientboundSetChunkCacheRadiusPacket(i));

		for (ServerLevel serverLevel : this.server.getAllLevels()) {
			if (serverLevel != null) {
				serverLevel.getChunkSource().setViewDistance(i);
			}
		}
	}

	public List<ServerPlayer> getPlayers() {
		return this.players;
	}

	@Nullable
	public ServerPlayer getPlayer(UUID uUID) {
		return (ServerPlayer)this.playersByUUID.get(uUID);
	}

	public boolean canBypassPlayerLimit(GameProfile gameProfile) {
		return false;
	}

	public void reloadResources() {
		for (PlayerAdvancements playerAdvancements : this.advancements.values()) {
			playerAdvancements.reload();
		}

		this.broadcastAll(new ClientboundUpdateTagsPacket(this.server.getTags()));
		ClientboundUpdateRecipesPacket clientboundUpdateRecipesPacket = new ClientboundUpdateRecipesPacket(this.server.getRecipeManager().getRecipes());

		for (ServerPlayer serverPlayer : this.players) {
			serverPlayer.connection.send(clientboundUpdateRecipesPacket);
			serverPlayer.getRecipeBook().sendInitialRecipeBook(serverPlayer);
		}
	}

	public boolean isAllowCheatsForAllPlayers() {
		return this.allowCheatsForAllPlayers;
	}
}

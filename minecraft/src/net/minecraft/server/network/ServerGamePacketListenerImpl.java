package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.net.SocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.LastSeenMessagesValidator;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSignatureCache;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FutureChain;
import net.minecraft.util.Mth;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;

public class ServerGamePacketListenerImpl implements ServerPlayerConnection, TickablePacketListener, ServerGamePacketListener {
	static final Logger LOGGER = LogUtils.getLogger();
	private static final int LATENCY_CHECK_INTERVAL = 15000;
	public static final double MAX_INTERACTION_DISTANCE = Mth.square(6.0);
	private static final int NO_BLOCK_UPDATES_TO_ACK = -1;
	private static final int TRACKED_MESSAGE_DISCONNECT_THRESHOLD = 4096;
	private static final Component CHAT_VALIDATION_FAILED = Component.translatable("multiplayer.disconnect.chat_validation_failed");
	private final Connection connection;
	private final MinecraftServer server;
	public ServerPlayer player;
	private int tickCount;
	private int ackBlockChangesUpTo = -1;
	private long keepAliveTime;
	private boolean keepAlivePending;
	private long keepAliveChallenge;
	private int chatSpamTickCount;
	private int dropSpamTickCount;
	private double firstGoodX;
	private double firstGoodY;
	private double firstGoodZ;
	private double lastGoodX;
	private double lastGoodY;
	private double lastGoodZ;
	@Nullable
	private Entity lastVehicle;
	private double vehicleFirstGoodX;
	private double vehicleFirstGoodY;
	private double vehicleFirstGoodZ;
	private double vehicleLastGoodX;
	private double vehicleLastGoodY;
	private double vehicleLastGoodZ;
	@Nullable
	private Vec3 awaitingPositionFromClient;
	private int awaitingTeleport;
	private int awaitingTeleportTime;
	private boolean clientIsFloating;
	private int aboveGroundTickCount;
	private boolean clientVehicleIsFloating;
	private int aboveGroundVehicleTickCount;
	private int receivedMovePacketCount;
	private int knownMovePacketCount;
	private final AtomicReference<Instant> lastChatTimeStamp = new AtomicReference(Instant.EPOCH);
	@Nullable
	private RemoteChatSession chatSession;
	private SignedMessageChain.Decoder signedMessageDecoder;
	private final LastSeenMessagesValidator lastSeenMessages = new LastSeenMessagesValidator(20);
	private final MessageSignatureCache messageSignatureCache = MessageSignatureCache.createDefault();
	private final FutureChain chatMessageChain;

	public ServerGamePacketListenerImpl(MinecraftServer minecraftServer, Connection connection, ServerPlayer serverPlayer) {
		this.server = minecraftServer;
		this.connection = connection;
		connection.setListener(this);
		this.player = serverPlayer;
		serverPlayer.connection = this;
		this.keepAliveTime = Util.getMillis();
		serverPlayer.getTextFilter().join();
		this.signedMessageDecoder = minecraftServer.enforceSecureProfile()
			? SignedMessageChain.Decoder.REJECT_ALL
			: SignedMessageChain.Decoder.unsigned(serverPlayer.getUUID());
		this.chatMessageChain = new FutureChain(minecraftServer);
	}

	@Override
	public void tick() {
		if (this.ackBlockChangesUpTo > -1) {
			this.send(new ClientboundBlockChangedAckPacket(this.ackBlockChangesUpTo));
			this.ackBlockChangesUpTo = -1;
		}

		this.resetPosition();
		this.player.xo = this.player.getX();
		this.player.yo = this.player.getY();
		this.player.zo = this.player.getZ();
		this.player.doTick();
		this.player.absMoveTo(this.firstGoodX, this.firstGoodY, this.firstGoodZ, this.player.getYRot(), this.player.getXRot());
		this.tickCount++;
		this.knownMovePacketCount = this.receivedMovePacketCount;
		if (this.clientIsFloating && !this.player.isSleeping() && !this.player.isPassenger() && !this.player.isDeadOrDying()) {
			if (++this.aboveGroundTickCount > 80) {
				LOGGER.warn("{} was kicked for floating too long!", this.player.getName().getString());
				this.disconnect(Component.translatable("multiplayer.disconnect.flying"));
				return;
			}
		} else {
			this.clientIsFloating = false;
			this.aboveGroundTickCount = 0;
		}

		this.lastVehicle = this.player.getRootVehicle();
		if (this.lastVehicle != this.player && this.lastVehicle.getControllingPassenger() == this.player) {
			this.vehicleFirstGoodX = this.lastVehicle.getX();
			this.vehicleFirstGoodY = this.lastVehicle.getY();
			this.vehicleFirstGoodZ = this.lastVehicle.getZ();
			this.vehicleLastGoodX = this.lastVehicle.getX();
			this.vehicleLastGoodY = this.lastVehicle.getY();
			this.vehicleLastGoodZ = this.lastVehicle.getZ();
			if (this.clientVehicleIsFloating && this.player.getRootVehicle().getControllingPassenger() == this.player) {
				if (++this.aboveGroundVehicleTickCount > 80) {
					LOGGER.warn("{} was kicked for floating a vehicle too long!", this.player.getName().getString());
					this.disconnect(Component.translatable("multiplayer.disconnect.flying"));
					return;
				}
			} else {
				this.clientVehicleIsFloating = false;
				this.aboveGroundVehicleTickCount = 0;
			}
		} else {
			this.lastVehicle = null;
			this.clientVehicleIsFloating = false;
			this.aboveGroundVehicleTickCount = 0;
		}

		this.server.getProfiler().push("keepAlive");
		long l = Util.getMillis();
		if (l - this.keepAliveTime >= 15000L) {
			if (this.keepAlivePending) {
				this.disconnect(Component.translatable("disconnect.timeout"));
			} else {
				this.keepAlivePending = true;
				this.keepAliveTime = l;
				this.keepAliveChallenge = l;
				this.send(new ClientboundKeepAlivePacket(this.keepAliveChallenge));
			}
		}

		this.server.getProfiler().pop();
		if (this.chatSpamTickCount > 0) {
			this.chatSpamTickCount--;
		}

		if (this.dropSpamTickCount > 0) {
			this.dropSpamTickCount--;
		}

		if (this.player.getLastActionTime() > 0L
			&& this.server.getPlayerIdleTimeout() > 0
			&& Util.getMillis() - this.player.getLastActionTime() > (long)this.server.getPlayerIdleTimeout() * 1000L * 60L) {
			this.disconnect(Component.translatable("multiplayer.disconnect.idling"));
		}
	}

	public void resetPosition() {
		this.firstGoodX = this.player.getX();
		this.firstGoodY = this.player.getY();
		this.firstGoodZ = this.player.getZ();
		this.lastGoodX = this.player.getX();
		this.lastGoodY = this.player.getY();
		this.lastGoodZ = this.player.getZ();
	}

	@Override
	public boolean isAcceptingMessages() {
		return this.connection.isConnected();
	}

	private boolean isSingleplayerOwner() {
		return this.server.isSingleplayerOwner(this.player.getGameProfile());
	}

	public void disconnect(Component component) {
		this.connection.send(new ClientboundDisconnectPacket(component), PacketSendListener.thenRun(() -> this.connection.disconnect(component)));
		this.connection.setReadOnly();
		this.server.executeBlocking(this.connection::handleDisconnection);
	}

	private <T, R> CompletableFuture<R> filterTextPacket(T object, BiFunction<TextFilter, T, CompletableFuture<R>> biFunction) {
		return ((CompletableFuture)biFunction.apply(this.player.getTextFilter(), object)).thenApply(objectx -> {
			if (!this.isAcceptingMessages()) {
				LOGGER.debug("Ignoring packet due to disconnection");
				throw new CancellationException("disconnected");
			} else {
				return objectx;
			}
		});
	}

	private CompletableFuture<FilteredText> filterTextPacket(String string) {
		return this.filterTextPacket(string, TextFilter::processStreamMessage);
	}

	private CompletableFuture<List<FilteredText>> filterTextPacket(List<String> list) {
		return this.filterTextPacket(list, TextFilter::processMessageBundle);
	}

	@Override
	public void handlePlayerInput(ServerboundPlayerInputPacket serverboundPlayerInputPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundPlayerInputPacket, this, this.player.getLevel());
		this.player
			.setPlayerInput(
				serverboundPlayerInputPacket.getXxa(),
				serverboundPlayerInputPacket.getZza(),
				serverboundPlayerInputPacket.isJumping(),
				serverboundPlayerInputPacket.isShiftKeyDown()
			);
	}

	private static boolean containsInvalidValues(double d, double e, double f, float g, float h) {
		return Double.isNaN(d) || Double.isNaN(e) || Double.isNaN(f) || !Floats.isFinite(h) || !Floats.isFinite(g);
	}

	private static double clampHorizontal(double d) {
		return Mth.clamp(d, -3.0E7, 3.0E7);
	}

	private static double clampVertical(double d) {
		return Mth.clamp(d, -2.0E7, 2.0E7);
	}

	@Override
	public void handleMoveVehicle(ServerboundMoveVehiclePacket serverboundMoveVehiclePacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundMoveVehiclePacket, this, this.player.getLevel());
		if (containsInvalidValues(
			serverboundMoveVehiclePacket.getX(),
			serverboundMoveVehiclePacket.getY(),
			serverboundMoveVehiclePacket.getZ(),
			serverboundMoveVehiclePacket.getYRot(),
			serverboundMoveVehiclePacket.getXRot()
		)) {
			this.disconnect(Component.translatable("multiplayer.disconnect.invalid_vehicle_movement"));
		} else {
			Entity entity = this.player.getRootVehicle();
			if (entity != this.player && entity.getControllingPassenger() == this.player && entity == this.lastVehicle) {
				ServerLevel serverLevel = this.player.getLevel();
				double d = entity.getX();
				double e = entity.getY();
				double f = entity.getZ();
				double g = clampHorizontal(serverboundMoveVehiclePacket.getX());
				double h = clampVertical(serverboundMoveVehiclePacket.getY());
				double i = clampHorizontal(serverboundMoveVehiclePacket.getZ());
				float j = Mth.wrapDegrees(serverboundMoveVehiclePacket.getYRot());
				float k = Mth.wrapDegrees(serverboundMoveVehiclePacket.getXRot());
				double l = g - this.vehicleFirstGoodX;
				double m = h - this.vehicleFirstGoodY;
				double n = i - this.vehicleFirstGoodZ;
				double o = entity.getDeltaMovement().lengthSqr();
				double p = l * l + m * m + n * n;
				if (p - o > 100.0 && !this.isSingleplayerOwner()) {
					LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}", entity.getName().getString(), this.player.getName().getString(), l, m, n);
					this.connection.send(new ClientboundMoveVehiclePacket(entity));
					return;
				}

				boolean bl = serverLevel.noCollision(entity, entity.getBoundingBox().deflate(0.0625));
				l = g - this.vehicleLastGoodX;
				m = h - this.vehicleLastGoodY - 1.0E-6;
				n = i - this.vehicleLastGoodZ;
				boolean bl2 = entity.verticalCollisionBelow;
				entity.move(MoverType.PLAYER, new Vec3(l, m, n));
				l = g - entity.getX();
				m = h - entity.getY();
				if (m > -0.5 || m < 0.5) {
					m = 0.0;
				}

				n = i - entity.getZ();
				p = l * l + m * m + n * n;
				boolean bl3 = false;
				if (p > 0.0625) {
					bl3 = true;
					LOGGER.warn("{} (vehicle of {}) moved wrongly! {}", entity.getName().getString(), this.player.getName().getString(), Math.sqrt(p));
				}

				entity.absMoveTo(g, h, i, j, k);
				boolean bl4 = serverLevel.noCollision(entity, entity.getBoundingBox().deflate(0.0625));
				if (bl && (bl3 || !bl4)) {
					entity.absMoveTo(d, e, f, j, k);
					this.connection.send(new ClientboundMoveVehiclePacket(entity));
					return;
				}

				this.player.getLevel().getChunkSource().move(this.player);
				this.player.checkMovementStatistics(this.player.getX() - d, this.player.getY() - e, this.player.getZ() - f);
				this.clientVehicleIsFloating = m >= -0.03125 && !bl2 && !this.server.isFlightAllowed() && !entity.isNoGravity() && this.noBlocksAround(entity);
				this.vehicleLastGoodX = entity.getX();
				this.vehicleLastGoodY = entity.getY();
				this.vehicleLastGoodZ = entity.getZ();
			}
		}
	}

	private boolean noBlocksAround(Entity entity) {
		return entity.level.getBlockStates(entity.getBoundingBox().inflate(0.0625).expandTowards(0.0, -0.55, 0.0)).allMatch(BlockBehaviour.BlockStateBase::isAir);
	}

	@Override
	public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket serverboundAcceptTeleportationPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundAcceptTeleportationPacket, this, this.player.getLevel());
		if (serverboundAcceptTeleportationPacket.getId() == this.awaitingTeleport) {
			if (this.awaitingPositionFromClient == null) {
				this.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
				return;
			}

			this.player
				.absMoveTo(
					this.awaitingPositionFromClient.x, this.awaitingPositionFromClient.y, this.awaitingPositionFromClient.z, this.player.getYRot(), this.player.getXRot()
				);
			this.lastGoodX = this.awaitingPositionFromClient.x;
			this.lastGoodY = this.awaitingPositionFromClient.y;
			this.lastGoodZ = this.awaitingPositionFromClient.z;
			if (this.player.isChangingDimension()) {
				this.player.hasChangedDimension();
			}

			this.awaitingPositionFromClient = null;
		}
	}

	@Override
	public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket serverboundRecipeBookSeenRecipePacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundRecipeBookSeenRecipePacket, this, this.player.getLevel());
		this.server.getRecipeManager().byKey(serverboundRecipeBookSeenRecipePacket.getRecipe()).ifPresent(this.player.getRecipeBook()::removeHighlight);
	}

	@Override
	public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket serverboundRecipeBookChangeSettingsPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundRecipeBookChangeSettingsPacket, this, this.player.getLevel());
		this.player
			.getRecipeBook()
			.setBookSetting(
				serverboundRecipeBookChangeSettingsPacket.getBookType(),
				serverboundRecipeBookChangeSettingsPacket.isOpen(),
				serverboundRecipeBookChangeSettingsPacket.isFiltering()
			);
	}

	@Override
	public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket serverboundSeenAdvancementsPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundSeenAdvancementsPacket, this, this.player.getLevel());
		if (serverboundSeenAdvancementsPacket.getAction() == ServerboundSeenAdvancementsPacket.Action.OPENED_TAB) {
			ResourceLocation resourceLocation = serverboundSeenAdvancementsPacket.getTab();
			Advancement advancement = this.server.getAdvancements().getAdvancement(resourceLocation);
			if (advancement != null) {
				this.player.getAdvancements().setSelectedTab(advancement);
			}
		}
	}

	@Override
	public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket serverboundCommandSuggestionPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundCommandSuggestionPacket, this, this.player.getLevel());
		StringReader stringReader = new StringReader(serverboundCommandSuggestionPacket.getCommand());
		if (stringReader.canRead() && stringReader.peek() == '/') {
			stringReader.skip();
		}

		ParseResults<CommandSourceStack> parseResults = this.server.getCommands().getDispatcher().parse(stringReader, this.player.createCommandSourceStack());
		this.server
			.getCommands()
			.getDispatcher()
			.getCompletionSuggestions(parseResults)
			.thenAccept(suggestions -> this.connection.send(new ClientboundCommandSuggestionsPacket(serverboundCommandSuggestionPacket.getId(), suggestions)));
	}

	@Override
	public void handleSetCommandBlock(ServerboundSetCommandBlockPacket serverboundSetCommandBlockPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundSetCommandBlockPacket, this, this.player.getLevel());
		if (!this.server.isCommandBlockEnabled()) {
			this.player.sendSystemMessage(Component.translatable("advMode.notEnabled"));
		} else if (!this.player.canUseGameMasterBlocks()) {
			this.player.sendSystemMessage(Component.translatable("advMode.notAllowed"));
		} else {
			BaseCommandBlock baseCommandBlock = null;
			CommandBlockEntity commandBlockEntity = null;
			BlockPos blockPos = serverboundSetCommandBlockPacket.getPos();
			BlockEntity blockEntity = this.player.level.getBlockEntity(blockPos);
			if (blockEntity instanceof CommandBlockEntity) {
				commandBlockEntity = (CommandBlockEntity)blockEntity;
				baseCommandBlock = commandBlockEntity.getCommandBlock();
			}

			String string = serverboundSetCommandBlockPacket.getCommand();
			boolean bl = serverboundSetCommandBlockPacket.isTrackOutput();
			if (baseCommandBlock != null) {
				CommandBlockEntity.Mode mode = commandBlockEntity.getMode();
				BlockState blockState = this.player.level.getBlockState(blockPos);
				Direction direction = blockState.getValue(CommandBlock.FACING);

				BlockState blockState3 = (switch (serverboundSetCommandBlockPacket.getMode()) {
					case SEQUENCE -> Blocks.CHAIN_COMMAND_BLOCK.defaultBlockState();
					case AUTO -> Blocks.REPEATING_COMMAND_BLOCK.defaultBlockState();
					default -> Blocks.COMMAND_BLOCK.defaultBlockState();
				}).setValue(CommandBlock.FACING, direction).setValue(CommandBlock.CONDITIONAL, Boolean.valueOf(serverboundSetCommandBlockPacket.isConditional()));
				if (blockState3 != blockState) {
					this.player.level.setBlock(blockPos, blockState3, 2);
					blockEntity.setBlockState(blockState3);
					this.player.level.getChunkAt(blockPos).setBlockEntity(blockEntity);
				}

				baseCommandBlock.setCommand(string);
				baseCommandBlock.setTrackOutput(bl);
				if (!bl) {
					baseCommandBlock.setLastOutput(null);
				}

				commandBlockEntity.setAutomatic(serverboundSetCommandBlockPacket.isAutomatic());
				if (mode != serverboundSetCommandBlockPacket.getMode()) {
					commandBlockEntity.onModeSwitch();
				}

				baseCommandBlock.onUpdated();
				if (!StringUtil.isNullOrEmpty(string)) {
					this.player.sendSystemMessage(Component.translatable("advMode.setCommand.success", string));
				}
			}
		}
	}

	@Override
	public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket serverboundSetCommandMinecartPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundSetCommandMinecartPacket, this, this.player.getLevel());
		if (!this.server.isCommandBlockEnabled()) {
			this.player.sendSystemMessage(Component.translatable("advMode.notEnabled"));
		} else if (!this.player.canUseGameMasterBlocks()) {
			this.player.sendSystemMessage(Component.translatable("advMode.notAllowed"));
		} else {
			BaseCommandBlock baseCommandBlock = serverboundSetCommandMinecartPacket.getCommandBlock(this.player.level);
			if (baseCommandBlock != null) {
				baseCommandBlock.setCommand(serverboundSetCommandMinecartPacket.getCommand());
				baseCommandBlock.setTrackOutput(serverboundSetCommandMinecartPacket.isTrackOutput());
				if (!serverboundSetCommandMinecartPacket.isTrackOutput()) {
					baseCommandBlock.setLastOutput(null);
				}

				baseCommandBlock.onUpdated();
				this.player.sendSystemMessage(Component.translatable("advMode.setCommand.success", serverboundSetCommandMinecartPacket.getCommand()));
			}
		}
	}

	@Override
	public void handlePickItem(ServerboundPickItemPacket serverboundPickItemPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundPickItemPacket, this, this.player.getLevel());
		this.player.getInventory().pickSlot(serverboundPickItemPacket.getSlot());
		this.player
			.connection
			.send(
				new ClientboundContainerSetSlotPacket(-2, 0, this.player.getInventory().selected, this.player.getInventory().getItem(this.player.getInventory().selected))
			);
		this.player
			.connection
			.send(
				new ClientboundContainerSetSlotPacket(-2, 0, serverboundPickItemPacket.getSlot(), this.player.getInventory().getItem(serverboundPickItemPacket.getSlot()))
			);
		this.player.connection.send(new ClientboundSetCarriedItemPacket(this.player.getInventory().selected));
	}

	@Override
	public void handleRenameItem(ServerboundRenameItemPacket serverboundRenameItemPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundRenameItemPacket, this, this.player.getLevel());
		if (this.player.containerMenu instanceof AnvilMenu anvilMenu) {
			if (!anvilMenu.stillValid(this.player)) {
				LOGGER.debug("Player {} interacted with invalid menu {}", this.player, anvilMenu);
				return;
			}

			String string = SharedConstants.filterText(serverboundRenameItemPacket.getName());
			if (string.length() <= 50) {
				anvilMenu.setItemName(string);
			}
		}
	}

	@Override
	public void handleSetBeaconPacket(ServerboundSetBeaconPacket serverboundSetBeaconPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundSetBeaconPacket, this, this.player.getLevel());
		if (this.player.containerMenu instanceof BeaconMenu beaconMenu) {
			if (!this.player.containerMenu.stillValid(this.player)) {
				LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
				return;
			}

			beaconMenu.updateEffects(serverboundSetBeaconPacket.getPrimary(), serverboundSetBeaconPacket.getSecondary());
		}
	}

	@Override
	public void handleSetStructureBlock(ServerboundSetStructureBlockPacket serverboundSetStructureBlockPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundSetStructureBlockPacket, this, this.player.getLevel());
		if (this.player.canUseGameMasterBlocks()) {
			BlockPos blockPos = serverboundSetStructureBlockPacket.getPos();
			BlockState blockState = this.player.level.getBlockState(blockPos);
			if (this.player.level.getBlockEntity(blockPos) instanceof StructureBlockEntity structureBlockEntity) {
				structureBlockEntity.setMode(serverboundSetStructureBlockPacket.getMode());
				structureBlockEntity.setStructureName(serverboundSetStructureBlockPacket.getName());
				structureBlockEntity.setStructurePos(serverboundSetStructureBlockPacket.getOffset());
				structureBlockEntity.setStructureSize(serverboundSetStructureBlockPacket.getSize());
				structureBlockEntity.setMirror(serverboundSetStructureBlockPacket.getMirror());
				structureBlockEntity.setRotation(serverboundSetStructureBlockPacket.getRotation());
				structureBlockEntity.setMetaData(serverboundSetStructureBlockPacket.getData());
				structureBlockEntity.setIgnoreEntities(serverboundSetStructureBlockPacket.isIgnoreEntities());
				structureBlockEntity.setShowAir(serverboundSetStructureBlockPacket.isShowAir());
				structureBlockEntity.setShowBoundingBox(serverboundSetStructureBlockPacket.isShowBoundingBox());
				structureBlockEntity.setIntegrity(serverboundSetStructureBlockPacket.getIntegrity());
				structureBlockEntity.setSeed(serverboundSetStructureBlockPacket.getSeed());
				if (structureBlockEntity.hasStructureName()) {
					String string = structureBlockEntity.getStructureName();
					if (serverboundSetStructureBlockPacket.getUpdateType() == StructureBlockEntity.UpdateType.SAVE_AREA) {
						if (structureBlockEntity.saveStructure()) {
							this.player.displayClientMessage(Component.translatable("structure_block.save_success", string), false);
						} else {
							this.player.displayClientMessage(Component.translatable("structure_block.save_failure", string), false);
						}
					} else if (serverboundSetStructureBlockPacket.getUpdateType() == StructureBlockEntity.UpdateType.LOAD_AREA) {
						if (!structureBlockEntity.isStructureLoadable()) {
							this.player.displayClientMessage(Component.translatable("structure_block.load_not_found", string), false);
						} else if (structureBlockEntity.loadStructure(this.player.getLevel())) {
							this.player.displayClientMessage(Component.translatable("structure_block.load_success", string), false);
						} else {
							this.player.displayClientMessage(Component.translatable("structure_block.load_prepare", string), false);
						}
					} else if (serverboundSetStructureBlockPacket.getUpdateType() == StructureBlockEntity.UpdateType.SCAN_AREA) {
						if (structureBlockEntity.detectSize()) {
							this.player.displayClientMessage(Component.translatable("structure_block.size_success", string), false);
						} else {
							this.player.displayClientMessage(Component.translatable("structure_block.size_failure"), false);
						}
					}
				} else {
					this.player.displayClientMessage(Component.translatable("structure_block.invalid_structure_name", serverboundSetStructureBlockPacket.getName()), false);
				}

				structureBlockEntity.setChanged();
				this.player.level.sendBlockUpdated(blockPos, blockState, blockState, 3);
			}
		}
	}

	@Override
	public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket serverboundSetJigsawBlockPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundSetJigsawBlockPacket, this, this.player.getLevel());
		if (this.player.canUseGameMasterBlocks()) {
			BlockPos blockPos = serverboundSetJigsawBlockPacket.getPos();
			BlockState blockState = this.player.level.getBlockState(blockPos);
			if (this.player.level.getBlockEntity(blockPos) instanceof JigsawBlockEntity jigsawBlockEntity) {
				jigsawBlockEntity.setName(serverboundSetJigsawBlockPacket.getName());
				jigsawBlockEntity.setTarget(serverboundSetJigsawBlockPacket.getTarget());
				jigsawBlockEntity.setPool(ResourceKey.create(Registries.TEMPLATE_POOL, serverboundSetJigsawBlockPacket.getPool()));
				jigsawBlockEntity.setFinalState(serverboundSetJigsawBlockPacket.getFinalState());
				jigsawBlockEntity.setJoint(serverboundSetJigsawBlockPacket.getJoint());
				jigsawBlockEntity.setChanged();
				this.player.level.sendBlockUpdated(blockPos, blockState, blockState, 3);
			}
		}
	}

	@Override
	public void handleJigsawGenerate(ServerboundJigsawGeneratePacket serverboundJigsawGeneratePacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundJigsawGeneratePacket, this, this.player.getLevel());
		if (this.player.canUseGameMasterBlocks()) {
			BlockPos blockPos = serverboundJigsawGeneratePacket.getPos();
			if (this.player.level.getBlockEntity(blockPos) instanceof JigsawBlockEntity jigsawBlockEntity) {
				jigsawBlockEntity.generate(this.player.getLevel(), serverboundJigsawGeneratePacket.levels(), serverboundJigsawGeneratePacket.keepJigsaws());
			}
		}
	}

	@Override
	public void handleSelectTrade(ServerboundSelectTradePacket serverboundSelectTradePacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundSelectTradePacket, this, this.player.getLevel());
		int i = serverboundSelectTradePacket.getItem();
		if (this.player.containerMenu instanceof MerchantMenu merchantMenu) {
			if (!merchantMenu.stillValid(this.player)) {
				LOGGER.debug("Player {} interacted with invalid menu {}", this.player, merchantMenu);
				return;
			}

			merchantMenu.setSelectionHint(i);
			merchantMenu.tryMoveItems(i);
		}
	}

	@Override
	public void handleEditBook(ServerboundEditBookPacket serverboundEditBookPacket) {
		int i = serverboundEditBookPacket.getSlot();
		if (Inventory.isHotbarSlot(i) || i == 40) {
			List<String> list = Lists.<String>newArrayList();
			Optional<String> optional = serverboundEditBookPacket.getTitle();
			optional.ifPresent(list::add);
			serverboundEditBookPacket.getPages().stream().limit(100L).forEach(list::add);
			Consumer<List<FilteredText>> consumer = optional.isPresent()
				? listx -> this.signBook((FilteredText)listx.get(0), listx.subList(1, listx.size()), i)
				: listx -> this.updateBookContents(listx, i);
			this.filterTextPacket(list).thenAcceptAsync(consumer, this.server);
		}
	}

	private void updateBookContents(List<FilteredText> list, int i) {
		ItemStack itemStack = this.player.getInventory().getItem(i);
		if (itemStack.is(Items.WRITABLE_BOOK)) {
			this.updateBookPages(list, UnaryOperator.identity(), itemStack);
		}
	}

	private void signBook(FilteredText filteredText, List<FilteredText> list, int i) {
		ItemStack itemStack = this.player.getInventory().getItem(i);
		if (itemStack.is(Items.WRITABLE_BOOK)) {
			ItemStack itemStack2 = new ItemStack(Items.WRITTEN_BOOK);
			CompoundTag compoundTag = itemStack.getTag();
			if (compoundTag != null) {
				itemStack2.setTag(compoundTag.copy());
			}

			itemStack2.addTagElement("author", StringTag.valueOf(this.player.getName().getString()));
			if (this.player.isTextFilteringEnabled()) {
				itemStack2.addTagElement("title", StringTag.valueOf(filteredText.filteredOrEmpty()));
			} else {
				itemStack2.addTagElement("filtered_title", StringTag.valueOf(filteredText.filteredOrEmpty()));
				itemStack2.addTagElement("title", StringTag.valueOf(filteredText.raw()));
			}

			this.updateBookPages(list, string -> Component.Serializer.toJson(Component.literal(string)), itemStack2);
			this.player.getInventory().setItem(i, itemStack2);
		}
	}

	private void updateBookPages(List<FilteredText> list, UnaryOperator<String> unaryOperator, ItemStack itemStack) {
		ListTag listTag = new ListTag();
		if (this.player.isTextFilteringEnabled()) {
			list.stream().map(filteredTextx -> StringTag.valueOf((String)unaryOperator.apply(filteredTextx.filteredOrEmpty()))).forEach(listTag::add);
		} else {
			CompoundTag compoundTag = new CompoundTag();
			int i = 0;

			for (int j = list.size(); i < j; i++) {
				FilteredText filteredText = (FilteredText)list.get(i);
				String string = filteredText.raw();
				listTag.add(StringTag.valueOf((String)unaryOperator.apply(string)));
				if (filteredText.isFiltered()) {
					compoundTag.putString(String.valueOf(i), (String)unaryOperator.apply(filteredText.filteredOrEmpty()));
				}
			}

			if (!compoundTag.isEmpty()) {
				itemStack.addTagElement("filtered_pages", compoundTag);
			}
		}

		itemStack.addTagElement("pages", listTag);
	}

	@Override
	public void handleEntityTagQuery(ServerboundEntityTagQuery serverboundEntityTagQuery) {
		PacketUtils.ensureRunningOnSameThread(serverboundEntityTagQuery, this, this.player.getLevel());
		if (this.player.hasPermissions(2)) {
			Entity entity = this.player.getLevel().getEntity(serverboundEntityTagQuery.getEntityId());
			if (entity != null) {
				CompoundTag compoundTag = entity.saveWithoutId(new CompoundTag());
				this.player.connection.send(new ClientboundTagQueryPacket(serverboundEntityTagQuery.getTransactionId(), compoundTag));
			}
		}
	}

	@Override
	public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQuery serverboundBlockEntityTagQuery) {
		PacketUtils.ensureRunningOnSameThread(serverboundBlockEntityTagQuery, this, this.player.getLevel());
		if (this.player.hasPermissions(2)) {
			BlockEntity blockEntity = this.player.getLevel().getBlockEntity(serverboundBlockEntityTagQuery.getPos());
			CompoundTag compoundTag = blockEntity != null ? blockEntity.saveWithoutMetadata() : null;
			this.player.connection.send(new ClientboundTagQueryPacket(serverboundBlockEntityTagQuery.getTransactionId(), compoundTag));
		}
	}

	@Override
	public void handleMovePlayer(ServerboundMovePlayerPacket serverboundMovePlayerPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundMovePlayerPacket, this, this.player.getLevel());
		if (containsInvalidValues(
			serverboundMovePlayerPacket.getX(0.0),
			serverboundMovePlayerPacket.getY(0.0),
			serverboundMovePlayerPacket.getZ(0.0),
			serverboundMovePlayerPacket.getYRot(0.0F),
			serverboundMovePlayerPacket.getXRot(0.0F)
		)) {
			this.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
		} else {
			ServerLevel serverLevel = this.player.getLevel();
			if (!this.player.wonGame) {
				if (this.tickCount == 0) {
					this.resetPosition();
				}

				if (this.awaitingPositionFromClient != null) {
					if (this.tickCount - this.awaitingTeleportTime > 20) {
						this.awaitingTeleportTime = this.tickCount;
						this.teleport(
							this.awaitingPositionFromClient.x, this.awaitingPositionFromClient.y, this.awaitingPositionFromClient.z, this.player.getYRot(), this.player.getXRot()
						);
					}
				} else {
					this.awaitingTeleportTime = this.tickCount;
					double d = clampHorizontal(serverboundMovePlayerPacket.getX(this.player.getX()));
					double e = clampVertical(serverboundMovePlayerPacket.getY(this.player.getY()));
					double f = clampHorizontal(serverboundMovePlayerPacket.getZ(this.player.getZ()));
					float g = Mth.wrapDegrees(serverboundMovePlayerPacket.getYRot(this.player.getYRot()));
					float h = Mth.wrapDegrees(serverboundMovePlayerPacket.getXRot(this.player.getXRot()));
					if (this.player.isPassenger()) {
						this.player.absMoveTo(this.player.getX(), this.player.getY(), this.player.getZ(), g, h);
						this.player.getLevel().getChunkSource().move(this.player);
					} else {
						double i = this.player.getX();
						double j = this.player.getY();
						double k = this.player.getZ();
						double l = this.player.getY();
						double m = d - this.firstGoodX;
						double n = e - this.firstGoodY;
						double o = f - this.firstGoodZ;
						double p = this.player.getDeltaMovement().lengthSqr();
						double q = m * m + n * n + o * o;
						if (this.player.isSleeping()) {
							if (q > 1.0) {
								this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), g, h);
							}
						} else {
							this.receivedMovePacketCount++;
							int r = this.receivedMovePacketCount - this.knownMovePacketCount;
							if (r > 5) {
								LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", this.player.getName().getString(), r);
								r = 1;
							}

							if (!this.player.isChangingDimension()
								&& (!this.player.getLevel().getGameRules().getBoolean(GameRules.RULE_DISABLE_ELYTRA_MOVEMENT_CHECK) || !this.player.isFallFlying())) {
								float s = this.player.isFallFlying() ? 300.0F : 100.0F;
								if (q - p > (double)(s * (float)r) && !this.isSingleplayerOwner()) {
									LOGGER.warn("{} moved too quickly! {},{},{}", this.player.getName().getString(), m, n, o);
									this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYRot(), this.player.getXRot());
									return;
								}
							}

							AABB aABB = this.player.getBoundingBox();
							m = d - this.lastGoodX;
							n = e - this.lastGoodY;
							o = f - this.lastGoodZ;
							boolean bl = n > 0.0;
							if (this.player.isOnGround() && !serverboundMovePlayerPacket.isOnGround() && bl) {
								this.player.jumpFromGround();
							}

							boolean bl2 = this.player.verticalCollisionBelow;
							this.player.move(MoverType.PLAYER, new Vec3(m, n, o));
							m = d - this.player.getX();
							n = e - this.player.getY();
							if (n > -0.5 || n < 0.5) {
								n = 0.0;
							}

							o = f - this.player.getZ();
							q = m * m + n * n + o * o;
							boolean bl3 = false;
							if (!this.player.isChangingDimension()
								&& q > 0.0625
								&& !this.player.isSleeping()
								&& !this.player.gameMode.isCreative()
								&& this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
								bl3 = true;
								LOGGER.warn("{} moved wrongly!", this.player.getName().getString());
							}

							this.player.absMoveTo(d, e, f, g, h);
							if (this.player.noPhysics
								|| this.player.isSleeping()
								|| (!bl3 || !serverLevel.noCollision(this.player, aABB)) && !this.isPlayerCollidingWithAnythingNew(serverLevel, aABB)) {
								this.clientIsFloating = n >= -0.03125
									&& !bl2
									&& this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR
									&& !this.server.isFlightAllowed()
									&& !this.player.getAbilities().mayfly
									&& !this.player.hasEffect(MobEffects.LEVITATION)
									&& !this.player.isFallFlying()
									&& !this.player.isAutoSpinAttack()
									&& this.noBlocksAround(this.player);
								this.player.getLevel().getChunkSource().move(this.player);
								this.player.doCheckFallDamage(this.player.getY() - l, serverboundMovePlayerPacket.isOnGround());
								this.player.setOnGround(serverboundMovePlayerPacket.isOnGround());
								if (bl) {
									this.player.resetFallDistance();
								}

								this.player.checkMovementStatistics(this.player.getX() - i, this.player.getY() - j, this.player.getZ() - k);
								this.lastGoodX = this.player.getX();
								this.lastGoodY = this.player.getY();
								this.lastGoodZ = this.player.getZ();
							} else {
								this.teleport(i, j, k, g, h);
								this.player.doCheckFallDamage(this.player.getY() - l, serverboundMovePlayerPacket.isOnGround());
							}
						}
					}
				}
			}
		}
	}

	private boolean isPlayerCollidingWithAnythingNew(LevelReader levelReader, AABB aABB) {
		Iterable<VoxelShape> iterable = levelReader.getCollisions(this.player, this.player.getBoundingBox().deflate(1.0E-5F));
		VoxelShape voxelShape = Shapes.create(aABB.deflate(1.0E-5F));

		for (VoxelShape voxelShape2 : iterable) {
			if (!Shapes.joinIsNotEmpty(voxelShape2, voxelShape, BooleanOp.AND)) {
				return true;
			}
		}

		return false;
	}

	public void teleport(double d, double e, double f, float g, float h) {
		this.teleport(d, e, f, g, h, Collections.emptySet());
	}

	public void teleport(double d, double e, double f, float g, float h, Set<RelativeMovement> set) {
		double i = set.contains(RelativeMovement.X) ? this.player.getX() : 0.0;
		double j = set.contains(RelativeMovement.Y) ? this.player.getY() : 0.0;
		double k = set.contains(RelativeMovement.Z) ? this.player.getZ() : 0.0;
		float l = set.contains(RelativeMovement.Y_ROT) ? this.player.getYRot() : 0.0F;
		float m = set.contains(RelativeMovement.X_ROT) ? this.player.getXRot() : 0.0F;
		this.awaitingPositionFromClient = new Vec3(d, e, f);
		if (++this.awaitingTeleport == Integer.MAX_VALUE) {
			this.awaitingTeleport = 0;
		}

		this.awaitingTeleportTime = this.tickCount;
		this.player.absMoveTo(d, e, f, g, h);
		this.player.connection.send(new ClientboundPlayerPositionPacket(d - i, e - j, f - k, g - l, h - m, set, this.awaitingTeleport));
	}

	@Override
	public void handlePlayerAction(ServerboundPlayerActionPacket serverboundPlayerActionPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundPlayerActionPacket, this, this.player.getLevel());
		BlockPos blockPos = serverboundPlayerActionPacket.getPos();
		this.player.resetLastActionTime();
		ServerboundPlayerActionPacket.Action action = serverboundPlayerActionPacket.getAction();
		switch (action) {
			case SWAP_ITEM_WITH_OFFHAND:
				if (!this.player.isSpectator()) {
					ItemStack itemStack = this.player.getItemInHand(InteractionHand.OFF_HAND);
					this.player.setItemInHand(InteractionHand.OFF_HAND, this.player.getItemInHand(InteractionHand.MAIN_HAND));
					this.player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
					this.player.stopUsingItem();
				}

				return;
			case DROP_ITEM:
				if (!this.player.isSpectator()) {
					this.player.drop(false);
				}

				return;
			case DROP_ALL_ITEMS:
				if (!this.player.isSpectator()) {
					this.player.drop(true);
				}

				return;
			case RELEASE_USE_ITEM:
				this.player.releaseUsingItem();
				return;
			case START_DESTROY_BLOCK:
			case ABORT_DESTROY_BLOCK:
			case STOP_DESTROY_BLOCK:
				this.player
					.gameMode
					.handleBlockBreakAction(
						blockPos, action, serverboundPlayerActionPacket.getDirection(), this.player.level.getMaxBuildHeight(), serverboundPlayerActionPacket.getSequence()
					);
				this.player.connection.ackBlockChangesUpTo(serverboundPlayerActionPacket.getSequence());
				return;
			default:
				throw new IllegalArgumentException("Invalid player action");
		}
	}

	private static boolean wasBlockPlacementAttempt(ServerPlayer serverPlayer, ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return false;
		} else {
			Item item = itemStack.getItem();
			return (item instanceof BlockItem || item instanceof BucketItem) && !serverPlayer.getCooldowns().isOnCooldown(item);
		}
	}

	@Override
	public void handleUseItemOn(ServerboundUseItemOnPacket serverboundUseItemOnPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundUseItemOnPacket, this, this.player.getLevel());
		this.player.connection.ackBlockChangesUpTo(serverboundUseItemOnPacket.getSequence());
		ServerLevel serverLevel = this.player.getLevel();
		InteractionHand interactionHand = serverboundUseItemOnPacket.getHand();
		ItemStack itemStack = this.player.getItemInHand(interactionHand);
		if (itemStack.isItemEnabled(serverLevel.enabledFeatures())) {
			BlockHitResult blockHitResult = serverboundUseItemOnPacket.getHitResult();
			Vec3 vec3 = blockHitResult.getLocation();
			BlockPos blockPos = blockHitResult.getBlockPos();
			Vec3 vec32 = Vec3.atCenterOf(blockPos);
			if (!(this.player.getEyePosition().distanceToSqr(vec32) > MAX_INTERACTION_DISTANCE)) {
				Vec3 vec33 = vec3.subtract(vec32);
				double d = 1.0000001;
				if (Math.abs(vec33.x()) < 1.0000001 && Math.abs(vec33.y()) < 1.0000001 && Math.abs(vec33.z()) < 1.0000001) {
					Direction direction = blockHitResult.getDirection();
					this.player.resetLastActionTime();
					int i = this.player.level.getMaxBuildHeight();
					if (blockPos.getY() < i) {
						if (this.awaitingPositionFromClient == null
							&& this.player.distanceToSqr((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5) < 64.0
							&& serverLevel.mayInteract(this.player, blockPos)) {
							InteractionResult interactionResult = this.player.gameMode.useItemOn(this.player, serverLevel, itemStack, interactionHand, blockHitResult);
							if (direction == Direction.UP && !interactionResult.consumesAction() && blockPos.getY() >= i - 1 && wasBlockPlacementAttempt(this.player, itemStack)) {
								Component component = Component.translatable("build.tooHigh", i - 1).withStyle(ChatFormatting.RED);
								this.player.sendSystemMessage(component, true);
							} else if (interactionResult.shouldSwing()) {
								this.player.swing(interactionHand, true);
							}
						}
					} else {
						Component component2 = Component.translatable("build.tooHigh", i - 1).withStyle(ChatFormatting.RED);
						this.player.sendSystemMessage(component2, true);
					}

					this.player.connection.send(new ClientboundBlockUpdatePacket(serverLevel, blockPos));
					this.player.connection.send(new ClientboundBlockUpdatePacket(serverLevel, blockPos.relative(direction)));
				} else {
					LOGGER.warn("Rejecting UseItemOnPacket from {}: Location {} too far away from hit block {}.", this.player.getGameProfile().getName(), vec3, blockPos);
				}
			}
		}
	}

	@Override
	public void handleUseItem(ServerboundUseItemPacket serverboundUseItemPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundUseItemPacket, this, this.player.getLevel());
		this.ackBlockChangesUpTo(serverboundUseItemPacket.getSequence());
		ServerLevel serverLevel = this.player.getLevel();
		InteractionHand interactionHand = serverboundUseItemPacket.getHand();
		ItemStack itemStack = this.player.getItemInHand(interactionHand);
		this.player.resetLastActionTime();
		if (!itemStack.isEmpty() && itemStack.isItemEnabled(serverLevel.enabledFeatures())) {
			InteractionResult interactionResult = this.player.gameMode.useItem(this.player, serverLevel, itemStack, interactionHand);
			if (interactionResult.shouldSwing()) {
				this.player.swing(interactionHand, true);
			}
		}
	}

	@Override
	public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket serverboundTeleportToEntityPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundTeleportToEntityPacket, this, this.player.getLevel());
		if (this.player.isSpectator()) {
			for (ServerLevel serverLevel : this.server.getAllLevels()) {
				Entity entity = serverboundTeleportToEntityPacket.getEntity(serverLevel);
				if (entity != null) {
					this.player.teleportTo(serverLevel, entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
					return;
				}
			}
		}
	}

	@Override
	public void handleResourcePackResponse(ServerboundResourcePackPacket serverboundResourcePackPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundResourcePackPacket, this, this.player.getLevel());
		if (serverboundResourcePackPacket.getAction() == ServerboundResourcePackPacket.Action.DECLINED && this.server.isResourcePackRequired()) {
			LOGGER.info("Disconnecting {} due to resource pack rejection", this.player.getName());
			this.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
		}
	}

	@Override
	public void handlePaddleBoat(ServerboundPaddleBoatPacket serverboundPaddleBoatPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundPaddleBoatPacket, this, this.player.getLevel());
		if (this.player.getControlledVehicle() instanceof Boat boat) {
			boat.setPaddleState(serverboundPaddleBoatPacket.getLeft(), serverboundPaddleBoatPacket.getRight());
		}
	}

	@Override
	public void handlePong(ServerboundPongPacket serverboundPongPacket) {
	}

	@Override
	public void onDisconnect(Component component) {
		this.chatMessageChain.close();
		LOGGER.info("{} lost connection: {}", this.player.getName().getString(), component.getString());
		this.server.invalidateStatus();
		this.server
			.getPlayerList()
			.broadcastSystemMessage(Component.translatable("multiplayer.player.left", this.player.getDisplayName()).withStyle(ChatFormatting.YELLOW), false);
		this.player.disconnect();
		this.server.getPlayerList().remove(this.player);
		this.player.getTextFilter().leave();
		if (this.isSingleplayerOwner()) {
			LOGGER.info("Stopping singleplayer server as player logged out");
			this.server.halt(false);
		}
	}

	public void ackBlockChangesUpTo(int i) {
		if (i < 0) {
			throw new IllegalArgumentException("Expected packet sequence nr >= 0");
		} else {
			this.ackBlockChangesUpTo = Math.max(i, this.ackBlockChangesUpTo);
		}
	}

	@Override
	public void send(Packet<?> packet) {
		this.send(packet, null);
	}

	public void send(Packet<?> packet, @Nullable PacketSendListener packetSendListener) {
		try {
			this.connection.send(packet, packetSendListener);
		} catch (Throwable var6) {
			CrashReport crashReport = CrashReport.forThrowable(var6, "Sending packet");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Packet being sent");
			crashReportCategory.setDetail("Packet class", (CrashReportDetail<String>)(() -> packet.getClass().getCanonicalName()));
			throw new ReportedException(crashReport);
		}
	}

	@Override
	public void handleSetCarriedItem(ServerboundSetCarriedItemPacket serverboundSetCarriedItemPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundSetCarriedItemPacket, this, this.player.getLevel());
		if (serverboundSetCarriedItemPacket.getSlot() >= 0 && serverboundSetCarriedItemPacket.getSlot() < Inventory.getSelectionSize()) {
			if (this.player.getInventory().selected != serverboundSetCarriedItemPacket.getSlot() && this.player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
				this.player.stopUsingItem();
			}

			this.player.getInventory().selected = serverboundSetCarriedItemPacket.getSlot();
			this.player.resetLastActionTime();
		} else {
			LOGGER.warn("{} tried to set an invalid carried item", this.player.getName().getString());
		}
	}

	@Override
	public void handleChat(ServerboundChatPacket serverboundChatPacket) {
		if (isChatMessageIllegal(serverboundChatPacket.message())) {
			this.disconnect(Component.translatable("multiplayer.disconnect.illegal_characters"));
		} else {
			Optional<LastSeenMessages> optional = this.tryHandleChat(
				serverboundChatPacket.message(), serverboundChatPacket.timeStamp(), serverboundChatPacket.lastSeenMessages()
			);
			if (optional.isPresent()) {
				this.server
					.submit(
						() -> {
							PlayerChatMessage playerChatMessage;
							try {
								playerChatMessage = this.getSignedMessage(serverboundChatPacket, (LastSeenMessages)optional.get());
							} catch (SignedMessageChain.DecodeException var6) {
								this.handleMessageDecodeFailure(var6);
								return;
							}

							CompletableFuture<FilteredText> completableFuture = this.filterTextPacket(playerChatMessage.signedContent());
							CompletableFuture<Component> completableFuture2 = this.server.getChatDecorator().decorate(this.player, playerChatMessage.decoratedContent());
							this.chatMessageChain
								.append(
									executor -> CompletableFuture.allOf(completableFuture, completableFuture2)
											.thenAcceptAsync(
												void_ -> {
													PlayerChatMessage playerChatMessage2 = playerChatMessage.withUnsignedContent((Component)completableFuture2.join())
														.filter(((FilteredText)completableFuture.join()).mask());
													this.broadcastChatMessage(playerChatMessage2);
												},
												executor
											)
								);
						}
					);
			}
		}
	}

	@Override
	public void handleChatCommand(ServerboundChatCommandPacket serverboundChatCommandPacket) {
		if (isChatMessageIllegal(serverboundChatCommandPacket.command())) {
			this.disconnect(Component.translatable("multiplayer.disconnect.illegal_characters"));
		} else {
			Optional<LastSeenMessages> optional = this.tryHandleChat(
				serverboundChatCommandPacket.command(), serverboundChatCommandPacket.timeStamp(), serverboundChatCommandPacket.lastSeenMessages()
			);
			if (optional.isPresent()) {
				this.server.submit(() -> {
					this.performChatCommand(serverboundChatCommandPacket, (LastSeenMessages)optional.get());
					this.detectRateSpam();
				});
			}
		}
	}

	private void performChatCommand(ServerboundChatCommandPacket serverboundChatCommandPacket, LastSeenMessages lastSeenMessages) {
		ParseResults<CommandSourceStack> parseResults = this.parseCommand(serverboundChatCommandPacket.command());

		Map<String, PlayerChatMessage> map;
		try {
			map = this.collectSignedArguments(serverboundChatCommandPacket, SignableCommand.of(parseResults), lastSeenMessages);
		} catch (SignedMessageChain.DecodeException var6) {
			this.handleMessageDecodeFailure(var6);
			return;
		}

		CommandSigningContext commandSigningContext = new CommandSigningContext.SignedArguments(map);
		parseResults = Commands.mapSource(parseResults, commandSourceStack -> commandSourceStack.withSigningContext(commandSigningContext));
		this.server.getCommands().performCommand(parseResults, serverboundChatCommandPacket.command());
	}

	private void handleMessageDecodeFailure(SignedMessageChain.DecodeException decodeException) {
		if (decodeException.shouldDisconnect()) {
			this.disconnect(decodeException.getComponent());
		} else {
			this.player.sendSystemMessage(decodeException.getComponent().copy().withStyle(ChatFormatting.RED));
		}
	}

	private Map<String, PlayerChatMessage> collectSignedArguments(
		ServerboundChatCommandPacket serverboundChatCommandPacket, SignableCommand<?> signableCommand, LastSeenMessages lastSeenMessages
	) throws SignedMessageChain.DecodeException {
		Map<String, PlayerChatMessage> map = new Object2ObjectOpenHashMap<>();

		for (SignableCommand.Argument<?> argument : signableCommand.arguments()) {
			MessageSignature messageSignature = serverboundChatCommandPacket.argumentSignatures().get(argument.name());
			SignedMessageBody signedMessageBody = new SignedMessageBody(
				argument.value(), serverboundChatCommandPacket.timeStamp(), serverboundChatCommandPacket.salt(), lastSeenMessages
			);
			map.put(argument.name(), this.signedMessageDecoder.unpack(messageSignature, signedMessageBody));
		}

		return map;
	}

	private ParseResults<CommandSourceStack> parseCommand(String string) {
		CommandDispatcher<CommandSourceStack> commandDispatcher = this.server.getCommands().getDispatcher();
		return commandDispatcher.parse(string, this.player.createCommandSourceStack());
	}

	private Optional<LastSeenMessages> tryHandleChat(String string, Instant instant, LastSeenMessages.Update update) {
		if (!this.updateChatOrder(instant)) {
			LOGGER.warn("{} sent out-of-order chat: '{}'", this.player.getName().getString(), string);
			this.disconnect(Component.translatable("multiplayer.disconnect.out_of_order_chat"));
			return Optional.empty();
		} else {
			Optional<LastSeenMessages> optional = this.unpackAndApplyLastSeen(update);
			if (this.player.getChatVisibility() == ChatVisiblity.HIDDEN) {
				this.send(new ClientboundSystemChatPacket(Component.translatable("chat.disabled.options").withStyle(ChatFormatting.RED), false));
				return Optional.empty();
			} else {
				this.player.resetLastActionTime();
				return optional;
			}
		}
	}

	private Optional<LastSeenMessages> unpackAndApplyLastSeen(LastSeenMessages.Update update) {
		synchronized (this.lastSeenMessages) {
			Optional<LastSeenMessages> optional = this.lastSeenMessages.applyUpdate(update);
			if (optional.isEmpty()) {
				LOGGER.warn("Failed to validate message acknowledgements from {}", this.player.getName().getString());
				this.disconnect(CHAT_VALIDATION_FAILED);
			}

			return optional;
		}
	}

	private boolean updateChatOrder(Instant instant) {
		Instant instant2;
		do {
			instant2 = (Instant)this.lastChatTimeStamp.get();
			if (instant.isBefore(instant2)) {
				return false;
			}
		} while (!this.lastChatTimeStamp.compareAndSet(instant2, instant));

		return true;
	}

	private static boolean isChatMessageIllegal(String string) {
		for (int i = 0; i < string.length(); i++) {
			if (!SharedConstants.isAllowedChatCharacter(string.charAt(i))) {
				return true;
			}
		}

		return false;
	}

	private PlayerChatMessage getSignedMessage(ServerboundChatPacket serverboundChatPacket, LastSeenMessages lastSeenMessages) throws SignedMessageChain.DecodeException {
		SignedMessageBody signedMessageBody = new SignedMessageBody(
			serverboundChatPacket.message(), serverboundChatPacket.timeStamp(), serverboundChatPacket.salt(), lastSeenMessages
		);
		return this.signedMessageDecoder.unpack(serverboundChatPacket.signature(), signedMessageBody);
	}

	private void broadcastChatMessage(PlayerChatMessage playerChatMessage) {
		this.server.getPlayerList().broadcastChatMessage(playerChatMessage, this.player, ChatType.bind(ChatType.CHAT, this.player));
		this.detectRateSpam();
	}

	private void detectRateSpam() {
		this.chatSpamTickCount += 20;
		if (this.chatSpamTickCount > 200 && !this.server.getPlayerList().isOp(this.player.getGameProfile())) {
			this.disconnect(Component.translatable("disconnect.spam"));
		}
	}

	@Override
	public void handleChatAck(ServerboundChatAckPacket serverboundChatAckPacket) {
		synchronized (this.lastSeenMessages) {
			if (!this.lastSeenMessages.applyOffset(serverboundChatAckPacket.offset())) {
				LOGGER.warn("Failed to validate message acknowledgements from {}", this.player.getName().getString());
				this.disconnect(CHAT_VALIDATION_FAILED);
			}
		}
	}

	@Override
	public void handleAnimate(ServerboundSwingPacket serverboundSwingPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundSwingPacket, this, this.player.getLevel());
		this.player.resetLastActionTime();
		this.player.swing(serverboundSwingPacket.getHand());
	}

	@Override
	public void handlePlayerCommand(ServerboundPlayerCommandPacket serverboundPlayerCommandPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundPlayerCommandPacket, this, this.player.getLevel());
		this.player.resetLastActionTime();
		switch (serverboundPlayerCommandPacket.getAction()) {
			case PRESS_SHIFT_KEY:
				this.player.setShiftKeyDown(true);
				break;
			case RELEASE_SHIFT_KEY:
				this.player.setShiftKeyDown(false);
				break;
			case START_SPRINTING:
				this.player.setSprinting(true);
				break;
			case STOP_SPRINTING:
				this.player.setSprinting(false);
				break;
			case STOP_SLEEPING:
				if (this.player.isSleeping()) {
					this.player.stopSleepInBed(false, true);
					this.awaitingPositionFromClient = this.player.position();
				}
				break;
			case START_RIDING_JUMP:
				if (this.player.getControlledVehicle() instanceof PlayerRideableJumping playerRideableJumping) {
					int i = serverboundPlayerCommandPacket.getData();
					if (playerRideableJumping.canJump() && i > 0) {
						playerRideableJumping.handleStartJump(i);
					}
				}
				break;
			case STOP_RIDING_JUMP:
				if (this.player.getControlledVehicle() instanceof PlayerRideableJumping playerRideableJumping) {
					playerRideableJumping.handleStopJump();
				}
				break;
			case OPEN_INVENTORY:
				if (this.player.getVehicle() instanceof HasCustomInventoryScreen hasCustomInventoryScreen) {
					hasCustomInventoryScreen.openCustomInventoryScreen(this.player);
				}
				break;
			case START_FALL_FLYING:
				if (!this.player.tryToStartFallFlying()) {
					this.player.stopFallFlying();
				}
				break;
			default:
				throw new IllegalArgumentException("Invalid client command!");
		}
	}

	public void addPendingMessage(PlayerChatMessage playerChatMessage) {
		MessageSignature messageSignature = playerChatMessage.signature();
		if (messageSignature != null) {
			this.messageSignatureCache.push(playerChatMessage);
			int i;
			synchronized (this.lastSeenMessages) {
				this.lastSeenMessages.addPending(messageSignature);
				i = this.lastSeenMessages.trackedMessagesCount();
			}

			if (i > 4096) {
				this.disconnect(Component.translatable("multiplayer.disconnect.too_many_pending_chats"));
			}
		}
	}

	public void sendPlayerChatMessage(PlayerChatMessage playerChatMessage, ChatType.Bound bound) {
		this.send(
			new ClientboundPlayerChatPacket(
				playerChatMessage.link().sender(),
				playerChatMessage.link().index(),
				playerChatMessage.signature(),
				playerChatMessage.signedBody().pack(this.messageSignatureCache),
				playerChatMessage.unsignedContent(),
				playerChatMessage.filterMask(),
				bound.toNetwork(this.player.level.registryAccess())
			)
		);
		this.addPendingMessage(playerChatMessage);
	}

	public void sendDisguisedChatMessage(Component component, ChatType.Bound bound) {
		this.send(new ClientboundDisguisedChatPacket(component, bound.toNetwork(this.player.level.registryAccess())));
	}

	public SocketAddress getRemoteAddress() {
		return this.connection.getRemoteAddress();
	}

	@Override
	public void handleInteract(ServerboundInteractPacket serverboundInteractPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundInteractPacket, this, this.player.getLevel());
		final ServerLevel serverLevel = this.player.getLevel();
		final Entity entity = serverboundInteractPacket.getTarget(serverLevel);
		this.player.resetLastActionTime();
		this.player.setShiftKeyDown(serverboundInteractPacket.isUsingSecondaryAction());
		if (entity != null) {
			if (!serverLevel.getWorldBorder().isWithinBounds(entity.blockPosition())) {
				return;
			}

			AABB aABB = entity.getBoundingBox();
			if (aABB.distanceToSqr(this.player.getEyePosition()) < MAX_INTERACTION_DISTANCE) {
				serverboundInteractPacket.dispatch(
					new ServerboundInteractPacket.Handler() {
						private void performInteraction(InteractionHand interactionHand, ServerGamePacketListenerImpl.EntityInteraction entityInteraction) {
							ItemStack itemStack = ServerGamePacketListenerImpl.this.player.getItemInHand(interactionHand);
							if (itemStack.isItemEnabled(serverLevel.enabledFeatures())) {
								ItemStack itemStack2 = itemStack.copy();
								InteractionResult interactionResult = entityInteraction.run(ServerGamePacketListenerImpl.this.player, entity, interactionHand);
								if (interactionResult.consumesAction()) {
									CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(ServerGamePacketListenerImpl.this.player, itemStack2, entity);
									if (interactionResult.shouldSwing()) {
										ServerGamePacketListenerImpl.this.player.swing(interactionHand, true);
									}
								}
							}
						}

						@Override
						public void onInteraction(InteractionHand interactionHand) {
							this.performInteraction(interactionHand, Player::interactOn);
						}

						@Override
						public void onInteraction(InteractionHand interactionHand, Vec3 vec3) {
							this.performInteraction(interactionHand, (serverPlayer, entityxx, interactionHandx) -> entityxx.interactAt(serverPlayer, vec3, interactionHandx));
						}

						@Override
						public void onAttack() {
							if (!(entity instanceof ItemEntity)
								&& !(entity instanceof ExperienceOrb)
								&& !(entity instanceof AbstractArrow)
								&& entity != ServerGamePacketListenerImpl.this.player) {
								ItemStack itemStack = ServerGamePacketListenerImpl.this.player.getItemInHand(InteractionHand.MAIN_HAND);
								if (itemStack.isItemEnabled(serverLevel.enabledFeatures())) {
									ServerGamePacketListenerImpl.this.player.attack(entity);
								}
							} else {
								ServerGamePacketListenerImpl.this.disconnect(Component.translatable("multiplayer.disconnect.invalid_entity_attacked"));
								ServerGamePacketListenerImpl.LOGGER.warn("Player {} tried to attack an invalid entity", ServerGamePacketListenerImpl.this.player.getName().getString());
							}
						}
					}
				);
			}
		}
	}

	@Override
	public void handleClientCommand(ServerboundClientCommandPacket serverboundClientCommandPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundClientCommandPacket, this, this.player.getLevel());
		this.player.resetLastActionTime();
		ServerboundClientCommandPacket.Action action = serverboundClientCommandPacket.getAction();
		switch (action) {
			case PERFORM_RESPAWN:
				if (this.player.wonGame) {
					this.player.wonGame = false;
					this.player = this.server.getPlayerList().respawn(this.player, true);
					CriteriaTriggers.CHANGED_DIMENSION.trigger(this.player, Level.END, Level.OVERWORLD);
				} else {
					if (this.player.getHealth() > 0.0F) {
						return;
					}

					this.player = this.server.getPlayerList().respawn(this.player, false);
					if (this.server.isHardcore()) {
						this.player.setGameMode(GameType.SPECTATOR);
						this.player.getLevel().getGameRules().getRule(GameRules.RULE_SPECTATORSGENERATECHUNKS).set(false, this.server);
					}
				}
				break;
			case REQUEST_STATS:
				this.player.getStats().sendStats(this.player);
		}
	}

	@Override
	public void handleContainerClose(ServerboundContainerClosePacket serverboundContainerClosePacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundContainerClosePacket, this, this.player.getLevel());
		this.player.doCloseContainer();
	}

	@Override
	public void handleContainerClick(ServerboundContainerClickPacket serverboundContainerClickPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundContainerClickPacket, this, this.player.getLevel());
		this.player.resetLastActionTime();
		if (this.player.containerMenu.containerId == serverboundContainerClickPacket.getContainerId()) {
			if (this.player.isSpectator()) {
				this.player.containerMenu.sendAllDataToRemote();
			} else if (!this.player.containerMenu.stillValid(this.player)) {
				LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
			} else {
				int i = serverboundContainerClickPacket.getSlotNum();
				if (!this.player.containerMenu.isValidSlotIndex(i)) {
					LOGGER.debug("Player {} clicked invalid slot index: {}, available slots: {}", this.player.getName(), i, this.player.containerMenu.slots.size());
				} else {
					boolean bl = serverboundContainerClickPacket.getStateId() != this.player.containerMenu.getStateId();
					this.player.containerMenu.suppressRemoteUpdates();
					this.player.containerMenu.clicked(i, serverboundContainerClickPacket.getButtonNum(), serverboundContainerClickPacket.getClickType(), this.player);

					for (Entry<ItemStack> entry : Int2ObjectMaps.fastIterable(serverboundContainerClickPacket.getChangedSlots())) {
						this.player.containerMenu.setRemoteSlotNoCopy(entry.getIntKey(), (ItemStack)entry.getValue());
					}

					this.player.containerMenu.setRemoteCarried(serverboundContainerClickPacket.getCarriedItem());
					this.player.containerMenu.resumeRemoteUpdates();
					if (bl) {
						this.player.containerMenu.broadcastFullState();
					} else {
						this.player.containerMenu.broadcastChanges();
					}
				}
			}
		}
	}

	@Override
	public void handlePlaceRecipe(ServerboundPlaceRecipePacket serverboundPlaceRecipePacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundPlaceRecipePacket, this, this.player.getLevel());
		this.player.resetLastActionTime();
		if (!this.player.isSpectator()
			&& this.player.containerMenu.containerId == serverboundPlaceRecipePacket.getContainerId()
			&& this.player.containerMenu instanceof RecipeBookMenu) {
			if (!this.player.containerMenu.stillValid(this.player)) {
				LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
			} else {
				this.server
					.getRecipeManager()
					.byKey(serverboundPlaceRecipePacket.getRecipe())
					.ifPresent(recipe -> ((RecipeBookMenu)this.player.containerMenu).handlePlacement(serverboundPlaceRecipePacket.isShiftDown(), recipe, this.player));
			}
		}
	}

	@Override
	public void handleContainerButtonClick(ServerboundContainerButtonClickPacket serverboundContainerButtonClickPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundContainerButtonClickPacket, this, this.player.getLevel());
		this.player.resetLastActionTime();
		if (this.player.containerMenu.containerId == serverboundContainerButtonClickPacket.getContainerId() && !this.player.isSpectator()) {
			if (!this.player.containerMenu.stillValid(this.player)) {
				LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
			} else {
				boolean bl = this.player.containerMenu.clickMenuButton(this.player, serverboundContainerButtonClickPacket.getButtonId());
				if (bl) {
					this.player.containerMenu.broadcastChanges();
				}
			}
		}
	}

	@Override
	public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket serverboundSetCreativeModeSlotPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundSetCreativeModeSlotPacket, this, this.player.getLevel());
		if (this.player.gameMode.isCreative()) {
			boolean bl = serverboundSetCreativeModeSlotPacket.getSlotNum() < 0;
			ItemStack itemStack = serverboundSetCreativeModeSlotPacket.getItem();
			if (!itemStack.isItemEnabled(this.player.getLevel().enabledFeatures())) {
				return;
			}

			CompoundTag compoundTag = BlockItem.getBlockEntityData(itemStack);
			if (!itemStack.isEmpty() && compoundTag != null && compoundTag.contains("x") && compoundTag.contains("y") && compoundTag.contains("z")) {
				BlockPos blockPos = BlockEntity.getPosFromTag(compoundTag);
				if (this.player.level.isLoaded(blockPos)) {
					BlockEntity blockEntity = this.player.level.getBlockEntity(blockPos);
					if (blockEntity != null) {
						blockEntity.saveToItem(itemStack);
					}
				}
			}

			boolean bl2 = serverboundSetCreativeModeSlotPacket.getSlotNum() >= 1 && serverboundSetCreativeModeSlotPacket.getSlotNum() <= 45;
			boolean bl3 = itemStack.isEmpty() || itemStack.getDamageValue() >= 0 && itemStack.getCount() <= 64 && !itemStack.isEmpty();
			if (bl2 && bl3) {
				this.player.inventoryMenu.getSlot(serverboundSetCreativeModeSlotPacket.getSlotNum()).setByPlayer(itemStack);
				this.player.inventoryMenu.broadcastChanges();
			} else if (bl && bl3 && this.dropSpamTickCount < 200) {
				this.dropSpamTickCount += 20;
				this.player.drop(itemStack, true);
			}
		}
	}

	@Override
	public void handleSignUpdate(ServerboundSignUpdatePacket serverboundSignUpdatePacket) {
		List<String> list = (List<String>)Stream.of(serverboundSignUpdatePacket.getLines()).map(ChatFormatting::stripFormatting).collect(Collectors.toList());
		this.filterTextPacket(list).thenAcceptAsync(listx -> this.updateSignText(serverboundSignUpdatePacket, listx), this.server);
	}

	private void updateSignText(ServerboundSignUpdatePacket serverboundSignUpdatePacket, List<FilteredText> list) {
		this.player.resetLastActionTime();
		ServerLevel serverLevel = this.player.getLevel();
		BlockPos blockPos = serverboundSignUpdatePacket.getPos();
		if (serverLevel.hasChunkAt(blockPos)) {
			if (!(serverLevel.getBlockEntity(blockPos) instanceof SignBlockEntity signBlockEntity)) {
				return;
			}

			signBlockEntity.updateSignText(this.player, serverboundSignUpdatePacket.isFrontText(), list);
		}
	}

	@Override
	public void handleKeepAlive(ServerboundKeepAlivePacket serverboundKeepAlivePacket) {
		if (this.keepAlivePending && serverboundKeepAlivePacket.getId() == this.keepAliveChallenge) {
			int i = (int)(Util.getMillis() - this.keepAliveTime);
			this.player.latency = (this.player.latency * 3 + i) / 4;
			this.keepAlivePending = false;
		} else if (!this.isSingleplayerOwner()) {
			this.disconnect(Component.translatable("disconnect.timeout"));
		}
	}

	@Override
	public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket serverboundPlayerAbilitiesPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundPlayerAbilitiesPacket, this, this.player.getLevel());
		this.player.getAbilities().flying = serverboundPlayerAbilitiesPacket.isFlying() && this.player.getAbilities().mayfly;
	}

	@Override
	public void handleClientInformation(ServerboundClientInformationPacket serverboundClientInformationPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundClientInformationPacket, this, this.player.getLevel());
		this.player.updateOptions(serverboundClientInformationPacket);
	}

	@Override
	public void handleCustomPayload(ServerboundCustomPayloadPacket serverboundCustomPayloadPacket) {
	}

	@Override
	public void handleChangeDifficulty(ServerboundChangeDifficultyPacket serverboundChangeDifficultyPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundChangeDifficultyPacket, this, this.player.getLevel());
		if (this.player.hasPermissions(2) || this.isSingleplayerOwner()) {
			this.server.setDifficulty(serverboundChangeDifficultyPacket.getDifficulty(), false);
		}
	}

	@Override
	public void handleLockDifficulty(ServerboundLockDifficultyPacket serverboundLockDifficultyPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundLockDifficultyPacket, this, this.player.getLevel());
		if (this.player.hasPermissions(2) || this.isSingleplayerOwner()) {
			this.server.setDifficultyLocked(serverboundLockDifficultyPacket.isLocked());
		}
	}

	@Override
	public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket serverboundChatSessionUpdatePacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundChatSessionUpdatePacket, this, this.player.getLevel());
		RemoteChatSession.Data data = serverboundChatSessionUpdatePacket.chatSession();
		ProfilePublicKey.Data data2 = this.chatSession != null ? this.chatSession.profilePublicKey().data() : null;
		ProfilePublicKey.Data data3 = data.profilePublicKey();
		if (!Objects.equals(data2, data3)) {
			if (data2 != null && data3.expiresAt().isBefore(data2.expiresAt())) {
				this.disconnect(ProfilePublicKey.EXPIRED_PROFILE_PUBLIC_KEY);
			} else {
				try {
					SignatureValidator signatureValidator = this.server.getServiceSignatureValidator();
					this.resetPlayerChatState(data.validate(this.player.getGameProfile(), signatureValidator, Duration.ZERO));
				} catch (ProfilePublicKey.ValidationException var6) {
					LOGGER.error("Failed to validate profile key: {}", var6.getMessage());
					this.disconnect(var6.getComponent());
				}
			}
		}
	}

	private void resetPlayerChatState(RemoteChatSession remoteChatSession) {
		this.chatSession = remoteChatSession;
		this.signedMessageDecoder = remoteChatSession.createMessageDecoder(this.player.getUUID());
		this.chatMessageChain
			.append(
				executor -> {
					this.player.setChatSession(remoteChatSession);
					this.server
						.getPlayerList()
						.broadcastAll(new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.INITIALIZE_CHAT), List.of(this.player)));
					return CompletableFuture.completedFuture(null);
				}
			);
	}

	@Override
	public ServerPlayer getPlayer() {
		return this.player;
	}

	@FunctionalInterface
	interface EntityInteraction {
		InteractionResult run(ServerPlayer serverPlayer, Entity entity, InteractionHand interactionHand);
	}
}

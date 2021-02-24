package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.Int2ShortOpenHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerAckPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQuery;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundContainerAckPacket;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WritableBookItem;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerGamePacketListenerImpl implements ServerPlayerConnection, ServerGamePacketListener {
	private static final Logger LOGGER = LogManager.getLogger();
	public final Connection connection;
	private final MinecraftServer server;
	public ServerPlayer player;
	private int tickCount;
	private long keepAliveTime;
	private boolean keepAlivePending;
	private long keepAliveChallenge;
	private int chatSpamTickCount;
	private int dropSpamTickCount;
	private final Int2ShortMap expectedAcks = new Int2ShortOpenHashMap();
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

	public ServerGamePacketListenerImpl(MinecraftServer minecraftServer, Connection connection, ServerPlayer serverPlayer) {
		this.server = minecraftServer;
		this.connection = connection;
		connection.setListener(this);
		this.player = serverPlayer;
		serverPlayer.connection = this;
		serverPlayer.getTextFilter().join();
	}

	public void tick() {
		this.resetPosition();
		this.player.xo = this.player.getX();
		this.player.yo = this.player.getY();
		this.player.zo = this.player.getZ();
		this.player.doTick();
		this.player.absMoveTo(this.firstGoodX, this.firstGoodY, this.firstGoodZ, this.player.yRot, this.player.xRot);
		this.tickCount++;
		this.knownMovePacketCount = this.receivedMovePacketCount;
		if (this.clientIsFloating && !this.player.isSleeping()) {
			if (++this.aboveGroundTickCount > 80) {
				LOGGER.warn("{} was kicked for floating too long!", this.player.getName().getString());
				this.disconnect(new TranslatableComponent("multiplayer.disconnect.flying"));
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
					this.disconnect(new TranslatableComponent("multiplayer.disconnect.flying"));
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
				this.disconnect(new TranslatableComponent("disconnect.timeout"));
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
			&& Util.getMillis() - this.player.getLastActionTime() > (long)(this.server.getPlayerIdleTimeout() * 1000 * 60)) {
			this.disconnect(new TranslatableComponent("multiplayer.disconnect.idling"));
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
	public Connection getConnection() {
		return this.connection;
	}

	private boolean isSingleplayerOwner() {
		return this.server.isSingleplayerOwner(this.player.getGameProfile());
	}

	public void disconnect(Component component) {
		this.connection.send(new ClientboundDisconnectPacket(component), future -> this.connection.disconnect(component));
		this.connection.setReadOnly();
		this.server.executeBlocking(this.connection::handleDisconnection);
	}

	private <T, R> void filterTextPacket(T object, Consumer<R> consumer, BiFunction<TextFilter, T, CompletableFuture<R>> biFunction) {
		BlockableEventLoop<?> blockableEventLoop = this.player.getLevel().getServer();
		Consumer<R> consumer2 = objectx -> {
			if (this.getConnection().isConnected()) {
				consumer.accept(objectx);
			} else {
				LOGGER.debug("Ignoring packet due to disconnection");
			}
		};
		((CompletableFuture)biFunction.apply(this.player.getTextFilter(), object)).thenAcceptAsync(consumer2, blockableEventLoop);
	}

	private void filterTextPacket(String string, Consumer<TextFilter.FilteredText> consumer) {
		this.filterTextPacket(string, consumer, TextFilter::processStreamMessage);
	}

	private void filterTextPacket(List<String> list, Consumer<List<TextFilter.FilteredText>> consumer) {
		this.filterTextPacket(list, consumer, TextFilter::processMessageBundle);
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

	private static boolean containsInvalidValues(ServerboundMovePlayerPacket serverboundMovePlayerPacket) {
		return Doubles.isFinite(serverboundMovePlayerPacket.getX(0.0))
				&& Doubles.isFinite(serverboundMovePlayerPacket.getY(0.0))
				&& Doubles.isFinite(serverboundMovePlayerPacket.getZ(0.0))
				&& Floats.isFinite(serverboundMovePlayerPacket.getXRot(0.0F))
				&& Floats.isFinite(serverboundMovePlayerPacket.getYRot(0.0F))
			? Math.abs(serverboundMovePlayerPacket.getX(0.0)) > 3.0E7
				|| Math.abs(serverboundMovePlayerPacket.getY(0.0)) > 3.0E7
				|| Math.abs(serverboundMovePlayerPacket.getZ(0.0)) > 3.0E7
			: true;
	}

	private static boolean containsInvalidValues(ServerboundMoveVehiclePacket serverboundMoveVehiclePacket) {
		return !Doubles.isFinite(serverboundMoveVehiclePacket.getX())
			|| !Doubles.isFinite(serverboundMoveVehiclePacket.getY())
			|| !Doubles.isFinite(serverboundMoveVehiclePacket.getZ())
			|| !Floats.isFinite(serverboundMoveVehiclePacket.getXRot())
			|| !Floats.isFinite(serverboundMoveVehiclePacket.getYRot());
	}

	@Override
	public void handleMoveVehicle(ServerboundMoveVehiclePacket serverboundMoveVehiclePacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundMoveVehiclePacket, this, this.player.getLevel());
		if (containsInvalidValues(serverboundMoveVehiclePacket)) {
			this.disconnect(new TranslatableComponent("multiplayer.disconnect.invalid_vehicle_movement"));
		} else {
			Entity entity = this.player.getRootVehicle();
			if (entity != this.player && entity.getControllingPassenger() == this.player && entity == this.lastVehicle) {
				ServerLevel serverLevel = this.player.getLevel();
				double d = entity.getX();
				double e = entity.getY();
				double f = entity.getZ();
				double g = serverboundMoveVehiclePacket.getX();
				double h = serverboundMoveVehiclePacket.getY();
				double i = serverboundMoveVehiclePacket.getZ();
				float j = serverboundMoveVehiclePacket.getYRot();
				float k = serverboundMoveVehiclePacket.getXRot();
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
				entity.move(MoverType.PLAYER, new Vec3(l, m, n));
				l = g - entity.getX();
				m = h - entity.getY();
				if (m > -0.5 || m < 0.5) {
					m = 0.0;
				}

				n = i - entity.getZ();
				p = l * l + m * m + n * n;
				boolean bl2 = false;
				if (p > 0.0625) {
					bl2 = true;
					LOGGER.warn("{} (vehicle of {}) moved wrongly! {}", entity.getName().getString(), this.player.getName().getString(), Math.sqrt(p));
				}

				entity.absMoveTo(g, h, i, j, k);
				boolean bl3 = serverLevel.noCollision(entity, entity.getBoundingBox().deflate(0.0625));
				if (bl && (bl2 || !bl3)) {
					entity.absMoveTo(d, e, f, j, k);
					this.connection.send(new ClientboundMoveVehiclePacket(entity));
					return;
				}

				this.player.getLevel().getChunkSource().move(this.player);
				this.player.checkMovementStatistics(this.player.getX() - d, this.player.getY() - e, this.player.getZ() - f);
				this.clientVehicleIsFloating = m >= -0.03125 && !this.server.isFlightAllowed() && this.noBlocksAround(entity);
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
			this.player
				.absMoveTo(this.awaitingPositionFromClient.x, this.awaitingPositionFromClient.y, this.awaitingPositionFromClient.z, this.player.yRot, this.player.xRot);
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
			this.player.sendMessage(new TranslatableComponent("advMode.notEnabled"), Util.NIL_UUID);
		} else if (!this.player.canUseGameMasterBlocks()) {
			this.player.sendMessage(new TranslatableComponent("advMode.notAllowed"), Util.NIL_UUID);
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
				BlockState blockState2;
				switch (serverboundSetCommandBlockPacket.getMode()) {
					case SEQUENCE:
						blockState2 = Blocks.CHAIN_COMMAND_BLOCK.defaultBlockState();
						break;
					case AUTO:
						blockState2 = Blocks.REPEATING_COMMAND_BLOCK.defaultBlockState();
						break;
					case REDSTONE:
					default:
						blockState2 = Blocks.COMMAND_BLOCK.defaultBlockState();
				}

				BlockState blockState3 = blockState2.setValue(CommandBlock.FACING, direction)
					.setValue(CommandBlock.CONDITIONAL, Boolean.valueOf(serverboundSetCommandBlockPacket.isConditional()));
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
					this.player.sendMessage(new TranslatableComponent("advMode.setCommand.success", string), Util.NIL_UUID);
				}
			}
		}
	}

	@Override
	public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket serverboundSetCommandMinecartPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundSetCommandMinecartPacket, this, this.player.getLevel());
		if (!this.server.isCommandBlockEnabled()) {
			this.player.sendMessage(new TranslatableComponent("advMode.notEnabled"), Util.NIL_UUID);
		} else if (!this.player.canUseGameMasterBlocks()) {
			this.player.sendMessage(new TranslatableComponent("advMode.notAllowed"), Util.NIL_UUID);
		} else {
			BaseCommandBlock baseCommandBlock = serverboundSetCommandMinecartPacket.getCommandBlock(this.player.level);
			if (baseCommandBlock != null) {
				baseCommandBlock.setCommand(serverboundSetCommandMinecartPacket.getCommand());
				baseCommandBlock.setTrackOutput(serverboundSetCommandMinecartPacket.isTrackOutput());
				if (!serverboundSetCommandMinecartPacket.isTrackOutput()) {
					baseCommandBlock.setLastOutput(null);
				}

				baseCommandBlock.onUpdated();
				this.player.sendMessage(new TranslatableComponent("advMode.setCommand.success", serverboundSetCommandMinecartPacket.getCommand()), Util.NIL_UUID);
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
				new ClientboundContainerSetSlotPacket(-2, this.player.getInventory().selected, this.player.getInventory().getItem(this.player.getInventory().selected))
			);
		this.player
			.connection
			.send(
				new ClientboundContainerSetSlotPacket(-2, serverboundPickItemPacket.getSlot(), this.player.getInventory().getItem(serverboundPickItemPacket.getSlot()))
			);
		this.player.connection.send(new ClientboundSetCarriedItemPacket(this.player.getInventory().selected));
	}

	@Override
	public void handleRenameItem(ServerboundRenameItemPacket serverboundRenameItemPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundRenameItemPacket, this, this.player.getLevel());
		if (this.player.containerMenu instanceof AnvilMenu) {
			AnvilMenu anvilMenu = (AnvilMenu)this.player.containerMenu;
			String string = SharedConstants.filterText(serverboundRenameItemPacket.getName());
			if (string.length() <= 35) {
				anvilMenu.setItemName(string);
			}
		}
	}

	@Override
	public void handleSetBeaconPacket(ServerboundSetBeaconPacket serverboundSetBeaconPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundSetBeaconPacket, this, this.player.getLevel());
		if (this.player.containerMenu instanceof BeaconMenu) {
			((BeaconMenu)this.player.containerMenu).updateEffects(serverboundSetBeaconPacket.getPrimary(), serverboundSetBeaconPacket.getSecondary());
		}
	}

	@Override
	public void handleSetStructureBlock(ServerboundSetStructureBlockPacket serverboundSetStructureBlockPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundSetStructureBlockPacket, this, this.player.getLevel());
		if (this.player.canUseGameMasterBlocks()) {
			BlockPos blockPos = serverboundSetStructureBlockPacket.getPos();
			BlockState blockState = this.player.level.getBlockState(blockPos);
			BlockEntity blockEntity = this.player.level.getBlockEntity(blockPos);
			if (blockEntity instanceof StructureBlockEntity) {
				StructureBlockEntity structureBlockEntity = (StructureBlockEntity)blockEntity;
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
							this.player.displayClientMessage(new TranslatableComponent("structure_block.save_success", string), false);
						} else {
							this.player.displayClientMessage(new TranslatableComponent("structure_block.save_failure", string), false);
						}
					} else if (serverboundSetStructureBlockPacket.getUpdateType() == StructureBlockEntity.UpdateType.LOAD_AREA) {
						if (!structureBlockEntity.isStructureLoadable()) {
							this.player.displayClientMessage(new TranslatableComponent("structure_block.load_not_found", string), false);
						} else if (structureBlockEntity.loadStructure(this.player.getLevel())) {
							this.player.displayClientMessage(new TranslatableComponent("structure_block.load_success", string), false);
						} else {
							this.player.displayClientMessage(new TranslatableComponent("structure_block.load_prepare", string), false);
						}
					} else if (serverboundSetStructureBlockPacket.getUpdateType() == StructureBlockEntity.UpdateType.SCAN_AREA) {
						if (structureBlockEntity.detectSize()) {
							this.player.displayClientMessage(new TranslatableComponent("structure_block.size_success", string), false);
						} else {
							this.player.displayClientMessage(new TranslatableComponent("structure_block.size_failure"), false);
						}
					}
				} else {
					this.player.displayClientMessage(new TranslatableComponent("structure_block.invalid_structure_name", serverboundSetStructureBlockPacket.getName()), false);
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
			BlockEntity blockEntity = this.player.level.getBlockEntity(blockPos);
			if (blockEntity instanceof JigsawBlockEntity) {
				JigsawBlockEntity jigsawBlockEntity = (JigsawBlockEntity)blockEntity;
				jigsawBlockEntity.setName(serverboundSetJigsawBlockPacket.getName());
				jigsawBlockEntity.setTarget(serverboundSetJigsawBlockPacket.getTarget());
				jigsawBlockEntity.setPool(serverboundSetJigsawBlockPacket.getPool());
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
			BlockEntity blockEntity = this.player.level.getBlockEntity(blockPos);
			if (blockEntity instanceof JigsawBlockEntity) {
				JigsawBlockEntity jigsawBlockEntity = (JigsawBlockEntity)blockEntity;
				jigsawBlockEntity.generate(this.player.getLevel(), serverboundJigsawGeneratePacket.levels(), serverboundJigsawGeneratePacket.keepJigsaws());
			}
		}
	}

	@Override
	public void handleSelectTrade(ServerboundSelectTradePacket serverboundSelectTradePacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundSelectTradePacket, this, this.player.getLevel());
		int i = serverboundSelectTradePacket.getItem();
		AbstractContainerMenu abstractContainerMenu = this.player.containerMenu;
		if (abstractContainerMenu instanceof MerchantMenu) {
			MerchantMenu merchantMenu = (MerchantMenu)abstractContainerMenu;
			merchantMenu.setSelectionHint(i);
			merchantMenu.tryMoveItems(i);
		}
	}

	@Override
	public void handleEditBook(ServerboundEditBookPacket serverboundEditBookPacket) {
		int i = serverboundEditBookPacket.getSlot();
		if (Inventory.isHotbarSlot(i) || i == 40) {
			ItemStack itemStack = serverboundEditBookPacket.getBook();
			if (itemStack.is(Items.WRITABLE_BOOK)) {
				CompoundTag compoundTag = itemStack.getTag();
				if (WritableBookItem.makeSureTagIsValid(compoundTag)) {
					List<String> list = Lists.<String>newArrayList();
					boolean bl = serverboundEditBookPacket.isSigning();
					if (bl) {
						list.add(compoundTag.getString("title"));
					}

					ListTag listTag = compoundTag.getList("pages", 8);

					for (int j = 0; j < listTag.size(); j++) {
						list.add(listTag.getString(j));
					}

					this.filterTextPacket(
						list, bl ? listx -> this.signBook((TextFilter.FilteredText)listx.get(0), listx.subList(1, listx.size()), i) : listx -> this.updateBookContents(listx, i)
					);
				}
			}
		}
	}

	private void updateBookContents(List<TextFilter.FilteredText> list, int i) {
		ItemStack itemStack = this.player.getInventory().getItem(i);
		if (itemStack.is(Items.WRITABLE_BOOK)) {
			this.updateBookPages(list, UnaryOperator.identity(), itemStack);
		}
	}

	private void signBook(TextFilter.FilteredText filteredText, List<TextFilter.FilteredText> list, int i) {
		ItemStack itemStack = this.player.getInventory().getItem(i);
		if (itemStack.is(Items.WRITABLE_BOOK)) {
			ItemStack itemStack2 = new ItemStack(Items.WRITTEN_BOOK);
			CompoundTag compoundTag = itemStack.getTag();
			if (compoundTag != null) {
				itemStack2.setTag(compoundTag.copy());
			}

			itemStack2.addTagElement("author", StringTag.valueOf(this.player.getName().getString()));
			if (this.player.isTextFilteringEnabled()) {
				itemStack2.addTagElement("title", StringTag.valueOf(filteredText.getFiltered()));
			} else {
				itemStack2.addTagElement("filtered_title", StringTag.valueOf(filteredText.getFiltered()));
				itemStack2.addTagElement("title", StringTag.valueOf(filteredText.getRaw()));
			}

			this.updateBookPages(list, string -> Component.Serializer.toJson(new TextComponent(string)), itemStack2);
			this.player.getInventory().setItem(i, itemStack2);
		}
	}

	private void updateBookPages(List<TextFilter.FilteredText> list, UnaryOperator<String> unaryOperator, ItemStack itemStack) {
		ListTag listTag = new ListTag();
		if (this.player.isTextFilteringEnabled()) {
			list.stream().map(filteredTextx -> StringTag.valueOf((String)unaryOperator.apply(filteredTextx.getFiltered()))).forEach(listTag::add);
		} else {
			CompoundTag compoundTag = new CompoundTag();
			int i = 0;

			for (int j = list.size(); i < j; i++) {
				TextFilter.FilteredText filteredText = (TextFilter.FilteredText)list.get(i);
				String string = filteredText.getRaw();
				listTag.add(StringTag.valueOf((String)unaryOperator.apply(string)));
				String string2 = filteredText.getFiltered();
				if (!string.equals(string2)) {
					compoundTag.putString(String.valueOf(i), (String)unaryOperator.apply(string2));
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
			CompoundTag compoundTag = blockEntity != null ? blockEntity.save(new CompoundTag()) : null;
			this.player.connection.send(new ClientboundTagQueryPacket(serverboundBlockEntityTagQuery.getTransactionId(), compoundTag));
		}
	}

	@Override
	public void handleMovePlayer(ServerboundMovePlayerPacket serverboundMovePlayerPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundMovePlayerPacket, this, this.player.getLevel());
		if (containsInvalidValues(serverboundMovePlayerPacket)) {
			this.disconnect(new TranslatableComponent("multiplayer.disconnect.invalid_player_movement"));
		} else {
			ServerLevel serverLevel = this.player.getLevel();
			if (!this.player.wonGame) {
				if (this.tickCount == 0) {
					this.resetPosition();
				}

				if (this.awaitingPositionFromClient != null) {
					if (this.tickCount - this.awaitingTeleportTime > 20) {
						this.awaitingTeleportTime = this.tickCount;
						this.teleport(this.awaitingPositionFromClient.x, this.awaitingPositionFromClient.y, this.awaitingPositionFromClient.z, this.player.yRot, this.player.xRot);
					}
				} else {
					this.awaitingTeleportTime = this.tickCount;
					if (this.player.isPassenger()) {
						this.player
							.absMoveTo(
								this.player.getX(),
								this.player.getY(),
								this.player.getZ(),
								serverboundMovePlayerPacket.getYRot(this.player.yRot),
								serverboundMovePlayerPacket.getXRot(this.player.xRot)
							);
						this.player.getLevel().getChunkSource().move(this.player);
					} else {
						double d = this.player.getX();
						double e = this.player.getY();
						double f = this.player.getZ();
						double g = this.player.getY();
						double h = serverboundMovePlayerPacket.getX(this.player.getX());
						double i = serverboundMovePlayerPacket.getY(this.player.getY());
						double j = serverboundMovePlayerPacket.getZ(this.player.getZ());
						float k = serverboundMovePlayerPacket.getYRot(this.player.yRot);
						float l = serverboundMovePlayerPacket.getXRot(this.player.xRot);
						double m = h - this.firstGoodX;
						double n = i - this.firstGoodY;
						double o = j - this.firstGoodZ;
						double p = this.player.getDeltaMovement().lengthSqr();
						double q = m * m + n * n + o * o;
						if (this.player.isSleeping()) {
							if (q > 1.0) {
								this.teleport(
									this.player.getX(),
									this.player.getY(),
									this.player.getZ(),
									serverboundMovePlayerPacket.getYRot(this.player.yRot),
									serverboundMovePlayerPacket.getXRot(this.player.xRot)
								);
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
									this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.yRot, this.player.xRot);
									return;
								}
							}

							AABB aABB = this.player.getBoundingBox();
							m = h - this.lastGoodX;
							n = i - this.lastGoodY;
							o = j - this.lastGoodZ;
							boolean bl = n > 0.0;
							if (this.player.isOnGround() && !serverboundMovePlayerPacket.isOnGround() && bl) {
								this.player.jumpFromGround();
							}

							this.player.move(MoverType.PLAYER, new Vec3(m, n, o));
							m = h - this.player.getX();
							n = i - this.player.getY();
							if (n > -0.5 || n < 0.5) {
								n = 0.0;
							}

							o = j - this.player.getZ();
							q = m * m + n * n + o * o;
							boolean bl2 = false;
							if (!this.player.isChangingDimension()
								&& q > 0.0625
								&& !this.player.isSleeping()
								&& !this.player.gameMode.isCreative()
								&& this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
								bl2 = true;
								LOGGER.warn("{} moved wrongly!", this.player.getName().getString());
							}

							this.player.absMoveTo(h, i, j, k, l);
							if (this.player.noPhysics
								|| this.player.isSleeping()
								|| (!bl2 || !serverLevel.noCollision(this.player, aABB)) && !this.isPlayerCollidingWithAnythingNew(serverLevel, aABB)) {
								this.clientIsFloating = n >= -0.03125
									&& this.player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR
									&& !this.server.isFlightAllowed()
									&& !this.player.getAbilities().mayfly
									&& !this.player.hasEffect(MobEffects.LEVITATION)
									&& !this.player.isFallFlying()
									&& this.noBlocksAround(this.player);
								this.player.getLevel().getChunkSource().move(this.player);
								this.player.doCheckFallDamage(this.player.getY() - g, serverboundMovePlayerPacket.isOnGround());
								this.player.setOnGround(serverboundMovePlayerPacket.isOnGround());
								if (bl) {
									this.player.fallDistance = 0.0F;
								}

								this.player.checkMovementStatistics(this.player.getX() - d, this.player.getY() - e, this.player.getZ() - f);
								this.lastGoodX = this.player.getX();
								this.lastGoodY = this.player.getY();
								this.lastGoodZ = this.player.getZ();
							} else {
								this.teleport(d, e, f, k, l);
							}
						}
					}
				}
			}
		}
	}

	private boolean isPlayerCollidingWithAnythingNew(LevelReader levelReader, AABB aABB) {
		Stream<VoxelShape> stream = levelReader.getCollisions(this.player, this.player.getBoundingBox().deflate(1.0E-5F), entity -> true);
		VoxelShape voxelShape = Shapes.create(aABB.deflate(1.0E-5F));
		return stream.anyMatch(voxelShape2 -> !Shapes.joinIsNotEmpty(voxelShape2, voxelShape, BooleanOp.AND));
	}

	public void dismount(double d, double e, double f, float g, float h) {
		this.teleport(d, e, f, g, h, Collections.emptySet(), true);
	}

	public void teleport(double d, double e, double f, float g, float h) {
		this.teleport(d, e, f, g, h, Collections.emptySet(), false);
	}

	public void teleport(double d, double e, double f, float g, float h, Set<ClientboundPlayerPositionPacket.RelativeArgument> set) {
		this.teleport(d, e, f, g, h, set, false);
	}

	public void teleport(double d, double e, double f, float g, float h, Set<ClientboundPlayerPositionPacket.RelativeArgument> set, boolean bl) {
		double i = set.contains(ClientboundPlayerPositionPacket.RelativeArgument.X) ? this.player.getX() : 0.0;
		double j = set.contains(ClientboundPlayerPositionPacket.RelativeArgument.Y) ? this.player.getY() : 0.0;
		double k = set.contains(ClientboundPlayerPositionPacket.RelativeArgument.Z) ? this.player.getZ() : 0.0;
		float l = set.contains(ClientboundPlayerPositionPacket.RelativeArgument.Y_ROT) ? this.player.yRot : 0.0F;
		float m = set.contains(ClientboundPlayerPositionPacket.RelativeArgument.X_ROT) ? this.player.xRot : 0.0F;
		this.awaitingPositionFromClient = new Vec3(d, e, f);
		if (++this.awaitingTeleport == Integer.MAX_VALUE) {
			this.awaitingTeleport = 0;
		}

		this.awaitingTeleportTime = this.tickCount;
		this.player.absMoveTo(d, e, f, g, h);
		this.player.connection.send(new ClientboundPlayerPositionPacket(d - i, e - j, f - k, g - l, h - m, set, this.awaitingTeleport, bl));
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
				this.player.gameMode.handleBlockBreakAction(blockPos, action, serverboundPlayerActionPacket.getDirection(), this.player.level.getMaxBuildHeight());
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
		ServerLevel serverLevel = this.player.getLevel();
		InteractionHand interactionHand = serverboundUseItemOnPacket.getHand();
		ItemStack itemStack = this.player.getItemInHand(interactionHand);
		BlockHitResult blockHitResult = serverboundUseItemOnPacket.getHitResult();
		BlockPos blockPos = blockHitResult.getBlockPos();
		Direction direction = blockHitResult.getDirection();
		this.player.resetLastActionTime();
		int i = this.player.level.getMaxBuildHeight();
		if (blockPos.getY() < i) {
			if (this.awaitingPositionFromClient == null
				&& this.player.distanceToSqr((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5) < 64.0
				&& serverLevel.mayInteract(this.player, blockPos)) {
				InteractionResult interactionResult = this.player.gameMode.useItemOn(this.player, serverLevel, itemStack, interactionHand, blockHitResult);
				if (direction == Direction.UP && !interactionResult.consumesAction() && blockPos.getY() >= i - 1 && wasBlockPlacementAttempt(this.player, itemStack)) {
					Component component = new TranslatableComponent("build.tooHigh", i).withStyle(ChatFormatting.RED);
					this.player.sendMessage(component, ChatType.GAME_INFO, Util.NIL_UUID);
				} else if (interactionResult.shouldSwing()) {
					this.player.swing(interactionHand, true);
				}
			}
		} else {
			Component component2 = new TranslatableComponent("build.tooHigh", i).withStyle(ChatFormatting.RED);
			this.player.sendMessage(component2, ChatType.GAME_INFO, Util.NIL_UUID);
		}

		this.player.connection.send(new ClientboundBlockUpdatePacket(serverLevel, blockPos));
		this.player.connection.send(new ClientboundBlockUpdatePacket(serverLevel, blockPos.relative(direction)));
	}

	@Override
	public void handleUseItem(ServerboundUseItemPacket serverboundUseItemPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundUseItemPacket, this, this.player.getLevel());
		ServerLevel serverLevel = this.player.getLevel();
		InteractionHand interactionHand = serverboundUseItemPacket.getHand();
		ItemStack itemStack = this.player.getItemInHand(interactionHand);
		this.player.resetLastActionTime();
		if (!itemStack.isEmpty()) {
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
					this.player.teleportTo(serverLevel, entity.getX(), entity.getY(), entity.getZ(), entity.yRot, entity.xRot);
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
			this.disconnect(new TranslatableComponent("multiplayer.requiredTexturePrompt.disconnect"));
		}
	}

	@Override
	public void handlePaddleBoat(ServerboundPaddleBoatPacket serverboundPaddleBoatPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundPaddleBoatPacket, this, this.player.getLevel());
		Entity entity = this.player.getVehicle();
		if (entity instanceof Boat) {
			((Boat)entity).setPaddleState(serverboundPaddleBoatPacket.getLeft(), serverboundPaddleBoatPacket.getRight());
		}
	}

	@Override
	public void onDisconnect(Component component) {
		LOGGER.info("{} lost connection: {}", this.player.getName().getString(), component.getString());
		this.server.invalidateStatus();
		this.server
			.getPlayerList()
			.broadcastMessage(
				new TranslatableComponent("multiplayer.player.left", this.player.getDisplayName()).withStyle(ChatFormatting.YELLOW), ChatType.SYSTEM, Util.NIL_UUID
			);
		this.player.disconnect();
		this.server.getPlayerList().remove(this.player);
		this.player.getTextFilter().leave();
		if (this.isSingleplayerOwner()) {
			LOGGER.info("Stopping singleplayer server as player logged out");
			this.server.halt(false);
		}
	}

	@Override
	public void send(Packet<?> packet) {
		this.send(packet, null);
	}

	public void send(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> genericFutureListener) {
		try {
			this.connection.send(packet, genericFutureListener);
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
		String string = StringUtils.normalizeSpace(serverboundChatPacket.getMessage());

		for (int i = 0; i < string.length(); i++) {
			if (!SharedConstants.isAllowedChatCharacter(string.charAt(i))) {
				this.disconnect(new TranslatableComponent("multiplayer.disconnect.illegal_characters"));
				return;
			}
		}

		if (string.startsWith("/")) {
			PacketUtils.ensureRunningOnSameThread(serverboundChatPacket, this, this.player.getLevel());
			this.handleChat(TextFilter.FilteredText.passThrough(string));
		} else {
			this.filterTextPacket(string, this::handleChat);
		}
	}

	private void handleChat(TextFilter.FilteredText filteredText) {
		if (this.player.getChatVisibility() == ChatVisiblity.HIDDEN) {
			this.send(new ClientboundChatPacket(new TranslatableComponent("chat.disabled.options").withStyle(ChatFormatting.RED), ChatType.SYSTEM, Util.NIL_UUID));
		} else {
			this.player.resetLastActionTime();
			String string = filteredText.getRaw();
			if (string.startsWith("/")) {
				this.handleCommand(string);
			} else {
				String string2 = filteredText.getFiltered();
				Component component = string2.isEmpty() ? null : new TranslatableComponent("chat.type.text", this.player.getDisplayName(), string2);
				Component component2 = new TranslatableComponent("chat.type.text", this.player.getDisplayName(), string);
				this.server
					.getPlayerList()
					.broadcastMessage(
						component2, serverPlayer -> this.player.shouldFilterMessageTo(serverPlayer) ? component : component2, ChatType.CHAT, this.player.getUUID()
					);
			}

			this.chatSpamTickCount += 20;
			if (this.chatSpamTickCount > 200 && !this.server.getPlayerList().isOp(this.player.getGameProfile())) {
				this.disconnect(new TranslatableComponent("disconnect.spam"));
			}
		}
	}

	private void handleCommand(String string) {
		this.server.getCommands().performCommand(this.player.createCommandSourceStack(), string);
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
				if (this.player.getVehicle() instanceof PlayerRideableJumping) {
					PlayerRideableJumping playerRideableJumping = (PlayerRideableJumping)this.player.getVehicle();
					int i = serverboundPlayerCommandPacket.getData();
					if (playerRideableJumping.canJump() && i > 0) {
						playerRideableJumping.handleStartJump(i);
					}
				}
				break;
			case STOP_RIDING_JUMP:
				if (this.player.getVehicle() instanceof PlayerRideableJumping) {
					PlayerRideableJumping playerRideableJumping = (PlayerRideableJumping)this.player.getVehicle();
					playerRideableJumping.handleStopJump();
				}
				break;
			case OPEN_INVENTORY:
				if (this.player.getVehicle() instanceof AbstractHorse) {
					((AbstractHorse)this.player.getVehicle()).openInventory(this.player);
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

	@Override
	public void handleInteract(ServerboundInteractPacket serverboundInteractPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundInteractPacket, this, this.player.getLevel());
		ServerLevel serverLevel = this.player.getLevel();
		final Entity entity = serverboundInteractPacket.getTarget(serverLevel);
		this.player.resetLastActionTime();
		this.player.setShiftKeyDown(serverboundInteractPacket.isUsingSecondaryAction());
		if (entity != null) {
			double d = 36.0;
			if (this.player.distanceToSqr(entity) < 36.0) {
				serverboundInteractPacket.dispatch(
					new ServerboundInteractPacket.Handler() {
						private void performInteraction(InteractionHand interactionHand, ServerGamePacketListenerImpl.EntityInteraction entityInteraction) {
							ItemStack itemStack = ServerGamePacketListenerImpl.this.player.getItemInHand(interactionHand).copy();
							InteractionResult interactionResult = entityInteraction.run(ServerGamePacketListenerImpl.this.player, entity, interactionHand);
							if (interactionResult.consumesAction()) {
								CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(ServerGamePacketListenerImpl.this.player, itemStack, entity);
								if (interactionResult.shouldSwing()) {
									ServerGamePacketListenerImpl.this.player.swing(interactionHand, true);
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
								ServerGamePacketListenerImpl.this.player.attack(entity);
							} else {
								ServerGamePacketListenerImpl.this.disconnect(new TranslatableComponent("multiplayer.disconnect.invalid_entity_attacked"));
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
		if (this.player.containerMenu.containerId == serverboundContainerClickPacket.getContainerId() && this.player.containerMenu.isSynched(this.player)) {
			if (this.player.isSpectator()) {
				NonNullList<ItemStack> nonNullList = NonNullList.create();

				for (int i = 0; i < this.player.containerMenu.slots.size(); i++) {
					nonNullList.add(this.player.containerMenu.slots.get(i).getItem());
				}

				this.player.refreshContainer(this.player.containerMenu, nonNullList);
			} else {
				ItemStack itemStack = this.player
					.containerMenu
					.clicked(
						serverboundContainerClickPacket.getSlotNum(), serverboundContainerClickPacket.getButtonNum(), serverboundContainerClickPacket.getClickType(), this.player
					);
				if (ItemStack.matches(serverboundContainerClickPacket.getItem(), itemStack)) {
					this.player
						.connection
						.send(new ClientboundContainerAckPacket(serverboundContainerClickPacket.getContainerId(), serverboundContainerClickPacket.getUid(), true));
					this.player.ignoreSlotUpdateHack = true;
					this.player.containerMenu.broadcastChanges();
					this.player.broadcastCarriedItem();
					this.player.ignoreSlotUpdateHack = false;
				} else {
					this.expectedAcks.put(this.player.containerMenu.containerId, serverboundContainerClickPacket.getUid());
					this.player
						.connection
						.send(new ClientboundContainerAckPacket(serverboundContainerClickPacket.getContainerId(), serverboundContainerClickPacket.getUid(), false));
					this.player.containerMenu.setSynched(this.player, false);
					NonNullList<ItemStack> nonNullList2 = NonNullList.create();

					for (int j = 0; j < this.player.containerMenu.slots.size(); j++) {
						ItemStack itemStack2 = this.player.containerMenu.slots.get(j).getItem();
						nonNullList2.add(itemStack2.isEmpty() ? ItemStack.EMPTY : itemStack2);
					}

					this.player.refreshContainer(this.player.containerMenu, nonNullList2);
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
			&& this.player.containerMenu.isSynched(this.player)
			&& this.player.containerMenu instanceof RecipeBookMenu) {
			this.server
				.getRecipeManager()
				.byKey(serverboundPlaceRecipePacket.getRecipe())
				.ifPresent(recipe -> ((RecipeBookMenu)this.player.containerMenu).handlePlacement(serverboundPlaceRecipePacket.isShiftDown(), recipe, this.player));
		}
	}

	@Override
	public void handleContainerButtonClick(ServerboundContainerButtonClickPacket serverboundContainerButtonClickPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundContainerButtonClickPacket, this, this.player.getLevel());
		this.player.resetLastActionTime();
		if (this.player.containerMenu.containerId == serverboundContainerButtonClickPacket.getContainerId()
			&& this.player.containerMenu.isSynched(this.player)
			&& !this.player.isSpectator()) {
			this.player.containerMenu.clickMenuButton(this.player, serverboundContainerButtonClickPacket.getButtonId());
			this.player.containerMenu.broadcastChanges();
		}
	}

	@Override
	public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket serverboundSetCreativeModeSlotPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundSetCreativeModeSlotPacket, this, this.player.getLevel());
		if (this.player.gameMode.isCreative()) {
			boolean bl = serverboundSetCreativeModeSlotPacket.getSlotNum() < 0;
			ItemStack itemStack = serverboundSetCreativeModeSlotPacket.getItem();
			CompoundTag compoundTag = itemStack.getTagElement("BlockEntityTag");
			if (!itemStack.isEmpty() && compoundTag != null && compoundTag.contains("x") && compoundTag.contains("y") && compoundTag.contains("z")) {
				BlockPos blockPos = new BlockPos(compoundTag.getInt("x"), compoundTag.getInt("y"), compoundTag.getInt("z"));
				BlockEntity blockEntity = this.player.level.getBlockEntity(blockPos);
				if (blockEntity != null) {
					CompoundTag compoundTag2 = blockEntity.save(new CompoundTag());
					compoundTag2.remove("x");
					compoundTag2.remove("y");
					compoundTag2.remove("z");
					itemStack.addTagElement("BlockEntityTag", compoundTag2);
				}
			}

			boolean bl2 = serverboundSetCreativeModeSlotPacket.getSlotNum() >= 1 && serverboundSetCreativeModeSlotPacket.getSlotNum() <= 45;
			boolean bl3 = itemStack.isEmpty() || itemStack.getDamageValue() >= 0 && itemStack.getCount() <= 64 && !itemStack.isEmpty();
			if (bl2 && bl3) {
				if (itemStack.isEmpty()) {
					this.player.inventoryMenu.setItem(serverboundSetCreativeModeSlotPacket.getSlotNum(), ItemStack.EMPTY);
				} else {
					this.player.inventoryMenu.setItem(serverboundSetCreativeModeSlotPacket.getSlotNum(), itemStack);
				}

				this.player.inventoryMenu.setSynched(this.player, true);
				this.player.inventoryMenu.broadcastChanges();
			} else if (bl && bl3 && this.dropSpamTickCount < 200) {
				this.dropSpamTickCount += 20;
				this.player.drop(itemStack, true);
			}
		}
	}

	@Override
	public void handleContainerAck(ServerboundContainerAckPacket serverboundContainerAckPacket) {
		PacketUtils.ensureRunningOnSameThread(serverboundContainerAckPacket, this, this.player.getLevel());
		int i = this.player.containerMenu.containerId;
		if (i == serverboundContainerAckPacket.getContainerId()
			&& this.expectedAcks.getOrDefault(i, (short)(serverboundContainerAckPacket.getUid() + 1)) == serverboundContainerAckPacket.getUid()
			&& !this.player.containerMenu.isSynched(this.player)
			&& !this.player.isSpectator()) {
			this.player.containerMenu.setSynched(this.player, true);
		}
	}

	@Override
	public void handleSignUpdate(ServerboundSignUpdatePacket serverboundSignUpdatePacket) {
		List<String> list = (List<String>)Stream.of(serverboundSignUpdatePacket.getLines()).map(ChatFormatting::stripFormatting).collect(Collectors.toList());
		this.filterTextPacket(list, listx -> this.updateSignText(serverboundSignUpdatePacket, listx));
	}

	private void updateSignText(ServerboundSignUpdatePacket serverboundSignUpdatePacket, List<TextFilter.FilteredText> list) {
		this.player.resetLastActionTime();
		ServerLevel serverLevel = this.player.getLevel();
		BlockPos blockPos = serverboundSignUpdatePacket.getPos();
		if (serverLevel.hasChunkAt(blockPos)) {
			BlockState blockState = serverLevel.getBlockState(blockPos);
			BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
			if (!(blockEntity instanceof SignBlockEntity)) {
				return;
			}

			SignBlockEntity signBlockEntity = (SignBlockEntity)blockEntity;
			if (!signBlockEntity.isEditable() || signBlockEntity.getPlayerWhoMayEdit() != this.player) {
				LOGGER.warn("Player {} just tried to change non-editable sign", this.player.getName().getString());
				return;
			}

			for (int i = 0; i < list.size(); i++) {
				TextFilter.FilteredText filteredText = (TextFilter.FilteredText)list.get(i);
				if (this.player.isTextFilteringEnabled()) {
					signBlockEntity.setMessage(i, new TextComponent(filteredText.getFiltered()));
				} else {
					signBlockEntity.setMessage(i, new TextComponent(filteredText.getRaw()), new TextComponent(filteredText.getFiltered()));
				}
			}

			signBlockEntity.setChanged();
			serverLevel.sendBlockUpdated(blockPos, blockState, blockState, 3);
		}
	}

	@Override
	public void handleKeepAlive(ServerboundKeepAlivePacket serverboundKeepAlivePacket) {
		if (this.keepAlivePending && serverboundKeepAlivePacket.getId() == this.keepAliveChallenge) {
			int i = (int)(Util.getMillis() - this.keepAliveTime);
			this.player.latency = (this.player.latency * 3 + i) / 4;
			this.keepAlivePending = false;
		} else if (!this.isSingleplayerOwner()) {
			this.disconnect(new TranslatableComponent("disconnect.timeout"));
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
	public ServerPlayer getPlayer() {
		return this.player;
	}

	@FunctionalInterface
	interface EntityInteraction {
		InteractionResult run(ServerPlayer serverPlayer, Entity entity, InteractionHand interactionHand);
	}
}

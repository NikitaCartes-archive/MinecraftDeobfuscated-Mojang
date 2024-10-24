package net.minecraft.server.level;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ServerPlayerGameMode {
	private static final Logger LOGGER = LogUtils.getLogger();
	protected ServerLevel level;
	protected final ServerPlayer player;
	private GameType gameModeForPlayer = GameType.DEFAULT_MODE;
	@Nullable
	private GameType previousGameModeForPlayer;
	private boolean isDestroyingBlock;
	private int destroyProgressStart;
	private BlockPos destroyPos = BlockPos.ZERO;
	private int gameTicks;
	private boolean hasDelayedDestroy;
	private BlockPos delayedDestroyPos = BlockPos.ZERO;
	private int delayedTickStart;
	private int lastSentState = -1;

	public ServerPlayerGameMode(ServerPlayer serverPlayer) {
		this.player = serverPlayer;
		this.level = serverPlayer.serverLevel();
	}

	public boolean changeGameModeForPlayer(GameType gameType) {
		if (gameType == this.gameModeForPlayer) {
			return false;
		} else {
			this.setGameModeForPlayer(gameType, this.previousGameModeForPlayer);
			this.player.onUpdateAbilities();
			this.player
				.server
				.getPlayerList()
				.broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, this.player));
			this.level.updateSleepingPlayerList();
			if (gameType == GameType.CREATIVE) {
				this.player.resetCurrentImpulseContext();
			}

			return true;
		}
	}

	protected void setGameModeForPlayer(GameType gameType, @Nullable GameType gameType2) {
		this.previousGameModeForPlayer = gameType2;
		this.gameModeForPlayer = gameType;
		gameType.updatePlayerAbilities(this.player.getAbilities());
	}

	public GameType getGameModeForPlayer() {
		return this.gameModeForPlayer;
	}

	@Nullable
	public GameType getPreviousGameModeForPlayer() {
		return this.previousGameModeForPlayer;
	}

	public boolean isSurvival() {
		return this.gameModeForPlayer.isSurvival();
	}

	public boolean isCreative() {
		return this.gameModeForPlayer.isCreative();
	}

	public void tick() {
		this.gameTicks++;
		if (this.hasDelayedDestroy) {
			BlockState blockState = this.level.getBlockState(this.delayedDestroyPos);
			if (blockState.isAir()) {
				this.hasDelayedDestroy = false;
			} else {
				float f = this.incrementDestroyProgress(blockState, this.delayedDestroyPos, this.delayedTickStart);
				if (f >= 1.0F) {
					this.hasDelayedDestroy = false;
					this.destroyBlock(this.delayedDestroyPos);
				}
			}
		} else if (this.isDestroyingBlock) {
			BlockState blockState = this.level.getBlockState(this.destroyPos);
			if (blockState.isAir()) {
				this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
				this.lastSentState = -1;
				this.isDestroyingBlock = false;
			} else {
				this.incrementDestroyProgress(blockState, this.destroyPos, this.destroyProgressStart);
			}
		}
	}

	private float incrementDestroyProgress(BlockState blockState, BlockPos blockPos, int i) {
		int j = this.gameTicks - i;
		float f = blockState.getDestroyProgress(this.player, this.player.level(), blockPos) * (float)(j + 1);
		int k = (int)(f * 10.0F);
		if (k != this.lastSentState) {
			this.level.destroyBlockProgress(this.player.getId(), blockPos, k);
			this.lastSentState = k;
		}

		return f;
	}

	private void debugLogging(BlockPos blockPos, boolean bl, int i, String string) {
	}

	public void handleBlockBreakAction(BlockPos blockPos, ServerboundPlayerActionPacket.Action action, Direction direction, int i, int j) {
		if (!this.player.canInteractWithBlock(blockPos, 1.0)) {
			this.debugLogging(blockPos, false, j, "too far");
		} else if (blockPos.getY() > i) {
			this.player.connection.send(new ClientboundBlockUpdatePacket(blockPos, this.level.getBlockState(blockPos)));
			this.debugLogging(blockPos, false, j, "too high");
		} else {
			if (action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
				if (!this.level.mayInteract(this.player, blockPos)) {
					this.player.connection.send(new ClientboundBlockUpdatePacket(blockPos, this.level.getBlockState(blockPos)));
					this.debugLogging(blockPos, false, j, "may not interact");
					return;
				}

				if (this.isCreative()) {
					this.destroyAndAck(blockPos, j, "creative destroy");
					return;
				}

				if (this.player.blockActionRestricted(this.level, blockPos, this.gameModeForPlayer)) {
					this.player.connection.send(new ClientboundBlockUpdatePacket(blockPos, this.level.getBlockState(blockPos)));
					this.debugLogging(blockPos, false, j, "block action restricted");
					return;
				}

				this.destroyProgressStart = this.gameTicks;
				float f = 1.0F;
				BlockState blockState = this.level.getBlockState(blockPos);
				if (!blockState.isAir()) {
					EnchantmentHelper.onHitBlock(
						this.level,
						this.player.getMainHandItem(),
						this.player,
						this.player,
						EquipmentSlot.MAINHAND,
						Vec3.atCenterOf(blockPos),
						blockState,
						item -> this.player.onEquippedItemBroken(item, EquipmentSlot.MAINHAND)
					);
					blockState.attack(this.level, blockPos, this.player);
					f = blockState.getDestroyProgress(this.player, this.player.level(), blockPos);
				}

				if (!blockState.isAir() && f >= 1.0F) {
					this.destroyAndAck(blockPos, j, "insta mine");
				} else {
					if (this.isDestroyingBlock) {
						this.player.connection.send(new ClientboundBlockUpdatePacket(this.destroyPos, this.level.getBlockState(this.destroyPos)));
						this.debugLogging(blockPos, false, j, "abort destroying since another started (client insta mine, server disagreed)");
					}

					this.isDestroyingBlock = true;
					this.destroyPos = blockPos.immutable();
					int k = (int)(f * 10.0F);
					this.level.destroyBlockProgress(this.player.getId(), blockPos, k);
					this.debugLogging(blockPos, true, j, "actual start of destroying");
					this.lastSentState = k;
				}
			} else if (action == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
				if (blockPos.equals(this.destroyPos)) {
					int l = this.gameTicks - this.destroyProgressStart;
					BlockState blockStatex = this.level.getBlockState(blockPos);
					if (!blockStatex.isAir()) {
						float g = blockStatex.getDestroyProgress(this.player, this.player.level(), blockPos) * (float)(l + 1);
						if (g >= 0.7F) {
							this.isDestroyingBlock = false;
							this.level.destroyBlockProgress(this.player.getId(), blockPos, -1);
							this.destroyAndAck(blockPos, j, "destroyed");
							return;
						}

						if (!this.hasDelayedDestroy) {
							this.isDestroyingBlock = false;
							this.hasDelayedDestroy = true;
							this.delayedDestroyPos = blockPos;
							this.delayedTickStart = this.destroyProgressStart;
						}
					}
				}

				this.debugLogging(blockPos, true, j, "stopped destroying");
			} else if (action == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
				this.isDestroyingBlock = false;
				if (!Objects.equals(this.destroyPos, blockPos)) {
					LOGGER.warn("Mismatch in destroy block pos: {} {}", this.destroyPos, blockPos);
					this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
					this.debugLogging(blockPos, true, j, "aborted mismatched destroying");
				}

				this.level.destroyBlockProgress(this.player.getId(), blockPos, -1);
				this.debugLogging(blockPos, true, j, "aborted destroying");
			}
		}
	}

	public void destroyAndAck(BlockPos blockPos, int i, String string) {
		if (this.destroyBlock(blockPos)) {
			this.debugLogging(blockPos, true, i, string);
		} else {
			this.player.connection.send(new ClientboundBlockUpdatePacket(blockPos, this.level.getBlockState(blockPos)));
			this.debugLogging(blockPos, false, i, string);
		}
	}

	public boolean destroyBlock(BlockPos blockPos) {
		BlockState blockState = this.level.getBlockState(blockPos);
		if (!this.player.getMainHandItem().getItem().canAttackBlock(blockState, this.level, blockPos, this.player)) {
			return false;
		} else {
			BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
			Block block = blockState.getBlock();
			if (block instanceof GameMasterBlock && !this.player.canUseGameMasterBlocks()) {
				this.level.sendBlockUpdated(blockPos, blockState, blockState, 3);
				return false;
			} else if (this.player.blockActionRestricted(this.level, blockPos, this.gameModeForPlayer)) {
				return false;
			} else {
				BlockState blockState2 = block.playerWillDestroy(this.level, blockPos, blockState, this.player);
				boolean bl = this.level.removeBlock(blockPos, false);
				if (bl) {
					block.destroy(this.level, blockPos, blockState2);
				}

				if (this.isCreative()) {
					return true;
				} else {
					ItemStack itemStack = this.player.getMainHandItem();
					ItemStack itemStack2 = itemStack.copy();
					boolean bl2 = this.player.hasCorrectToolForDrops(blockState2);
					itemStack.mineBlock(this.level, blockState2, blockPos, this.player);
					if (bl && bl2) {
						block.playerDestroy(this.level, this.player, blockPos, blockState2, blockEntity, itemStack2);
					}

					return true;
				}
			}
		}
	}

	public InteractionResult useItem(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand) {
		if (this.gameModeForPlayer == GameType.SPECTATOR) {
			return InteractionResult.PASS;
		} else if (serverPlayer.getCooldowns().isOnCooldown(itemStack)) {
			return InteractionResult.PASS;
		} else {
			int i = itemStack.getCount();
			int j = itemStack.getDamageValue();
			InteractionResult interactionResult = itemStack.use(level, serverPlayer, interactionHand);
			ItemStack itemStack2;
			if (interactionResult instanceof InteractionResult.Success success) {
				itemStack2 = (ItemStack)Objects.requireNonNullElse(success.heldItemTransformedTo(), serverPlayer.getItemInHand(interactionHand));
			} else {
				itemStack2 = serverPlayer.getItemInHand(interactionHand);
			}

			if (itemStack2 == itemStack && itemStack2.getCount() == i && itemStack2.getUseDuration(serverPlayer) <= 0 && itemStack2.getDamageValue() == j) {
				return interactionResult;
			} else if (interactionResult instanceof InteractionResult.Fail && itemStack2.getUseDuration(serverPlayer) > 0 && !serverPlayer.isUsingItem()) {
				return interactionResult;
			} else {
				if (itemStack != itemStack2) {
					serverPlayer.setItemInHand(interactionHand, itemStack2);
				}

				if (itemStack2.isEmpty()) {
					serverPlayer.setItemInHand(interactionHand, ItemStack.EMPTY);
				}

				if (!serverPlayer.isUsingItem()) {
					serverPlayer.inventoryMenu.sendAllDataToRemote();
				}

				return interactionResult;
			}
		}
	}

	public InteractionResult useItemOn(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand, BlockHitResult blockHitResult) {
		BlockPos blockPos = blockHitResult.getBlockPos();
		BlockState blockState = level.getBlockState(blockPos);
		if (!blockState.getBlock().isEnabled(level.enabledFeatures())) {
			return InteractionResult.FAIL;
		} else if (this.gameModeForPlayer == GameType.SPECTATOR) {
			MenuProvider menuProvider = blockState.getMenuProvider(level, blockPos);
			if (menuProvider != null) {
				serverPlayer.openMenu(menuProvider);
				return InteractionResult.CONSUME;
			} else {
				return InteractionResult.PASS;
			}
		} else {
			boolean bl = !serverPlayer.getMainHandItem().isEmpty() || !serverPlayer.getOffhandItem().isEmpty();
			boolean bl2 = serverPlayer.isSecondaryUseActive() && bl;
			ItemStack itemStack2 = itemStack.copy();
			if (!bl2) {
				InteractionResult interactionResult = blockState.useItemOn(
					serverPlayer.getItemInHand(interactionHand), level, serverPlayer, interactionHand, blockHitResult
				);
				if (interactionResult.consumesAction()) {
					CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, blockPos, itemStack2);
					return interactionResult;
				}

				if (interactionResult instanceof InteractionResult.TryEmptyHandInteraction && interactionHand == InteractionHand.MAIN_HAND) {
					InteractionResult interactionResult2 = blockState.useWithoutItem(level, serverPlayer, blockHitResult);
					if (interactionResult2.consumesAction()) {
						CriteriaTriggers.DEFAULT_BLOCK_USE.trigger(serverPlayer, blockPos);
						return interactionResult2;
					}
				}
			}

			if (!itemStack.isEmpty() && !serverPlayer.getCooldowns().isOnCooldown(itemStack)) {
				UseOnContext useOnContext = new UseOnContext(serverPlayer, interactionHand, blockHitResult);
				InteractionResult interactionResult2;
				if (this.isCreative()) {
					int i = itemStack.getCount();
					interactionResult2 = itemStack.useOn(useOnContext);
					itemStack.setCount(i);
				} else {
					interactionResult2 = itemStack.useOn(useOnContext);
				}

				if (interactionResult2.consumesAction()) {
					CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, blockPos, itemStack2);
				}

				return interactionResult2;
			} else {
				return InteractionResult.PASS;
			}
		}
	}

	public void setLevel(ServerLevel serverLevel) {
		this.level = serverLevel;
	}
}

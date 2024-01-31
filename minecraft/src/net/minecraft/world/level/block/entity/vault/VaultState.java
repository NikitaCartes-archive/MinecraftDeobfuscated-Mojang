package net.minecraft.world.level.block.entity.vault;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public enum VaultState implements StringRepresentable {
	INACTIVE("inactive", VaultState.LightLevel.HALF_LIT) {
		@Override
		protected void onEnter(ServerLevel serverLevel, BlockPos blockPos, VaultConfig vaultConfig, VaultSharedData vaultSharedData) {
			vaultSharedData.setDisplayItem(ItemStack.EMPTY);
			serverLevel.levelEvent(3016, blockPos, 0);
		}
	},
	ACTIVE("active", VaultState.LightLevel.LIT) {
		@Override
		protected void onEnter(ServerLevel serverLevel, BlockPos blockPos, VaultConfig vaultConfig, VaultSharedData vaultSharedData) {
			if (!vaultSharedData.hasDisplayItem()) {
				VaultBlockEntity.Server.cycleDisplayItemFromLootTable(serverLevel, this, vaultConfig, vaultSharedData, blockPos);
			}

			serverLevel.levelEvent(3015, blockPos, 0);
		}
	},
	UNLOCKING("unlocking", VaultState.LightLevel.LIT) {
		@Override
		protected void onEnter(ServerLevel serverLevel, BlockPos blockPos, VaultConfig vaultConfig, VaultSharedData vaultSharedData) {
			serverLevel.playSound(null, blockPos, SoundEvents.VAULT_INSERT_ITEM, SoundSource.BLOCKS);
		}
	},
	EJECTING("ejecting", VaultState.LightLevel.LIT) {
		@Override
		protected void onEnter(ServerLevel serverLevel, BlockPos blockPos, VaultConfig vaultConfig, VaultSharedData vaultSharedData) {
			serverLevel.playSound(null, blockPos, SoundEvents.VAULT_OPEN_SHUTTER, SoundSource.BLOCKS);
		}

		@Override
		protected void onExit(ServerLevel serverLevel, BlockPos blockPos, VaultConfig vaultConfig, VaultSharedData vaultSharedData) {
			serverLevel.playSound(null, blockPos, SoundEvents.VAULT_CLOSE_SHUTTER, SoundSource.BLOCKS);
		}
	};

	private static final int UPDATE_CONNECTED_PLAYERS_TICK_RATE = 20;
	private static final int DELAY_BETWEEN_EJECTIONS_TICKS = 20;
	private static final int DELAY_AFTER_LAST_EJECTION_TICKS = 20;
	private static final int DELAY_BEFORE_FIRST_EJECTION_TICKS = 20;
	private final String stateName;
	private final VaultState.LightLevel lightLevel;

	VaultState(String string2, VaultState.LightLevel lightLevel) {
		this.stateName = string2;
		this.lightLevel = lightLevel;
	}

	@Override
	public String getSerializedName() {
		return this.stateName;
	}

	public int lightLevel() {
		return this.lightLevel.value;
	}

	public VaultState tickAndGetNext(
		ServerLevel serverLevel, BlockPos blockPos, VaultConfig vaultConfig, VaultServerData vaultServerData, VaultSharedData vaultSharedData
	) {
		return switch (this) {
			case INACTIVE -> updateStateForConnectedPlayers(serverLevel, blockPos, vaultConfig, vaultServerData, vaultSharedData, vaultConfig.activationRange());
			case ACTIVE -> updateStateForConnectedPlayers(serverLevel, blockPos, vaultConfig, vaultServerData, vaultSharedData, vaultConfig.deactivationRange());
			case UNLOCKING -> {
				vaultServerData.pauseStateUpdatingUntil(serverLevel.getGameTime() + 20L);
				yield EJECTING;
			}
			case EJECTING -> {
				if (vaultServerData.getItemsToEject().isEmpty()) {
					vaultServerData.markEjectionFinished();
					yield updateStateForConnectedPlayers(serverLevel, blockPos, vaultConfig, vaultServerData, vaultSharedData, vaultConfig.deactivationRange());
				} else {
					float f = vaultServerData.ejectionProgress();
					this.ejectResultItem(serverLevel, blockPos, vaultServerData.popNextItemToEject(), f);
					vaultSharedData.setDisplayItem(vaultServerData.getNextItemToEject());
					boolean bl = vaultServerData.getItemsToEject().isEmpty();
					int i = bl ? 20 : 20;
					vaultServerData.pauseStateUpdatingUntil(serverLevel.getGameTime() + (long)i);
					yield EJECTING;
				}
			}
		};
	}

	private static VaultState updateStateForConnectedPlayers(
		ServerLevel serverLevel, BlockPos blockPos, VaultConfig vaultConfig, VaultServerData vaultServerData, VaultSharedData vaultSharedData, double d
	) {
		vaultSharedData.updateConnectedPlayersWithinRange(serverLevel, blockPos, vaultServerData, vaultConfig, d);
		vaultServerData.pauseStateUpdatingUntil(serverLevel.getGameTime() + 20L);
		return vaultSharedData.hasConnectedPlayers() ? ACTIVE : INACTIVE;
	}

	public void onTransition(ServerLevel serverLevel, BlockPos blockPos, VaultState vaultState, VaultConfig vaultConfig, VaultSharedData vaultSharedData) {
		this.onExit(serverLevel, blockPos, vaultConfig, vaultSharedData);
		vaultState.onEnter(serverLevel, blockPos, vaultConfig, vaultSharedData);
	}

	protected void onEnter(ServerLevel serverLevel, BlockPos blockPos, VaultConfig vaultConfig, VaultSharedData vaultSharedData) {
	}

	protected void onExit(ServerLevel serverLevel, BlockPos blockPos, VaultConfig vaultConfig, VaultSharedData vaultSharedData) {
	}

	private void ejectResultItem(ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, float f) {
		DefaultDispenseItemBehavior.spawnItem(serverLevel, itemStack, 2, Direction.UP, Vec3.atBottomCenterOf(blockPos).relative(Direction.UP, 1.2));
		serverLevel.levelEvent(3017, blockPos, 0);
		serverLevel.playSound(null, blockPos, SoundEvents.VAULT_EJECT_ITEM, SoundSource.BLOCKS, 1.0F, 0.8F + 0.4F * f);
	}

	static enum LightLevel {
		HALF_LIT(6),
		LIT(12);

		final int value;

		private LightLevel(int j) {
			this.value = j;
		}
	}
}

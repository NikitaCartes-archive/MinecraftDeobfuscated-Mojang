package net.minecraft.world.level.block.entity.vault;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class VaultBlockEntity extends BlockEntity {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final VaultServerData serverData = new VaultServerData();
	private final VaultSharedData sharedData = new VaultSharedData();
	private final VaultClientData clientData = new VaultClientData();
	private VaultConfig config = VaultConfig.DEFAULT;

	public VaultBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.VAULT, blockPos, blockState);
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		return Util.make(new CompoundTag(), compoundTag -> compoundTag.put("shared_data", encode(VaultSharedData.CODEC, this.sharedData, provider)));
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
		compoundTag.put("config", encode(VaultConfig.CODEC, this.config, provider));
		compoundTag.put("shared_data", encode(VaultSharedData.CODEC, this.sharedData, provider));
		compoundTag.put("server_data", encode(VaultServerData.CODEC, this.serverData, provider));
	}

	private static <T> Tag encode(Codec<T> codec, T object, HolderLookup.Provider provider) {
		return Util.getOrThrow(codec.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), object), IllegalStateException::new);
	}

	@Override
	public void load(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.load(compoundTag, provider);
		DynamicOps<Tag> dynamicOps = provider.createSerializationContext(NbtOps.INSTANCE);
		if (compoundTag.contains("server_data")) {
			VaultServerData.CODEC.parse(dynamicOps, compoundTag.get("server_data")).resultOrPartial(LOGGER::error).ifPresent(this.serverData::set);
		}

		if (compoundTag.contains("config")) {
			VaultConfig.CODEC.parse(dynamicOps, compoundTag.get("config")).resultOrPartial(LOGGER::error).ifPresent(vaultConfig -> this.config = vaultConfig);
		}

		if (compoundTag.contains("shared_data")) {
			VaultSharedData.CODEC.parse(dynamicOps, compoundTag.get("shared_data")).resultOrPartial(LOGGER::error).ifPresent(this.sharedData::set);
		}
	}

	@Nullable
	public VaultServerData getServerData() {
		return this.level != null && !this.level.isClientSide ? this.serverData : null;
	}

	public VaultSharedData getSharedData() {
		return this.sharedData;
	}

	public VaultClientData getClientData() {
		return this.clientData;
	}

	public VaultConfig getConfig() {
		return this.config;
	}

	@VisibleForTesting
	public void setConfig(VaultConfig vaultConfig) {
		this.config = vaultConfig;
	}

	public static final class Client {
		private static final int PARTICLE_TICK_RATE = 20;
		private static final float IDLE_PARTICLE_CHANCE = 0.5F;
		private static final float AMBIENT_SOUND_CHANCE = 0.02F;
		private static final int ACTIVATION_PARTICLE_COUNT = 20;
		private static final int DEACTIVATION_PARTICLE_COUNT = 20;

		public static void tick(Level level, BlockPos blockPos, BlockState blockState, VaultClientData vaultClientData, VaultSharedData vaultSharedData) {
			vaultClientData.updateDisplayItemSpin();
			if (level.getGameTime() % 20L == 0L) {
				emitConnectionParticlesForNearbyPlayers(level, blockPos, blockState, vaultSharedData);
			}

			emitIdleParticles(level, blockPos, vaultSharedData);
			playIdleSounds(level, blockPos, vaultSharedData);
		}

		public static void emitActivationParticles(Level level, BlockPos blockPos, BlockState blockState, VaultSharedData vaultSharedData) {
			emitConnectionParticlesForNearbyPlayers(level, blockPos, blockState, vaultSharedData);
			RandomSource randomSource = level.random;

			for (int i = 0; i < 20; i++) {
				Vec3 vec3 = randomPosInsideCage(blockPos, randomSource);
				level.addParticle(ParticleTypes.SMOKE, vec3.x(), vec3.y(), vec3.z(), 0.0, 0.0, 0.0);
				level.addParticle(ParticleTypes.SMALL_FLAME, vec3.x(), vec3.y(), vec3.z(), 0.0, 0.0, 0.0);
			}
		}

		public static void emitDeactivationParticles(Level level, BlockPos blockPos) {
			RandomSource randomSource = level.random;

			for (int i = 0; i < 20; i++) {
				Vec3 vec3 = randomPosCenterOfCage(blockPos, randomSource);
				Vec3 vec32 = new Vec3(randomSource.nextGaussian() * 0.02, randomSource.nextGaussian() * 0.02, randomSource.nextGaussian() * 0.02);
				level.addParticle(ParticleTypes.SMALL_FLAME, vec3.x(), vec3.y(), vec3.z(), vec32.x(), vec32.y(), vec32.z());
			}
		}

		private static void emitIdleParticles(Level level, BlockPos blockPos, VaultSharedData vaultSharedData) {
			RandomSource randomSource = level.getRandom();
			if (randomSource.nextFloat() <= 0.5F) {
				Vec3 vec3 = randomPosInsideCage(blockPos, randomSource);
				level.addParticle(ParticleTypes.SMOKE, vec3.x(), vec3.y(), vec3.z(), 0.0, 0.0, 0.0);
				if (shouldDisplayActiveEffects(vaultSharedData)) {
					level.addParticle(ParticleTypes.SMALL_FLAME, vec3.x(), vec3.y(), vec3.z(), 0.0, 0.0, 0.0);
				}
			}
		}

		private static void emitConnectionParticlesForPlayer(Level level, Vec3 vec3, Player player) {
			RandomSource randomSource = level.random;
			Vec3 vec32 = vec3.vectorTo(player.position().add(0.0, (double)(player.getBbHeight() / 2.0F), 0.0));
			int i = Mth.nextInt(randomSource, 2, 5);

			for (int j = 0; j < i; j++) {
				Vec3 vec33 = vec32.offsetRandom(randomSource, 1.0F);
				level.addParticle(ParticleTypes.VAULT_CONNECTION, vec3.x(), vec3.y(), vec3.z(), vec33.x(), vec33.y(), vec33.z());
			}
		}

		private static void emitConnectionParticlesForNearbyPlayers(Level level, BlockPos blockPos, BlockState blockState, VaultSharedData vaultSharedData) {
			Set<UUID> set = vaultSharedData.getConnectedPlayers();
			if (!set.isEmpty()) {
				Vec3 vec3 = keyholePos(blockPos, blockState.getValue(VaultBlock.FACING));

				for (UUID uUID : set) {
					Player player = level.getPlayerByUUID(uUID);
					if (player != null && isWithinConnectionRange(blockPos, vaultSharedData, player)) {
						emitConnectionParticlesForPlayer(level, vec3, player);
					}
				}
			}
		}

		private static boolean isWithinConnectionRange(BlockPos blockPos, VaultSharedData vaultSharedData, Player player) {
			return player.blockPosition().distSqr(blockPos) <= Mth.square(vaultSharedData.connectedParticlesRange());
		}

		private static void playIdleSounds(Level level, BlockPos blockPos, VaultSharedData vaultSharedData) {
			if (shouldDisplayActiveEffects(vaultSharedData)) {
				RandomSource randomSource = level.getRandom();
				if (randomSource.nextFloat() <= 0.02F) {
					level.playLocalSound(
						blockPos, SoundEvents.VAULT_AMBIENT, SoundSource.BLOCKS, randomSource.nextFloat() * 0.25F + 0.75F, randomSource.nextFloat() + 0.5F, false
					);
				}
			}
		}

		public static boolean shouldDisplayActiveEffects(VaultSharedData vaultSharedData) {
			return vaultSharedData.hasDisplayItem();
		}

		private static Vec3 randomPosCenterOfCage(BlockPos blockPos, RandomSource randomSource) {
			return Vec3.atLowerCornerOf(blockPos)
				.add(Mth.nextDouble(randomSource, 0.4, 0.6), Mth.nextDouble(randomSource, 0.4, 0.6), Mth.nextDouble(randomSource, 0.4, 0.6));
		}

		private static Vec3 randomPosInsideCage(BlockPos blockPos, RandomSource randomSource) {
			return Vec3.atLowerCornerOf(blockPos)
				.add(Mth.nextDouble(randomSource, 0.1, 0.9), Mth.nextDouble(randomSource, 0.25, 0.75), Mth.nextDouble(randomSource, 0.1, 0.9));
		}

		private static Vec3 keyholePos(BlockPos blockPos, Direction direction) {
			return Vec3.atBottomCenterOf(blockPos).add((double)direction.getStepX() * 0.5, 1.75, (double)direction.getStepZ() * 0.5);
		}
	}

	public static final class Server {
		private static final int UNLOCKING_DELAY_TICKS = 14;
		private static final int DISPLAY_CYCLE_TICK_RATE = 20;
		private static final int INSERT_FAIL_SOUND_BUFFER_TICKS = 15;

		public static void tick(
			ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, VaultConfig vaultConfig, VaultServerData vaultServerData, VaultSharedData vaultSharedData
		) {
			VaultState vaultState = blockState.getValue(VaultBlock.STATE);
			if (shouldCycleDisplayItem(serverLevel.getGameTime(), vaultState)) {
				cycleDisplayItemFromLootTable(serverLevel, vaultState, vaultConfig, vaultSharedData, blockPos);
			}

			BlockState blockState2 = blockState;
			if (serverLevel.getGameTime() >= vaultServerData.stateUpdatingResumesAt()) {
				blockState2 = blockState.setValue(VaultBlock.STATE, vaultState.tickAndGetNext(serverLevel, blockPos, vaultConfig, vaultServerData, vaultSharedData));
				if (!blockState.equals(blockState2)) {
					setVaultState(serverLevel, blockPos, blockState, blockState2, vaultConfig, vaultSharedData);
				}
			}

			if (vaultServerData.isDirty || vaultSharedData.isDirty) {
				VaultBlockEntity.setChanged(serverLevel, blockPos, blockState);
				if (vaultSharedData.isDirty) {
					serverLevel.sendBlockUpdated(blockPos, blockState, blockState2, 2);
				}

				vaultServerData.isDirty = false;
				vaultSharedData.isDirty = false;
			}
		}

		public static void tryInsertKey(
			ServerLevel serverLevel,
			BlockPos blockPos,
			BlockState blockState,
			VaultConfig vaultConfig,
			VaultServerData vaultServerData,
			VaultSharedData vaultSharedData,
			Player player,
			ItemStack itemStack
		) {
			VaultState vaultState = blockState.getValue(VaultBlock.STATE);
			if (canEjectReward(vaultConfig, vaultState)) {
				if (!isValidToInsert(vaultConfig, itemStack)) {
					playInsertFailSound(serverLevel, vaultServerData, blockPos);
				} else if (vaultServerData.hasRewardedPlayer(player)) {
					playInsertFailSound(serverLevel, vaultServerData, blockPos);
				} else {
					List<ItemStack> list = resolveItemsToEject(serverLevel, vaultConfig, blockPos, player);
					if (!list.isEmpty()) {
						player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
						if (!player.isCreative()) {
							itemStack.shrink(vaultConfig.keyItem().getCount());
						}

						unlock(serverLevel, blockState, blockPos, vaultConfig, vaultServerData, vaultSharedData, list);
						vaultServerData.addToRewardedPlayers(player);
						vaultSharedData.updateConnectedPlayersWithinRange(serverLevel, blockPos, vaultServerData, vaultConfig, vaultConfig.deactivationRange());
					}
				}
			}
		}

		static void setVaultState(
			ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, BlockState blockState2, VaultConfig vaultConfig, VaultSharedData vaultSharedData
		) {
			VaultState vaultState = blockState.getValue(VaultBlock.STATE);
			VaultState vaultState2 = blockState2.getValue(VaultBlock.STATE);
			serverLevel.setBlock(blockPos, blockState2, 3);
			vaultState.onTransition(serverLevel, blockPos, vaultState2, vaultConfig, vaultSharedData);
		}

		static void cycleDisplayItemFromLootTable(
			ServerLevel serverLevel, VaultState vaultState, VaultConfig vaultConfig, VaultSharedData vaultSharedData, BlockPos blockPos
		) {
			if (!canEjectReward(vaultConfig, vaultState)) {
				vaultSharedData.setDisplayItem(ItemStack.EMPTY);
			} else {
				ItemStack itemStack = getRandomDisplayItemFromLootTable(
					serverLevel, blockPos, (ResourceLocation)vaultConfig.overrideLootTableToDisplay().orElse(vaultConfig.lootTable())
				);
				vaultSharedData.setDisplayItem(itemStack);
			}
		}

		private static ItemStack getRandomDisplayItemFromLootTable(ServerLevel serverLevel, BlockPos blockPos, ResourceLocation resourceLocation) {
			LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(resourceLocation);
			LootParams lootParams = new LootParams.Builder(serverLevel)
				.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos))
				.create(LootContextParamSets.VAULT);
			List<ItemStack> list = lootTable.getRandomItems(lootParams);
			return list.isEmpty() ? ItemStack.EMPTY : Util.getRandom(list, serverLevel.getRandom());
		}

		private static void unlock(
			ServerLevel serverLevel,
			BlockState blockState,
			BlockPos blockPos,
			VaultConfig vaultConfig,
			VaultServerData vaultServerData,
			VaultSharedData vaultSharedData,
			List<ItemStack> list
		) {
			vaultServerData.setItemsToEject(list);
			vaultSharedData.setDisplayItem(vaultServerData.getNextItemToEject());
			vaultServerData.pauseStateUpdatingUntil(serverLevel.getGameTime() + 14L);
			setVaultState(serverLevel, blockPos, blockState, blockState.setValue(VaultBlock.STATE, VaultState.UNLOCKING), vaultConfig, vaultSharedData);
		}

		private static List<ItemStack> resolveItemsToEject(ServerLevel serverLevel, VaultConfig vaultConfig, BlockPos blockPos, Player player) {
			LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(vaultConfig.lootTable());
			LootParams lootParams = new LootParams.Builder(serverLevel)
				.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos))
				.withLuck(player.getLuck())
				.withParameter(LootContextParams.THIS_ENTITY, player)
				.create(LootContextParamSets.VAULT);
			return lootTable.getRandomItems(lootParams);
		}

		private static boolean canEjectReward(VaultConfig vaultConfig, VaultState vaultState) {
			return !vaultConfig.lootTable().equals(BuiltInLootTables.EMPTY) && !vaultConfig.keyItem().isEmpty() && vaultState != VaultState.INACTIVE;
		}

		private static boolean isValidToInsert(VaultConfig vaultConfig, ItemStack itemStack) {
			return ItemStack.isSameItemSameComponents(itemStack, vaultConfig.keyItem()) && itemStack.getCount() >= vaultConfig.keyItem().getCount();
		}

		private static boolean shouldCycleDisplayItem(long l, VaultState vaultState) {
			return l % 20L == 0L && vaultState == VaultState.ACTIVE;
		}

		private static void playInsertFailSound(ServerLevel serverLevel, VaultServerData vaultServerData, BlockPos blockPos) {
			if (serverLevel.getGameTime() >= vaultServerData.getLastInsertFailTimestamp() + 15L) {
				serverLevel.playSound(null, blockPos, SoundEvents.VAULT_INSERT_ITEM_FAIL, SoundSource.BLOCKS);
				vaultServerData.setLastInsertFailTimestamp(serverLevel.getGameTime());
			}
		}
	}
}

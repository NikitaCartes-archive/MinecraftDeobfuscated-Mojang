package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class TheEndGatewayBlockEntity extends TheEndPortalBlockEntity {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int SPAWN_TIME = 200;
	private static final int COOLDOWN_TIME = 40;
	private static final int ATTENTION_INTERVAL = 2400;
	private static final int EVENT_COOLDOWN = 1;
	private static final int GATEWAY_HEIGHT_ABOVE_SURFACE = 10;
	private long age;
	private int teleportCooldown;
	@Nullable
	private BlockPos exitPortal;
	private boolean exactTeleport;

	public TheEndGatewayBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.END_GATEWAY, blockPos, blockState);
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
		compoundTag.putLong("Age", this.age);
		if (this.exitPortal != null) {
			compoundTag.put("exit_portal", NbtUtils.writeBlockPos(this.exitPortal));
		}

		if (this.exactTeleport) {
			compoundTag.putBoolean("ExactTeleport", true);
		}
	}

	@Override
	protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.loadAdditional(compoundTag, provider);
		this.age = compoundTag.getLong("Age");
		NbtUtils.readBlockPos(compoundTag, "exit_portal").filter(Level::isInSpawnableBounds).ifPresent(blockPos -> this.exitPortal = blockPos);
		this.exactTeleport = compoundTag.getBoolean("ExactTeleport");
	}

	public static void beamAnimationTick(Level level, BlockPos blockPos, BlockState blockState, TheEndGatewayBlockEntity theEndGatewayBlockEntity) {
		theEndGatewayBlockEntity.age++;
		if (theEndGatewayBlockEntity.isCoolingDown()) {
			theEndGatewayBlockEntity.teleportCooldown--;
		}
	}

	public static void portalTick(Level level, BlockPos blockPos, BlockState blockState, TheEndGatewayBlockEntity theEndGatewayBlockEntity) {
		boolean bl = theEndGatewayBlockEntity.isSpawning();
		boolean bl2 = theEndGatewayBlockEntity.isCoolingDown();
		theEndGatewayBlockEntity.age++;
		if (bl2) {
			theEndGatewayBlockEntity.teleportCooldown--;
		} else if (theEndGatewayBlockEntity.age % 2400L == 0L) {
			triggerCooldown(level, blockPos, blockState, theEndGatewayBlockEntity);
		}

		if (bl != theEndGatewayBlockEntity.isSpawning() || bl2 != theEndGatewayBlockEntity.isCoolingDown()) {
			setChanged(level, blockPos, blockState);
		}
	}

	public boolean isSpawning() {
		return this.age < 200L;
	}

	public boolean isCoolingDown() {
		return this.teleportCooldown > 0;
	}

	public float getSpawnPercent(float f) {
		return Mth.clamp(((float)this.age + f) / 200.0F, 0.0F, 1.0F);
	}

	public float getCooldownPercent(float f) {
		return 1.0F - Mth.clamp(((float)this.teleportCooldown - f) / 40.0F, 0.0F, 1.0F);
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		return this.saveCustomOnly(provider);
	}

	public static void triggerCooldown(Level level, BlockPos blockPos, BlockState blockState, TheEndGatewayBlockEntity theEndGatewayBlockEntity) {
		if (!level.isClientSide) {
			theEndGatewayBlockEntity.teleportCooldown = 40;
			level.blockEvent(blockPos, blockState.getBlock(), 1, 0);
			setChanged(level, blockPos, blockState);
		}
	}

	@Override
	public boolean triggerEvent(int i, int j) {
		if (i == 1) {
			this.teleportCooldown = 40;
			return true;
		} else {
			return super.triggerEvent(i, j);
		}
	}

	@Nullable
	public Vec3 getPortalPosition(ServerLevel serverLevel, BlockPos blockPos) {
		if (this.exitPortal == null && serverLevel.dimension() == Level.END) {
			BlockPos blockPos2 = findOrCreateValidTeleportPos(serverLevel, blockPos);
			blockPos2 = blockPos2.above(10);
			LOGGER.debug("Creating portal at {}", blockPos2);
			spawnGatewayPortal(serverLevel, blockPos2, EndGatewayConfiguration.knownExit(blockPos, false));
			this.exitPortal = blockPos2;
		}

		if (this.exitPortal != null) {
			BlockPos blockPos2 = this.exactTeleport ? this.exitPortal : findExitPosition(serverLevel, this.exitPortal);
			return new Vec3((double)blockPos2.getX() + 0.5, (double)blockPos2.getY(), (double)blockPos2.getZ() + 0.5);
		} else {
			return null;
		}
	}

	private static BlockPos findExitPosition(Level level, BlockPos blockPos) {
		BlockPos blockPos2 = findTallestBlock(level, blockPos.offset(0, 2, 0), 5, false);
		LOGGER.debug("Best exit position for portal at {} is {}", blockPos, blockPos2);
		return blockPos2.above();
	}

	private static BlockPos findOrCreateValidTeleportPos(ServerLevel serverLevel, BlockPos blockPos) {
		Vec3 vec3 = findExitPortalXZPosTentative(serverLevel, blockPos);
		LevelChunk levelChunk = getChunk(serverLevel, vec3);
		BlockPos blockPos2 = findValidSpawnInChunk(levelChunk);
		if (blockPos2 == null) {
			BlockPos blockPos3 = BlockPos.containing(vec3.x + 0.5, 75.0, vec3.z + 0.5);
			LOGGER.debug("Failed to find a suitable block to teleport to, spawning an island on {}", blockPos3);
			serverLevel.registryAccess()
				.registry(Registries.CONFIGURED_FEATURE)
				.flatMap(registry -> registry.getHolder(EndFeatures.END_ISLAND))
				.ifPresent(
					reference -> ((ConfiguredFeature)reference.value())
							.place(serverLevel, serverLevel.getChunkSource().getGenerator(), RandomSource.create(blockPos3.asLong()), blockPos3)
				);
			blockPos2 = blockPos3;
		} else {
			LOGGER.debug("Found suitable block to teleport to: {}", blockPos2);
		}

		return findTallestBlock(serverLevel, blockPos2, 16, true);
	}

	private static Vec3 findExitPortalXZPosTentative(ServerLevel serverLevel, BlockPos blockPos) {
		Vec3 vec3 = new Vec3((double)blockPos.getX(), 0.0, (double)blockPos.getZ()).normalize();
		int i = 1024;
		Vec3 vec32 = vec3.scale(1024.0);

		for (int j = 16; !isChunkEmpty(serverLevel, vec32) && j-- > 0; vec32 = vec32.add(vec3.scale(-16.0))) {
			LOGGER.debug("Skipping backwards past nonempty chunk at {}", vec32);
		}

		for (int var6 = 16; isChunkEmpty(serverLevel, vec32) && var6-- > 0; vec32 = vec32.add(vec3.scale(16.0))) {
			LOGGER.debug("Skipping forward past empty chunk at {}", vec32);
		}

		LOGGER.debug("Found chunk at {}", vec32);
		return vec32;
	}

	private static boolean isChunkEmpty(ServerLevel serverLevel, Vec3 vec3) {
		return getChunk(serverLevel, vec3).getHighestFilledSectionIndex() == -1;
	}

	private static BlockPos findTallestBlock(BlockGetter blockGetter, BlockPos blockPos, int i, boolean bl) {
		BlockPos blockPos2 = null;

		for (int j = -i; j <= i; j++) {
			for (int k = -i; k <= i; k++) {
				if (j != 0 || k != 0 || bl) {
					for (int l = blockGetter.getMaxBuildHeight() - 1; l > (blockPos2 == null ? blockGetter.getMinBuildHeight() : blockPos2.getY()); l--) {
						BlockPos blockPos3 = new BlockPos(blockPos.getX() + j, l, blockPos.getZ() + k);
						BlockState blockState = blockGetter.getBlockState(blockPos3);
						if (blockState.isCollisionShapeFullBlock(blockGetter, blockPos3) && (bl || !blockState.is(Blocks.BEDROCK))) {
							blockPos2 = blockPos3;
							break;
						}
					}
				}
			}
		}

		return blockPos2 == null ? blockPos : blockPos2;
	}

	private static LevelChunk getChunk(Level level, Vec3 vec3) {
		return level.getChunk(Mth.floor(vec3.x / 16.0), Mth.floor(vec3.z / 16.0));
	}

	@Nullable
	private static BlockPos findValidSpawnInChunk(LevelChunk levelChunk) {
		ChunkPos chunkPos = levelChunk.getPos();
		BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), 30, chunkPos.getMinBlockZ());
		int i = levelChunk.getHighestSectionPosition() + 16 - 1;
		BlockPos blockPos2 = new BlockPos(chunkPos.getMaxBlockX(), i, chunkPos.getMaxBlockZ());
		BlockPos blockPos3 = null;
		double d = 0.0;

		for (BlockPos blockPos4 : BlockPos.betweenClosed(blockPos, blockPos2)) {
			BlockState blockState = levelChunk.getBlockState(blockPos4);
			BlockPos blockPos5 = blockPos4.above();
			BlockPos blockPos6 = blockPos4.above(2);
			if (blockState.is(Blocks.END_STONE)
				&& !levelChunk.getBlockState(blockPos5).isCollisionShapeFullBlock(levelChunk, blockPos5)
				&& !levelChunk.getBlockState(blockPos6).isCollisionShapeFullBlock(levelChunk, blockPos6)) {
				double e = blockPos4.distToCenterSqr(0.0, 0.0, 0.0);
				if (blockPos3 == null || e < d) {
					blockPos3 = blockPos4;
					d = e;
				}
			}
		}

		return blockPos3;
	}

	private static void spawnGatewayPortal(ServerLevel serverLevel, BlockPos blockPos, EndGatewayConfiguration endGatewayConfiguration) {
		Feature.END_GATEWAY.place(endGatewayConfiguration, serverLevel, serverLevel.getChunkSource().getGenerator(), RandomSource.create(), blockPos);
	}

	@Override
	public boolean shouldRenderFace(Direction direction) {
		return Block.shouldRenderFace(this.getBlockState(), this.level, this.getBlockPos(), direction, this.getBlockPos().relative(direction));
	}

	public int getParticleAmount() {
		int i = 0;

		for (Direction direction : Direction.values()) {
			i += this.shouldRenderFace(direction) ? 1 : 0;
		}

		return i;
	}

	public void setExitPosition(BlockPos blockPos, boolean bl) {
		this.exactTeleport = bl;
		this.exitPortal = blockPos;
	}
}

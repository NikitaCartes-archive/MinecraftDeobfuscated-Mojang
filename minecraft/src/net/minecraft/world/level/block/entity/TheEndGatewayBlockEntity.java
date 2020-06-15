package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TheEndGatewayBlockEntity extends TheEndPortalBlockEntity implements TickableBlockEntity {
	private static final Logger LOGGER = LogManager.getLogger();
	private long age;
	private int teleportCooldown;
	@Nullable
	private BlockPos exitPortal;
	private boolean exactTeleport;

	public TheEndGatewayBlockEntity() {
		super(BlockEntityType.END_GATEWAY);
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		super.save(compoundTag);
		compoundTag.putLong("Age", this.age);
		if (this.exitPortal != null) {
			compoundTag.put("ExitPortal", NbtUtils.writeBlockPos(this.exitPortal));
		}

		if (this.exactTeleport) {
			compoundTag.putBoolean("ExactTeleport", this.exactTeleport);
		}

		return compoundTag;
	}

	@Override
	public void load(BlockState blockState, CompoundTag compoundTag) {
		super.load(blockState, compoundTag);
		this.age = compoundTag.getLong("Age");
		if (compoundTag.contains("ExitPortal", 10)) {
			this.exitPortal = NbtUtils.readBlockPos(compoundTag.getCompound("ExitPortal"));
		}

		this.exactTeleport = compoundTag.getBoolean("ExactTeleport");
	}

	@Environment(EnvType.CLIENT)
	@Override
	public double getViewDistance() {
		return 256.0;
	}

	@Override
	public void tick() {
		boolean bl = this.isSpawning();
		boolean bl2 = this.isCoolingDown();
		this.age++;
		if (bl2) {
			this.teleportCooldown--;
		} else if (!this.level.isClientSide) {
			List<Entity> list = this.level.getEntitiesOfClass(Entity.class, new AABB(this.getBlockPos()));
			if (!list.isEmpty()) {
				this.teleportEntity((Entity)list.get(this.level.random.nextInt(list.size())));
			}

			if (this.age % 2400L == 0L) {
				this.triggerCooldown();
			}
		}

		if (bl != this.isSpawning() || bl2 != this.isCoolingDown()) {
			this.setChanged();
		}
	}

	public boolean isSpawning() {
		return this.age < 200L;
	}

	public boolean isCoolingDown() {
		return this.teleportCooldown > 0;
	}

	@Environment(EnvType.CLIENT)
	public float getSpawnPercent(float f) {
		return Mth.clamp(((float)this.age + f) / 200.0F, 0.0F, 1.0F);
	}

	@Environment(EnvType.CLIENT)
	public float getCooldownPercent(float f) {
		return 1.0F - Mth.clamp(((float)this.teleportCooldown - f) / 40.0F, 0.0F, 1.0F);
	}

	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return new ClientboundBlockEntityDataPacket(this.worldPosition, 8, this.getUpdateTag());
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.save(new CompoundTag());
	}

	public void triggerCooldown() {
		if (!this.level.isClientSide) {
			this.teleportCooldown = 40;
			this.level.blockEvent(this.getBlockPos(), this.getBlockState().getBlock(), 1, 0);
			this.setChanged();
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

	public void teleportEntity(Entity entity) {
		if (this.level instanceof ServerLevel && !this.isCoolingDown()) {
			this.teleportCooldown = 100;
			if (this.exitPortal == null && this.level.dimension() == Level.END) {
				this.findExitPortal((ServerLevel)this.level);
			}

			if (this.exitPortal != null) {
				BlockPos blockPos = this.exactTeleport ? this.exitPortal : this.findExitPosition();
				Entity entity3;
				if (entity instanceof ThrownEnderpearl) {
					Entity entity2 = ((ThrownEnderpearl)entity).getOwner();
					if (entity2 instanceof ServerPlayer) {
						CriteriaTriggers.ENTER_BLOCK.trigger((ServerPlayer)entity2, this.level.getBlockState(this.getBlockPos()));
					}

					if (entity2 != null) {
						entity3 = entity2;
						entity.remove();
					} else {
						entity3 = entity;
					}
				} else {
					entity3 = entity.getRootVehicle();
				}

				entity3.teleportToWithTicket((double)blockPos.getX() + 0.5, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5);
			}

			this.triggerCooldown();
		}
	}

	private BlockPos findExitPosition() {
		BlockPos blockPos = findTallestBlock(this.level, this.exitPortal, 5, false);
		LOGGER.debug("Best exit position for portal at {} is {}", this.exitPortal, blockPos);
		return blockPos.above();
	}

	private void findExitPortal(ServerLevel serverLevel) {
		Vec3 vec3 = new Vec3((double)this.getBlockPos().getX(), 0.0, (double)this.getBlockPos().getZ()).normalize();
		Vec3 vec32 = vec3.scale(1024.0);

		for (int i = 16; getChunk(serverLevel, vec32).getHighestSectionPosition() > 0 && i-- > 0; vec32 = vec32.add(vec3.scale(-16.0))) {
			LOGGER.debug("Skipping backwards past nonempty chunk at {}", vec32);
		}

		for (int var6 = 16; getChunk(serverLevel, vec32).getHighestSectionPosition() == 0 && var6-- > 0; vec32 = vec32.add(vec3.scale(16.0))) {
			LOGGER.debug("Skipping forward past empty chunk at {}", vec32);
		}

		LOGGER.debug("Found chunk at {}", vec32);
		LevelChunk levelChunk = getChunk(serverLevel, vec32);
		this.exitPortal = findValidSpawnInChunk(levelChunk);
		if (this.exitPortal == null) {
			this.exitPortal = new BlockPos(vec32.x + 0.5, 75.0, vec32.z + 0.5);
			LOGGER.debug("Failed to find suitable block, settling on {}", this.exitPortal);
			Feature.END_ISLAND
				.configured(FeatureConfiguration.NONE)
				.place(
					serverLevel, serverLevel.structureFeatureManager(), serverLevel.getChunkSource().getGenerator(), new Random(this.exitPortal.asLong()), this.exitPortal
				);
		} else {
			LOGGER.debug("Found block at {}", this.exitPortal);
		}

		this.exitPortal = findTallestBlock(serverLevel, this.exitPortal, 16, true);
		LOGGER.debug("Creating portal at {}", this.exitPortal);
		this.exitPortal = this.exitPortal.above(10);
		this.createExitPortal(serverLevel, this.exitPortal);
		this.setChanged();
	}

	private static BlockPos findTallestBlock(BlockGetter blockGetter, BlockPos blockPos, int i, boolean bl) {
		BlockPos blockPos2 = null;

		for (int j = -i; j <= i; j++) {
			for (int k = -i; k <= i; k++) {
				if (j != 0 || k != 0 || bl) {
					for (int l = 255; l > (blockPos2 == null ? 0 : blockPos2.getY()); l--) {
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
				double e = blockPos4.distSqr(0.0, 0.0, 0.0, true);
				if (blockPos3 == null || e < d) {
					blockPos3 = blockPos4;
					d = e;
				}
			}
		}

		return blockPos3;
	}

	private void createExitPortal(ServerLevel serverLevel, BlockPos blockPos) {
		Feature.END_GATEWAY
			.configured(EndGatewayConfiguration.knownExit(this.getBlockPos(), false))
			.place(serverLevel, serverLevel.structureFeatureManager(), serverLevel.getChunkSource().getGenerator(), new Random(), blockPos);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean shouldRenderFace(Direction direction) {
		return Block.shouldRenderFace(this.getBlockState(), this.level, this.getBlockPos(), direction);
	}

	@Environment(EnvType.CLIENT)
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

/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import java.util.List;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class TheEndGatewayBlockEntity
extends TheEndPortalBlockEntity {
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
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        compoundTag.putLong("Age", this.age);
        if (this.exitPortal != null) {
            compoundTag.put("ExitPortal", NbtUtils.writeBlockPos(this.exitPortal));
        }
        if (this.exactTeleport) {
            compoundTag.putBoolean("ExactTeleport", true);
        }
    }

    @Override
    public void load(CompoundTag compoundTag) {
        BlockPos blockPos;
        super.load(compoundTag);
        this.age = compoundTag.getLong("Age");
        if (compoundTag.contains("ExitPortal", 10) && Level.isInSpawnableBounds(blockPos = NbtUtils.readBlockPos(compoundTag.getCompound("ExitPortal")))) {
            this.exitPortal = blockPos;
        }
        this.exactTeleport = compoundTag.getBoolean("ExactTeleport");
    }

    public static void beamAnimationTick(Level level, BlockPos blockPos, BlockState blockState, TheEndGatewayBlockEntity theEndGatewayBlockEntity) {
        ++theEndGatewayBlockEntity.age;
        if (theEndGatewayBlockEntity.isCoolingDown()) {
            --theEndGatewayBlockEntity.teleportCooldown;
        }
    }

    public static void teleportTick(Level level, BlockPos blockPos, BlockState blockState, TheEndGatewayBlockEntity theEndGatewayBlockEntity) {
        boolean bl = theEndGatewayBlockEntity.isSpawning();
        boolean bl2 = theEndGatewayBlockEntity.isCoolingDown();
        ++theEndGatewayBlockEntity.age;
        if (bl2) {
            --theEndGatewayBlockEntity.teleportCooldown;
        } else {
            List<Entity> list = level.getEntitiesOfClass(Entity.class, new AABB(blockPos), TheEndGatewayBlockEntity::canEntityTeleport);
            if (!list.isEmpty()) {
                TheEndGatewayBlockEntity.teleportEntity(level, blockPos, blockState, list.get(level.random.nextInt(list.size())), theEndGatewayBlockEntity);
            }
            if (theEndGatewayBlockEntity.age % 2400L == 0L) {
                TheEndGatewayBlockEntity.triggerCooldown(level, blockPos, blockState, theEndGatewayBlockEntity);
            }
        }
        if (bl != theEndGatewayBlockEntity.isSpawning() || bl2 != theEndGatewayBlockEntity.isCoolingDown()) {
            TheEndGatewayBlockEntity.setChanged(level, blockPos, blockState);
        }
    }

    public static boolean canEntityTeleport(Entity entity) {
        return EntitySelector.NO_SPECTATORS.test(entity) && !entity.getRootVehicle().isOnPortalCooldown();
    }

    public boolean isSpawning() {
        return this.age < 200L;
    }

    public boolean isCoolingDown() {
        return this.teleportCooldown > 0;
    }

    public float getSpawnPercent(float f) {
        return Mth.clamp(((float)this.age + f) / 200.0f, 0.0f, 1.0f);
    }

    public float getCooldownPercent(float f) {
        return 1.0f - Mth.clamp(((float)this.teleportCooldown - f) / 40.0f, 0.0f, 1.0f);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    private static void triggerCooldown(Level level, BlockPos blockPos, BlockState blockState, TheEndGatewayBlockEntity theEndGatewayBlockEntity) {
        if (!level.isClientSide) {
            theEndGatewayBlockEntity.teleportCooldown = 40;
            level.blockEvent(blockPos, blockState.getBlock(), 1, 0);
            TheEndGatewayBlockEntity.setChanged(level, blockPos, blockState);
        }
    }

    @Override
    public boolean triggerEvent(int i, int j) {
        if (i == 1) {
            this.teleportCooldown = 40;
            return true;
        }
        return super.triggerEvent(i, j);
    }

    public static void teleportEntity(Level level, BlockPos blockPos, BlockState blockState, Entity entity, TheEndGatewayBlockEntity theEndGatewayBlockEntity) {
        BlockPos blockPos2;
        if (!(level instanceof ServerLevel) || theEndGatewayBlockEntity.isCoolingDown()) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        theEndGatewayBlockEntity.teleportCooldown = 100;
        if (theEndGatewayBlockEntity.exitPortal == null && level.dimension() == Level.END) {
            blockPos2 = TheEndGatewayBlockEntity.findOrCreateValidTeleportPos(serverLevel, blockPos);
            blockPos2 = blockPos2.above(10);
            LOGGER.debug("Creating portal at {}", (Object)blockPos2);
            TheEndGatewayBlockEntity.spawnGatewayPortal(serverLevel, blockPos2, EndGatewayConfiguration.knownExit(blockPos, false));
            theEndGatewayBlockEntity.exitPortal = blockPos2;
        }
        if (theEndGatewayBlockEntity.exitPortal != null) {
            Entity entity3;
            BlockPos blockPos3 = blockPos2 = theEndGatewayBlockEntity.exactTeleport ? theEndGatewayBlockEntity.exitPortal : TheEndGatewayBlockEntity.findExitPosition(level, theEndGatewayBlockEntity.exitPortal);
            if (entity instanceof ThrownEnderpearl) {
                Entity entity2 = ((ThrownEnderpearl)entity).getOwner();
                if (entity2 instanceof ServerPlayer) {
                    CriteriaTriggers.ENTER_BLOCK.trigger((ServerPlayer)entity2, blockState);
                }
                if (entity2 != null) {
                    entity3 = entity2;
                    entity.discard();
                } else {
                    entity3 = entity;
                }
            } else {
                entity3 = entity.getRootVehicle();
            }
            entity3.setPortalCooldown();
            entity3.teleportToWithTicket((double)blockPos2.getX() + 0.5, blockPos2.getY(), (double)blockPos2.getZ() + 0.5);
        }
        TheEndGatewayBlockEntity.triggerCooldown(level, blockPos, blockState, theEndGatewayBlockEntity);
    }

    private static BlockPos findExitPosition(Level level, BlockPos blockPos) {
        BlockPos blockPos2 = TheEndGatewayBlockEntity.findTallestBlock(level, blockPos.offset(0, 2, 0), 5, false);
        LOGGER.debug("Best exit position for portal at {} is {}", (Object)blockPos, (Object)blockPos2);
        return blockPos2.above();
    }

    private static BlockPos findOrCreateValidTeleportPos(ServerLevel serverLevel, BlockPos blockPos) {
        Vec3 vec3 = TheEndGatewayBlockEntity.findExitPortalXZPosTentative(serverLevel, blockPos);
        LevelChunk levelChunk = TheEndGatewayBlockEntity.getChunk(serverLevel, vec3);
        BlockPos blockPos2 = TheEndGatewayBlockEntity.findValidSpawnInChunk(levelChunk);
        if (blockPos2 == null) {
            BlockPos blockPos3 = new BlockPos(vec3.x + 0.5, 75.0, vec3.z + 0.5);
            LOGGER.debug("Failed to find a suitable block to teleport to, spawning an island on {}", (Object)blockPos3);
            serverLevel.registryAccess().registry(Registry.CONFIGURED_FEATURE_REGISTRY).flatMap(registry -> registry.getHolder(EndFeatures.END_ISLAND)).ifPresent(reference -> ((ConfiguredFeature)reference.value()).place(serverLevel, serverLevel.getChunkSource().getGenerator(), RandomSource.create(blockPos3.asLong()), blockPos3));
            blockPos2 = blockPos3;
        } else {
            LOGGER.debug("Found suitable block to teleport to: {}", (Object)blockPos2);
        }
        return TheEndGatewayBlockEntity.findTallestBlock(serverLevel, blockPos2, 16, true);
    }

    private static Vec3 findExitPortalXZPosTentative(ServerLevel serverLevel, BlockPos blockPos) {
        Vec3 vec3 = new Vec3(blockPos.getX(), 0.0, blockPos.getZ()).normalize();
        int i = 1024;
        Vec3 vec32 = vec3.scale(1024.0);
        int j = 16;
        while (!TheEndGatewayBlockEntity.isChunkEmpty(serverLevel, vec32) && j-- > 0) {
            LOGGER.debug("Skipping backwards past nonempty chunk at {}", (Object)vec32);
            vec32 = vec32.add(vec3.scale(-16.0));
        }
        j = 16;
        while (TheEndGatewayBlockEntity.isChunkEmpty(serverLevel, vec32) && j-- > 0) {
            LOGGER.debug("Skipping forward past empty chunk at {}", (Object)vec32);
            vec32 = vec32.add(vec3.scale(16.0));
        }
        LOGGER.debug("Found chunk at {}", (Object)vec32);
        return vec32;
    }

    private static boolean isChunkEmpty(ServerLevel serverLevel, Vec3 vec3) {
        return TheEndGatewayBlockEntity.getChunk(serverLevel, vec3).getHighestSectionPosition() <= serverLevel.getMinBuildHeight();
    }

    private static BlockPos findTallestBlock(BlockGetter blockGetter, BlockPos blockPos, int i, boolean bl) {
        Vec3i blockPos2 = null;
        for (int j = -i; j <= i; ++j) {
            block1: for (int k = -i; k <= i; ++k) {
                if (j == 0 && k == 0 && !bl) continue;
                for (int l = blockGetter.getMaxBuildHeight() - 1; l > (blockPos2 == null ? blockGetter.getMinBuildHeight() : blockPos2.getY()); --l) {
                    BlockPos blockPos3 = new BlockPos(blockPos.getX() + j, l, blockPos.getZ() + k);
                    BlockState blockState = blockGetter.getBlockState(blockPos3);
                    if (!blockState.isCollisionShapeFullBlock(blockGetter, blockPos3) || !bl && blockState.is(Blocks.BEDROCK)) continue;
                    blockPos2 = blockPos3;
                    continue block1;
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
            if (!blockState.is(Blocks.END_STONE) || levelChunk.getBlockState(blockPos5).isCollisionShapeFullBlock(levelChunk, blockPos5) || levelChunk.getBlockState(blockPos6).isCollisionShapeFullBlock(levelChunk, blockPos6)) continue;
            double e = blockPos4.distToCenterSqr(0.0, 0.0, 0.0);
            if (blockPos3 != null && !(e < d)) continue;
            blockPos3 = blockPos4;
            d = e;
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

    public /* synthetic */ Packet getUpdatePacket() {
        return this.getUpdatePacket();
    }
}


/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.item;

import com.mojang.logging.LogUtils;
import java.util.function.Predicate;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class FallingBlockEntity
extends Entity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private BlockState blockState = Blocks.SAND.defaultBlockState();
    public int time;
    public boolean dropItem = true;
    private boolean cancelDrop;
    private boolean hurtEntities;
    private int fallDamageMax = 40;
    private float fallDamagePerDistance;
    @Nullable
    public CompoundTag blockData;
    protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(FallingBlockEntity.class, EntityDataSerializers.BLOCK_POS);

    public FallingBlockEntity(EntityType<? extends FallingBlockEntity> entityType, Level level) {
        super(entityType, level);
    }

    private FallingBlockEntity(Level level, double d, double e, double f, BlockState blockState) {
        this((EntityType<? extends FallingBlockEntity>)EntityType.FALLING_BLOCK, level);
        this.blockState = blockState;
        this.blocksBuilding = true;
        this.setPos(d, e, f);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = d;
        this.yo = e;
        this.zo = f;
        this.setStartPos(this.blockPosition());
    }

    public static FallingBlockEntity fall(Level level, BlockPos blockPos, BlockState blockState) {
        FallingBlockEntity fallingBlockEntity = new FallingBlockEntity(level, (double)blockPos.getX() + 0.5, blockPos.getY(), (double)blockPos.getZ() + 0.5, blockState.hasProperty(BlockStateProperties.WATERLOGGED) ? (BlockState)blockState.setValue(BlockStateProperties.WATERLOGGED, false) : blockState);
        level.setBlock(blockPos, blockState.getFluidState().createLegacyBlock(), 3);
        level.addFreshEntity(fallingBlockEntity);
        return fallingBlockEntity;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    public void setStartPos(BlockPos blockPos) {
        this.entityData.set(DATA_START_POS, blockPos);
    }

    public BlockPos getStartPos() {
        return this.entityData.get(DATA_START_POS);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_START_POS, BlockPos.ZERO);
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public void tick() {
        if (this.blockState.isAir()) {
            this.discard();
            return;
        }
        Block block = this.blockState.getBlock();
        ++this.time;
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (!this.level.isClientSide) {
            BlockHitResult blockHitResult;
            BlockPos blockPos = this.blockPosition();
            boolean bl = this.blockState.getBlock() instanceof ConcretePowderBlock;
            boolean bl2 = bl && this.level.getFluidState(blockPos).is(FluidTags.WATER);
            double d = this.getDeltaMovement().lengthSqr();
            if (bl && d > 1.0 && (blockHitResult = this.level.clip(new ClipContext(new Vec3(this.xo, this.yo, this.zo), this.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this))).getType() != HitResult.Type.MISS && this.level.getFluidState(blockHitResult.getBlockPos()).is(FluidTags.WATER)) {
                blockPos = blockHitResult.getBlockPos();
                bl2 = true;
            }
            if (this.onGround || bl2) {
                BlockState blockState = this.level.getBlockState(blockPos);
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));
                if (!blockState.is(Blocks.MOVING_PISTON)) {
                    if (!this.cancelDrop) {
                        boolean bl5;
                        boolean bl3 = blockState.canBeReplaced(new DirectionalPlaceContext(this.level, blockPos, Direction.DOWN, ItemStack.EMPTY, Direction.UP));
                        boolean bl4 = FallingBlock.isFree(this.level.getBlockState(blockPos.below())) && (!bl || !bl2);
                        boolean bl6 = bl5 = this.blockState.canSurvive(this.level, blockPos) && !bl4;
                        if (bl3 && bl5) {
                            if (this.blockState.hasProperty(BlockStateProperties.WATERLOGGED) && this.level.getFluidState(blockPos).getType() == Fluids.WATER) {
                                this.blockState = (BlockState)this.blockState.setValue(BlockStateProperties.WATERLOGGED, true);
                            }
                            if (this.level.setBlock(blockPos, this.blockState, 3)) {
                                BlockEntity blockEntity;
                                ((ServerLevel)this.level).getChunkSource().chunkMap.broadcast(this, new ClientboundBlockUpdatePacket(blockPos, this.level.getBlockState(blockPos)));
                                this.discard();
                                if (block instanceof Fallable) {
                                    ((Fallable)((Object)block)).onLand(this.level, blockPos, this.blockState, blockState, this);
                                }
                                if (this.blockData != null && this.blockState.hasBlockEntity() && (blockEntity = this.level.getBlockEntity(blockPos)) != null) {
                                    CompoundTag compoundTag = blockEntity.saveWithoutMetadata();
                                    for (String string : this.blockData.getAllKeys()) {
                                        compoundTag.put(string, this.blockData.get(string).copy());
                                    }
                                    try {
                                        blockEntity.load(compoundTag);
                                    } catch (Exception exception) {
                                        LOGGER.error("Failed to load block entity from falling block", exception);
                                    }
                                    blockEntity.setChanged();
                                }
                            } else if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                this.discard();
                                this.callOnBrokenAfterFall(block, blockPos);
                                this.spawnAtLocation(block);
                            }
                        } else {
                            this.discard();
                            if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                this.callOnBrokenAfterFall(block, blockPos);
                                this.spawnAtLocation(block);
                            }
                        }
                    } else {
                        this.discard();
                        this.callOnBrokenAfterFall(block, blockPos);
                    }
                }
            } else if (!(this.level.isClientSide || (this.time <= 100 || blockPos.getY() > this.level.getMinBuildHeight() && blockPos.getY() <= this.level.getMaxBuildHeight()) && this.time <= 600)) {
                if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                    this.spawnAtLocation(block);
                }
                this.discard();
            }
        }
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
    }

    public void callOnBrokenAfterFall(Block block, BlockPos blockPos) {
        if (block instanceof Fallable) {
            ((Fallable)((Object)block)).onBrokenAfterFall(this.level, blockPos, this);
        }
    }

    @Override
    public boolean causeFallDamage(float f, float g, DamageSource damageSource) {
        DamageSource damageSource2;
        Predicate<Entity> predicate;
        if (!this.hurtEntities) {
            return false;
        }
        int i = Mth.ceil(f - 1.0f);
        if (i < 0) {
            return false;
        }
        if (this.blockState.getBlock() instanceof Fallable) {
            Fallable fallable = (Fallable)((Object)this.blockState.getBlock());
            predicate = fallable.getHurtsEntitySelector();
            damageSource2 = fallable.getFallDamageSource(this);
        } else {
            predicate = EntitySelector.NO_SPECTATORS;
            damageSource2 = this.damageSources().fallingBlock(this);
        }
        float h = Math.min(Mth.floor((float)i * this.fallDamagePerDistance), this.fallDamageMax);
        this.level.getEntities(this, this.getBoundingBox(), predicate).forEach(entity -> entity.hurt(damageSource2, h));
        boolean bl = this.blockState.is(BlockTags.ANVIL);
        if (bl && h > 0.0f && this.random.nextFloat() < 0.05f + (float)i * 0.05f) {
            BlockState blockState = AnvilBlock.damage(this.blockState);
            if (blockState == null) {
                this.cancelDrop = true;
            } else {
                this.blockState = blockState;
            }
        }
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.put("BlockState", NbtUtils.writeBlockState(this.blockState));
        compoundTag.putInt("Time", this.time);
        compoundTag.putBoolean("DropItem", this.dropItem);
        compoundTag.putBoolean("HurtEntities", this.hurtEntities);
        compoundTag.putFloat("FallHurtAmount", this.fallDamagePerDistance);
        compoundTag.putInt("FallHurtMax", this.fallDamageMax);
        if (this.blockData != null) {
            compoundTag.put("TileEntityData", this.blockData);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.blockState = NbtUtils.readBlockState(this.level.holderLookup(Registries.BLOCK), compoundTag.getCompound("BlockState"));
        this.time = compoundTag.getInt("Time");
        if (compoundTag.contains("HurtEntities", 99)) {
            this.hurtEntities = compoundTag.getBoolean("HurtEntities");
            this.fallDamagePerDistance = compoundTag.getFloat("FallHurtAmount");
            this.fallDamageMax = compoundTag.getInt("FallHurtMax");
        } else if (this.blockState.is(BlockTags.ANVIL)) {
            this.hurtEntities = true;
        }
        if (compoundTag.contains("DropItem", 99)) {
            this.dropItem = compoundTag.getBoolean("DropItem");
        }
        if (compoundTag.contains("TileEntityData", 10)) {
            this.blockData = compoundTag.getCompound("TileEntityData");
        }
        if (this.blockState.isAir()) {
            this.blockState = Blocks.SAND.defaultBlockState();
        }
    }

    public void setHurtsEntities(float f, int i) {
        this.hurtEntities = true;
        this.fallDamagePerDistance = f;
        this.fallDamageMax = i;
    }

    public void disableDrop() {
        this.cancelDrop = true;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
        super.fillCrashReportCategory(crashReportCategory);
        crashReportCategory.setDetail("Immitating BlockState", this.blockState.toString());
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    @Override
    protected Component getTypeName() {
        return Component.translatable("entity.minecraft.falling_block_type", this.blockState.getBlock().getName());
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, Block.getId(this.getBlockState()));
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        super.recreateFromPacket(clientboundAddEntityPacket);
        this.blockState = Block.stateById(clientboundAddEntityPacket.getData());
        this.blocksBuilding = true;
        double d = clientboundAddEntityPacket.getX();
        double e = clientboundAddEntityPacket.getY();
        double f = clientboundAddEntityPacket.getZ();
        this.setPos(d, e, f);
        this.setStartPos(this.blockPosition());
    }
}


/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.item;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.DirectionalPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FallingBlockEntity
extends Entity {
    private BlockState blockState = Blocks.SAND.defaultBlockState();
    public int time;
    public boolean dropItem = true;
    private boolean cancelDrop;
    private boolean hurtEntities;
    private int fallDamageMax = 40;
    private float fallDamageAmount = 2.0f;
    public CompoundTag blockData;
    protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(FallingBlockEntity.class, EntityDataSerializers.BLOCK_POS);

    public FallingBlockEntity(EntityType<? extends FallingBlockEntity> entityType, Level level) {
        super(entityType, level);
    }

    public FallingBlockEntity(Level level, double d, double e, double f, BlockState blockState) {
        this((EntityType<? extends FallingBlockEntity>)EntityType.FALLING_BLOCK, level);
        this.blockState = blockState;
        this.blocksBuilding = true;
        this.setPos(d, e + (double)((1.0f - this.getBbHeight()) / 2.0f), f);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = d;
        this.yo = e;
        this.zo = f;
        this.setStartPos(this.blockPosition());
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    public void setStartPos(BlockPos blockPos) {
        this.entityData.set(DATA_START_POS, blockPos);
    }

    @Environment(value=EnvType.CLIENT)
    public BlockPos getStartPos() {
        return this.entityData.get(DATA_START_POS);
    }

    @Override
    protected boolean isMovementNoisy() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_START_POS, BlockPos.ZERO);
    }

    @Override
    public boolean isPickable() {
        return !this.removed;
    }

    @Override
    public void tick() {
        BlockPos blockPos;
        if (this.blockState.isAir()) {
            this.remove();
            return;
        }
        Block block = this.blockState.getBlock();
        if (this.time++ == 0) {
            blockPos = this.blockPosition();
            if (this.level.getBlockState(blockPos).is(block)) {
                this.level.removeBlock(blockPos, false);
            } else if (!this.level.isClientSide) {
                this.remove();
                return;
            }
        }
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (!this.level.isClientSide) {
            BlockHitResult blockHitResult;
            blockPos = this.blockPosition();
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
                    this.remove();
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
                                if (block instanceof FallingBlock) {
                                    ((FallingBlock)block).onLand(this.level, blockPos, this.blockState, blockState, this);
                                }
                                if (this.blockData != null && block instanceof EntityBlock && (blockEntity = this.level.getBlockEntity(blockPos)) != null) {
                                    CompoundTag compoundTag = blockEntity.save(new CompoundTag());
                                    for (String string : this.blockData.getAllKeys()) {
                                        Tag tag = this.blockData.get(string);
                                        if ("x".equals(string) || "y".equals(string) || "z".equals(string)) continue;
                                        compoundTag.put(string, tag.copy());
                                    }
                                    blockEntity.load(this.blockState, compoundTag);
                                    blockEntity.setChanged();
                                }
                            } else if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                this.spawnAtLocation(block);
                            }
                        } else if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                            this.spawnAtLocation(block);
                        }
                    } else if (block instanceof FallingBlock) {
                        ((FallingBlock)block).onBroken(this.level, blockPos, this);
                    }
                }
            } else if (!(this.level.isClientSide || (this.time <= 100 || blockPos.getY() >= 1 && blockPos.getY() <= 256) && this.time <= 600)) {
                if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                    this.spawnAtLocation(block);
                }
                this.remove();
            }
        }
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
    }

    @Override
    public boolean causeFallDamage(float f, float g) {
        int i;
        if (this.hurtEntities && (i = Mth.ceil(f - 1.0f)) > 0) {
            ArrayList<Entity> list = Lists.newArrayList(this.level.getEntities(this, this.getBoundingBox()));
            boolean bl = this.blockState.is(BlockTags.ANVIL);
            DamageSource damageSource = bl ? DamageSource.ANVIL : DamageSource.FALLING_BLOCK;
            for (Entity entity : list) {
                entity.hurt(damageSource, Math.min(Mth.floor((float)i * this.fallDamageAmount), this.fallDamageMax));
            }
            if (bl && (double)this.random.nextFloat() < (double)0.05f + (double)i * 0.05) {
                BlockState blockState = AnvilBlock.damage(this.blockState);
                if (blockState == null) {
                    this.cancelDrop = true;
                } else {
                    this.blockState = blockState;
                }
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
        compoundTag.putFloat("FallHurtAmount", this.fallDamageAmount);
        compoundTag.putInt("FallHurtMax", this.fallDamageMax);
        if (this.blockData != null) {
            compoundTag.put("TileEntityData", this.blockData);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.blockState = NbtUtils.readBlockState(compoundTag.getCompound("BlockState"));
        this.time = compoundTag.getInt("Time");
        if (compoundTag.contains("HurtEntities", 99)) {
            this.hurtEntities = compoundTag.getBoolean("HurtEntities");
            this.fallDamageAmount = compoundTag.getFloat("FallHurtAmount");
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

    @Environment(value=EnvType.CLIENT)
    public Level getLevel() {
        return this.level;
    }

    public void setHurtsEntities(boolean bl) {
        this.hurtEntities = bl;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
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
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, Block.getId(this.getBlockState()));
    }
}


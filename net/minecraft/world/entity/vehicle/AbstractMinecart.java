/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.vehicle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.BlockUtil;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.entity.vehicle.MinecartHopper;
import net.minecraft.world.entity.vehicle.MinecartSpawner;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMinecart
extends Entity {
    private static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ID_HURTDIR = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_ID_DISPLAY_BLOCK = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ID_DISPLAY_OFFSET = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ID_CUSTOM_DISPLAY = SynchedEntityData.defineId(AbstractMinecart.class, EntityDataSerializers.BOOLEAN);
    private static final ImmutableMap<Pose, ImmutableList<Integer>> POSE_DISMOUNT_HEIGHTS = ImmutableMap.of(Pose.STANDING, ImmutableList.of(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(-1)), Pose.CROUCHING, ImmutableList.of(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(-1)), Pose.SWIMMING, ImmutableList.of(Integer.valueOf(0), Integer.valueOf(1)));
    private boolean flipped;
    private static final Map<RailShape, Pair<Vec3i, Vec3i>> EXITS = Util.make(Maps.newEnumMap(RailShape.class), enumMap -> {
        Vec3i vec3i = Direction.WEST.getNormal();
        Vec3i vec3i2 = Direction.EAST.getNormal();
        Vec3i vec3i3 = Direction.NORTH.getNormal();
        Vec3i vec3i4 = Direction.SOUTH.getNormal();
        Vec3i vec3i5 = vec3i.below();
        Vec3i vec3i6 = vec3i2.below();
        Vec3i vec3i7 = vec3i3.below();
        Vec3i vec3i8 = vec3i4.below();
        enumMap.put(RailShape.NORTH_SOUTH, Pair.of(vec3i3, vec3i4));
        enumMap.put(RailShape.EAST_WEST, Pair.of(vec3i, vec3i2));
        enumMap.put(RailShape.ASCENDING_EAST, Pair.of(vec3i5, vec3i2));
        enumMap.put(RailShape.ASCENDING_WEST, Pair.of(vec3i, vec3i6));
        enumMap.put(RailShape.ASCENDING_NORTH, Pair.of(vec3i3, vec3i8));
        enumMap.put(RailShape.ASCENDING_SOUTH, Pair.of(vec3i7, vec3i4));
        enumMap.put(RailShape.SOUTH_EAST, Pair.of(vec3i4, vec3i2));
        enumMap.put(RailShape.SOUTH_WEST, Pair.of(vec3i4, vec3i));
        enumMap.put(RailShape.NORTH_WEST, Pair.of(vec3i3, vec3i));
        enumMap.put(RailShape.NORTH_EAST, Pair.of(vec3i3, vec3i2));
    });
    private int lSteps;
    private double lx;
    private double ly;
    private double lz;
    private double lyr;
    private double lxr;
    @Environment(value=EnvType.CLIENT)
    private double lxd;
    @Environment(value=EnvType.CLIENT)
    private double lyd;
    @Environment(value=EnvType.CLIENT)
    private double lzd;

    protected AbstractMinecart(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
    }

    protected AbstractMinecart(EntityType<?> entityType, Level level, double d, double e, double f) {
        this(entityType, level);
        this.setPos(d, e, f);
        this.xo = d;
        this.yo = e;
        this.zo = f;
    }

    public static AbstractMinecart createMinecart(Level level, double d, double e, double f, Type type) {
        if (type == Type.CHEST) {
            return new MinecartChest(level, d, e, f);
        }
        if (type == Type.FURNACE) {
            return new MinecartFurnace(level, d, e, f);
        }
        if (type == Type.TNT) {
            return new MinecartTNT(level, d, e, f);
        }
        if (type == Type.SPAWNER) {
            return new MinecartSpawner(level, d, e, f);
        }
        if (type == Type.HOPPER) {
            return new MinecartHopper(level, d, e, f);
        }
        if (type == Type.COMMAND_BLOCK) {
            return new MinecartCommandBlock(level, d, e, f);
        }
        return new Minecart(level, d, e, f);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ID_HURT, 0);
        this.entityData.define(DATA_ID_HURTDIR, 1);
        this.entityData.define(DATA_ID_DAMAGE, Float.valueOf(0.0f));
        this.entityData.define(DATA_ID_DISPLAY_BLOCK, Block.getId(Blocks.AIR.defaultBlockState()));
        this.entityData.define(DATA_ID_DISPLAY_OFFSET, 6);
        this.entityData.define(DATA_ID_CUSTOM_DISPLAY, false);
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return Boat.canVehicleCollide(this, entity);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected Vec3 getRelativePortalPosition(Direction.Axis axis, BlockUtil.FoundRectangle foundRectangle) {
        return LivingEntity.resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition(axis, foundRectangle));
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.0;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
        Direction direction = this.getMotionDirection();
        if (direction.getAxis() == Direction.Axis.Y) {
            return super.getDismountLocationForPassenger(livingEntity);
        }
        int[][] is = DismountHelper.offsetsForDirection(direction);
        BlockPos blockPos2 = this.blockPosition();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        ImmutableList<Pose> immutableList = livingEntity.getDismountPoses();
        for (Pose pose : immutableList) {
            EntityDimensions entityDimensions = livingEntity.getDimensions(pose);
            float f = Math.min(entityDimensions.width, 1.0f) / 2.0f;
            Iterator iterator = POSE_DISMOUNT_HEIGHTS.get((Object)pose).iterator();
            while (iterator.hasNext()) {
                int i = (Integer)iterator.next();
                for (int[] js : is) {
                    Vec3 vec3;
                    AABB aABB;
                    mutableBlockPos.set(blockPos2.getX() + js[0], blockPos2.getY() + i, blockPos2.getZ() + js[1]);
                    double d = this.level.getBlockFloorHeight(DismountHelper.nonClimbableShape(this.level, mutableBlockPos), () -> DismountHelper.nonClimbableShape(this.level, (BlockPos)mutableBlockPos.below()));
                    if (!DismountHelper.isBlockFloorValid(d) || !DismountHelper.canDismountTo(this.level, livingEntity, (aABB = new AABB(-f, 0.0, -f, f, entityDimensions.height, f)).move(vec3 = Vec3.upFromBottomCenterOf(mutableBlockPos, d)))) continue;
                    livingEntity.setPose(pose);
                    return vec3;
                }
            }
        }
        double e = this.getBoundingBox().maxY;
        mutableBlockPos.set((double)blockPos2.getX(), e, (double)blockPos2.getZ());
        for (Pose pose2 : immutableList) {
            double g = livingEntity.getDimensions((Pose)pose2).height;
            int j = Mth.ceil(e - (double)mutableBlockPos.getY() + g);
            double h = DismountHelper.findCeilingFrom(mutableBlockPos, j, blockPos -> this.level.getBlockState((BlockPos)blockPos).getCollisionShape(this.level, (BlockPos)blockPos));
            if (!(e + g <= h)) continue;
            livingEntity.setPose(pose2);
            break;
        }
        return super.getDismountLocationForPassenger(livingEntity);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        boolean bl;
        if (this.level.isClientSide || this.isRemoved()) {
            return true;
        }
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        }
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.markHurt();
        this.setDamage(this.getDamage() + f * 10.0f);
        this.gameEvent(GameEvent.ENTITY_DAMAGED, damageSource.getEntity());
        boolean bl2 = bl = damageSource.getEntity() instanceof Player && ((Player)damageSource.getEntity()).getAbilities().instabuild;
        if (bl || this.getDamage() > 40.0f) {
            this.ejectPassengers();
            if (!bl || this.hasCustomName()) {
                this.destroy(damageSource);
            } else {
                this.discard();
            }
        }
        return true;
    }

    @Override
    protected float getBlockSpeedFactor() {
        BlockState blockState = this.level.getBlockState(this.blockPosition());
        if (blockState.is(BlockTags.RAILS)) {
            return 1.0f;
        }
        return super.getBlockSpeedFactor();
    }

    public void destroy(DamageSource damageSource) {
        this.remove(Entity.RemovalReason.KILLED);
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            ItemStack itemStack = new ItemStack(Items.MINECART);
            if (this.hasCustomName()) {
                itemStack.setHoverName(this.getCustomName());
            }
            this.spawnAtLocation(itemStack);
        }
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void animateHurt() {
        this.setHurtDir(-this.getHurtDir());
        this.setHurtTime(10);
        this.setDamage(this.getDamage() + this.getDamage() * 10.0f);
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    private static Pair<Vec3i, Vec3i> exits(RailShape railShape) {
        return EXITS.get(railShape);
    }

    @Override
    public Direction getMotionDirection() {
        return this.flipped ? this.getDirection().getOpposite().getClockWise() : this.getDirection().getClockWise();
    }

    @Override
    public void tick() {
        double m;
        BlockPos blockPos;
        BlockState blockState;
        int k;
        int j;
        int i;
        if (this.getHurtTime() > 0) {
            this.setHurtTime(this.getHurtTime() - 1);
        }
        if (this.getDamage() > 0.0f) {
            this.setDamage(this.getDamage() - 1.0f);
        }
        this.checkOutOfWorld();
        this.handleNetherPortal();
        if (this.level.isClientSide) {
            if (this.lSteps > 0) {
                double d = this.getX() + (this.lx - this.getX()) / (double)this.lSteps;
                double e = this.getY() + (this.ly - this.getY()) / (double)this.lSteps;
                double f = this.getZ() + (this.lz - this.getZ()) / (double)this.lSteps;
                double g = Mth.wrapDegrees(this.lyr - (double)this.yRot);
                this.yRot = (float)((double)this.yRot + g / (double)this.lSteps);
                this.xRot = (float)((double)this.xRot + (this.lxr - (double)this.xRot) / (double)this.lSteps);
                --this.lSteps;
                this.setPos(d, e, f);
                this.setRot(this.yRot, this.xRot);
            } else {
                this.reapplyPosition();
                this.setRot(this.yRot, this.xRot);
            }
            return;
        }
        if (!this.isNoGravity()) {
            double d = this.isInWater() ? -0.005 : -0.04;
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, d, 0.0));
        }
        if (this.level.getBlockState(new BlockPos(i = Mth.floor(this.getX()), (j = Mth.floor(this.getY())) - 1, k = Mth.floor(this.getZ()))).is(BlockTags.RAILS)) {
            --j;
        }
        if (BaseRailBlock.isRail(blockState = this.level.getBlockState(blockPos = new BlockPos(i, j, k)))) {
            this.moveAlongTrack(blockPos, blockState);
            if (blockState.is(Blocks.ACTIVATOR_RAIL)) {
                this.activateMinecart(i, j, k, blockState.getValue(PoweredRailBlock.POWERED));
            }
        } else {
            this.comeOffTrack();
        }
        this.checkInsideBlocks();
        this.xRot = 0.0f;
        double h = this.xo - this.getX();
        double l = this.zo - this.getZ();
        if (h * h + l * l > 0.001) {
            this.yRot = (float)(Mth.atan2(l, h) * 180.0 / Math.PI);
            if (this.flipped) {
                this.yRot += 180.0f;
            }
        }
        if ((m = (double)Mth.wrapDegrees(this.yRot - this.yRotO)) < -170.0 || m >= 170.0) {
            this.yRot += 180.0f;
            this.flipped = !this.flipped;
        }
        this.setRot(this.yRot, this.xRot);
        if (this.getMinecartType() == Type.RIDEABLE && AbstractMinecart.getHorizontalDistanceSqr(this.getDeltaMovement()) > 0.01) {
            List<Entity> list = this.level.getEntities(this, this.getBoundingBox().inflate(0.2f, 0.0, 0.2f), EntitySelector.pushableBy(this));
            if (!list.isEmpty()) {
                for (int n = 0; n < list.size(); ++n) {
                    Entity entity = list.get(n);
                    if (entity instanceof Player || entity instanceof IronGolem || entity instanceof AbstractMinecart || this.isVehicle() || entity.isPassenger()) {
                        entity.push(this);
                        continue;
                    }
                    entity.startRiding(this);
                }
            }
        } else {
            for (Entity entity2 : this.level.getEntities(this, this.getBoundingBox().inflate(0.2f, 0.0, 0.2f))) {
                if (this.hasPassenger(entity2) || !entity2.isPushable() || !(entity2 instanceof AbstractMinecart)) continue;
                entity2.push(this);
            }
        }
        this.updateInWaterStateAndDoFluidPushing();
        if (this.isInLava()) {
            this.lavaHurt();
            this.fallDistance *= 0.5f;
        }
        this.firstTick = false;
    }

    protected double getMaxSpeed() {
        return (this.isInWater() ? 4.0 : 8.0) / 20.0;
    }

    public void activateMinecart(int i, int j, int k, boolean bl) {
    }

    protected void comeOffTrack() {
        double d = this.getMaxSpeed();
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(Mth.clamp(vec3.x, -d, d), vec3.y, Mth.clamp(vec3.z, -d, d));
        if (this.onGround) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (!this.onGround) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.95));
        }
    }

    protected void moveAlongTrack(BlockPos blockPos, BlockState blockState) {
        double w;
        Vec3 vec35;
        double u;
        double t;
        double s;
        this.fallDistance = 0.0f;
        double d = this.getX();
        double e = this.getY();
        double f = this.getZ();
        Vec3 vec3 = this.getPos(d, e, f);
        e = blockPos.getY();
        boolean bl = false;
        boolean bl2 = false;
        if (blockState.is(Blocks.POWERED_RAIL)) {
            bl = blockState.getValue(PoweredRailBlock.POWERED);
            bl2 = !bl;
        }
        double g = 0.0078125;
        if (this.isInWater()) {
            g *= 0.2;
        }
        Vec3 vec32 = this.getDeltaMovement();
        RailShape railShape = blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty());
        switch (railShape) {
            case ASCENDING_EAST: {
                this.setDeltaMovement(vec32.add(-g, 0.0, 0.0));
                e += 1.0;
                break;
            }
            case ASCENDING_WEST: {
                this.setDeltaMovement(vec32.add(g, 0.0, 0.0));
                e += 1.0;
                break;
            }
            case ASCENDING_NORTH: {
                this.setDeltaMovement(vec32.add(0.0, 0.0, g));
                e += 1.0;
                break;
            }
            case ASCENDING_SOUTH: {
                this.setDeltaMovement(vec32.add(0.0, 0.0, -g));
                e += 1.0;
            }
        }
        vec32 = this.getDeltaMovement();
        Pair<Vec3i, Vec3i> pair = AbstractMinecart.exits(railShape);
        Vec3i vec3i = pair.getFirst();
        Vec3i vec3i2 = pair.getSecond();
        double h = vec3i2.getX() - vec3i.getX();
        double i = vec3i2.getZ() - vec3i.getZ();
        double j = Math.sqrt(h * h + i * i);
        double k = vec32.x * h + vec32.z * i;
        if (k < 0.0) {
            h = -h;
            i = -i;
        }
        double l = Math.min(2.0, Math.sqrt(AbstractMinecart.getHorizontalDistanceSqr(vec32)));
        vec32 = new Vec3(l * h / j, vec32.y, l * i / j);
        this.setDeltaMovement(vec32);
        Entity entity = this.getFirstPassenger();
        if (entity instanceof Player) {
            Vec3 vec33 = entity.getDeltaMovement();
            double m = AbstractMinecart.getHorizontalDistanceSqr(vec33);
            double n = AbstractMinecart.getHorizontalDistanceSqr(this.getDeltaMovement());
            if (m > 1.0E-4 && n < 0.01) {
                this.setDeltaMovement(this.getDeltaMovement().add(vec33.x * 0.1, 0.0, vec33.z * 0.1));
                bl2 = false;
            }
        }
        if (bl2) {
            double o = Math.sqrt(AbstractMinecart.getHorizontalDistanceSqr(this.getDeltaMovement()));
            if (o < 0.03) {
                this.setDeltaMovement(Vec3.ZERO);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.0, 0.5));
            }
        }
        double o = (double)blockPos.getX() + 0.5 + (double)vec3i.getX() * 0.5;
        double p = (double)blockPos.getZ() + 0.5 + (double)vec3i.getZ() * 0.5;
        double q = (double)blockPos.getX() + 0.5 + (double)vec3i2.getX() * 0.5;
        double r = (double)blockPos.getZ() + 0.5 + (double)vec3i2.getZ() * 0.5;
        h = q - o;
        i = r - p;
        if (h == 0.0) {
            s = f - (double)blockPos.getZ();
        } else if (i == 0.0) {
            s = d - (double)blockPos.getX();
        } else {
            t = d - o;
            u = f - p;
            s = (t * h + u * i) * 2.0;
        }
        d = o + h * s;
        f = p + i * s;
        this.setPos(d, e, f);
        t = this.isVehicle() ? 0.75 : 1.0;
        u = this.getMaxSpeed();
        vec32 = this.getDeltaMovement();
        this.move(MoverType.SELF, new Vec3(Mth.clamp(t * vec32.x, -u, u), 0.0, Mth.clamp(t * vec32.z, -u, u)));
        if (vec3i.getY() != 0 && Mth.floor(this.getX()) - blockPos.getX() == vec3i.getX() && Mth.floor(this.getZ()) - blockPos.getZ() == vec3i.getZ()) {
            this.setPos(this.getX(), this.getY() + (double)vec3i.getY(), this.getZ());
        } else if (vec3i2.getY() != 0 && Mth.floor(this.getX()) - blockPos.getX() == vec3i2.getX() && Mth.floor(this.getZ()) - blockPos.getZ() == vec3i2.getZ()) {
            this.setPos(this.getX(), this.getY() + (double)vec3i2.getY(), this.getZ());
        }
        this.applyNaturalSlowdown();
        Vec3 vec34 = this.getPos(this.getX(), this.getY(), this.getZ());
        if (vec34 != null && vec3 != null) {
            double v = (vec3.y - vec34.y) * 0.05;
            vec35 = this.getDeltaMovement();
            w = Math.sqrt(AbstractMinecart.getHorizontalDistanceSqr(vec35));
            if (w > 0.0) {
                this.setDeltaMovement(vec35.multiply((w + v) / w, 1.0, (w + v) / w));
            }
            this.setPos(this.getX(), vec34.y, this.getZ());
        }
        int x = Mth.floor(this.getX());
        int y = Mth.floor(this.getZ());
        if (x != blockPos.getX() || y != blockPos.getZ()) {
            vec35 = this.getDeltaMovement();
            w = Math.sqrt(AbstractMinecart.getHorizontalDistanceSqr(vec35));
            this.setDeltaMovement(w * (double)(x - blockPos.getX()), vec35.y, w * (double)(y - blockPos.getZ()));
        }
        if (bl) {
            vec35 = this.getDeltaMovement();
            w = Math.sqrt(AbstractMinecart.getHorizontalDistanceSqr(vec35));
            if (w > 0.01) {
                double z = 0.06;
                this.setDeltaMovement(vec35.add(vec35.x / w * 0.06, 0.0, vec35.z / w * 0.06));
            } else {
                Vec3 vec36 = this.getDeltaMovement();
                double aa = vec36.x;
                double ab = vec36.z;
                if (railShape == RailShape.EAST_WEST) {
                    if (this.isRedstoneConductor(blockPos.west())) {
                        aa = 0.02;
                    } else if (this.isRedstoneConductor(blockPos.east())) {
                        aa = -0.02;
                    }
                } else if (railShape == RailShape.NORTH_SOUTH) {
                    if (this.isRedstoneConductor(blockPos.north())) {
                        ab = 0.02;
                    } else if (this.isRedstoneConductor(blockPos.south())) {
                        ab = -0.02;
                    }
                } else {
                    return;
                }
                this.setDeltaMovement(aa, vec36.y, ab);
            }
        }
    }

    private boolean isRedstoneConductor(BlockPos blockPos) {
        return this.level.getBlockState(blockPos).isRedstoneConductor(this.level, blockPos);
    }

    protected void applyNaturalSlowdown() {
        double d = this.isVehicle() ? 0.997 : 0.96;
        Vec3 vec3 = this.getDeltaMovement();
        vec3 = vec3.multiply(d, 0.0, d);
        if (this.isInWater()) {
            vec3 = vec3.scale(0.95f);
        }
        this.setDeltaMovement(vec3);
    }

    @Nullable
    @Environment(value=EnvType.CLIENT)
    public Vec3 getPosOffs(double d, double e, double f, double g) {
        BlockState blockState;
        int k;
        int j;
        int i = Mth.floor(d);
        if (this.level.getBlockState(new BlockPos(i, (j = Mth.floor(e)) - 1, k = Mth.floor(f))).is(BlockTags.RAILS)) {
            --j;
        }
        if (BaseRailBlock.isRail(blockState = this.level.getBlockState(new BlockPos(i, j, k)))) {
            RailShape railShape = blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty());
            e = j;
            if (railShape.isAscending()) {
                e = j + 1;
            }
            Pair<Vec3i, Vec3i> pair = AbstractMinecart.exits(railShape);
            Vec3i vec3i = pair.getFirst();
            Vec3i vec3i2 = pair.getSecond();
            double h = vec3i2.getX() - vec3i.getX();
            double l = vec3i2.getZ() - vec3i.getZ();
            double m = Math.sqrt(h * h + l * l);
            if (vec3i.getY() != 0 && Mth.floor(d += (h /= m) * g) - i == vec3i.getX() && Mth.floor(f += (l /= m) * g) - k == vec3i.getZ()) {
                e += (double)vec3i.getY();
            } else if (vec3i2.getY() != 0 && Mth.floor(d) - i == vec3i2.getX() && Mth.floor(f) - k == vec3i2.getZ()) {
                e += (double)vec3i2.getY();
            }
            return this.getPos(d, e, f);
        }
        return null;
    }

    @Nullable
    public Vec3 getPos(double d, double e, double f) {
        BlockState blockState;
        int k;
        int j;
        int i = Mth.floor(d);
        if (this.level.getBlockState(new BlockPos(i, (j = Mth.floor(e)) - 1, k = Mth.floor(f))).is(BlockTags.RAILS)) {
            --j;
        }
        if (BaseRailBlock.isRail(blockState = this.level.getBlockState(new BlockPos(i, j, k)))) {
            double s;
            RailShape railShape = blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty());
            Pair<Vec3i, Vec3i> pair = AbstractMinecart.exits(railShape);
            Vec3i vec3i = pair.getFirst();
            Vec3i vec3i2 = pair.getSecond();
            double g = (double)i + 0.5 + (double)vec3i.getX() * 0.5;
            double h = (double)j + 0.0625 + (double)vec3i.getY() * 0.5;
            double l = (double)k + 0.5 + (double)vec3i.getZ() * 0.5;
            double m = (double)i + 0.5 + (double)vec3i2.getX() * 0.5;
            double n = (double)j + 0.0625 + (double)vec3i2.getY() * 0.5;
            double o = (double)k + 0.5 + (double)vec3i2.getZ() * 0.5;
            double p = m - g;
            double q = (n - h) * 2.0;
            double r = o - l;
            if (p == 0.0) {
                s = f - (double)k;
            } else if (r == 0.0) {
                s = d - (double)i;
            } else {
                double t = d - g;
                double u = f - l;
                s = (t * p + u * r) * 2.0;
            }
            d = g + p * s;
            e = h + q * s;
            f = l + r * s;
            if (q < 0.0) {
                e += 1.0;
            } else if (q > 0.0) {
                e += 0.5;
            }
            return new Vec3(d, e, f);
        }
        return null;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public AABB getBoundingBoxForCulling() {
        AABB aABB = this.getBoundingBox();
        if (this.hasCustomDisplay()) {
            return aABB.inflate((double)Math.abs(this.getDisplayOffset()) / 16.0);
        }
        return aABB;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        if (compoundTag.getBoolean("CustomDisplayTile")) {
            this.setDisplayBlockState(NbtUtils.readBlockState(compoundTag.getCompound("DisplayState")));
            this.setDisplayOffset(compoundTag.getInt("DisplayOffset"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        if (this.hasCustomDisplay()) {
            compoundTag.putBoolean("CustomDisplayTile", true);
            compoundTag.put("DisplayState", NbtUtils.writeBlockState(this.getDisplayBlockState()));
            compoundTag.putInt("DisplayOffset", this.getDisplayOffset());
        }
    }

    @Override
    public void push(Entity entity) {
        double e;
        if (this.level.isClientSide) {
            return;
        }
        if (entity.noPhysics || this.noPhysics) {
            return;
        }
        if (this.hasPassenger(entity)) {
            return;
        }
        double d = entity.getX() - this.getX();
        double f = d * d + (e = entity.getZ() - this.getZ()) * e;
        if (f >= (double)1.0E-4f) {
            f = Mth.sqrt(f);
            d /= f;
            e /= f;
            double g = 1.0 / f;
            if (g > 1.0) {
                g = 1.0;
            }
            d *= g;
            e *= g;
            d *= (double)0.1f;
            e *= (double)0.1f;
            d *= (double)(1.0f - this.pushthrough);
            e *= (double)(1.0f - this.pushthrough);
            d *= 0.5;
            e *= 0.5;
            if (entity instanceof AbstractMinecart) {
                Vec3 vec32;
                double i;
                double h = entity.getX() - this.getX();
                Vec3 vec3 = new Vec3(h, 0.0, i = entity.getZ() - this.getZ()).normalize();
                double j = Math.abs(vec3.dot(vec32 = new Vec3(Mth.cos(this.yRot * ((float)Math.PI / 180)), 0.0, Mth.sin(this.yRot * ((float)Math.PI / 180))).normalize()));
                if (j < (double)0.8f) {
                    return;
                }
                Vec3 vec33 = this.getDeltaMovement();
                Vec3 vec34 = entity.getDeltaMovement();
                if (((AbstractMinecart)entity).getMinecartType() == Type.FURNACE && this.getMinecartType() != Type.FURNACE) {
                    this.setDeltaMovement(vec33.multiply(0.2, 1.0, 0.2));
                    this.push(vec34.x - d, 0.0, vec34.z - e);
                    entity.setDeltaMovement(vec34.multiply(0.95, 1.0, 0.95));
                } else if (((AbstractMinecart)entity).getMinecartType() != Type.FURNACE && this.getMinecartType() == Type.FURNACE) {
                    entity.setDeltaMovement(vec34.multiply(0.2, 1.0, 0.2));
                    entity.push(vec33.x + d, 0.0, vec33.z + e);
                    this.setDeltaMovement(vec33.multiply(0.95, 1.0, 0.95));
                } else {
                    double k = (vec34.x + vec33.x) / 2.0;
                    double l = (vec34.z + vec33.z) / 2.0;
                    this.setDeltaMovement(vec33.multiply(0.2, 1.0, 0.2));
                    this.push(k - d, 0.0, l - e);
                    entity.setDeltaMovement(vec34.multiply(0.2, 1.0, 0.2));
                    entity.push(k + d, 0.0, l + e);
                }
            } else {
                this.push(-d, 0.0, -e);
                entity.push(d / 4.0, 0.0, e / 4.0);
            }
        }
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void lerpTo(double d, double e, double f, float g, float h, int i, boolean bl) {
        this.lx = d;
        this.ly = e;
        this.lz = f;
        this.lyr = g;
        this.lxr = h;
        this.lSteps = i + 2;
        this.setDeltaMovement(this.lxd, this.lyd, this.lzd);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void lerpMotion(double d, double e, double f) {
        this.lxd = d;
        this.lyd = e;
        this.lzd = f;
        this.setDeltaMovement(this.lxd, this.lyd, this.lzd);
    }

    public void setDamage(float f) {
        this.entityData.set(DATA_ID_DAMAGE, Float.valueOf(f));
    }

    public float getDamage() {
        return this.entityData.get(DATA_ID_DAMAGE).floatValue();
    }

    public void setHurtTime(int i) {
        this.entityData.set(DATA_ID_HURT, i);
    }

    public int getHurtTime() {
        return this.entityData.get(DATA_ID_HURT);
    }

    public void setHurtDir(int i) {
        this.entityData.set(DATA_ID_HURTDIR, i);
    }

    public int getHurtDir() {
        return this.entityData.get(DATA_ID_HURTDIR);
    }

    public abstract Type getMinecartType();

    public BlockState getDisplayBlockState() {
        if (!this.hasCustomDisplay()) {
            return this.getDefaultDisplayBlockState();
        }
        return Block.stateById(this.getEntityData().get(DATA_ID_DISPLAY_BLOCK));
    }

    public BlockState getDefaultDisplayBlockState() {
        return Blocks.AIR.defaultBlockState();
    }

    public int getDisplayOffset() {
        if (!this.hasCustomDisplay()) {
            return this.getDefaultDisplayOffset();
        }
        return this.getEntityData().get(DATA_ID_DISPLAY_OFFSET);
    }

    public int getDefaultDisplayOffset() {
        return 6;
    }

    public void setDisplayBlockState(BlockState blockState) {
        this.getEntityData().set(DATA_ID_DISPLAY_BLOCK, Block.getId(blockState));
        this.setCustomDisplay(true);
    }

    public void setDisplayOffset(int i) {
        this.getEntityData().set(DATA_ID_DISPLAY_OFFSET, i);
        this.setCustomDisplay(true);
    }

    public boolean hasCustomDisplay() {
        return this.getEntityData().get(DATA_ID_CUSTOM_DISPLAY);
    }

    public void setCustomDisplay(boolean bl) {
        this.getEntityData().set(DATA_ID_CUSTOM_DISPLAY, bl);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public ItemStack getPickResult() {
        Item item;
        switch (this.getMinecartType()) {
            case FURNACE: {
                item = Items.FURNACE_MINECART;
                break;
            }
            case CHEST: {
                item = Items.CHEST_MINECART;
                break;
            }
            case TNT: {
                item = Items.TNT_MINECART;
                break;
            }
            case HOPPER: {
                item = Items.HOPPER_MINECART;
                break;
            }
            case COMMAND_BLOCK: {
                item = Items.COMMAND_BLOCK_MINECART;
                break;
            }
            default: {
                item = Items.MINECART;
            }
        }
        return new ItemStack(item);
    }

    public static enum Type {
        RIDEABLE,
        CHEST,
        FURNACE,
        TNT,
        SPAWNER,
        HOPPER,
        COMMAND_BLOCK;

    }
}


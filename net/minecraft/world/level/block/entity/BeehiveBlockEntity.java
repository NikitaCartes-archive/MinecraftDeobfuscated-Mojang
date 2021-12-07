/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BeehiveBlockEntity
extends BlockEntity {
    public static final String TAG_FLOWER_POS = "FlowerPos";
    public static final String MIN_OCCUPATION_TICKS = "MinOccupationTicks";
    public static final String ENTITY_DATA = "EntityData";
    public static final String TICKS_IN_HIVE = "TicksInHive";
    public static final String HAS_NECTAR = "HasNectar";
    public static final String BEES = "Bees";
    private static final List<String> IGNORED_BEE_TAGS = Arrays.asList("Air", "ArmorDropChances", "ArmorItems", "Brain", "CanPickUpLoot", "DeathTime", "FallDistance", "FallFlying", "Fire", "HandDropChances", "HandItems", "HurtByTimestamp", "HurtTime", "LeftHanded", "Motion", "NoGravity", "OnGround", "PortalCooldown", "Pos", "Rotation", "CannotEnterHiveTicks", "TicksSincePollination", "CropsGrownSincePollination", "HivePos", "Passengers", "Leash", "UUID");
    public static final int MAX_OCCUPANTS = 3;
    private static final int MIN_TICKS_BEFORE_REENTERING_HIVE = 400;
    private static final int MIN_OCCUPATION_TICKS_NECTAR = 2400;
    public static final int MIN_OCCUPATION_TICKS_NECTARLESS = 600;
    private final List<BeeData> stored = Lists.newArrayList();
    @Nullable
    private BlockPos savedFlowerPos;

    public BeehiveBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.BEEHIVE, blockPos, blockState);
    }

    @Override
    public void setChanged() {
        if (this.isFireNearby()) {
            this.emptyAllLivingFromHive(null, this.level.getBlockState(this.getBlockPos()), BeeReleaseStatus.EMERGENCY);
        }
        super.setChanged();
    }

    public boolean isFireNearby() {
        if (this.level == null) {
            return false;
        }
        for (BlockPos blockPos : BlockPos.betweenClosed(this.worldPosition.offset(-1, -1, -1), this.worldPosition.offset(1, 1, 1))) {
            if (!(this.level.getBlockState(blockPos).getBlock() instanceof FireBlock)) continue;
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return this.stored.isEmpty();
    }

    public boolean isFull() {
        return this.stored.size() == 3;
    }

    public void emptyAllLivingFromHive(@Nullable Player player, BlockState blockState, BeeReleaseStatus beeReleaseStatus) {
        List<Entity> list = this.releaseAllOccupants(blockState, beeReleaseStatus);
        if (player != null) {
            for (Entity entity : list) {
                if (!(entity instanceof Bee)) continue;
                Bee bee = (Bee)entity;
                if (!(player.position().distanceToSqr(entity.position()) <= 16.0)) continue;
                if (!this.isSedated()) {
                    bee.setTarget(player);
                    continue;
                }
                bee.setStayOutOfHiveCountdown(400);
            }
        }
    }

    private List<Entity> releaseAllOccupants(BlockState blockState, BeeReleaseStatus beeReleaseStatus) {
        ArrayList<Entity> list = Lists.newArrayList();
        this.stored.removeIf(beeData -> BeehiveBlockEntity.releaseOccupant(this.level, this.worldPosition, blockState, beeData, list, beeReleaseStatus, this.savedFlowerPos));
        if (!list.isEmpty()) {
            super.setChanged();
        }
        return list;
    }

    public void addOccupant(Entity entity, boolean bl) {
        this.addOccupantWithPresetTicks(entity, bl, 0);
    }

    @VisibleForDebug
    public int getOccupantCount() {
        return this.stored.size();
    }

    public static int getHoneyLevel(BlockState blockState) {
        return blockState.getValue(BeehiveBlock.HONEY_LEVEL);
    }

    @VisibleForDebug
    public boolean isSedated() {
        return CampfireBlock.isSmokeyPos(this.level, this.getBlockPos());
    }

    public void addOccupantWithPresetTicks(Entity entity, boolean bl, int i) {
        if (this.stored.size() >= 3) {
            return;
        }
        entity.stopRiding();
        entity.ejectPassengers();
        CompoundTag compoundTag = new CompoundTag();
        entity.save(compoundTag);
        this.storeBee(compoundTag, i, bl);
        if (this.level != null) {
            Bee bee;
            if (entity instanceof Bee && (bee = (Bee)entity).hasSavedFlowerPos() && (!this.hasSavedFlowerPos() || this.level.random.nextBoolean())) {
                this.savedFlowerPos = bee.getSavedFlowerPos();
            }
            BlockPos blockPos = this.getBlockPos();
            this.level.playSound(null, blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.BEEHIVE_ENTER, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        entity.discard();
        super.setChanged();
    }

    public void storeBee(CompoundTag compoundTag, int i, boolean bl) {
        this.stored.add(new BeeData(compoundTag, i, bl ? 2400 : 600));
    }

    private static boolean releaseOccupant(Level level, BlockPos blockPos, BlockState blockState, BeeData beeData, @Nullable List<Entity> list, BeeReleaseStatus beeReleaseStatus, @Nullable BlockPos blockPos2) {
        boolean bl;
        if ((level.isNight() || level.isRaining()) && beeReleaseStatus != BeeReleaseStatus.EMERGENCY) {
            return false;
        }
        CompoundTag compoundTag = beeData.entityData.copy();
        BeehiveBlockEntity.removeIgnoredBeeTags(compoundTag);
        compoundTag.put("HivePos", NbtUtils.writeBlockPos(blockPos));
        compoundTag.putBoolean("NoGravity", true);
        Direction direction = blockState.getValue(BeehiveBlock.FACING);
        BlockPos blockPos3 = blockPos.relative(direction);
        boolean bl2 = bl = !level.getBlockState(blockPos3).getCollisionShape(level, blockPos3).isEmpty();
        if (bl && beeReleaseStatus != BeeReleaseStatus.EMERGENCY) {
            return false;
        }
        Entity entity2 = EntityType.loadEntityRecursive(compoundTag, level, entity -> entity);
        if (entity2 != null) {
            if (!entity2.getType().is(EntityTypeTags.BEEHIVE_INHABITORS)) {
                return false;
            }
            if (entity2 instanceof Bee) {
                Bee bee = (Bee)entity2;
                if (blockPos2 != null && !bee.hasSavedFlowerPos() && level.random.nextFloat() < 0.9f) {
                    bee.setSavedFlowerPos(blockPos2);
                }
                if (beeReleaseStatus == BeeReleaseStatus.HONEY_DELIVERED) {
                    int i;
                    bee.dropOffNectar();
                    if (blockState.is(BlockTags.BEEHIVES) && (i = BeehiveBlockEntity.getHoneyLevel(blockState)) < 5) {
                        int j;
                        int n = j = level.random.nextInt(100) == 0 ? 2 : 1;
                        if (i + j > 5) {
                            --j;
                        }
                        level.setBlockAndUpdate(blockPos, (BlockState)blockState.setValue(BeehiveBlock.HONEY_LEVEL, i + j));
                    }
                }
                BeehiveBlockEntity.setBeeReleaseData(beeData.ticksInHive, bee);
                if (list != null) {
                    list.add(bee);
                }
                float f = entity2.getBbWidth();
                double d = bl ? 0.0 : 0.55 + (double)(f / 2.0f);
                double e = (double)blockPos.getX() + 0.5 + d * (double)direction.getStepX();
                double g = (double)blockPos.getY() + 0.5 - (double)(entity2.getBbHeight() / 2.0f);
                double h = (double)blockPos.getZ() + 0.5 + d * (double)direction.getStepZ();
                entity2.moveTo(e, g, h, entity2.getYRot(), entity2.getXRot());
            }
            level.playSound(null, blockPos, SoundEvents.BEEHIVE_EXIT, SoundSource.BLOCKS, 1.0f, 1.0f);
            return level.addFreshEntity(entity2);
        }
        return false;
    }

    static void removeIgnoredBeeTags(CompoundTag compoundTag) {
        for (String string : IGNORED_BEE_TAGS) {
            compoundTag.remove(string);
        }
    }

    private static void setBeeReleaseData(int i, Bee bee) {
        int j = bee.getAge();
        if (j < 0) {
            bee.setAge(Math.min(0, j + i));
        } else if (j > 0) {
            bee.setAge(Math.max(0, j - i));
        }
        bee.setInLoveTime(Math.max(0, bee.getInLoveTime() - i));
    }

    private boolean hasSavedFlowerPos() {
        return this.savedFlowerPos != null;
    }

    private static void tickOccupants(Level level, BlockPos blockPos, BlockState blockState, List<BeeData> list, @Nullable BlockPos blockPos2) {
        boolean bl = false;
        Iterator<BeeData> iterator = list.iterator();
        while (iterator.hasNext()) {
            BeeData beeData = iterator.next();
            if (beeData.ticksInHive > beeData.minOccupationTicks) {
                BeeReleaseStatus beeReleaseStatus;
                BeeReleaseStatus beeReleaseStatus2 = beeReleaseStatus = beeData.entityData.getBoolean(HAS_NECTAR) ? BeeReleaseStatus.HONEY_DELIVERED : BeeReleaseStatus.BEE_RELEASED;
                if (BeehiveBlockEntity.releaseOccupant(level, blockPos, blockState, beeData, null, beeReleaseStatus, blockPos2)) {
                    bl = true;
                    iterator.remove();
                }
            }
            ++beeData.ticksInHive;
        }
        if (bl) {
            BeehiveBlockEntity.setChanged(level, blockPos, blockState);
        }
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, BeehiveBlockEntity beehiveBlockEntity) {
        BeehiveBlockEntity.tickOccupants(level, blockPos, blockState, beehiveBlockEntity.stored, beehiveBlockEntity.savedFlowerPos);
        if (!beehiveBlockEntity.stored.isEmpty() && level.getRandom().nextDouble() < 0.005) {
            double d = (double)blockPos.getX() + 0.5;
            double e = blockPos.getY();
            double f = (double)blockPos.getZ() + 0.5;
            level.playSound(null, d, e, f, SoundEvents.BEEHIVE_WORK, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        DebugPackets.sendHiveInfo(level, blockPos, blockState, beehiveBlockEntity);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        this.stored.clear();
        ListTag listTag = compoundTag.getList(BEES, 10);
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag2 = listTag.getCompound(i);
            BeeData beeData = new BeeData(compoundTag2.getCompound(ENTITY_DATA), compoundTag2.getInt(TICKS_IN_HIVE), compoundTag2.getInt(MIN_OCCUPATION_TICKS));
            this.stored.add(beeData);
        }
        this.savedFlowerPos = null;
        if (compoundTag.contains(TAG_FLOWER_POS)) {
            this.savedFlowerPos = NbtUtils.readBlockPos(compoundTag.getCompound(TAG_FLOWER_POS));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        compoundTag.put(BEES, this.writeBees());
        if (this.hasSavedFlowerPos()) {
            compoundTag.put(TAG_FLOWER_POS, NbtUtils.writeBlockPos(this.savedFlowerPos));
        }
    }

    public ListTag writeBees() {
        ListTag listTag = new ListTag();
        for (BeeData beeData : this.stored) {
            CompoundTag compoundTag = beeData.entityData.copy();
            compoundTag.remove("UUID");
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag2.put(ENTITY_DATA, compoundTag);
            compoundTag2.putInt(TICKS_IN_HIVE, beeData.ticksInHive);
            compoundTag2.putInt(MIN_OCCUPATION_TICKS, beeData.minOccupationTicks);
            listTag.add(compoundTag2);
        }
        return listTag;
    }

    public static enum BeeReleaseStatus {
        HONEY_DELIVERED,
        BEE_RELEASED,
        EMERGENCY;

    }

    static class BeeData {
        final CompoundTag entityData;
        int ticksInHive;
        final int minOccupationTicks;

        BeeData(CompoundTag compoundTag, int i, int j) {
            BeehiveBlockEntity.removeIgnoredBeeTags(compoundTag);
            this.entityData = compoundTag;
            this.ticksInHive = i;
            this.minOccupationTicks = j;
        }
    }
}


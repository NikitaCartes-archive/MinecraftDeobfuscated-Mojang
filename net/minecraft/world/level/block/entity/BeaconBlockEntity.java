/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.LockCode;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class BeaconBlockEntity
extends BlockEntity
implements MenuProvider {
    private static final int MAX_LEVELS = 4;
    public static final MobEffect[][] BEACON_EFFECTS = new MobEffect[][]{{MobEffects.MOVEMENT_SPEED, MobEffects.DIG_SPEED}, {MobEffects.DAMAGE_RESISTANCE, MobEffects.JUMP}, {MobEffects.DAMAGE_BOOST}, {MobEffects.REGENERATION}};
    private static final Set<MobEffect> VALID_EFFECTS = Arrays.stream(BEACON_EFFECTS).flatMap(Arrays::stream).collect(Collectors.toSet());
    public static final int DATA_LEVELS = 0;
    public static final int DATA_PRIMARY = 1;
    public static final int DATA_SECONDARY = 2;
    public static final int NUM_DATA_VALUES = 3;
    private static final int BLOCKS_CHECK_PER_TICK = 10;
    List<BeaconBeamSection> beamSections = Lists.newArrayList();
    private List<BeaconBeamSection> checkingBeamSections = Lists.newArrayList();
    int levels;
    private int lastCheckY;
    @Nullable
    MobEffect primaryPower;
    @Nullable
    MobEffect secondaryPower;
    @Nullable
    private Component name;
    private LockCode lockKey = LockCode.NO_LOCK;
    private final ContainerData dataAccess = new ContainerData(){

        @Override
        public int get(int i) {
            return switch (i) {
                case 0 -> BeaconBlockEntity.this.levels;
                case 1 -> MobEffect.getIdFromNullable(BeaconBlockEntity.this.primaryPower);
                case 2 -> MobEffect.getIdFromNullable(BeaconBlockEntity.this.secondaryPower);
                default -> 0;
            };
        }

        @Override
        public void set(int i, int j) {
            switch (i) {
                case 0: {
                    BeaconBlockEntity.this.levels = j;
                    break;
                }
                case 1: {
                    if (!BeaconBlockEntity.this.level.isClientSide && !BeaconBlockEntity.this.beamSections.isEmpty()) {
                        BeaconBlockEntity.playSound(BeaconBlockEntity.this.level, BeaconBlockEntity.this.worldPosition, SoundEvents.BEACON_POWER_SELECT);
                    }
                    BeaconBlockEntity.this.primaryPower = BeaconBlockEntity.getValidEffectById(j);
                    break;
                }
                case 2: {
                    BeaconBlockEntity.this.secondaryPower = BeaconBlockEntity.getValidEffectById(j);
                }
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public BeaconBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.BEACON, blockPos, blockState);
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, BeaconBlockEntity beaconBlockEntity) {
        int m;
        BlockPos blockPos2;
        int i = blockPos.getX();
        int j = blockPos.getY();
        int k = blockPos.getZ();
        if (beaconBlockEntity.lastCheckY < j) {
            blockPos2 = blockPos;
            beaconBlockEntity.checkingBeamSections = Lists.newArrayList();
            beaconBlockEntity.lastCheckY = blockPos2.getY() - 1;
        } else {
            blockPos2 = new BlockPos(i, beaconBlockEntity.lastCheckY + 1, k);
        }
        BeaconBeamSection beaconBeamSection = beaconBlockEntity.checkingBeamSections.isEmpty() ? null : beaconBlockEntity.checkingBeamSections.get(beaconBlockEntity.checkingBeamSections.size() - 1);
        int l = level.getHeight(Heightmap.Types.WORLD_SURFACE, i, k);
        for (m = 0; m < 10 && blockPos2.getY() <= l; ++m) {
            block18: {
                BlockState blockState2;
                block16: {
                    float[] fs;
                    block17: {
                        blockState2 = level.getBlockState(blockPos2);
                        Block block = blockState2.getBlock();
                        if (!(block instanceof BeaconBeamBlock)) break block16;
                        fs = ((BeaconBeamBlock)((Object)block)).getColor().getTextureDiffuseColors();
                        if (beaconBlockEntity.checkingBeamSections.size() > 1) break block17;
                        beaconBeamSection = new BeaconBeamSection(fs);
                        beaconBlockEntity.checkingBeamSections.add(beaconBeamSection);
                        break block18;
                    }
                    if (beaconBeamSection == null) break block18;
                    if (Arrays.equals(fs, beaconBeamSection.color)) {
                        beaconBeamSection.increaseHeight();
                    } else {
                        beaconBeamSection = new BeaconBeamSection(new float[]{(beaconBeamSection.color[0] + fs[0]) / 2.0f, (beaconBeamSection.color[1] + fs[1]) / 2.0f, (beaconBeamSection.color[2] + fs[2]) / 2.0f});
                        beaconBlockEntity.checkingBeamSections.add(beaconBeamSection);
                    }
                    break block18;
                }
                if (beaconBeamSection != null && (blockState2.getLightBlock(level, blockPos2) < 15 || blockState2.is(Blocks.BEDROCK))) {
                    beaconBeamSection.increaseHeight();
                } else {
                    beaconBlockEntity.checkingBeamSections.clear();
                    beaconBlockEntity.lastCheckY = l;
                    break;
                }
            }
            blockPos2 = blockPos2.above();
            ++beaconBlockEntity.lastCheckY;
        }
        m = beaconBlockEntity.levels;
        if (level.getGameTime() % 80L == 0L) {
            if (!beaconBlockEntity.beamSections.isEmpty()) {
                beaconBlockEntity.levels = BeaconBlockEntity.updateBase(level, i, j, k);
            }
            if (beaconBlockEntity.levels > 0 && !beaconBlockEntity.beamSections.isEmpty()) {
                BeaconBlockEntity.applyEffects(level, blockPos, beaconBlockEntity.levels, beaconBlockEntity.primaryPower, beaconBlockEntity.secondaryPower);
                BeaconBlockEntity.playSound(level, blockPos, SoundEvents.BEACON_AMBIENT);
            }
        }
        if (beaconBlockEntity.lastCheckY >= l) {
            beaconBlockEntity.lastCheckY = level.getMinBuildHeight() - 1;
            boolean bl = m > 0;
            beaconBlockEntity.beamSections = beaconBlockEntity.checkingBeamSections;
            if (!level.isClientSide) {
                boolean bl2;
                boolean bl3 = bl2 = beaconBlockEntity.levels > 0;
                if (!bl && bl2) {
                    BeaconBlockEntity.playSound(level, blockPos, SoundEvents.BEACON_ACTIVATE);
                    for (ServerPlayer serverPlayer : level.getEntitiesOfClass(ServerPlayer.class, new AABB(i, j, k, i, j - 4, k).inflate(10.0, 5.0, 10.0))) {
                        CriteriaTriggers.CONSTRUCT_BEACON.trigger(serverPlayer, beaconBlockEntity.levels);
                    }
                } else if (bl && !bl2) {
                    BeaconBlockEntity.playSound(level, blockPos, SoundEvents.BEACON_DEACTIVATE);
                }
            }
        }
    }

    private static int updateBase(Level level, int i, int j, int k) {
        int n;
        int l = 0;
        int m = 1;
        while (m <= 4 && (n = j - m) >= level.getMinBuildHeight()) {
            boolean bl = true;
            block1: for (int o = i - m; o <= i + m && bl; ++o) {
                for (int p = k - m; p <= k + m; ++p) {
                    if (level.getBlockState(new BlockPos(o, n, p)).is(BlockTags.BEACON_BASE_BLOCKS)) continue;
                    bl = false;
                    continue block1;
                }
            }
            if (!bl) break;
            l = m++;
        }
        return l;
    }

    @Override
    public void setRemoved() {
        BeaconBlockEntity.playSound(this.level, this.worldPosition, SoundEvents.BEACON_DEACTIVATE);
        super.setRemoved();
    }

    private static void applyEffects(Level level, BlockPos blockPos, int i, @Nullable MobEffect mobEffect, @Nullable MobEffect mobEffect2) {
        if (level.isClientSide || mobEffect == null) {
            return;
        }
        double d = i * 10 + 10;
        int j = 0;
        if (i >= 4 && mobEffect == mobEffect2) {
            j = 1;
        }
        int k = (9 + i * 2) * 20;
        AABB aABB = new AABB(blockPos).inflate(d).expandTowards(0.0, level.getHeight(), 0.0);
        List<Player> list = level.getEntitiesOfClass(Player.class, aABB);
        for (Player player : list) {
            player.addEffect(new MobEffectInstance(mobEffect, k, j, true, true));
        }
        if (i >= 4 && mobEffect != mobEffect2 && mobEffect2 != null) {
            for (Player player : list) {
                player.addEffect(new MobEffectInstance(mobEffect2, k, 0, true, true));
            }
        }
    }

    public static void playSound(Level level, BlockPos blockPos, SoundEvent soundEvent) {
        level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    public List<BeaconBeamSection> getBeamSections() {
        return this.levels == 0 ? ImmutableList.of() : this.beamSections;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Nullable
    static MobEffect getValidEffectById(int i) {
        MobEffect mobEffect = MobEffect.byId(i);
        return VALID_EFFECTS.contains(mobEffect) ? mobEffect : null;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        this.primaryPower = BeaconBlockEntity.getValidEffectById(compoundTag.getInt("Primary"));
        this.secondaryPower = BeaconBlockEntity.getValidEffectById(compoundTag.getInt("Secondary"));
        if (compoundTag.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(compoundTag.getString("CustomName"));
        }
        this.lockKey = LockCode.fromTag(compoundTag);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        compoundTag.putInt("Primary", MobEffect.getIdFromNullable(this.primaryPower));
        compoundTag.putInt("Secondary", MobEffect.getIdFromNullable(this.secondaryPower));
        compoundTag.putInt("Levels", this.levels);
        if (this.name != null) {
            compoundTag.putString("CustomName", Component.Serializer.toJson(this.name));
        }
        this.lockKey.addToTag(compoundTag);
    }

    public void setCustomName(@Nullable Component component) {
        this.name = component;
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        if (BaseContainerBlockEntity.canUnlock(player, this.lockKey, this.getDisplayName())) {
            return new BeaconMenu(i, inventory, this.dataAccess, ContainerLevelAccess.create(this.level, this.getBlockPos()));
        }
        return null;
    }

    @Override
    public Component getDisplayName() {
        return this.name != null ? this.name : new TranslatableComponent("container.beacon");
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        this.lastCheckY = level.getMinBuildHeight() - 1;
    }

    public /* synthetic */ Packet getUpdatePacket() {
        return this.getUpdatePacket();
    }

    public static class BeaconBeamSection {
        final float[] color;
        private int height;

        public BeaconBeamSection(float[] fs) {
            this.color = fs;
            this.height = 1;
        }

        protected void increaseHeight() {
            ++this.height;
        }

        public float[] getColor() {
            return this.color;
        }

        public int getHeight() {
            return this.height;
        }
    }
}


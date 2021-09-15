/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BannerBlockEntity
extends BlockEntity
implements Nameable {
    public static final int MAX_PATTERNS = 6;
    public static final String TAG_PATTERNS = "Patterns";
    public static final String TAG_PATTERN = "Pattern";
    public static final String TAG_COLOR = "Color";
    @Nullable
    private Component name;
    private DyeColor baseColor;
    @Nullable
    private ListTag itemPatterns;
    private boolean receivedData;
    @Nullable
    private List<Pair<BannerPattern, DyeColor>> patterns;

    public BannerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.BANNER, blockPos, blockState);
        this.baseColor = ((AbstractBannerBlock)blockState.getBlock()).getColor();
    }

    public BannerBlockEntity(BlockPos blockPos, BlockState blockState, DyeColor dyeColor) {
        this(blockPos, blockState);
        this.baseColor = dyeColor;
    }

    @Nullable
    public static ListTag getItemPatterns(ItemStack itemStack) {
        ListTag listTag = null;
        CompoundTag compoundTag = BlockItem.getBlockEntityData(itemStack);
        if (compoundTag != null && compoundTag.contains(TAG_PATTERNS, 9)) {
            listTag = compoundTag.getList(TAG_PATTERNS, 10).copy();
        }
        return listTag;
    }

    public void fromItem(ItemStack itemStack, DyeColor dyeColor) {
        this.itemPatterns = BannerBlockEntity.getItemPatterns(itemStack);
        this.baseColor = dyeColor;
        this.patterns = null;
        this.receivedData = true;
        this.name = itemStack.hasCustomHoverName() ? itemStack.getHoverName() : null;
    }

    @Override
    public Component getName() {
        if (this.name != null) {
            return this.name;
        }
        return new TranslatableComponent("block.minecraft.banner");
    }

    @Override
    @Nullable
    public Component getCustomName() {
        return this.name;
    }

    public void setCustomName(Component component) {
        this.name = component;
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        if (this.itemPatterns != null) {
            compoundTag.put(TAG_PATTERNS, this.itemPatterns);
        }
        if (this.name != null) {
            compoundTag.putString("CustomName", Component.Serializer.toJson(this.name));
        }
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        if (compoundTag.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(compoundTag.getString("CustomName"));
        }
        this.itemPatterns = compoundTag.getList(TAG_PATTERNS, 10);
        this.patterns = null;
        this.receivedData = true;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public static int getPatternCount(ItemStack itemStack) {
        CompoundTag compoundTag = BlockItem.getBlockEntityData(itemStack);
        if (compoundTag != null && compoundTag.contains(TAG_PATTERNS)) {
            return compoundTag.getList(TAG_PATTERNS, 10).size();
        }
        return 0;
    }

    public List<Pair<BannerPattern, DyeColor>> getPatterns() {
        if (this.patterns == null && this.receivedData) {
            this.patterns = BannerBlockEntity.createPatterns(this.baseColor, this.itemPatterns);
        }
        return this.patterns;
    }

    public static List<Pair<BannerPattern, DyeColor>> createPatterns(DyeColor dyeColor, @Nullable ListTag listTag) {
        ArrayList<Pair<BannerPattern, DyeColor>> list = Lists.newArrayList();
        list.add(Pair.of(BannerPattern.BASE, dyeColor));
        if (listTag != null) {
            for (int i = 0; i < listTag.size(); ++i) {
                CompoundTag compoundTag = listTag.getCompound(i);
                BannerPattern bannerPattern = BannerPattern.byHash(compoundTag.getString(TAG_PATTERN));
                if (bannerPattern == null) continue;
                int j = compoundTag.getInt(TAG_COLOR);
                list.add(Pair.of(bannerPattern, DyeColor.byId(j)));
            }
        }
        return list;
    }

    public static void removeLastPattern(ItemStack itemStack) {
        CompoundTag compoundTag = BlockItem.getBlockEntityData(itemStack);
        if (compoundTag == null || !compoundTag.contains(TAG_PATTERNS, 9)) {
            return;
        }
        ListTag listTag = compoundTag.getList(TAG_PATTERNS, 10);
        if (listTag.isEmpty()) {
            return;
        }
        listTag.remove(listTag.size() - 1);
        if (listTag.isEmpty()) {
            compoundTag.remove(TAG_PATTERNS);
        }
        BlockItem.setBlockEntityData(itemStack, BlockEntityType.BANNER, compoundTag);
    }

    public ItemStack getItem() {
        ItemStack itemStack = new ItemStack(BannerBlock.byColor(this.baseColor));
        if (this.itemPatterns != null && !this.itemPatterns.isEmpty()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.put(TAG_PATTERNS, this.itemPatterns.copy());
            BlockItem.setBlockEntityData(itemStack, this.getType(), compoundTag);
        }
        if (this.name != null) {
            itemStack.setHoverName(this.name);
        }
        return itemStack;
    }

    public DyeColor getBaseColor() {
        return this.baseColor;
    }

    public /* synthetic */ Packet getUpdatePacket() {
        return this.getUpdatePacket();
    }
}


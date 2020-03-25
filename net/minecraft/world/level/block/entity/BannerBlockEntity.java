/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Nameable;
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
    @Nullable
    private Component name;
    @Nullable
    private DyeColor baseColor = DyeColor.WHITE;
    @Nullable
    private ListTag itemPatterns;
    private boolean receivedData;
    @Nullable
    private List<Pair<BannerPattern, DyeColor>> patterns;

    public BannerBlockEntity() {
        super(BlockEntityType.BANNER);
    }

    public BannerBlockEntity(DyeColor dyeColor) {
        this();
        this.baseColor = dyeColor;
    }

    @Nullable
    @Environment(value=EnvType.CLIENT)
    public static ListTag getItemPatterns(ItemStack itemStack) {
        ListTag listTag = null;
        CompoundTag compoundTag = itemStack.getTagElement("BlockEntityTag");
        if (compoundTag != null && compoundTag.contains("Patterns", 9)) {
            listTag = compoundTag.getList("Patterns", 10).copy();
        }
        return listTag;
    }

    @Environment(value=EnvType.CLIENT)
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
        return new TranslatableComponent("block.minecraft.banner", new Object[0]);
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
    public CompoundTag save(CompoundTag compoundTag) {
        super.save(compoundTag);
        if (this.itemPatterns != null) {
            compoundTag.put("Patterns", this.itemPatterns);
        }
        if (this.name != null) {
            compoundTag.putString("CustomName", Component.Serializer.toJson(this.name));
        }
        return compoundTag;
    }

    @Override
    public void load(BlockState blockState, CompoundTag compoundTag) {
        super.load(blockState, compoundTag);
        if (compoundTag.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(compoundTag.getString("CustomName"));
        }
        this.baseColor = this.hasLevel() ? ((AbstractBannerBlock)this.getBlockState().getBlock()).getColor() : null;
        this.itemPatterns = compoundTag.getList("Patterns", 10);
        this.patterns = null;
        this.receivedData = true;
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 6, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    public static int getPatternCount(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTagElement("BlockEntityTag");
        if (compoundTag != null && compoundTag.contains("Patterns")) {
            return compoundTag.getList("Patterns", 10).size();
        }
        return 0;
    }

    @Environment(value=EnvType.CLIENT)
    public List<Pair<BannerPattern, DyeColor>> getPatterns() {
        if (this.patterns == null && this.receivedData) {
            this.patterns = BannerBlockEntity.createPatterns(this.getBaseColor(this::getBlockState), this.itemPatterns);
        }
        return this.patterns;
    }

    @Environment(value=EnvType.CLIENT)
    public static List<Pair<BannerPattern, DyeColor>> createPatterns(DyeColor dyeColor, @Nullable ListTag listTag) {
        ArrayList<Pair<BannerPattern, DyeColor>> list = Lists.newArrayList();
        list.add(Pair.of(BannerPattern.BASE, dyeColor));
        if (listTag != null) {
            for (int i = 0; i < listTag.size(); ++i) {
                CompoundTag compoundTag = listTag.getCompound(i);
                BannerPattern bannerPattern = BannerPattern.byHash(compoundTag.getString("Pattern"));
                if (bannerPattern == null) continue;
                int j = compoundTag.getInt("Color");
                list.add(Pair.of(bannerPattern, DyeColor.byId(j)));
            }
        }
        return list;
    }

    public static void removeLastPattern(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTagElement("BlockEntityTag");
        if (compoundTag == null || !compoundTag.contains("Patterns", 9)) {
            return;
        }
        ListTag listTag = compoundTag.getList("Patterns", 10);
        if (listTag.isEmpty()) {
            return;
        }
        listTag.remove(listTag.size() - 1);
        if (listTag.isEmpty()) {
            itemStack.removeTagKey("BlockEntityTag");
        }
    }

    @Environment(value=EnvType.CLIENT)
    public ItemStack getItem(BlockState blockState) {
        ItemStack itemStack = new ItemStack(BannerBlock.byColor(this.getBaseColor(() -> blockState)));
        if (this.itemPatterns != null && !this.itemPatterns.isEmpty()) {
            itemStack.getOrCreateTagElement("BlockEntityTag").put("Patterns", this.itemPatterns.copy());
        }
        if (this.name != null) {
            itemStack.setHoverName(this.name);
        }
        return itemStack;
    }

    public DyeColor getBaseColor(Supplier<BlockState> supplier) {
        if (this.baseColor == null) {
            this.baseColor = ((AbstractBannerBlock)supplier.get().getBlock()).getColor();
        }
        return this.baseColor;
    }
}


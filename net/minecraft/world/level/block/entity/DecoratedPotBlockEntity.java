/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class DecoratedPotBlockEntity
extends BlockEntity {
    private static final String TAG_SHARDS = "shards";
    private static final int SHARDS_IN_POT = 4;
    private boolean isBroken = false;
    private final List<Item> shards = Util.make(new ArrayList(4), arrayList -> {
        arrayList.add(Items.BRICK);
        arrayList.add(Items.BRICK);
        arrayList.add(Items.BRICK);
        arrayList.add(Items.BRICK);
    });

    public DecoratedPotBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.DECORATED_POT, blockPos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        DecoratedPotBlockEntity.saveShards(this.shards, compoundTag);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        if (compoundTag.contains(TAG_SHARDS, 9)) {
            int j;
            ListTag listTag = compoundTag.getList(TAG_SHARDS, 8);
            this.shards.clear();
            int i = Math.min(4, listTag.size());
            for (j = 0; j < i; ++j) {
                Tag tag = listTag.get(j);
                if (tag instanceof StringTag) {
                    StringTag stringTag = (StringTag)tag;
                    this.shards.add(BuiltInRegistries.ITEM.get(new ResourceLocation(stringTag.getAsString())));
                    continue;
                }
                this.shards.add(Items.BRICK);
            }
            j = 4 - i;
            for (int k = 0; k < j; ++k) {
                this.shards.add(Items.BRICK);
            }
        }
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public static void saveShards(List<Item> list, CompoundTag compoundTag) {
        ListTag listTag = new ListTag();
        for (Item item : list) {
            listTag.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(item).toString()));
        }
        compoundTag.put(TAG_SHARDS, listTag);
    }

    public ItemStack getItem() {
        ItemStack itemStack = new ItemStack(Blocks.DECORATED_POT);
        CompoundTag compoundTag = new CompoundTag();
        DecoratedPotBlockEntity.saveShards(this.shards, compoundTag);
        BlockItem.setBlockEntityData(itemStack, BlockEntityType.DECORATED_POT, compoundTag);
        return itemStack;
    }

    public List<Item> getShards() {
        return this.shards;
    }

    public void playerDestroy(Level level, BlockPos blockPos, ItemStack itemStack, Player player) {
        if (player.isCreative()) {
            this.isBroken = true;
            return;
        }
        if (itemStack.is(ItemTags.BREAKS_DECORATED_POTS) && !EnchantmentHelper.hasSilkTouch(itemStack)) {
            List<Item> list = this.getShards();
            NonNullList<ItemStack> nonNullList = NonNullList.createWithCapacity(list.size());
            nonNullList.addAll(0, list.stream().map(Item::getDefaultInstance).toList());
            Containers.dropContents(level, blockPos, nonNullList);
            this.isBroken = true;
            level.playSound(null, blockPos, SoundEvents.DECORATED_POT_SHATTER, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    public boolean isBroken() {
        return this.isBroken;
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public void setFromItem(ItemStack itemStack) {
        CompoundTag compoundTag = BlockItem.getBlockEntityData(itemStack);
        if (compoundTag != null) {
            this.load(compoundTag);
        }
    }

    public /* synthetic */ Packet getUpdatePacket() {
        return this.getUpdatePacket();
    }
}


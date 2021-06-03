/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.apache.commons.lang3.StringUtils;

public class PlayerHeadItem
extends StandingAndWallBlockItem {
    public static final String TAG_SKULL_OWNER = "SkullOwner";

    public PlayerHeadItem(Block block, Block block2, Item.Properties properties) {
        super(block, block2, properties);
    }

    @Override
    public Component getName(ItemStack itemStack) {
        if (itemStack.is(Items.PLAYER_HEAD) && itemStack.hasTag()) {
            CompoundTag compoundTag2;
            String string = null;
            CompoundTag compoundTag = itemStack.getTag();
            if (compoundTag.contains(TAG_SKULL_OWNER, 8)) {
                string = compoundTag.getString(TAG_SKULL_OWNER);
            } else if (compoundTag.contains(TAG_SKULL_OWNER, 10) && (compoundTag2 = compoundTag.getCompound(TAG_SKULL_OWNER)).contains("Name", 8)) {
                string = compoundTag2.getString("Name");
            }
            if (string != null) {
                return new TranslatableComponent(this.getDescriptionId() + ".named", string);
            }
        }
        return super.getName(itemStack);
    }

    @Override
    public void verifyTagAfterLoad(CompoundTag compoundTag) {
        super.verifyTagAfterLoad(compoundTag);
        if (compoundTag.contains(TAG_SKULL_OWNER, 8) && !StringUtils.isBlank(compoundTag.getString(TAG_SKULL_OWNER))) {
            GameProfile gameProfile2 = new GameProfile(null, compoundTag.getString(TAG_SKULL_OWNER));
            SkullBlockEntity.updateGameprofile(gameProfile2, gameProfile -> compoundTag.put(TAG_SKULL_OWNER, NbtUtils.writeGameProfile(new CompoundTag(), gameProfile)));
        }
    }
}


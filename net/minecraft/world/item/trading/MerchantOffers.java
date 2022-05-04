/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.trading;

import java.util.ArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.Nullable;

public class MerchantOffers
extends ArrayList<MerchantOffer> {
    public MerchantOffers() {
    }

    private MerchantOffers(int i) {
        super(i);
    }

    public MerchantOffers(CompoundTag compoundTag) {
        ListTag listTag = compoundTag.getList("Recipes", 10);
        for (int i = 0; i < listTag.size(); ++i) {
            this.add(new MerchantOffer(listTag.getCompound(i)));
        }
    }

    @Nullable
    public MerchantOffer getRecipeFor(ItemStack itemStack, ItemStack itemStack2, int i) {
        if (i > 0 && i < this.size()) {
            MerchantOffer merchantOffer = (MerchantOffer)this.get(i);
            if (merchantOffer.satisfiedBy(itemStack, itemStack2)) {
                return merchantOffer;
            }
            return null;
        }
        for (int j = 0; j < this.size(); ++j) {
            MerchantOffer merchantOffer2 = (MerchantOffer)this.get(j);
            if (!merchantOffer2.satisfiedBy(itemStack, itemStack2)) continue;
            return merchantOffer2;
        }
        return null;
    }

    public void writeToStream(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeCollection(this, (friendlyByteBuf, merchantOffer) -> {
            friendlyByteBuf.writeItem(merchantOffer.getBaseCostA());
            friendlyByteBuf.writeItem(merchantOffer.getResult());
            friendlyByteBuf.writeItem(merchantOffer.getCostB());
            friendlyByteBuf.writeBoolean(merchantOffer.isOutOfStock());
            friendlyByteBuf.writeInt(merchantOffer.getUses());
            friendlyByteBuf.writeInt(merchantOffer.getMaxUses());
            friendlyByteBuf.writeInt(merchantOffer.getXp());
            friendlyByteBuf.writeInt(merchantOffer.getSpecialPriceDiff());
            friendlyByteBuf.writeFloat(merchantOffer.getPriceMultiplier());
            friendlyByteBuf.writeInt(merchantOffer.getDemand());
        });
    }

    public static MerchantOffers createFromStream(FriendlyByteBuf friendlyByteBuf2) {
        return friendlyByteBuf2.readCollection(MerchantOffers::new, friendlyByteBuf -> {
            ItemStack itemStack = friendlyByteBuf.readItem();
            ItemStack itemStack2 = friendlyByteBuf.readItem();
            ItemStack itemStack3 = friendlyByteBuf.readItem();
            boolean bl = friendlyByteBuf.readBoolean();
            int i = friendlyByteBuf.readInt();
            int j = friendlyByteBuf.readInt();
            int k = friendlyByteBuf.readInt();
            int l = friendlyByteBuf.readInt();
            float f = friendlyByteBuf.readFloat();
            int m = friendlyByteBuf.readInt();
            MerchantOffer merchantOffer = new MerchantOffer(itemStack, itemStack3, itemStack2, i, j, k, f, m);
            if (bl) {
                merchantOffer.setToOutOfStock();
            }
            merchantOffer.setSpecialPriceDiff(l);
            return merchantOffer;
        });
    }

    public CompoundTag createTag() {
        CompoundTag compoundTag = new CompoundTag();
        ListTag listTag = new ListTag();
        for (int i = 0; i < this.size(); ++i) {
            MerchantOffer merchantOffer = (MerchantOffer)this.get(i);
            listTag.add(merchantOffer.createTag());
        }
        compoundTag.put("Recipes", listTag);
        return compoundTag;
    }
}


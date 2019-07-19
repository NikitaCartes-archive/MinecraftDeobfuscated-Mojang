package net.minecraft.world.item.trading;

import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class MerchantOffers extends ArrayList<MerchantOffer> {
	public MerchantOffers() {
	}

	public MerchantOffers(CompoundTag compoundTag) {
		ListTag listTag = compoundTag.getList("Recipes", 10);

		for (int i = 0; i < listTag.size(); i++) {
			this.add(new MerchantOffer(listTag.getCompound(i)));
		}
	}

	@Nullable
	public MerchantOffer getRecipeFor(ItemStack itemStack, ItemStack itemStack2, int i) {
		if (i > 0 && i < this.size()) {
			MerchantOffer merchantOffer = (MerchantOffer)this.get(i);
			return merchantOffer.satisfiedBy(itemStack, itemStack2) ? merchantOffer : null;
		} else {
			for (int j = 0; j < this.size(); j++) {
				MerchantOffer merchantOffer2 = (MerchantOffer)this.get(j);
				if (merchantOffer2.satisfiedBy(itemStack, itemStack2)) {
					return merchantOffer2;
				}
			}

			return null;
		}
	}

	public void writeToStream(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByte((byte)(this.size() & 0xFF));

		for (int i = 0; i < this.size(); i++) {
			MerchantOffer merchantOffer = (MerchantOffer)this.get(i);
			friendlyByteBuf.writeItem(merchantOffer.getBaseCostA());
			friendlyByteBuf.writeItem(merchantOffer.getResult());
			ItemStack itemStack = merchantOffer.getCostB();
			friendlyByteBuf.writeBoolean(!itemStack.isEmpty());
			if (!itemStack.isEmpty()) {
				friendlyByteBuf.writeItem(itemStack);
			}

			friendlyByteBuf.writeBoolean(merchantOffer.isOutOfStock());
			friendlyByteBuf.writeInt(merchantOffer.getUses());
			friendlyByteBuf.writeInt(merchantOffer.getMaxUses());
			friendlyByteBuf.writeInt(merchantOffer.getXp());
			friendlyByteBuf.writeInt(merchantOffer.getSpecialPriceDiff());
			friendlyByteBuf.writeFloat(merchantOffer.getPriceMultiplier());
			friendlyByteBuf.writeInt(merchantOffer.getDemand());
		}
	}

	public static MerchantOffers createFromStream(FriendlyByteBuf friendlyByteBuf) {
		MerchantOffers merchantOffers = new MerchantOffers();
		int i = friendlyByteBuf.readByte() & 255;

		for (int j = 0; j < i; j++) {
			ItemStack itemStack = friendlyByteBuf.readItem();
			ItemStack itemStack2 = friendlyByteBuf.readItem();
			ItemStack itemStack3 = ItemStack.EMPTY;
			if (friendlyByteBuf.readBoolean()) {
				itemStack3 = friendlyByteBuf.readItem();
			}

			boolean bl = friendlyByteBuf.readBoolean();
			int k = friendlyByteBuf.readInt();
			int l = friendlyByteBuf.readInt();
			int m = friendlyByteBuf.readInt();
			int n = friendlyByteBuf.readInt();
			float f = friendlyByteBuf.readFloat();
			int o = friendlyByteBuf.readInt();
			MerchantOffer merchantOffer = new MerchantOffer(itemStack, itemStack3, itemStack2, k, l, m, f, o);
			if (bl) {
				merchantOffer.setToOutOfStock();
			}

			merchantOffer.setSpecialPriceDiff(n);
			merchantOffers.add(merchantOffer);
		}

		return merchantOffers;
	}

	public CompoundTag createTag() {
		CompoundTag compoundTag = new CompoundTag();
		ListTag listTag = new ListTag();

		for (int i = 0; i < this.size(); i++) {
			MerchantOffer merchantOffer = (MerchantOffer)this.get(i);
			listTag.add(merchantOffer.createTag());
		}

		compoundTag.put("Recipes", listTag);
		return compoundTag;
	}
}

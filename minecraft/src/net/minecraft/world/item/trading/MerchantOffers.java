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

	private MerchantOffers(int i) {
		super(i);
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
		friendlyByteBuf.writeCollection(this, (friendlyByteBufx, merchantOffer) -> {
			friendlyByteBufx.writeItem(merchantOffer.getBaseCostA());
			friendlyByteBufx.writeItem(merchantOffer.getResult());
			friendlyByteBufx.writeItem(merchantOffer.getCostB());
			friendlyByteBufx.writeBoolean(merchantOffer.isOutOfStock());
			friendlyByteBufx.writeInt(merchantOffer.getUses());
			friendlyByteBufx.writeInt(merchantOffer.getMaxUses());
			friendlyByteBufx.writeInt(merchantOffer.getXp());
			friendlyByteBufx.writeInt(merchantOffer.getSpecialPriceDiff());
			friendlyByteBufx.writeFloat(merchantOffer.getPriceMultiplier());
			friendlyByteBufx.writeInt(merchantOffer.getDemand());
		});
	}

	public static MerchantOffers createFromStream(FriendlyByteBuf friendlyByteBuf) {
		return friendlyByteBuf.readCollection(MerchantOffers::new, friendlyByteBufx -> {
			ItemStack itemStack = friendlyByteBufx.readItem();
			ItemStack itemStack2 = friendlyByteBufx.readItem();
			ItemStack itemStack3 = friendlyByteBufx.readItem();
			boolean bl = friendlyByteBufx.readBoolean();
			int i = friendlyByteBufx.readInt();
			int j = friendlyByteBufx.readInt();
			int k = friendlyByteBufx.readInt();
			int l = friendlyByteBufx.readInt();
			float f = friendlyByteBufx.readFloat();
			int m = friendlyByteBufx.readInt();
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

		for (int i = 0; i < this.size(); i++) {
			MerchantOffer merchantOffer = (MerchantOffer)this.get(i);
			listTag.add(merchantOffer.createTag());
		}

		compoundTag.put("Recipes", listTag);
		return compoundTag;
	}

	public MerchantOffers copy() {
		MerchantOffers merchantOffers = new MerchantOffers(this.size());

		for (MerchantOffer merchantOffer : this) {
			merchantOffers.add(merchantOffer.copy());
		}

		return merchantOffers;
	}
}

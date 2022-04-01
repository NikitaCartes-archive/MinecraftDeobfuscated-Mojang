package net.minecraft.world.item.trading;

import java.util.ArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

public class MerchantOffers extends ArrayList<MerchantOffer> {
	public MerchantOffers() {
	}

	public MerchantOffers(CompoundTag compoundTag) {
		ListTag listTag = compoundTag.getList("Recipes", 10);

		for (int i = 0; i < listTag.size(); i++) {
			this.add(new MerchantOffer(listTag.getCompound(i)));
		}
	}

	public void writeToStream(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByte((byte)(this.size() & 0xFF));

		for (int i = 0; i < this.size(); i++) {
			MerchantOffer merchantOffer = (MerchantOffer)this.get(i);
			friendlyByteBuf.writeWithCodec(CarryableTrade.CODEC, merchantOffer.getCost());
			friendlyByteBuf.writeWithCodec(CarryableTrade.CODEC, merchantOffer.getResult());
			friendlyByteBuf.writeBoolean(merchantOffer.isOutOfStock());
			friendlyByteBuf.writeInt(merchantOffer.getUses());
			friendlyByteBuf.writeInt(merchantOffer.getMaxUses());
			friendlyByteBuf.writeInt(merchantOffer.getXp());
			friendlyByteBuf.writeFloat(merchantOffer.getPriceMultiplier());
			friendlyByteBuf.writeInt(merchantOffer.getDemand());
		}
	}

	public static MerchantOffers createFromStream(FriendlyByteBuf friendlyByteBuf) {
		MerchantOffers merchantOffers = new MerchantOffers();
		int i = friendlyByteBuf.readByte() & 255;

		for (int j = 0; j < i; j++) {
			CarryableTrade carryableTrade = friendlyByteBuf.readWithCodec(CarryableTrade.CODEC);
			CarryableTrade carryableTrade2 = friendlyByteBuf.readWithCodec(CarryableTrade.CODEC);
			boolean bl = friendlyByteBuf.readBoolean();
			int k = friendlyByteBuf.readInt();
			int l = friendlyByteBuf.readInt();
			int m = friendlyByteBuf.readInt();
			float f = friendlyByteBuf.readFloat();
			int n = friendlyByteBuf.readInt();
			MerchantOffer merchantOffer = new MerchantOffer(carryableTrade, carryableTrade2, k, l, m, f, n);
			if (bl) {
				merchantOffer.setToOutOfStock();
			}

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

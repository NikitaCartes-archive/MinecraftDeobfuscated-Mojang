package net.minecraft.world.item.trading;

import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class MerchantOffers extends ArrayList<MerchantOffer> {
	public static final StreamCodec<RegistryFriendlyByteBuf, MerchantOffers> STREAM_CODEC = MerchantOffer.STREAM_CODEC
		.apply(ByteBufCodecs.collection(MerchantOffers::new));

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

package net.minecraft.world.item.trading;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class MerchantOffers extends ArrayList<MerchantOffer> {
	public static final Codec<MerchantOffers> CODEC = MerchantOffer.CODEC
		.listOf()
		.fieldOf("Recipes")
		.<MerchantOffers>xmap(MerchantOffers::new, Function.identity())
		.codec();
	public static final StreamCodec<RegistryFriendlyByteBuf, MerchantOffers> STREAM_CODEC = MerchantOffer.STREAM_CODEC
		.apply(ByteBufCodecs.collection(MerchantOffers::new));

	public MerchantOffers() {
	}

	private MerchantOffers(int i) {
		super(i);
	}

	private MerchantOffers(Collection<MerchantOffer> collection) {
		super(collection);
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

	public MerchantOffers copy() {
		MerchantOffers merchantOffers = new MerchantOffers(this.size());

		for (MerchantOffer merchantOffer : this) {
			merchantOffers.add(merchantOffer.copy());
		}

		return merchantOffers;
	}
}

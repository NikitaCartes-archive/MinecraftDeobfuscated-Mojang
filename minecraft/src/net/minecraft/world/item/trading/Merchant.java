package net.minecraft.world.item.trading;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface Merchant {
	void setTradingPlayer(@Nullable Player player);

	@Nullable
	Player getTradingPlayer();

	MerchantOffers getOffers();

	void overrideOffers(MerchantOffers merchantOffers);

	void notifyTrade(MerchantOffer merchantOffer);

	void notifyTradeUpdated(ItemStack itemStack);

	int getVillagerXp();

	void overrideXp(int i);

	boolean showProgressBar();

	SoundEvent getNotifyTradeSound();

	default boolean canRestock() {
		return false;
	}

	boolean isClientSide();
}

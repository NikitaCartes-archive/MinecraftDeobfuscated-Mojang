package net.minecraft.world.item.trading;

import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface Merchant {
	void setTradingPlayer(@Nullable Player player);

	@Nullable
	Player getTradingPlayer();

	MerchantOffers getOffers();

	@Environment(EnvType.CLIENT)
	void overrideOffers(@Nullable MerchantOffers merchantOffers);

	void notifyTrade(MerchantOffer merchantOffer);

	void notifyTradeUpdated(ItemStack itemStack);

	Level getLevel();

	int getVillagerXp();

	void overrideXp(int i);

	boolean showProgressBar();

	SoundEvent getNotifyTradeSound();

	default boolean canRestock() {
		return false;
	}

	default void openTradingScreen(Player player, Component component, int i) {
		OptionalInt optionalInt = player.openMenu(new SimpleMenuProvider((ix, inventory, playerx) -> new MerchantMenu(ix, inventory, this), component));
		if (optionalInt.isPresent()) {
			MerchantOffers merchantOffers = this.getOffers();
			if (!merchantOffers.isEmpty()) {
				player.sendMerchantOffers(optionalInt.getAsInt(), merchantOffers, i, this.getVillagerXp(), this.showProgressBar(), this.canRestock());
			}
		}
	}
}

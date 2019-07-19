package net.minecraft.world.entity.npc;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

public class ClientSideMerchant implements Merchant {
	private final MerchantContainer container;
	private final Player source;
	private MerchantOffers offers = new MerchantOffers();
	private int xp;

	public ClientSideMerchant(Player player) {
		this.source = player;
		this.container = new MerchantContainer(this);
	}

	@Nullable
	@Override
	public Player getTradingPlayer() {
		return this.source;
	}

	@Override
	public void setTradingPlayer(@Nullable Player player) {
	}

	@Override
	public MerchantOffers getOffers() {
		return this.offers;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void overrideOffers(@Nullable MerchantOffers merchantOffers) {
		this.offers = merchantOffers;
	}

	@Override
	public void notifyTrade(MerchantOffer merchantOffer) {
		merchantOffer.increaseUses();
	}

	@Override
	public void notifyTradeUpdated(ItemStack itemStack) {
	}

	@Override
	public Level getLevel() {
		return this.source.level;
	}

	@Override
	public int getVillagerXp() {
		return this.xp;
	}

	@Override
	public void overrideXp(int i) {
		this.xp = i;
	}

	@Override
	public boolean showProgressBar() {
		return true;
	}

	@Override
	public SoundEvent getNotifyTradeSound() {
		return SoundEvents.VILLAGER_YES;
	}
}

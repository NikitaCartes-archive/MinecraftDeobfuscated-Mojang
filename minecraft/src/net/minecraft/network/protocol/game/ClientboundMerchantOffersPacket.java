package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.trading.MerchantOffers;

public class ClientboundMerchantOffersPacket implements Packet<ClientGamePacketListener> {
	private final int containerId;
	private final MerchantOffers offers;
	private final int villagerLevel;
	private final int villagerXp;
	private final boolean showProgress;
	private final boolean canRestock;

	public ClientboundMerchantOffersPacket(int i, MerchantOffers merchantOffers, int j, int k, boolean bl, boolean bl2) {
		this.containerId = i;
		this.offers = merchantOffers;
		this.villagerLevel = j;
		this.villagerXp = k;
		this.showProgress = bl;
		this.canRestock = bl2;
	}

	public ClientboundMerchantOffersPacket(FriendlyByteBuf friendlyByteBuf) {
		this.containerId = friendlyByteBuf.readVarInt();
		this.offers = MerchantOffers.createFromStream(friendlyByteBuf);
		this.villagerLevel = friendlyByteBuf.readVarInt();
		this.villagerXp = friendlyByteBuf.readVarInt();
		this.showProgress = friendlyByteBuf.readBoolean();
		this.canRestock = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.containerId);
		this.offers.writeToStream(friendlyByteBuf);
		friendlyByteBuf.writeVarInt(this.villagerLevel);
		friendlyByteBuf.writeVarInt(this.villagerXp);
		friendlyByteBuf.writeBoolean(this.showProgress);
		friendlyByteBuf.writeBoolean(this.canRestock);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleMerchantOffers(this);
	}

	public int getContainerId() {
		return this.containerId;
	}

	public MerchantOffers getOffers() {
		return this.offers;
	}

	public int getVillagerLevel() {
		return this.villagerLevel;
	}

	public int getVillagerXp() {
		return this.villagerXp;
	}

	public boolean showProgress() {
		return this.showProgress;
	}

	public boolean canRestock() {
		return this.canRestock;
	}
}

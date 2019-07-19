package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.trading.MerchantOffers;

public class ClientboundMerchantOffersPacket implements Packet<ClientGamePacketListener> {
	private int containerId;
	private MerchantOffers offers;
	private int villagerLevel;
	private int villagerXp;
	private boolean showProgress;
	private boolean canRestock;

	public ClientboundMerchantOffersPacket() {
	}

	public ClientboundMerchantOffersPacket(int i, MerchantOffers merchantOffers, int j, int k, boolean bl, boolean bl2) {
		this.containerId = i;
		this.offers = merchantOffers;
		this.villagerLevel = j;
		this.villagerXp = k;
		this.showProgress = bl;
		this.canRestock = bl2;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.containerId = friendlyByteBuf.readVarInt();
		this.offers = MerchantOffers.createFromStream(friendlyByteBuf);
		this.villagerLevel = friendlyByteBuf.readVarInt();
		this.villagerXp = friendlyByteBuf.readVarInt();
		this.showProgress = friendlyByteBuf.readBoolean();
		this.canRestock = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
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

	@Environment(EnvType.CLIENT)
	public int getContainerId() {
		return this.containerId;
	}

	@Environment(EnvType.CLIENT)
	public MerchantOffers getOffers() {
		return this.offers;
	}

	@Environment(EnvType.CLIENT)
	public int getVillagerLevel() {
		return this.villagerLevel;
	}

	@Environment(EnvType.CLIENT)
	public int getVillagerXp() {
		return this.villagerXp;
	}

	@Environment(EnvType.CLIENT)
	public boolean showProgress() {
		return this.showProgress;
	}

	@Environment(EnvType.CLIENT)
	public boolean canRestock() {
		return this.canRestock;
	}
}

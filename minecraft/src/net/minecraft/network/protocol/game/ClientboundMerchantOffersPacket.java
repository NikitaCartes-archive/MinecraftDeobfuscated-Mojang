package net.minecraft.network.protocol.game;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.trading.MerchantOffers;

public class ClientboundMerchantOffersPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundMerchantOffersPacket> STREAM_CODEC = Packet.codec(
		ClientboundMerchantOffersPacket::write, ClientboundMerchantOffersPacket::new
	);
	private final int containerId;
	private final MerchantOffers offers;
	private final int villagerLevel;
	private final int villagerXp;
	private final boolean showProgress;
	private final boolean canRestock;

	public ClientboundMerchantOffersPacket(int i, MerchantOffers merchantOffers, int j, int k, boolean bl, boolean bl2) {
		this.containerId = i;
		this.offers = merchantOffers.copy();
		this.villagerLevel = j;
		this.villagerXp = k;
		this.showProgress = bl;
		this.canRestock = bl2;
	}

	private ClientboundMerchantOffersPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		this.containerId = registryFriendlyByteBuf.readContainerId();
		this.offers = MerchantOffers.STREAM_CODEC.decode(registryFriendlyByteBuf);
		this.villagerLevel = registryFriendlyByteBuf.readVarInt();
		this.villagerXp = registryFriendlyByteBuf.readVarInt();
		this.showProgress = registryFriendlyByteBuf.readBoolean();
		this.canRestock = registryFriendlyByteBuf.readBoolean();
	}

	private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		registryFriendlyByteBuf.writeContainerId(this.containerId);
		MerchantOffers.STREAM_CODEC.encode(registryFriendlyByteBuf, this.offers);
		registryFriendlyByteBuf.writeVarInt(this.villagerLevel);
		registryFriendlyByteBuf.writeVarInt(this.villagerXp);
		registryFriendlyByteBuf.writeBoolean(this.showProgress);
		registryFriendlyByteBuf.writeBoolean(this.canRestock);
	}

	@Override
	public PacketType<ClientboundMerchantOffersPacket> type() {
		return GamePacketTypes.CLIENTBOUND_MERCHANT_OFFERS;
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

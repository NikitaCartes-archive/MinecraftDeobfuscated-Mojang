package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class ClientboundUpdateAttributesPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdateAttributesPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		ClientboundUpdateAttributesPacket::getEntityId,
		ClientboundUpdateAttributesPacket.AttributeSnapshot.STREAM_CODEC.apply(ByteBufCodecs.list()),
		ClientboundUpdateAttributesPacket::getValues,
		ClientboundUpdateAttributesPacket::new
	);
	private final int entityId;
	private final List<ClientboundUpdateAttributesPacket.AttributeSnapshot> attributes;

	public ClientboundUpdateAttributesPacket(int i, Collection<AttributeInstance> collection) {
		this.entityId = i;
		this.attributes = Lists.<ClientboundUpdateAttributesPacket.AttributeSnapshot>newArrayList();

		for (AttributeInstance attributeInstance : collection) {
			this.attributes
				.add(
					new ClientboundUpdateAttributesPacket.AttributeSnapshot(
						attributeInstance.getAttribute(), attributeInstance.getBaseValue(), attributeInstance.getModifiers()
					)
				);
		}
	}

	private ClientboundUpdateAttributesPacket(int i, List<ClientboundUpdateAttributesPacket.AttributeSnapshot> list) {
		this.entityId = i;
		this.attributes = list;
	}

	@Override
	public PacketType<ClientboundUpdateAttributesPacket> type() {
		return GamePacketTypes.CLIENTBOUND_UPDATE_ATTRIBUTES;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleUpdateAttributes(this);
	}

	public int getEntityId() {
		return this.entityId;
	}

	public List<ClientboundUpdateAttributesPacket.AttributeSnapshot> getValues() {
		return this.attributes;
	}

	public static record AttributeSnapshot(Holder<Attribute> attribute, double base, Collection<AttributeModifier> modifiers) {
		public static final StreamCodec<ByteBuf, AttributeModifier> MODIFIER_STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC,
			AttributeModifier::id,
			ByteBufCodecs.DOUBLE,
			AttributeModifier::amount,
			AttributeModifier.Operation.STREAM_CODEC,
			AttributeModifier::operation,
			(uUID, double_, operation) -> new AttributeModifier(uUID, "Unknown synced attribute modifier", double_, operation)
		);
		public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdateAttributesPacket.AttributeSnapshot> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.holderRegistry(Registries.ATTRIBUTE),
			ClientboundUpdateAttributesPacket.AttributeSnapshot::attribute,
			ByteBufCodecs.DOUBLE,
			ClientboundUpdateAttributesPacket.AttributeSnapshot::base,
			MODIFIER_STREAM_CODEC.apply(ByteBufCodecs.collection(ArrayList::new)),
			ClientboundUpdateAttributesPacket.AttributeSnapshot::modifiers,
			ClientboundUpdateAttributesPacket.AttributeSnapshot::new
		);
	}
}

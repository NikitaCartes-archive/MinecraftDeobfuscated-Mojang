package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import io.netty.handler.codec.DecoderException;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class ClientboundUpdateAttributesPacket implements Packet<ClientGamePacketListener> {
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

	public ClientboundUpdateAttributesPacket(FriendlyByteBuf friendlyByteBuf) {
		this.entityId = friendlyByteBuf.readVarInt();
		this.attributes = friendlyByteBuf.readList(
			friendlyByteBufx -> {
				Holder<Attribute> holder = friendlyByteBufx.readById(BuiltInRegistries.ATTRIBUTE.asHolderIdMap());
				if (holder == null) {
					throw new DecoderException("Received unrecognized attribute id");
				} else {
					double d = friendlyByteBufx.readDouble();
					List<AttributeModifier> list = friendlyByteBufx.readList(
						friendlyByteBufxx -> new AttributeModifier(
								friendlyByteBufxx.readUUID(),
								"Unknown synced attribute modifier",
								friendlyByteBufxx.readDouble(),
								AttributeModifier.Operation.fromValue(friendlyByteBufxx.readByte())
							)
					);
					return new ClientboundUpdateAttributesPacket.AttributeSnapshot(holder, d, list);
				}
			}
		);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.entityId);
		friendlyByteBuf.writeCollection(this.attributes, (friendlyByteBufx, attributeSnapshot) -> {
			friendlyByteBufx.writeId(BuiltInRegistries.ATTRIBUTE.asHolderIdMap(), attributeSnapshot.attribute());
			friendlyByteBufx.writeDouble(attributeSnapshot.base());
			friendlyByteBufx.writeCollection(attributeSnapshot.modifiers(), (friendlyByteBufxx, attributeModifier) -> {
				friendlyByteBufxx.writeUUID(attributeModifier.getId());
				friendlyByteBufxx.writeDouble(attributeModifier.getAmount());
				friendlyByteBufxx.writeByte(attributeModifier.getOperation().toValue());
			});
		});
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
	}
}

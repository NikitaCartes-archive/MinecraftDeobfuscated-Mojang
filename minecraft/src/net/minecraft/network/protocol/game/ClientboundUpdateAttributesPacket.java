package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
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
				ResourceLocation resourceLocation = friendlyByteBufx.readResourceLocation();
				Attribute attribute = Registry.ATTRIBUTE.get(resourceLocation);
				double d = friendlyByteBufx.readDouble();
				List<AttributeModifier> list = friendlyByteBufx.readList(
					friendlyByteBufxx -> new AttributeModifier(
							friendlyByteBufxx.readUUID(),
							"Unknown synced attribute modifier",
							friendlyByteBufxx.readDouble(),
							AttributeModifier.Operation.fromValue(friendlyByteBufxx.readByte())
						)
				);
				return new ClientboundUpdateAttributesPacket.AttributeSnapshot(attribute, d, list);
			}
		);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.entityId);
		friendlyByteBuf.writeCollection(this.attributes, (friendlyByteBufx, attributeSnapshot) -> {
			friendlyByteBufx.writeResourceLocation(Registry.ATTRIBUTE.getKey(attributeSnapshot.getAttribute()));
			friendlyByteBufx.writeDouble(attributeSnapshot.getBase());
			friendlyByteBufx.writeCollection(attributeSnapshot.getModifiers(), (friendlyByteBufxx, attributeModifier) -> {
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

	public static class AttributeSnapshot {
		private final Attribute attribute;
		private final double base;
		private final Collection<AttributeModifier> modifiers;

		public AttributeSnapshot(Attribute attribute, double d, Collection<AttributeModifier> collection) {
			this.attribute = attribute;
			this.base = d;
			this.modifiers = collection;
		}

		public Attribute getAttribute() {
			return this.attribute;
		}

		public double getBase() {
			return this.base;
		}

		public Collection<AttributeModifier> getModifiers() {
			return this.modifiers;
		}
	}
}

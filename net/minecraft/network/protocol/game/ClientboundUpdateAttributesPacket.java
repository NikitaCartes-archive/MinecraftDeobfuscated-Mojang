/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class ClientboundUpdateAttributesPacket
implements Packet<ClientGamePacketListener> {
    private final int entityId;
    private final List<AttributeSnapshot> attributes;

    public ClientboundUpdateAttributesPacket(int i, Collection<AttributeInstance> collection) {
        this.entityId = i;
        this.attributes = Lists.newArrayList();
        for (AttributeInstance attributeInstance : collection) {
            this.attributes.add(new AttributeSnapshot(attributeInstance.getAttribute(), attributeInstance.getBaseValue(), attributeInstance.getModifiers()));
        }
    }

    public ClientboundUpdateAttributesPacket(FriendlyByteBuf friendlyByteBuf) {
        this.entityId = friendlyByteBuf.readVarInt();
        this.attributes = friendlyByteBuf.readList(friendlyByteBuf2 -> {
            ResourceLocation resourceLocation = friendlyByteBuf2.readResourceLocation();
            Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(resourceLocation);
            double d = friendlyByteBuf2.readDouble();
            List<AttributeModifier> list = friendlyByteBuf2.readList(friendlyByteBuf -> new AttributeModifier(friendlyByteBuf.readUUID(), "Unknown synced attribute modifier", friendlyByteBuf.readDouble(), AttributeModifier.Operation.fromValue(friendlyByteBuf.readByte())));
            return new AttributeSnapshot(attribute, d, list);
        });
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.entityId);
        friendlyByteBuf.writeCollection(this.attributes, (friendlyByteBuf2, attributeSnapshot) -> {
            friendlyByteBuf2.writeResourceLocation(BuiltInRegistries.ATTRIBUTE.getKey(attributeSnapshot.getAttribute()));
            friendlyByteBuf2.writeDouble(attributeSnapshot.getBase());
            friendlyByteBuf2.writeCollection(attributeSnapshot.getModifiers(), (friendlyByteBuf, attributeModifier) -> {
                friendlyByteBuf.writeUUID(attributeModifier.getId());
                friendlyByteBuf.writeDouble(attributeModifier.getAmount());
                friendlyByteBuf.writeByte(attributeModifier.getOperation().toValue());
            });
        });
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleUpdateAttributes(this);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public List<AttributeSnapshot> getValues() {
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


package net.minecraft.network.protocol;

import net.minecraft.resources.ResourceLocation;

public record PacketType<T extends Packet<?>>(PacketFlow flow, ResourceLocation id) {
	public String toString() {
		return this.flow.id() + "/" + this.id;
	}
}

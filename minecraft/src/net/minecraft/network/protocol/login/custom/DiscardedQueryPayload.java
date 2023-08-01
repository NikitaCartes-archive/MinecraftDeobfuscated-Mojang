package net.minecraft.network.protocol.login.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record DiscardedQueryPayload(ResourceLocation id) implements CustomQueryPayload {
	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
	}
}

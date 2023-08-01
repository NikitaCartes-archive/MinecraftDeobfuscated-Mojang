package net.minecraft.network.protocol.login.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface CustomQueryPayload {
	ResourceLocation id();

	void write(FriendlyByteBuf friendlyByteBuf);
}

package net.minecraft.network.protocol.login.custom;

import net.minecraft.network.FriendlyByteBuf;

public interface CustomQueryAnswerPayload {
	void write(FriendlyByteBuf friendlyByteBuf);
}

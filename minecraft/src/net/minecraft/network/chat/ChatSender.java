package net.minecraft.network.chat;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record ChatSender(UUID profileId, @Nullable ProfilePublicKey profilePublicKey) {
	public static final ChatSender SYSTEM = new ChatSender(Util.NIL_UUID, null);

	public boolean isSystem() {
		return SYSTEM.equals(this);
	}
}

package net.minecraft.client.gui.screens.voting;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface VoteListenerScreen {
	void onVotesChanged();
}

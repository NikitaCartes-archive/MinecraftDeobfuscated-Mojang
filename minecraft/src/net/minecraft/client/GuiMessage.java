package net.minecraft.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;

@Environment(EnvType.CLIENT)
public class GuiMessage {
	private final int addedTime;
	private final FormattedText message;
	private final int id;

	public GuiMessage(int i, FormattedText formattedText, int j) {
		this.message = formattedText;
		this.addedTime = i;
		this.id = j;
	}

	public FormattedText getMessage() {
		return this.message;
	}

	public int getAddedTime() {
		return this.addedTime;
	}

	public int getId() {
		return this.id;
	}
}

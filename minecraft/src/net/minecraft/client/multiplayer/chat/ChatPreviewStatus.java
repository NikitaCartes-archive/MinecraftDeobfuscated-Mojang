package net.minecraft.client.multiplayer.chat;

import java.util.Arrays;
import java.util.Comparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import net.minecraft.util.OptionEnum;

@Environment(EnvType.CLIENT)
public enum ChatPreviewStatus implements OptionEnum {
	OFF(0, "options.off"),
	LIVE(1, "options.chatPreview.live"),
	CONFIRM(2, "options.chatPreview.confirm");

	private static final ChatPreviewStatus[] BY_ID = (ChatPreviewStatus[])Arrays.stream(values())
		.sorted(Comparator.comparingInt(ChatPreviewStatus::getId))
		.toArray(ChatPreviewStatus[]::new);
	private final int id;
	private final String key;

	private ChatPreviewStatus(int j, String string2) {
		this.id = j;
		this.key = string2;
	}

	@Override
	public String getKey() {
		return this.key;
	}

	@Override
	public int getId() {
		return this.id;
	}

	public static ChatPreviewStatus byId(int i) {
		return BY_ID[Mth.positiveModulo(i, BY_ID.length)];
	}
}

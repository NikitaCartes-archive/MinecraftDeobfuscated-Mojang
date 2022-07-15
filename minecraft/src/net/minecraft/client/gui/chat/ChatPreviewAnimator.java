package net.minecraft.client.gui.chat;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ChatPreviewAnimator {
	private static final long FADE_DURATION = 200L;
	@Nullable
	private Component residualPreview;
	private long fadeTime;
	private long lastTime;

	public void reset(long l) {
		this.residualPreview = null;
		this.fadeTime = 0L;
		this.lastTime = l;
	}

	public ChatPreviewAnimator.State get(long l, @Nullable Component component) {
		long m = l - this.lastTime;
		this.lastTime = l;
		return component != null ? this.getEnabled(m, component) : this.getDisabled(m);
	}

	private ChatPreviewAnimator.State getEnabled(long l, Component component) {
		this.residualPreview = component;
		if (this.fadeTime < 200L) {
			this.fadeTime = Math.min(this.fadeTime + l, 200L);
		}

		return new ChatPreviewAnimator.State(component, alpha(this.fadeTime));
	}

	private ChatPreviewAnimator.State getDisabled(long l) {
		if (this.fadeTime > 0L) {
			this.fadeTime = Math.max(this.fadeTime - l, 0L);
		}

		return this.fadeTime > 0L ? new ChatPreviewAnimator.State(this.residualPreview, alpha(this.fadeTime)) : ChatPreviewAnimator.State.DISABLED;
	}

	private static float alpha(long l) {
		return (float)l / 200.0F;
	}

	@Environment(EnvType.CLIENT)
	public static record State(@Nullable Component preview, float alpha) {
		public static final ChatPreviewAnimator.State DISABLED = new ChatPreviewAnimator.State(null, 0.0F);
	}
}

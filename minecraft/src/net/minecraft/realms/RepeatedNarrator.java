package net.minecraft.realms;

import com.google.common.util.concurrent.RateLimiter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;

@Environment(EnvType.CLIENT)
class RepeatedNarrator {
	final Duration repeatDelay;
	private final float permitsPerSecond;
	final AtomicReference<RepeatedNarrator.Params> params;

	public RepeatedNarrator(Duration duration) {
		this.repeatDelay = duration;
		this.params = new AtomicReference();
		float f = (float)duration.toMillis() / 1000.0F;
		this.permitsPerSecond = 1.0F / f;
	}

	public void narrate(String string) {
		RepeatedNarrator.Params params = (RepeatedNarrator.Params)this.params
			.updateAndGet(
				paramsx -> paramsx != null && string.equals(paramsx.narration)
						? paramsx
						: new RepeatedNarrator.Params(string, RateLimiter.create((double)this.permitsPerSecond))
			);
		if (params.rateLimiter.tryAcquire(1)) {
			NarratorChatListener narratorChatListener = NarratorChatListener.INSTANCE;
			narratorChatListener.handle(ChatType.SYSTEM, new TextComponent(string));
		}
	}

	@Environment(EnvType.CLIENT)
	static class Params {
		String narration;
		RateLimiter rateLimiter;

		Params(String string, RateLimiter rateLimiter) {
			this.narration = string;
			this.rateLimiter = rateLimiter;
		}
	}
}

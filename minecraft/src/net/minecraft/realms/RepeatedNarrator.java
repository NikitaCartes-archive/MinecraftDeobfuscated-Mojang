package net.minecraft.realms;

import com.google.common.util.concurrent.RateLimiter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;

@Environment(EnvType.CLIENT)
public class RepeatedNarrator {
	private final float permitsPerSecond;
	private final AtomicReference<RepeatedNarrator.Params> params = new AtomicReference();

	public RepeatedNarrator(Duration duration) {
		this.permitsPerSecond = 1000.0F / (float)duration.toMillis();
	}

	public void narrate(String string) {
		RepeatedNarrator.Params params = (RepeatedNarrator.Params)this.params
			.updateAndGet(
				paramsx -> paramsx != null && string.equals(paramsx.narration)
						? paramsx
						: new RepeatedNarrator.Params(string, RateLimiter.create((double)this.permitsPerSecond))
			);
		if (params.rateLimiter.tryAcquire(1)) {
			NarratorChatListener.INSTANCE.handle(ChatType.SYSTEM, new TextComponent(string), Util.NIL_UUID);
		}
	}

	@Environment(EnvType.CLIENT)
	static class Params {
		final String narration;
		final RateLimiter rateLimiter;

		Params(String string, RateLimiter rateLimiter) {
			this.narration = string;
			this.rateLimiter = rateLimiter;
		}
	}
}

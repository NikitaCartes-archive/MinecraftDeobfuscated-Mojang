package net.minecraft.realms;

import com.google.common.util.concurrent.RateLimiter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class RepeatedNarrator {
	private final float permitsPerSecond;
	private final AtomicReference<RepeatedNarrator.Params> params = new AtomicReference();

	public RepeatedNarrator(Duration duration) {
		this.permitsPerSecond = 1000.0F / (float)duration.toMillis();
	}

	public void narrate(GameNarrator gameNarrator, Component component) {
		RepeatedNarrator.Params params = (RepeatedNarrator.Params)this.params
			.updateAndGet(
				paramsx -> paramsx != null && component.equals(paramsx.narration)
						? paramsx
						: new RepeatedNarrator.Params(component, RateLimiter.create((double)this.permitsPerSecond))
			);
		if (params.rateLimiter.tryAcquire(1)) {
			gameNarrator.sayNow(component);
		}
	}

	@Environment(EnvType.CLIENT)
	static class Params {
		final Component narration;
		final RateLimiter rateLimiter;

		Params(Component component, RateLimiter rateLimiter) {
			this.narration = component;
			this.rateLimiter = rateLimiter;
		}
	}
}

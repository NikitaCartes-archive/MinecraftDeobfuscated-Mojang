package net.minecraft.gametest.framework;

import java.util.Iterator;
import java.util.List;

public class GameTestSequence {
	private final GameTestInfo parent;
	private final List<GameTestEvent> events;
	private long lastTick;

	public void tickAndContinue(long l) {
		try {
			this.tick(l);
		} catch (Exception var4) {
		}
	}

	public void tickAndFailIfNotComplete(long l) {
		try {
			this.tick(l);
		} catch (Exception var4) {
			this.parent.fail(var4);
		}
	}

	private void tick(long l) {
		Iterator<GameTestEvent> iterator = this.events.iterator();

		while (iterator.hasNext()) {
			GameTestEvent gameTestEvent = (GameTestEvent)iterator.next();
			gameTestEvent.assertion.run();
			iterator.remove();
			long m = l - this.lastTick;
			long n = this.lastTick;
			this.lastTick = l;
			if (gameTestEvent.expectedDelay != null && gameTestEvent.expectedDelay != m) {
				this.parent.fail(new GameTestAssertException("Succeeded in invalid tick: expected " + (n + gameTestEvent.expectedDelay) + ", but current tick is " + l));
				break;
			}
		}
	}
}

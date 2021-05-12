package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class GameTestSequence {
	final GameTestInfo parent;
	private final List<GameTestEvent> events = Lists.<GameTestEvent>newArrayList();
	private long lastTick;

	GameTestSequence(GameTestInfo gameTestInfo) {
		this.parent = gameTestInfo;
		this.lastTick = gameTestInfo.getTick();
	}

	public GameTestSequence thenWaitUntil(Runnable runnable) {
		this.events.add(GameTestEvent.create(runnable));
		return this;
	}

	public GameTestSequence thenWaitUntil(long l, Runnable runnable) {
		this.events.add(GameTestEvent.create(l, runnable));
		return this;
	}

	public GameTestSequence thenIdle(int i) {
		return this.thenExecuteAfter(i, () -> {
		});
	}

	public GameTestSequence thenExecute(Runnable runnable) {
		this.events.add(GameTestEvent.create(() -> this.executeWithoutFail(runnable)));
		return this;
	}

	public GameTestSequence thenExecuteAfter(int i, Runnable runnable) {
		this.events.add(GameTestEvent.create(() -> {
			if (this.parent.getTick() < this.lastTick + (long)i) {
				throw new GameTestAssertException("Waiting");
			} else {
				this.executeWithoutFail(runnable);
			}
		}));
		return this;
	}

	public GameTestSequence thenExecuteFor(int i, Runnable runnable) {
		this.events.add(GameTestEvent.create(() -> {
			if (this.parent.getTick() < this.lastTick + (long)i) {
				this.executeWithoutFail(runnable);
				throw new GameTestAssertException("Waiting");
			}
		}));
		return this;
	}

	public void thenSucceed() {
		this.events.add(GameTestEvent.create(this.parent::succeed));
	}

	public void thenFail(Supplier<Exception> supplier) {
		this.events.add(GameTestEvent.create(() -> this.parent.fail((Throwable)supplier.get())));
	}

	public GameTestSequence.Condition thenTrigger() {
		GameTestSequence.Condition condition = new GameTestSequence.Condition();
		this.events.add(GameTestEvent.create(() -> condition.trigger(this.parent.getTick())));
		return condition;
	}

	public void tickAndContinue(long l) {
		try {
			this.tick(l);
		} catch (GameTestAssertException var4) {
		}
	}

	public void tickAndFailIfNotComplete(long l) {
		try {
			this.tick(l);
		} catch (GameTestAssertException var4) {
			this.parent.fail(var4);
		}
	}

	private void executeWithoutFail(Runnable runnable) {
		try {
			runnable.run();
		} catch (GameTestAssertException var3) {
			this.parent.fail(var3);
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

	public class Condition {
		private static final long NOT_TRIGGERED = -1L;
		private long triggerTime = -1L;

		void trigger(long l) {
			if (this.triggerTime != -1L) {
				throw new IllegalStateException("Condition already triggered at " + this.triggerTime);
			} else {
				this.triggerTime = l;
			}
		}

		public void assertTriggeredThisTick() {
			long l = GameTestSequence.this.parent.getTick();
			if (this.triggerTime != l) {
				if (this.triggerTime == -1L) {
					throw new GameTestAssertException("Condition not triggered (t=" + l + ")");
				} else {
					throw new GameTestAssertException("Condition triggered at " + this.triggerTime + ", (t=" + l + ")");
				}
			}
		}
	}
}

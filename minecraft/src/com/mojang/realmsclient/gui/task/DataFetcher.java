package com.mojang.realmsclient.gui.task;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.TimeSource;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class DataFetcher {
	static final Logger LOGGER = LogUtils.getLogger();
	final Executor executor;
	final TimeUnit resolution;
	final TimeSource timeSource;

	public DataFetcher(Executor executor, TimeUnit timeUnit, TimeSource timeSource) {
		this.executor = executor;
		this.resolution = timeUnit;
		this.timeSource = timeSource;
	}

	public <T> DataFetcher.Task<T> createTask(String string, Callable<T> callable, Duration duration, RepeatedDelayStrategy repeatedDelayStrategy) {
		long l = this.resolution.convert(duration);
		if (l == 0L) {
			throw new IllegalArgumentException("Period of " + duration + " too short for selected resolution of " + this.resolution);
		} else {
			return new DataFetcher.Task<>(string, callable, l, repeatedDelayStrategy);
		}
	}

	public DataFetcher.Subscription createSubscription() {
		return new DataFetcher.Subscription();
	}

	@Environment(EnvType.CLIENT)
	static record ComputationResult<T>(Either<T, Exception> value, long time) {
	}

	@Environment(EnvType.CLIENT)
	class SubscribedTask<T> {
		private final DataFetcher.Task<T> task;
		private final Consumer<T> output;
		private long lastCheckTime = -1L;

		SubscribedTask(final DataFetcher.Task<T> task, final Consumer<T> consumer) {
			this.task = task;
			this.output = consumer;
		}

		void update(long l) {
			this.task.updateIfNeeded(l);
			this.runCallbackIfNeeded();
		}

		void runCallbackIfNeeded() {
			DataFetcher.SuccessfulComputationResult<T> successfulComputationResult = this.task.lastResult;
			if (successfulComputationResult != null && this.lastCheckTime < successfulComputationResult.time) {
				this.output.accept(successfulComputationResult.value);
				this.lastCheckTime = successfulComputationResult.time;
			}
		}

		void runCallback() {
			DataFetcher.SuccessfulComputationResult<T> successfulComputationResult = this.task.lastResult;
			if (successfulComputationResult != null) {
				this.output.accept(successfulComputationResult.value);
				this.lastCheckTime = successfulComputationResult.time;
			}
		}

		void reset() {
			this.task.reset();
			this.lastCheckTime = -1L;
		}
	}

	@Environment(EnvType.CLIENT)
	public class Subscription {
		private final List<DataFetcher.SubscribedTask<?>> subscriptions = new ArrayList();

		public <T> void subscribe(DataFetcher.Task<T> task, Consumer<T> consumer) {
			DataFetcher.SubscribedTask<T> subscribedTask = DataFetcher.this.new SubscribedTask<>(task, consumer);
			this.subscriptions.add(subscribedTask);
			subscribedTask.runCallbackIfNeeded();
		}

		public void forceUpdate() {
			for (DataFetcher.SubscribedTask<?> subscribedTask : this.subscriptions) {
				subscribedTask.runCallback();
			}
		}

		public void tick() {
			for (DataFetcher.SubscribedTask<?> subscribedTask : this.subscriptions) {
				subscribedTask.update(DataFetcher.this.timeSource.get(DataFetcher.this.resolution));
			}
		}

		public void reset() {
			for (DataFetcher.SubscribedTask<?> subscribedTask : this.subscriptions) {
				subscribedTask.reset();
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static record SuccessfulComputationResult<T>(T value, long time) {
	}

	@Environment(EnvType.CLIENT)
	public class Task<T> {
		private final String id;
		private final Callable<T> updater;
		private final long period;
		private final RepeatedDelayStrategy repeatStrategy;
		@Nullable
		private CompletableFuture<DataFetcher.ComputationResult<T>> pendingTask;
		@Nullable
		DataFetcher.SuccessfulComputationResult<T> lastResult;
		private long nextUpdate = -1L;

		Task(final String string, final Callable<T> callable, final long l, final RepeatedDelayStrategy repeatedDelayStrategy) {
			this.id = string;
			this.updater = callable;
			this.period = l;
			this.repeatStrategy = repeatedDelayStrategy;
		}

		void updateIfNeeded(long l) {
			if (this.pendingTask != null) {
				DataFetcher.ComputationResult<T> computationResult = (DataFetcher.ComputationResult<T>)this.pendingTask.getNow(null);
				if (computationResult == null) {
					return;
				}

				this.pendingTask = null;
				long m = computationResult.time;
				computationResult.value().ifLeft(object -> {
					this.lastResult = new DataFetcher.SuccessfulComputationResult<>((T)object, m);
					this.nextUpdate = m + this.period * this.repeatStrategy.delayCyclesAfterSuccess();
				}).ifRight(exception -> {
					long mx = this.repeatStrategy.delayCyclesAfterFailure();
					DataFetcher.LOGGER.warn("Failed to process task {}, will repeat after {} cycles", this.id, mx, exception);
					this.nextUpdate = m + this.period * mx;
				});
			}

			if (this.nextUpdate <= l) {
				this.pendingTask = CompletableFuture.supplyAsync(() -> {
					try {
						T object = (T)this.updater.call();
						long lx = DataFetcher.this.timeSource.get(DataFetcher.this.resolution);
						return new DataFetcher.ComputationResult<>(Either.left(object), lx);
					} catch (Exception var4x) {
						long lx = DataFetcher.this.timeSource.get(DataFetcher.this.resolution);
						return new DataFetcher.ComputationResult(Either.right(var4x), lx);
					}
				}, DataFetcher.this.executor);
			}
		}

		public void reset() {
			this.pendingTask = null;
			this.lastResult = null;
			this.nextUpdate = -1L;
		}
	}
}

package net.minecraft;

import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceImmutableList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SingleKeyCache;
import net.minecraft.util.TimeSource;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.block.state.properties.Property;
import org.slf4j.Logger;

public class Util {
	static final Logger LOGGER = LogUtils.getLogger();
	private static final int DEFAULT_MAX_THREADS = 255;
	private static final int DEFAULT_SAFE_FILE_OPERATION_RETRIES = 10;
	private static final String MAX_THREADS_SYSTEM_PROPERTY = "max.bg.threads";
	private static final TracingExecutor BACKGROUND_EXECUTOR = makeExecutor("Main");
	private static final TracingExecutor IO_POOL = makeIoExecutor("IO-Worker-", false);
	private static final TracingExecutor DOWNLOAD_POOL = makeIoExecutor("Download-", true);
	private static final DateTimeFormatter FILENAME_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);
	public static final int LINEAR_LOOKUP_THRESHOLD = 8;
	private static final Set<String> ALLOWED_UNTRUSTED_LINK_PROTOCOLS = Set.of("http", "https");
	public static final long NANOS_PER_MILLI = 1000000L;
	public static TimeSource.NanoTimeSource timeSource = System::nanoTime;
	public static final Ticker TICKER = new Ticker() {
		@Override
		public long read() {
			return Util.timeSource.getAsLong();
		}
	};
	public static final UUID NIL_UUID = new UUID(0L, 0L);
	public static final FileSystemProvider ZIP_FILE_SYSTEM_PROVIDER = (FileSystemProvider)FileSystemProvider.installedProviders()
		.stream()
		.filter(fileSystemProvider -> fileSystemProvider.getScheme().equalsIgnoreCase("jar"))
		.findFirst()
		.orElseThrow(() -> new IllegalStateException("No jar file system provider found"));
	private static Consumer<String> thePauser = string -> {
	};

	public static <K, V> Collector<Entry<? extends K, ? extends V>, ?, Map<K, V>> toMap() {
		return Collectors.toMap(Entry::getKey, Entry::getValue);
	}

	public static <T> Collector<T, ?, List<T>> toMutableList() {
		return Collectors.toCollection(Lists::newArrayList);
	}

	public static <T extends Comparable<T>> String getPropertyName(Property<T> property, Object object) {
		return property.getName((T)object);
	}

	public static String makeDescriptionId(String string, @Nullable ResourceLocation resourceLocation) {
		return resourceLocation == null
			? string + ".unregistered_sadface"
			: string + "." + resourceLocation.getNamespace() + "." + resourceLocation.getPath().replace('/', '.');
	}

	public static long getMillis() {
		return getNanos() / 1000000L;
	}

	public static long getNanos() {
		return timeSource.getAsLong();
	}

	public static long getEpochMillis() {
		return Instant.now().toEpochMilli();
	}

	public static String getFilenameFormattedDateTime() {
		return FILENAME_DATE_TIME_FORMATTER.format(ZonedDateTime.now());
	}

	private static TracingExecutor makeExecutor(String string) {
		int i = Mth.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, getMaxThreads());
		ExecutorService executorService;
		if (i <= 0) {
			executorService = MoreExecutors.newDirectExecutorService();
		} else {
			AtomicInteger atomicInteger = new AtomicInteger(1);
			executorService = new ForkJoinPool(i, forkJoinPool -> {
				final String string2 = "Worker-" + string + "-" + atomicInteger.getAndIncrement();
				ForkJoinWorkerThread forkJoinWorkerThread = new ForkJoinWorkerThread(forkJoinPool) {
					protected void onStart() {
						TracyClient.setThreadName(string2, string.hashCode());
						super.onStart();
					}

					protected void onTermination(Throwable throwable) {
						if (throwable != null) {
							Util.LOGGER.warn("{} died", this.getName(), throwable);
						} else {
							Util.LOGGER.debug("{} shutdown", this.getName());
						}

						super.onTermination(throwable);
					}
				};
				forkJoinWorkerThread.setName(string2);
				return forkJoinWorkerThread;
			}, Util::onThreadException, true);
		}

		return new TracingExecutor(executorService);
	}

	private static int getMaxThreads() {
		String string = System.getProperty("max.bg.threads");
		if (string != null) {
			try {
				int i = Integer.parseInt(string);
				if (i >= 1 && i <= 255) {
					return i;
				}

				LOGGER.error("Wrong {} property value '{}'. Should be an integer value between 1 and {}.", "max.bg.threads", string, 255);
			} catch (NumberFormatException var2) {
				LOGGER.error("Could not parse {} property value '{}'. Should be an integer value between 1 and {}.", "max.bg.threads", string, 255);
			}
		}

		return 255;
	}

	public static TracingExecutor backgroundExecutor() {
		return BACKGROUND_EXECUTOR;
	}

	public static TracingExecutor ioPool() {
		return IO_POOL;
	}

	public static TracingExecutor nonCriticalIoPool() {
		return DOWNLOAD_POOL;
	}

	public static void shutdownExecutors() {
		BACKGROUND_EXECUTOR.shutdownAndAwait(3L, TimeUnit.SECONDS);
		IO_POOL.shutdownAndAwait(3L, TimeUnit.SECONDS);
	}

	private static TracingExecutor makeIoExecutor(String string, boolean bl) {
		AtomicInteger atomicInteger = new AtomicInteger(1);
		return new TracingExecutor(Executors.newCachedThreadPool(runnable -> {
			Thread thread = new Thread(runnable);
			String string2 = string + atomicInteger.getAndIncrement();
			TracyClient.setThreadName(string2, string.hashCode());
			thread.setName(string2);
			thread.setDaemon(bl);
			thread.setUncaughtExceptionHandler(Util::onThreadException);
			return thread;
		}));
	}

	public static void throwAsRuntime(Throwable throwable) {
		throw throwable instanceof RuntimeException ? (RuntimeException)throwable : new RuntimeException(throwable);
	}

	private static void onThreadException(Thread thread, Throwable throwable) {
		pauseInIde(throwable);
		if (throwable instanceof CompletionException) {
			throwable = throwable.getCause();
		}

		if (throwable instanceof ReportedException reportedException) {
			Bootstrap.realStdoutPrintln(reportedException.getReport().getFriendlyReport(ReportType.CRASH));
			System.exit(-1);
		}

		LOGGER.error(String.format(Locale.ROOT, "Caught exception in thread %s", thread), throwable);
	}

	@Nullable
	public static Type<?> fetchChoiceType(TypeReference typeReference, String string) {
		return !SharedConstants.CHECK_DATA_FIXER_SCHEMA ? null : doFetchChoiceType(typeReference, string);
	}

	@Nullable
	private static Type<?> doFetchChoiceType(TypeReference typeReference, String string) {
		Type<?> type = null;

		try {
			type = DataFixers.getDataFixer()
				.getSchema(DataFixUtils.makeKey(SharedConstants.getCurrentVersion().getDataVersion().getVersion()))
				.getChoiceType(typeReference, string);
		} catch (IllegalArgumentException var4) {
			LOGGER.error("No data fixer registered for {}", string);
			if (SharedConstants.IS_RUNNING_IN_IDE) {
				throw var4;
			}
		}

		return type;
	}

	public static void runNamed(Runnable runnable, String string) {
		if (SharedConstants.IS_RUNNING_IN_IDE) {
			Thread thread = Thread.currentThread();
			String string2 = thread.getName();
			thread.setName(string);

			try (Zone zone = TracyClient.beginZone(string, SharedConstants.IS_RUNNING_IN_IDE)) {
				runnable.run();
			} finally {
				thread.setName(string2);
			}
		} else {
			try (Zone zone2 = TracyClient.beginZone(string, SharedConstants.IS_RUNNING_IN_IDE)) {
				runnable.run();
			}
		}
	}

	public static <T> String getRegisteredName(Registry<T> registry, T object) {
		ResourceLocation resourceLocation = registry.getKey(object);
		return resourceLocation == null ? "[unregistered]" : resourceLocation.toString();
	}

	public static <T> Predicate<T> allOf() {
		return object -> true;
	}

	public static <T> Predicate<T> allOf(Predicate<? super T> predicate) {
		return (Predicate<T>)predicate;
	}

	public static <T> Predicate<T> allOf(Predicate<? super T> predicate, Predicate<? super T> predicate2) {
		return object -> predicate.test(object) && predicate2.test(object);
	}

	public static <T> Predicate<T> allOf(Predicate<? super T> predicate, Predicate<? super T> predicate2, Predicate<? super T> predicate3) {
		return object -> predicate.test(object) && predicate2.test(object) && predicate3.test(object);
	}

	public static <T> Predicate<T> allOf(
		Predicate<? super T> predicate, Predicate<? super T> predicate2, Predicate<? super T> predicate3, Predicate<? super T> predicate4
	) {
		return object -> predicate.test(object) && predicate2.test(object) && predicate3.test(object) && predicate4.test(object);
	}

	public static <T> Predicate<T> allOf(
		Predicate<? super T> predicate,
		Predicate<? super T> predicate2,
		Predicate<? super T> predicate3,
		Predicate<? super T> predicate4,
		Predicate<? super T> predicate5
	) {
		return object -> predicate.test(object) && predicate2.test(object) && predicate3.test(object) && predicate4.test(object) && predicate5.test(object);
	}

	@SafeVarargs
	public static <T> Predicate<T> allOf(Predicate<? super T>... predicates) {
		return object -> {
			for (Predicate<? super T> predicate : predicates) {
				if (!predicate.test(object)) {
					return false;
				}
			}

			return true;
		};
	}

	public static <T> Predicate<T> allOf(List<? extends Predicate<? super T>> list) {
		return switch (list.size()) {
			case 0 -> allOf();
			case 1 -> allOf((Predicate<? super T>)list.get(0));
			case 2 -> allOf((Predicate<? super T>)list.get(0), (Predicate<? super T>)list.get(1));
			case 3 -> allOf((Predicate<? super T>)list.get(0), (Predicate<? super T>)list.get(1), (Predicate<? super T>)list.get(2));
			case 4 -> allOf((Predicate<? super T>)list.get(0), (Predicate<? super T>)list.get(1), (Predicate<? super T>)list.get(2), (Predicate<? super T>)list.get(3));
			case 5 -> allOf(
			(Predicate<? super T>)list.get(0),
			(Predicate<? super T>)list.get(1),
			(Predicate<? super T>)list.get(2),
			(Predicate<? super T>)list.get(3),
			(Predicate<? super T>)list.get(4)
		);
			default -> {
				Predicate<? super T>[] predicates = (Predicate<? super T>[])list.toArray(Predicate[]::new);
				yield allOf(predicates);
			}
		};
	}

	public static <T> Predicate<T> anyOf() {
		return object -> false;
	}

	public static <T> Predicate<T> anyOf(Predicate<? super T> predicate) {
		return (Predicate<T>)predicate;
	}

	public static <T> Predicate<T> anyOf(Predicate<? super T> predicate, Predicate<? super T> predicate2) {
		return object -> predicate.test(object) || predicate2.test(object);
	}

	public static <T> Predicate<T> anyOf(Predicate<? super T> predicate, Predicate<? super T> predicate2, Predicate<? super T> predicate3) {
		return object -> predicate.test(object) || predicate2.test(object) || predicate3.test(object);
	}

	public static <T> Predicate<T> anyOf(
		Predicate<? super T> predicate, Predicate<? super T> predicate2, Predicate<? super T> predicate3, Predicate<? super T> predicate4
	) {
		return object -> predicate.test(object) || predicate2.test(object) || predicate3.test(object) || predicate4.test(object);
	}

	public static <T> Predicate<T> anyOf(
		Predicate<? super T> predicate,
		Predicate<? super T> predicate2,
		Predicate<? super T> predicate3,
		Predicate<? super T> predicate4,
		Predicate<? super T> predicate5
	) {
		return object -> predicate.test(object) || predicate2.test(object) || predicate3.test(object) || predicate4.test(object) || predicate5.test(object);
	}

	@SafeVarargs
	public static <T> Predicate<T> anyOf(Predicate<? super T>... predicates) {
		return object -> {
			for (Predicate<? super T> predicate : predicates) {
				if (predicate.test(object)) {
					return true;
				}
			}

			return false;
		};
	}

	public static <T> Predicate<T> anyOf(List<? extends Predicate<? super T>> list) {
		return switch (list.size()) {
			case 0 -> anyOf();
			case 1 -> anyOf((Predicate<? super T>)list.get(0));
			case 2 -> anyOf((Predicate<? super T>)list.get(0), (Predicate<? super T>)list.get(1));
			case 3 -> anyOf((Predicate<? super T>)list.get(0), (Predicate<? super T>)list.get(1), (Predicate<? super T>)list.get(2));
			case 4 -> anyOf((Predicate<? super T>)list.get(0), (Predicate<? super T>)list.get(1), (Predicate<? super T>)list.get(2), (Predicate<? super T>)list.get(3));
			case 5 -> anyOf(
			(Predicate<? super T>)list.get(0),
			(Predicate<? super T>)list.get(1),
			(Predicate<? super T>)list.get(2),
			(Predicate<? super T>)list.get(3),
			(Predicate<? super T>)list.get(4)
		);
			default -> {
				Predicate<? super T>[] predicates = (Predicate<? super T>[])list.toArray(Predicate[]::new);
				yield anyOf(predicates);
			}
		};
	}

	public static <T> boolean isSymmetrical(int i, int j, List<T> list) {
		if (i == 1) {
			return true;
		} else {
			int k = i / 2;

			for (int l = 0; l < j; l++) {
				for (int m = 0; m < k; m++) {
					int n = i - 1 - m;
					T object = (T)list.get(m + l * i);
					T object2 = (T)list.get(n + l * i);
					if (!object.equals(object2)) {
						return false;
					}
				}
			}

			return true;
		}
	}

	public static Util.OS getPlatform() {
		String string = System.getProperty("os.name").toLowerCase(Locale.ROOT);
		if (string.contains("win")) {
			return Util.OS.WINDOWS;
		} else if (string.contains("mac")) {
			return Util.OS.OSX;
		} else if (string.contains("solaris")) {
			return Util.OS.SOLARIS;
		} else if (string.contains("sunos")) {
			return Util.OS.SOLARIS;
		} else if (string.contains("linux")) {
			return Util.OS.LINUX;
		} else {
			return string.contains("unix") ? Util.OS.LINUX : Util.OS.UNKNOWN;
		}
	}

	public static URI parseAndValidateUntrustedUri(String string) throws URISyntaxException {
		URI uRI = new URI(string);
		String string2 = uRI.getScheme();
		if (string2 == null) {
			throw new URISyntaxException(string, "Missing protocol in URI: " + string);
		} else {
			String string3 = string2.toLowerCase(Locale.ROOT);
			if (!ALLOWED_UNTRUSTED_LINK_PROTOCOLS.contains(string3)) {
				throw new URISyntaxException(string, "Unsupported protocol in URI: " + string);
			} else {
				return uRI;
			}
		}
	}

	public static Stream<String> getVmArguments() {
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		return runtimeMXBean.getInputArguments().stream().filter(string -> string.startsWith("-X"));
	}

	public static <T> T lastOf(List<T> list) {
		return (T)list.get(list.size() - 1);
	}

	public static <T> T findNextInIterable(Iterable<T> iterable, @Nullable T object) {
		Iterator<T> iterator = iterable.iterator();
		T object2 = (T)iterator.next();
		if (object != null) {
			T object3 = object2;

			while (object3 != object) {
				if (iterator.hasNext()) {
					object3 = (T)iterator.next();
				}
			}

			if (iterator.hasNext()) {
				return (T)iterator.next();
			}
		}

		return object2;
	}

	public static <T> T findPreviousInIterable(Iterable<T> iterable, @Nullable T object) {
		Iterator<T> iterator = iterable.iterator();
		T object2 = null;

		while (iterator.hasNext()) {
			T object3 = (T)iterator.next();
			if (object3 == object) {
				if (object2 == null) {
					object2 = iterator.hasNext() ? Iterators.getLast(iterator) : object;
				}
				break;
			}

			object2 = object3;
		}

		return object2;
	}

	public static <T> T make(Supplier<T> supplier) {
		return (T)supplier.get();
	}

	public static <T> T make(T object, Consumer<? super T> consumer) {
		consumer.accept(object);
		return object;
	}

	public static <K extends Enum<K>, V> EnumMap<K, V> makeEnumMap(Class<K> class_, Function<K, V> function) {
		EnumMap<K, V> enumMap = new EnumMap(class_);

		for (K enum_ : (Enum[])class_.getEnumConstants()) {
			enumMap.put(enum_, function.apply(enum_));
		}

		return enumMap;
	}

	public static <V> CompletableFuture<List<V>> sequence(List<? extends CompletableFuture<V>> list) {
		if (list.isEmpty()) {
			return CompletableFuture.completedFuture(List.of());
		} else if (list.size() == 1) {
			return ((CompletableFuture)list.get(0)).thenApply(List::of);
		} else {
			CompletableFuture<Void> completableFuture = CompletableFuture.allOf((CompletableFuture[])list.toArray(new CompletableFuture[0]));
			return completableFuture.thenApply(void_ -> list.stream().map(CompletableFuture::join).toList());
		}
	}

	public static <V> CompletableFuture<List<V>> sequenceFailFast(List<? extends CompletableFuture<? extends V>> list) {
		CompletableFuture<List<V>> completableFuture = new CompletableFuture();
		return fallibleSequence(list, completableFuture::completeExceptionally).applyToEither(completableFuture, Function.identity());
	}

	public static <V> CompletableFuture<List<V>> sequenceFailFastAndCancel(List<? extends CompletableFuture<? extends V>> list) {
		CompletableFuture<List<V>> completableFuture = new CompletableFuture();
		return fallibleSequence(list, throwable -> {
			if (completableFuture.completeExceptionally(throwable)) {
				for (CompletableFuture<? extends V> completableFuture2 : list) {
					completableFuture2.cancel(true);
				}
			}
		}).applyToEither(completableFuture, Function.identity());
	}

	private static <V> CompletableFuture<List<V>> fallibleSequence(List<? extends CompletableFuture<? extends V>> list, Consumer<Throwable> consumer) {
		List<V> list2 = Lists.<V>newArrayListWithCapacity(list.size());
		CompletableFuture<?>[] completableFutures = new CompletableFuture[list.size()];
		list.forEach(completableFuture -> {
			int i = list2.size();
			list2.add(null);
			completableFutures[i] = completableFuture.whenComplete((object, throwable) -> {
				if (throwable != null) {
					consumer.accept(throwable);
				} else {
					list2.set(i, object);
				}
			});
		});
		return CompletableFuture.allOf(completableFutures).thenApply(void_ -> list2);
	}

	public static <T> Optional<T> ifElse(Optional<T> optional, Consumer<T> consumer, Runnable runnable) {
		if (optional.isPresent()) {
			consumer.accept(optional.get());
		} else {
			runnable.run();
		}

		return optional;
	}

	public static <T> Supplier<T> name(Supplier<T> supplier, Supplier<String> supplier2) {
		return supplier;
	}

	public static Runnable name(Runnable runnable, Supplier<String> supplier) {
		return runnable;
	}

	public static void logAndPauseIfInIde(String string) {
		LOGGER.error(string);
		if (SharedConstants.IS_RUNNING_IN_IDE) {
			doPause(string);
		}
	}

	public static void logAndPauseIfInIde(String string, Throwable throwable) {
		LOGGER.error(string, throwable);
		if (SharedConstants.IS_RUNNING_IN_IDE) {
			doPause(string);
		}
	}

	public static <T extends Throwable> T pauseInIde(T throwable) {
		if (SharedConstants.IS_RUNNING_IN_IDE) {
			LOGGER.error("Trying to throw a fatal exception, pausing in IDE", throwable);
			doPause(throwable.getMessage());
		}

		return throwable;
	}

	public static void setPause(Consumer<String> consumer) {
		thePauser = consumer;
	}

	private static void doPause(String string) {
		Instant instant = Instant.now();
		LOGGER.warn("Did you remember to set a breakpoint here?");
		boolean bl = Duration.between(instant, Instant.now()).toMillis() > 500L;
		if (!bl) {
			thePauser.accept(string);
		}
	}

	public static String describeError(Throwable throwable) {
		if (throwable.getCause() != null) {
			return describeError(throwable.getCause());
		} else {
			return throwable.getMessage() != null ? throwable.getMessage() : throwable.toString();
		}
	}

	public static <T> T getRandom(T[] objects, RandomSource randomSource) {
		return objects[randomSource.nextInt(objects.length)];
	}

	public static int getRandom(int[] is, RandomSource randomSource) {
		return is[randomSource.nextInt(is.length)];
	}

	public static <T> T getRandom(List<T> list, RandomSource randomSource) {
		return (T)list.get(randomSource.nextInt(list.size()));
	}

	public static <T> Optional<T> getRandomSafe(List<T> list, RandomSource randomSource) {
		return list.isEmpty() ? Optional.empty() : Optional.of(getRandom(list, randomSource));
	}

	private static BooleanSupplier createRenamer(Path path, Path path2) {
		return new BooleanSupplier() {
			public boolean getAsBoolean() {
				try {
					Files.move(path, path2);
					return true;
				} catch (IOException var2) {
					Util.LOGGER.error("Failed to rename", (Throwable)var2);
					return false;
				}
			}

			public String toString() {
				return "rename " + path + " to " + path2;
			}
		};
	}

	private static BooleanSupplier createDeleter(Path path) {
		return new BooleanSupplier() {
			public boolean getAsBoolean() {
				try {
					Files.deleteIfExists(path);
					return true;
				} catch (IOException var2) {
					Util.LOGGER.warn("Failed to delete", (Throwable)var2);
					return false;
				}
			}

			public String toString() {
				return "delete old " + path;
			}
		};
	}

	private static BooleanSupplier createFileDeletedCheck(Path path) {
		return new BooleanSupplier() {
			public boolean getAsBoolean() {
				return !Files.exists(path, new LinkOption[0]);
			}

			public String toString() {
				return "verify that " + path + " is deleted";
			}
		};
	}

	private static BooleanSupplier createFileCreatedCheck(Path path) {
		return new BooleanSupplier() {
			public boolean getAsBoolean() {
				return Files.isRegularFile(path, new LinkOption[0]);
			}

			public String toString() {
				return "verify that " + path + " is present";
			}
		};
	}

	private static boolean executeInSequence(BooleanSupplier... booleanSuppliers) {
		for (BooleanSupplier booleanSupplier : booleanSuppliers) {
			if (!booleanSupplier.getAsBoolean()) {
				LOGGER.warn("Failed to execute {}", booleanSupplier);
				return false;
			}
		}

		return true;
	}

	private static boolean runWithRetries(int i, String string, BooleanSupplier... booleanSuppliers) {
		for (int j = 0; j < i; j++) {
			if (executeInSequence(booleanSuppliers)) {
				return true;
			}

			LOGGER.error("Failed to {}, retrying {}/{}", string, j, i);
		}

		LOGGER.error("Failed to {}, aborting, progress might be lost", string);
		return false;
	}

	public static void safeReplaceFile(Path path, Path path2, Path path3) {
		safeReplaceOrMoveFile(path, path2, path3, false);
	}

	public static boolean safeReplaceOrMoveFile(Path path, Path path2, Path path3, boolean bl) {
		if (Files.exists(path, new LinkOption[0])
			&& !runWithRetries(10, "create backup " + path3, createDeleter(path3), createRenamer(path, path3), createFileCreatedCheck(path3))) {
			return false;
		} else if (!runWithRetries(10, "remove old " + path, createDeleter(path), createFileDeletedCheck(path))) {
			return false;
		} else if (!runWithRetries(10, "replace " + path + " with " + path2, createRenamer(path2, path), createFileCreatedCheck(path)) && !bl) {
			runWithRetries(10, "restore " + path + " from " + path3, createRenamer(path3, path), createFileCreatedCheck(path));
			return false;
		} else {
			return true;
		}
	}

	public static int offsetByCodepoints(String string, int i, int j) {
		int k = string.length();
		if (j >= 0) {
			for (int l = 0; i < k && l < j; l++) {
				if (Character.isHighSurrogate(string.charAt(i++)) && i < k && Character.isLowSurrogate(string.charAt(i))) {
					i++;
				}
			}
		} else {
			for (int lx = j; i > 0 && lx < 0; lx++) {
				i--;
				if (Character.isLowSurrogate(string.charAt(i)) && i > 0 && Character.isHighSurrogate(string.charAt(i - 1))) {
					i--;
				}
			}
		}

		return i;
	}

	public static Consumer<String> prefix(String string, Consumer<String> consumer) {
		return string2 -> consumer.accept(string + string2);
	}

	public static DataResult<int[]> fixedSize(IntStream intStream, int i) {
		int[] is = intStream.limit((long)(i + 1)).toArray();
		if (is.length != i) {
			Supplier<String> supplier = () -> "Input is not a list of " + i + " ints";
			return is.length >= i ? DataResult.error(supplier, Arrays.copyOf(is, i)) : DataResult.error(supplier);
		} else {
			return DataResult.success(is);
		}
	}

	public static DataResult<long[]> fixedSize(LongStream longStream, int i) {
		long[] ls = longStream.limit((long)(i + 1)).toArray();
		if (ls.length != i) {
			Supplier<String> supplier = () -> "Input is not a list of " + i + " longs";
			return ls.length >= i ? DataResult.error(supplier, Arrays.copyOf(ls, i)) : DataResult.error(supplier);
		} else {
			return DataResult.success(ls);
		}
	}

	public static <T> DataResult<List<T>> fixedSize(List<T> list, int i) {
		if (list.size() != i) {
			Supplier<String> supplier = () -> "Input is not a list of " + i + " elements";
			return list.size() >= i ? DataResult.error(supplier, list.subList(0, i)) : DataResult.error(supplier);
		} else {
			return DataResult.success(list);
		}
	}

	public static void startTimerHackThread() {
		Thread thread = new Thread("Timer hack thread") {
			public void run() {
				while (true) {
					try {
						Thread.sleep(2147483647L);
					} catch (InterruptedException var2) {
						Util.LOGGER.warn("Timer hack thread interrupted, that really should not happen");
						return;
					}
				}
			}
		};
		thread.setDaemon(true);
		thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
		thread.start();
	}

	public static void copyBetweenDirs(Path path, Path path2, Path path3) throws IOException {
		Path path4 = path.relativize(path3);
		Path path5 = path2.resolve(path4);
		Files.copy(path3, path5);
	}

	public static String sanitizeName(String string, CharPredicate charPredicate) {
		return (String)string.toLowerCase(Locale.ROOT)
			.chars()
			.mapToObj(i -> charPredicate.test((char)i) ? Character.toString((char)i) : "_")
			.collect(Collectors.joining());
	}

	public static <K, V> SingleKeyCache<K, V> singleKeyCache(Function<K, V> function) {
		return new SingleKeyCache<>(function);
	}

	public static <T, R> Function<T, R> memoize(Function<T, R> function) {
		return new Function<T, R>() {
			private final Map<T, R> cache = new ConcurrentHashMap();

			public R apply(T object) {
				return (R)this.cache.computeIfAbsent(object, function);
			}

			public String toString() {
				return "memoize/1[function=" + function + ", size=" + this.cache.size() + "]";
			}
		};
	}

	public static <T, U, R> BiFunction<T, U, R> memoize(BiFunction<T, U, R> biFunction) {
		return new BiFunction<T, U, R>() {
			private final Map<Pair<T, U>, R> cache = new ConcurrentHashMap();

			public R apply(T object, U object2) {
				return (R)this.cache.computeIfAbsent(Pair.of(object, object2), pair -> biFunction.apply(pair.getFirst(), pair.getSecond()));
			}

			public String toString() {
				return "memoize/2[function=" + biFunction + ", size=" + this.cache.size() + "]";
			}
		};
	}

	public static <T> List<T> toShuffledList(Stream<T> stream, RandomSource randomSource) {
		ObjectArrayList<T> objectArrayList = (ObjectArrayList<T>)stream.collect(ObjectArrayList.toList());
		shuffle(objectArrayList, randomSource);
		return objectArrayList;
	}

	public static IntArrayList toShuffledList(IntStream intStream, RandomSource randomSource) {
		IntArrayList intArrayList = IntArrayList.wrap(intStream.toArray());
		int i = intArrayList.size();

		for (int j = i; j > 1; j--) {
			int k = randomSource.nextInt(j);
			intArrayList.set(j - 1, intArrayList.set(k, intArrayList.getInt(j - 1)));
		}

		return intArrayList;
	}

	public static <T> List<T> shuffledCopy(T[] objects, RandomSource randomSource) {
		ObjectArrayList<T> objectArrayList = new ObjectArrayList<>(objects);
		shuffle(objectArrayList, randomSource);
		return objectArrayList;
	}

	public static <T> List<T> shuffledCopy(ObjectArrayList<T> objectArrayList, RandomSource randomSource) {
		ObjectArrayList<T> objectArrayList2 = new ObjectArrayList<>(objectArrayList);
		shuffle(objectArrayList2, randomSource);
		return objectArrayList2;
	}

	public static <T> void shuffle(List<T> list, RandomSource randomSource) {
		int i = list.size();

		for (int j = i; j > 1; j--) {
			int k = randomSource.nextInt(j);
			list.set(j - 1, list.set(k, list.get(j - 1)));
		}
	}

	public static <T> CompletableFuture<T> blockUntilDone(Function<Executor, CompletableFuture<T>> function) {
		return blockUntilDone(function, CompletableFuture::isDone);
	}

	public static <T> T blockUntilDone(Function<Executor, T> function, Predicate<T> predicate) {
		BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue();
		T object = (T)function.apply(blockingQueue::add);

		while (!predicate.test(object)) {
			try {
				Runnable runnable = (Runnable)blockingQueue.poll(100L, TimeUnit.MILLISECONDS);
				if (runnable != null) {
					runnable.run();
				}
			} catch (InterruptedException var5) {
				LOGGER.warn("Interrupted wait");
				break;
			}
		}

		int i = blockingQueue.size();
		if (i > 0) {
			LOGGER.warn("Tasks left in queue: {}", i);
		}

		return object;
	}

	public static <T> ToIntFunction<T> createIndexLookup(List<T> list) {
		int i = list.size();
		if (i < 8) {
			return list::indexOf;
		} else {
			Object2IntMap<T> object2IntMap = new Object2IntOpenHashMap<>(i);
			object2IntMap.defaultReturnValue(-1);

			for (int j = 0; j < i; j++) {
				object2IntMap.put((T)list.get(j), j);
			}

			return object2IntMap;
		}
	}

	public static <T> ToIntFunction<T> createIndexIdentityLookup(List<T> list) {
		int i = list.size();
		if (i < 8) {
			ReferenceList<T> referenceList = new ReferenceImmutableList<>(list);
			return referenceList::indexOf;
		} else {
			Reference2IntMap<T> reference2IntMap = new Reference2IntOpenHashMap<>(i);
			reference2IntMap.defaultReturnValue(-1);

			for (int j = 0; j < i; j++) {
				reference2IntMap.put((T)list.get(j), j);
			}

			return reference2IntMap;
		}
	}

	public static <A, B> Typed<B> writeAndReadTypedOrThrow(Typed<A> typed, Type<B> type, UnaryOperator<Dynamic<?>> unaryOperator) {
		Dynamic<?> dynamic = (Dynamic<?>)typed.write().getOrThrow();
		return readTypedOrThrow(type, (Dynamic<?>)unaryOperator.apply(dynamic), true);
	}

	public static <T> Typed<T> readTypedOrThrow(Type<T> type, Dynamic<?> dynamic) {
		return readTypedOrThrow(type, dynamic, false);
	}

	public static <T> Typed<T> readTypedOrThrow(Type<T> type, Dynamic<?> dynamic, boolean bl) {
		DataResult<Typed<T>> dataResult = type.readTyped(dynamic).map(Pair::getFirst);

		try {
			return bl ? dataResult.getPartialOrThrow(IllegalStateException::new) : dataResult.getOrThrow(IllegalStateException::new);
		} catch (IllegalStateException var7) {
			CrashReport crashReport = CrashReport.forThrowable(var7, "Reading type");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Info");
			crashReportCategory.setDetail("Data", dynamic);
			crashReportCategory.setDetail("Type", type);
			throw new ReportedException(crashReport);
		}
	}

	public static <T> List<T> copyAndAdd(List<T> list, T object) {
		return ImmutableList.<T>builderWithExpectedSize(list.size() + 1).addAll(list).add(object).build();
	}

	public static <T> List<T> copyAndAdd(T object, List<T> list) {
		return ImmutableList.<T>builderWithExpectedSize(list.size() + 1).add(object).addAll(list).build();
	}

	public static <K, V> Map<K, V> copyAndPut(Map<K, V> map, K object, V object2) {
		return ImmutableMap.<K, V>builderWithExpectedSize(map.size() + 1).putAll(map).put(object, object2).buildKeepingLast();
	}

	public static enum OS {
		LINUX("linux"),
		SOLARIS("solaris"),
		WINDOWS("windows") {
			@Override
			protected String[] getOpenUriArguments(URI uRI) {
				return new String[]{"rundll32", "url.dll,FileProtocolHandler", uRI.toString()};
			}
		},
		OSX("mac") {
			@Override
			protected String[] getOpenUriArguments(URI uRI) {
				return new String[]{"open", uRI.toString()};
			}
		},
		UNKNOWN("unknown");

		private final String telemetryName;

		OS(final String string2) {
			this.telemetryName = string2;
		}

		public void openUri(URI uRI) {
			try {
				Process process = (Process)AccessController.doPrivileged(() -> Runtime.getRuntime().exec(this.getOpenUriArguments(uRI)));
				process.getInputStream().close();
				process.getErrorStream().close();
				process.getOutputStream().close();
			} catch (IOException | PrivilegedActionException var3) {
				Util.LOGGER.error("Couldn't open location '{}'", uRI, var3);
			}
		}

		public void openFile(File file) {
			this.openUri(file.toURI());
		}

		public void openPath(Path path) {
			this.openUri(path.toUri());
		}

		protected String[] getOpenUriArguments(URI uRI) {
			String string = uRI.toString();
			if ("file".equals(uRI.getScheme())) {
				string = string.replace("file:", "file://");
			}

			return new String[]{"xdg-open", string};
		}

		public void openUri(String string) {
			try {
				this.openUri(new URI(string));
			} catch (IllegalArgumentException | URISyntaxException var3) {
				Util.LOGGER.error("Couldn't open uri '{}'", string, var3);
			}
		}

		public String telemetryName() {
			return this.telemetryName;
		}
	}
}

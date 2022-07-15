/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft;

import com.google.common.base.Ticker;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.CopyOption;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
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
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.CharPredicate;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeSource;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class Util {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_MAX_THREADS = 255;
    private static final String MAX_THREADS_SYSTEM_PROPERTY = "max.bg.threads";
    private static final AtomicInteger WORKER_COUNT = new AtomicInteger(1);
    private static final ExecutorService BOOTSTRAP_EXECUTOR = Util.makeExecutor("Bootstrap");
    private static final ExecutorService BACKGROUND_EXECUTOR = Util.makeExecutor("Main");
    private static final ExecutorService IO_POOL = Util.makeIoExecutor();
    private static final DateTimeFormatter FILENAME_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);
    public static TimeSource.NanoTimeSource timeSource = System::nanoTime;
    public static final Ticker TICKER = new Ticker(){

        @Override
        public long read() {
            return timeSource.getAsLong();
        }
    };
    public static final UUID NIL_UUID = new UUID(0L, 0L);
    public static final FileSystemProvider ZIP_FILE_SYSTEM_PROVIDER = FileSystemProvider.installedProviders().stream().filter(fileSystemProvider -> fileSystemProvider.getScheme().equalsIgnoreCase("jar")).findFirst().orElseThrow(() -> new IllegalStateException("No jar file system provider found"));
    private static Consumer<String> thePauser = string -> {};

    public static <K, V> Collector<Map.Entry<? extends K, ? extends V>, ?, Map<K, V>> toMap() {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    public static <T extends Comparable<T>> String getPropertyName(Property<T> property, Object object) {
        return property.getName((Comparable)object);
    }

    public static String makeDescriptionId(String string, @Nullable ResourceLocation resourceLocation) {
        if (resourceLocation == null) {
            return string + ".unregistered_sadface";
        }
        return string + "." + resourceLocation.getNamespace() + "." + resourceLocation.getPath().replace('/', '.');
    }

    public static long getMillis() {
        return Util.getNanos() / 1000000L;
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

    private static ExecutorService makeExecutor(String string) {
        int i = Mth.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, Util.getMaxThreads());
        ExecutorService executorService = i <= 0 ? MoreExecutors.newDirectExecutorService() : new ForkJoinPool(i, forkJoinPool -> {
            ForkJoinWorkerThread forkJoinWorkerThread = new ForkJoinWorkerThread(forkJoinPool){

                @Override
                protected void onTermination(Throwable throwable) {
                    if (throwable != null) {
                        LOGGER.warn("{} died", (Object)this.getName(), (Object)throwable);
                    } else {
                        LOGGER.debug("{} shutdown", (Object)this.getName());
                    }
                    super.onTermination(throwable);
                }
            };
            forkJoinWorkerThread.setName("Worker-" + string + "-" + WORKER_COUNT.getAndIncrement());
            return forkJoinWorkerThread;
        }, Util::onThreadException, true);
        return executorService;
    }

    private static int getMaxThreads() {
        String string = System.getProperty(MAX_THREADS_SYSTEM_PROPERTY);
        if (string != null) {
            try {
                int i = Integer.parseInt(string);
                if (i >= 1 && i <= 255) {
                    return i;
                }
                LOGGER.error("Wrong {} property value '{}'. Should be an integer value between 1 and {}.", MAX_THREADS_SYSTEM_PROPERTY, string, 255);
            } catch (NumberFormatException numberFormatException) {
                LOGGER.error("Could not parse {} property value '{}'. Should be an integer value between 1 and {}.", MAX_THREADS_SYSTEM_PROPERTY, string, 255);
            }
        }
        return 255;
    }

    public static ExecutorService bootstrapExecutor() {
        return BOOTSTRAP_EXECUTOR;
    }

    public static ExecutorService backgroundExecutor() {
        return BACKGROUND_EXECUTOR;
    }

    public static ExecutorService ioPool() {
        return IO_POOL;
    }

    public static void shutdownExecutors() {
        Util.shutdownExecutor(BACKGROUND_EXECUTOR);
        Util.shutdownExecutor(IO_POOL);
    }

    private static void shutdownExecutor(ExecutorService executorService) {
        boolean bl;
        executorService.shutdown();
        try {
            bl = executorService.awaitTermination(3L, TimeUnit.SECONDS);
        } catch (InterruptedException interruptedException) {
            bl = false;
        }
        if (!bl) {
            executorService.shutdownNow();
        }
    }

    private static ExecutorService makeIoExecutor() {
        return Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("IO-Worker-" + WORKER_COUNT.getAndIncrement());
            thread.setUncaughtExceptionHandler(Util::onThreadException);
            return thread;
        });
    }

    public static <T> CompletableFuture<T> failedFuture(Throwable throwable) {
        CompletableFuture completableFuture = new CompletableFuture();
        completableFuture.completeExceptionally(throwable);
        return completableFuture;
    }

    public static void throwAsRuntime(Throwable throwable) {
        throw throwable instanceof RuntimeException ? (RuntimeException)throwable : new RuntimeException(throwable);
    }

    private static void onThreadException(Thread thread, Throwable throwable) {
        Util.pauseInIde(throwable);
        if (throwable instanceof CompletionException) {
            throwable = throwable.getCause();
        }
        if (throwable instanceof ReportedException) {
            Bootstrap.realStdoutPrintln(((ReportedException)throwable).getReport().getFriendlyReport());
            System.exit(-1);
        }
        LOGGER.error(String.format(Locale.ROOT, "Caught exception in thread %s", thread), throwable);
    }

    @Nullable
    public static Type<?> fetchChoiceType(DSL.TypeReference typeReference, String string) {
        if (!SharedConstants.CHECK_DATA_FIXER_SCHEMA) {
            return null;
        }
        return Util.doFetchChoiceType(typeReference, string);
    }

    @Nullable
    private static Type<?> doFetchChoiceType(DSL.TypeReference typeReference, String string) {
        Type<?> type;
        block2: {
            type = null;
            try {
                type = DataFixers.getDataFixer().getSchema(DataFixUtils.makeKey(SharedConstants.getCurrentVersion().getWorldVersion())).getChoiceType(typeReference, string);
            } catch (IllegalArgumentException illegalArgumentException) {
                LOGGER.error("No data fixer registered for {}", (Object)string);
                if (!SharedConstants.IS_RUNNING_IN_IDE) break block2;
                throw illegalArgumentException;
            }
        }
        return type;
    }

    public static Runnable wrapThreadWithTaskName(String string, Runnable runnable) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            return () -> {
                Thread thread = Thread.currentThread();
                String string2 = thread.getName();
                thread.setName(string);
                try {
                    runnable.run();
                } finally {
                    thread.setName(string2);
                }
            };
        }
        return runnable;
    }

    public static <V> Supplier<V> wrapThreadWithTaskName(String string, Supplier<V> supplier) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            return () -> {
                Thread thread = Thread.currentThread();
                String string2 = thread.getName();
                thread.setName(string);
                try {
                    Object t = supplier.get();
                    return t;
                } finally {
                    thread.setName(string2);
                }
            };
        }
        return supplier;
    }

    public static OS getPlatform() {
        String string = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (string.contains("win")) {
            return OS.WINDOWS;
        }
        if (string.contains("mac")) {
            return OS.OSX;
        }
        if (string.contains("solaris")) {
            return OS.SOLARIS;
        }
        if (string.contains("sunos")) {
            return OS.SOLARIS;
        }
        if (string.contains("linux")) {
            return OS.LINUX;
        }
        if (string.contains("unix")) {
            return OS.LINUX;
        }
        return OS.UNKNOWN;
    }

    public static Stream<String> getVmArguments() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return runtimeMXBean.getInputArguments().stream().filter(string -> string.startsWith("-X"));
    }

    public static <T> T lastOf(List<T> list) {
        return list.get(list.size() - 1);
    }

    public static <T> T findNextInIterable(Iterable<T> iterable, @Nullable T object) {
        Iterator<T> iterator = iterable.iterator();
        T object2 = iterator.next();
        if (object != null) {
            T object3 = object2;
            while (true) {
                if (object3 == object) {
                    if (!iterator.hasNext()) break;
                    return iterator.next();
                }
                if (!iterator.hasNext()) continue;
                object3 = iterator.next();
            }
        }
        return object2;
    }

    public static <T> T findPreviousInIterable(Iterable<T> iterable, @Nullable T object) {
        Iterator<T> iterator = iterable.iterator();
        T object2 = null;
        while (iterator.hasNext()) {
            T object3 = iterator.next();
            if (object3 == object) {
                if (object2 != null) break;
                object2 = iterator.hasNext() ? Iterators.getLast(iterator) : object;
                break;
            }
            object2 = object3;
        }
        return object2;
    }

    public static <T> T make(Supplier<T> supplier) {
        return supplier.get();
    }

    public static <T> T make(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }

    @Nullable
    public static <T, R> R mapNullable(@Nullable T object, Function<T, R> function) {
        if (object == null) {
            return null;
        }
        return function.apply(object);
    }

    public static <T, R> R mapNullable(@Nullable T object, Function<T, R> function, R object2) {
        if (object == null) {
            return object2;
        }
        return function.apply(object);
    }

    public static <K> Hash.Strategy<K> identityStrategy() {
        return IdentityStrategy.INSTANCE;
    }

    public static <V> CompletableFuture<List<V>> sequence(List<? extends CompletableFuture<V>> list) {
        if (list.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }
        if (list.size() == 1) {
            return list.get(0).thenApply(List::of);
        }
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(list.toArray(new CompletableFuture[0]));
        return completableFuture.thenApply(void_ -> list.stream().map(CompletableFuture::join).toList());
    }

    public static <V> CompletableFuture<List<V>> sequenceFailFast(List<? extends CompletableFuture<? extends V>> list) {
        CompletableFuture completableFuture = new CompletableFuture();
        return Util.fallibleSequence(list, completableFuture::completeExceptionally).applyToEither((CompletionStage)completableFuture, Function.identity());
    }

    public static <V> CompletableFuture<List<V>> sequenceFailFastAndCancel(List<? extends CompletableFuture<? extends V>> list) {
        CompletableFuture completableFuture = new CompletableFuture();
        return Util.fallibleSequence(list, throwable -> {
            for (CompletableFuture completableFuture2 : list) {
                completableFuture2.cancel(true);
            }
            completableFuture.completeExceptionally((Throwable)throwable);
        }).applyToEither((CompletionStage)completableFuture, Function.identity());
    }

    private static <V> CompletableFuture<List<V>> fallibleSequence(List<? extends CompletableFuture<? extends V>> list, Consumer<Throwable> consumer) {
        ArrayList list2 = Lists.newArrayListWithCapacity(list.size());
        CompletableFuture[] completableFutures = new CompletableFuture[list.size()];
        list.forEach(completableFuture -> {
            int i = list2.size();
            list2.add(null);
            completableFutures[i] = completableFuture.whenComplete((object, throwable) -> {
                if (throwable != null) {
                    consumer.accept((Throwable)throwable);
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
            Util.doPause(string);
        }
    }

    public static void logAndPauseIfInIde(String string, Throwable throwable) {
        LOGGER.error(string, throwable);
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            Util.doPause(string);
        }
    }

    public static <T extends Throwable> T pauseInIde(T throwable) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            LOGGER.error("Trying to throw a fatal exception, pausing in IDE", throwable);
            Util.doPause(throwable.getMessage());
        }
        return throwable;
    }

    public static void setPause(Consumer<String> consumer) {
        thePauser = consumer;
    }

    private static void doPause(String string) {
        boolean bl;
        Instant instant = Instant.now();
        LOGGER.warn("Did you remember to set a breakpoint here?");
        boolean bl2 = bl = Duration.between(instant, Instant.now()).toMillis() > 500L;
        if (!bl) {
            thePauser.accept(string);
        }
    }

    public static String describeError(Throwable throwable) {
        if (throwable.getCause() != null) {
            return Util.describeError(throwable.getCause());
        }
        if (throwable.getMessage() != null) {
            return throwable.getMessage();
        }
        return throwable.toString();
    }

    public static <T> T getRandom(T[] objects, RandomSource randomSource) {
        return objects[randomSource.nextInt(objects.length)];
    }

    public static int getRandom(int[] is, RandomSource randomSource) {
        return is[randomSource.nextInt(is.length)];
    }

    public static <T> T getRandom(List<T> list, RandomSource randomSource) {
        return list.get(randomSource.nextInt(list.size()));
    }

    public static <T> Optional<T> getRandomSafe(List<T> list, RandomSource randomSource) {
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Util.getRandom(list, randomSource));
    }

    private static BooleanSupplier createRenamer(final Path path, final Path path2) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                try {
                    Files.move(path, path2, new CopyOption[0]);
                    return true;
                } catch (IOException iOException) {
                    LOGGER.error("Failed to rename", iOException);
                    return false;
                }
            }

            public String toString() {
                return "rename " + path + " to " + path2;
            }
        };
    }

    private static BooleanSupplier createDeleter(final Path path) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                try {
                    Files.deleteIfExists(path);
                    return true;
                } catch (IOException iOException) {
                    LOGGER.warn("Failed to delete", iOException);
                    return false;
                }
            }

            public String toString() {
                return "delete old " + path;
            }
        };
    }

    private static BooleanSupplier createFileDeletedCheck(final Path path) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                return !Files.exists(path, new LinkOption[0]);
            }

            public String toString() {
                return "verify that " + path + " is deleted";
            }
        };
    }

    private static BooleanSupplier createFileCreatedCheck(final Path path) {
        return new BooleanSupplier(){

            @Override
            public boolean getAsBoolean() {
                return Files.isRegularFile(path, new LinkOption[0]);
            }

            public String toString() {
                return "verify that " + path + " is present";
            }
        };
    }

    private static boolean executeInSequence(BooleanSupplier ... booleanSuppliers) {
        for (BooleanSupplier booleanSupplier : booleanSuppliers) {
            if (booleanSupplier.getAsBoolean()) continue;
            LOGGER.warn("Failed to execute {}", (Object)booleanSupplier);
            return false;
        }
        return true;
    }

    private static boolean runWithRetries(int i, String string, BooleanSupplier ... booleanSuppliers) {
        for (int j = 0; j < i; ++j) {
            if (Util.executeInSequence(booleanSuppliers)) {
                return true;
            }
            LOGGER.error("Failed to {}, retrying {}/{}", string, j, i);
        }
        LOGGER.error("Failed to {}, aborting, progress might be lost", (Object)string);
        return false;
    }

    public static void safeReplaceFile(File file, File file2, File file3) {
        Util.safeReplaceFile(file.toPath(), file2.toPath(), file3.toPath());
    }

    public static void safeReplaceFile(Path path, Path path2, Path path3) {
        Util.safeReplaceOrMoveFile(path, path2, path3, false);
    }

    public static void safeReplaceOrMoveFile(File file, File file2, File file3, boolean bl) {
        Util.safeReplaceOrMoveFile(file.toPath(), file2.toPath(), file3.toPath(), bl);
    }

    public static void safeReplaceOrMoveFile(Path path, Path path2, Path path3, boolean bl) {
        int i = 10;
        if (Files.exists(path, new LinkOption[0]) && !Util.runWithRetries(10, "create backup " + path3, Util.createDeleter(path3), Util.createRenamer(path, path3), Util.createFileCreatedCheck(path3))) {
            return;
        }
        if (!Util.runWithRetries(10, "remove old " + path, Util.createDeleter(path), Util.createFileDeletedCheck(path))) {
            return;
        }
        if (!Util.runWithRetries(10, "replace " + path + " with " + path2, Util.createRenamer(path2, path), Util.createFileCreatedCheck(path)) && !bl) {
            Util.runWithRetries(10, "restore " + path + " from " + path3, Util.createRenamer(path3, path), Util.createFileCreatedCheck(path));
        }
    }

    public static int offsetByCodepoints(String string, int i, int j) {
        int k = string.length();
        if (j >= 0) {
            for (int l = 0; i < k && l < j; ++l) {
                if (!Character.isHighSurrogate(string.charAt(i++)) || i >= k || !Character.isLowSurrogate(string.charAt(i))) continue;
                ++i;
            }
        } else {
            for (int l = j; i > 0 && l < 0; ++l) {
                if (!Character.isLowSurrogate(string.charAt(--i)) || i <= 0 || !Character.isHighSurrogate(string.charAt(i - 1))) continue;
                --i;
            }
        }
        return i;
    }

    public static Consumer<String> prefix(String string, Consumer<String> consumer) {
        return string2 -> consumer.accept(string + string2);
    }

    public static DataResult<int[]> fixedSize(IntStream intStream, int i) {
        int[] is = intStream.limit(i + 1).toArray();
        if (is.length != i) {
            String string = "Input is not a list of " + i + " ints";
            if (is.length >= i) {
                return DataResult.error(string, Arrays.copyOf(is, i));
            }
            return DataResult.error(string);
        }
        return DataResult.success(is);
    }

    public static <T> DataResult<List<T>> fixedSize(List<T> list, int i) {
        if (list.size() != i) {
            String string = "Input is not a list of " + i + " elements";
            if (list.size() >= i) {
                return DataResult.error(string, list.subList(0, i));
            }
            return DataResult.error(string);
        }
        return DataResult.success(list);
    }

    public static void startTimerHackThread() {
        Thread thread = new Thread("Timer hack thread"){

            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(Integer.MAX_VALUE);
                    }
                } catch (InterruptedException interruptedException) {
                    LOGGER.warn("Timer hack thread interrupted, that really should not happen");
                    return;
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
        Files.copy(path3, path5, new CopyOption[0]);
    }

    public static String sanitizeName(String string, CharPredicate charPredicate) {
        return string.toLowerCase(Locale.ROOT).chars().mapToObj(i -> charPredicate.test((char)i) ? Character.toString((char)i) : "_").collect(Collectors.joining());
    }

    public static <T, R> Function<T, R> memoize(final Function<T, R> function) {
        return new Function<T, R>(){
            private final Map<T, R> cache = Maps.newHashMap();

            @Override
            public R apply(T object) {
                return this.cache.computeIfAbsent(object, function);
            }

            public String toString() {
                return "memoize/1[function=" + function + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T, U, R> BiFunction<T, U, R> memoize(final BiFunction<T, U, R> biFunction) {
        return new BiFunction<T, U, R>(){
            private final Map<Pair<T, U>, R> cache = Maps.newHashMap();

            @Override
            public R apply(T object, U object2) {
                return this.cache.computeIfAbsent(Pair.of(object, object2), pair -> biFunction.apply(pair.getFirst(), pair.getSecond()));
            }

            public String toString() {
                return "memoize/2[function=" + biFunction + ", size=" + this.cache.size() + "]";
            }
        };
    }

    public static <T> List<T> toShuffledList(Stream<T> stream, RandomSource randomSource) {
        ObjectArrayList objectArrayList = stream.collect(ObjectArrayList.toList());
        Util.shuffle(objectArrayList, randomSource);
        return objectArrayList;
    }

    public static IntArrayList toShuffledList(IntStream intStream, RandomSource randomSource) {
        int i;
        IntArrayList intArrayList = IntArrayList.wrap(intStream.toArray());
        for (int j = i = intArrayList.size(); j > 1; --j) {
            int k = randomSource.nextInt(j);
            intArrayList.set(j - 1, intArrayList.set(k, intArrayList.getInt(j - 1)));
        }
        return intArrayList;
    }

    public static <T> List<T> shuffledCopy(T[] objects, RandomSource randomSource) {
        ObjectArrayList<T> objectArrayList = new ObjectArrayList<T>(objects);
        Util.shuffle(objectArrayList, randomSource);
        return objectArrayList;
    }

    public static <T> List<T> shuffledCopy(ObjectArrayList<T> objectArrayList, RandomSource randomSource) {
        ObjectArrayList<T> objectArrayList2 = new ObjectArrayList<T>(objectArrayList);
        Util.shuffle(objectArrayList2, randomSource);
        return objectArrayList2;
    }

    public static <T> void shuffle(ObjectArrayList<T> objectArrayList, RandomSource randomSource) {
        int i;
        for (int j = i = objectArrayList.size(); j > 1; --j) {
            int k = randomSource.nextInt(j);
            objectArrayList.set(j - 1, objectArrayList.set(k, objectArrayList.get(j - 1)));
        }
    }

    public static <T> CompletableFuture<T> blockUntilDone(Function<Executor, CompletableFuture<T>> function) {
        return Util.blockUntilDone(function, CompletableFuture::isDone);
    }

    public static <T> T blockUntilDone(Function<Executor, T> function, Predicate<T> predicate) {
        int i;
        LinkedBlockingQueue blockingQueue = new LinkedBlockingQueue();
        T object = function.apply(blockingQueue::add);
        while (!predicate.test(object)) {
            try {
                Runnable runnable = (Runnable)blockingQueue.poll(100L, TimeUnit.MILLISECONDS);
                if (runnable == null) continue;
                runnable.run();
            } catch (InterruptedException interruptedException) {
                LOGGER.warn("Interrupted wait");
                break;
            }
        }
        if ((i = blockingQueue.size()) > 0) {
            LOGGER.warn("Tasks left in queue: {}", (Object)i);
        }
        return object;
    }

    public static <T> ToIntFunction<T> createIndexLookup(List<T> list) {
        return Util.createIndexLookup(list, Object2IntOpenHashMap::new);
    }

    public static <T> ToIntFunction<T> createIndexLookup(List<T> list, IntFunction<Object2IntMap<T>> intFunction) {
        Object2IntMap<T> object2IntMap = intFunction.apply(list.size());
        for (int i = 0; i < list.size(); ++i) {
            object2IntMap.put(list.get(i), i);
        }
        return object2IntMap;
    }

    /*
     * Uses 'sealed' constructs - enablewith --sealed true
     */
    public static enum OS {
        LINUX("linux"),
        SOLARIS("solaris"),
        WINDOWS("windows"){

            @Override
            protected String[] getOpenUrlArguments(URL uRL) {
                return new String[]{"rundll32", "url.dll,FileProtocolHandler", uRL.toString()};
            }
        }
        ,
        OSX("mac"){

            @Override
            protected String[] getOpenUrlArguments(URL uRL) {
                return new String[]{"open", uRL.toString()};
            }
        }
        ,
        UNKNOWN("unknown");

        private final String telemetryName;

        OS(String string2) {
            this.telemetryName = string2;
        }

        public void openUrl(URL uRL) {
            try {
                Process process = AccessController.doPrivileged(() -> Runtime.getRuntime().exec(this.getOpenUrlArguments(uRL)));
                for (String string : IOUtils.readLines(process.getErrorStream())) {
                    LOGGER.error(string);
                }
                process.getInputStream().close();
                process.getErrorStream().close();
                process.getOutputStream().close();
            } catch (IOException | PrivilegedActionException exception) {
                LOGGER.error("Couldn't open url '{}'", (Object)uRL, (Object)exception);
            }
        }

        public void openUri(URI uRI) {
            try {
                this.openUrl(uRI.toURL());
            } catch (MalformedURLException malformedURLException) {
                LOGGER.error("Couldn't open uri '{}'", (Object)uRI, (Object)malformedURLException);
            }
        }

        public void openFile(File file) {
            try {
                this.openUrl(file.toURI().toURL());
            } catch (MalformedURLException malformedURLException) {
                LOGGER.error("Couldn't open file '{}'", (Object)file, (Object)malformedURLException);
            }
        }

        protected String[] getOpenUrlArguments(URL uRL) {
            String string = uRL.toString();
            if ("file".equals(uRL.getProtocol())) {
                string = string.replace("file:", "file://");
            }
            return new String[]{"xdg-open", string};
        }

        public void openUri(String string) {
            try {
                this.openUrl(new URI(string).toURL());
            } catch (IllegalArgumentException | MalformedURLException | URISyntaxException exception) {
                LOGGER.error("Couldn't open uri '{}'", (Object)string, (Object)exception);
            }
        }

        public String telemetryName() {
            return this.telemetryName;
        }
    }

    static enum IdentityStrategy implements Hash.Strategy<Object>
    {
        INSTANCE;


        @Override
        public int hashCode(Object object) {
            return System.identityHashCode(object);
        }

        @Override
        public boolean equals(Object object, Object object2) {
            return object == object2;
        }
    }
}


/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.resources;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.DelegatingOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegistryReadOps<T>
extends DelegatingOps<T> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ResourceManager resourceManager;
    private final RegistryAccess.RegistryHolder registryHolder;
    private final Map<ResourceKey<? extends Registry<?>>, ReadCache<?>> readCache = Maps.newIdentityHashMap();

    public static <T> RegistryReadOps<T> create(DynamicOps<T> dynamicOps, ResourceManager resourceManager, RegistryAccess.RegistryHolder registryHolder) {
        RegistryReadOps<T> registryReadOps = new RegistryReadOps<T>(dynamicOps, resourceManager, registryHolder);
        RegistryAccess.load(registryHolder, registryReadOps);
        return registryReadOps;
    }

    private RegistryReadOps(DynamicOps<T> dynamicOps, ResourceManager resourceManager, RegistryAccess.RegistryHolder registryHolder) {
        super(dynamicOps);
        this.resourceManager = resourceManager;
        this.registryHolder = registryHolder;
    }

    protected <E> DataResult<Pair<java.util.function.Supplier<E>, T>> decodeElement(T object, ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec) {
        Optional optional = this.registryHolder.registry(resourceKey);
        if (!optional.isPresent()) {
            return DataResult.error("Unknown registry: " + resourceKey);
        }
        WritableRegistry writableRegistry = optional.get();
        DataResult dataResult = ResourceLocation.CODEC.decode(this.delegate, object);
        if (!dataResult.result().isPresent()) {
            return codec.decode(this.delegate, object).map(pair -> pair.mapFirst(object -> () -> object));
        }
        Pair pair2 = dataResult.result().get();
        ResourceLocation resourceLocation = (ResourceLocation)pair2.getFirst();
        return this.readAndRegisterElement(resourceKey, writableRegistry, codec, resourceLocation).map(supplier -> Pair.of(supplier, pair2.getSecond()));
    }

    public <E> DataResult<MappedRegistry<E>> decodeElements(MappedRegistry<E> mappedRegistry2, ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec) {
        ResourceLocation resourceLocation = resourceKey.location();
        Collection<ResourceLocation> collection = this.resourceManager.listResources(resourceLocation.getPath(), string -> string.endsWith(".json"));
        DataResult<MappedRegistry<Object>> dataResult = DataResult.success(mappedRegistry2, Lifecycle.stable());
        String string2 = resourceLocation.getPath() + "/";
        for (ResourceLocation resourceLocation2 : collection) {
            String string22 = resourceLocation2.getPath();
            if (!string22.endsWith(".json")) {
                LOGGER.warn("Skipping resource {} since it is not a json file", (Object)resourceLocation2);
                continue;
            }
            if (!string22.startsWith(string2)) {
                LOGGER.warn("Skipping resource {} since it does not have a registry name prefix", (Object)resourceLocation2);
                continue;
            }
            String string3 = string22.substring(string2.length(), string22.length() - ".json".length());
            ResourceLocation resourceLocation3 = new ResourceLocation(resourceLocation2.getNamespace(), string3);
            dataResult = dataResult.flatMap(mappedRegistry -> this.readAndRegisterElement(resourceKey, (WritableRegistry)mappedRegistry, codec, resourceLocation3).map(supplier -> mappedRegistry));
        }
        return dataResult.setPartial(mappedRegistry2);
    }

    private <E> DataResult<java.util.function.Supplier<E>> readAndRegisterElement(ResourceKey<? extends Registry<E>> resourceKey, WritableRegistry<E> writableRegistry, Codec<E> codec, ResourceLocation resourceLocation) {
        DataResult dataResult3;
        ResourceKey resourceKey2 = ResourceKey.create(resourceKey, resourceLocation);
        ReadCache<E> readCache = this.readCache(resourceKey);
        DataResult dataResult = (DataResult)((ReadCache)readCache).values.get(resourceKey2);
        if (dataResult != null) {
            return dataResult;
        }
        Supplier<Object> supplier = Suppliers.memoize(() -> {
            Object object = writableRegistry.get(resourceKey2);
            if (object == null) {
                throw new RuntimeException("Error during recursive registry parsing, element resolved too early: " + resourceKey2);
            }
            return object;
        });
        ((ReadCache)readCache).values.put(resourceKey2, DataResult.success(supplier));
        DataResult<java.util.function.Supplier> dataResult2 = this.readElementFromFile(resourceKey, resourceKey2, codec);
        if (dataResult2.result().isPresent()) {
            writableRegistry.registerOrOverride(resourceKey2, dataResult2.result().get());
            dataResult3 = dataResult2;
        } else {
            Object object2 = writableRegistry.get(resourceKey2);
            dataResult3 = object2 != null ? DataResult.success(object2, Lifecycle.stable()) : dataResult2;
        }
        DataResult<java.util.function.Supplier<E>> dataResult4 = dataResult3.map(object -> () -> object);
        ((ReadCache)readCache).values.put(resourceKey2, dataResult4);
        return dataResult4;
    }

    /*
     * Exception decompiling
     */
    private <E> DataResult<E> readElementFromFile(ResourceKey<? extends Registry<E>> resourceKey, ResourceKey<E> resourceKey2, Codec<E> codec) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 2 blocks at once
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:538)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:261)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:143)
         *     at net.fabricmc.loom.decompilers.cfr.LoomCFRDecompiler.decompile(LoomCFRDecompiler.java:89)
         *     at net.fabricmc.loom.task.GenerateSourcesTask$DecompileAction.doDecompile(GenerateSourcesTask.java:269)
         *     at net.fabricmc.loom.task.GenerateSourcesTask$DecompileAction.execute(GenerateSourcesTask.java:234)
         *     at org.gradle.workers.internal.DefaultWorkerServer.execute(DefaultWorkerServer.java:63)
         *     at org.gradle.workers.internal.AbstractClassLoaderWorker$1.create(AbstractClassLoaderWorker.java:49)
         *     at org.gradle.workers.internal.AbstractClassLoaderWorker$1.create(AbstractClassLoaderWorker.java:43)
         *     at org.gradle.internal.classloader.ClassLoaderUtils.executeInClassloader(ClassLoaderUtils.java:100)
         *     at org.gradle.workers.internal.AbstractClassLoaderWorker.executeInClassLoader(AbstractClassLoaderWorker.java:43)
         *     at org.gradle.workers.internal.IsolatedClassloaderWorker.run(IsolatedClassloaderWorker.java:49)
         *     at org.gradle.workers.internal.IsolatedClassloaderWorker.run(IsolatedClassloaderWorker.java:30)
         *     at org.gradle.workers.internal.WorkerDaemonServer.run(WorkerDaemonServer.java:87)
         *     at org.gradle.workers.internal.WorkerDaemonServer.run(WorkerDaemonServer.java:56)
         *     at org.gradle.process.internal.worker.request.WorkerAction$1.call(WorkerAction.java:138)
         *     at org.gradle.process.internal.worker.child.WorkerLogEventListener.withWorkerLoggingProtocol(WorkerLogEventListener.java:41)
         *     at org.gradle.process.internal.worker.request.WorkerAction.run(WorkerAction.java:135)
         *     at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
         *     at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
         *     at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
         *     at java.base/java.lang.reflect.Method.invoke(Method.java:568)
         *     at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:36)
         *     at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:24)
         *     at org.gradle.internal.remote.internal.hub.MessageHubBackedObjectConnection$DispatchWrapper.dispatch(MessageHubBackedObjectConnection.java:182)
         *     at org.gradle.internal.remote.internal.hub.MessageHubBackedObjectConnection$DispatchWrapper.dispatch(MessageHubBackedObjectConnection.java:164)
         *     at org.gradle.internal.remote.internal.hub.MessageHub$Handler.run(MessageHub.java:414)
         *     at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:64)
         *     at org.gradle.internal.concurrent.ManagedExecutorImpl$1.run(ManagedExecutorImpl.java:49)
         *     at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
         *     at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
         *     at java.base/java.lang.Thread.run(Thread.java:833)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    private <E> ReadCache<E> readCache(ResourceKey<? extends Registry<E>> resourceKey2) {
        return this.readCache.computeIfAbsent(resourceKey2, resourceKey -> new ReadCache());
    }

    static final class ReadCache<E> {
        private final Map<ResourceKey<E>, DataResult<java.util.function.Supplier<E>>> values = Maps.newIdentityHashMap();

        private ReadCache() {
        }
    }
}


/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources;

import com.google.common.collect.Lists;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.User;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SplashManager
extends SimplePreparableReloadListener<List<String>> {
    private static final ResourceLocation SPLASHES_LOCATION = new ResourceLocation("texts/splashes.txt");
    private static final Random RANDOM = new Random();
    private final List<String> splashes = Lists.newArrayList();
    private final User user;

    public SplashManager(User user) {
        this.user = user;
    }

    /*
     * Exception decompiling
     */
    @Override
    protected List<String> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
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

    @Override
    protected void apply(List<String> list, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.splashes.clear();
        this.splashes.addAll(list);
    }

    @Nullable
    public String getSplash() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if (calendar.get(2) + 1 == 12 && calendar.get(5) == 24) {
            return "Merry X-mas!";
        }
        if (calendar.get(2) + 1 == 1 && calendar.get(5) == 1) {
            return "Happy new year!";
        }
        if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31) {
            return "OOoooOOOoooo! Spooky!";
        }
        if (this.splashes.isEmpty()) {
            return null;
        }
        if (this.user != null && RANDOM.nextInt(this.splashes.size()) == 42) {
            return this.user.getName().toUpperCase(Locale.ROOT) + " IS YOU";
        }
        return this.splashes.get(RANDOM.nextInt(this.splashes.size()));
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }

    private static /* synthetic */ boolean method_18664(String string) {
        return string.hashCode() != 125780783;
    }
}


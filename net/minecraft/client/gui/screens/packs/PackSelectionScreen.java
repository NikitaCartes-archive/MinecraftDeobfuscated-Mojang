/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.packs;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class PackSelectionScreen
extends Screen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Component DRAG_AND_DROP = new TranslatableComponent("pack.dropInfo").withStyle(ChatFormatting.GRAY);
    private static final Component DIRECTORY_BUTTON_TOOLTIP = new TranslatableComponent("pack.folderInfo");
    private static final ResourceLocation DEFAULT_ICON = new ResourceLocation("textures/misc/unknown_pack.png");
    private final PackSelectionModel model;
    private final Screen lastScreen;
    @Nullable
    private Watcher watcher;
    private long ticksToReload;
    private TransferableSelectionList availablePackList;
    private TransferableSelectionList selectedPackList;
    private final File packDir;
    private Button doneButton;
    private final Map<String, ResourceLocation> packIcons = Maps.newHashMap();

    public PackSelectionScreen(Screen screen, PackRepository packRepository, Consumer<PackRepository> consumer, File file, Component component) {
        super(component);
        this.lastScreen = screen;
        this.model = new PackSelectionModel(this::populateLists, this::getPackIcon, packRepository, consumer);
        this.packDir = file;
        this.watcher = Watcher.create(file);
    }

    @Override
    public void onClose() {
        this.model.commit();
        this.minecraft.setScreen(this.lastScreen);
        this.closeWatcher();
    }

    private void closeWatcher() {
        if (this.watcher != null) {
            try {
                this.watcher.close();
                this.watcher = null;
            } catch (Exception exception) {
                // empty catch block
            }
        }
    }

    @Override
    protected void init() {
        this.doneButton = this.addButton(new Button(this.width / 2 + 4, this.height - 48, 150, 20, CommonComponents.GUI_DONE, button -> this.onClose()));
        this.addButton(new Button(this.width / 2 - 154, this.height - 48, 150, 20, new TranslatableComponent("pack.openFolder"), button -> Util.getPlatform().openFile(this.packDir), (button, poseStack, i, j) -> this.renderTooltip(poseStack, DIRECTORY_BUTTON_TOOLTIP, i, j)));
        this.availablePackList = new TransferableSelectionList(this.minecraft, 200, this.height, new TranslatableComponent("pack.available.title"));
        this.availablePackList.setLeftPos(this.width / 2 - 4 - 200);
        this.children.add(this.availablePackList);
        this.selectedPackList = new TransferableSelectionList(this.minecraft, 200, this.height, new TranslatableComponent("pack.selected.title"));
        this.selectedPackList.setLeftPos(this.width / 2 + 4);
        this.children.add(this.selectedPackList);
        this.reload();
    }

    @Override
    public void tick() {
        if (this.watcher != null) {
            try {
                if (this.watcher.pollForChanges()) {
                    this.ticksToReload = 20L;
                }
            } catch (IOException iOException) {
                LOGGER.warn("Failed to poll for directory {} changes, stopping", (Object)this.packDir);
                this.closeWatcher();
            }
        }
        if (this.ticksToReload > 0L && --this.ticksToReload == 0L) {
            this.reload();
        }
    }

    private void populateLists() {
        this.updateList(this.selectedPackList, this.model.getSelected());
        this.updateList(this.availablePackList, this.model.getUnselected());
        this.doneButton.active = !this.selectedPackList.children().isEmpty();
    }

    private void updateList(TransferableSelectionList transferableSelectionList, Stream<PackSelectionModel.Entry> stream) {
        transferableSelectionList.children().clear();
        stream.forEach(entry -> transferableSelectionList.children().add(new TransferableSelectionList.PackEntry(this.minecraft, transferableSelectionList, this, (PackSelectionModel.Entry)entry)));
    }

    private void reload() {
        this.model.findNewPacks();
        this.populateLists();
        this.ticksToReload = 0L;
        this.packIcons.clear();
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderDirtBackground(0);
        this.availablePackList.render(poseStack, i, j, f);
        this.selectedPackList.render(poseStack, i, j, f);
        PackSelectionScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 8, 0xFFFFFF);
        PackSelectionScreen.drawCenteredString(poseStack, this.font, DRAG_AND_DROP, this.width / 2, 20, 0xFFFFFF);
        super.render(poseStack, i, j, f);
    }

    protected static void copyPacks(Minecraft minecraft, List<Path> list, Path path) {
        MutableBoolean mutableBoolean = new MutableBoolean();
        list.forEach(path2 -> {
            try (Stream<Path> stream = Files.walk(path2, new FileVisitOption[0]);){
                stream.forEach(path3 -> {
                    try {
                        Util.copyBetweenDirs(path2.getParent(), path, path3);
                    } catch (IOException iOException) {
                        LOGGER.warn("Failed to copy datapack file  from {} to {}", path3, (Object)path, (Object)iOException);
                        mutableBoolean.setTrue();
                    }
                });
            } catch (IOException iOException) {
                LOGGER.warn("Failed to copy datapack file from {} to {}", path2, (Object)path);
                mutableBoolean.setTrue();
            }
        });
        if (mutableBoolean.isTrue()) {
            SystemToast.onPackCopyFailure(minecraft, path.toString());
        }
    }

    @Override
    public void onFilesDrop(List<Path> list) {
        String string = list.stream().map(Path::getFileName).map(Path::toString).collect(Collectors.joining(", "));
        this.minecraft.setScreen(new ConfirmScreen(bl -> {
            if (bl) {
                PackSelectionScreen.copyPacks(this.minecraft, list, this.packDir.toPath());
                this.reload();
            }
            this.minecraft.setScreen(this);
        }, new TranslatableComponent("pack.dropConfirm"), new TextComponent(string)));
    }

    /*
     * Exception decompiling
     */
    private ResourceLocation loadPackIcon(TextureManager textureManager, Pack pack) {
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

    private ResourceLocation getPackIcon(Pack pack) {
        return this.packIcons.computeIfAbsent(pack.getId(), string -> this.loadPackIcon(this.minecraft.getTextureManager(), pack));
    }

    @Environment(value=EnvType.CLIENT)
    static class Watcher
    implements AutoCloseable {
        private final WatchService watcher;
        private final Path packPath;

        public Watcher(File file) throws IOException {
            this.packPath = file.toPath();
            this.watcher = this.packPath.getFileSystem().newWatchService();
            try {
                this.watchDir(this.packPath);
                try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(this.packPath);){
                    for (Path path : directoryStream) {
                        if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) continue;
                        this.watchDir(path);
                    }
                }
            } catch (Exception exception) {
                this.watcher.close();
                throw exception;
            }
        }

        @Nullable
        public static Watcher create(File file) {
            try {
                return new Watcher(file);
            } catch (IOException iOException) {
                LOGGER.warn("Failed to initialize pack directory {} monitoring", (Object)file, (Object)iOException);
                return null;
            }
        }

        private void watchDir(Path path) throws IOException {
            path.register(this.watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        }

        public boolean pollForChanges() throws IOException {
            WatchKey watchKey;
            boolean bl = false;
            while ((watchKey = this.watcher.poll()) != null) {
                List<WatchEvent<?>> list = watchKey.pollEvents();
                for (WatchEvent<?> watchEvent : list) {
                    Path path;
                    bl = true;
                    if (watchKey.watchable() != this.packPath || watchEvent.kind() != StandardWatchEventKinds.ENTRY_CREATE || !Files.isDirectory(path = this.packPath.resolve((Path)watchEvent.context()), LinkOption.NOFOLLOW_LINKS)) continue;
                    this.watchDir(path);
                }
                watchKey.reset();
            }
            return bl;
        }

        @Override
        public void close() throws IOException {
            this.watcher.close();
        }
    }
}


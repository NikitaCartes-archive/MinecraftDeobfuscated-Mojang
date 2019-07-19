/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.realmsclient.client.FileDownload;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsDefaultVertexFormat;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsDownloadLatestWorldScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final RealmsScreen lastScreen;
    private final WorldDownload worldDownload;
    private final String downloadTitle;
    private final RateLimiter narrationRateLimiter;
    private RealmsButton cancelButton;
    private final String worldName;
    private final DownloadStatus downloadStatus;
    private volatile String errorMessage;
    private volatile String status;
    private volatile String progress;
    private volatile boolean cancelled;
    private volatile boolean showDots = true;
    private volatile boolean finished;
    private volatile boolean extracting;
    private Long previousWrittenBytes;
    private Long previousTimeSnapshot;
    private long bytesPersSecond;
    private int animTick;
    private static final String[] DOTS = new String[]{"", ".", ". .", ". . ."};
    private int dotIndex;
    private final int WARNING_ID = 100;
    private int confirmationId = -1;
    private boolean checked;
    private static final ReentrantLock downloadLock = new ReentrantLock();

    public RealmsDownloadLatestWorldScreen(RealmsScreen realmsScreen, WorldDownload worldDownload, String string) {
        this.lastScreen = realmsScreen;
        this.worldName = string;
        this.worldDownload = worldDownload;
        this.downloadStatus = new DownloadStatus();
        this.downloadTitle = RealmsDownloadLatestWorldScreen.getLocalizedString("mco.download.title");
        this.narrationRateLimiter = RateLimiter.create(0.1f);
    }

    public void setConfirmationId(int i) {
        this.confirmationId = i;
    }

    @Override
    public void init() {
        this.setKeyboardHandlerSendRepeatsToGui(true);
        this.cancelButton = new RealmsButton(0, this.width() / 2 - 100, this.height() - 42, 200, 20, RealmsDownloadLatestWorldScreen.getLocalizedString("gui.cancel")){

            @Override
            public void onPress() {
                RealmsDownloadLatestWorldScreen.this.cancelled = true;
                RealmsDownloadLatestWorldScreen.this.backButtonClicked();
            }
        };
        this.buttonsAdd(this.cancelButton);
        this.checkDownloadSize();
    }

    private void checkDownloadSize() {
        if (this.finished) {
            return;
        }
        if (!this.checked && this.getContentLength(this.worldDownload.downloadLink) >= 0x140000000L) {
            String string = RealmsDownloadLatestWorldScreen.getLocalizedString("mco.download.confirmation.line1", RealmsDownloadLatestWorldScreen.humanReadableSize(0x140000000L));
            String string2 = RealmsDownloadLatestWorldScreen.getLocalizedString("mco.download.confirmation.line2");
            Realms.setScreen(new RealmsLongConfirmationScreen(this, RealmsLongConfirmationScreen.Type.Warning, string, string2, false, 100));
        } else {
            this.downloadSave();
        }
    }

    @Override
    public void confirmResult(boolean bl, int i) {
        this.checked = true;
        Realms.setScreen(this);
        this.downloadSave();
    }

    private long getContentLength(String string) {
        FileDownload fileDownload = new FileDownload();
        return fileDownload.contentLength(string);
    }

    @Override
    public void tick() {
        super.tick();
        ++this.animTick;
        if (this.status != null && this.narrationRateLimiter.tryAcquire(1)) {
            ArrayList<String> arrayList = new ArrayList<String>();
            arrayList.add(this.downloadTitle);
            arrayList.add(this.status);
            if (this.progress != null) {
                arrayList.add(this.progress + "%");
                arrayList.add(RealmsDownloadLatestWorldScreen.humanReadableSpeed(this.bytesPersSecond));
            }
            if (this.errorMessage != null) {
                arrayList.add(this.errorMessage);
            }
            String string = String.join((CharSequence)System.lineSeparator(), arrayList);
            Realms.narrateNow(string);
        }
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 256) {
            this.cancelled = true;
            this.backButtonClicked();
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    private void backButtonClicked() {
        if (this.finished && this.confirmationId != -1 && this.errorMessage == null) {
            this.lastScreen.confirmResult(true, this.confirmationId);
        }
        Realms.setScreen(this.lastScreen);
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        if (this.extracting && !this.finished) {
            this.status = RealmsDownloadLatestWorldScreen.getLocalizedString("mco.download.extracting");
        }
        this.drawCenteredString(this.downloadTitle, this.width() / 2, 20, 0xFFFFFF);
        this.drawCenteredString(this.status, this.width() / 2, 50, 0xFFFFFF);
        if (this.showDots) {
            this.drawDots();
        }
        if (this.downloadStatus.bytesWritten != 0L && !this.cancelled) {
            this.drawProgressBar();
            this.drawDownloadSpeed();
        }
        if (this.errorMessage != null) {
            this.drawCenteredString(this.errorMessage, this.width() / 2, 110, 0xFF0000);
        }
        super.render(i, j, f);
    }

    private void drawDots() {
        int i = this.fontWidth(this.status);
        if (this.animTick % 10 == 0) {
            ++this.dotIndex;
        }
        this.drawString(DOTS[this.dotIndex % DOTS.length], this.width() / 2 + i / 2 + 5, 50, 0xFFFFFF);
    }

    private void drawProgressBar() {
        double d = this.downloadStatus.bytesWritten.doubleValue() / this.downloadStatus.totalBytes.doubleValue() * 100.0;
        this.progress = String.format(Locale.ROOT, "%.1f", d);
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.disableTexture();
        Tezzelator tezzelator = Tezzelator.instance;
        tezzelator.begin(7, RealmsDefaultVertexFormat.POSITION_COLOR);
        double e = this.width() / 2 - 100;
        double f = 0.5;
        tezzelator.vertex(e - 0.5, 95.5, 0.0).color(217, 210, 210, 255).endVertex();
        tezzelator.vertex(e + 200.0 * d / 100.0 + 0.5, 95.5, 0.0).color(217, 210, 210, 255).endVertex();
        tezzelator.vertex(e + 200.0 * d / 100.0 + 0.5, 79.5, 0.0).color(217, 210, 210, 255).endVertex();
        tezzelator.vertex(e - 0.5, 79.5, 0.0).color(217, 210, 210, 255).endVertex();
        tezzelator.vertex(e, 95.0, 0.0).color(128, 128, 128, 255).endVertex();
        tezzelator.vertex(e + 200.0 * d / 100.0, 95.0, 0.0).color(128, 128, 128, 255).endVertex();
        tezzelator.vertex(e + 200.0 * d / 100.0, 80.0, 0.0).color(128, 128, 128, 255).endVertex();
        tezzelator.vertex(e, 80.0, 0.0).color(128, 128, 128, 255).endVertex();
        tezzelator.end();
        GlStateManager.enableTexture();
        this.drawCenteredString(this.progress + " %", this.width() / 2, 84, 0xFFFFFF);
    }

    private void drawDownloadSpeed() {
        if (this.animTick % 20 == 0) {
            if (this.previousWrittenBytes != null) {
                long l = System.currentTimeMillis() - this.previousTimeSnapshot;
                if (l == 0L) {
                    l = 1L;
                }
                this.bytesPersSecond = 1000L * (this.downloadStatus.bytesWritten - this.previousWrittenBytes) / l;
                this.drawDownloadSpeed0(this.bytesPersSecond);
            }
            this.previousWrittenBytes = this.downloadStatus.bytesWritten;
            this.previousTimeSnapshot = System.currentTimeMillis();
        } else {
            this.drawDownloadSpeed0(this.bytesPersSecond);
        }
    }

    private void drawDownloadSpeed0(long l) {
        if (l > 0L) {
            int i = this.fontWidth(this.progress);
            String string = "(" + RealmsDownloadLatestWorldScreen.humanReadableSpeed(l) + ")";
            this.drawString(string, this.width() / 2 + i / 2 + 15, 84, 0xFFFFFF);
        }
    }

    public static String humanReadableSpeed(long l) {
        int i = 1024;
        if (l < 1024L) {
            return l + " B/s";
        }
        int j = (int)(Math.log(l) / Math.log(1024.0));
        String string = "KMGTPE".charAt(j - 1) + "";
        return String.format(Locale.ROOT, "%.1f %sB/s", (double)l / Math.pow(1024.0, j), string);
    }

    public static String humanReadableSize(long l) {
        int i = 1024;
        if (l < 1024L) {
            return l + " B";
        }
        int j = (int)(Math.log(l) / Math.log(1024.0));
        String string = "KMGTPE".charAt(j - 1) + "";
        return String.format(Locale.ROOT, "%.0f %sB", (double)l / Math.pow(1024.0, j), string);
    }

    private void downloadSave() {
        new Thread(){

            @Override
            public void run() {
                try {
                    if (!downloadLock.tryLock(1L, TimeUnit.SECONDS)) {
                        return;
                    }
                    RealmsDownloadLatestWorldScreen.this.status = RealmsScreen.getLocalizedString("mco.download.preparing");
                    if (RealmsDownloadLatestWorldScreen.this.cancelled) {
                        RealmsDownloadLatestWorldScreen.this.downloadCancelled();
                        return;
                    }
                    RealmsDownloadLatestWorldScreen.this.status = RealmsScreen.getLocalizedString("mco.download.downloading", RealmsDownloadLatestWorldScreen.this.worldName);
                    FileDownload fileDownload = new FileDownload();
                    fileDownload.contentLength(((RealmsDownloadLatestWorldScreen)RealmsDownloadLatestWorldScreen.this).worldDownload.downloadLink);
                    fileDownload.download(RealmsDownloadLatestWorldScreen.this.worldDownload, RealmsDownloadLatestWorldScreen.this.worldName, RealmsDownloadLatestWorldScreen.this.downloadStatus, RealmsDownloadLatestWorldScreen.this.getLevelStorageSource());
                    while (!fileDownload.isFinished()) {
                        if (fileDownload.isError()) {
                            fileDownload.cancel();
                            RealmsDownloadLatestWorldScreen.this.errorMessage = RealmsScreen.getLocalizedString("mco.download.failed");
                            RealmsDownloadLatestWorldScreen.this.cancelButton.setMessage(RealmsScreen.getLocalizedString("gui.done"));
                            return;
                        }
                        if (fileDownload.isExtracting()) {
                            RealmsDownloadLatestWorldScreen.this.extracting = true;
                        }
                        if (RealmsDownloadLatestWorldScreen.this.cancelled) {
                            fileDownload.cancel();
                            RealmsDownloadLatestWorldScreen.this.downloadCancelled();
                            return;
                        }
                        try {
                            Thread.sleep(500L);
                        } catch (InterruptedException interruptedException) {
                            LOGGER.error("Failed to check Realms backup download status");
                        }
                    }
                    RealmsDownloadLatestWorldScreen.this.finished = true;
                    RealmsDownloadLatestWorldScreen.this.status = RealmsScreen.getLocalizedString("mco.download.done");
                    RealmsDownloadLatestWorldScreen.this.cancelButton.setMessage(RealmsScreen.getLocalizedString("gui.done"));
                } catch (InterruptedException interruptedException2) {
                    LOGGER.error("Could not acquire upload lock");
                } catch (Exception exception) {
                    RealmsDownloadLatestWorldScreen.this.errorMessage = RealmsScreen.getLocalizedString("mco.download.failed");
                    exception.printStackTrace();
                } finally {
                    if (!downloadLock.isHeldByCurrentThread()) {
                        return;
                    }
                    downloadLock.unlock();
                    RealmsDownloadLatestWorldScreen.this.showDots = false;
                    RealmsDownloadLatestWorldScreen.this.finished = true;
                }
            }
        }.start();
    }

    private void downloadCancelled() {
        this.status = RealmsDownloadLatestWorldScreen.getLocalizedString("mco.download.cancelled");
    }

    @Environment(value=EnvType.CLIENT)
    public class DownloadStatus {
        public volatile Long bytesWritten = 0L;
        public volatile Long totalBytes = 0L;
    }
}


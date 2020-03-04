package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.realmsclient.Unit;
import com.mojang.realmsclient.client.FileDownload;
import com.mojang.realmsclient.dto.WorldDownload;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsDownloadLatestWorldScreen extends RealmsScreen {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ReentrantLock DOWNLOAD_LOCK = new ReentrantLock();
	private final Screen lastScreen;
	private final WorldDownload worldDownload;
	private final String downloadTitle;
	private final RateLimiter narrationRateLimiter;
	private Button cancelButton;
	private final String worldName;
	private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;
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
	private boolean checked;
	private final BooleanConsumer callback;

	public RealmsDownloadLatestWorldScreen(Screen screen, WorldDownload worldDownload, String string, BooleanConsumer booleanConsumer) {
		this.callback = booleanConsumer;
		this.lastScreen = screen;
		this.worldName = string;
		this.worldDownload = worldDownload;
		this.downloadStatus = new RealmsDownloadLatestWorldScreen.DownloadStatus();
		this.downloadTitle = I18n.get("mco.download.title");
		this.narrationRateLimiter = RateLimiter.create(0.1F);
	}

	@Override
	public void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.cancelButton = this.addButton(new Button(this.width / 2 - 100, this.height - 42, 200, 20, I18n.get("gui.cancel"), button -> {
			this.cancelled = true;
			this.backButtonClicked();
		}));
		this.checkDownloadSize();
	}

	private void checkDownloadSize() {
		if (!this.finished) {
			if (!this.checked && this.getContentLength(this.worldDownload.downloadLink) >= 5368709120L) {
				String string = I18n.get("mco.download.confirmation.line1", Unit.humanReadable(5368709120L));
				String string2 = I18n.get("mco.download.confirmation.line2");
				this.minecraft.setScreen(new RealmsLongConfirmationScreen(bl -> {
					this.checked = true;
					this.minecraft.setScreen(this);
					this.downloadSave();
				}, RealmsLongConfirmationScreen.Type.Warning, string, string2, false));
			} else {
				this.downloadSave();
			}
		}
	}

	private long getContentLength(String string) {
		FileDownload fileDownload = new FileDownload();
		return fileDownload.contentLength(string);
	}

	@Override
	public void tick() {
		super.tick();
		this.animTick++;
		if (this.status != null && this.narrationRateLimiter.tryAcquire(1)) {
			List<String> list = Lists.<String>newArrayList();
			list.add(this.downloadTitle);
			list.add(this.status);
			if (this.progress != null) {
				list.add(this.progress + "%");
				list.add(Unit.humanReadable(this.bytesPersSecond) + "/s");
			}

			if (this.errorMessage != null) {
				list.add(this.errorMessage);
			}

			String string = String.join(System.lineSeparator(), list);
			NarrationHelper.now(string);
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.cancelled = true;
			this.backButtonClicked();
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	private void backButtonClicked() {
		if (this.finished && this.callback != null && this.errorMessage == null) {
			this.callback.accept(true);
		}

		this.minecraft.setScreen(this.lastScreen);
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		if (this.extracting && !this.finished) {
			this.status = I18n.get("mco.download.extracting");
		}

		this.drawCenteredString(this.font, this.downloadTitle, this.width / 2, 20, 16777215);
		this.drawCenteredString(this.font, this.status, this.width / 2, 50, 16777215);
		if (this.showDots) {
			this.drawDots();
		}

		if (this.downloadStatus.bytesWritten != 0L && !this.cancelled) {
			this.drawProgressBar();
			this.drawDownloadSpeed();
		}

		if (this.errorMessage != null) {
			this.drawCenteredString(this.font, this.errorMessage, this.width / 2, 110, 16711680);
		}

		super.render(i, j, f);
	}

	private void drawDots() {
		int i = this.font.width(this.status);
		if (this.animTick % 10 == 0) {
			this.dotIndex++;
		}

		this.font.draw(DOTS[this.dotIndex % DOTS.length], (float)(this.width / 2 + i / 2 + 5), 50.0F, 16777215);
	}

	private void drawProgressBar() {
		double d = this.downloadStatus.bytesWritten.doubleValue() / this.downloadStatus.totalBytes.doubleValue() * 100.0;
		this.progress = String.format(Locale.ROOT, "%.1f", d);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableTexture();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
		double e = (double)(this.width / 2 - 100);
		double f = 0.5;
		bufferBuilder.vertex(e - 0.5, 95.5, 0.0).color(217, 210, 210, 255).endVertex();
		bufferBuilder.vertex(e + 200.0 * d / 100.0 + 0.5, 95.5, 0.0).color(217, 210, 210, 255).endVertex();
		bufferBuilder.vertex(e + 200.0 * d / 100.0 + 0.5, 79.5, 0.0).color(217, 210, 210, 255).endVertex();
		bufferBuilder.vertex(e - 0.5, 79.5, 0.0).color(217, 210, 210, 255).endVertex();
		bufferBuilder.vertex(e, 95.0, 0.0).color(128, 128, 128, 255).endVertex();
		bufferBuilder.vertex(e + 200.0 * d / 100.0, 95.0, 0.0).color(128, 128, 128, 255).endVertex();
		bufferBuilder.vertex(e + 200.0 * d / 100.0, 80.0, 0.0).color(128, 128, 128, 255).endVertex();
		bufferBuilder.vertex(e, 80.0, 0.0).color(128, 128, 128, 255).endVertex();
		tesselator.end();
		RenderSystem.enableTexture();
		this.drawCenteredString(this.font, this.progress + " %", this.width / 2, 84, 16777215);
	}

	private void drawDownloadSpeed() {
		if (this.animTick % 20 == 0) {
			if (this.previousWrittenBytes != null) {
				long l = Util.getMillis() - this.previousTimeSnapshot;
				if (l == 0L) {
					l = 1L;
				}

				this.bytesPersSecond = 1000L * (this.downloadStatus.bytesWritten - this.previousWrittenBytes) / l;
				this.drawDownloadSpeed0(this.bytesPersSecond);
			}

			this.previousWrittenBytes = this.downloadStatus.bytesWritten;
			this.previousTimeSnapshot = Util.getMillis();
		} else {
			this.drawDownloadSpeed0(this.bytesPersSecond);
		}
	}

	private void drawDownloadSpeed0(long l) {
		if (l > 0L) {
			int i = this.font.width(this.progress);
			String string = "(" + Unit.humanReadable(l) + "/s)";
			this.font.draw(string, (float)(this.width / 2 + i / 2 + 15), 84.0F, 16777215);
		}
	}

	private void downloadSave() {
		new Thread(() -> {
			try {
				try {
					if (!DOWNLOAD_LOCK.tryLock(1L, TimeUnit.SECONDS)) {
						return;
					}

					this.status = I18n.get("mco.download.preparing");
					if (this.cancelled) {
						this.downloadCancelled();
						return;
					}

					this.status = I18n.get("mco.download.downloading", this.worldName);
					FileDownload fileDownload = new FileDownload();
					fileDownload.contentLength(this.worldDownload.downloadLink);
					fileDownload.download(this.worldDownload, this.worldName, this.downloadStatus, this.minecraft.getLevelSource());

					while (!fileDownload.isFinished()) {
						if (fileDownload.isError()) {
							fileDownload.cancel();
							this.errorMessage = I18n.get("mco.download.failed");
							this.cancelButton.setMessage(I18n.get("gui.done"));
							return;
						}

						if (fileDownload.isExtracting()) {
							this.extracting = true;
						}

						if (this.cancelled) {
							fileDownload.cancel();
							this.downloadCancelled();
							return;
						}

						try {
							Thread.sleep(500L);
						} catch (InterruptedException var8) {
							LOGGER.error("Failed to check Realms backup download status");
						}
					}

					this.finished = true;
					this.status = I18n.get("mco.download.done");
					this.cancelButton.setMessage(I18n.get("gui.done"));
					return;
				} catch (InterruptedException var9) {
					LOGGER.error("Could not acquire upload lock");
				} catch (Exception var10) {
					this.errorMessage = I18n.get("mco.download.failed");
					var10.printStackTrace();
				}
			} finally {
				if (!DOWNLOAD_LOCK.isHeldByCurrentThread()) {
					return;
				} else {
					DOWNLOAD_LOCK.unlock();
					this.showDots = false;
					this.finished = true;
				}
			}
		}).start();
	}

	private void downloadCancelled() {
		this.status = I18n.get("mco.download.cancelled");
	}

	@Environment(EnvType.CLIENT)
	public class DownloadStatus {
		public volatile Long bytesWritten = 0L;
		public volatile Long totalBytes = 0L;
	}
}

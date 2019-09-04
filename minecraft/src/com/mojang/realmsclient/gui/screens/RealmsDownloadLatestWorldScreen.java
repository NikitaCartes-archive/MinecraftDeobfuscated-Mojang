package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.client.FileDownload;
import com.mojang.realmsclient.dto.WorldDownload;
import java.util.List;
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

@Environment(EnvType.CLIENT)
public class RealmsDownloadLatestWorldScreen extends RealmsScreen {
	private static final Logger LOGGER = LogManager.getLogger();
	private final RealmsScreen lastScreen;
	private final WorldDownload worldDownload;
	private final String downloadTitle;
	private final RateLimiter narrationRateLimiter;
	private RealmsButton cancelButton;
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
	private final int WARNING_ID = 100;
	private int confirmationId = -1;
	private boolean checked;
	private static final ReentrantLock downloadLock = new ReentrantLock();

	public RealmsDownloadLatestWorldScreen(RealmsScreen realmsScreen, WorldDownload worldDownload, String string) {
		this.lastScreen = realmsScreen;
		this.worldName = string;
		this.worldDownload = worldDownload;
		this.downloadStatus = new RealmsDownloadLatestWorldScreen.DownloadStatus();
		this.downloadTitle = getLocalizedString("mco.download.title");
		this.narrationRateLimiter = RateLimiter.create(0.1F);
	}

	public void setConfirmationId(int i) {
		this.confirmationId = i;
	}

	@Override
	public void init() {
		this.setKeyboardHandlerSendRepeatsToGui(true);
		this.buttonsAdd(this.cancelButton = new RealmsButton(0, this.width() / 2 - 100, this.height() - 42, 200, 20, getLocalizedString("gui.cancel")) {
			@Override
			public void onPress() {
				RealmsDownloadLatestWorldScreen.this.cancelled = true;
				RealmsDownloadLatestWorldScreen.this.backButtonClicked();
			}
		});
		this.checkDownloadSize();
	}

	private void checkDownloadSize() {
		if (!this.finished) {
			if (!this.checked && this.getContentLength(this.worldDownload.downloadLink) >= 5368709120L) {
				String string = getLocalizedString("mco.download.confirmation.line1", new Object[]{humanReadableSize(5368709120L)});
				String string2 = getLocalizedString("mco.download.confirmation.line2");
				Realms.setScreen(new RealmsLongConfirmationScreen(this, RealmsLongConfirmationScreen.Type.Warning, string, string2, false, 100));
			} else {
				this.downloadSave();
			}
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
		this.animTick++;
		if (this.status != null && this.narrationRateLimiter.tryAcquire(1)) {
			List<String> list = Lists.<String>newArrayList();
			list.add(this.downloadTitle);
			list.add(this.status);
			if (this.progress != null) {
				list.add(this.progress + "%");
				list.add(humanReadableSpeed(this.bytesPersSecond));
			}

			if (this.errorMessage != null) {
				list.add(this.errorMessage);
			}

			String string = String.join(System.lineSeparator(), list);
			Realms.narrateNow(string);
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
		if (this.finished && this.confirmationId != -1 && this.errorMessage == null) {
			this.lastScreen.confirmResult(true, this.confirmationId);
		}

		Realms.setScreen(this.lastScreen);
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		if (this.extracting && !this.finished) {
			this.status = getLocalizedString("mco.download.extracting");
		}

		this.drawCenteredString(this.downloadTitle, this.width() / 2, 20, 16777215);
		this.drawCenteredString(this.status, this.width() / 2, 50, 16777215);
		if (this.showDots) {
			this.drawDots();
		}

		if (this.downloadStatus.bytesWritten != 0L && !this.cancelled) {
			this.drawProgressBar();
			this.drawDownloadSpeed();
		}

		if (this.errorMessage != null) {
			this.drawCenteredString(this.errorMessage, this.width() / 2, 110, 16711680);
		}

		super.render(i, j, f);
	}

	private void drawDots() {
		int i = this.fontWidth(this.status);
		if (this.animTick % 10 == 0) {
			this.dotIndex++;
		}

		this.drawString(DOTS[this.dotIndex % DOTS.length], this.width() / 2 + i / 2 + 5, 50, 16777215);
	}

	private void drawProgressBar() {
		double d = this.downloadStatus.bytesWritten.doubleValue() / this.downloadStatus.totalBytes.doubleValue() * 100.0;
		this.progress = String.format(Locale.ROOT, "%.1f", d);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableTexture();
		Tezzelator tezzelator = Tezzelator.instance;
		tezzelator.begin(7, RealmsDefaultVertexFormat.POSITION_COLOR);
		double e = (double)(this.width() / 2 - 100);
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
		RenderSystem.enableTexture();
		this.drawCenteredString(this.progress + " %", this.width() / 2, 84, 16777215);
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
			String string = "(" + humanReadableSpeed(l) + ")";
			this.drawString(string, this.width() / 2 + i / 2 + 15, 84, 16777215);
		}
	}

	public static String humanReadableSpeed(long l) {
		int i = 1024;
		if (l < 1024L) {
			return l + " B/s";
		} else {
			int j = (int)(Math.log((double)l) / Math.log(1024.0));
			String string = "KMGTPE".charAt(j - 1) + "";
			return String.format(Locale.ROOT, "%.1f %sB/s", (double)l / Math.pow(1024.0, (double)j), string);
		}
	}

	public static String humanReadableSize(long l) {
		int i = 1024;
		if (l < 1024L) {
			return l + " B";
		} else {
			int j = (int)(Math.log((double)l) / Math.log(1024.0));
			String string = "KMGTPE".charAt(j - 1) + "";
			return String.format(Locale.ROOT, "%.0f %sB", (double)l / Math.pow(1024.0, (double)j), string);
		}
	}

	private void downloadSave() {
		new Thread(() -> {
			try {
				try {
					if (!downloadLock.tryLock(1L, TimeUnit.SECONDS)) {
						return;
					}

					this.status = getLocalizedString("mco.download.preparing");
					if (this.cancelled) {
						this.downloadCancelled();
						return;
					}

					this.status = getLocalizedString("mco.download.downloading", new Object[]{this.worldName});
					FileDownload fileDownload = new FileDownload();
					fileDownload.contentLength(this.worldDownload.downloadLink);
					fileDownload.download(this.worldDownload, this.worldName, this.downloadStatus, this.getLevelStorageSource());

					while (!fileDownload.isFinished()) {
						if (fileDownload.isError()) {
							fileDownload.cancel();
							this.errorMessage = getLocalizedString("mco.download.failed");
							this.cancelButton.setMessage(getLocalizedString("gui.done"));
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
					this.status = getLocalizedString("mco.download.done");
					this.cancelButton.setMessage(getLocalizedString("gui.done"));
					return;
				} catch (InterruptedException var9) {
					LOGGER.error("Could not acquire upload lock");
				} catch (Exception var10) {
					this.errorMessage = getLocalizedString("mco.download.failed");
					var10.printStackTrace();
				}
			} finally {
				if (!downloadLock.isHeldByCurrentThread()) {
					return;
				} else {
					downloadLock.unlock();
					this.showDots = false;
					this.finished = true;
				}
			}
		}).start();
	}

	private void downloadCancelled() {
		this.status = getLocalizedString("mco.download.cancelled");
	}

	@Environment(EnvType.CLIENT)
	public class DownloadStatus {
		public volatile Long bytesWritten = 0L;
		public volatile Long totalBytes = 0L;
	}
}

package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.realmsclient.Unit;
import com.mojang.realmsclient.client.FileUpload;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.client.UploadStatus;
import com.mojang.realmsclient.dto.UploadInfo;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.util.UploadTokenCache;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsUploadScreen extends RealmsScreen {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ReentrantLock UPLOAD_LOCK = new ReentrantLock();
	private static final String[] DOTS = new String[]{"", ".", ". .", ". . ."};
	private static final Component VERIFYING_TEXT = new TranslatableComponent("mco.upload.verifying");
	private final RealmsResetWorldScreen lastScreen;
	private final LevelSummary selectedLevel;
	private final long worldId;
	private final int slotId;
	private final UploadStatus uploadStatus;
	private final RateLimiter narrationRateLimiter;
	private volatile Component[] errorMessage;
	private volatile Component status = new TranslatableComponent("mco.upload.preparing");
	private volatile String progress;
	private volatile boolean cancelled;
	private volatile boolean uploadFinished;
	private volatile boolean showDots = true;
	private volatile boolean uploadStarted;
	private Button backButton;
	private Button cancelButton;
	private int tickCount;
	private Long previousWrittenBytes;
	private Long previousTimeSnapshot;
	private long bytesPersSecond;
	private final Runnable callback;

	public RealmsUploadScreen(long l, int i, RealmsResetWorldScreen realmsResetWorldScreen, LevelSummary levelSummary, Runnable runnable) {
		this.worldId = l;
		this.slotId = i;
		this.lastScreen = realmsResetWorldScreen;
		this.selectedLevel = levelSummary;
		this.uploadStatus = new UploadStatus();
		this.narrationRateLimiter = RateLimiter.create(0.1F);
		this.callback = runnable;
	}

	@Override
	public void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.backButton = this.addButton(new Button(this.width / 2 - 100, this.height - 42, 200, 20, CommonComponents.GUI_BACK, button -> this.onBack()));
		this.backButton.visible = false;
		this.cancelButton = this.addButton(new Button(this.width / 2 - 100, this.height - 42, 200, 20, CommonComponents.GUI_CANCEL, button -> this.onCancel()));
		if (!this.uploadStarted) {
			if (this.lastScreen.slot == -1) {
				this.upload();
			} else {
				this.lastScreen.switchSlot(() -> {
					if (!this.uploadStarted) {
						this.uploadStarted = true;
						this.minecraft.setScreen(this);
						this.upload();
					}
				});
			}
		}
	}

	@Override
	public void removed() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	private void onBack() {
		this.callback.run();
	}

	private void onCancel() {
		this.cancelled = true;
		this.minecraft.setScreen(this.lastScreen);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			if (this.showDots) {
				this.onCancel();
			} else {
				this.onBack();
			}

			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		if (!this.uploadFinished && this.uploadStatus.bytesWritten != 0L && this.uploadStatus.bytesWritten == this.uploadStatus.totalBytes) {
			this.status = VERIFYING_TEXT;
			this.cancelButton.active = false;
		}

		drawCenteredString(poseStack, this.font, this.status, this.width / 2, 50, 16777215);
		if (this.showDots) {
			this.drawDots(poseStack);
		}

		if (this.uploadStatus.bytesWritten != 0L && !this.cancelled) {
			this.drawProgressBar(poseStack);
			this.drawUploadSpeed(poseStack);
		}

		if (this.errorMessage != null) {
			for (int k = 0; k < this.errorMessage.length; k++) {
				drawCenteredString(poseStack, this.font, this.errorMessage[k], this.width / 2, 110 + 12 * k, 16711680);
			}
		}

		super.render(poseStack, i, j, f);
	}

	private void drawDots(PoseStack poseStack) {
		int i = this.font.width(this.status);
		this.font.draw(poseStack, DOTS[this.tickCount / 10 % DOTS.length], (float)(this.width / 2 + i / 2 + 5), 50.0F, 16777215);
	}

	private void drawProgressBar(PoseStack poseStack) {
		double d = Math.min((double)this.uploadStatus.bytesWritten / (double)this.uploadStatus.totalBytes, 1.0);
		this.progress = String.format(Locale.ROOT, "%.1f", d * 100.0);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableTexture();
		double e = (double)(this.width / 2 - 100);
		double f = 0.5;
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
		bufferBuilder.vertex(e - 0.5, 95.5, 0.0).color(217, 210, 210, 255).endVertex();
		bufferBuilder.vertex(e + 200.0 * d + 0.5, 95.5, 0.0).color(217, 210, 210, 255).endVertex();
		bufferBuilder.vertex(e + 200.0 * d + 0.5, 79.5, 0.0).color(217, 210, 210, 255).endVertex();
		bufferBuilder.vertex(e - 0.5, 79.5, 0.0).color(217, 210, 210, 255).endVertex();
		bufferBuilder.vertex(e, 95.0, 0.0).color(128, 128, 128, 255).endVertex();
		bufferBuilder.vertex(e + 200.0 * d, 95.0, 0.0).color(128, 128, 128, 255).endVertex();
		bufferBuilder.vertex(e + 200.0 * d, 80.0, 0.0).color(128, 128, 128, 255).endVertex();
		bufferBuilder.vertex(e, 80.0, 0.0).color(128, 128, 128, 255).endVertex();
		tesselator.end();
		RenderSystem.enableTexture();
		drawCenteredString(poseStack, this.font, this.progress + " %", this.width / 2, 84, 16777215);
	}

	private void drawUploadSpeed(PoseStack poseStack) {
		if (this.tickCount % 20 == 0) {
			if (this.previousWrittenBytes != null) {
				long l = Util.getMillis() - this.previousTimeSnapshot;
				if (l == 0L) {
					l = 1L;
				}

				this.bytesPersSecond = 1000L * (this.uploadStatus.bytesWritten - this.previousWrittenBytes) / l;
				this.drawUploadSpeed0(poseStack, this.bytesPersSecond);
			}

			this.previousWrittenBytes = this.uploadStatus.bytesWritten;
			this.previousTimeSnapshot = Util.getMillis();
		} else {
			this.drawUploadSpeed0(poseStack, this.bytesPersSecond);
		}
	}

	private void drawUploadSpeed0(PoseStack poseStack, long l) {
		if (l > 0L) {
			int i = this.font.width(this.progress);
			String string = "(" + Unit.humanReadable(l) + "/s)";
			this.font.draw(poseStack, string, (float)(this.width / 2 + i / 2 + 15), 84.0F, 16777215);
		}
	}

	@Override
	public void tick() {
		super.tick();
		this.tickCount++;
		if (this.status != null && this.narrationRateLimiter.tryAcquire(1)) {
			List<String> list = Lists.<String>newArrayList();
			list.add(this.status.getString());
			if (this.progress != null) {
				list.add(this.progress + "%");
			}

			if (this.errorMessage != null) {
				Stream.of(this.errorMessage).map(Component::getString).forEach(list::add);
			}

			NarrationHelper.now(String.join(System.lineSeparator(), list));
		}
	}

	private void upload() {
		this.uploadStarted = true;
		new Thread(
				() -> {
					File file = null;
					RealmsClient realmsClient = RealmsClient.create();
					long l = this.worldId;

					try {
						if (!UPLOAD_LOCK.tryLock(1L, TimeUnit.SECONDS)) {
							this.status = new TranslatableComponent("mco.upload.close.failure");
						} else {
							UploadInfo uploadInfo = null;

							for (int i = 0; i < 20; i++) {
								try {
									if (this.cancelled) {
										this.uploadCancelled();
										return;
									}

									uploadInfo = realmsClient.requestUploadInfo(l, UploadTokenCache.get(l));
									if (uploadInfo != null) {
										break;
									}
								} catch (RetryCallException var20) {
									Thread.sleep((long)(var20.delaySeconds * 1000));
								}
							}

							if (uploadInfo == null) {
								this.status = new TranslatableComponent("mco.upload.close.failure");
							} else {
								UploadTokenCache.put(l, uploadInfo.getToken());
								if (!uploadInfo.isWorldClosed()) {
									this.status = new TranslatableComponent("mco.upload.close.failure");
								} else if (this.cancelled) {
									this.uploadCancelled();
								} else {
									File file2 = new File(this.minecraft.gameDirectory.getAbsolutePath(), "saves");
									file = this.tarGzipArchive(new File(file2, this.selectedLevel.getLevelId()));
									if (this.cancelled) {
										this.uploadCancelled();
									} else if (this.verify(file)) {
										this.status = new TranslatableComponent("mco.upload.uploading", this.selectedLevel.getLevelName());
										FileUpload fileUpload = new FileUpload(
											file, this.worldId, this.slotId, uploadInfo, this.minecraft.getUser(), SharedConstants.getCurrentVersion().getName(), this.uploadStatus
										);
										fileUpload.upload(uploadResult -> {
											if (uploadResult.statusCode >= 200 && uploadResult.statusCode < 300) {
												this.uploadFinished = true;
												this.status = new TranslatableComponent("mco.upload.done");
												this.backButton.setMessage(CommonComponents.GUI_DONE);
												UploadTokenCache.invalidate(l);
											} else if (uploadResult.statusCode == 400 && uploadResult.errorMessage != null) {
												this.setErrorMessage(new TranslatableComponent("mco.upload.failed", uploadResult.errorMessage));
											} else {
												this.setErrorMessage(new TranslatableComponent("mco.upload.failed", uploadResult.statusCode));
											}
										});

										while (!fileUpload.isFinished()) {
											if (this.cancelled) {
												fileUpload.cancel();
												this.uploadCancelled();
												return;
											}

											try {
												Thread.sleep(500L);
											} catch (InterruptedException var19) {
												LOGGER.error("Failed to check Realms file upload status");
											}
										}
									} else {
										long m = file.length();
										Unit unit = Unit.getLargest(m);
										Unit unit2 = Unit.getLargest(5368709120L);
										if (Unit.humanReadable(m, unit).equals(Unit.humanReadable(5368709120L, unit2)) && unit != Unit.B) {
											Unit unit3 = Unit.values()[unit.ordinal() - 1];
											this.setErrorMessage(
												new TranslatableComponent("mco.upload.size.failure.line1", this.selectedLevel.getLevelName()),
												new TranslatableComponent("mco.upload.size.failure.line2", Unit.humanReadable(m, unit3), Unit.humanReadable(5368709120L, unit3))
											);
										} else {
											this.setErrorMessage(
												new TranslatableComponent("mco.upload.size.failure.line1", this.selectedLevel.getLevelName()),
												new TranslatableComponent("mco.upload.size.failure.line2", Unit.humanReadable(m, unit), Unit.humanReadable(5368709120L, unit2))
											);
										}
									}
								}
							}
						}
					} catch (IOException var21) {
						this.setErrorMessage(new TranslatableComponent("mco.upload.failed", var21.getMessage()));
					} catch (RealmsServiceException var22) {
						this.setErrorMessage(new TranslatableComponent("mco.upload.failed", var22.toString()));
					} catch (InterruptedException var23) {
						LOGGER.error("Could not acquire upload lock");
					} finally {
						this.uploadFinished = true;
						if (UPLOAD_LOCK.isHeldByCurrentThread()) {
							UPLOAD_LOCK.unlock();
							this.showDots = false;
							this.backButton.visible = true;
							this.cancelButton.visible = false;
							if (file != null) {
								LOGGER.debug("Deleting file " + file.getAbsolutePath());
								file.delete();
							}
						} else {
							return;
						}
					}
				}
			)
			.start();
	}

	private void setErrorMessage(Component... components) {
		this.errorMessage = components;
	}

	private void uploadCancelled() {
		this.status = new TranslatableComponent("mco.upload.cancelled");
		LOGGER.debug("Upload was cancelled");
	}

	private boolean verify(File file) {
		return file.length() < 5368709120L;
	}

	private File tarGzipArchive(File file) throws IOException {
		TarArchiveOutputStream tarArchiveOutputStream = null;

		File var4;
		try {
			File file2 = File.createTempFile("realms-upload-file", ".tar.gz");
			tarArchiveOutputStream = new TarArchiveOutputStream(new GZIPOutputStream(new FileOutputStream(file2)));
			tarArchiveOutputStream.setLongFileMode(3);
			this.addFileToTarGz(tarArchiveOutputStream, file.getAbsolutePath(), "world", true);
			tarArchiveOutputStream.finish();
			var4 = file2;
		} finally {
			if (tarArchiveOutputStream != null) {
				tarArchiveOutputStream.close();
			}
		}

		return var4;
	}

	private void addFileToTarGz(TarArchiveOutputStream tarArchiveOutputStream, String string, String string2, boolean bl) throws IOException {
		if (!this.cancelled) {
			File file = new File(string);
			String string3 = bl ? string2 : string2 + file.getName();
			TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(file, string3);
			tarArchiveOutputStream.putArchiveEntry(tarArchiveEntry);
			if (file.isFile()) {
				IOUtils.copy(new FileInputStream(file), tarArchiveOutputStream);
				tarArchiveOutputStream.closeArchiveEntry();
			} else {
				tarArchiveOutputStream.closeArchiveEntry();
				File[] files = file.listFiles();
				if (files != null) {
					for (File file2 : files) {
						this.addFileToTarGz(tarArchiveOutputStream, file2.getAbsolutePath(), string3 + "/", false);
					}
				}
			}
		}
	}
}

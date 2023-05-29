package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SymlinkWarningScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class WorldSelectionList extends ObjectSelectionList<WorldSelectionList.Entry> {
	static final Logger LOGGER = LogUtils.getLogger();
	static final DateFormat DATE_FORMAT = new SimpleDateFormat();
	private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
	static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/world_selection.png");
	static final Component FROM_NEWER_TOOLTIP_1 = Component.translatable("selectWorld.tooltip.fromNewerVersion1").withStyle(ChatFormatting.RED);
	static final Component FROM_NEWER_TOOLTIP_2 = Component.translatable("selectWorld.tooltip.fromNewerVersion2").withStyle(ChatFormatting.RED);
	static final Component SNAPSHOT_TOOLTIP_1 = Component.translatable("selectWorld.tooltip.snapshot1").withStyle(ChatFormatting.GOLD);
	static final Component SNAPSHOT_TOOLTIP_2 = Component.translatable("selectWorld.tooltip.snapshot2").withStyle(ChatFormatting.GOLD);
	static final Component WORLD_LOCKED_TOOLTIP = Component.translatable("selectWorld.locked").withStyle(ChatFormatting.RED);
	static final Component WORLD_REQUIRES_CONVERSION = Component.translatable("selectWorld.conversion.tooltip").withStyle(ChatFormatting.RED);
	private final SelectWorldScreen screen;
	private CompletableFuture<List<LevelSummary>> pendingLevels;
	@Nullable
	private List<LevelSummary> currentlyDisplayedLevels;
	private String filter;
	private final WorldSelectionList.LoadingHeader loadingHeader;

	public WorldSelectionList(
		SelectWorldScreen selectWorldScreen, Minecraft minecraft, int i, int j, int k, int l, int m, String string, @Nullable WorldSelectionList worldSelectionList
	) {
		super(minecraft, i, j, k, l, m);
		this.screen = selectWorldScreen;
		this.loadingHeader = new WorldSelectionList.LoadingHeader(minecraft);
		this.filter = string;
		if (worldSelectionList != null) {
			this.pendingLevels = worldSelectionList.pendingLevels;
		} else {
			this.pendingLevels = this.loadLevels();
		}

		this.handleNewLevels(this.pollLevelsIgnoreErrors());
	}

	@Override
	protected void clearEntries() {
		this.children().forEach(WorldSelectionList.Entry::close);
		super.clearEntries();
	}

	@Nullable
	private List<LevelSummary> pollLevelsIgnoreErrors() {
		try {
			return (List<LevelSummary>)this.pendingLevels.getNow(null);
		} catch (CancellationException | CompletionException var2) {
			return null;
		}
	}

	void reloadWorldList() {
		this.pendingLevels = this.loadLevels();
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (CommonInputs.selected(i)) {
			Optional<WorldSelectionList.WorldListEntry> optional = this.getSelectedOpt();
			if (optional.isPresent()) {
				((WorldSelectionList.WorldListEntry)optional.get()).joinWorld();
				return true;
			}
		}

		return super.keyPressed(i, j, k);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		List<LevelSummary> list = this.pollLevelsIgnoreErrors();
		if (list != this.currentlyDisplayedLevels) {
			this.handleNewLevels(list);
		}

		super.render(guiGraphics, i, j, f);
	}

	private void handleNewLevels(@Nullable List<LevelSummary> list) {
		if (list == null) {
			this.fillLoadingLevels();
		} else {
			this.fillLevels(this.filter, list);
		}

		this.currentlyDisplayedLevels = list;
	}

	public void updateFilter(String string) {
		if (this.currentlyDisplayedLevels != null && !string.equals(this.filter)) {
			this.fillLevels(string, this.currentlyDisplayedLevels);
		}

		this.filter = string;
	}

	private CompletableFuture<List<LevelSummary>> loadLevels() {
		LevelStorageSource.LevelCandidates levelCandidates;
		try {
			levelCandidates = this.minecraft.getLevelSource().findLevelCandidates();
		} catch (LevelStorageException var3) {
			LOGGER.error("Couldn't load level list", (Throwable)var3);
			this.handleLevelLoadFailure(var3.getMessageComponent());
			return CompletableFuture.completedFuture(List.of());
		}

		if (levelCandidates.isEmpty()) {
			CreateWorldScreen.openFresh(this.minecraft, null);
			return CompletableFuture.completedFuture(List.of());
		} else {
			return this.minecraft.getLevelSource().loadLevelSummaries(levelCandidates).exceptionally(throwable -> {
				this.minecraft.delayCrash(CrashReport.forThrowable(throwable, "Couldn't load level list"));
				return List.of();
			});
		}
	}

	private void fillLevels(String string, List<LevelSummary> list) {
		this.clearEntries();
		string = string.toLowerCase(Locale.ROOT);

		for (LevelSummary levelSummary : list) {
			if (this.filterAccepts(string, levelSummary)) {
				this.addEntry(new WorldSelectionList.WorldListEntry(this, levelSummary));
			}
		}

		this.notifyListUpdated();
	}

	private boolean filterAccepts(String string, LevelSummary levelSummary) {
		return levelSummary.getLevelName().toLowerCase(Locale.ROOT).contains(string) || levelSummary.getLevelId().toLowerCase(Locale.ROOT).contains(string);
	}

	private void fillLoadingLevels() {
		this.clearEntries();
		this.addEntry(this.loadingHeader);
		this.notifyListUpdated();
	}

	private void notifyListUpdated() {
		this.screen.triggerImmediateNarration(true);
	}

	private void handleLevelLoadFailure(Component component) {
		this.minecraft.setScreen(new ErrorScreen(Component.translatable("selectWorld.unable_to_load"), component));
	}

	@Override
	protected int getScrollbarPosition() {
		return super.getScrollbarPosition() + 20;
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 50;
	}

	public void setSelected(@Nullable WorldSelectionList.Entry entry) {
		super.setSelected(entry);
		this.screen.updateButtonStatus(entry != null && entry.isSelectable(), entry != null);
	}

	public Optional<WorldSelectionList.WorldListEntry> getSelectedOpt() {
		WorldSelectionList.Entry entry = this.getSelected();
		return entry instanceof WorldSelectionList.WorldListEntry worldListEntry ? Optional.of(worldListEntry) : Optional.empty();
	}

	public SelectWorldScreen getScreen() {
		return this.screen;
	}

	@Override
	public void updateNarration(NarrationElementOutput narrationElementOutput) {
		if (this.children().contains(this.loadingHeader)) {
			this.loadingHeader.updateNarration(narrationElementOutput);
		} else {
			super.updateNarration(narrationElementOutput);
		}
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry extends ObjectSelectionList.Entry<WorldSelectionList.Entry> implements AutoCloseable {
		public abstract boolean isSelectable();

		public void close() {
		}
	}

	@Environment(EnvType.CLIENT)
	public static class LoadingHeader extends WorldSelectionList.Entry {
		private static final Component LOADING_LABEL = Component.translatable("selectWorld.loading_list");
		private final Minecraft minecraft;

		public LoadingHeader(Minecraft minecraft) {
			this.minecraft = minecraft;
		}

		@Override
		public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			int p = (this.minecraft.screen.width - this.minecraft.font.width(LOADING_LABEL)) / 2;
			int q = j + (m - 9) / 2;
			guiGraphics.drawString(this.minecraft.font, LOADING_LABEL, p, q, 16777215, false);
			String string = LoadingDotsText.get(Util.getMillis());
			int r = (this.minecraft.screen.width - this.minecraft.font.width(string)) / 2;
			int s = q + 9;
			guiGraphics.drawString(this.minecraft.font, string, r, s, 8421504, false);
		}

		@Override
		public Component getNarration() {
			return LOADING_LABEL;
		}

		@Override
		public boolean isSelectable() {
			return false;
		}
	}

	@Environment(EnvType.CLIENT)
	public final class WorldListEntry extends WorldSelectionList.Entry implements AutoCloseable {
		private static final int ICON_WIDTH = 32;
		private static final int ICON_HEIGHT = 32;
		private static final int ICON_OVERLAY_X_JOIN = 0;
		private static final int ICON_OVERLAY_X_JOIN_WITH_NOTIFY = 32;
		private static final int ICON_OVERLAY_X_WARNING = 64;
		private static final int ICON_OVERLAY_X_ERROR = 96;
		private static final int ICON_OVERLAY_Y_UNSELECTED = 0;
		private static final int ICON_OVERLAY_Y_SELECTED = 32;
		private final Minecraft minecraft;
		private final SelectWorldScreen screen;
		private final LevelSummary summary;
		private final FaviconTexture icon;
		@Nullable
		private Path iconFile;
		private long lastClickTime;

		public WorldListEntry(WorldSelectionList worldSelectionList2, LevelSummary levelSummary) {
			this.minecraft = worldSelectionList2.minecraft;
			this.screen = worldSelectionList2.getScreen();
			this.summary = levelSummary;
			this.icon = FaviconTexture.forWorld(this.minecraft.getTextureManager(), levelSummary.getLevelId());
			this.iconFile = levelSummary.getIcon();
			this.validateIconFile();
			this.loadIcon();
		}

		private void validateIconFile() {
			if (this.iconFile != null) {
				try {
					BasicFileAttributes basicFileAttributes = Files.readAttributes(this.iconFile, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
					if (basicFileAttributes.isSymbolicLink()) {
						List<ForbiddenSymlinkInfo> list = new ArrayList();
						this.minecraft.getLevelSource().getWorldDirValidator().validateSymlink(this.iconFile, list);
						if (!list.isEmpty()) {
							WorldSelectionList.LOGGER.warn(ContentValidationException.getMessage(this.iconFile, list));
							this.iconFile = null;
						} else {
							basicFileAttributes = Files.readAttributes(this.iconFile, BasicFileAttributes.class);
						}
					}

					if (!basicFileAttributes.isRegularFile()) {
						this.iconFile = null;
					}
				} catch (NoSuchFileException var3) {
					this.iconFile = null;
				} catch (IOException var4) {
					WorldSelectionList.LOGGER.error("could not validate symlink", (Throwable)var4);
					this.iconFile = null;
				}
			}
		}

		@Override
		public Component getNarration() {
			Component component = Component.translatable(
				"narrator.select.world_info", this.summary.getLevelName(), new Date(this.summary.getLastPlayed()), this.summary.getInfo()
			);
			Component component2;
			if (this.summary.isLocked()) {
				component2 = CommonComponents.joinForNarration(component, WorldSelectionList.WORLD_LOCKED_TOOLTIP);
			} else {
				component2 = component;
			}

			return Component.translatable("narrator.select", component2);
		}

		@Override
		public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			String string = this.summary.getLevelName();
			String string2 = this.summary.getLevelId();
			long p = this.summary.getLastPlayed();
			if (p != -1L) {
				string2 = string2 + " (" + WorldSelectionList.DATE_FORMAT.format(new Date(p)) + ")";
			}

			if (StringUtils.isEmpty(string)) {
				string = I18n.get("selectWorld.world") + " " + (i + 1);
			}

			Component component = this.summary.getInfo();
			guiGraphics.drawString(this.minecraft.font, string, k + 32 + 3, j + 1, 16777215, false);
			guiGraphics.drawString(this.minecraft.font, string2, k + 32 + 3, j + 9 + 3, 8421504, false);
			guiGraphics.drawString(this.minecraft.font, component, k + 32 + 3, j + 9 + 9 + 3, 8421504, false);
			RenderSystem.enableBlend();
			guiGraphics.blit(this.icon.textureLocation(), k, j, 0.0F, 0.0F, 32, 32, 32, 32);
			RenderSystem.disableBlend();
			if (this.minecraft.options.touchscreen().get() || bl) {
				guiGraphics.fill(k, j, k + 32, j + 32, -1601138544);
				int q = n - k;
				boolean bl2 = q < 32;
				int r = bl2 ? 32 : 0;
				if (this.summary instanceof LevelSummary.SymlinkLevelSummary) {
					guiGraphics.blit(WorldSelectionList.ICON_OVERLAY_LOCATION, k, j, 96.0F, (float)r, 32, 32, 256, 256);
					guiGraphics.blit(WorldSelectionList.ICON_OVERLAY_LOCATION, k, j, 32.0F, (float)r, 32, 32, 256, 256);
					return;
				}

				if (this.summary.isLocked()) {
					guiGraphics.blit(WorldSelectionList.ICON_OVERLAY_LOCATION, k, j, 96.0F, (float)r, 32, 32, 256, 256);
					if (bl2) {
						this.screen.setTooltipForNextRenderPass(this.minecraft.font.split(WorldSelectionList.WORLD_LOCKED_TOOLTIP, 175));
					}
				} else if (this.summary.requiresManualConversion()) {
					guiGraphics.blit(WorldSelectionList.ICON_OVERLAY_LOCATION, k, j, 96.0F, (float)r, 32, 32, 256, 256);
					if (bl2) {
						this.screen.setTooltipForNextRenderPass(this.minecraft.font.split(WorldSelectionList.WORLD_REQUIRES_CONVERSION, 175));
					}
				} else if (this.summary.markVersionInList()) {
					guiGraphics.blit(WorldSelectionList.ICON_OVERLAY_LOCATION, k, j, 32.0F, (float)r, 32, 32, 256, 256);
					if (this.summary.askToOpenWorld()) {
						guiGraphics.blit(WorldSelectionList.ICON_OVERLAY_LOCATION, k, j, 96.0F, (float)r, 32, 32, 256, 256);
						if (bl2) {
							this.screen
								.setTooltipForNextRenderPass(
									ImmutableList.of(WorldSelectionList.FROM_NEWER_TOOLTIP_1.getVisualOrderText(), WorldSelectionList.FROM_NEWER_TOOLTIP_2.getVisualOrderText())
								);
						}
					} else if (!SharedConstants.getCurrentVersion().isStable()) {
						guiGraphics.blit(WorldSelectionList.ICON_OVERLAY_LOCATION, k, j, 64.0F, (float)r, 32, 32, 256, 256);
						if (bl2) {
							this.screen
								.setTooltipForNextRenderPass(
									ImmutableList.of(WorldSelectionList.SNAPSHOT_TOOLTIP_1.getVisualOrderText(), WorldSelectionList.SNAPSHOT_TOOLTIP_2.getVisualOrderText())
								);
						}
					}
				} else {
					guiGraphics.blit(WorldSelectionList.ICON_OVERLAY_LOCATION, k, j, 0.0F, (float)r, 32, 32, 256, 256);
				}
			}
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			if (this.summary.isDisabled()) {
				return true;
			} else {
				WorldSelectionList.this.setSelected((WorldSelectionList.Entry)this);
				if (d - (double)WorldSelectionList.this.getRowLeft() <= 32.0) {
					this.joinWorld();
					return true;
				} else if (Util.getMillis() - this.lastClickTime < 250L) {
					this.joinWorld();
					return true;
				} else {
					this.lastClickTime = Util.getMillis();
					return true;
				}
			}
		}

		public void joinWorld() {
			if (!this.summary.isDisabled()) {
				if (this.summary instanceof LevelSummary.SymlinkLevelSummary) {
					this.minecraft.setScreen(new SymlinkWarningScreen(this.screen));
				} else {
					LevelSummary.BackupStatus backupStatus = this.summary.backupStatus();
					if (backupStatus.shouldBackup()) {
						String string = "selectWorld.backupQuestion." + backupStatus.getTranslationKey();
						String string2 = "selectWorld.backupWarning." + backupStatus.getTranslationKey();
						MutableComponent mutableComponent = Component.translatable(string);
						if (backupStatus.isSevere()) {
							mutableComponent.withStyle(ChatFormatting.BOLD, ChatFormatting.RED);
						}

						Component component = Component.translatable(string2, this.summary.getWorldVersionName(), SharedConstants.getCurrentVersion().getName());
						this.minecraft.setScreen(new BackupConfirmScreen(this.screen, (bl, bl2) -> {
							if (bl) {
								String stringx = this.summary.getLevelId();

								try (LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().validateAndCreateAccess(stringx)) {
									EditWorldScreen.makeBackupAndShowToast(levelStorageAccess);
								} catch (IOException var9) {
									SystemToast.onWorldAccessFailure(this.minecraft, stringx);
									WorldSelectionList.LOGGER.error("Failed to backup level {}", stringx, var9);
								} catch (ContentValidationException var10) {
									WorldSelectionList.LOGGER.warn("{}", var10.getMessage());
									this.minecraft.setScreen(new SymlinkWarningScreen(this.screen));
								}
							}

							this.loadWorld();
						}, mutableComponent, component, false));
					} else if (this.summary.askToOpenWorld()) {
						this.minecraft
							.setScreen(
								new ConfirmScreen(
									bl -> {
										if (bl) {
											try {
												this.loadWorld();
											} catch (Exception var3x) {
												WorldSelectionList.LOGGER.error("Failure to open 'future world'", (Throwable)var3x);
												this.minecraft
													.setScreen(
														new AlertScreen(
															() -> this.minecraft.setScreen(this.screen),
															Component.translatable("selectWorld.futureworld.error.title"),
															Component.translatable("selectWorld.futureworld.error.text")
														)
													);
											}
										} else {
											this.minecraft.setScreen(this.screen);
										}
									},
									Component.translatable("selectWorld.versionQuestion"),
									Component.translatable("selectWorld.versionWarning", this.summary.getWorldVersionName()),
									Component.translatable("selectWorld.versionJoinButton"),
									CommonComponents.GUI_CANCEL
								)
							);
					} else {
						this.loadWorld();
					}
				}
			}
		}

		public void deleteWorld() {
			this.minecraft
				.setScreen(
					new ConfirmScreen(
						bl -> {
							if (bl) {
								this.minecraft.setScreen(new ProgressScreen(true));
								this.doDeleteWorld();
							}

							this.minecraft.setScreen(this.screen);
						},
						Component.translatable("selectWorld.deleteQuestion"),
						Component.translatable("selectWorld.deleteWarning", this.summary.getLevelName()),
						Component.translatable("selectWorld.deleteButton"),
						CommonComponents.GUI_CANCEL
					)
				);
		}

		public void doDeleteWorld() {
			LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
			String string = this.summary.getLevelId();

			try (LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.createAccess(string)) {
				levelStorageAccess.deleteLevel();
			} catch (IOException var8) {
				SystemToast.onWorldDeleteFailure(this.minecraft, string);
				WorldSelectionList.LOGGER.error("Failed to delete world {}", string, var8);
			}

			WorldSelectionList.this.reloadWorldList();
		}

		public void editWorld() {
			if (this.summary instanceof LevelSummary.SymlinkLevelSummary) {
				this.minecraft.setScreen(new SymlinkWarningScreen(this.screen));
			} else {
				this.queueLoadScreen();
				String string = this.summary.getLevelId();

				try {
					LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().validateAndCreateAccess(string);
					this.minecraft.setScreen(new EditWorldScreen(bl -> {
						try {
							levelStorageAccess.close();
						} catch (IOException var5) {
							WorldSelectionList.LOGGER.error("Failed to unlock level {}", string, var5);
						}

						if (bl) {
							WorldSelectionList.this.reloadWorldList();
						}

						this.minecraft.setScreen(this.screen);
					}, levelStorageAccess));
				} catch (IOException var3) {
					SystemToast.onWorldAccessFailure(this.minecraft, string);
					WorldSelectionList.LOGGER.error("Failed to access level {}", string, var3);
					WorldSelectionList.this.reloadWorldList();
				} catch (ContentValidationException var4) {
					WorldSelectionList.LOGGER.warn("{}", var4.getMessage());
					this.minecraft.setScreen(new SymlinkWarningScreen(this.screen));
				}
			}
		}

		public void recreateWorld() {
			if (this.summary instanceof LevelSummary.SymlinkLevelSummary) {
				this.minecraft.setScreen(new SymlinkWarningScreen(this.screen));
			} else {
				this.queueLoadScreen();

				try (LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().validateAndCreateAccess(this.summary.getLevelId())) {
					Pair<LevelSettings, WorldCreationContext> pair = this.minecraft.createWorldOpenFlows().recreateWorldData(levelStorageAccess);
					LevelSettings levelSettings = pair.getFirst();
					WorldCreationContext worldCreationContext = pair.getSecond();
					Path path = CreateWorldScreen.createTempDataPackDirFromExistingWorld(levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR), this.minecraft);
					if (worldCreationContext.options().isOldCustomizedWorld()) {
						this.minecraft
							.setScreen(
								new ConfirmScreen(
									bl -> this.minecraft
											.setScreen((Screen)(bl ? CreateWorldScreen.createFromExisting(this.minecraft, this.screen, levelSettings, worldCreationContext, path) : this.screen)),
									Component.translatable("selectWorld.recreate.customized.title"),
									Component.translatable("selectWorld.recreate.customized.text"),
									CommonComponents.GUI_PROCEED,
									CommonComponents.GUI_CANCEL
								)
							);
					} else {
						this.minecraft.setScreen(CreateWorldScreen.createFromExisting(this.minecraft, this.screen, levelSettings, worldCreationContext, path));
					}
				} catch (ContentValidationException var8) {
					WorldSelectionList.LOGGER.warn("{}", var8.getMessage());
					this.minecraft.setScreen(new SymlinkWarningScreen(this.screen));
				} catch (Exception var9) {
					WorldSelectionList.LOGGER.error("Unable to recreate world", (Throwable)var9);
					this.minecraft
						.setScreen(
							new AlertScreen(
								() -> this.minecraft.setScreen(this.screen),
								Component.translatable("selectWorld.recreate.error.title"),
								Component.translatable("selectWorld.recreate.error.text")
							)
						);
				}
			}
		}

		private void loadWorld() {
			this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			if (this.minecraft.getLevelSource().levelExists(this.summary.getLevelId())) {
				this.queueLoadScreen();
				this.minecraft.createWorldOpenFlows().loadLevel(this.screen, this.summary.getLevelId());
			}
		}

		private void queueLoadScreen() {
			this.minecraft.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("selectWorld.data_read")));
		}

		private void loadIcon() {
			boolean bl = this.iconFile != null && Files.isRegularFile(this.iconFile, new LinkOption[0]);
			if (bl) {
				try {
					InputStream inputStream = Files.newInputStream(this.iconFile);

					try {
						this.icon.upload(NativeImage.read(inputStream));
					} catch (Throwable var6) {
						if (inputStream != null) {
							try {
								inputStream.close();
							} catch (Throwable var5) {
								var6.addSuppressed(var5);
							}
						}

						throw var6;
					}

					if (inputStream != null) {
						inputStream.close();
					}
				} catch (Throwable var7) {
					WorldSelectionList.LOGGER.error("Invalid icon for world {}", this.summary.getLevelId(), var7);
					this.iconFile = null;
				}
			} else {
				this.icon.clear();
			}
		}

		@Override
		public void close() {
			this.icon.close();
		}

		public String getLevelName() {
			return this.summary.getLevelName();
		}

		@Override
		public boolean isSelectable() {
			return !this.summary.isDisabled();
		}
	}
}

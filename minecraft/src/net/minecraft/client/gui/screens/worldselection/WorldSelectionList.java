package net.minecraft.client.gui.screens.worldselection;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class WorldSelectionList extends ObjectSelectionList<WorldSelectionList.Entry> {
	static final Logger LOGGER = LogUtils.getLogger();
	static final DateFormat DATE_FORMAT = new SimpleDateFormat();
	static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
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
	public void render(PoseStack poseStack, int i, int j, float f) {
		List<LevelSummary> list = this.pollLevelsIgnoreErrors();
		if (list != this.currentlyDisplayedLevels) {
			this.handleNewLevels(list);
		}

		super.render(poseStack, i, j, f);
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
		this.screen.updateButtonStatus(entry != null && entry.isSelectable());
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
		public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			int p = (this.minecraft.screen.width - this.minecraft.font.width(LOADING_LABEL)) / 2;
			int q = j + (m - 9) / 2;
			this.minecraft.font.draw(poseStack, LOADING_LABEL, (float)p, (float)q, 16777215);
			String string = LoadingDotsText.get(Util.getMillis());
			int r = (this.minecraft.screen.width - this.minecraft.font.width(string)) / 2;
			int s = q + 9;
			this.minecraft.font.draw(poseStack, string, (float)r, (float)s, 8421504);
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
		private final ResourceLocation iconLocation;
		@Nullable
		private Path iconFile;
		@Nullable
		private final DynamicTexture icon;
		private long lastClickTime;

		public WorldListEntry(WorldSelectionList worldSelectionList2, LevelSummary levelSummary) {
			this.minecraft = worldSelectionList2.minecraft;
			this.screen = worldSelectionList2.getScreen();
			this.summary = levelSummary;
			String string = levelSummary.getLevelId();
			this.iconLocation = new ResourceLocation(
				"minecraft", "worlds/" + Util.sanitizeName(string, ResourceLocation::validPathChar) + "/" + Hashing.sha1().hashUnencodedChars(string) + "/icon"
			);
			this.iconFile = levelSummary.getIcon();
			if (!Files.isRegularFile(this.iconFile, new LinkOption[0])) {
				this.iconFile = null;
			}

			this.icon = this.loadServerIcon();
		}

		@Override
		public Component getNarration() {
			Component component = Component.translatable(
				"narrator.select.world",
				this.summary.getLevelName(),
				new Date(this.summary.getLastPlayed()),
				this.summary.isHardcore() ? Component.translatable("gameMode.hardcore") : Component.translatable("gameMode." + this.summary.getGameMode().getName()),
				this.summary.hasCheats() ? Component.translatable("selectWorld.cheats") : CommonComponents.EMPTY,
				this.summary.getWorldVersionName()
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
		public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			String string = this.summary.getLevelName();
			String string2 = this.summary.getLevelId() + " (" + WorldSelectionList.DATE_FORMAT.format(new Date(this.summary.getLastPlayed())) + ")";
			if (StringUtils.isEmpty(string)) {
				string = I18n.get("selectWorld.world") + " " + (i + 1);
			}

			Component component = this.summary.getInfo();
			this.minecraft.font.draw(poseStack, string, (float)(k + 32 + 3), (float)(j + 1), 16777215);
			this.minecraft.font.draw(poseStack, string2, (float)(k + 32 + 3), (float)(j + 9 + 3), 8421504);
			this.minecraft.font.draw(poseStack, component, (float)(k + 32 + 3), (float)(j + 9 + 9 + 3), 8421504);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, this.icon != null ? this.iconLocation : WorldSelectionList.ICON_MISSING);
			RenderSystem.enableBlend();
			GuiComponent.blit(poseStack, k, j, 0.0F, 0.0F, 32, 32, 32, 32);
			RenderSystem.disableBlend();
			if (this.minecraft.options.touchscreen().get() || bl) {
				RenderSystem.setShaderTexture(0, WorldSelectionList.ICON_OVERLAY_LOCATION);
				GuiComponent.fill(poseStack, k, j, k + 32, j + 32, -1601138544);
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				int p = n - k;
				boolean bl2 = p < 32;
				int q = bl2 ? 32 : 0;
				if (this.summary.isLocked()) {
					GuiComponent.blit(poseStack, k, j, 96.0F, (float)q, 32, 32, 256, 256);
					if (bl2) {
						this.screen.setTooltipForNextRenderPass(this.minecraft.font.split(WorldSelectionList.WORLD_LOCKED_TOOLTIP, 175));
					}
				} else if (this.summary.requiresManualConversion()) {
					GuiComponent.blit(poseStack, k, j, 96.0F, (float)q, 32, 32, 256, 256);
					if (bl2) {
						this.screen.setTooltipForNextRenderPass(this.minecraft.font.split(WorldSelectionList.WORLD_REQUIRES_CONVERSION, 175));
					}
				} else if (this.summary.markVersionInList()) {
					GuiComponent.blit(poseStack, k, j, 32.0F, (float)q, 32, 32, 256, 256);
					if (this.summary.askToOpenWorld()) {
						GuiComponent.blit(poseStack, k, j, 96.0F, (float)q, 32, 32, 256, 256);
						if (bl2) {
							this.screen
								.setTooltipForNextRenderPass(
									ImmutableList.of(WorldSelectionList.FROM_NEWER_TOOLTIP_1.getVisualOrderText(), WorldSelectionList.FROM_NEWER_TOOLTIP_2.getVisualOrderText())
								);
						}
					} else if (!SharedConstants.getCurrentVersion().isStable()) {
						GuiComponent.blit(poseStack, k, j, 64.0F, (float)q, 32, 32, 256, 256);
						if (bl2) {
							this.screen
								.setTooltipForNextRenderPass(
									ImmutableList.of(WorldSelectionList.SNAPSHOT_TOOLTIP_1.getVisualOrderText(), WorldSelectionList.SNAPSHOT_TOOLTIP_2.getVisualOrderText())
								);
						}
					}
				} else {
					GuiComponent.blit(poseStack, k, j, 0.0F, (float)q, 32, 32, 256, 256);
				}
			}
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			if (this.summary.isDisabled()) {
				return true;
			} else {
				WorldSelectionList.this.setSelected((WorldSelectionList.Entry)this);
				this.screen.updateButtonStatus(WorldSelectionList.this.getSelectedOpt().isPresent());
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

							try (LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess(stringx)) {
								EditWorldScreen.makeBackupAndShowToast(levelStorageAccess);
							} catch (IOException var9) {
								SystemToast.onWorldAccessFailure(this.minecraft, stringx);
								WorldSelectionList.LOGGER.error("Failed to backup level {}", stringx, var9);
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
			this.queueLoadScreen();
			String string = this.summary.getLevelId();

			try {
				LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess(string);
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
			}
		}

		public void recreateWorld() {
			this.queueLoadScreen();

			try (LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess(this.summary.getLevelId())) {
				Pair<LevelSettings, WorldCreationContext> pair = this.minecraft.createWorldOpenFlows().recreateWorldData(levelStorageAccess);
				LevelSettings levelSettings = pair.getFirst();
				WorldCreationContext worldCreationContext = pair.getSecond();
				Path path = CreateWorldScreen.createTempDataPackDirFromExistingWorld(levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR), this.minecraft);
				if (worldCreationContext.options().isOldCustomizedWorld()) {
					this.minecraft
						.setScreen(
							new ConfirmScreen(
								bl -> this.minecraft
										.setScreen((Screen)(bl ? CreateWorldScreen.createFromExisting(this.screen, levelSettings, worldCreationContext, path) : this.screen)),
								Component.translatable("selectWorld.recreate.customized.title"),
								Component.translatable("selectWorld.recreate.customized.text"),
								CommonComponents.GUI_PROCEED,
								CommonComponents.GUI_CANCEL
							)
						);
				} else {
					this.minecraft.setScreen(CreateWorldScreen.createFromExisting(this.screen, levelSettings, worldCreationContext, path));
				}
			} catch (Exception var8) {
				WorldSelectionList.LOGGER.error("Unable to recreate world", (Throwable)var8);
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

		@Nullable
		private DynamicTexture loadServerIcon() {
			boolean bl = this.iconFile != null && Files.isRegularFile(this.iconFile, new LinkOption[0]);
			if (bl) {
				try {
					InputStream inputStream = Files.newInputStream(this.iconFile);

					DynamicTexture var5;
					try {
						NativeImage nativeImage = NativeImage.read(inputStream);
						Preconditions.checkState(nativeImage.getWidth() == 64, "Must be 64 pixels wide");
						Preconditions.checkState(nativeImage.getHeight() == 64, "Must be 64 pixels high");
						DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
						this.minecraft.getTextureManager().register(this.iconLocation, dynamicTexture);
						var5 = dynamicTexture;
					} catch (Throwable var7) {
						if (inputStream != null) {
							try {
								inputStream.close();
							} catch (Throwable var6) {
								var7.addSuppressed(var6);
							}
						}

						throw var7;
					}

					if (inputStream != null) {
						inputStream.close();
					}

					return var5;
				} catch (Throwable var8) {
					WorldSelectionList.LOGGER.error("Invalid icon for world {}", this.summary.getLevelId(), var8);
					this.iconFile = null;
					return null;
				}
			} else {
				this.minecraft.getTextureManager().release(this.iconLocation);
				return null;
			}
		}

		@Override
		public void close() {
			if (this.icon != null) {
				this.icon.close();
			}
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

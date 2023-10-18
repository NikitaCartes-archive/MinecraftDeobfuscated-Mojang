package net.minecraft.client.gui.screens;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RecoverWorldDataScreen extends Screen {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int SCREEN_SIDE_MARGIN = 25;
	private static final Component TITLE = Component.translatable("recover_world.title").withStyle(ChatFormatting.BOLD);
	private static final Component BUGTRACKER_BUTTON = Component.translatable("recover_world.bug_tracker");
	private static final Component RESTORE_BUTTON = Component.translatable("recover_world.restore");
	private static final Component NO_FALLBACK_TOOLTIP = Component.translatable("recover_world.no_fallback");
	private static final Component DONE_TITLE = Component.translatable("recover_world.done.title");
	private static final Component DONE_SUCCESS = Component.translatable("recover_world.done.success");
	private static final Component DONE_FAILED = Component.translatable("recover_world.done.failed");
	private static final Component NO_ISSUES = Component.translatable("recover_world.issue.none").withStyle(ChatFormatting.GREEN);
	private static final Component MISSING_FILE = Component.translatable("recover_world.issue.missing_file").withStyle(ChatFormatting.RED);
	private final BooleanConsumer callback;
	private final LinearLayout layout = LinearLayout.vertical().spacing(10);
	private final Component message;
	private final MultiLineTextWidget messageWidget;
	private final MultiLineTextWidget issuesWidget;
	private final LevelStorageSource.LevelStorageAccess storageAccess;

	public RecoverWorldDataScreen(Minecraft minecraft, BooleanConsumer booleanConsumer, LevelStorageSource.LevelStorageAccess levelStorageAccess) {
		super(TITLE);
		this.callback = booleanConsumer;
		this.message = Component.translatable("recover_world.message", Component.literal(levelStorageAccess.getLevelId()).withStyle(ChatFormatting.GRAY));
		this.messageWidget = new MultiLineTextWidget(this.message, minecraft.font);
		this.storageAccess = levelStorageAccess;
		Exception exception = this.collectIssue(levelStorageAccess, false);
		Exception exception2 = this.collectIssue(levelStorageAccess, true);
		Component component = Component.empty()
			.append(this.buildInfo(levelStorageAccess, false, exception))
			.append("\n")
			.append(this.buildInfo(levelStorageAccess, true, exception2));
		this.issuesWidget = new MultiLineTextWidget(component, minecraft.font);
		boolean bl = exception != null && exception2 == null;
		this.layout.defaultCellSetting().alignHorizontallyCenter();
		this.layout.addChild(new StringWidget(this.title, minecraft.font));
		this.layout.addChild(this.messageWidget.setCentered(true));
		this.layout.addChild(this.issuesWidget);
		LinearLayout linearLayout = LinearLayout.horizontal().spacing(5);
		linearLayout.addChild(Button.builder(BUGTRACKER_BUTTON, ConfirmLinkScreen.confirmLink(this, "https://aka.ms/snapshotbugs?ref=game")).size(120, 20).build());
		linearLayout.addChild(
				Button.builder(RESTORE_BUTTON, button -> this.attemptRestore(minecraft)).size(120, 20).tooltip(bl ? null : Tooltip.create(NO_FALLBACK_TOOLTIP)).build()
			)
			.active = bl;
		this.layout.addChild(linearLayout);
		this.layout.addChild(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).size(120, 20).build());
		this.layout.visitWidgets(this::addRenderableWidget);
	}

	private void attemptRestore(Minecraft minecraft) {
		Exception exception = this.collectIssue(this.storageAccess, false);
		Exception exception2 = this.collectIssue(this.storageAccess, true);
		if (exception != null && exception2 == null) {
			minecraft.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("recover_world.restoring")));
			EditWorldScreen.makeBackupAndShowToast(this.storageAccess);
			if (this.storageAccess.restoreLevelDataFromOld()) {
				minecraft.setScreen(new ConfirmScreen(this.callback, DONE_TITLE, DONE_SUCCESS, CommonComponents.GUI_CONTINUE, CommonComponents.GUI_BACK));
			} else {
				minecraft.setScreen(new AlertScreen(() -> this.callback.accept(false), DONE_TITLE, DONE_FAILED));
			}
		} else {
			LOGGER.error(
				"Failed to recover world, files not as expected. level.dat: {}, level.dat_old: {}",
				exception != null ? exception.getMessage() : "no issues",
				exception2 != null ? exception2.getMessage() : "no issues"
			);
			minecraft.setScreen(new AlertScreen(() -> this.callback.accept(false), DONE_TITLE, DONE_FAILED));
		}
	}

	private Component buildInfo(LevelStorageSource.LevelStorageAccess levelStorageAccess, boolean bl, @Nullable Exception exception) {
		if (bl && exception instanceof FileNotFoundException) {
			return Component.empty();
		} else {
			MutableComponent mutableComponent = Component.empty();
			Instant instant = levelStorageAccess.getFileModificationTime(bl);
			MutableComponent mutableComponent2 = instant != null
				? Component.literal(WorldSelectionList.DATE_FORMAT.format(instant))
				: Component.translatable("recover_world.state_entry.unknown");
			mutableComponent.append(Component.translatable("recover_world.state_entry", mutableComponent2.withStyle(ChatFormatting.GRAY)));
			if (exception == null) {
				mutableComponent.append(NO_ISSUES);
			} else if (exception instanceof FileNotFoundException) {
				mutableComponent.append(MISSING_FILE);
			} else if (exception instanceof ReportedNbtException) {
				mutableComponent.append(Component.literal(exception.getCause().toString()).withStyle(ChatFormatting.RED));
			} else {
				mutableComponent.append(Component.literal(exception.toString()).withStyle(ChatFormatting.RED));
			}

			return mutableComponent;
		}
	}

	@Nullable
	private Exception collectIssue(LevelStorageSource.LevelStorageAccess levelStorageAccess, boolean bl) {
		try {
			if (!bl) {
				levelStorageAccess.getSummary(levelStorageAccess.getDataTag());
			} else {
				levelStorageAccess.getSummary(levelStorageAccess.getDataTagFallback());
			}

			return null;
		} catch (NbtException | ReportedNbtException | IOException var4) {
			return var4;
		}
	}

	@Override
	protected void init() {
		super.init();
		this.repositionElements();
	}

	@Override
	protected void repositionElements() {
		this.issuesWidget.setMaxWidth(this.width - 50);
		this.messageWidget.setMaxWidth(this.width - 50);
		this.layout.arrangeElements();
		FrameLayout.centerInRectangle(this.layout, this.getRectangle());
	}

	@Override
	public Component getNarrationMessage() {
		return CommonComponents.joinForNarration(super.getNarrationMessage(), this.message);
	}

	@Override
	public void onClose() {
		this.callback.accept(false);
	}
}

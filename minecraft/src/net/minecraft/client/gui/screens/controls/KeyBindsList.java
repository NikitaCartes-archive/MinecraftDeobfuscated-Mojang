package net.minecraft.client.gui.screens.controls;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.lang3.ArrayUtils;

@Environment(EnvType.CLIENT)
public class KeyBindsList extends ContainerObjectSelectionList<KeyBindsList.Entry> {
	final KeyBindsScreen keyBindsScreen;
	int maxNameWidth;

	public KeyBindsList(KeyBindsScreen keyBindsScreen, Minecraft minecraft) {
		super(minecraft, keyBindsScreen.width + 45, keyBindsScreen.height, 20, keyBindsScreen.height - 32, 20);
		this.keyBindsScreen = keyBindsScreen;
		KeyMapping[] keyMappings = ArrayUtils.clone((KeyMapping[])minecraft.options.keyMappings);
		Arrays.sort(keyMappings);
		String string = null;

		for (KeyMapping keyMapping : keyMappings) {
			String string2 = keyMapping.getCategory();
			if (!string2.equals(string)) {
				string = string2;
				this.addEntry(new KeyBindsList.CategoryEntry(Component.translatable(string2)));
			}

			Component component = Component.translatable(keyMapping.getName());
			int i = minecraft.font.width(component);
			if (i > this.maxNameWidth) {
				this.maxNameWidth = i;
			}

			this.addEntry(new KeyBindsList.KeyEntry(keyMapping, component));
		}
	}

	public void resetMappingAndUpdateButtons() {
		KeyMapping.resetMapping();
		this.refreshEntries();
	}

	public void refreshEntries() {
		this.children().forEach(KeyBindsList.Entry::refreshEntry);
	}

	@Override
	protected int getScrollbarPosition() {
		return super.getScrollbarPosition() + 15;
	}

	@Override
	public int getRowWidth() {
		return super.getRowWidth() + 32;
	}

	@Environment(EnvType.CLIENT)
	public class CategoryEntry extends KeyBindsList.Entry {
		final Component name;
		private final int width;

		public CategoryEntry(Component component) {
			this.name = component;
			this.width = KeyBindsList.this.minecraft.font.width(this.name);
		}

		@Override
		public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			KeyBindsList.this.minecraft
				.font
				.draw(poseStack, this.name, (float)(KeyBindsList.this.minecraft.screen.width / 2 - this.width / 2), (float)(j + m - 9 - 1), 16777215);
		}

		@Nullable
		@Override
		public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
			return null;
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return Collections.emptyList();
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return ImmutableList.of(new NarratableEntry() {
				@Override
				public NarratableEntry.NarrationPriority narrationPriority() {
					return NarratableEntry.NarrationPriority.HOVERED;
				}

				@Override
				public void updateNarration(NarrationElementOutput narrationElementOutput) {
					narrationElementOutput.add(NarratedElementType.TITLE, CategoryEntry.this.name);
				}
			});
		}

		@Override
		protected void refreshEntry() {
		}
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry extends ContainerObjectSelectionList.Entry<KeyBindsList.Entry> {
		abstract void refreshEntry();
	}

	@Environment(EnvType.CLIENT)
	public class KeyEntry extends KeyBindsList.Entry {
		private final KeyMapping key;
		private final Component name;
		private final Button changeButton;
		private final Button resetButton;
		private boolean hasCollision = false;

		KeyEntry(KeyMapping keyMapping, Component component) {
			this.key = keyMapping;
			this.name = component;
			this.changeButton = Button.builder(component, button -> {
					KeyBindsList.this.keyBindsScreen.selectedKey = keyMapping;
					KeyBindsList.this.resetMappingAndUpdateButtons();
				})
				.bounds(0, 0, 75, 20)
				.createNarration(
					supplier -> keyMapping.isUnbound()
							? Component.translatable("narrator.controls.unbound", component)
							: Component.translatable("narrator.controls.bound", component, supplier.get())
				)
				.build();
			this.resetButton = Button.builder(Component.translatable("controls.reset"), button -> {
				KeyBindsList.this.minecraft.options.setKey(keyMapping, keyMapping.getDefaultKey());
				KeyBindsList.this.resetMappingAndUpdateButtons();
			}).bounds(0, 0, 50, 20).createNarration(supplier -> Component.translatable("narrator.controls.reset", component)).build();
			this.refreshEntry();
		}

		@Override
		public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			float var10003 = (float)(k + 90 - KeyBindsList.this.maxNameWidth);
			KeyBindsList.this.minecraft.font.draw(poseStack, this.name, var10003, (float)(j + m / 2 - 9 / 2), 16777215);
			this.resetButton.setX(k + 190);
			this.resetButton.setY(j);
			this.resetButton.render(poseStack, n, o, f);
			this.changeButton.setX(k + 105);
			this.changeButton.setY(j);
			if (this.hasCollision) {
				int p = 3;
				int q = this.changeButton.getX() - 6;
				GuiComponent.fill(poseStack, q, j + 2, q + 3, j + m + 2, ChatFormatting.RED.getColor() | 0xFF000000);
			}

			this.changeButton.render(poseStack, n, o, f);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return ImmutableList.of(this.changeButton, this.resetButton);
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return ImmutableList.of(this.changeButton, this.resetButton);
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			return this.changeButton.mouseClicked(d, e, i) ? true : this.resetButton.mouseClicked(d, e, i);
		}

		@Override
		public boolean mouseReleased(double d, double e, int i) {
			return this.changeButton.mouseReleased(d, e, i) || this.resetButton.mouseReleased(d, e, i);
		}

		@Override
		protected void refreshEntry() {
			this.changeButton.setMessage(this.key.getTranslatedKeyMessage());
			this.resetButton.active = !this.key.isDefault();
			this.hasCollision = false;
			MutableComponent mutableComponent = Component.empty();
			if (!this.key.isUnbound()) {
				for (KeyMapping keyMapping : KeyBindsList.this.minecraft.options.keyMappings) {
					if (keyMapping != this.key && this.key.same(keyMapping)) {
						if (this.hasCollision) {
							mutableComponent.append(", ");
						}

						this.hasCollision = true;
						mutableComponent.append(Component.translatable(keyMapping.getName()));
					}
				}
			}

			if (this.hasCollision) {
				this.changeButton
					.setMessage(
						Component.literal("[ ").append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.WHITE)).append(" ]").withStyle(ChatFormatting.RED)
					);
				this.changeButton.setTooltip(Tooltip.create(Component.translatable("controls.keybinds.duplicateKeybinds", mutableComponent)));
			} else {
				this.changeButton.setTooltip(null);
			}

			if (KeyBindsList.this.keyBindsScreen.selectedKey == this.key) {
				this.changeButton
					.setMessage(
						Component.literal("> ")
							.append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
							.append(" <")
							.withStyle(ChatFormatting.YELLOW)
					);
			}
		}
	}
}

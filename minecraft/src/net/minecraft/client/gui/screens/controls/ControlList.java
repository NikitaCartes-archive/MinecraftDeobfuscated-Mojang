package net.minecraft.client.gui.screens.controls;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.commons.lang3.ArrayUtils;

@Environment(EnvType.CLIENT)
public class ControlList extends ContainerObjectSelectionList<ControlList.Entry> {
	final ControlsScreen controlsScreen;
	int maxNameWidth;

	public ControlList(ControlsScreen controlsScreen, Minecraft minecraft) {
		super(minecraft, controlsScreen.width + 45, controlsScreen.height, 43, controlsScreen.height - 32, 20);
		this.controlsScreen = controlsScreen;
		KeyMapping[] keyMappings = ArrayUtils.clone(minecraft.options.keyMappings);
		Arrays.sort(keyMappings);
		String string = null;

		for (KeyMapping keyMapping : keyMappings) {
			String string2 = keyMapping.getCategory();
			if (!string2.equals(string)) {
				string = string2;
				this.addEntry(new ControlList.CategoryEntry(new TranslatableComponent(string2)));
			}

			Component component = new TranslatableComponent(keyMapping.getName());
			int i = minecraft.font.width(component);
			if (i > this.maxNameWidth) {
				this.maxNameWidth = i;
			}

			this.addEntry(new ControlList.KeyEntry(keyMapping, component));
		}
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
	public class CategoryEntry extends ControlList.Entry {
		final Component name;
		private final int width;

		public CategoryEntry(Component component) {
			this.name = component;
			this.width = ControlList.this.minecraft.font.width(this.name);
		}

		@Override
		public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			ControlList.this.minecraft
				.font
				.draw(poseStack, this.name, (float)(ControlList.this.minecraft.screen.width / 2 - this.width / 2), (float)(j + m - 9 - 1), 16777215);
		}

		@Override
		public boolean changeFocus(boolean bl) {
			return false;
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
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry extends ContainerObjectSelectionList.Entry<ControlList.Entry> {
	}

	@Environment(EnvType.CLIENT)
	public class KeyEntry extends ControlList.Entry {
		private final KeyMapping key;
		private final Component name;
		private final Button changeButton;
		private final Button resetButton;

		KeyEntry(KeyMapping keyMapping, Component component) {
			this.key = keyMapping;
			this.name = component;
			this.changeButton = new Button(0, 0, 75, 20, component, button -> ControlList.this.controlsScreen.selectedKey = keyMapping) {
				@Override
				protected MutableComponent createNarrationMessage() {
					return keyMapping.isUnbound()
						? new TranslatableComponent("narrator.controls.unbound", component)
						: new TranslatableComponent("narrator.controls.bound", component, super.createNarrationMessage());
				}
			};
			this.resetButton = new Button(0, 0, 50, 20, new TranslatableComponent("controls.reset"), button -> {
				ControlList.this.minecraft.options.setKey(keyMapping, keyMapping.getDefaultKey());
				KeyMapping.resetMapping();
			}) {
				@Override
				protected MutableComponent createNarrationMessage() {
					return new TranslatableComponent("narrator.controls.reset", component);
				}
			};
		}

		@Override
		public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			boolean bl2 = ControlList.this.controlsScreen.selectedKey == this.key;
			float var10003 = (float)(k + 90 - ControlList.this.maxNameWidth);
			ControlList.this.minecraft.font.draw(poseStack, this.name, var10003, (float)(j + m / 2 - 9 / 2), 16777215);
			this.resetButton.x = k + 190;
			this.resetButton.y = j;
			this.resetButton.active = !this.key.isDefault();
			this.resetButton.render(poseStack, n, o, f);
			this.changeButton.x = k + 105;
			this.changeButton.y = j;
			this.changeButton.setMessage(this.key.getTranslatedKeyMessage());
			boolean bl3 = false;
			if (!this.key.isUnbound()) {
				for (KeyMapping keyMapping : ControlList.this.minecraft.options.keyMappings) {
					if (keyMapping != this.key && this.key.same(keyMapping)) {
						bl3 = true;
						break;
					}
				}
			}

			if (bl2) {
				this.changeButton
					.setMessage(
						new TextComponent("> ").append(this.changeButton.getMessage().copy().withStyle(ChatFormatting.YELLOW)).append(" <").withStyle(ChatFormatting.YELLOW)
					);
			} else if (bl3) {
				this.changeButton.setMessage(this.changeButton.getMessage().copy().withStyle(ChatFormatting.RED));
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
	}
}

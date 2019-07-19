package net.minecraft.client.gui.screens.controls;

import com.google.common.collect.ImmutableList;
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
import net.minecraft.client.resources.language.I18n;
import org.apache.commons.lang3.ArrayUtils;

@Environment(EnvType.CLIENT)
public class ControlList extends ContainerObjectSelectionList<ControlList.Entry> {
	private final ControlsScreen controlsScreen;
	private int maxNameWidth;

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
				this.addEntry(new ControlList.CategoryEntry(string2));
			}

			int i = minecraft.font.width(I18n.get(keyMapping.getName()));
			if (i > this.maxNameWidth) {
				this.maxNameWidth = i;
			}

			this.addEntry(new ControlList.KeyEntry(keyMapping));
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
		private final String name;
		private final int width;

		public CategoryEntry(String string) {
			this.name = I18n.get(string);
			this.width = ControlList.this.minecraft.font.width(this.name);
		}

		@Override
		public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			ControlList.this.minecraft.font.draw(this.name, (float)(ControlList.this.minecraft.screen.width / 2 - this.width / 2), (float)(j + m - 9 - 1), 16777215);
		}

		@Override
		public boolean changeFocus(boolean bl) {
			return false;
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return Collections.emptyList();
		}
	}

	@Environment(EnvType.CLIENT)
	public abstract static class Entry extends ContainerObjectSelectionList.Entry<ControlList.Entry> {
	}

	@Environment(EnvType.CLIENT)
	public class KeyEntry extends ControlList.Entry {
		private final KeyMapping key;
		private final String name;
		private final Button changeButton;
		private final Button resetButton;

		private KeyEntry(KeyMapping keyMapping) {
			this.key = keyMapping;
			this.name = I18n.get(keyMapping.getName());
			this.changeButton = new Button(0, 0, 75, 20, this.name, button -> ControlList.this.controlsScreen.selectedKey = keyMapping) {
				@Override
				protected String getNarrationMessage() {
					return keyMapping.isUnbound()
						? I18n.get("narrator.controls.unbound", KeyEntry.this.name)
						: I18n.get("narrator.controls.bound", KeyEntry.this.name, super.getNarrationMessage());
				}
			};
			this.resetButton = new Button(0, 0, 50, 20, I18n.get("controls.reset"), button -> {
				ControlList.this.minecraft.options.setKey(keyMapping, keyMapping.getDefaultKey());
				KeyMapping.resetMapping();
			}) {
				@Override
				protected String getNarrationMessage() {
					return I18n.get("narrator.controls.reset", KeyEntry.this.name);
				}
			};
		}

		@Override
		public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			boolean bl2 = ControlList.this.controlsScreen.selectedKey == this.key;
			ControlList.this.minecraft.font.draw(this.name, (float)(k + 90 - ControlList.this.maxNameWidth), (float)(j + m / 2 - 9 / 2), 16777215);
			this.resetButton.x = k + 190;
			this.resetButton.y = j;
			this.resetButton.active = !this.key.isDefault();
			this.resetButton.render(n, o, f);
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
				this.changeButton.setMessage(ChatFormatting.WHITE + "> " + ChatFormatting.YELLOW + this.changeButton.getMessage() + ChatFormatting.WHITE + " <");
			} else if (bl3) {
				this.changeButton.setMessage(ChatFormatting.RED + this.changeButton.getMessage());
			}

			this.changeButton.render(n, o, f);
		}

		@Override
		public List<? extends GuiEventListener> children() {
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

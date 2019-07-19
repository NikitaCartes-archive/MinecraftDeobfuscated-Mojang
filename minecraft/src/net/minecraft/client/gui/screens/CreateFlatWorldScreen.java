package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

@Environment(EnvType.CLIENT)
public class CreateFlatWorldScreen extends Screen {
	private final CreateWorldScreen parent;
	private FlatLevelGeneratorSettings generator = FlatLevelGeneratorSettings.getDefault();
	private String columnType;
	private String columnHeight;
	private CreateFlatWorldScreen.DetailsList list;
	private Button deleteLayerButton;

	public CreateFlatWorldScreen(CreateWorldScreen createWorldScreen, CompoundTag compoundTag) {
		super(new TranslatableComponent("createWorld.customize.flat.title"));
		this.parent = createWorldScreen;
		this.loadLayers(compoundTag);
	}

	public String saveLayerString() {
		return this.generator.toString();
	}

	public CompoundTag saveLayers() {
		return (CompoundTag)this.generator.toObject(NbtOps.INSTANCE).getValue();
	}

	public void loadLayers(String string) {
		this.generator = FlatLevelGeneratorSettings.fromString(string);
	}

	public void loadLayers(CompoundTag compoundTag) {
		this.generator = FlatLevelGeneratorSettings.fromObject(new Dynamic<>(NbtOps.INSTANCE, compoundTag));
	}

	@Override
	protected void init() {
		this.columnType = I18n.get("createWorld.customize.flat.tile");
		this.columnHeight = I18n.get("createWorld.customize.flat.height");
		this.list = new CreateFlatWorldScreen.DetailsList();
		this.children.add(this.list);
		this.deleteLayerButton = this.addButton(
			new Button(this.width / 2 - 155, this.height - 52, 150, 20, I18n.get("createWorld.customize.flat.removeLayer"), button -> {
				if (this.hasValidSelection()) {
					List<FlatLayerInfo> list = this.generator.getLayersInfo();
					int i = this.list.children().indexOf(this.list.getSelected());
					int j = list.size() - i - 1;
					list.remove(j);
					this.list.setSelected(list.isEmpty() ? null : (CreateFlatWorldScreen.DetailsList.Entry)this.list.children().get(Math.min(i, list.size() - 1)));
					this.generator.updateLayers();
					this.updateButtonValidity();
				}
			})
		);
		this.addButton(new Button(this.width / 2 + 5, this.height - 52, 150, 20, I18n.get("createWorld.customize.presets"), button -> {
			this.minecraft.setScreen(new PresetFlatWorldScreen(this));
			this.generator.updateLayers();
			this.updateButtonValidity();
		}));
		this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, I18n.get("gui.done"), button -> {
			this.parent.levelTypeOptions = this.saveLayers();
			this.minecraft.setScreen(this.parent);
			this.generator.updateLayers();
			this.updateButtonValidity();
		}));
		this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, I18n.get("gui.cancel"), button -> {
			this.minecraft.setScreen(this.parent);
			this.generator.updateLayers();
			this.updateButtonValidity();
		}));
		this.generator.updateLayers();
		this.updateButtonValidity();
	}

	public void updateButtonValidity() {
		this.deleteLayerButton.active = this.hasValidSelection();
		this.list.resetRows();
	}

	private boolean hasValidSelection() {
		return this.list.getSelected() != null;
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.list.render(i, j, f);
		this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 8, 16777215);
		int k = this.width / 2 - 92 - 16;
		this.drawString(this.font, this.columnType, k, 32, 16777215);
		this.drawString(this.font, this.columnHeight, k + 2 + 213 - this.font.width(this.columnHeight), 32, 16777215);
		super.render(i, j, f);
	}

	@Environment(EnvType.CLIENT)
	class DetailsList extends ObjectSelectionList<CreateFlatWorldScreen.DetailsList.Entry> {
		public DetailsList() {
			super(
				CreateFlatWorldScreen.this.minecraft, CreateFlatWorldScreen.this.width, CreateFlatWorldScreen.this.height, 43, CreateFlatWorldScreen.this.height - 60, 24
			);

			for (int i = 0; i < CreateFlatWorldScreen.this.generator.getLayersInfo().size(); i++) {
				this.addEntry(new CreateFlatWorldScreen.DetailsList.Entry());
			}
		}

		public void setSelected(@Nullable CreateFlatWorldScreen.DetailsList.Entry entry) {
			super.setSelected(entry);
			if (entry != null) {
				FlatLayerInfo flatLayerInfo = (FlatLayerInfo)CreateFlatWorldScreen.this.generator
					.getLayersInfo()
					.get(CreateFlatWorldScreen.this.generator.getLayersInfo().size() - this.children().indexOf(entry) - 1);
				Item item = flatLayerInfo.getBlockState().getBlock().asItem();
				if (item != Items.AIR) {
					NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("narrator.select", item.getName(new ItemStack(item))).getString());
				}
			}
		}

		@Override
		protected void moveSelection(int i) {
			super.moveSelection(i);
			CreateFlatWorldScreen.this.updateButtonValidity();
		}

		@Override
		protected boolean isFocused() {
			return CreateFlatWorldScreen.this.getFocused() == this;
		}

		@Override
		protected int getScrollbarPosition() {
			return this.width - 70;
		}

		public void resetRows() {
			int i = this.children().indexOf(this.getSelected());
			this.clearEntries();

			for (int j = 0; j < CreateFlatWorldScreen.this.generator.getLayersInfo().size(); j++) {
				this.addEntry(new CreateFlatWorldScreen.DetailsList.Entry());
			}

			List<CreateFlatWorldScreen.DetailsList.Entry> list = this.children();
			if (i >= 0 && i < list.size()) {
				this.setSelected((CreateFlatWorldScreen.DetailsList.Entry)list.get(i));
			}
		}

		@Environment(EnvType.CLIENT)
		class Entry extends ObjectSelectionList.Entry<CreateFlatWorldScreen.DetailsList.Entry> {
			private Entry() {
			}

			@Override
			public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				FlatLayerInfo flatLayerInfo = (FlatLayerInfo)CreateFlatWorldScreen.this.generator
					.getLayersInfo()
					.get(CreateFlatWorldScreen.this.generator.getLayersInfo().size() - i - 1);
				BlockState blockState = flatLayerInfo.getBlockState();
				Block block = blockState.getBlock();
				Item item = block.asItem();
				if (item == Items.AIR) {
					if (block == Blocks.WATER) {
						item = Items.WATER_BUCKET;
					} else if (block == Blocks.LAVA) {
						item = Items.LAVA_BUCKET;
					}
				}

				ItemStack itemStack = new ItemStack(item);
				String string = item.getName(itemStack).getColoredString();
				this.blitSlot(k, j, itemStack);
				CreateFlatWorldScreen.this.font.draw(string, (float)(k + 18 + 5), (float)(j + 3), 16777215);
				String string2;
				if (i == 0) {
					string2 = I18n.get("createWorld.customize.flat.layer.top", flatLayerInfo.getHeight());
				} else if (i == CreateFlatWorldScreen.this.generator.getLayersInfo().size() - 1) {
					string2 = I18n.get("createWorld.customize.flat.layer.bottom", flatLayerInfo.getHeight());
				} else {
					string2 = I18n.get("createWorld.customize.flat.layer", flatLayerInfo.getHeight());
				}

				CreateFlatWorldScreen.this.font.draw(string2, (float)(k + 2 + 213 - CreateFlatWorldScreen.this.font.width(string2)), (float)(j + 3), 16777215);
			}

			@Override
			public boolean mouseClicked(double d, double e, int i) {
				if (i == 0) {
					DetailsList.this.setSelected(this);
					CreateFlatWorldScreen.this.updateButtonValidity();
					return true;
				} else {
					return false;
				}
			}

			private void blitSlot(int i, int j, ItemStack itemStack) {
				this.blitSlotBg(i + 1, j + 1);
				GlStateManager.enableRescaleNormal();
				if (!itemStack.isEmpty()) {
					Lighting.turnOnGui();
					CreateFlatWorldScreen.this.itemRenderer.renderGuiItem(itemStack, i + 2, j + 2);
					Lighting.turnOff();
				}

				GlStateManager.disableRescaleNormal();
			}

			private void blitSlotBg(int i, int j) {
				GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				DetailsList.this.minecraft.getTextureManager().bind(GuiComponent.STATS_ICON_LOCATION);
				GuiComponent.blit(i, j, CreateFlatWorldScreen.this.blitOffset, 0.0F, 0.0F, 18, 18, 128, 128);
			}
		}
	}
}

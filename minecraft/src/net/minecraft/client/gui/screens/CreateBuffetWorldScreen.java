package net.minecraft.client.gui.screens;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.BiomeSourceType;

@Environment(EnvType.CLIENT)
public class CreateBuffetWorldScreen extends Screen {
	private static final List<ResourceLocation> GENERATORS = (List<ResourceLocation>)Registry.CHUNK_GENERATOR_TYPE
		.keySet()
		.stream()
		.filter(resourceLocation -> Registry.CHUNK_GENERATOR_TYPE.get(resourceLocation).isPublic())
		.collect(Collectors.toList());
	private final CreateWorldScreen parent;
	private final CompoundTag optionsTag;
	private CreateBuffetWorldScreen.BiomeList list;
	private int generatorIndex;
	private Button doneButton;

	public CreateBuffetWorldScreen(CreateWorldScreen createWorldScreen, CompoundTag compoundTag) {
		super(new TranslatableComponent("createWorld.customize.buffet.title"));
		this.parent = createWorldScreen;
		this.optionsTag = compoundTag;
	}

	@Override
	protected void init() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		this.addButton(
			new Button(
				(this.width - 200) / 2,
				40,
				200,
				20,
				I18n.get("createWorld.customize.buffet.generatortype")
					+ " "
					+ I18n.get(Util.makeDescriptionId("generator", (ResourceLocation)GENERATORS.get(this.generatorIndex))),
				button -> {
					this.generatorIndex++;
					if (this.generatorIndex >= GENERATORS.size()) {
						this.generatorIndex = 0;
					}

					button.setMessage(
						I18n.get("createWorld.customize.buffet.generatortype")
							+ " "
							+ I18n.get(Util.makeDescriptionId("generator", (ResourceLocation)GENERATORS.get(this.generatorIndex)))
					);
				}
			)
		);
		this.list = new CreateBuffetWorldScreen.BiomeList();
		this.children.add(this.list);
		this.doneButton = this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, I18n.get("gui.done"), button -> {
			this.parent.levelTypeOptions = this.saveOptions();
			this.minecraft.setScreen(this.parent);
		}));
		this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, I18n.get("gui.cancel"), button -> this.minecraft.setScreen(this.parent)));
		this.loadOptions();
		this.updateButtonValidity();
	}

	private void loadOptions() {
		if (this.optionsTag.contains("chunk_generator", 10) && this.optionsTag.getCompound("chunk_generator").contains("type", 8)) {
			ResourceLocation resourceLocation = new ResourceLocation(this.optionsTag.getCompound("chunk_generator").getString("type"));

			for (int i = 0; i < GENERATORS.size(); i++) {
				if (((ResourceLocation)GENERATORS.get(i)).equals(resourceLocation)) {
					this.generatorIndex = i;
					break;
				}
			}
		}

		if (this.optionsTag.contains("biome_source", 10) && this.optionsTag.getCompound("biome_source").contains("biomes", 9)) {
			ListTag listTag = this.optionsTag.getCompound("biome_source").getList("biomes", 8);

			for (int ix = 0; ix < listTag.size(); ix++) {
				ResourceLocation resourceLocation2 = new ResourceLocation(listTag.getString(ix));
				this.list
					.setSelected(
						(CreateBuffetWorldScreen.BiomeList.Entry)this.list
							.children()
							.stream()
							.filter(entry -> Objects.equals(entry.key, resourceLocation2))
							.findFirst()
							.orElse(null)
					);
			}
		}

		this.optionsTag.remove("chunk_generator");
		this.optionsTag.remove("biome_source");
	}

	private CompoundTag saveOptions() {
		CompoundTag compoundTag = new CompoundTag();
		CompoundTag compoundTag2 = new CompoundTag();
		compoundTag2.putString("type", Registry.BIOME_SOURCE_TYPE.getKey(BiomeSourceType.FIXED).toString());
		CompoundTag compoundTag3 = new CompoundTag();
		ListTag listTag = new ListTag();
		listTag.add(StringTag.valueOf(this.list.getSelected().key.toString()));
		compoundTag3.put("biomes", listTag);
		compoundTag2.put("options", compoundTag3);
		CompoundTag compoundTag4 = new CompoundTag();
		CompoundTag compoundTag5 = new CompoundTag();
		compoundTag4.putString("type", ((ResourceLocation)GENERATORS.get(this.generatorIndex)).toString());
		compoundTag5.putString("default_block", "minecraft:stone");
		compoundTag5.putString("default_fluid", "minecraft:water");
		compoundTag4.put("options", compoundTag5);
		compoundTag.put("biome_source", compoundTag2);
		compoundTag.put("chunk_generator", compoundTag4);
		return compoundTag;
	}

	public void updateButtonValidity() {
		this.doneButton.active = this.list.getSelected() != null;
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderDirtBackground(0);
		this.list.render(i, j, f);
		this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 8, 16777215);
		this.drawCenteredString(this.font, I18n.get("createWorld.customize.buffet.generator"), this.width / 2, 30, 10526880);
		this.drawCenteredString(this.font, I18n.get("createWorld.customize.buffet.biome"), this.width / 2, 68, 10526880);
		super.render(i, j, f);
	}

	@Environment(EnvType.CLIENT)
	class BiomeList extends ObjectSelectionList<CreateBuffetWorldScreen.BiomeList.Entry> {
		private BiomeList() {
			super(
				CreateBuffetWorldScreen.this.minecraft,
				CreateBuffetWorldScreen.this.width,
				CreateBuffetWorldScreen.this.height,
				80,
				CreateBuffetWorldScreen.this.height - 37,
				16
			);
			Registry.BIOME
				.keySet()
				.stream()
				.sorted(Comparator.comparing(resourceLocation -> Registry.BIOME.get(resourceLocation).getName().getString()))
				.forEach(resourceLocation -> this.addEntry(new CreateBuffetWorldScreen.BiomeList.Entry(resourceLocation)));
		}

		@Override
		protected boolean isFocused() {
			return CreateBuffetWorldScreen.this.getFocused() == this;
		}

		public void setSelected(@Nullable CreateBuffetWorldScreen.BiomeList.Entry entry) {
			super.setSelected(entry);
			if (entry != null) {
				NarratorChatListener.INSTANCE.sayNow(new TranslatableComponent("narrator.select", Registry.BIOME.get(entry.key).getName().getString()).getString());
			}
		}

		@Override
		protected void moveSelection(int i) {
			super.moveSelection(i);
			CreateBuffetWorldScreen.this.updateButtonValidity();
		}

		@Environment(EnvType.CLIENT)
		class Entry extends ObjectSelectionList.Entry<CreateBuffetWorldScreen.BiomeList.Entry> {
			private final ResourceLocation key;

			public Entry(ResourceLocation resourceLocation) {
				this.key = resourceLocation;
			}

			@Override
			public void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
				BiomeList.this.drawString(CreateBuffetWorldScreen.this.font, Registry.BIOME.get(this.key).getName().getString(), k + 5, j + 2, 16777215);
			}

			@Override
			public boolean mouseClicked(double d, double e, int i) {
				if (i == 0) {
					BiomeList.this.setSelected(this);
					CreateBuffetWorldScreen.this.updateButtonValidity();
					return true;
				} else {
					return false;
				}
			}
		}
	}
}

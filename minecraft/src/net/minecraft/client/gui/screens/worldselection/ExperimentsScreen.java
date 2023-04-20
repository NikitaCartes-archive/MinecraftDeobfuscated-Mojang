package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;

@Environment(EnvType.CLIENT)
public class ExperimentsScreen extends Screen {
	private static final int MAIN_CONTENT_WIDTH = 310;
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	private final Screen parent;
	private final PackRepository packRepository;
	private final Consumer<PackRepository> output;
	private final Object2BooleanMap<Pack> packs = new Object2BooleanLinkedOpenHashMap<>();

	protected ExperimentsScreen(Screen screen, PackRepository packRepository, Consumer<PackRepository> consumer) {
		super(Component.translatable("experiments_screen.title"));
		this.parent = screen;
		this.packRepository = packRepository;
		this.output = consumer;

		for (Pack pack : packRepository.getAvailablePacks()) {
			if (pack.getPackSource() == PackSource.FEATURE) {
				this.packs.put(pack, packRepository.getSelectedPacks().contains(pack));
			}
		}
	}

	@Override
	protected void init() {
		this.layout.addToHeader(new StringWidget(Component.translatable("selectWorld.experiments"), this.font));
		GridLayout.RowHelper rowHelper = this.layout.addToContents(new GridLayout()).createRowHelper(1);
		rowHelper.addChild(
			new MultiLineTextWidget(Component.translatable("selectWorld.experiments.info").withStyle(ChatFormatting.RED), this.font).setMaxWidth(310),
			rowHelper.newCellSettings().paddingBottom(15)
		);
		SwitchGrid.Builder builder = SwitchGrid.builder(310).withInfoUnderneath(2, true).withRowSpacing(4);
		this.packs
			.forEach(
				(pack, boolean_) -> builder.addSwitch(
							getHumanReadableTitle(pack), () -> this.packs.getBoolean(pack), boolean_x -> this.packs.put(pack, boolean_x.booleanValue())
						)
						.withInfo(pack.getDescription())
			);
		builder.build(rowHelper::addChild);
		GridLayout.RowHelper rowHelper2 = this.layout.addToFooter(new GridLayout().columnSpacing(10)).createRowHelper(2);
		rowHelper2.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onDone()).build());
		rowHelper2.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).build());
		this.layout.visitWidgets(guiEventListener -> {
			AbstractWidget var10000 = this.addRenderableWidget(guiEventListener);
		});
		this.repositionElements();
	}

	private static Component getHumanReadableTitle(Pack pack) {
		String string = "dataPack." + pack.getId() + ".name";
		return (Component)(I18n.exists(string) ? Component.translatable(string) : pack.getTitle());
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parent);
	}

	private void onDone() {
		List<Pack> list = new ArrayList(this.packRepository.getSelectedPacks());
		List<Pack> list2 = new ArrayList();
		this.packs.forEach((pack, boolean_) -> {
			list.remove(pack);
			if (boolean_) {
				list2.add(pack);
			}
		});
		list.addAll(Lists.reverse(list2));
		this.packRepository.setSelected(list.stream().map(Pack::getId).toList());
		this.output.accept(this.packRepository);
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderBackground(guiGraphics);
		guiGraphics.setColor(0.125F, 0.125F, 0.125F, 1.0F);
		int k = 32;
		guiGraphics.blit(
			BACKGROUND_LOCATION,
			0,
			this.layout.getHeaderHeight(),
			0.0F,
			0.0F,
			this.width,
			this.height - this.layout.getHeaderHeight() - this.layout.getFooterHeight(),
			32,
			32
		);
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		super.render(guiGraphics, i, j, f);
	}
}

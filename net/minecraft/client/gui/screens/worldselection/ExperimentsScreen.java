/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import java.util.ArrayList;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SwitchGrid;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;

@Environment(value=EnvType.CLIENT)
public class ExperimentsScreen
extends Screen {
    private static final int MAIN_CONTENT_WIDTH = 310;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final Screen parent;
    private final PackRepository packRepository;
    private final Consumer<PackRepository> output;
    private final Object2BooleanMap<Pack> packs = new Object2BooleanLinkedOpenHashMap<Pack>();

    protected ExperimentsScreen(Screen screen, PackRepository packRepository, Consumer<PackRepository> consumer) {
        super(Component.translatable("experiments_screen.title"));
        this.parent = screen;
        this.packRepository = packRepository;
        this.output = consumer;
        for (Pack pack : packRepository.getAvailablePacks()) {
            if (pack.getPackSource() != PackSource.FEATURE) continue;
            this.packs.put(pack, packRepository.getSelectedPacks().contains(pack));
        }
    }

    @Override
    protected void init() {
        this.layout.addToHeader(new StringWidget(Component.translatable("selectWorld.experiments"), this.font));
        GridLayout.RowHelper rowHelper = this.layout.addToContents(new GridLayout()).createRowHelper(1);
        rowHelper.addChild(new MultiLineTextWidget(Component.translatable("selectWorld.experiments.info").withStyle(ChatFormatting.RED), this.font).setMaxWidth(310), rowHelper.newCellSettings().paddingBottom(15));
        SwitchGrid.Builder builder = SwitchGrid.builder(310).withInfoUnderneath(2, true).withRowSpacing(4);
        this.packs.forEach((pack, boolean_2) -> builder.addSwitch(ExperimentsScreen.getHumanReadableTitle(pack), () -> this.packs.getBoolean(pack), boolean_ -> this.packs.put((Pack)pack, (boolean)boolean_)).withInfo(pack.getDescription()));
        builder.build(rowHelper::addChild);
        GridLayout.RowHelper rowHelper2 = this.layout.addToFooter(new GridLayout().columnSpacing(10)).createRowHelper(2);
        rowHelper2.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onDone()).build());
        rowHelper2.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).build());
        this.layout.visitWidgets(guiEventListener -> {
            AbstractWidget cfr_ignored_0 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
        });
        this.repositionElements();
    }

    private static Component getHumanReadableTitle(Pack pack) {
        String string = "dataPack." + pack.getId() + ".name";
        return I18n.exists(string) ? Component.translatable(string) : pack.getTitle();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private void onDone() {
        ArrayList<Pack> list = new ArrayList<Pack>(this.packRepository.getSelectedPacks());
        ArrayList list2 = new ArrayList();
        this.packs.forEach((pack, boolean_) -> {
            list.remove(pack);
            if (boolean_.booleanValue()) {
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
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
        RenderSystem.setShaderColor(0.125f, 0.125f, 0.125f, 1.0f);
        int k = 32;
        ExperimentsScreen.blit(poseStack, 0, this.layout.getHeaderHeight(), 0.0f, 0.0f, this.width, this.height - this.layout.getHeaderHeight() - this.layout.getFooterHeight(), 32, 32);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        super.render(poseStack, i, j, f);
    }
}


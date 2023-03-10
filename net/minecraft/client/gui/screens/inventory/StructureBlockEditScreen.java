/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;

@Environment(value=EnvType.CLIENT)
public class StructureBlockEditScreen
extends Screen {
    private static final Component NAME_LABEL = Component.translatable("structure_block.structure_name");
    private static final Component POSITION_LABEL = Component.translatable("structure_block.position");
    private static final Component SIZE_LABEL = Component.translatable("structure_block.size");
    private static final Component INTEGRITY_LABEL = Component.translatable("structure_block.integrity");
    private static final Component CUSTOM_DATA_LABEL = Component.translatable("structure_block.custom_data");
    private static final Component INCLUDE_ENTITIES_LABEL = Component.translatable("structure_block.include_entities");
    private static final Component DETECT_SIZE_LABEL = Component.translatable("structure_block.detect_size");
    private static final Component SHOW_AIR_LABEL = Component.translatable("structure_block.show_air");
    private static final Component SHOW_BOUNDING_BOX_LABEL = Component.translatable("structure_block.show_boundingbox");
    private static final ImmutableList<StructureMode> ALL_MODES = ImmutableList.copyOf(StructureMode.values());
    private static final ImmutableList<StructureMode> DEFAULT_MODES = ALL_MODES.stream().filter(structureMode -> structureMode != StructureMode.DATA).collect(ImmutableList.toImmutableList());
    private final StructureBlockEntity structure;
    private Mirror initialMirror = Mirror.NONE;
    private Rotation initialRotation = Rotation.NONE;
    private StructureMode initialMode = StructureMode.DATA;
    private boolean initialEntityIgnoring;
    private boolean initialShowAir;
    private boolean initialShowBoundingBox;
    private EditBox nameEdit;
    private EditBox posXEdit;
    private EditBox posYEdit;
    private EditBox posZEdit;
    private EditBox sizeXEdit;
    private EditBox sizeYEdit;
    private EditBox sizeZEdit;
    private EditBox integrityEdit;
    private EditBox seedEdit;
    private EditBox dataEdit;
    private Button saveButton;
    private Button loadButton;
    private Button rot0Button;
    private Button rot90Button;
    private Button rot180Button;
    private Button rot270Button;
    private Button detectButton;
    private CycleButton<Boolean> includeEntitiesButton;
    private CycleButton<Mirror> mirrorButton;
    private CycleButton<Boolean> toggleAirButton;
    private CycleButton<Boolean> toggleBoundingBox;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.0###");

    public StructureBlockEditScreen(StructureBlockEntity structureBlockEntity) {
        super(Component.translatable(Blocks.STRUCTURE_BLOCK.getDescriptionId()));
        this.structure = structureBlockEntity;
        this.decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
        this.posXEdit.tick();
        this.posYEdit.tick();
        this.posZEdit.tick();
        this.sizeXEdit.tick();
        this.sizeYEdit.tick();
        this.sizeZEdit.tick();
        this.integrityEdit.tick();
        this.seedEdit.tick();
        this.dataEdit.tick();
    }

    private void onDone() {
        if (this.sendToServer(StructureBlockEntity.UpdateType.UPDATE_DATA)) {
            this.minecraft.setScreen(null);
        }
    }

    private void onCancel() {
        this.structure.setMirror(this.initialMirror);
        this.structure.setRotation(this.initialRotation);
        this.structure.setMode(this.initialMode);
        this.structure.setIgnoreEntities(this.initialEntityIgnoring);
        this.structure.setShowAir(this.initialShowAir);
        this.structure.setShowBoundingBox(this.initialShowBoundingBox);
        this.minecraft.setScreen(null);
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onDone()).bounds(this.width / 2 - 4 - 150, 210, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onCancel()).bounds(this.width / 2 + 4, 210, 150, 20).build());
        this.initialMirror = this.structure.getMirror();
        this.initialRotation = this.structure.getRotation();
        this.initialMode = this.structure.getMode();
        this.initialEntityIgnoring = this.structure.isIgnoreEntities();
        this.initialShowAir = this.structure.getShowAir();
        this.initialShowBoundingBox = this.structure.getShowBoundingBox();
        this.saveButton = this.addRenderableWidget(Button.builder(Component.translatable("structure_block.button.save"), button -> {
            if (this.structure.getMode() == StructureMode.SAVE) {
                this.sendToServer(StructureBlockEntity.UpdateType.SAVE_AREA);
                this.minecraft.setScreen(null);
            }
        }).bounds(this.width / 2 + 4 + 100, 185, 50, 20).build());
        this.loadButton = this.addRenderableWidget(Button.builder(Component.translatable("structure_block.button.load"), button -> {
            if (this.structure.getMode() == StructureMode.LOAD) {
                this.sendToServer(StructureBlockEntity.UpdateType.LOAD_AREA);
                this.minecraft.setScreen(null);
            }
        }).bounds(this.width / 2 + 4 + 100, 185, 50, 20).build());
        this.addRenderableWidget(CycleButton.builder(structureMode -> Component.translatable("structure_block.mode." + structureMode.getSerializedName())).withValues((List<StructureMode>)DEFAULT_MODES, (List<StructureMode>)ALL_MODES).displayOnlyValue().withInitialValue(this.initialMode).create(this.width / 2 - 4 - 150, 185, 50, 20, Component.literal("MODE"), (cycleButton, structureMode) -> {
            this.structure.setMode((StructureMode)structureMode);
            this.updateMode((StructureMode)structureMode);
        }));
        this.detectButton = this.addRenderableWidget(Button.builder(Component.translatable("structure_block.button.detect_size"), button -> {
            if (this.structure.getMode() == StructureMode.SAVE) {
                this.sendToServer(StructureBlockEntity.UpdateType.SCAN_AREA);
                this.minecraft.setScreen(null);
            }
        }).bounds(this.width / 2 + 4 + 100, 120, 50, 20).build());
        this.includeEntitiesButton = this.addRenderableWidget(CycleButton.onOffBuilder(!this.structure.isIgnoreEntities()).displayOnlyValue().create(this.width / 2 + 4 + 100, 160, 50, 20, INCLUDE_ENTITIES_LABEL, (cycleButton, boolean_) -> this.structure.setIgnoreEntities(boolean_ == false)));
        this.mirrorButton = this.addRenderableWidget(CycleButton.builder(Mirror::symbol).withValues((Mirror[])Mirror.values()).displayOnlyValue().withInitialValue(this.initialMirror).create(this.width / 2 - 20, 185, 40, 20, Component.literal("MIRROR"), (cycleButton, mirror) -> this.structure.setMirror((Mirror)mirror)));
        this.toggleAirButton = this.addRenderableWidget(CycleButton.onOffBuilder(this.structure.getShowAir()).displayOnlyValue().create(this.width / 2 + 4 + 100, 80, 50, 20, SHOW_AIR_LABEL, (cycleButton, boolean_) -> this.structure.setShowAir((boolean)boolean_)));
        this.toggleBoundingBox = this.addRenderableWidget(CycleButton.onOffBuilder(this.structure.getShowBoundingBox()).displayOnlyValue().create(this.width / 2 + 4 + 100, 80, 50, 20, SHOW_BOUNDING_BOX_LABEL, (cycleButton, boolean_) -> this.structure.setShowBoundingBox((boolean)boolean_)));
        this.rot0Button = this.addRenderableWidget(Button.builder(Component.literal("0"), button -> {
            this.structure.setRotation(Rotation.NONE);
            this.updateDirectionButtons();
        }).bounds(this.width / 2 - 1 - 40 - 1 - 40 - 20, 185, 40, 20).build());
        this.rot90Button = this.addRenderableWidget(Button.builder(Component.literal("90"), button -> {
            this.structure.setRotation(Rotation.CLOCKWISE_90);
            this.updateDirectionButtons();
        }).bounds(this.width / 2 - 1 - 40 - 20, 185, 40, 20).build());
        this.rot180Button = this.addRenderableWidget(Button.builder(Component.literal("180"), button -> {
            this.structure.setRotation(Rotation.CLOCKWISE_180);
            this.updateDirectionButtons();
        }).bounds(this.width / 2 + 1 + 20, 185, 40, 20).build());
        this.rot270Button = this.addRenderableWidget(Button.builder(Component.literal("270"), button -> {
            this.structure.setRotation(Rotation.COUNTERCLOCKWISE_90);
            this.updateDirectionButtons();
        }).bounds(this.width / 2 + 1 + 40 + 1 + 20, 185, 40, 20).build());
        this.nameEdit = new EditBox(this.font, this.width / 2 - 152, 40, 300, 20, (Component)Component.translatable("structure_block.structure_name")){

            @Override
            public boolean charTyped(char c, int i) {
                if (!StructureBlockEditScreen.this.isValidCharacterForName(this.getValue(), c, this.getCursorPosition())) {
                    return false;
                }
                return super.charTyped(c, i);
            }
        };
        this.nameEdit.setMaxLength(128);
        this.nameEdit.setValue(this.structure.getStructureName());
        this.addWidget(this.nameEdit);
        BlockPos blockPos = this.structure.getStructurePos();
        this.posXEdit = new EditBox(this.font, this.width / 2 - 152, 80, 80, 20, Component.translatable("structure_block.position.x"));
        this.posXEdit.setMaxLength(15);
        this.posXEdit.setValue(Integer.toString(blockPos.getX()));
        this.addWidget(this.posXEdit);
        this.posYEdit = new EditBox(this.font, this.width / 2 - 72, 80, 80, 20, Component.translatable("structure_block.position.y"));
        this.posYEdit.setMaxLength(15);
        this.posYEdit.setValue(Integer.toString(blockPos.getY()));
        this.addWidget(this.posYEdit);
        this.posZEdit = new EditBox(this.font, this.width / 2 + 8, 80, 80, 20, Component.translatable("structure_block.position.z"));
        this.posZEdit.setMaxLength(15);
        this.posZEdit.setValue(Integer.toString(blockPos.getZ()));
        this.addWidget(this.posZEdit);
        Vec3i vec3i = this.structure.getStructureSize();
        this.sizeXEdit = new EditBox(this.font, this.width / 2 - 152, 120, 80, 20, Component.translatable("structure_block.size.x"));
        this.sizeXEdit.setMaxLength(15);
        this.sizeXEdit.setValue(Integer.toString(vec3i.getX()));
        this.addWidget(this.sizeXEdit);
        this.sizeYEdit = new EditBox(this.font, this.width / 2 - 72, 120, 80, 20, Component.translatable("structure_block.size.y"));
        this.sizeYEdit.setMaxLength(15);
        this.sizeYEdit.setValue(Integer.toString(vec3i.getY()));
        this.addWidget(this.sizeYEdit);
        this.sizeZEdit = new EditBox(this.font, this.width / 2 + 8, 120, 80, 20, Component.translatable("structure_block.size.z"));
        this.sizeZEdit.setMaxLength(15);
        this.sizeZEdit.setValue(Integer.toString(vec3i.getZ()));
        this.addWidget(this.sizeZEdit);
        this.integrityEdit = new EditBox(this.font, this.width / 2 - 152, 120, 80, 20, Component.translatable("structure_block.integrity.integrity"));
        this.integrityEdit.setMaxLength(15);
        this.integrityEdit.setValue(this.decimalFormat.format(this.structure.getIntegrity()));
        this.addWidget(this.integrityEdit);
        this.seedEdit = new EditBox(this.font, this.width / 2 - 72, 120, 80, 20, Component.translatable("structure_block.integrity.seed"));
        this.seedEdit.setMaxLength(31);
        this.seedEdit.setValue(Long.toString(this.structure.getSeed()));
        this.addWidget(this.seedEdit);
        this.dataEdit = new EditBox(this.font, this.width / 2 - 152, 120, 240, 20, Component.translatable("structure_block.custom_data"));
        this.dataEdit.setMaxLength(128);
        this.dataEdit.setValue(this.structure.getMetaData());
        this.addWidget(this.dataEdit);
        this.updateDirectionButtons();
        this.updateMode(this.initialMode);
        this.setInitialFocus(this.nameEdit);
    }

    @Override
    public void resize(Minecraft minecraft, int i, int j) {
        String string = this.nameEdit.getValue();
        String string2 = this.posXEdit.getValue();
        String string3 = this.posYEdit.getValue();
        String string4 = this.posZEdit.getValue();
        String string5 = this.sizeXEdit.getValue();
        String string6 = this.sizeYEdit.getValue();
        String string7 = this.sizeZEdit.getValue();
        String string8 = this.integrityEdit.getValue();
        String string9 = this.seedEdit.getValue();
        String string10 = this.dataEdit.getValue();
        this.init(minecraft, i, j);
        this.nameEdit.setValue(string);
        this.posXEdit.setValue(string2);
        this.posYEdit.setValue(string3);
        this.posZEdit.setValue(string4);
        this.sizeXEdit.setValue(string5);
        this.sizeYEdit.setValue(string6);
        this.sizeZEdit.setValue(string7);
        this.integrityEdit.setValue(string8);
        this.seedEdit.setValue(string9);
        this.dataEdit.setValue(string10);
    }

    private void updateDirectionButtons() {
        this.rot0Button.active = true;
        this.rot90Button.active = true;
        this.rot180Button.active = true;
        this.rot270Button.active = true;
        switch (this.structure.getRotation()) {
            case NONE: {
                this.rot0Button.active = false;
                break;
            }
            case CLOCKWISE_180: {
                this.rot180Button.active = false;
                break;
            }
            case COUNTERCLOCKWISE_90: {
                this.rot270Button.active = false;
                break;
            }
            case CLOCKWISE_90: {
                this.rot90Button.active = false;
            }
        }
    }

    private void updateMode(StructureMode structureMode) {
        this.nameEdit.setVisible(false);
        this.posXEdit.setVisible(false);
        this.posYEdit.setVisible(false);
        this.posZEdit.setVisible(false);
        this.sizeXEdit.setVisible(false);
        this.sizeYEdit.setVisible(false);
        this.sizeZEdit.setVisible(false);
        this.integrityEdit.setVisible(false);
        this.seedEdit.setVisible(false);
        this.dataEdit.setVisible(false);
        this.saveButton.visible = false;
        this.loadButton.visible = false;
        this.detectButton.visible = false;
        this.includeEntitiesButton.visible = false;
        this.mirrorButton.visible = false;
        this.rot0Button.visible = false;
        this.rot90Button.visible = false;
        this.rot180Button.visible = false;
        this.rot270Button.visible = false;
        this.toggleAirButton.visible = false;
        this.toggleBoundingBox.visible = false;
        switch (structureMode) {
            case SAVE: {
                this.nameEdit.setVisible(true);
                this.posXEdit.setVisible(true);
                this.posYEdit.setVisible(true);
                this.posZEdit.setVisible(true);
                this.sizeXEdit.setVisible(true);
                this.sizeYEdit.setVisible(true);
                this.sizeZEdit.setVisible(true);
                this.saveButton.visible = true;
                this.detectButton.visible = true;
                this.includeEntitiesButton.visible = true;
                this.toggleAirButton.visible = true;
                break;
            }
            case LOAD: {
                this.nameEdit.setVisible(true);
                this.posXEdit.setVisible(true);
                this.posYEdit.setVisible(true);
                this.posZEdit.setVisible(true);
                this.integrityEdit.setVisible(true);
                this.seedEdit.setVisible(true);
                this.loadButton.visible = true;
                this.includeEntitiesButton.visible = true;
                this.mirrorButton.visible = true;
                this.rot0Button.visible = true;
                this.rot90Button.visible = true;
                this.rot180Button.visible = true;
                this.rot270Button.visible = true;
                this.toggleBoundingBox.visible = true;
                this.updateDirectionButtons();
                break;
            }
            case CORNER: {
                this.nameEdit.setVisible(true);
                break;
            }
            case DATA: {
                this.dataEdit.setVisible(true);
            }
        }
    }

    private boolean sendToServer(StructureBlockEntity.UpdateType updateType) {
        BlockPos blockPos = new BlockPos(this.parseCoordinate(this.posXEdit.getValue()), this.parseCoordinate(this.posYEdit.getValue()), this.parseCoordinate(this.posZEdit.getValue()));
        Vec3i vec3i = new Vec3i(this.parseCoordinate(this.sizeXEdit.getValue()), this.parseCoordinate(this.sizeYEdit.getValue()), this.parseCoordinate(this.sizeZEdit.getValue()));
        float f = this.parseIntegrity(this.integrityEdit.getValue());
        long l = this.parseSeed(this.seedEdit.getValue());
        this.minecraft.getConnection().send(new ServerboundSetStructureBlockPacket(this.structure.getBlockPos(), updateType, this.structure.getMode(), this.nameEdit.getValue(), blockPos, vec3i, this.structure.getMirror(), this.structure.getRotation(), this.dataEdit.getValue(), this.structure.isIgnoreEntities(), this.structure.getShowAir(), this.structure.getShowBoundingBox(), f, l));
        return true;
    }

    private long parseSeed(String string) {
        try {
            return Long.valueOf(string);
        } catch (NumberFormatException numberFormatException) {
            return 0L;
        }
    }

    private float parseIntegrity(String string) {
        try {
            return Float.valueOf(string).floatValue();
        } catch (NumberFormatException numberFormatException) {
            return 1.0f;
        }
    }

    private int parseCoordinate(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException numberFormatException) {
            return 0;
        }
    }

    @Override
    public void onClose() {
        this.onCancel();
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (super.keyPressed(i, j, k)) {
            return true;
        }
        if (i == 257 || i == 335) {
            this.onDone();
            return true;
        }
        return false;
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        StructureMode structureMode = this.structure.getMode();
        StructureBlockEditScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 10, 0xFFFFFF);
        if (structureMode != StructureMode.DATA) {
            StructureBlockEditScreen.drawString(poseStack, this.font, NAME_LABEL, this.width / 2 - 153, 30, 0xA0A0A0);
            this.nameEdit.render(poseStack, i, j, f);
        }
        if (structureMode == StructureMode.LOAD || structureMode == StructureMode.SAVE) {
            StructureBlockEditScreen.drawString(poseStack, this.font, POSITION_LABEL, this.width / 2 - 153, 70, 0xA0A0A0);
            this.posXEdit.render(poseStack, i, j, f);
            this.posYEdit.render(poseStack, i, j, f);
            this.posZEdit.render(poseStack, i, j, f);
            StructureBlockEditScreen.drawString(poseStack, this.font, INCLUDE_ENTITIES_LABEL, this.width / 2 + 154 - this.font.width(INCLUDE_ENTITIES_LABEL), 150, 0xA0A0A0);
        }
        if (structureMode == StructureMode.SAVE) {
            StructureBlockEditScreen.drawString(poseStack, this.font, SIZE_LABEL, this.width / 2 - 153, 110, 0xA0A0A0);
            this.sizeXEdit.render(poseStack, i, j, f);
            this.sizeYEdit.render(poseStack, i, j, f);
            this.sizeZEdit.render(poseStack, i, j, f);
            StructureBlockEditScreen.drawString(poseStack, this.font, DETECT_SIZE_LABEL, this.width / 2 + 154 - this.font.width(DETECT_SIZE_LABEL), 110, 0xA0A0A0);
            StructureBlockEditScreen.drawString(poseStack, this.font, SHOW_AIR_LABEL, this.width / 2 + 154 - this.font.width(SHOW_AIR_LABEL), 70, 0xA0A0A0);
        }
        if (structureMode == StructureMode.LOAD) {
            StructureBlockEditScreen.drawString(poseStack, this.font, INTEGRITY_LABEL, this.width / 2 - 153, 110, 0xA0A0A0);
            this.integrityEdit.render(poseStack, i, j, f);
            this.seedEdit.render(poseStack, i, j, f);
            StructureBlockEditScreen.drawString(poseStack, this.font, SHOW_BOUNDING_BOX_LABEL, this.width / 2 + 154 - this.font.width(SHOW_BOUNDING_BOX_LABEL), 70, 0xA0A0A0);
        }
        if (structureMode == StructureMode.DATA) {
            StructureBlockEditScreen.drawString(poseStack, this.font, CUSTOM_DATA_LABEL, this.width / 2 - 153, 110, 0xA0A0A0);
            this.dataEdit.render(poseStack, i, j, f);
        }
        StructureBlockEditScreen.drawString(poseStack, this.font, structureMode.getDisplayName(), this.width / 2 - 153, 174, 0xA0A0A0);
        super.render(poseStack, i, j, f);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}


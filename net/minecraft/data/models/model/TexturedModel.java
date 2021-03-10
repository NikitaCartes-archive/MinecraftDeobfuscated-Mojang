/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.models.model;

import com.google.gson.JsonElement;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class TexturedModel {
    public static final Provider CUBE = TexturedModel.createDefault(TextureMapping::cube, ModelTemplates.CUBE_ALL);
    public static final Provider CUBE_MIRRORED = TexturedModel.createDefault(TextureMapping::cube, ModelTemplates.CUBE_MIRRORED_ALL);
    public static final Provider COLUMN = TexturedModel.createDefault(TextureMapping::column, ModelTemplates.CUBE_COLUMN);
    public static final Provider COLUMN_HORIZONTAL = TexturedModel.createDefault(TextureMapping::column, ModelTemplates.CUBE_COLUMN_HORIZONTAL);
    public static final Provider CUBE_TOP_BOTTOM = TexturedModel.createDefault(TextureMapping::cubeBottomTop, ModelTemplates.CUBE_BOTTOM_TOP);
    public static final Provider CUBE_TOP = TexturedModel.createDefault(TextureMapping::cubeTop, ModelTemplates.CUBE_TOP);
    public static final Provider ORIENTABLE_ONLY_TOP = TexturedModel.createDefault(TextureMapping::orientableCubeOnlyTop, ModelTemplates.CUBE_ORIENTABLE);
    public static final Provider ORIENTABLE = TexturedModel.createDefault(TextureMapping::orientableCube, ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM);
    public static final Provider CARPET = TexturedModel.createDefault(TextureMapping::wool, ModelTemplates.CARPET);
    public static final Provider GLAZED_TERRACOTTA = TexturedModel.createDefault(TextureMapping::pattern, ModelTemplates.GLAZED_TERRACOTTA);
    public static final Provider CORAL_FAN = TexturedModel.createDefault(TextureMapping::fan, ModelTemplates.CORAL_FAN);
    public static final Provider PARTICLE_ONLY = TexturedModel.createDefault(TextureMapping::particle, ModelTemplates.PARTICLE_ONLY);
    public static final Provider ANVIL = TexturedModel.createDefault(TextureMapping::top, ModelTemplates.ANVIL);
    public static final Provider LEAVES = TexturedModel.createDefault(TextureMapping::cube, ModelTemplates.LEAVES);
    public static final Provider LANTERN = TexturedModel.createDefault(TextureMapping::lantern, ModelTemplates.LANTERN);
    public static final Provider HANGING_LANTERN = TexturedModel.createDefault(TextureMapping::lantern, ModelTemplates.HANGING_LANTERN);
    public static final Provider SEAGRASS = TexturedModel.createDefault(TextureMapping::defaultTexture, ModelTemplates.SEAGRASS);
    public static final Provider COLUMN_ALT = TexturedModel.createDefault(TextureMapping::logColumn, ModelTemplates.CUBE_COLUMN);
    public static final Provider COLUMN_HORIZONTAL_ALT = TexturedModel.createDefault(TextureMapping::logColumn, ModelTemplates.CUBE_COLUMN_HORIZONTAL);
    public static final Provider TOP_BOTTOM_WITH_WALL = TexturedModel.createDefault(TextureMapping::cubeBottomTopWithWall, ModelTemplates.CUBE_BOTTOM_TOP);
    public static final Provider COLUMN_WITH_WALL = TexturedModel.createDefault(TextureMapping::columnWithWall, ModelTemplates.CUBE_COLUMN);
    private final TextureMapping mapping;
    private final ModelTemplate template;

    private TexturedModel(TextureMapping textureMapping, ModelTemplate modelTemplate) {
        this.mapping = textureMapping;
        this.template = modelTemplate;
    }

    public ModelTemplate getTemplate() {
        return this.template;
    }

    public TextureMapping getMapping() {
        return this.mapping;
    }

    public TexturedModel updateTextures(Consumer<TextureMapping> consumer) {
        consumer.accept(this.mapping);
        return this;
    }

    public ResourceLocation create(Block block, BiConsumer<ResourceLocation, Supplier<JsonElement>> biConsumer) {
        return this.template.create(block, this.mapping, biConsumer);
    }

    public ResourceLocation createWithSuffix(Block block, String string, BiConsumer<ResourceLocation, Supplier<JsonElement>> biConsumer) {
        return this.template.createWithSuffix(block, string, this.mapping, biConsumer);
    }

    private static Provider createDefault(Function<Block, TextureMapping> function, ModelTemplate modelTemplate) {
        return block -> new TexturedModel((TextureMapping)function.apply(block), modelTemplate);
    }

    public static TexturedModel createAllSame(ResourceLocation resourceLocation) {
        return new TexturedModel(TextureMapping.cube(resourceLocation), ModelTemplates.CUBE_ALL);
    }

    @FunctionalInterface
    public static interface Provider {
        public TexturedModel get(Block var1);

        default public ResourceLocation create(Block block, BiConsumer<ResourceLocation, Supplier<JsonElement>> biConsumer) {
            return this.get(block).create(block, biConsumer);
        }

        default public ResourceLocation createWithSuffix(Block block, String string, BiConsumer<ResourceLocation, Supplier<JsonElement>> biConsumer) {
            return this.get(block).createWithSuffix(block, string, biConsumer);
        }
    }
}


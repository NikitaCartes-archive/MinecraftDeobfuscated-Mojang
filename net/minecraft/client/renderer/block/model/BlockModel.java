/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.block.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BuiltInModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BlockModel
implements UnbakedModel {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    @VisibleForTesting
    static final Gson GSON = new GsonBuilder().registerTypeAdapter((Type)((Object)BlockModel.class), new Deserializer()).registerTypeAdapter((Type)((Object)BlockElement.class), new BlockElement.Deserializer()).registerTypeAdapter((Type)((Object)BlockElementFace.class), new BlockElementFace.Deserializer()).registerTypeAdapter((Type)((Object)BlockFaceUV.class), new BlockFaceUV.Deserializer()).registerTypeAdapter((Type)((Object)ItemTransform.class), new ItemTransform.Deserializer()).registerTypeAdapter((Type)((Object)ItemTransforms.class), new ItemTransforms.Deserializer()).registerTypeAdapter((Type)((Object)ItemOverride.class), new ItemOverride.Deserializer()).create();
    private final List<BlockElement> elements;
    @Nullable
    private final GuiLight guiLight;
    private final boolean hasAmbientOcclusion;
    private final ItemTransforms transforms;
    private final List<ItemOverride> overrides;
    public String name = "";
    @VisibleForTesting
    protected final Map<String, Either<Material, String>> textureMap;
    @Nullable
    protected BlockModel parent;
    @Nullable
    protected ResourceLocation parentLocation;

    public static BlockModel fromStream(Reader reader) {
        return GsonHelper.fromJson(GSON, reader, BlockModel.class);
    }

    public static BlockModel fromString(String string) {
        return BlockModel.fromStream(new StringReader(string));
    }

    public BlockModel(@Nullable ResourceLocation resourceLocation, List<BlockElement> list, Map<String, Either<Material, String>> map, boolean bl, @Nullable GuiLight guiLight, ItemTransforms itemTransforms, List<ItemOverride> list2) {
        this.elements = list;
        this.hasAmbientOcclusion = bl;
        this.guiLight = guiLight;
        this.textureMap = map;
        this.parentLocation = resourceLocation;
        this.transforms = itemTransforms;
        this.overrides = list2;
    }

    public List<BlockElement> getElements() {
        if (this.elements.isEmpty() && this.parent != null) {
            return this.parent.getElements();
        }
        return this.elements;
    }

    public boolean hasAmbientOcclusion() {
        if (this.parent != null) {
            return this.parent.hasAmbientOcclusion();
        }
        return this.hasAmbientOcclusion;
    }

    public GuiLight getGuiLight() {
        if (this.guiLight != null) {
            return this.guiLight;
        }
        if (this.parent != null) {
            return this.parent.getGuiLight();
        }
        return GuiLight.SIDE;
    }

    public List<ItemOverride> getOverrides() {
        return this.overrides;
    }

    private ItemOverrides getItemOverrides(ModelBakery modelBakery, BlockModel blockModel) {
        if (this.overrides.isEmpty()) {
            return ItemOverrides.EMPTY;
        }
        return new ItemOverrides(modelBakery, blockModel, modelBakery::getModel, this.overrides);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        HashSet<ResourceLocation> set = Sets.newHashSet();
        for (ItemOverride itemOverride : this.overrides) {
            set.add(itemOverride.getModel());
        }
        if (this.parentLocation != null) {
            set.add(this.parentLocation);
        }
        return set;
    }

    @Override
    public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> function, Set<Pair<String, String>> set) {
        LinkedHashSet<BlockModel> set2 = Sets.newLinkedHashSet();
        BlockModel blockModel = this;
        while (blockModel.parentLocation != null && blockModel.parent == null) {
            set2.add(blockModel);
            UnbakedModel unbakedModel = function.apply(blockModel.parentLocation);
            if (unbakedModel == null) {
                LOGGER.warn("No parent '{}' while loading model '{}'", (Object)this.parentLocation, (Object)blockModel);
            }
            if (set2.contains(unbakedModel)) {
                LOGGER.warn("Found 'parent' loop while loading model '{}' in chain: {} -> {}", (Object)blockModel, (Object)set2.stream().map(Object::toString).collect(Collectors.joining(" -> ")), (Object)this.parentLocation);
                unbakedModel = null;
            }
            if (unbakedModel == null) {
                blockModel.parentLocation = ModelBakery.MISSING_MODEL_LOCATION;
                unbakedModel = function.apply(blockModel.parentLocation);
            }
            if (!(unbakedModel instanceof BlockModel)) {
                throw new IllegalStateException("BlockModel parent has to be a block model.");
            }
            blockModel.parent = (BlockModel)unbakedModel;
            blockModel = blockModel.parent;
        }
        HashSet<Material> set3 = Sets.newHashSet(this.getMaterial("particle"));
        for (BlockElement blockElement : this.getElements()) {
            for (BlockElementFace blockElementFace : blockElement.faces.values()) {
                Material material = this.getMaterial(blockElementFace.texture);
                if (Objects.equals(material.texture(), MissingTextureAtlasSprite.getLocation())) {
                    set.add(Pair.of(blockElementFace.texture, this.name));
                }
                set3.add(material);
            }
        }
        this.overrides.forEach(itemOverride -> {
            UnbakedModel unbakedModel = (UnbakedModel)function.apply(itemOverride.getModel());
            if (Objects.equals(unbakedModel, this)) {
                return;
            }
            set3.addAll(unbakedModel.getMaterials(function, set));
        });
        if (this.getRootModel() == ModelBakery.GENERATION_MARKER) {
            ItemModelGenerator.LAYERS.forEach(string -> set3.add(this.getMaterial((String)string)));
        }
        return set3;
    }

    @Override
    public BakedModel bake(ModelBakery modelBakery, Function<Material, TextureAtlasSprite> function, ModelState modelState, ResourceLocation resourceLocation) {
        return this.bake(modelBakery, this, function, modelState, resourceLocation, true);
    }

    public BakedModel bake(ModelBakery modelBakery, BlockModel blockModel, Function<Material, TextureAtlasSprite> function, ModelState modelState, ResourceLocation resourceLocation, boolean bl) {
        TextureAtlasSprite textureAtlasSprite = function.apply(this.getMaterial("particle"));
        if (this.getRootModel() == ModelBakery.BLOCK_ENTITY_MARKER) {
            return new BuiltInModel(this.getTransforms(), this.getItemOverrides(modelBakery, blockModel), textureAtlasSprite, this.getGuiLight().lightLikeBlock());
        }
        SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(this, this.getItemOverrides(modelBakery, blockModel), bl).particle(textureAtlasSprite);
        for (BlockElement blockElement : this.getElements()) {
            for (Direction direction : blockElement.faces.keySet()) {
                BlockElementFace blockElementFace = blockElement.faces.get(direction);
                TextureAtlasSprite textureAtlasSprite2 = function.apply(this.getMaterial(blockElementFace.texture));
                if (blockElementFace.cullForDirection == null) {
                    builder.addUnculledFace(BlockModel.bakeFace(blockElement, blockElementFace, textureAtlasSprite2, direction, modelState, resourceLocation));
                    continue;
                }
                builder.addCulledFace(Direction.rotate(modelState.getRotation().getMatrix(), blockElementFace.cullForDirection), BlockModel.bakeFace(blockElement, blockElementFace, textureAtlasSprite2, direction, modelState, resourceLocation));
            }
        }
        return builder.build();
    }

    private static BakedQuad bakeFace(BlockElement blockElement, BlockElementFace blockElementFace, TextureAtlasSprite textureAtlasSprite, Direction direction, ModelState modelState, ResourceLocation resourceLocation) {
        return FACE_BAKERY.bakeQuad(blockElement.from, blockElement.to, blockElementFace, textureAtlasSprite, direction, modelState, blockElement.rotation, blockElement.shade, resourceLocation);
    }

    public boolean hasTexture(String string) {
        return !MissingTextureAtlasSprite.getLocation().equals(this.getMaterial(string).texture());
    }

    public Material getMaterial(String string) {
        if (BlockModel.isTextureReference(string)) {
            string = string.substring(1);
        }
        ArrayList<String> list = Lists.newArrayList();
        Either<Material, String> either;
        Optional<Material> optional;
        while (!(optional = (either = this.findTextureEntry(string)).left()).isPresent()) {
            string = either.right().get();
            if (list.contains(string)) {
                LOGGER.warn("Unable to resolve texture due to reference chain {}->{} in {}", (Object)Joiner.on("->").join(list), (Object)string, (Object)this.name);
                return new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation());
            }
            list.add(string);
        }
        return optional.get();
    }

    private Either<Material, String> findTextureEntry(String string) {
        BlockModel blockModel = this;
        while (blockModel != null) {
            Either<Material, String> either = blockModel.textureMap.get(string);
            if (either != null) {
                return either;
            }
            blockModel = blockModel.parent;
        }
        return Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation()));
    }

    private static boolean isTextureReference(String string) {
        return string.charAt(0) == '#';
    }

    public BlockModel getRootModel() {
        return this.parent == null ? this : this.parent.getRootModel();
    }

    public ItemTransforms getTransforms() {
        ItemTransform itemTransform = this.getTransform(ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND);
        ItemTransform itemTransform2 = this.getTransform(ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
        ItemTransform itemTransform3 = this.getTransform(ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND);
        ItemTransform itemTransform4 = this.getTransform(ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND);
        ItemTransform itemTransform5 = this.getTransform(ItemTransforms.TransformType.HEAD);
        ItemTransform itemTransform6 = this.getTransform(ItemTransforms.TransformType.GUI);
        ItemTransform itemTransform7 = this.getTransform(ItemTransforms.TransformType.GROUND);
        ItemTransform itemTransform8 = this.getTransform(ItemTransforms.TransformType.FIXED);
        return new ItemTransforms(itemTransform, itemTransform2, itemTransform3, itemTransform4, itemTransform5, itemTransform6, itemTransform7, itemTransform8);
    }

    private ItemTransform getTransform(ItemTransforms.TransformType transformType) {
        if (this.parent != null && !this.transforms.hasTransform(transformType)) {
            return this.parent.getTransform(transformType);
        }
        return this.transforms.getTransform(transformType);
    }

    public String toString() {
        return this.name;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum GuiLight {
        FRONT("front"),
        SIDE("side");

        private final String name;

        private GuiLight(String string2) {
            this.name = string2;
        }

        public static GuiLight getByName(String string) {
            for (GuiLight guiLight : GuiLight.values()) {
                if (!guiLight.name.equals(string)) continue;
                return guiLight;
            }
            throw new IllegalArgumentException("Invalid gui light: " + string);
        }

        public boolean lightLikeBlock() {
            return this == SIDE;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Deserializer
    implements JsonDeserializer<BlockModel> {
        @Override
        public BlockModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            List<BlockElement> list = this.getElements(jsonDeserializationContext, jsonObject);
            String string = this.getParentName(jsonObject);
            Map<String, Either<Material, String>> map = this.getTextureMap(jsonObject);
            boolean bl = this.getAmbientOcclusion(jsonObject);
            ItemTransforms itemTransforms = ItemTransforms.NO_TRANSFORMS;
            if (jsonObject.has("display")) {
                JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "display");
                itemTransforms = (ItemTransforms)jsonDeserializationContext.deserialize(jsonObject2, (Type)((Object)ItemTransforms.class));
            }
            List<ItemOverride> list2 = this.getOverrides(jsonDeserializationContext, jsonObject);
            GuiLight guiLight = null;
            if (jsonObject.has("gui_light")) {
                guiLight = GuiLight.getByName(GsonHelper.getAsString(jsonObject, "gui_light"));
            }
            ResourceLocation resourceLocation = string.isEmpty() ? null : new ResourceLocation(string);
            return new BlockModel(resourceLocation, list, map, bl, guiLight, itemTransforms, list2);
        }

        protected List<ItemOverride> getOverrides(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
            ArrayList<ItemOverride> list = Lists.newArrayList();
            if (jsonObject.has("overrides")) {
                JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "overrides");
                for (JsonElement jsonElement : jsonArray) {
                    list.add((ItemOverride)jsonDeserializationContext.deserialize(jsonElement, (Type)((Object)ItemOverride.class)));
                }
            }
            return list;
        }

        private Map<String, Either<Material, String>> getTextureMap(JsonObject jsonObject) {
            ResourceLocation resourceLocation = TextureAtlas.LOCATION_BLOCKS;
            HashMap<String, Either<Material, String>> map = Maps.newHashMap();
            if (jsonObject.has("textures")) {
                JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "textures");
                for (Map.Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
                    map.put(entry.getKey(), Deserializer.parseTextureLocationOrReference(resourceLocation, entry.getValue().getAsString()));
                }
            }
            return map;
        }

        private static Either<Material, String> parseTextureLocationOrReference(ResourceLocation resourceLocation, String string) {
            if (BlockModel.isTextureReference(string)) {
                return Either.right(string.substring(1));
            }
            ResourceLocation resourceLocation2 = ResourceLocation.tryParse(string);
            if (resourceLocation2 == null) {
                throw new JsonParseException(string + " is not valid resource location");
            }
            return Either.left(new Material(resourceLocation, resourceLocation2));
        }

        private String getParentName(JsonObject jsonObject) {
            return GsonHelper.getAsString(jsonObject, "parent", "");
        }

        protected boolean getAmbientOcclusion(JsonObject jsonObject) {
            return GsonHelper.getAsBoolean(jsonObject, "ambientocclusion", true);
        }

        protected List<BlockElement> getElements(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
            ArrayList<BlockElement> list = Lists.newArrayList();
            if (jsonObject.has("elements")) {
                for (JsonElement jsonElement : GsonHelper.getAsJsonArray(jsonObject, "elements")) {
                    list.add((BlockElement)jsonDeserializationContext.deserialize(jsonElement, (Type)((Object)BlockElement.class)));
                }
            }
            return list;
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}


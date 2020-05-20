/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.texture.AtlasSet;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ModelBakery {
    public static final Material FIRE_0 = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/fire_0"));
    public static final Material FIRE_1 = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/fire_1"));
    public static final Material LAVA_FLOW = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/lava_flow"));
    public static final Material WATER_FLOW = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/water_flow"));
    public static final Material WATER_OVERLAY = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/water_overlay"));
    public static final Material BANNER_BASE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/banner_base"));
    public static final Material SHIELD_BASE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/shield_base"));
    public static final Material NO_PATTERN_SHIELD = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/shield_base_nopattern"));
    public static final List<ResourceLocation> DESTROY_STAGES = IntStream.range(0, 10).mapToObj(i -> new ResourceLocation("block/destroy_stage_" + i)).collect(Collectors.toList());
    public static final List<ResourceLocation> BREAKING_LOCATIONS = DESTROY_STAGES.stream().map(resourceLocation -> new ResourceLocation("textures/" + resourceLocation.getPath() + ".png")).collect(Collectors.toList());
    public static final List<RenderType> DESTROY_TYPES = BREAKING_LOCATIONS.stream().map(RenderType::crumbling).collect(Collectors.toList());
    private static final Set<Material> UNREFERENCED_TEXTURES = Util.make(Sets.newHashSet(), hashSet -> {
        hashSet.add(WATER_FLOW);
        hashSet.add(LAVA_FLOW);
        hashSet.add(WATER_OVERLAY);
        hashSet.add(FIRE_0);
        hashSet.add(FIRE_1);
        hashSet.add(BellRenderer.BELL_RESOURCE_LOCATION);
        hashSet.add(ConduitRenderer.SHELL_TEXTURE);
        hashSet.add(ConduitRenderer.ACTIVE_SHELL_TEXTURE);
        hashSet.add(ConduitRenderer.WIND_TEXTURE);
        hashSet.add(ConduitRenderer.VERTICAL_WIND_TEXTURE);
        hashSet.add(ConduitRenderer.OPEN_EYE_TEXTURE);
        hashSet.add(ConduitRenderer.CLOSED_EYE_TEXTURE);
        hashSet.add(EnchantTableRenderer.BOOK_LOCATION);
        hashSet.add(BANNER_BASE);
        hashSet.add(SHIELD_BASE);
        hashSet.add(NO_PATTERN_SHIELD);
        for (ResourceLocation resourceLocation : DESTROY_STAGES) {
            hashSet.add(new Material(TextureAtlas.LOCATION_BLOCKS, resourceLocation));
        }
        hashSet.add(new Material(TextureAtlas.LOCATION_BLOCKS, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET));
        hashSet.add(new Material(TextureAtlas.LOCATION_BLOCKS, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE));
        hashSet.add(new Material(TextureAtlas.LOCATION_BLOCKS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS));
        hashSet.add(new Material(TextureAtlas.LOCATION_BLOCKS, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS));
        hashSet.add(new Material(TextureAtlas.LOCATION_BLOCKS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD));
        Sheets.getAllMaterials(hashSet::add);
    });
    private static final Logger LOGGER = LogManager.getLogger();
    public static final ModelResourceLocation MISSING_MODEL_LOCATION = new ModelResourceLocation("builtin/missing", "missing");
    private static final String MISSING_MODEL_LOCATION_STRING = MISSING_MODEL_LOCATION.toString();
    @VisibleForTesting
    public static final String MISSING_MODEL_MESH = ("{    'textures': {       'particle': '" + MissingTextureAtlasSprite.getLocation().getPath() + "',       'missingno': '" + MissingTextureAtlasSprite.getLocation().getPath() + "'    },    'elements': [         {  'from': [ 0, 0, 0 ],            'to': [ 16, 16, 16 ],            'faces': {                'down':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'down',  'texture': '#missingno' },                'up':    { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'up',    'texture': '#missingno' },                'north': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'north', 'texture': '#missingno' },                'south': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'south', 'texture': '#missingno' },                'west':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'west',  'texture': '#missingno' },                'east':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'east',  'texture': '#missingno' }            }        }    ]}").replace('\'', '\"');
    private static final Map<String, String> BUILTIN_MODELS = Maps.newHashMap(ImmutableMap.of("missing", MISSING_MODEL_MESH));
    private static final Splitter COMMA_SPLITTER = Splitter.on(',');
    private static final Splitter EQUAL_SPLITTER = Splitter.on('=').limit(2);
    public static final BlockModel GENERATION_MARKER = Util.make(BlockModel.fromString("{\"gui_light\": \"front\"}"), blockModel -> {
        blockModel.name = "generation marker";
    });
    public static final BlockModel BLOCK_ENTITY_MARKER = Util.make(BlockModel.fromString("{\"gui_light\": \"side\"}"), blockModel -> {
        blockModel.name = "block entity marker";
    });
    private static final StateDefinition<Block, BlockState> ITEM_FRAME_FAKE_DEFINITION = new StateDefinition.Builder(Blocks.AIR).add(BooleanProperty.create("map")).create(Block::defaultBlockState, BlockState::new);
    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    private static final Map<ResourceLocation, StateDefinition<Block, BlockState>> STATIC_DEFINITIONS = ImmutableMap.of(new ResourceLocation("item_frame"), ITEM_FRAME_FAKE_DEFINITION);
    private final ResourceManager resourceManager;
    @Nullable
    private AtlasSet atlasSet;
    private final BlockColors blockColors;
    private final Set<ResourceLocation> loadingStack = Sets.newHashSet();
    private final BlockModelDefinition.Context context = new BlockModelDefinition.Context();
    private final Map<ResourceLocation, UnbakedModel> unbakedCache = Maps.newHashMap();
    private final Map<Triple<ResourceLocation, Transformation, Boolean>, BakedModel> bakedCache = Maps.newHashMap();
    private final Map<ResourceLocation, UnbakedModel> topLevelModels = Maps.newHashMap();
    private final Map<ResourceLocation, BakedModel> bakedTopLevelModels = Maps.newHashMap();
    private final Map<ResourceLocation, Pair<TextureAtlas, TextureAtlas.Preparations>> atlasPreparations;
    private int nextModelGroup = 1;
    private final Object2IntMap<BlockState> modelGroups = Util.make(new Object2IntOpenHashMap(), object2IntOpenHashMap -> object2IntOpenHashMap.defaultReturnValue(-1));

    public ModelBakery(ResourceManager resourceManager, BlockColors blockColors, ProfilerFiller profilerFiller, int i) {
        this.resourceManager = resourceManager;
        this.blockColors = blockColors;
        profilerFiller.push("missing_model");
        try {
            this.unbakedCache.put(MISSING_MODEL_LOCATION, this.loadBlockModel(MISSING_MODEL_LOCATION));
            this.loadTopLevel(MISSING_MODEL_LOCATION);
        } catch (IOException iOException) {
            LOGGER.error("Error loading missing model, should never happen :(", (Throwable)iOException);
            throw new RuntimeException(iOException);
        }
        profilerFiller.popPush("static_definitions");
        STATIC_DEFINITIONS.forEach((resourceLocation, stateDefinition) -> stateDefinition.getPossibleStates().forEach(blockState -> this.loadTopLevel(BlockModelShaper.stateToModelLocation(resourceLocation, blockState))));
        profilerFiller.popPush("blocks");
        for (Block block : Registry.BLOCK) {
            block.getStateDefinition().getPossibleStates().forEach(blockState -> this.loadTopLevel(BlockModelShaper.stateToModelLocation(blockState)));
        }
        profilerFiller.popPush("items");
        for (ResourceLocation resourceLocation2 : Registry.ITEM.keySet()) {
            this.loadTopLevel(new ModelResourceLocation(resourceLocation2, "inventory"));
        }
        profilerFiller.popPush("special");
        this.loadTopLevel(new ModelResourceLocation("minecraft:trident_in_hand#inventory"));
        profilerFiller.popPush("textures");
        LinkedHashSet set = Sets.newLinkedHashSet();
        Set set2 = this.topLevelModels.values().stream().flatMap(unbakedModel -> unbakedModel.getMaterials(this::getModel, set).stream()).collect(Collectors.toSet());
        set2.addAll(UNREFERENCED_TEXTURES);
        set.stream().filter(pair -> !((String)pair.getSecond()).equals(MISSING_MODEL_LOCATION_STRING)).forEach(pair -> LOGGER.warn("Unable to resolve texture reference: {} in {}", pair.getFirst(), pair.getSecond()));
        Map<ResourceLocation, List<Material>> map = set2.stream().collect(Collectors.groupingBy(Material::atlasLocation));
        profilerFiller.popPush("stitching");
        this.atlasPreparations = Maps.newHashMap();
        for (Map.Entry<ResourceLocation, List<Material>> entry : map.entrySet()) {
            TextureAtlas textureAtlas = new TextureAtlas(entry.getKey());
            TextureAtlas.Preparations preparations = textureAtlas.prepareToStitch(this.resourceManager, entry.getValue().stream().map(Material::texture), profilerFiller, i);
            this.atlasPreparations.put(entry.getKey(), Pair.of(textureAtlas, preparations));
        }
        profilerFiller.pop();
    }

    public AtlasSet uploadTextures(TextureManager textureManager, ProfilerFiller profilerFiller) {
        profilerFiller.push("atlas");
        for (Pair<TextureAtlas, TextureAtlas.Preparations> pair : this.atlasPreparations.values()) {
            TextureAtlas textureAtlas = pair.getFirst();
            TextureAtlas.Preparations preparations = pair.getSecond();
            textureAtlas.reload(preparations);
            textureManager.register(textureAtlas.location(), textureAtlas);
            textureManager.bind(textureAtlas.location());
            textureAtlas.updateFilter(preparations);
        }
        this.atlasSet = new AtlasSet(this.atlasPreparations.values().stream().map(Pair::getFirst).collect(Collectors.toList()));
        profilerFiller.popPush("baking");
        this.topLevelModels.keySet().forEach(resourceLocation -> {
            BakedModel bakedModel = null;
            try {
                bakedModel = this.bake((ResourceLocation)resourceLocation, BlockModelRotation.X0_Y0);
            } catch (Exception exception) {
                LOGGER.warn("Unable to bake model: '{}': {}", resourceLocation, (Object)exception);
            }
            if (bakedModel != null) {
                this.bakedTopLevelModels.put((ResourceLocation)resourceLocation, bakedModel);
            }
        });
        profilerFiller.pop();
        return this.atlasSet;
    }

    private static Predicate<BlockState> predicate(StateDefinition<Block, BlockState> stateDefinition, String string) {
        HashMap<Property<?>, ?> map = Maps.newHashMap();
        for (String string2 : COMMA_SPLITTER.split(string)) {
            Iterator<String> iterator = EQUAL_SPLITTER.split(string2).iterator();
            if (!iterator.hasNext()) continue;
            String string3 = iterator.next();
            Property<?> property = stateDefinition.getProperty(string3);
            if (property != null && iterator.hasNext()) {
                String string4 = iterator.next();
                Object comparable = ModelBakery.getValueHelper(property, string4);
                if (comparable != null) {
                    map.put(property, comparable);
                    continue;
                }
                throw new RuntimeException("Unknown value: '" + string4 + "' for blockstate property: '" + string3 + "' " + property.getPossibleValues());
            }
            if (string3.isEmpty()) continue;
            throw new RuntimeException("Unknown blockstate property: '" + string3 + "'");
        }
        Block block = stateDefinition.getOwner();
        return blockState -> {
            if (blockState == null || block != blockState.getBlock()) {
                return false;
            }
            for (Map.Entry entry : map.entrySet()) {
                if (Objects.equals(blockState.getValue((Property)entry.getKey()), entry.getValue())) continue;
                return false;
            }
            return true;
        };
    }

    @Nullable
    static <T extends Comparable<T>> T getValueHelper(Property<T> property, String string) {
        return (T)((Comparable)property.getValue(string).orElse(null));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public UnbakedModel getModel(ResourceLocation resourceLocation) {
        if (this.unbakedCache.containsKey(resourceLocation)) {
            return this.unbakedCache.get(resourceLocation);
        }
        if (this.loadingStack.contains(resourceLocation)) {
            throw new IllegalStateException("Circular reference while loading " + resourceLocation);
        }
        this.loadingStack.add(resourceLocation);
        UnbakedModel unbakedModel = this.unbakedCache.get(MISSING_MODEL_LOCATION);
        while (!this.loadingStack.isEmpty()) {
            ResourceLocation resourceLocation2 = this.loadingStack.iterator().next();
            try {
                if (this.unbakedCache.containsKey(resourceLocation2)) continue;
                this.loadModel(resourceLocation2);
            } catch (BlockStateDefinitionException blockStateDefinitionException) {
                LOGGER.warn(blockStateDefinitionException.getMessage());
                this.unbakedCache.put(resourceLocation2, unbakedModel);
            } catch (Exception exception) {
                LOGGER.warn("Unable to load model: '{}' referenced from: {}: {}", (Object)resourceLocation2, (Object)resourceLocation, (Object)exception);
                this.unbakedCache.put(resourceLocation2, unbakedModel);
            } finally {
                this.loadingStack.remove(resourceLocation2);
            }
        }
        return this.unbakedCache.getOrDefault(resourceLocation, unbakedModel);
    }

    private void loadModel(ResourceLocation resourceLocation) throws Exception {
        if (!(resourceLocation instanceof ModelResourceLocation)) {
            this.cacheAndQueueDependencies(resourceLocation, this.loadBlockModel(resourceLocation));
            return;
        }
        ModelResourceLocation modelResourceLocation2 = (ModelResourceLocation)resourceLocation;
        if (Objects.equals(modelResourceLocation2.getVariant(), "inventory")) {
            ResourceLocation resourceLocation2 = new ResourceLocation(resourceLocation.getNamespace(), "item/" + resourceLocation.getPath());
            BlockModel blockModel = this.loadBlockModel(resourceLocation2);
            this.cacheAndQueueDependencies(modelResourceLocation2, blockModel);
            this.unbakedCache.put(resourceLocation2, blockModel);
        } else {
            ResourceLocation resourceLocation2 = new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath());
            StateDefinition stateDefinition = Optional.ofNullable(STATIC_DEFINITIONS.get(resourceLocation2)).orElseGet(() -> Registry.BLOCK.get(resourceLocation2).getStateDefinition());
            this.context.setDefinition(stateDefinition);
            ImmutableList<Property<?>> list = ImmutableList.copyOf(this.blockColors.getColoringProperties((Block)stateDefinition.getOwner()));
            ImmutableList immutableList = stateDefinition.getPossibleStates();
            HashMap<ModelResourceLocation, BlockState> map = Maps.newHashMap();
            immutableList.forEach(blockState -> map.put(BlockModelShaper.stateToModelLocation(resourceLocation2, blockState), (BlockState)blockState));
            HashMap map2 = Maps.newHashMap();
            ResourceLocation resourceLocation3 = new ResourceLocation(resourceLocation.getNamespace(), "blockstates/" + resourceLocation.getPath() + ".json");
            UnbakedModel unbakedModel = this.unbakedCache.get(MISSING_MODEL_LOCATION);
            ModelGroupKey modelGroupKey2 = new ModelGroupKey(ImmutableList.of(unbakedModel), ImmutableList.of());
            Pair<UnbakedModel, Supplier<ModelGroupKey>> pair = Pair.of(unbakedModel, () -> modelGroupKey2);
            try {
                List list2;
                try {
                    list2 = this.resourceManager.getResources(resourceLocation3).stream().map(resource -> {
                        try (InputStream inputStream = resource.getInputStream();){
                            Pair<String, BlockModelDefinition> pair = Pair.of(resource.getSourceName(), BlockModelDefinition.fromStream(this.context, new InputStreamReader(inputStream, StandardCharsets.UTF_8)));
                            return pair;
                        } catch (Exception exception) {
                            throw new BlockStateDefinitionException(String.format("Exception loading blockstate definition: '%s' in resourcepack: '%s': %s", resource.getLocation(), resource.getSourceName(), exception.getMessage()));
                        }
                    }).collect(Collectors.toList());
                } catch (IOException iOException) {
                    LOGGER.warn("Exception loading blockstate definition: {}: {}", (Object)resourceLocation3, (Object)iOException);
                    HashMap<ModelGroupKey, Set> map3 = Maps.newHashMap();
                    map.forEach((modelResourceLocation, blockState) -> {
                        Pair pair2 = (Pair)map2.get(blockState);
                        if (pair2 == null) {
                            LOGGER.warn("Exception loading blockstate definition: '{}' missing model for variant: '{}'", (Object)resourceLocation3, modelResourceLocation);
                            pair2 = pair;
                        }
                        this.cacheAndQueueDependencies((ResourceLocation)modelResourceLocation, (UnbakedModel)pair2.getFirst());
                        try {
                            ModelGroupKey modelGroupKey2 = (ModelGroupKey)((Supplier)pair2.getSecond()).get();
                            map3.computeIfAbsent(modelGroupKey2, modelGroupKey -> Sets.newIdentityHashSet()).add(blockState);
                        } catch (Exception exception) {
                            LOGGER.warn("Exception evaluating model definition: '{}'", modelResourceLocation, (Object)exception);
                        }
                    });
                    map3.forEach((modelGroupKey, set) -> {
                        Iterator iterator = set.iterator();
                        while (iterator.hasNext()) {
                            BlockState blockState = (BlockState)iterator.next();
                            if (blockState.getRenderShape() == RenderShape.MODEL) continue;
                            iterator.remove();
                            this.modelGroups.put(blockState, 0);
                        }
                        if (set.size() > 1) {
                            this.registerModelGroup((Iterable<BlockState>)set);
                        }
                    });
                    return;
                }
                for (Pair pair2 : list2) {
                    MultiPart multiPart;
                    BlockModelDefinition blockModelDefinition = (BlockModelDefinition)pair2.getSecond();
                    IdentityHashMap map4 = Maps.newIdentityHashMap();
                    if (blockModelDefinition.isMultiPart()) {
                        multiPart = blockModelDefinition.getMultiPart();
                        immutableList.forEach(blockState -> map4.put(blockState, Pair.of(multiPart, () -> ModelGroupKey.create(blockState, multiPart, list))));
                    } else {
                        multiPart = null;
                    }
                    blockModelDefinition.getVariants().forEach((string, multiVariant) -> {
                        try {
                            immutableList.stream().filter(ModelBakery.predicate(stateDefinition, string)).forEach(blockState -> {
                                Pair<MultiVariant, Supplier<ModelGroupKey>> pair2 = map4.put(blockState, Pair.of(multiVariant, () -> ModelGroupKey.create(blockState, multiVariant, list)));
                                if (pair2 != null && pair2.getFirst() != multiPart) {
                                    map4.put(blockState, pair);
                                    throw new RuntimeException("Overlapping definition with: " + (String)blockModelDefinition.getVariants().entrySet().stream().filter(entry -> entry.getValue() == pair2.getFirst()).findFirst().get().getKey());
                                }
                            });
                        } catch (Exception exception) {
                            LOGGER.warn("Exception loading blockstate definition: '{}' in resourcepack: '{}' for variant: '{}': {}", (Object)resourceLocation3, pair2.getFirst(), string, (Object)exception.getMessage());
                        }
                    });
                    map2.putAll(map4);
                }
            } catch (BlockStateDefinitionException blockStateDefinitionException) {
                throw blockStateDefinitionException;
            } catch (Exception exception) {
                throw new BlockStateDefinitionException(String.format("Exception loading blockstate definition: '%s': %s", resourceLocation3, exception));
            } finally {
                HashMap<ModelGroupKey, Set> map6 = Maps.newHashMap();
                map.forEach((modelResourceLocation, blockState) -> {
                    Pair pair2 = (Pair)map2.get(blockState);
                    if (pair2 == null) {
                        LOGGER.warn("Exception loading blockstate definition: '{}' missing model for variant: '{}'", (Object)resourceLocation3, modelResourceLocation);
                        pair2 = pair;
                    }
                    this.cacheAndQueueDependencies((ResourceLocation)modelResourceLocation, (UnbakedModel)pair2.getFirst());
                    try {
                        ModelGroupKey modelGroupKey2 = (ModelGroupKey)((Supplier)pair2.getSecond()).get();
                        map3.computeIfAbsent(modelGroupKey2, modelGroupKey -> Sets.newIdentityHashSet()).add(blockState);
                    } catch (Exception exception) {
                        LOGGER.warn("Exception evaluating model definition: '{}'", modelResourceLocation, (Object)exception);
                    }
                });
                map6.forEach((modelGroupKey, set) -> {
                    Iterator iterator = set.iterator();
                    while (iterator.hasNext()) {
                        BlockState blockState = (BlockState)iterator.next();
                        if (blockState.getRenderShape() == RenderShape.MODEL) continue;
                        iterator.remove();
                        this.modelGroups.put(blockState, 0);
                    }
                    if (set.size() > 1) {
                        this.registerModelGroup((Iterable<BlockState>)set);
                    }
                });
            }
        }
    }

    private void cacheAndQueueDependencies(ResourceLocation resourceLocation, UnbakedModel unbakedModel) {
        this.unbakedCache.put(resourceLocation, unbakedModel);
        this.loadingStack.addAll(unbakedModel.getDependencies());
    }

    private void loadTopLevel(ModelResourceLocation modelResourceLocation) {
        UnbakedModel unbakedModel = this.getModel(modelResourceLocation);
        this.unbakedCache.put(modelResourceLocation, unbakedModel);
        this.topLevelModels.put(modelResourceLocation, unbakedModel);
    }

    private void registerModelGroup(Iterable<BlockState> iterable) {
        int i = this.nextModelGroup++;
        iterable.forEach(blockState -> this.modelGroups.put((BlockState)blockState, i));
    }

    @Nullable
    public BakedModel bake(ResourceLocation resourceLocation, ModelState modelState) {
        BlockModel blockModel;
        Triple<ResourceLocation, Transformation, Boolean> triple = Triple.of(resourceLocation, modelState.getRotation(), modelState.isUvLocked());
        if (this.bakedCache.containsKey(triple)) {
            return this.bakedCache.get(triple);
        }
        if (this.atlasSet == null) {
            throw new IllegalStateException("bake called too early");
        }
        UnbakedModel unbakedModel = this.getModel(resourceLocation);
        if (unbakedModel instanceof BlockModel && (blockModel = (BlockModel)unbakedModel).getRootModel() == GENERATION_MARKER) {
            return ITEM_MODEL_GENERATOR.generateBlockModel(this.atlasSet::getSprite, blockModel).bake(this, blockModel, this.atlasSet::getSprite, modelState, resourceLocation, false);
        }
        BakedModel bakedModel = unbakedModel.bake(this, this.atlasSet::getSprite, modelState, resourceLocation);
        this.bakedCache.put(triple, bakedModel);
        return bakedModel;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private BlockModel loadBlockModel(ResourceLocation resourceLocation) throws IOException {
        String string;
        Resource resource;
        Reader reader;
        block8: {
            block7: {
                BlockModel blockModel;
                reader = null;
                resource = null;
                try {
                    string = resourceLocation.getPath();
                    if (!"builtin/generated".equals(string)) break block7;
                    blockModel = GENERATION_MARKER;
                } catch (Throwable throwable) {
                    IOUtils.closeQuietly(reader);
                    IOUtils.closeQuietly(resource);
                    throw throwable;
                }
                IOUtils.closeQuietly(reader);
                IOUtils.closeQuietly(resource);
                return blockModel;
            }
            if (!"builtin/entity".equals(string)) break block8;
            BlockModel blockModel = BLOCK_ENTITY_MARKER;
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(resource);
            return blockModel;
        }
        if (string.startsWith("builtin/")) {
            String string2 = string.substring("builtin/".length());
            String string3 = BUILTIN_MODELS.get(string2);
            if (string3 == null) {
                throw new FileNotFoundException(resourceLocation.toString());
            }
            reader = new StringReader(string3);
        } else {
            resource = this.resourceManager.getResource(new ResourceLocation(resourceLocation.getNamespace(), "models/" + resourceLocation.getPath() + ".json"));
            reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
        }
        BlockModel blockModel = BlockModel.fromStream(reader);
        blockModel.name = resourceLocation.toString();
        BlockModel blockModel2 = blockModel;
        IOUtils.closeQuietly(reader);
        IOUtils.closeQuietly((Closeable)resource);
        return blockModel2;
    }

    public Map<ResourceLocation, BakedModel> getBakedTopLevelModels() {
        return this.bakedTopLevelModels;
    }

    public Object2IntMap<BlockState> getModelGroups() {
        return this.modelGroups;
    }

    @Environment(value=EnvType.CLIENT)
    static class ModelGroupKey {
        private final List<UnbakedModel> models;
        private final List<Object> coloringValues;

        public ModelGroupKey(List<UnbakedModel> list, List<Object> list2) {
            this.models = list;
            this.coloringValues = list2;
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof ModelGroupKey) {
                ModelGroupKey modelGroupKey = (ModelGroupKey)object;
                return Objects.equals(this.models, modelGroupKey.models) && Objects.equals(this.coloringValues, modelGroupKey.coloringValues);
            }
            return false;
        }

        public int hashCode() {
            return 31 * this.models.hashCode() + this.coloringValues.hashCode();
        }

        public static ModelGroupKey create(BlockState blockState, MultiPart multiPart, Collection<Property<?>> collection) {
            StateDefinition<Block, BlockState> stateDefinition = blockState.getBlock().getStateDefinition();
            List list = multiPart.getSelectors().stream().filter(selector -> selector.getPredicate(stateDefinition).test(blockState)).map(Selector::getVariant).collect(ImmutableList.toImmutableList());
            List<Object> list2 = ModelGroupKey.getColoringValues(blockState, collection);
            return new ModelGroupKey(list, list2);
        }

        public static ModelGroupKey create(BlockState blockState, UnbakedModel unbakedModel, Collection<Property<?>> collection) {
            List<Object> list = ModelGroupKey.getColoringValues(blockState, collection);
            return new ModelGroupKey(ImmutableList.of(unbakedModel), list);
        }

        private static List<Object> getColoringValues(BlockState blockState, Collection<Property<?>> collection) {
            return collection.stream().map(blockState::getValue).collect(ImmutableList.toImmutableList());
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class BlockStateDefinitionException
    extends RuntimeException {
        public BlockStateDefinitionException(String string) {
            super(string);
        }
    }
}


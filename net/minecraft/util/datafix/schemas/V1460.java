/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.schemas;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import net.minecraft.util.datafix.schemas.V100;
import net.minecraft.util.datafix.schemas.V1451_6;
import net.minecraft.util.datafix.schemas.V705;

public class V1460
extends NamespacedSchema {
    public V1460(int i, Schema schema) {
        super(i, schema);
    }

    protected static void registerMob(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
        schema.register(map, string, () -> V100.equipment(schema));
    }

    protected static void registerInventory(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
        schema.register(map, string, () -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(schema))));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        HashMap<String, Supplier<TypeTemplate>> map = Maps.newHashMap();
        schema.registerSimple(map, "minecraft:area_effect_cloud");
        V1460.registerMob(schema, map, "minecraft:armor_stand");
        schema.register(map, "minecraft:arrow", (String string) -> DSL.optionalFields("inBlockState", References.BLOCK_STATE.in(schema)));
        V1460.registerMob(schema, map, "minecraft:bat");
        V1460.registerMob(schema, map, "minecraft:blaze");
        schema.registerSimple(map, "minecraft:boat");
        V1460.registerMob(schema, map, "minecraft:cave_spider");
        schema.register(map, "minecraft:chest_minecart", (String string) -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(schema), "Items", DSL.list(References.ITEM_STACK.in(schema))));
        V1460.registerMob(schema, map, "minecraft:chicken");
        schema.register(map, "minecraft:commandblock_minecart", (String string) -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(schema)));
        V1460.registerMob(schema, map, "minecraft:cow");
        V1460.registerMob(schema, map, "minecraft:creeper");
        schema.register(map, "minecraft:donkey", (String string) -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(schema)), "SaddleItem", References.ITEM_STACK.in(schema), V100.equipment(schema)));
        schema.registerSimple(map, "minecraft:dragon_fireball");
        schema.registerSimple(map, "minecraft:egg");
        V1460.registerMob(schema, map, "minecraft:elder_guardian");
        schema.registerSimple(map, "minecraft:ender_crystal");
        V1460.registerMob(schema, map, "minecraft:ender_dragon");
        schema.register(map, "minecraft:enderman", (String string) -> DSL.optionalFields("carriedBlockState", References.BLOCK_STATE.in(schema), V100.equipment(schema)));
        V1460.registerMob(schema, map, "minecraft:endermite");
        schema.registerSimple(map, "minecraft:ender_pearl");
        schema.registerSimple(map, "minecraft:evocation_fangs");
        V1460.registerMob(schema, map, "minecraft:evocation_illager");
        schema.registerSimple(map, "minecraft:eye_of_ender_signal");
        schema.register(map, "minecraft:falling_block", (String string) -> DSL.optionalFields("BlockState", References.BLOCK_STATE.in(schema), "TileEntityData", References.BLOCK_ENTITY.in(schema)));
        schema.registerSimple(map, "minecraft:fireball");
        schema.register(map, "minecraft:fireworks_rocket", (String string) -> DSL.optionalFields("FireworksItem", References.ITEM_STACK.in(schema)));
        schema.register(map, "minecraft:furnace_minecart", (String string) -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(schema)));
        V1460.registerMob(schema, map, "minecraft:ghast");
        V1460.registerMob(schema, map, "minecraft:giant");
        V1460.registerMob(schema, map, "minecraft:guardian");
        schema.register(map, "minecraft:hopper_minecart", (String string) -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(schema), "Items", DSL.list(References.ITEM_STACK.in(schema))));
        schema.register(map, "minecraft:horse", (String string) -> DSL.optionalFields("ArmorItem", References.ITEM_STACK.in(schema), "SaddleItem", References.ITEM_STACK.in(schema), V100.equipment(schema)));
        V1460.registerMob(schema, map, "minecraft:husk");
        schema.registerSimple(map, "minecraft:illusion_illager");
        schema.register(map, "minecraft:item", (String string) -> DSL.optionalFields("Item", References.ITEM_STACK.in(schema)));
        schema.register(map, "minecraft:item_frame", (String string) -> DSL.optionalFields("Item", References.ITEM_STACK.in(schema)));
        schema.registerSimple(map, "minecraft:leash_knot");
        schema.register(map, "minecraft:llama", (String string) -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(schema)), "SaddleItem", References.ITEM_STACK.in(schema), "DecorItem", References.ITEM_STACK.in(schema), V100.equipment(schema)));
        schema.registerSimple(map, "minecraft:llama_spit");
        V1460.registerMob(schema, map, "minecraft:magma_cube");
        schema.register(map, "minecraft:minecart", (String string) -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(schema)));
        V1460.registerMob(schema, map, "minecraft:mooshroom");
        schema.register(map, "minecraft:mule", (String string) -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(schema)), "SaddleItem", References.ITEM_STACK.in(schema), V100.equipment(schema)));
        V1460.registerMob(schema, map, "minecraft:ocelot");
        schema.registerSimple(map, "minecraft:painting");
        schema.registerSimple(map, "minecraft:parrot");
        V1460.registerMob(schema, map, "minecraft:pig");
        V1460.registerMob(schema, map, "minecraft:polar_bear");
        schema.register(map, "minecraft:potion", (String string) -> DSL.optionalFields("Potion", References.ITEM_STACK.in(schema)));
        V1460.registerMob(schema, map, "minecraft:rabbit");
        V1460.registerMob(schema, map, "minecraft:sheep");
        V1460.registerMob(schema, map, "minecraft:shulker");
        schema.registerSimple(map, "minecraft:shulker_bullet");
        V1460.registerMob(schema, map, "minecraft:silverfish");
        V1460.registerMob(schema, map, "minecraft:skeleton");
        schema.register(map, "minecraft:skeleton_horse", (String string) -> DSL.optionalFields("SaddleItem", References.ITEM_STACK.in(schema), V100.equipment(schema)));
        V1460.registerMob(schema, map, "minecraft:slime");
        schema.registerSimple(map, "minecraft:small_fireball");
        schema.registerSimple(map, "minecraft:snowball");
        V1460.registerMob(schema, map, "minecraft:snowman");
        schema.register(map, "minecraft:spawner_minecart", (String string) -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(schema), References.UNTAGGED_SPAWNER.in(schema)));
        schema.register(map, "minecraft:spectral_arrow", (String string) -> DSL.optionalFields("inBlockState", References.BLOCK_STATE.in(schema)));
        V1460.registerMob(schema, map, "minecraft:spider");
        V1460.registerMob(schema, map, "minecraft:squid");
        V1460.registerMob(schema, map, "minecraft:stray");
        schema.registerSimple(map, "minecraft:tnt");
        schema.register(map, "minecraft:tnt_minecart", (String string) -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in(schema)));
        V1460.registerMob(schema, map, "minecraft:vex");
        schema.register(map, "minecraft:villager", (String string) -> DSL.optionalFields("Inventory", DSL.list(References.ITEM_STACK.in(schema)), "Offers", DSL.optionalFields("Recipes", DSL.list(DSL.optionalFields("buy", References.ITEM_STACK.in(schema), "buyB", References.ITEM_STACK.in(schema), "sell", References.ITEM_STACK.in(schema)))), V100.equipment(schema)));
        V1460.registerMob(schema, map, "minecraft:villager_golem");
        V1460.registerMob(schema, map, "minecraft:vindication_illager");
        V1460.registerMob(schema, map, "minecraft:witch");
        V1460.registerMob(schema, map, "minecraft:wither");
        V1460.registerMob(schema, map, "minecraft:wither_skeleton");
        schema.registerSimple(map, "minecraft:wither_skull");
        V1460.registerMob(schema, map, "minecraft:wolf");
        schema.registerSimple(map, "minecraft:xp_bottle");
        schema.registerSimple(map, "minecraft:xp_orb");
        V1460.registerMob(schema, map, "minecraft:zombie");
        schema.register(map, "minecraft:zombie_horse", (String string) -> DSL.optionalFields("SaddleItem", References.ITEM_STACK.in(schema), V100.equipment(schema)));
        V1460.registerMob(schema, map, "minecraft:zombie_pigman");
        V1460.registerMob(schema, map, "minecraft:zombie_villager");
        return map;
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        HashMap<String, Supplier<TypeTemplate>> map = Maps.newHashMap();
        V1460.registerInventory(schema, map, "minecraft:furnace");
        V1460.registerInventory(schema, map, "minecraft:chest");
        V1460.registerInventory(schema, map, "minecraft:trapped_chest");
        schema.registerSimple(map, "minecraft:ender_chest");
        schema.register(map, "minecraft:jukebox", (String string) -> DSL.optionalFields("RecordItem", References.ITEM_STACK.in(schema)));
        V1460.registerInventory(schema, map, "minecraft:dispenser");
        V1460.registerInventory(schema, map, "minecraft:dropper");
        schema.registerSimple(map, "minecraft:sign");
        schema.register(map, "minecraft:mob_spawner", (String string) -> References.UNTAGGED_SPAWNER.in(schema));
        schema.register(map, "minecraft:piston", (String string) -> DSL.optionalFields("blockState", References.BLOCK_STATE.in(schema)));
        V1460.registerInventory(schema, map, "minecraft:brewing_stand");
        schema.registerSimple(map, "minecraft:enchanting_table");
        schema.registerSimple(map, "minecraft:end_portal");
        schema.registerSimple(map, "minecraft:beacon");
        schema.registerSimple(map, "minecraft:skull");
        schema.registerSimple(map, "minecraft:daylight_detector");
        V1460.registerInventory(schema, map, "minecraft:hopper");
        schema.registerSimple(map, "minecraft:comparator");
        schema.registerSimple(map, "minecraft:banner");
        schema.registerSimple(map, "minecraft:structure_block");
        schema.registerSimple(map, "minecraft:end_gateway");
        schema.registerSimple(map, "minecraft:command_block");
        V1460.registerInventory(schema, map, "minecraft:shulker_box");
        schema.registerSimple(map, "minecraft:bed");
        return map;
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        schema.registerType(false, References.LEVEL, DSL::remainder);
        schema.registerType(false, References.RECIPE, () -> DSL.constType(V1460.namespacedString()));
        schema.registerType(false, References.PLAYER, () -> DSL.optionalFields("RootVehicle", DSL.optionalFields("Entity", References.ENTITY_TREE.in(schema)), "Inventory", DSL.list(References.ITEM_STACK.in(schema)), "EnderItems", DSL.list(References.ITEM_STACK.in(schema)), DSL.optionalFields("ShoulderEntityLeft", References.ENTITY_TREE.in(schema), "ShoulderEntityRight", References.ENTITY_TREE.in(schema), "recipeBook", DSL.optionalFields("recipes", DSL.list(References.RECIPE.in(schema)), "toBeDisplayed", DSL.list(References.RECIPE.in(schema))))));
        schema.registerType(false, References.CHUNK, () -> DSL.fields("Level", DSL.optionalFields("Entities", DSL.list(References.ENTITY_TREE.in(schema)), "TileEntities", DSL.list(DSL.or(References.BLOCK_ENTITY.in(schema), DSL.remainder())), "TileTicks", DSL.list(DSL.fields("i", References.BLOCK_NAME.in(schema))), "Sections", DSL.list(DSL.optionalFields("Palette", DSL.list(References.BLOCK_STATE.in(schema)))))));
        schema.registerType(true, References.BLOCK_ENTITY, () -> DSL.taggedChoiceLazy("id", V1460.namespacedString(), map2));
        schema.registerType(true, References.ENTITY_TREE, () -> DSL.optionalFields("Passengers", DSL.list(References.ENTITY_TREE.in(schema)), References.ENTITY.in(schema)));
        schema.registerType(true, References.ENTITY, () -> DSL.taggedChoiceLazy("id", V1460.namespacedString(), map));
        schema.registerType(true, References.ITEM_STACK, () -> DSL.hook(DSL.optionalFields("id", References.ITEM_NAME.in(schema), "tag", DSL.optionalFields("EntityTag", References.ENTITY_TREE.in(schema), "BlockEntityTag", References.BLOCK_ENTITY.in(schema), "CanDestroy", DSL.list(References.BLOCK_NAME.in(schema)), "CanPlaceOn", DSL.list(References.BLOCK_NAME.in(schema)), "Items", DSL.list(References.ITEM_STACK.in(schema)))), V705.ADD_NAMES, Hook.HookFunction.IDENTITY));
        schema.registerType(false, References.HOTBAR, () -> DSL.compoundList(DSL.list(References.ITEM_STACK.in(schema))));
        schema.registerType(false, References.OPTIONS, DSL::remainder);
        schema.registerType(false, References.STRUCTURE, () -> DSL.optionalFields("entities", DSL.list(DSL.optionalFields("nbt", References.ENTITY_TREE.in(schema))), "blocks", DSL.list(DSL.optionalFields("nbt", References.BLOCK_ENTITY.in(schema))), "palette", DSL.list(References.BLOCK_STATE.in(schema))));
        schema.registerType(false, References.BLOCK_NAME, () -> DSL.constType(V1460.namespacedString()));
        schema.registerType(false, References.ITEM_NAME, () -> DSL.constType(V1460.namespacedString()));
        schema.registerType(false, References.BLOCK_STATE, DSL::remainder);
        Supplier<TypeTemplate> supplier = () -> DSL.compoundList(References.ITEM_NAME.in(schema), DSL.constType(DSL.intType()));
        schema.registerType(false, References.STATS, () -> DSL.optionalFields("stats", DSL.optionalFields("minecraft:mined", DSL.compoundList(References.BLOCK_NAME.in(schema), DSL.constType(DSL.intType())), "minecraft:crafted", (TypeTemplate)supplier.get(), "minecraft:used", (TypeTemplate)supplier.get(), "minecraft:broken", (TypeTemplate)supplier.get(), "minecraft:picked_up", (TypeTemplate)supplier.get(), DSL.optionalFields("minecraft:dropped", (TypeTemplate)supplier.get(), "minecraft:killed", DSL.compoundList(References.ENTITY_NAME.in(schema), DSL.constType(DSL.intType())), "minecraft:killed_by", DSL.compoundList(References.ENTITY_NAME.in(schema), DSL.constType(DSL.intType())), "minecraft:custom", DSL.compoundList(DSL.constType(V1460.namespacedString()), DSL.constType(DSL.intType()))))));
        schema.registerType(false, References.SAVED_DATA, () -> DSL.optionalFields("data", DSL.optionalFields("Features", DSL.compoundList(References.STRUCTURE_FEATURE.in(schema)), "Objectives", DSL.list(References.OBJECTIVE.in(schema)), "Teams", DSL.list(References.TEAM.in(schema)))));
        schema.registerType(false, References.STRUCTURE_FEATURE, DSL::remainder);
        Map<String, Supplier<TypeTemplate>> map3 = V1451_6.createCriterionTypes(schema);
        schema.registerType(false, References.OBJECTIVE, () -> DSL.hook(DSL.optionalFields("CriteriaType", DSL.taggedChoiceLazy("type", DSL.string(), map3)), V1451_6.UNPACK_OBJECTIVE_ID, V1451_6.REPACK_OBJECTIVE_ID));
        schema.registerType(false, References.TEAM, DSL::remainder);
        schema.registerType(true, References.UNTAGGED_SPAWNER, () -> DSL.optionalFields("SpawnPotentials", DSL.list(DSL.fields("Entity", References.ENTITY_TREE.in(schema))), "SpawnData", References.ENTITY_TREE.in(schema)));
        schema.registerType(false, References.ADVANCEMENTS, () -> DSL.optionalFields("minecraft:adventure/adventuring_time", DSL.optionalFields("criteria", DSL.compoundList(References.BIOME.in(schema), DSL.constType(DSL.string()))), "minecraft:adventure/kill_a_mob", DSL.optionalFields("criteria", DSL.compoundList(References.ENTITY_NAME.in(schema), DSL.constType(DSL.string()))), "minecraft:adventure/kill_all_mobs", DSL.optionalFields("criteria", DSL.compoundList(References.ENTITY_NAME.in(schema), DSL.constType(DSL.string()))), "minecraft:husbandry/bred_all_animals", DSL.optionalFields("criteria", DSL.compoundList(References.ENTITY_NAME.in(schema), DSL.constType(DSL.string())))));
        schema.registerType(false, References.BIOME, () -> DSL.constType(V1460.namespacedString()));
        schema.registerType(false, References.ENTITY_NAME, () -> DSL.constType(V1460.namespacedString()));
        schema.registerType(false, References.POI_CHUNK, DSL::remainder);
        schema.registerType(false, References.WORLD_GEN_SETTINGS, DSL::remainder);
        schema.registerType(false, References.ENTITY_CHUNK, () -> DSL.optionalFields("Entities", DSL.list(References.ENTITY_TREE.in(schema))));
    }
}


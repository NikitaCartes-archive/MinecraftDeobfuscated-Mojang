/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.schemas;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class V99
extends Schema {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<String, String> ITEM_TO_BLOCKENTITY = DataFixUtils.make(Maps.newHashMap(), hashMap -> {
        hashMap.put("minecraft:furnace", "Furnace");
        hashMap.put("minecraft:lit_furnace", "Furnace");
        hashMap.put("minecraft:chest", "Chest");
        hashMap.put("minecraft:trapped_chest", "Chest");
        hashMap.put("minecraft:ender_chest", "EnderChest");
        hashMap.put("minecraft:jukebox", "RecordPlayer");
        hashMap.put("minecraft:dispenser", "Trap");
        hashMap.put("minecraft:dropper", "Dropper");
        hashMap.put("minecraft:sign", "Sign");
        hashMap.put("minecraft:mob_spawner", "MobSpawner");
        hashMap.put("minecraft:noteblock", "Music");
        hashMap.put("minecraft:brewing_stand", "Cauldron");
        hashMap.put("minecraft:enhanting_table", "EnchantTable");
        hashMap.put("minecraft:command_block", "CommandBlock");
        hashMap.put("minecraft:beacon", "Beacon");
        hashMap.put("minecraft:skull", "Skull");
        hashMap.put("minecraft:daylight_detector", "DLDetector");
        hashMap.put("minecraft:hopper", "Hopper");
        hashMap.put("minecraft:banner", "Banner");
        hashMap.put("minecraft:flower_pot", "FlowerPot");
        hashMap.put("minecraft:repeating_command_block", "CommandBlock");
        hashMap.put("minecraft:chain_command_block", "CommandBlock");
        hashMap.put("minecraft:standing_sign", "Sign");
        hashMap.put("minecraft:wall_sign", "Sign");
        hashMap.put("minecraft:piston_head", "Piston");
        hashMap.put("minecraft:daylight_detector_inverted", "DLDetector");
        hashMap.put("minecraft:unpowered_comparator", "Comparator");
        hashMap.put("minecraft:powered_comparator", "Comparator");
        hashMap.put("minecraft:wall_banner", "Banner");
        hashMap.put("minecraft:standing_banner", "Banner");
        hashMap.put("minecraft:structure_block", "Structure");
        hashMap.put("minecraft:end_portal", "Airportal");
        hashMap.put("minecraft:end_gateway", "EndGateway");
        hashMap.put("minecraft:shield", "Banner");
    });
    protected static final Hook.HookFunction ADD_NAMES = new Hook.HookFunction(){

        @Override
        public <T> T apply(DynamicOps<T> dynamicOps, T object) {
            return V99.addNames(new Dynamic<T>(dynamicOps, object), ITEM_TO_BLOCKENTITY, "ArmorStand");
        }
    };

    public V99(int i, Schema schema) {
        super(i, schema);
    }

    protected static TypeTemplate equipment(Schema schema) {
        return DSL.optionalFields("Equipment", DSL.list(References.ITEM_STACK.in(schema)));
    }

    protected static void registerMob(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
        schema.register(map, string, () -> V99.equipment(schema));
    }

    protected static void registerThrowableProjectile(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
        schema.register(map, string, () -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(schema)));
    }

    protected static void registerMinecart(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
        schema.register(map, string, () -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema)));
    }

    protected static void registerInventory(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
        schema.register(map, string, () -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(schema))));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        HashMap<String, Supplier<TypeTemplate>> map = Maps.newHashMap();
        schema.register(map, "Item", (String string) -> DSL.optionalFields("Item", References.ITEM_STACK.in(schema)));
        schema.registerSimple(map, "XPOrb");
        V99.registerThrowableProjectile(schema, map, "ThrownEgg");
        schema.registerSimple(map, "LeashKnot");
        schema.registerSimple(map, "Painting");
        schema.register(map, "Arrow", (String string) -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(schema)));
        schema.register(map, "TippedArrow", (String string) -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(schema)));
        schema.register(map, "SpectralArrow", (String string) -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(schema)));
        V99.registerThrowableProjectile(schema, map, "Snowball");
        V99.registerThrowableProjectile(schema, map, "Fireball");
        V99.registerThrowableProjectile(schema, map, "SmallFireball");
        V99.registerThrowableProjectile(schema, map, "ThrownEnderpearl");
        schema.registerSimple(map, "EyeOfEnderSignal");
        schema.register(map, "ThrownPotion", (String string) -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(schema), "Potion", References.ITEM_STACK.in(schema)));
        V99.registerThrowableProjectile(schema, map, "ThrownExpBottle");
        schema.register(map, "ItemFrame", (String string) -> DSL.optionalFields("Item", References.ITEM_STACK.in(schema)));
        V99.registerThrowableProjectile(schema, map, "WitherSkull");
        schema.registerSimple(map, "PrimedTnt");
        schema.register(map, "FallingSand", (String string) -> DSL.optionalFields("Block", References.BLOCK_NAME.in(schema), "TileEntityData", References.BLOCK_ENTITY.in(schema)));
        schema.register(map, "FireworksRocketEntity", (String string) -> DSL.optionalFields("FireworksItem", References.ITEM_STACK.in(schema)));
        schema.registerSimple(map, "Boat");
        schema.register(map, "Minecart", () -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema), "Items", DSL.list(References.ITEM_STACK.in(schema))));
        V99.registerMinecart(schema, map, "MinecartRideable");
        schema.register(map, "MinecartChest", (String string) -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema), "Items", DSL.list(References.ITEM_STACK.in(schema))));
        V99.registerMinecart(schema, map, "MinecartFurnace");
        V99.registerMinecart(schema, map, "MinecartTNT");
        schema.register(map, "MinecartSpawner", () -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema), References.UNTAGGED_SPAWNER.in(schema)));
        schema.register(map, "MinecartHopper", (String string) -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema), "Items", DSL.list(References.ITEM_STACK.in(schema))));
        V99.registerMinecart(schema, map, "MinecartCommandBlock");
        V99.registerMob(schema, map, "ArmorStand");
        V99.registerMob(schema, map, "Creeper");
        V99.registerMob(schema, map, "Skeleton");
        V99.registerMob(schema, map, "Spider");
        V99.registerMob(schema, map, "Giant");
        V99.registerMob(schema, map, "Zombie");
        V99.registerMob(schema, map, "Slime");
        V99.registerMob(schema, map, "Ghast");
        V99.registerMob(schema, map, "PigZombie");
        schema.register(map, "Enderman", (String string) -> DSL.optionalFields("carried", References.BLOCK_NAME.in(schema), V99.equipment(schema)));
        V99.registerMob(schema, map, "CaveSpider");
        V99.registerMob(schema, map, "Silverfish");
        V99.registerMob(schema, map, "Blaze");
        V99.registerMob(schema, map, "LavaSlime");
        V99.registerMob(schema, map, "EnderDragon");
        V99.registerMob(schema, map, "WitherBoss");
        V99.registerMob(schema, map, "Bat");
        V99.registerMob(schema, map, "Witch");
        V99.registerMob(schema, map, "Endermite");
        V99.registerMob(schema, map, "Guardian");
        V99.registerMob(schema, map, "Pig");
        V99.registerMob(schema, map, "Sheep");
        V99.registerMob(schema, map, "Cow");
        V99.registerMob(schema, map, "Chicken");
        V99.registerMob(schema, map, "Squid");
        V99.registerMob(schema, map, "Wolf");
        V99.registerMob(schema, map, "MushroomCow");
        V99.registerMob(schema, map, "SnowMan");
        V99.registerMob(schema, map, "Ozelot");
        V99.registerMob(schema, map, "VillagerGolem");
        schema.register(map, "EntityHorse", (String string) -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(schema)), "ArmorItem", References.ITEM_STACK.in(schema), "SaddleItem", References.ITEM_STACK.in(schema), V99.equipment(schema)));
        V99.registerMob(schema, map, "Rabbit");
        schema.register(map, "Villager", (String string) -> DSL.optionalFields("Inventory", DSL.list(References.ITEM_STACK.in(schema)), "Offers", DSL.optionalFields("Recipes", DSL.list(DSL.optionalFields("buy", References.ITEM_STACK.in(schema), "buyB", References.ITEM_STACK.in(schema), "sell", References.ITEM_STACK.in(schema)))), V99.equipment(schema)));
        schema.registerSimple(map, "EnderCrystal");
        schema.registerSimple(map, "AreaEffectCloud");
        schema.registerSimple(map, "ShulkerBullet");
        V99.registerMob(schema, map, "Shulker");
        return map;
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        HashMap<String, Supplier<TypeTemplate>> map = Maps.newHashMap();
        V99.registerInventory(schema, map, "Furnace");
        V99.registerInventory(schema, map, "Chest");
        schema.registerSimple(map, "EnderChest");
        schema.register(map, "RecordPlayer", (String string) -> DSL.optionalFields("RecordItem", References.ITEM_STACK.in(schema)));
        V99.registerInventory(schema, map, "Trap");
        V99.registerInventory(schema, map, "Dropper");
        schema.registerSimple(map, "Sign");
        schema.register(map, "MobSpawner", (String string) -> References.UNTAGGED_SPAWNER.in(schema));
        schema.registerSimple(map, "Music");
        schema.registerSimple(map, "Piston");
        V99.registerInventory(schema, map, "Cauldron");
        schema.registerSimple(map, "EnchantTable");
        schema.registerSimple(map, "Airportal");
        schema.registerSimple(map, "Control");
        schema.registerSimple(map, "Beacon");
        schema.registerSimple(map, "Skull");
        schema.registerSimple(map, "DLDetector");
        V99.registerInventory(schema, map, "Hopper");
        schema.registerSimple(map, "Comparator");
        schema.register(map, "FlowerPot", (String string) -> DSL.optionalFields("Item", DSL.or(DSL.constType(DSL.intType()), References.ITEM_NAME.in(schema))));
        schema.registerSimple(map, "Banner");
        schema.registerSimple(map, "Structure");
        schema.registerSimple(map, "EndGateway");
        return map;
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        schema.registerType(false, References.LEVEL, DSL::remainder);
        schema.registerType(false, References.PLAYER, () -> DSL.optionalFields("Inventory", DSL.list(References.ITEM_STACK.in(schema)), "EnderItems", DSL.list(References.ITEM_STACK.in(schema))));
        schema.registerType(false, References.CHUNK, () -> DSL.fields("Level", DSL.optionalFields("Entities", DSL.list(References.ENTITY_TREE.in(schema)), "TileEntities", DSL.list(References.BLOCK_ENTITY.in(schema)), "TileTicks", DSL.list(DSL.fields("i", References.BLOCK_NAME.in(schema))))));
        schema.registerType(true, References.BLOCK_ENTITY, () -> DSL.taggedChoiceLazy("id", DSL.string(), map2));
        schema.registerType(true, References.ENTITY_TREE, () -> DSL.optionalFields("Riding", References.ENTITY_TREE.in(schema), References.ENTITY.in(schema)));
        schema.registerType(false, References.ENTITY_NAME, () -> DSL.constType(DSL.namespacedString()));
        schema.registerType(true, References.ENTITY, () -> DSL.taggedChoiceLazy("id", DSL.string(), map));
        schema.registerType(true, References.ITEM_STACK, () -> DSL.hook(DSL.optionalFields("id", DSL.or(DSL.constType(DSL.intType()), References.ITEM_NAME.in(schema)), "tag", DSL.optionalFields("EntityTag", References.ENTITY_TREE.in(schema), "BlockEntityTag", References.BLOCK_ENTITY.in(schema), "CanDestroy", DSL.list(References.BLOCK_NAME.in(schema)), "CanPlaceOn", DSL.list(References.BLOCK_NAME.in(schema)))), ADD_NAMES, Hook.HookFunction.IDENTITY));
        schema.registerType(false, References.OPTIONS, DSL::remainder);
        schema.registerType(false, References.BLOCK_NAME, () -> DSL.or(DSL.constType(DSL.intType()), DSL.constType(DSL.namespacedString())));
        schema.registerType(false, References.ITEM_NAME, () -> DSL.constType(DSL.namespacedString()));
        schema.registerType(false, References.STATS, DSL::remainder);
        schema.registerType(false, References.SAVED_DATA, () -> DSL.optionalFields("data", DSL.optionalFields("Features", DSL.compoundList(References.STRUCTURE_FEATURE.in(schema)), "Objectives", DSL.list(References.OBJECTIVE.in(schema)), "Teams", DSL.list(References.TEAM.in(schema)))));
        schema.registerType(false, References.STRUCTURE_FEATURE, DSL::remainder);
        schema.registerType(false, References.OBJECTIVE, DSL::remainder);
        schema.registerType(false, References.TEAM, DSL::remainder);
        schema.registerType(true, References.UNTAGGED_SPAWNER, DSL::remainder);
        schema.registerType(false, References.POI_CHUNK, DSL::remainder);
    }

    protected static <T> T addNames(Dynamic<T> dynamic, Map<String, String> map, String string) {
        return dynamic.update("tag", dynamic22 -> dynamic22.update("BlockEntityTag", dynamic2 -> {
            String string = dynamic.get("id").asString("");
            String string2 = (String)map.get(NamespacedSchema.ensureNamespaced(string));
            if (string2 != null) {
                return dynamic2.set("id", dynamic.createString(string2));
            }
            LOGGER.warn("Unable to resolve BlockEntity for ItemStack: {}", (Object)string);
            return dynamic2;
        }).update("EntityTag", dynamic2 -> {
            String string2 = dynamic.get("id").asString("");
            if (Objects.equals(NamespacedSchema.ensureNamespaced(string2), "minecraft:armor_stand")) {
                return dynamic2.set("id", dynamic.createString(string));
            }
            return dynamic2;
        })).getValue();
    }
}


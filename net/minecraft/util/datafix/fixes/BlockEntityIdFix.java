/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import java.util.Map;
import net.minecraft.util.datafix.fixes.References;

public class BlockEntityIdFix
extends DataFix {
    private static final Map<String, String> ID_MAP = DataFixUtils.make(Maps.newHashMap(), hashMap -> {
        hashMap.put("Airportal", "minecraft:end_portal");
        hashMap.put("Banner", "minecraft:banner");
        hashMap.put("Beacon", "minecraft:beacon");
        hashMap.put("Cauldron", "minecraft:brewing_stand");
        hashMap.put("Chest", "minecraft:chest");
        hashMap.put("Comparator", "minecraft:comparator");
        hashMap.put("Control", "minecraft:command_block");
        hashMap.put("DLDetector", "minecraft:daylight_detector");
        hashMap.put("Dropper", "minecraft:dropper");
        hashMap.put("EnchantTable", "minecraft:enchanting_table");
        hashMap.put("EndGateway", "minecraft:end_gateway");
        hashMap.put("EnderChest", "minecraft:ender_chest");
        hashMap.put("FlowerPot", "minecraft:flower_pot");
        hashMap.put("Furnace", "minecraft:furnace");
        hashMap.put("Hopper", "minecraft:hopper");
        hashMap.put("MobSpawner", "minecraft:mob_spawner");
        hashMap.put("Music", "minecraft:noteblock");
        hashMap.put("Piston", "minecraft:piston");
        hashMap.put("RecordPlayer", "minecraft:jukebox");
        hashMap.put("Sign", "minecraft:sign");
        hashMap.put("Skull", "minecraft:skull");
        hashMap.put("Structure", "minecraft:structure_block");
        hashMap.put("Trap", "minecraft:dispenser");
    });

    public BlockEntityIdFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
        Type<?> type2 = this.getOutputSchema().getType(References.ITEM_STACK);
        TaggedChoice.TaggedChoiceType<?> taggedChoiceType = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
        TaggedChoice.TaggedChoiceType<?> taggedChoiceType2 = this.getOutputSchema().findChoiceType(References.BLOCK_ENTITY);
        return TypeRewriteRule.seq(this.convertUnchecked("item stack block entity name hook converter", type, type2), this.fixTypeEverywhere("BlockEntityIdFix", taggedChoiceType, taggedChoiceType2, dynamicOps -> pair -> pair.mapFirst(string -> ID_MAP.getOrDefault(string, (String)string))));
    }
}


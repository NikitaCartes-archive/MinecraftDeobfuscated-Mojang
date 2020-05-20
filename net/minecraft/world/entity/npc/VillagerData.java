/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.npc;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;

public class VillagerData {
    private static final int[] NEXT_LEVEL_XP_THRESHOLDS = new int[]{0, 10, 70, 150, 250};
    public static final Codec<VillagerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Registry.VILLAGER_TYPE.fieldOf("type")).forGetter(villagerData -> villagerData.type), ((MapCodec)Registry.VILLAGER_PROFESSION.fieldOf("profession")).forGetter(villagerData -> villagerData.profession), ((MapCodec)Codec.INT.fieldOf("level")).withDefault(1).forGetter(villagerData -> villagerData.level)).apply((Applicative<VillagerData, ?>)instance, VillagerData::new));
    private final VillagerType type;
    private final VillagerProfession profession;
    private final int level;

    public VillagerData(VillagerType villagerType, VillagerProfession villagerProfession, int i) {
        this.type = villagerType;
        this.profession = villagerProfession;
        this.level = Math.max(1, i);
    }

    public VillagerType getType() {
        return this.type;
    }

    public VillagerProfession getProfession() {
        return this.profession;
    }

    public int getLevel() {
        return this.level;
    }

    public VillagerData setType(VillagerType villagerType) {
        return new VillagerData(villagerType, this.profession, this.level);
    }

    public VillagerData setProfession(VillagerProfession villagerProfession) {
        return new VillagerData(this.type, villagerProfession, this.level);
    }

    public VillagerData setLevel(int i) {
        return new VillagerData(this.type, this.profession, i);
    }

    @Environment(value=EnvType.CLIENT)
    public static int getMinXpPerLevel(int i) {
        return VillagerData.canLevelUp(i) ? NEXT_LEVEL_XP_THRESHOLDS[i - 1] : 0;
    }

    public static int getMaxXpPerLevel(int i) {
        return VillagerData.canLevelUp(i) ? NEXT_LEVEL_XP_THRESHOLDS[i] : 0;
    }

    public static boolean canLevelUp(int i) {
        return i >= 1 && i < 5;
    }
}


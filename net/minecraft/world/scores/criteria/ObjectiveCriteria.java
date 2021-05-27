/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.scores.criteria;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatType;

public class ObjectiveCriteria {
    private static final Map<String, ObjectiveCriteria> CUSTOM_CRITERIA = Maps.newHashMap();
    private static final Map<String, ObjectiveCriteria> CRITERIA_CACHE = Maps.newHashMap();
    public static final ObjectiveCriteria DUMMY = ObjectiveCriteria.registerCustom("dummy");
    public static final ObjectiveCriteria TRIGGER = ObjectiveCriteria.registerCustom("trigger");
    public static final ObjectiveCriteria DEATH_COUNT = ObjectiveCriteria.registerCustom("deathCount");
    public static final ObjectiveCriteria KILL_COUNT_PLAYERS = ObjectiveCriteria.registerCustom("playerKillCount");
    public static final ObjectiveCriteria KILL_COUNT_ALL = ObjectiveCriteria.registerCustom("totalKillCount");
    public static final ObjectiveCriteria HEALTH = ObjectiveCriteria.registerCustom("health", true, RenderType.HEARTS);
    public static final ObjectiveCriteria FOOD = ObjectiveCriteria.registerCustom("food", true, RenderType.INTEGER);
    public static final ObjectiveCriteria AIR = ObjectiveCriteria.registerCustom("air", true, RenderType.INTEGER);
    public static final ObjectiveCriteria ARMOR = ObjectiveCriteria.registerCustom("armor", true, RenderType.INTEGER);
    public static final ObjectiveCriteria EXPERIENCE = ObjectiveCriteria.registerCustom("xp", true, RenderType.INTEGER);
    public static final ObjectiveCriteria LEVEL = ObjectiveCriteria.registerCustom("level", true, RenderType.INTEGER);
    public static final ObjectiveCriteria[] TEAM_KILL = new ObjectiveCriteria[]{ObjectiveCriteria.registerCustom("teamkill." + ChatFormatting.BLACK.getName()), ObjectiveCriteria.registerCustom("teamkill." + ChatFormatting.DARK_BLUE.getName()), ObjectiveCriteria.registerCustom("teamkill." + ChatFormatting.DARK_GREEN.getName()), ObjectiveCriteria.registerCustom("teamkill." + ChatFormatting.DARK_AQUA.getName()), ObjectiveCriteria.registerCustom("teamkill." + ChatFormatting.DARK_RED.getName()), ObjectiveCriteria.registerCustom("teamkill." + ChatFormatting.DARK_PURPLE.getName()), ObjectiveCriteria.registerCustom("teamkill." + ChatFormatting.GOLD.getName()), ObjectiveCriteria.registerCustom("teamkill." + ChatFormatting.GRAY.getName()), ObjectiveCriteria.registerCustom("teamkill." + ChatFormatting.DARK_GRAY.getName()), ObjectiveCriteria.registerCustom("teamkill." + ChatFormatting.BLUE.getName()), ObjectiveCriteria.registerCustom("teamkill." + ChatFormatting.GREEN.getName()), ObjectiveCriteria.registerCustom("teamkill." + ChatFormatting.AQUA.getName()), ObjectiveCriteria.registerCustom("teamkill." + ChatFormatting.RED.getName()), ObjectiveCriteria.registerCustom("teamkill." + ChatFormatting.LIGHT_PURPLE.getName()), ObjectiveCriteria.registerCustom("teamkill." + ChatFormatting.YELLOW.getName()), ObjectiveCriteria.registerCustom("teamkill." + ChatFormatting.WHITE.getName())};
    public static final ObjectiveCriteria[] KILLED_BY_TEAM = new ObjectiveCriteria[]{ObjectiveCriteria.registerCustom("killedByTeam." + ChatFormatting.BLACK.getName()), ObjectiveCriteria.registerCustom("killedByTeam." + ChatFormatting.DARK_BLUE.getName()), ObjectiveCriteria.registerCustom("killedByTeam." + ChatFormatting.DARK_GREEN.getName()), ObjectiveCriteria.registerCustom("killedByTeam." + ChatFormatting.DARK_AQUA.getName()), ObjectiveCriteria.registerCustom("killedByTeam." + ChatFormatting.DARK_RED.getName()), ObjectiveCriteria.registerCustom("killedByTeam." + ChatFormatting.DARK_PURPLE.getName()), ObjectiveCriteria.registerCustom("killedByTeam." + ChatFormatting.GOLD.getName()), ObjectiveCriteria.registerCustom("killedByTeam." + ChatFormatting.GRAY.getName()), ObjectiveCriteria.registerCustom("killedByTeam." + ChatFormatting.DARK_GRAY.getName()), ObjectiveCriteria.registerCustom("killedByTeam." + ChatFormatting.BLUE.getName()), ObjectiveCriteria.registerCustom("killedByTeam." + ChatFormatting.GREEN.getName()), ObjectiveCriteria.registerCustom("killedByTeam." + ChatFormatting.AQUA.getName()), ObjectiveCriteria.registerCustom("killedByTeam." + ChatFormatting.RED.getName()), ObjectiveCriteria.registerCustom("killedByTeam." + ChatFormatting.LIGHT_PURPLE.getName()), ObjectiveCriteria.registerCustom("killedByTeam." + ChatFormatting.YELLOW.getName()), ObjectiveCriteria.registerCustom("killedByTeam." + ChatFormatting.WHITE.getName())};
    private final String name;
    private final boolean readOnly;
    private final RenderType renderType;

    private static ObjectiveCriteria registerCustom(String string, boolean bl, RenderType renderType) {
        ObjectiveCriteria objectiveCriteria = new ObjectiveCriteria(string, bl, renderType);
        CUSTOM_CRITERIA.put(string, objectiveCriteria);
        return objectiveCriteria;
    }

    private static ObjectiveCriteria registerCustom(String string) {
        return ObjectiveCriteria.registerCustom(string, false, RenderType.INTEGER);
    }

    protected ObjectiveCriteria(String string) {
        this(string, false, RenderType.INTEGER);
    }

    protected ObjectiveCriteria(String string, boolean bl, RenderType renderType) {
        this.name = string;
        this.readOnly = bl;
        this.renderType = renderType;
        CRITERIA_CACHE.put(string, this);
    }

    public static Set<String> getCustomCriteriaNames() {
        return ImmutableSet.copyOf(CUSTOM_CRITERIA.keySet());
    }

    public static Optional<ObjectiveCriteria> byName(String string) {
        ObjectiveCriteria objectiveCriteria = CRITERIA_CACHE.get(string);
        if (objectiveCriteria != null) {
            return Optional.of(objectiveCriteria);
        }
        int i = string.indexOf(58);
        if (i < 0) {
            return Optional.empty();
        }
        return Registry.STAT_TYPE.getOptional(ResourceLocation.of(string.substring(0, i), '.')).flatMap(statType -> ObjectiveCriteria.getStat(statType, ResourceLocation.of(string.substring(i + 1), '.')));
    }

    private static <T> Optional<ObjectiveCriteria> getStat(StatType<T> statType, ResourceLocation resourceLocation) {
        return statType.getRegistry().getOptional(resourceLocation).map(statType::get);
    }

    public String getName() {
        return this.name;
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public RenderType getDefaultRenderType() {
        return this.renderType;
    }

    public static enum RenderType {
        INTEGER("integer"),
        HEARTS("hearts");

        private final String id;
        private static final Map<String, RenderType> BY_ID;

        private RenderType(String string2) {
            this.id = string2;
        }

        public String getId() {
            return this.id;
        }

        public static RenderType byId(String string) {
            return BY_ID.getOrDefault(string, INTEGER);
        }

        static {
            ImmutableMap.Builder<String, RenderType> builder = ImmutableMap.builder();
            for (RenderType renderType : RenderType.values()) {
                builder.put(renderType.id, renderType);
            }
            BY_ID = builder.build();
        }
    }
}


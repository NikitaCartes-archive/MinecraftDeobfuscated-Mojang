/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.scores;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

public abstract class Team {
    public boolean isAlliedTo(@Nullable Team team) {
        if (team == null) {
            return false;
        }
        return this == team;
    }

    public abstract String getName();

    public abstract MutableComponent getFormattedName(Component var1);

    public abstract boolean canSeeFriendlyInvisibles();

    public abstract boolean isAllowFriendlyFire();

    public abstract Visibility getNameTagVisibility();

    public abstract ChatFormatting getColor();

    public abstract Collection<String> getPlayers();

    public abstract Visibility getDeathMessageVisibility();

    public abstract CollisionRule getCollisionRule();

    public static enum CollisionRule {
        ALWAYS("always", 0),
        NEVER("never", 1),
        PUSH_OTHER_TEAMS("pushOtherTeams", 2),
        PUSH_OWN_TEAM("pushOwnTeam", 3);

        private static final Map<String, CollisionRule> BY_NAME;
        public final String name;
        public final int id;

        @Nullable
        public static CollisionRule byName(String string) {
            return BY_NAME.get(string);
        }

        private CollisionRule(String string2, int j) {
            this.name = string2;
            this.id = j;
        }

        public Component getDisplayName() {
            return Component.translatable("team.collision." + this.name);
        }

        static {
            BY_NAME = Arrays.stream(CollisionRule.values()).collect(Collectors.toMap(collisionRule -> collisionRule.name, collisionRule -> collisionRule));
        }
    }

    public static enum Visibility {
        ALWAYS("always", 0),
        NEVER("never", 1),
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams", 2),
        HIDE_FOR_OWN_TEAM("hideForOwnTeam", 3);

        private static final Map<String, Visibility> BY_NAME;
        public final String name;
        public final int id;

        public static String[] getAllNames() {
            return BY_NAME.keySet().toArray(new String[0]);
        }

        @Nullable
        public static Visibility byName(String string) {
            return BY_NAME.get(string);
        }

        private Visibility(String string2, int j) {
            this.name = string2;
            this.id = j;
        }

        public Component getDisplayName() {
            return Component.translatable("team.visibility." + this.name);
        }

        static {
            BY_NAME = Arrays.stream(Visibility.values()).collect(Collectors.toMap(visibility -> visibility.name, visibility -> visibility));
        }
    }
}


/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Abilities;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public enum GameType implements StringRepresentable
{
    SURVIVAL(0, "survival"),
    CREATIVE(1, "creative"),
    ADVENTURE(2, "adventure"),
    SPECTATOR(3, "spectator");

    public static final GameType DEFAULT_MODE;
    public static final StringRepresentable.EnumCodec<GameType> CODEC;
    private static final IntFunction<GameType> BY_ID;
    private static final int NOT_SET = -1;
    private final int id;
    private final String name;
    private final Component shortName;
    private final Component longName;

    private GameType(int j, String string2) {
        this.id = j;
        this.name = string2;
        this.shortName = Component.translatable("selectWorld.gameMode." + string2);
        this.longName = Component.translatable("gameMode." + string2);
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public Component getLongDisplayName() {
        return this.longName;
    }

    public Component getShortDisplayName() {
        return this.shortName;
    }

    public void updatePlayerAbilities(Abilities abilities) {
        if (this == CREATIVE) {
            abilities.mayfly = true;
            abilities.instabuild = true;
            abilities.invulnerable = true;
        } else if (this == SPECTATOR) {
            abilities.mayfly = true;
            abilities.instabuild = false;
            abilities.invulnerable = true;
            abilities.flying = true;
        } else {
            abilities.mayfly = false;
            abilities.instabuild = false;
            abilities.invulnerable = false;
            abilities.flying = false;
        }
        abilities.mayBuild = !this.isBlockPlacingRestricted();
    }

    public boolean isBlockPlacingRestricted() {
        return this == ADVENTURE || this == SPECTATOR;
    }

    public boolean isCreative() {
        return this == CREATIVE;
    }

    public boolean isSurvival() {
        return this == SURVIVAL || this == ADVENTURE;
    }

    public static GameType byId(int i) {
        return BY_ID.apply(i);
    }

    public static GameType byName(String string) {
        return GameType.byName(string, SURVIVAL);
    }

    @Nullable
    @Contract(value="_,!null->!null;_,null->_")
    public static GameType byName(String string, @Nullable GameType gameType) {
        GameType gameType2 = CODEC.byName(string);
        return gameType2 != null ? gameType2 : gameType;
    }

    public static int getNullableId(@Nullable GameType gameType) {
        return gameType != null ? gameType.id : -1;
    }

    @Nullable
    public static GameType byNullableId(int i) {
        if (i == -1) {
            return null;
        }
        return GameType.byId(i);
    }

    static {
        DEFAULT_MODE = SURVIVAL;
        CODEC = StringRepresentable.fromEnum(GameType::values);
        BY_ID = ByIdMap.continuous(GameType::getId, GameType.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    }
}


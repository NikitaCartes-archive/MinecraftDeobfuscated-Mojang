package net.minecraft.world.level;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Abilities;

public enum GameType {
	NOT_SET(-1, ""),
	SURVIVAL(0, "survival"),
	CREATIVE(1, "creative"),
	ADVENTURE(2, "adventure"),
	SPECTATOR(3, "spectator");

	private final int id;
	private final String name;

	private GameType(int j, String string2) {
		this.id = j;
		this.name = string2;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public Component getDisplayName() {
		return new TranslatableComponent("gameMode." + this.name);
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

	@Environment(EnvType.CLIENT)
	public float getInteractionRange() {
		return 2.5F;
	}

	@Environment(EnvType.CLIENT)
	public float getMaxInteractionRange() {
		return 6.0F;
	}

	@Environment(EnvType.CLIENT)
	public float getBlockPickRange() {
		return this.isCreative() ? 5.0F : 4.5F;
	}

	public static GameType byId(int i) {
		return byId(i, SURVIVAL);
	}

	public static GameType byId(int i, GameType gameType) {
		for (GameType gameType2 : values()) {
			if (gameType2.id == i) {
				return gameType2;
			}
		}

		return gameType;
	}

	public static GameType byName(String string) {
		return byName(string, SURVIVAL);
	}

	public static GameType byName(String string, GameType gameType) {
		for (GameType gameType2 : values()) {
			if (gameType2.name.equals(string)) {
				return gameType2;
			}
		}

		return gameType;
	}
}

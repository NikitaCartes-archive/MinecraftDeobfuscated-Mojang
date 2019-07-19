package net.minecraft.client.renderer.chunk;

import java.util.BitSet;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;

@Environment(EnvType.CLIENT)
public class VisibilitySet {
	private static final int FACINGS = Direction.values().length;
	private final BitSet data = new BitSet(FACINGS * FACINGS);

	public void add(Set<Direction> set) {
		for (Direction direction : set) {
			for (Direction direction2 : set) {
				this.set(direction, direction2, true);
			}
		}
	}

	public void set(Direction direction, Direction direction2, boolean bl) {
		this.data.set(direction.ordinal() + direction2.ordinal() * FACINGS, bl);
		this.data.set(direction2.ordinal() + direction.ordinal() * FACINGS, bl);
	}

	public void setAll(boolean bl) {
		this.data.set(0, this.data.size(), bl);
	}

	public boolean visibilityBetween(Direction direction, Direction direction2) {
		return this.data.get(direction.ordinal() + direction2.ordinal() * FACINGS);
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(' ');

		for (Direction direction : Direction.values()) {
			stringBuilder.append(' ').append(direction.toString().toUpperCase().charAt(0));
		}

		stringBuilder.append('\n');

		for (Direction direction : Direction.values()) {
			stringBuilder.append(direction.toString().toUpperCase().charAt(0));

			for (Direction direction2 : Direction.values()) {
				if (direction == direction2) {
					stringBuilder.append("  ");
				} else {
					boolean bl = this.visibilityBetween(direction, direction2);
					stringBuilder.append(' ').append((char)(bl ? 'Y' : 'n'));
				}
			}

			stringBuilder.append('\n');
		}

		return stringBuilder.toString();
	}
}

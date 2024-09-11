package net.minecraft.world.level.block.state.properties;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.Direction;

public class DirectionProperty extends EnumProperty<Direction> {
	protected DirectionProperty(String string, List<Direction> list) {
		super(string, Direction.class, list);
	}

	public static DirectionProperty create(String string) {
		return create(string, (Predicate<Direction>)(direction -> true));
	}

	public static DirectionProperty create(String string, Predicate<Direction> predicate) {
		return create(string, (List<Direction>)Arrays.stream(Direction.values()).filter(predicate).collect(Collectors.toList()));
	}

	public static DirectionProperty create(String string, Direction... directions) {
		return create(string, Lists.<Direction>newArrayList(directions));
	}

	public static DirectionProperty create(String string, List<Direction> list) {
		return new DirectionProperty(string, list);
	}
}

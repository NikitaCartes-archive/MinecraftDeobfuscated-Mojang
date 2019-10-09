package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.Registry;

public class TreeDecoratorType<P extends TreeDecorator> {
	public static final TreeDecoratorType<TrunkVineDecorator> TRUNK_VINE = register("trunk_vine", TrunkVineDecorator::new);
	public static final TreeDecoratorType<LeaveVineDecorator> LEAVE_VINE = register("leave_vine", LeaveVineDecorator::new);
	public static final TreeDecoratorType<CocoaDecorator> COCOA = register("cocoa", CocoaDecorator::new);
	public static final TreeDecoratorType<BeehiveDecorator> BEEHIVE = register("beehive", BeehiveDecorator::new);
	public static final TreeDecoratorType<AlterGroundDecorator> ALTER_GROUND = register("alter_ground", AlterGroundDecorator::new);
	private final Function<Dynamic<?>, P> deserializer;

	private static <P extends TreeDecorator> TreeDecoratorType<P> register(String string, Function<Dynamic<?>, P> function) {
		return Registry.register(Registry.TREE_DECORATOR_TYPES, string, new TreeDecoratorType<>(function));
	}

	private TreeDecoratorType(Function<Dynamic<?>, P> function) {
		this.deserializer = function;
	}

	public P deserialize(Dynamic<?> dynamic) {
		return (P)this.deserializer.apply(dynamic);
	}
}

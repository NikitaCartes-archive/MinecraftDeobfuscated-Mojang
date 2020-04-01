package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.Registry;

public class TreeDecoratorType<P extends TreeDecorator> {
	public static final TreeDecoratorType<TrunkVineDecorator> TRUNK_VINE = register("trunk_vine", TrunkVineDecorator::new, TrunkVineDecorator::random);
	public static final TreeDecoratorType<LeaveVineDecorator> LEAVE_VINE = register("leave_vine", LeaveVineDecorator::new, LeaveVineDecorator::random);
	public static final TreeDecoratorType<CocoaDecorator> COCOA = register("cocoa", CocoaDecorator::new, CocoaDecorator::random);
	public static final TreeDecoratorType<BeehiveDecorator> BEEHIVE = register("beehive", BeehiveDecorator::new, BeehiveDecorator::random);
	public static final TreeDecoratorType<AlterGroundDecorator> ALTER_GROUND = register("alter_ground", AlterGroundDecorator::new, AlterGroundDecorator::random);
	private final Function<Dynamic<?>, P> deserializer;
	private final Function<Random, P> randomProvider;

	private static <P extends TreeDecorator> TreeDecoratorType<P> register(String string, Function<Dynamic<?>, P> function, Function<Random, P> function2) {
		return Registry.register(Registry.TREE_DECORATOR_TYPES, string, new TreeDecoratorType<>(function, function2));
	}

	public TreeDecoratorType(Function<Dynamic<?>, P> function, Function<Random, P> function2) {
		this.deserializer = function;
		this.randomProvider = function2;
	}

	public P deserialize(Dynamic<?> dynamic) {
		return (P)this.deserializer.apply(dynamic);
	}

	public P createRandom(Random random) {
		return (P)this.randomProvider.apply(random);
	}
}

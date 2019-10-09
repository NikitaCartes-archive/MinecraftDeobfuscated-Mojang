/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.treedecorators.AlterGroundDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.BeehiveDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.CocoaDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TrunkVineDecorator;

public class TreeDecoratorType<P extends TreeDecorator> {
    public static final TreeDecoratorType<TrunkVineDecorator> TRUNK_VINE = TreeDecoratorType.register("trunk_vine", TrunkVineDecorator::new);
    public static final TreeDecoratorType<LeaveVineDecorator> LEAVE_VINE = TreeDecoratorType.register("leave_vine", LeaveVineDecorator::new);
    public static final TreeDecoratorType<CocoaDecorator> COCOA = TreeDecoratorType.register("cocoa", CocoaDecorator::new);
    public static final TreeDecoratorType<BeehiveDecorator> BEEHIVE = TreeDecoratorType.register("beehive", BeehiveDecorator::new);
    public static final TreeDecoratorType<AlterGroundDecorator> ALTER_GROUND = TreeDecoratorType.register("alter_ground", AlterGroundDecorator::new);
    private final Function<Dynamic<?>, P> deserializer;

    private static <P extends TreeDecorator> TreeDecoratorType<P> register(String string, Function<Dynamic<?>, P> function) {
        return Registry.register(Registry.TREE_DECORATOR_TYPES, string, new TreeDecoratorType<P>(function));
    }

    private TreeDecoratorType(Function<Dynamic<?>, P> function) {
        this.deserializer = function;
    }

    public P deserialize(Dynamic<?> dynamic) {
        return (P)((TreeDecorator)this.deserializer.apply(dynamic));
    }
}


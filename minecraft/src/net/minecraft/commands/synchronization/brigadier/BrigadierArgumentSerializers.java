package net.minecraft.commands.synchronization.brigadier;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;

public class BrigadierArgumentSerializers {
	private static final byte NUMBER_FLAG_MIN = 1;
	private static final byte NUMBER_FLAG_MAX = 2;

	public static void bootstrap() {
		ArgumentTypes.register("brigadier:bool", BoolArgumentType.class, new EmptyArgumentSerializer(BoolArgumentType::bool));
		ArgumentTypes.register("brigadier:float", FloatArgumentType.class, new FloatArgumentSerializer());
		ArgumentTypes.register("brigadier:double", DoubleArgumentType.class, new DoubleArgumentSerializer());
		ArgumentTypes.register("brigadier:integer", IntegerArgumentType.class, new IntegerArgumentSerializer());
		ArgumentTypes.register("brigadier:long", LongArgumentType.class, new LongArgumentSerializer());
		ArgumentTypes.register("brigadier:string", StringArgumentType.class, new StringArgumentSerializer());
	}

	public static byte createNumberFlags(boolean bl, boolean bl2) {
		byte b = 0;
		if (bl) {
			b = (byte)(b | 1);
		}

		if (bl2) {
			b = (byte)(b | 2);
		}

		return b;
	}

	public static boolean numberHasMin(byte b) {
		return (b & 1) != 0;
	}

	public static boolean numberHasMax(byte b) {
		return (b & 2) != 0;
	}
}

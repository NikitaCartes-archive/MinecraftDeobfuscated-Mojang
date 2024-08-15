package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.animal.horse.Markings;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class HorseRenderState extends EquineRenderState {
	public Variant variant = Variant.WHITE;
	public Markings markings = Markings.NONE;
	public ItemStack bodyArmorItem = ItemStack.EMPTY;
}

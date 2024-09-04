package net.minecraft.client.renderer.entity.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class LlamaRenderState extends LivingEntityRenderState {
	public Llama.Variant variant = Llama.Variant.CREAMY;
	public boolean hasChest;
	public ItemStack bodyItem = ItemStack.EMPTY;
	public boolean isTraderLlama;
}

package net.avizvul.esquissemod.item.custom;

import net.avizvul.esquissemod.EsquisseMod;
import net.avizvul.esquissemod.component.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class StampPadItem extends ColorPencilItem {

    public StampPadItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        List<Integer> colors = ModDataComponents.STORED_COLORS.getOrDefault(stack, new ArrayList<>());
        if (colors.isEmpty()) {
            return Component.translatable("item.esquissemod.empty_stamp_pad");
        }
        return Component.translatable(this.getDescriptionId(stack));
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return new net.minecraft.resources.ResourceLocation(EsquisseMod.MOD_ID, "textures/gui/button_stamp_base.png");
    }
}
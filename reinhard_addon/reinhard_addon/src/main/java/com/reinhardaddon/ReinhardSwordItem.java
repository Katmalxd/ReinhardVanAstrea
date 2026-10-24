package com.reinhardaddon;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

/**
 * La espada que se equipa a Reinhard. El modelo 3D (OBJ) y las texturas se portan desde la
 * "Dragon Sword [Reid]" de Tensura Blade con tools/extract_assets.py.
 * El item en sí es una espada vanilla (Netherite, daño base 4 + 8): no depende de Tensura.
 */
public class ReinhardSwordItem extends SwordItem {
    public ReinhardSwordItem(Properties props) {
        super(Tiers.NETHERITE, 8, -2.0F, props.stacksTo(1).fireResistant().rarity(Rarity.EPIC));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false; // el modelo ya trae su capa emisiva; evitamos el brillo de encantamiento encima
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.reinhard_addon.reinhard_sword.1").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.reinhard_addon.reinhard_sword.2").withStyle(ChatFormatting.GRAY));
    }
}

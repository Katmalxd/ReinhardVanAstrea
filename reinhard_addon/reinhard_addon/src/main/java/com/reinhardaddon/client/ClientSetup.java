package com.reinhardaddon.client;

import com.reinhardaddon.ModEntities;
import com.reinhardaddon.ModItems;
import com.reinhardaddon.ReinhardAbility;
import com.reinhardaddon.ReinhardAddon;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ReinhardAddon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.REINHARD_SUNSTRIKE.get(), WhiteSunstrikeRenderer::new);
    }

    /**
     * Predicado reinhard_addon:drawn (equivalente al tensura_blade:drawn del mod original):
     * 1 si el NBT del ítem trae Drawn:1b -> el JSON de modelo cambia al modelo desenvainado.
     */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                ModItems.REINHARD_SWORD.get(),
                new ResourceLocation(ReinhardAddon.MODID, "drawn"),
                (stack, level, entity, seed) -> ReinhardAbility.isDrawn(stack) ? 1.0F : 0.0F));
    }
}

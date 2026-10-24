package com.reinhardaddon;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ReinhardAddon.MODID)
public class ReinhardAddon {
    public static final String MODID = "reinhard_addon";

    public ReinhardAddon() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        modBus.addListener(ReinhardAddon::addToCreativeTab);

        // Los handlers con @Mod.EventBusSubscriber se registran solos; el comando se añade aquí.
        MinecraftForge.EVENT_BUS.addListener(ReinhardCommands::register);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.REINHARD_SWORD);
            event.accept(ModItems.REINHARD_SHEATH);
        }
    }
}

package com.reinhardaddon;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ReinhardAddon.MODID);

    /** Espada de Reinhard. Modelo enfundado por defecto; con NBT {Drawn:1b} usa el modelo desenvainado. */
    public static final RegistryObject<Item> REINHARD_SWORD =
            ITEMS.register("reinhard_sword", () -> new ReinhardSwordItem(new Item.Properties()));

    /** Vaina decorativa: se pone en la otra mano mientras la espada está desenvainada (como en Tensura Blade). */
    public static final RegistryObject<Item> REINHARD_SHEATH =
            ITEMS.register("reinhard_sheath", () -> new Item(new Item.Properties().stacksTo(1)));

    private ModItems() {}
}

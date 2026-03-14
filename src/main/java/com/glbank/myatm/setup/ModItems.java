package com.glbank.myatm.setup;

import com.glbank.myatm.MyATMMod;
import com.glbank.myatm.item.CardItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MyATMMod.MODID);

    public static final RegistryObject<Item> CARD = ITEMS.register("card",
            () -> new CardItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> ATM_ITEM = ITEMS.register("atm",
            () -> new BlockItem(ModBlocks.ATM.get(), new Item.Properties()));

    public static final RegistryObject<Item> TERMINAL_ITEM = ITEMS.register("terminal",
            () -> new BlockItem(ModBlocks.TERMINAL.get(), new Item.Properties()));
}

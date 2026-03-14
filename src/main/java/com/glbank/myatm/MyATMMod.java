package com.glbank.myatm;

import com.glbank.myatm.network.PacketHandler;
import com.glbank.myatm.setup.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MyATMMod.MODID)
public class MyATMMod {

    public static final String MODID = "myatm";

    public MyATMMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModBlockEntities.BLOCK_ENTITIES.register(bus);
        ModMenuTypes.MENU_TYPES.register(bus);
        ModCreativeTabs.CREATIVE_TABS.register(bus);

        bus.addListener(ClientSetup::onClientSetup);

        PacketHandler.register();
    }
}

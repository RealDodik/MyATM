package com.glbank.myatm.setup;

import com.glbank.myatm.screen.ATMScreen;
import com.glbank.myatm.screen.TerminalScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.ATM_MENU.get(), ATMScreen::new);
            MenuScreens.register(ModMenuTypes.TERMINAL_MENU.get(), TerminalScreen::new);
        });
    }
}

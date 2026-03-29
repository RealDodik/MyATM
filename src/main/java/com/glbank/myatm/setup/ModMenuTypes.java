package com.glbank.myatm.setup;

import com.glbank.myatm.MyATMMod;
import com.glbank.myatm.menu.CardLinkerMenu;
import com.glbank.myatm.menu.TerminalMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, MyATMMod.MODID);

    public static final RegistryObject<MenuType<CardLinkerMenu>> CARD_LINKER_MENU =
            MENU_TYPES.register("card_linker_menu",
                    () -> IForgeMenuType.create(CardLinkerMenu::new));

    public static final RegistryObject<MenuType<TerminalMenu>> TERMINAL_MENU =
            MENU_TYPES.register("terminal_menu",
                    () -> IForgeMenuType.create(TerminalMenu::new));
}

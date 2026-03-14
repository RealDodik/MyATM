package com.glbank.myatm.setup;

import com.glbank.myatm.MyATMMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MyATMMod.MODID);

    public static final RegistryObject<CreativeModeTab> MYATM_TAB =
            CREATIVE_TABS.register("myatm_tab", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.myatm"))
                            .icon(() -> new ItemStack(ModItems.ATM_ITEM.get()))
                            .displayItems((params, output) -> {
                                output.accept(ModItems.ATM_ITEM.get());
                                output.accept(ModItems.TERMINAL_ITEM.get());
                                output.accept(ModItems.CARD.get());
                            })
                            .build()
            );
}

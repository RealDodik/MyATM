package com.glbank.myatm.setup;

import com.glbank.myatm.MyATMMod;
import com.glbank.myatm.blockentity.ATMBlockEntity;
import com.glbank.myatm.blockentity.TerminalBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MyATMMod.MODID);

    public static final RegistryObject<BlockEntityType<ATMBlockEntity>> ATM_BE =
            BLOCK_ENTITIES.register("atm",
                    () -> BlockEntityType.Builder.of(ATMBlockEntity::new,
                            ModBlocks.ATM.get()).build(null));

    public static final RegistryObject<BlockEntityType<TerminalBlockEntity>> TERMINAL_BE =
            BLOCK_ENTITIES.register("terminal",
                    () -> BlockEntityType.Builder.of(TerminalBlockEntity::new,
                            ModBlocks.TERMINAL.get()).build(null));
}

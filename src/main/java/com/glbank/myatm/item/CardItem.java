package com.glbank.myatm.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CardItem extends Item {

    public CardItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("CardNumber")) {
            String num = tag.getString("CardNumber");
            if (num.length() == 16) {
                String formatted = num.substring(0, 4) + " " + num.substring(4, 8)
                        + " " + num.substring(8, 12) + " " + num.substring(12, 16);
                tooltip.add(Component.literal("§7Card: §f" + formatted));
            } else {
                tooltip.add(Component.literal("§7Card: §f" + num));
            }
            if (tag.contains("CVV")) {
                tooltip.add(Component.literal("§7CVV: §f" + tag.getString("CVV")));
            }
        } else {
            tooltip.add(Component.literal("§8[blank card — use Card Linker]"));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains("CardNumber");
    }
}
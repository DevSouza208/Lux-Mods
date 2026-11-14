package dev.luxmods.hud.server;

import dev.luxmods.hud.LuxHud;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Simple early-game medicine, independent of other food/medical mods. */
public final class HudHealingItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LuxHud.MOD_ID);
    public static final DeferredItem<BandageItem> BANDAGE =
            ITEMS.registerItem("bandage", BandageItem::new, new Item.Properties().stacksTo(16));

    private HudHealingItems() {}
}

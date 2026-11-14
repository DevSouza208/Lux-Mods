package dev.luxmods.hud.server;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Restores two hearts, consumes one item and has a short cooldown. */
public final class BandageItem extends Item {
    public BandageItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.getHealth() >= player.getMaxHealth()) {
            return InteractionResultHolder.pass(held);
        }
        if (!level.isClientSide) {
            player.heal(4.0F);
            if (!player.getAbilities().instabuild) held.shrink(1);
            player.getCooldowns().addCooldown(this, 80);
        }
        return InteractionResultHolder.sidedSuccess(held, level.isClientSide);
    }
}

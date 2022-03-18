package ru.lionzxy.fastlogblock.ui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ru.lionzxy.fastlogblock.handlers.EventHandlingManager;
import ru.lionzxy.fastlogblock.utils.MinecraftUtils;

public class InfoItem {
    private final EventHandlingManager eventHandlingManager;

    public InfoItem(EventHandlingManager eventHandlingManager) {
        this.eventHandlingManager = eventHandlingManager;
    }

    @SubscribeEvent
    public void onItemRightClick(PlayerInteractEvent.RightClickBlock event) {
        BlockPos pos = event.getPos();
        EnumFacing face = event.getFace();
        if (face != null) {
            pos = pos.offset(face);
        }

        EnumActionResult result = onItemUse(event.getEntityPlayer(), event.getWorld(), pos, event.getItemStack());
        if (result == EnumActionResult.SUCCESS || result == EnumActionResult.PASS) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onItemLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        BlockPos pos = event.getPos();
        EnumActionResult result = onItemUse(event.getEntityPlayer(), event.getWorld(), pos, event.getItemStack());
        if (result == EnumActionResult.SUCCESS || result == EnumActionResult.PASS) {
            event.setCanceled(true);
        }
    }

    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, ItemStack itemStack) {
        if (itemStack.getItem() != Items.WOODEN_HOE) {
            return EnumActionResult.FAIL;
        }
        if (worldIn.isRemote) {
            return EnumActionResult.PASS;
        }
        if (!MinecraftUtils.canShowLog(player)) {
            player.sendMessage(new TextComponentString(String.format(I18n.translateToLocal("message.fastlogblock:blockinfo.event.permissionerror"))));
            return EnumActionResult.PASS;
        }
        player.sendMessage(new TextComponentString(String.format(I18n.translateToLocal("message.fastlogblock:blockinfo.start"), pos.getX(), pos.getY(), pos.getZ())));
        eventHandlingManager.handleLogByPos(player, pos, worldIn);
        return EnumActionResult.SUCCESS;
    }
}

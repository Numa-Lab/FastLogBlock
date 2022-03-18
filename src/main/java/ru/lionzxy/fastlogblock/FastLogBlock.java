package ru.lionzxy.fastlogblock;

import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import ru.lionzxy.fastlogblock.handlers.EventHandlingManager;
import ru.lionzxy.fastlogblock.ui.InfoItem;

import java.io.IOException;

@Mod(modid = FastLogBlock.MODID, version = FastLogBlock.VERSION,
        serverSideOnly = true,
        acceptableRemoteVersions = "*",
        updateJSON = "https://raw.githubusercontent.com/LionZXY/FastLogBlock/master/update.json")
public class FastLogBlock {
    public static final String MODID = "fastlogblock";
    public static final String VERSION = "1.0.2";
    private EventHandlingManager eventHandlingManager;
    private InfoItem infoitem;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) throws IOException {
        FMLLog.log.info("Initializing eventHandlingManager...");
        eventHandlingManager = new EventHandlingManager();
        infoitem = new InfoItem(eventHandlingManager);
        FMLLog.log.info("Done!");
        LanguageMap.inject(getClass().getClassLoader().getResourceAsStream("assets/fastlogblock/lang/en_us.lang"));
        MinecraftForge.EVENT_BUS.register(eventHandlingManager);
        MinecraftForge.EVENT_BUS.register(infoitem);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @EventHandler
    public void serverStopped(final FMLServerStoppedEvent event) {
        eventHandlingManager.stop();
    }
}

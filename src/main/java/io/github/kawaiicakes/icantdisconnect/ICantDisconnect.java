package io.github.kawaiicakes.icantdisconnect;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
@Mod(ICantDisconnect.MODID)
public class ICantDisconnect
{

    public static final String MODID = "icantdisconnect";

    private static final Logger LOGGER = LogUtils.getLogger();

    public ICantDisconnect()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
    }

}

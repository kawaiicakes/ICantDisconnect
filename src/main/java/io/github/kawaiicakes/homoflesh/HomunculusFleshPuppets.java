package io.github.kawaiicakes.homoflesh;

import com.mojang.logging.LogUtils;
import io.github.kawaiicakes.homoflesh.event.DisconnectEvent;
import io.github.kawaiicakes.homoflesh.registry.EntityRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import static io.github.kawaiicakes.homoflesh.Config.CONFIG;

@Mod(HomunculusFleshPuppets.MOD_ID)
public class HomunculusFleshPuppets
{

    public static final String MOD_ID = "homoflesh";

    private static final Logger LOGGER = LogUtils.getLogger();

    public HomunculusFleshPuppets()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CONFIG);

        modEventBus.register(DisconnectEvent.class);
        EntityRegistry.register(modEventBus);
    }
}

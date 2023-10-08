package io.github.kawaiicakes.homoflesh;

import io.github.kawaiicakes.homoflesh.entity.Homunculus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static io.github.kawaiicakes.homoflesh.Config.CONFIG;
import static net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES;

@Mod(HomunculusFleshPuppets.MOD_ID)
public class HomunculusFleshPuppets
{
    public static final String MOD_ID = "homoflesh";

    private static final DeferredRegister<EntityType<?>> REGISTRY_ENTITY_TYPES
            = DeferredRegister.create(ENTITY_TYPES, MOD_ID);

    public static final RegistryObject<EntityType<Homunculus>> HOMUNCULUS
            = REGISTRY_ENTITY_TYPES.register("homunculus", () ->
                    EntityType.Builder.of(Homunculus::new, MobCategory.CREATURE)
                            .sized(0.4f, 1.5f)
                            .setShouldReceiveVelocityUpdates(true)
                            // .setTrackingRange(Config.entityVisionRangeBlocks())
                            .setUpdateInterval(1) // wtf does this do lol
                            .build(new ResourceLocation(MOD_ID, "homunculus").toString()));

    public HomunculusFleshPuppets() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CONFIG);

        REGISTRY_ENTITY_TYPES.register(modEventBus);
    }
}

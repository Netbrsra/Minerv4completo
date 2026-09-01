package br.com.automaticminer.client;

import br.com.automaticminer.AutomaticMiner;
import br.com.automaticminer.init.ModBlocks;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = AutomaticMiner.MODID, value = Side.CLIENT)
public final class ClientModelRegistry {
    private ClientModelRegistry() {}

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(
            net.minecraft.item.Item.getItemFromBlock(ModBlocks.MINER),
            0,
            new ModelResourceLocation(AutomaticMiner.MODID + ":automatic_miner", "inventory")
        );
    }
}

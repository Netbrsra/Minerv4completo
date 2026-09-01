package br.com.automaticminer;

import br.com.automaticminer.gui.GuiHandler;
import br.com.automaticminer.init.ModBlocks;
import br.com.automaticminer.init.ModTileEntities;
import br.com.automaticminer.network.ModNetwork;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid=AutomaticMiner.MODID,name=AutomaticMiner.NAME,version=AutomaticMiner.VERSION)
public class AutomaticMiner {
    public static final String MODID="automaticminer";
    public static final String NAME="Automatic Miner";
    public static final String VERSION="1.0";
    @Mod.Instance(MODID) public static AutomaticMiner INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e){
        ModBlocks.init();
        ModTileEntities.init();
        ModNetwork.init();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e){
        NetworkRegistry.INSTANCE.registerGuiHandler(this,new GuiHandler());
    }
}

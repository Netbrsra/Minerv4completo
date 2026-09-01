package br.com.automaticminer.init;
import br.com.automaticminer.AutomaticMiner;
import br.com.automaticminer.tile.TileAutomaticMiner;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
public class ModTileEntities {
 public static void init(){
  GameRegistry.registerTileEntity(TileAutomaticMiner.class,new ResourceLocation(AutomaticMiner.MODID,"automatic_miner"));
 }
}

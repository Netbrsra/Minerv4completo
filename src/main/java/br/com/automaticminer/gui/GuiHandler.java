package br.com.automaticminer.gui;
import br.com.automaticminer.gui.client.GuiAutomaticMiner;
import br.com.automaticminer.inventory.ContainerAutomaticMiner;
import br.com.automaticminer.tile.TileAutomaticMiner;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
public class GuiHandler implements IGuiHandler {
 public Object getServerGuiElement(int id,EntityPlayer p,World w,int x,int y,int z){
  if(id==0)return new ContainerAutomaticMiner(p.inventory,(TileAutomaticMiner)w.getTileEntity(new net.minecraft.util.math.BlockPos(x,y,z)));return null;}
 public Object getClientGuiElement(int id,EntityPlayer p,World w,int x,int y,int z){
  if(id==0)return new GuiAutomaticMiner(p.inventory,(TileAutomaticMiner)w.getTileEntity(new net.minecraft.util.math.BlockPos(x,y,z)));return null;}
}

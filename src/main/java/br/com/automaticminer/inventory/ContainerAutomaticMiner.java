package br.com.automaticminer.inventory;
import br.com.automaticminer.tile.TileAutomaticMiner;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;

public class ContainerAutomaticMiner extends Container {
 public ContainerAutomaticMiner(InventoryPlayer player, TileAutomaticMiner te){
  // Combustível: slots 0-26
  for(int r=0;r<3;r++) for(int c=0;c<9;c++)
   addSlotToContainer(new Slot(te,c+r*9,8+c*18,28+r*18));
  // Itens minerados: slots 27-53
  for(int r=0;r<3;r++) for(int c=0;c<9;c++)
   addSlotToContainer(new Slot(te,27+c+r*9,8+c*18,105+r*18));
  // Inventário do jogador
  for(int r=0;r<3;r++) for(int c=0;c<9;c++)
   addSlotToContainer(new Slot(player,c+r*9+9,8+c*18,174+r*18));
  for(int c=0;c<9;c++) addSlotToContainer(new Slot(player,c,8+c*18,232));
 }
 @Override public boolean canInteractWith(EntityPlayer p){return true;}
 @Override public ItemStack transferStackInSlot(EntityPlayer p,int i){return ItemStack.EMPTY;}
}
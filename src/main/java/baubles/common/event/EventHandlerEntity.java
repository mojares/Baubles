package baubles.common.event;

import java.io.File;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import baubles.api.IBauble;
import baubles.common.Baubles;
import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;

import com.google.common.io.Files;

public class EventHandlerEntity {

	@SubscribeEvent
	public void playerTick(PlayerEvent.LivingUpdateEvent event) {

		// player events
		if (event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entity;

			InventoryBaubles baubles = PlayerHandler.getPlayerBaubles(player);
			for (int a = 0; a < baubles.getSizeInventory(); a++) {
				if (baubles.getStackInSlot(a) != null
						&& baubles.getStackInSlot(a).getItem() instanceof IBauble) {
					((IBauble) baubles.getStackInSlot(a).getItem()).onWornTick(
							baubles.getStackInSlot(a), player);
				}
			}

		}

	}

	@SubscribeEvent
	public void playerDeath(PlayerDropsEvent event) {
		if (event.entity instanceof EntityPlayer
				&& !event.entity.worldObj.isRemote
				&& !event.entity.worldObj.getGameRules()
						.getGameRuleBooleanValue("keepInventory")) {
			PlayerHandler.getPlayerBaubles(event.entityPlayer).dropItemsAt(
					event.drops,event.entityPlayer);
		}

	}

	@SubscribeEvent
	public void playerLoad(PlayerEvent.LoadFromFile event) {
		PlayerHandler.clearPlayerBaubles(event.entityPlayer);
		
		File file1 = getPlayerFile("baub", event.playerDirectory, event.entityPlayer.getDisplayNameString());
		if (!file1.exists()) {
			File filep = event.getPlayerFile("baub");
			if (filep.exists()) {
				try {
					Files.copy(filep, file1);					
					Baubles.log.info("Using and converting UUID Baubles savefile for "+event.entityPlayer.getDisplayNameString());
					filep.delete();
					File fb = event.getPlayerFile("baubback");
					if (fb.exists()) fb.delete();					
				} catch (IOException e) {}
			} else {
				File filet = getLegacyFileFromPlayer(event.entityPlayer);
				if (filet.exists()) {
					try {
						Files.copy(filet, file1);
						Baubles.log.info("Using pre MC 1.7.10 Baubles savefile for "+event.entityPlayer.getDisplayNameString());
					} catch (IOException e) {}
				}
			}
		}
		
		PlayerHandler.loadPlayerBaubles(event.entityPlayer, file1, getPlayerFile("baubback", event.playerDirectory, event.entityPlayer.getDisplayNameString()));
		EventHandlerNetwork.syncBaubles(event.entityPlayer);
	}
	
	public File getPlayerFile(String suffix, File playerDirectory, String playername)
    {
        if ("dat".equals(suffix)) throw new IllegalArgumentException("The suffix 'dat' is reserved");
        return new File(playerDirectory, playername+"."+suffix);
    }
	
	public static File getLegacyFileFromPlayer(EntityPlayer player)
    {
		try {
			File playersDirectory = new File(player.worldObj.getSaveHandler().getWorldDirectory(), "players");
			return new File(playersDirectory, player.getDisplayNameString() + ".baub");
		} catch (Exception e) { e.printStackTrace(); }
		return null;
    }

	@SubscribeEvent
	public void playerSave(PlayerEvent.SaveToFile event) {
		PlayerHandler.savePlayerBaubles(event.entityPlayer, 
				getPlayerFile("baub", event.playerDirectory, event.entityPlayer.getDisplayNameString()),
				getPlayerFile("baubback", event.playerDirectory, event.entityPlayer.getDisplayNameString()));
	}

}

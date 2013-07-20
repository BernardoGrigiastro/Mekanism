package mekanism.generators.client;

import mekanism.common.MekanismUtils;
import mekanism.common.MekanismUtils.ResourceType;
import mekanism.generators.common.ContainerHydrogenGenerator;
import mekanism.generators.common.TileEntityHydrogenGenerator;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;

@SideOnly(Side.CLIENT)
public class GuiHydrogenGenerator extends GuiContainer
{
	public TileEntityHydrogenGenerator tileEntity;
	
	private int guiWidth;
	private int guiHeight;
	
	public GuiHydrogenGenerator(InventoryPlayer inventory, TileEntityHydrogenGenerator tentity)
    {
        super(new ContainerHydrogenGenerator(inventory, tentity));
        tileEntity = tentity;
    }

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
		int xAxis = (mouseX - (width - xSize) / 2);
		int yAxis = (mouseY - (height - ySize) / 2);
		
        fontRenderer.drawString(tileEntity.fullName, 45, 6, 0x404040);
        fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
        fontRenderer.drawString(ElectricityDisplay.getDisplayShort(tileEntity.electricityStored, ElectricUnit.JOULES), 51, 26, 0x00CD00);
        fontRenderer.drawString("H: " + tileEntity.hydrogenStored, 51, 35, 0x00CD00);
        fontRenderer.drawString(tileEntity.getVoltage() + "v", 51, 44, 0x00CD00);
        
    	if(xAxis >= 165 && xAxis <= 169 && yAxis >= 17 && yAxis <= 69)
		{
			drawCreativeTabHoveringText(ElectricityDisplay.getDisplayShort(tileEntity.electricityStored, ElectricUnit.JOULES), xAxis, yAxis);
		}
    }

	@Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
		mc.renderEngine.func_110577_a(MekanismUtils.getResource(ResourceType.GUI, "GuiHydrogenGenerator.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        guiWidth = (width - xSize) / 2;
        guiHeight = (height - ySize) / 2;
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        int displayInt;
        
        displayInt = tileEntity.getScaledHydrogenLevel(52);
        drawTexturedModalRect(guiWidth + 7, guiHeight + 17 + 52 - displayInt, 176, 52 + 52 - displayInt, 4, displayInt);
        
        displayInt = tileEntity.getScaledEnergyLevel(52);
        drawTexturedModalRect(guiWidth + 165, guiHeight + 17 + 52 - displayInt, 176, 52 - displayInt, 4, displayInt);
    }
}

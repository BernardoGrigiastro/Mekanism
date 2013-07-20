package mekanism.generators.client;

import mekanism.common.MekanismUtils;
import mekanism.common.MekanismUtils.ResourceType;
import mekanism.generators.common.TileEntityHeatGenerator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderHeatGenerator extends TileEntitySpecialRenderer
{
	private ModelHeatGenerator model = new ModelHeatGenerator();
	
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick)
	{
		renderAModelAt((TileEntityHeatGenerator)tileEntity, x, y, z, partialTick);
	}
	
	private void renderAModelAt(TileEntityHeatGenerator tileEntity, double x, double y, double z, float partialTick)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
		func_110628_a(MekanismUtils.getResource(ResourceType.RENDER, tileEntity.isActive ? "HeatGeneratorOn.png" : "HeatGenerator.png"));
		
	    switch(tileEntity.facing)
	    {
		    case 2: GL11.glRotatef(270, 0.0F, 1.0F, 0.0F); break;
			case 3: GL11.glRotatef(90, 0.0F, 1.0F, 0.0F); break;
			case 4: GL11.glRotatef(0, 0.0F, 1.0F, 0.0F); break;
			case 5: GL11.glRotatef(180, 0.0F, 1.0F, 0.0F); break;
	    }
		
		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
		model.render(0.0625F);
		GL11.glPopMatrix();
	}
}

/*
 * LibraryEx
 * Copyright (c) 2017-2019 by LogicTechCorp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package logictechcorp.libraryex.block;

import logictechcorp.libraryex.block.builder.BlockProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class BlockModInfiniteFluid extends BlockFluidClassic
{
    public BlockModInfiniteFluid(ResourceLocation registryName, Fluid fluid, BlockProperties properties)
    {
        super(fluid, properties.getMaterial());
        this.setRegistryName(registryName);
        this.setSoundType(properties.getSoundType());
        this.setLightLevel(properties.getLightLevel());
        this.setHarvestLevel(properties.getHarvestTool(), properties.getHarvestLevel());
        this.setHardness(properties.getHardness());
        this.setResistance(properties.getResistance());
        this.setTickRandomly(properties.needsRandomTick());
        this.setTranslationKey(registryName.toString());
    }
}

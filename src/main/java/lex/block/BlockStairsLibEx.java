/*
 * LibEx
 * Copyright (c) 2017-2018 by MineEx
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

package lex.block;

import com.google.common.base.CaseFormat;
import lex.IModData;
import lex.client.model.item.IModelContainer;
import lex.client.model.item.ItemModelHandler;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockStairsLibEx extends BlockStairs implements IModelContainer
{
    public BlockStairsLibEx(IModData data, String name, IBlockState state)
    {
        super(state);
        this.setRegistryName(data.getModId() + ":" + name + "_stairs");
        this.setTranslationKey(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, this.getRegistryName().toString()));
        this.useNeighborBrightness = true;
        this.setSoundType(SoundType.STONE);
        this.setCreativeTab(data.getCreativeTab());
        data.getModelContainers().add(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel()
    {
        ItemModelHandler.registerBlockModel(this, "normal");
    }
}

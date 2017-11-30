/*
 * LibEx
 * Copyright (c) 2017 by MineEx
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

package lex.world.gen.feature;

import lex.config.IConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class FeatureCluster extends AbstractFeature
{
    protected IBlockState blockToSpawn;
    protected IBlockState blockToAttachTo;
    protected EnumFacing direction;

    FeatureCluster(Builder builder)
    {
        super(builder);
        blockToSpawn = builder.blockToSpawn;
        blockToAttachTo = builder.blockToAttachTo;
        direction = builder.direction;
    }

    @Override
    public boolean generate(World world, Random rand, BlockPos pos)
    {
        if(!world.isAirBlock(pos))
        {
            return false;
        }
        else if(world.getBlockState(pos.offset(direction.getOpposite())) != blockToAttachTo)
        {
            return false;
        }
        else
        {
            world.setBlockState(pos, blockToSpawn, 3);

            for(int i = 0; i < 1500; i++)
            {
                BlockPos newPos;

                switch(direction)
                {
                    default:
                    case DOWN:
                        newPos = pos.add(rand.nextInt(8) - rand.nextInt(8), -rand.nextInt(12), rand.nextInt(8) - rand.nextInt(8));
                        break;
                    case UP:
                        newPos = pos.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(12), rand.nextInt(8) - rand.nextInt(8));
                        break;
                    case NORTH:
                        newPos = pos.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(8) - rand.nextInt(8), -rand.nextInt(12));
                        break;
                    case SOUTH:
                        newPos = pos.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(8) - rand.nextInt(8), rand.nextInt(12));
                        break;
                    case WEST:
                        newPos = pos.add(-rand.nextInt(12), rand.nextInt(8) - rand.nextInt(8), rand.nextInt(8) - rand.nextInt(8));
                        break;
                    case EAST:
                        newPos = pos.add(rand.nextInt(12), rand.nextInt(8) - rand.nextInt(8), rand.nextInt(8) - rand.nextInt(8));
                        break;
                }

                if(world.isAirBlock(newPos))
                {
                    int j = 0;

                    for(EnumFacing facing : EnumFacing.values())
                    {
                        if(world.getBlockState(newPos.offset(facing)).getBlock() == blockToSpawn.getBlock())
                        {
                            j++;
                        }

                        if(j > 1)
                        {
                            break;
                        }
                    }

                    if(j == 1)
                    {
                        world.setBlockState(newPos, blockToSpawn, 3);
                    }
                }
            }

            return true;
        }
    }

    public static class Builder extends AbstractBuilder<Builder, FeatureCluster>
    {
        protected IBlockState blockToSpawn;
        protected IBlockState blockToAttachTo;
        protected EnumFacing direction;

        @Override
        public Builder configure(IConfig config)
        {
            super.configure(config);
            blockToSpawn = config.getBlock("blockToSpawn", Blocks.STONE.getDefaultState());
            blockToAttachTo = config.getBlock("blockToAttachTo", Blocks.AIR.getDefaultState());
            direction = config.getEnum("direction", EnumFacing.class, EnumFacing.DOWN);
            return this;
        }

        @Override
        public FeatureCluster create()
        {
            return new FeatureCluster(this);
        }
    }
}
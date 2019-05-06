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

package logictechcorp.libraryex.world.generation.trait.impl;

import com.electronwill.nightconfig.core.Config;
import logictechcorp.libraryex.utility.ConfigHelper;
import logictechcorp.libraryex.utility.RandomHelper;
import logictechcorp.libraryex.utility.StructureHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;

import java.util.*;

public class BiomeTraitStructure extends BiomeTraitConfigurable
{
    private List<ResourceLocation> structures;
    private Type type;
    private Mirror mirror;
    private Rotation rotation;
    private Block ignoredBlock;
    private double clearancePercentage;
    private boolean orientRandomly;

    public BiomeTraitStructure(int generationAttempts, boolean randomizeGenerationAttempts, double generationProbability, int minimumGenerationHeight, int maximumGenerationHeight, List<ResourceLocation> structures, Type type, Mirror mirror, Rotation rotation, Block ignoredBlock, double clearancePercentage)
    {
        super(generationAttempts, randomizeGenerationAttempts, generationProbability, minimumGenerationHeight, maximumGenerationHeight);
        this.structures = structures;
        this.type = type;
        this.mirror = mirror;
        this.rotation = rotation;
        this.ignoredBlock = ignoredBlock;
        this.clearancePercentage = clearancePercentage;
        this.orientRandomly = false;
    }

    public BiomeTraitStructure(int generationAttempts, boolean randomizeGenerationAttempts, double generationProbability, int minimumGenerationHeight, int maximumGenerationHeight, List<ResourceLocation> structures, Type type, Block ignoredBlock, double clearancePercentage)
    {
        super(generationAttempts, randomizeGenerationAttempts, generationProbability, minimumGenerationHeight, maximumGenerationHeight);
        this.structures = structures;
        this.type = type;
        this.ignoredBlock = ignoredBlock;
        this.clearancePercentage = clearancePercentage;
        this.orientRandomly = true;
    }

    private BiomeTraitStructure(Builder builder)
    {
        super(builder);
        this.structures = builder.structures;
        this.type = builder.type;
        this.mirror = builder.mirror;
        this.rotation = builder.rotation;
        this.ignoredBlock = builder.ignoredBlock;
        this.clearancePercentage = builder.clearancePercentage;
    }

    @Override
    public void readFromConfig(Config config)
    {
        super.readFromConfig(config);
        List<String> structureNames = config.getOrElse("structures", new ArrayList<>());
        this.structures = new ArrayList<>();

        for(String structureName : structureNames)
        {
            this.structures.add(new ResourceLocation(structureName));
        }

        this.type = config.getEnumOrElse("type", Type.GROUND);
        this.orientRandomly = config.getOrElse("orientRandomly", true);

        if(!this.orientRandomly)
        {
            this.mirror = config.getEnumOrElse("mirror", RandomHelper.getRandomEnum(Mirror.class));
            this.rotation = config.getEnumOrElse("rotation", RandomHelper.getRandomEnum(Rotation.class));
        }

        IBlockState ignoredBlockState = ConfigHelper.getBlockState(config, "ignoredBlock");

        if(ignoredBlockState != null)
        {
            this.ignoredBlock = ignoredBlockState.getBlock();
        }

        this.clearancePercentage = config.getOrElse("clearancePercentage", 0.875D);
    }

    @Override
    public void writeToConfig(Config config)
    {
        super.writeToConfig(config);
        List<String> structureNames = new ArrayList<>();

        for(ResourceLocation structureName : this.structures)
        {
            structureNames.add(structureName.toString());
        }

        config.add("structures", structureNames);
        config.add("type", this.type.toString().toLowerCase());

        if(!this.orientRandomly)
        {
            config.add("mirror", this.mirror);
            config.add("rotation", this.rotation);
        }

        ConfigHelper.setBlockState(config, "ignoredBlock", this.ignoredBlock.getDefaultState());
        config.add("clearancePercentage", this.clearancePercentage);
    }

    @Override
    public boolean generate(World world, BlockPos pos, Random random)
    {
        if(this.structures == null || this.type == null || this.ignoredBlock == null)
        {
            return false;
        }

        if(this.orientRandomly)
        {
            this.mirror = RandomHelper.getRandomEnum(Mirror.class, random);
            this.rotation = RandomHelper.getRandomEnum(Rotation.class, random);
        }

        MinecraftServer server = world.getMinecraftServer();
        TemplateManager templateManager = world.getSaveHandler().getStructureTemplateManager();
        Template template = templateManager.get(server, this.structures.get(random.nextInt(this.structures.size())));

        if(template != null)
        {
            PlacementSettings placementSettings = new PlacementSettings().setMirror(this.mirror).setRotation(this.rotation).setReplacedBlock(this.ignoredBlock).setRandom(random);
            BlockPos structureSize = template.transformedSize(placementSettings.getRotation());
            BlockPos spawnPos = null;

            if(this.type == Type.GROUND)
            {
                spawnPos = StructureHelper.getGroundPos(world, pos, placementSettings, structureSize, this.clearancePercentage);
            }
            else if(this.type == Type.AIR)
            {
                spawnPos = StructureHelper.getAirPos(world, pos, placementSettings, structureSize, this.clearancePercentage);
            }
            else if(this.type == Type.BURIED)
            {
                spawnPos = StructureHelper.getBuriedPos(world, pos, placementSettings, structureSize, this.clearancePercentage);
            }
            else if(this.type == Type.CEILING)
            {
                spawnPos = StructureHelper.getCeilingPos(world, pos, placementSettings, structureSize, this.clearancePercentage);
            }

            if(spawnPos != null && spawnPos.getY() >= this.minimumGenerationHeight && spawnPos.getY() <= this.maximumGenerationHeight)
            {
                template.addBlocksToWorld(world, spawnPos, placementSettings.copy());
                this.handleDataBlocks(world, spawnPos, template, placementSettings.copy(), random);
                return true;
            }
        }

        if(this.orientRandomly)
        {
            this.mirror = null;
            this.rotation = null;
        }

        return false;
    }

    private void handleDataBlocks(World world, BlockPos pos, Template template, PlacementSettings placementSettings, Random random)
    {
        Map<BlockPos, String> map = template.getDataBlocks(pos, placementSettings);

        for(Map.Entry<BlockPos, String> entry : map.entrySet())
        {
            BlockPos dataPos = entry.getKey();
            String[] data = entry.getValue().split("\\s+");

            if(data[0].equals("chest") && data.length == 2)
            {
                world.setBlockState(dataPos, Blocks.CHEST.correctFacing(world, dataPos, Blocks.CHEST.getDefaultState()));
                TileEntityChest chest = (TileEntityChest) world.getTileEntity(dataPos);

                if(chest != null)
                {
                    chest.setLootTable(new ResourceLocation(data[1]), random.nextLong());
                }
            }
            else if(data[0].equals("spawner") && data.length == 2)
            {
                world.setBlockState(dataPos, Blocks.MOB_SPAWNER.getDefaultState());
                TileEntityMobSpawner spawner = (TileEntityMobSpawner) world.getTileEntity(dataPos);

                if(spawner != null)
                {
                    MobSpawnerBaseLogic logic = spawner.getSpawnerBaseLogic();
                    NBTTagCompound compound = new NBTTagCompound();
                    logic.writeToNBT(compound);
                    compound.removeTag("SpawnPotentials");
                    logic.readFromNBT(compound);
                    logic.setEntityId(new ResourceLocation(data[1]));
                    spawner.markDirty();
                    IBlockState state = world.getBlockState(dataPos);
                    world.notifyBlockUpdate(pos, state, state, 3);
                }
            }
            else if(data[0].equals("entity") && data.length == 2)
            {
                Entity entity = EntityList.newEntity(EntityList.getClass(new ResourceLocation(data[1])), world);

                if(entity != null)
                {
                    entity.setPosition(dataPos.getX() + 0.5F, dataPos.getY(), dataPos.getZ() + 0.5F);
                    world.spawnEntity(entity);
                }
            }
            else
            {
                world.setBlockToAir(dataPos);
            }
        }
    }

    public static class Builder extends BiomeTrait.Builder
    {
        private List<ResourceLocation> structures;
        private Type type;
        private Mirror mirror;
        private Rotation rotation;
        private Block ignoredBlock;
        private double clearancePercentage;
        private boolean orientRandomly;

        public Builder()
        {
            this.structures = Collections.singletonList(new ResourceLocation("minecraft:missing_no"));
            this.type = Type.GROUND;
            this.mirror = null;
            this.rotation = null;
            this.ignoredBlock = Blocks.STRUCTURE_VOID;
            this.clearancePercentage = 75.0D;
        }

        public Builder structure(List<ResourceLocation> structures)
        {
            this.structures = structures;
            return this;
        }

        public Builder type(Type type)
        {
            this.type = type;
            return this;
        }

        public Builder mirror(Mirror mirror)
        {
            this.mirror = mirror;
            return this;
        }

        public Builder rotation(Rotation rotation)
        {
            this.rotation = rotation;
            return this;
        }

        public Builder ignoredBlock(Block ignoredBlock)
        {
            this.ignoredBlock = ignoredBlock;
            return this;
        }

        public Builder clearancePercentage(double clearancePercentage)
        {
            this.clearancePercentage = clearancePercentage;
            return this;
        }

        public Builder orientRandomly(boolean orientRandomly)
        {
            this.orientRandomly = orientRandomly;
            return this;
        }

        @Override
        public BiomeTrait create()
        {
            return new BiomeTraitStructure(this);
        }
    }

    public enum Type
    {
        GROUND,
        AIR,
        CEILING,
        BURIED
    }
}

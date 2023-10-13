package com.rosstail.karma.blocks;

import kotlin.text.Regex;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class BlocksData {

    public final BlocksModel blocksModel;
    public final List<Integer> ageList = new ArrayList<Integer>();
    public final boolean ageReverse;

    public BlocksData(BlocksModel blocksModel, ConfigurationSection section) {
        this.blocksModel = blocksModel;
        this.ageList.addAll(section.getIntegerList("age.ages"));
        this.ageReverse = section.getBoolean("age.blacklist", false);
    }

    public boolean checkData(Block block) {
        if (block.getBlockData() instanceof Ageable && !ageList.isEmpty()) {
           int age = ((Ageable) block.getBlockData()).getAge();

           if ((ageReverse && ageList.contains(age)) || (!ageReverse && !ageList.contains(age))) {
               return false;
           }

           //other things. Do not simplify
        }
        return true;
    }
}

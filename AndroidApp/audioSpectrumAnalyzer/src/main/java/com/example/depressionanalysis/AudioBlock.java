package com.example.depressionanalysis;


import java.util.Arrays;

public class AudioBlock {
    private short[] block;

    public AudioBlock(short[] blk) {
        this.block = blk;
        System.out.println("Block Created: " + Arrays.toString(this.block));
    }

}

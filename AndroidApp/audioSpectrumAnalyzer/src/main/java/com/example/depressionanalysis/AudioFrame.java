package com.example.depressionanalysis;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class AudioFrame {
    private AudioBlock[] aFrame = new AudioBlock[5];
    private int counter = 0;

    public AudioFrame(){

    }

    public AudioFrame(@NonNull AudioFrame oldFrame){
        AudioBlock[] blocks = oldFrame.getBlocks();
//        while (counter < 4) {
//            this.aFrame[counter] = blocks[counter + 2];
            this.aFrame[0] = blocks[2];
            this.aFrame[1] = blocks[3];
            this.aFrame[2] = blocks[4];
            counter = 3;
//            counter++;
//        }
    }

    public AudioBlock[] getBlocks() {
        return aFrame;
    }

    public boolean isBlockFull() {
        if(counter == 5){

            return true;

        }

        return false;
    }

    public void setBlocks(AudioBlock[] aFrame) {
        this.aFrame = aFrame;
    }

    public int noOfBlocks(){
        return counter;
    }

    public boolean addBlock(AudioBlock aBlock){
        //Checks if number of blocks
        if (counter < 5) {
            aFrame[counter] = aBlock;
            counter++;
            return true;
        }
        return false;
    }

    public String getContent(){
        String str = "Frame Content: {";

        str = "Frame Content: " + Arrays.toString(aFrame);

        if (this.counter > 0)
            str = str + this.aFrame[0].toString();

        if(this.counter > 1)
            str = str + "," + this.aFrame[1].toString();

        if(this.counter > 2)
            str = str + "," + this.aFrame[2].toString();

        if(this.counter > 3)
            str = str + "," + this.aFrame[3].toString();

        if(this.counter > 4)
            str = str + "," + this.aFrame[4].toString();

        str = str + "}";

        return str;
    }
}

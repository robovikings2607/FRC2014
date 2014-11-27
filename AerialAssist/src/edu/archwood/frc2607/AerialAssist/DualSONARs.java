/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.archwood.frc2607.AerialAssist;

import edu.wpi.first.wpilibj.AnalogChannel;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DigitalOutput;

/**
 *
 * @author frcdev
 */
public class DualSONARs {
    private final DigitalInput RX1, RX2;
    private final DigitalOutput TX1, TX2;
    private final AnalogChannel AC1, AC2;
    private double leftValue, rightValue;
    private boolean left = false;
    public DualSONARs(int RX1, int RX2, int TX1, int TX2, int AC1, int AC2) {
        this.RX1 = new DigitalInput(RX1);
        this.RX2 = new DigitalInput(RX2);
        this.TX1 = new DigitalOutput(TX1);
        this.TX2 = new DigitalOutput(TX2);
        this.AC1 = new AnalogChannel(AC1);
        this.AC2 = new AnalogChannel(AC2);
    }
    public void poll(double time)
    {
        if (left)
        {
            TX1.set(true);
            TX2.set(false);
            leftValue = AC1.getVoltage()/(5d/512d);
            left = !left;
        }
        else
        {
            TX1.set(false);
            TX2.set(true);
            rightValue = AC2.getVoltage()/(5d/512d);
            left = !left;
        }
    }

    public double getLeftRange() {
        return leftValue;
    }

    public double getRightRange() {
        return rightValue;
    }
    
}

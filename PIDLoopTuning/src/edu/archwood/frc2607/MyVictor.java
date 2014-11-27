/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.archwood.frc2607;

import edu.wpi.first.wpilibj.Victor;

/**
 *
 * @author frcdev
 */
public class MyVictor extends Victor {
    
    private double curSpeed = 0.0;
    
    public MyVictor(int pwm) {
        super(pwm);
    }
    
    public void pidWrite(double speed) {
        curSpeed += speed;
        set(curSpeed);
    }
}

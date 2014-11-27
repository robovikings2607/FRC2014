/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.archwood.frc2607.AerialAssist;

import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Talon;

/**
 *
 * @author rossron
 */
public class TalonPair implements PIDOutput {
    private Talon motor1, motor2;
    
    // motor2PWM may be 0 if we're using y-splitters
    public TalonPair(int motor1PWM, int motor2PWM) {
        motor1 = new Talon(motor1PWM);
        if (motor2PWM > 0) {
            motor2 = new Talon(motor2PWM);
        } else {
            motor2 = null;
        }
    }
    
    public void pidWrite(double d) {
        if (motor2 != null) {
            motor1.set(d);
            motor2.set(d);
        } else {
            motor1.set(d);
        }
    }
    
}

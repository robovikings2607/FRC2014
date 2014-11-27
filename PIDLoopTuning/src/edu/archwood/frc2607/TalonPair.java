/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.archwood.frc2607;

import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Talon;

/**
 *
 * @author rossron
 */
public class TalonPair implements SpeedController {
    private Talon motor1, motor2;
    
    public TalonPair(int motor1PWM, int motor2PWM) {
        motor1 = new Talon(motor1PWM);
        motor2 = new Talon(motor2PWM);
    }

    public void pidWrite(double d) {
        motor1.set(d);
        motor2.set(d);
    }

    public double get() {
        return motor1.get();
    }

    public void set(double d, byte b) {
        motor1.set(d);
        motor2.set(d);
    }

    public void set(double d) {
        motor1.set(d);
        motor2.set(d);
    }

    public void disable() {
        motor1.disable();
        motor2.disable();
    }
}

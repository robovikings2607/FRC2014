/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.archwood.frc2607.AerialAssist;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.SpeedController;

/**
 *
 * @author rossron
 */

public class WheelRPMController implements SpeedController, CompBotConstants {
    private PIDController pidLoop;
    private SmoothedEncoder enc;
    private TalonPair motors;
    private double curMaxSpeed;
    private int wheelIndex;
    private String deviceName;
    private boolean forceOff = false;
    public static boolean off;
    private short errorCount = 0;
    public WheelRPMController(String name, int index) {
//        motors = new TalonPair(pwmMecanumAddresses[index][0], 
//                               pwmMecanumAddresses[index][1]);
        motors = new TalonPair(pwmMecanumAddresses[index][0], 0);  // 2nd parameter = 0 when 
                                                                   // using the Y-splitter
        
        enc = new SmoothedEncoder(diEncoders[index][0], diEncoders[index][1],
                                  false, Encoder.EncodingType.k1X);
        pidLoop = new PIDController(talonHighGearPIDGains[index][0],
                                    talonHighGearPIDGains[index][1],
                                    talonHighGearPIDGains[index][2],
                                    enc, motors);
        wheelIndex = index;
        deviceName = name;
        curMaxSpeed = talonHighGearMaxSpeeds[index];
        pidLoop.setInputRange(-curMaxSpeed, curMaxSpeed);
        
    }

    public void displayWheelRPM() {
        System.out.print(deviceName + ": ");
        System.out.print(enc.getCurrentRate() + " ");
    }
    
    public void setGearPID(boolean highGear) {
        if (highGear) {
            
            pidLoop.setPID(talonHighGearPIDGains[wheelIndex][0],
                           talonHighGearPIDGains[wheelIndex][1],
                           talonHighGearPIDGains[wheelIndex][2]);
            curMaxSpeed = talonHighGearMaxSpeeds[wheelIndex];
            pidLoop.setInputRange(-curMaxSpeed, curMaxSpeed);                       
        } else {
            pidLoop.setPID(talonLowGearPIDGains[wheelIndex][0],
                           talonLowGearPIDGains[wheelIndex][1],
                           talonLowGearPIDGains[wheelIndex][2]);
            curMaxSpeed = talonLowGearMaxSpeeds[wheelIndex];
            pidLoop.setInputRange(-curMaxSpeed, curMaxSpeed);                                   
        }
    }
    
    public double get() {
        return pidLoop.getSetpoint();
    }

    public void set(double d, byte b) {
        set(d);
    }
    public int getError()
    {
        if (errorCount>10)
        {
            errorCount = 10;
            return (1<<wheelIndex);
        }
        return 0;
    }
    
    public void set(double d) {
        if (d == 0&&off)
        {
            pidLoop.reset();
            forceOff = true;
        }
        else
        {
            if (forceOff)
            {
                forceOff = false;
                pidLoop.enable();
            }
            pidLoop.setSetpoint(d * curMaxSpeed);
            if (Math.abs(d*curMaxSpeed)>0&&enc.getCurrentRate()==0)
            {
                errorCount++;
            }
            else
            {
                errorCount = 0;
            }
        }
    }

    public void disable() {
        enc.reset();
        pidLoop.disable();
    }

    public void enable() {
        enc.setPIDSourceParameter(PIDSource.PIDSourceParameter.kRate);
        enc.start();
        pidLoop.enable();
    }
    
    public void pidWrite(double d) {
        
    } 
}

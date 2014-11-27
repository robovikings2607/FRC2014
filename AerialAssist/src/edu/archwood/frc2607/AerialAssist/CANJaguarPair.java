/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.archwood.frc2607.AerialAssist;

import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.can.CANTimeoutException;

/**
 *
 * @author frcdev
 */
public class CANJaguarPair implements SpeedController, CompBotConstants {
    private double desiredSpeed;
    private final String deviceName;
    private final int jagIndex;
    private int voltJagBrownOutCount, speedJagBrownOutCount;
    private int maxSpeed;
    private static int badJag = 0;
    private CANJaguar speedJag = null, voltJag = null;
                      
    private boolean speedJagOK(String opName) {
        if (speedJag == null) {
            System.out.println(deviceName + ":speedJag is NULL!  Skipping " + opName);
            return false;
        } else {
            return true;
        }
    }
    
    private boolean voltJagOK(String opName) {
        if (voltJag == null) {
            System.out.println(deviceName + ":voltJag is NULL!  Skipping " + opName);
            return false;
        } else {
            return true;
        }
    }
    
    public void setGearPID(boolean highGear) throws Exception {

        if (highGear) {
            System.out.println(deviceName + " setting HIGH GEAR PID");
            if (speedJagOK("setGearPID")) {
                speedJag.setPID(jagHighGearPIDGains[jagIndex][0], jagHighGearPIDGains[jagIndex][1], jagHighGearPIDGains[jagIndex][2]);
            }
            maxSpeed = jagHighGearMaxSpeed;
        } else {
            System.out.println(deviceName + " setting LOW GEAR PID");
            if (speedJagOK("setGearPID")) {
                speedJag.setPID(jagLowGearPIDGains[jagIndex][0], jagLowGearPIDGains[jagIndex][1], jagLowGearPIDGains[jagIndex][2]);
            }
            maxSpeed = jagLowGearMaxSpeed;
        }
        
    }
    
    public CANJaguarPair(String name, int index) {
        deviceName = name;
        jagIndex = index;
        voltJagBrownOutCount = 0;
        speedJagBrownOutCount = 0;
        
        int retries = 0;
        while (speedJag == null && ++retries < 15) {
            try {
                speedJag = new CANJaguar(canMecanumAddresses[jagIndex][speedJagIndex]);                
            } catch (CANTimeoutException e) {
                badJag = canMecanumAddresses[jagIndex][speedJagIndex];
                System.out.println("retry CANJaguarPair(" + deviceName + 
                        "):speedJag (CAN Addr: " + canMecanumAddresses[jagIndex][speedJagIndex] + ")"); 
                Timer.delay(.25);
            }
        }

        retries = 0;
        while (voltJag == null && ++retries < 15) {
            try {
                voltJag = new CANJaguar(canMecanumAddresses[jagIndex][voltJagIndex]);                
            } catch (CANTimeoutException e) {
                badJag = canMecanumAddresses[jagIndex][speedJagIndex];
                System.out.println("retry CANJaguarPair(" + deviceName + 
                        "):voltJag (CAN Addr: " + canMecanumAddresses[jagIndex][voltJagIndex] + ")"); 
                Timer.delay(.25);
            }
        }    
        configSpeedJag(true);
        configVoltJag();
        badJag = 0;
        maxSpeed = jagHighGearMaxSpeed;
    }

    public static int getBadJag() {
        return badJag;
    }

    private void configVoltJag() {
        boolean keepTrying = true;
        if (!voltJagOK("configVoltJag")) return;
        while (keepTrying) {
            try {
                voltJag.changeControlMode(CANJaguar.ControlMode.kVoltage);
                voltJag.configNeutralMode(CANJaguar.NeutralMode.kCoast);
                voltJag.enableControl();
                keepTrying = false;
            } catch (CANTimeoutException e) {
                System.out.println("retry CANJaguarPair(" + deviceName + "):configVoltJag");
                Timer.delay(.25);
            }
        }    
    }
    
    private void configSpeedJag(boolean highGear) {         
        boolean keepTrying = true;
        if (!speedJagOK("configSpeedJag")) return;
        while (keepTrying) {
            try {
                speedJag.changeControlMode(CANJaguar.ControlMode.kSpeed);
                speedJag.setSpeedReference(CANJaguar.SpeedReference.kEncoder);
                speedJag.configEncoderCodesPerRev(jagEncoderLines[jagIndex]);
                setGearPID(highGear);
                speedJag.enableControl();
                keepTrying = false;
            } catch (Exception e) {
                System.out.println("retry CANJaguarPair(" + deviceName + "):configSpeedJag");
                Timer.delay(.25);
            }
        }
    }

    public double get() {
        return desiredSpeed;
    }

    public double getSpeedJagSpeed() {
        if (speedJagOK("getSpeedJagSpeed")) {
            try { return speedJag.getSpeed(); } catch (Exception e) { return 0.0; }
        } else {
            return 0.0;
        }
    }
    
    public void getBrownOuts() {        
        System.out.println(deviceName + " speedJagBrownOuts: " + speedJagBrownOutCount + 
                "; voltJagBrownOuts: " + voltJagBrownOutCount);    
        speedJagBrownOutCount = 0;
        voltJagBrownOutCount = 0;
    }
    
    public void checkForBrownOuts(final boolean highGear) {
        try {
            if (speedJagOK("checkForBrownOuts")) {
                if (speedJag.getPowerCycled()) {
                    speedJagBrownOutCount += 1;
                    System.out.print(deviceName);
                    System.out.println(" speedJag BROWN OUT");
                    new Thread(new Runnable(){public void run(){try{Thread.sleep(1000);}catch(InterruptedException e){}configSpeedJag(highGear);}}).start();                
                }
            }
            if (voltJagOK("checkForBrownOuts")) {
                if (voltJag.getPowerCycled()) {
                    voltJagBrownOutCount += 1;
                    System.out.print(deviceName); 
                    System.out.println(" voltJag BROWN OUT");
                    new Thread(new Runnable(){public void run(){try{Thread.sleep(1000);}catch(InterruptedException e){}configVoltJag();}}).start();
                }
            }
        } catch (CANTimeoutException e) {
            System.out.println("EXCEPTION: CANJaguarPair(" + deviceName + ").checkForBrownOuts " + e.getClass().getName());
        }
    }
    
    public void set(double d, byte b) {        
        desiredSpeed = d;
        
        try {
            if (speedJagOK("set")) {
                speedJag.setX(d,b);
                if (voltJagOK("set")) voltJag.setX(speedJag.getOutputVoltage());
            } else {
                if (voltJagOK("set")) voltJag.setX((desiredSpeed / maxSpeed) * 12.0);
            }
        } catch (CANTimeoutException e) {
            System.out.println("EXCEPTION: CANJaguarPair(" + deviceName + ").set(" + d + "," + b + ") : " + e.getClass().getName());
        }
    }

    public void set(double d) {
        System.out.println("CANJaguarPair(" + deviceName + ").set(double) does nothing!");
    }

    public void disable() {

        try {
            if (speedJagOK("disable")) speedJag.disableControl();
            if (voltJagOK("disable")) voltJag.disableControl();
        } catch (CANTimeoutException e) {
            System.out.println("CANJaguarPair(" + deviceName + ").disable() : " + e.getMessage());
            //e.printStackTrace();
        }
    }

    public void pidWrite(double d) {
        System.out.println("CANJaguarPair(" + deviceName + ").pidWrite(double) does nothing!");
        //while (true) System.out.println("LIES! It prints out: CANJaguarPair(" + deviceName + ").pidWrite(double) does nothing!");
    }
        
}

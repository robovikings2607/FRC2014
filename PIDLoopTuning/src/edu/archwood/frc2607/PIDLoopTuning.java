/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package edu.archwood.frc2607;


import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class PIDLoopTuning extends IterativeRobot {
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    
    public Encoder enc;
    public PIDController pid;
    public TalonPair motors;
    public robovikingStick stick;
    public int tick;
    public double Kp, Ki, Kd;
    private int countKp, countKi, countKd;
    // From jags below:
    // ?? 2200 low gear max?  4400 high gear max?
    // ?? lowspeed   //use .06 .005 .001 2200RPM max
    // ?? highspeed  // use .02 .005 .001  4400RPM max
    
    // for the 250 line encoder (no hole in cover):
    // high gear 19230 max  // .00002 .00001 0.0
    // low gear 8620 max    // .00006 .00003 0.0
    
    // for the 360 line encoders:
    // high gear 26315 max  // same PID gains
    // low gear 11904 max   // same PID gains
    
    public void robotInit() {
        motors = new TalonPair(5,6);
        enc = new Encoder(1,2, false, Encoder.EncodingType.k1X);
        enc.setPIDSourceParameter(PIDSource.PIDSourceParameter.kRate);
        enc.start();
        stick = new robovikingStick(1);
        countKp = 0;
        countKi = 0;
        countKd = 0;
        pid = new PIDController(0.0, 0.0, 0.0, enc, motors);
        pid.disable();
        // pid.setInputRange()
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {

    }
    //this line was entered by touch screen technology !
    /**
     * This function is called periodically during operator control
     */
    public void teleopInit() {
        tick = 0;
    }
    
    public void disabledInit() {
        tick = 0;
    }
       
    int desiredSpeed = 0, curMaxSpeed = 26315;
    double desiredPct = .2;
    boolean highGear = true, changeGears = false;

    public void teleopPeriodic() {
        boolean setPID = true;
        boolean setSpeed = true;
        
        if (stick.getButtonToggle(1)) {
            highGear = !highGear;
            changeGears = true;
        }
        
        if (stick.getButtonToggle(5)) {
            desiredPct += .1;
            System.out.println("desiredPct: " + desiredPct);
            setSpeed = true;
        }
        
        if (stick.getButtonToggle(3)) {
            desiredPct -= .1;
            System.out.println("desiredPct: " + desiredPct);
            setSpeed = true;
        }
        
        if (stick.getButtonToggle(7)) {
            countKp += 1;
            setPID = true;           
        }

        if (stick.getButtonToggle(9)) {
            countKi += 1;
            setPID = true;
        }

        if (stick.getButtonToggle(11)) {
            countKd += 1;
            setPID = true;
        }

        if (stick.getButtonToggle(8)) {
            countKp -= 1;
            setPID = true;
        }

        if (stick.getButtonToggle(10)) {
            countKi -= 1;
            setPID = true;
        }

        if (stick.getButtonToggle(12)) {
            countKd -= 1;
            setPID = true;
        }
        
        if (changeGears) {
            if (highGear) {
                setPID = true;
                setSpeed = true;
                curMaxSpeed = 26315;
            } else {
                setPID = true;
                setSpeed = true;
                curMaxSpeed = 11904;
            }
            changeGears = false;    
        }
/*        
        if (stick.getRawButton(6)) {
            pid.setInputRange(-curMaxSpeed, curMaxSpeed);
            Kp = countKp * .00001;
            Ki = countKi * .00001;
            Kd = countKd * .00001;
            pid.setPID(Kp, Ki, Kd);
            pid.setSetpoint(desiredPct * curMaxSpeed);
            pid.enable();
        } else {
            pid.disable();
        }
*/
        desiredPct = stick.getY();
        pid.setInputRange(-curMaxSpeed, curMaxSpeed);
        Kp = countKp * .00001;
        Ki = countKi * .00001;
        Kd = countKd * .00001;
        pid.setPID(Kp, Ki, Kd);
        pid.setSetpoint(desiredPct * curMaxSpeed);
        pid.enable();

        if (++tick >=25) {
            try {
                System.out.println("highGear: " + highGear + " SP: " + pid.getSetpoint() + " PV: " + enc.pidGet() + " Kp: " + countKp + " Ki: " + countKi + " Kd: " + countKd + 
                        " desiredPct: " + desiredPct + " curMaxSpeed: " + curMaxSpeed);
            } catch (Exception e) {
                System.out.println("Exception when getting jag1 speed");
            }
            tick = 0;
        }
        
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    
    }
    
}

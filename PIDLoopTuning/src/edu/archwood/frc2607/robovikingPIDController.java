/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.archwood.frc2607;

import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PIDSource;
import java.util.TimerTask;

/**
 *
 * @author rossron
 */
public class robovikingPIDController extends edu.wpi.first.wpilibj.PIDController {
    private volatile int step;
    private volatile double currentCO, stepChangePct;
    private long lastStepChangeTime;
    
    /* log:
     *      current timestamp (ms)
     *      PV (PIDSource.pidGet())
     *      MV (i.e. CO, whatever we're currently sending PIDOutput.pidWrite())
     *      timestamp (ms) that we triggered step change
     */
    private class LoggingTask extends TimerTask {
        
    }
    
    public robovikingPIDController(double Kp, double Ki, double Kd, PIDSource pidIn, PIDOutput pidOut) {
        super(Kp, Ki, Kd, pidIn, pidOut);
        step = 0;
        lastStepChangeTime = 0;
    }
    
    public void stepUp() {
        ++step;
    }
    
    public void stepDown() {
        --step;
    }
    
    public void startOpenLoop() {
                
    }
    
    public void stopOpenLoop() {
        
    }
}

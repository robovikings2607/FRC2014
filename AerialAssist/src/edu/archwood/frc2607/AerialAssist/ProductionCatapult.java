/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.archwood.frc2607.AerialAssist;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Victor;

/**
 *
 * @author frcdev
 */
public class ProductionCatapult extends Thread implements CompBotConstants{
    
    Solenoid chaputpult;
    Victor chaputpultWinder;
    DigitalInput chaputpultReadySwitch;
    boolean isShooting;
    boolean windTheCatapult;
    boolean fire;
    
    public ProductionCatapult(){
        chaputpult = new Solenoid(solenoidLauncher);
        chaputpultWinder = new Victor(pwmChaputpultWinder);
        chaputpultReadySwitch = new DigitalInput(diLauncherSwitch);
    }
    
    public boolean isCatapultShootable(){
        return isSwitchTriggered() && !isShooting;
    }
    

    public void fire(boolean override){
        if (isCatapultShootable() || (override && !isShooting)){
            fire = true;
        }
    }
    
    public void run(){
        while (true){
            if (windTheCatapult && !isSwitchTriggered()){
                chaputpultWinder.set(-1.00);
            }
            else {
                chaputpultWinder.set(0);        
            }
            
            if (fire){
                isShooting = true;
                
                shoot();
                
                fire = false;
            }
            
        }
    }
    
    private void shoot(){
            chaputpult.set(true);
            try { Thread.sleep(1000); } catch (Exception e) {}
            chaputpult.set(false);
    }
    
    private boolean isSwitchTriggered(){
        return !chaputpultReadySwitch.get();
    }
    
    public void windTheChaputpult(boolean winding){
        windTheCatapult = (winding && !isSwitchTriggered());
    }

}

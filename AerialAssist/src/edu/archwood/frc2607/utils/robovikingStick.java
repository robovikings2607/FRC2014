/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.archwood.frc2607.utils;

/**
 *
 * @author rossron
 */
import edu.wpi.first.wpilibj.Joystick;

public class robovikingStick extends Joystick {
    
    private int previousState;
    
    public robovikingStick(int port) {
        super(port);
        previousState = 0;
    }
    
    public boolean getButtonToggle(int buttonNumber) {
        int bitValue = 0x1 << (buttonNumber - 1);
        boolean retValue = false;
        
        boolean buttonWasOff = (bitValue & previousState) == 0;
        boolean buttonIsOn = getRawButton(buttonNumber);
        
        if (buttonWasOff && buttonIsOn) retValue = true;
        if (buttonIsOn) previousState = previousState | bitValue;
        if (!buttonIsOn) previousState = previousState & ~bitValue;
        
        return retValue;
    }
}

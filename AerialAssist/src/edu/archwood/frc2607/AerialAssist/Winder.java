/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.archwood.frc2607.AerialAssist;

import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Victor;

/**
 *
 * @author frcdev
 */
public class Winder {
    Relay relay;
    Victor victor;
    boolean compBot = false;
    public Winder(Relay mine)
    {
        relay = mine;
        compBot = false;
    }
    public Winder(Victor mine)
    {
        victor = mine;
        compBot = true;
    }
    
    public void stop()
    {
        if (compBot)
        {
            victor.set(0);
        }
        else
        {
            relay.set(Relay.Value.kOff);
        }
    }
    public void windSlowly()
    {
        if (compBot)
        {
            victor.set(-0.3);
        }
        else
        {
            relay.set(Relay.Value.kForward);
        }
    }
    public void wind()
    {
        if (compBot)
        {
            victor.set(-1);
        }
        else
        {
            relay.set(Relay.Value.kForward);
        }
    }
}

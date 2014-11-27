/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.archwood.frc2607.AerialAssist;

import edu.wpi.first.wpilibj.DigitalOutput;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author frcdev
 */
public class LEDManager extends TimerTask implements CompBotConstants{
    public static final byte DEFAULT = 1, POLICE = 2, MERICAMODE = 3, IRISH = 4;
    private static final int _delay = 200;
    private final DigitalOutput dO;
    private static int _mode = 5;
    public Timer timer;
    public LEDManager()
    {
        dO = new DigitalOutput(digitalOutputLED);
        setupTimer();
        
    }
    public void changePattern(int newMode)
    {
        _mode = newMode;
        timer.schedule(this, 0);
    }
    private void setupTimer()
    {
        Thread me = new Thread(this);
        me.start();
       // timer = new Timer();
       // timer.scheduleAtFixedRate(this, 0, 10000);
    }
    boolean isRunning = false;
    public void run() {
        while (true)
        try {
            if (isRunning) return;
            isRunning = true;
            System.out.println("LEDS!");
            for (int i = 0; i < _mode; i ++)
            {
                dO.set(true);
                Thread.sleep(_mode*_delay);
                dO.set(false);
                Thread.sleep(_mode*_delay);
            }
            isRunning = false;
        } catch (InterruptedException ex) {
            isRunning = false;
        }
    }
}

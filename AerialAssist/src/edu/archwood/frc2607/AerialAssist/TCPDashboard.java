/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.archwood.frc2607.AerialAssist;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

/**
 *
 * @author frcdev
 */
public class TCPDashboard implements Runnable{
    private SocketConnection conn;
    private DataOutputStream out;
    private DataInputStream in;

    public int getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage(int warningMessage) {
        this.warningMessage = warningMessage;
        if (warningMessage!=0)
        {
            System.out.println(warningMessage);
        }
    }
    boolean catState, rollerState, shifterState, alliance, leftHot, rightHot;
    int warningMessage = 0;
    double leftSONAR, rightSONAR;

    public double getLeftSONAR() {
        return leftSONAR;
    }

    public void setLeftSONAR(double leftSONAR) {
        this.leftSONAR = leftSONAR;
    }

    public double getRightSONAR() {
        return rightSONAR;
    }

    public void setRightSONAR(double rightSONAR) {
        this.rightSONAR = rightSONAR;
    }
    public boolean isAlliance() {
        return alliance;
    }

    public void setAlliance(boolean alliance) {
        this.alliance = alliance;
    }

    public boolean isShifterState() {
        return shifterState;
    }

    public void setShifterState(boolean shifterState) {
        this.shifterState = shifterState;
    }
    public void start()
    {
        Thread me = new Thread(this);
        me.start();
    }
    public boolean areBothHot() {
        return leftHot && rightHot;
    }
    public boolean isLeftHot() {
        return leftHot;
    }
    public boolean isRightHot() {
        return rightHot;
    }
    public boolean isCatState() {
        return catState;
    }

    public void setCatState(boolean catState) {
        this.catState = catState;
    }

    public boolean isRollerState() {
        return rollerState;
    }

    public void setRollerState(boolean rollerState) {
        this.rollerState = rollerState;
    }
    public void run() {
        boolean error = true;
        while (error)
        {
            try 
            {
                conn = (SocketConnection)Connector.open("socket://10.26.7.5:1180",Connector.READ_WRITE,false);
                in = conn.openDataInputStream();
                out = conn.openDataOutputStream();
                error = false;
            } 
            catch (IOException ex){System.out.println(ex.getMessage());}
            try {Thread.sleep(100);} catch (InterruptedException ex) {}
        }
        while (true)
        {
            try 
            {
                out.writeBoolean(!catState);
                out.writeBoolean(rollerState);
                out.writeBoolean(shifterState);
                out.writeBoolean(alliance);
                //out.writeDouble(leftSONAR);
                //out.writeDouble(rightSONAR);
                out.write(0);
                leftHot = in.readBoolean();
                rightHot = in.readBoolean();
                Thread.sleep(50);
            } 
            catch (InterruptedException ie){}
            catch (IOException ex) 
            {
                try 
                {
                    conn = (SocketConnection)Connector.open("socket://10.26.7.5:1180",Connector.READ_WRITE,false);
                    in = conn.openDataInputStream();
                    out = conn.openDataOutputStream();
                }
                catch (IOException e){}
           }
           
        }
    }
    
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package therobotdrawing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author John
 */
public class Yay extends JPanel implements Runnable{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        JFrame swag = new JFrame();
        swag.setContentPane(new Yay());
        swag.setAutoRequestFocus(false);
        swag.toBack();
        swag.setMinimumSize(new Dimension(660,500));
        swag.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        swag.setVisible(true);
    }
    
    private final String warnings[];
    private final int lengths[];
    BufferedImage warning[] = new BufferedImage[2], frame, bumper[] = new BufferedImage[2], roller[] = new BufferedImage[2], catapult[] = new BufferedImage[2], shifter[] = new BufferedImage[2];
    BufferedImage visionBuf;
    private visionTrackerFTW imWatchingYou = new visionTrackerFTW();
    
    public Yay()
    {
        this.warnings = new String[]
        {
            "ALL SYSTEMS NOMINAL",
            "Jaguar 1 not communicating",
            "Jaguar 2 not communicating",
            "Jaguar 3 not communicating",
            "Jaguar 4 not communicating",
            "Jaguar 5 not communicating",
            "Jaguar 6 not communicating",
            "Jaguar 7 not communicating",
            "Jaguar 8 not communicating",
            "Jaguar 9 not communicating",
        };
        Graphics2D gdd = (Graphics2D) this.getGraphics();
        this.lengths = new int[]
        {
            32,
            24,
            24,
            24,
            24,
            24,
            24,
            24,
            24,
            24,
            24,
            24
        };
        loadPictures();
        Thread ne = new Thread(this);
        ne.start();
        Thread yyr = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    try{
                    Thread.sleep(10);}catch(InterruptedException e){}
                    repaint();
                }
            }
        });
        yyr.start();
        Thread iwy = new Thread(imWatchingYou);
        iwy.start();
    }
    /**
     *
     * @param g
     */
    double catAngle = -0.0;
    double rolAngle = 2.14;
    Polygon rollers, redFire, yellowFire, orangeFire;
    int shifted = 1, cocked = 0, rolled = 0, bluealliance = 0, warningSignal = 1;
    boolean leftHot = false, rightHot = false;
    int flipped = 0;
    int xx = 1000, yy = 500;
    @Override
    public void paint(Graphics g)
    {
        Graphics2D g2d = (Graphics2D)g;
        
        g2d.setColor(Color.white);
        g2d.fillRect(0,0,getWidth(),getHeight());
        g2d.drawImage(catapult[cocked], xx, yy, null);
        g2d.drawImage(roller[rolled], xx, yy, null);
        g2d.drawImage(frame, xx, yy, null);
        g2d.drawImage(shifter[shifted], xx, yy, null);
        g2d.drawImage(bumper[bluealliance], xx, yy, null);
        if (warningSignal!=0)
        {
            g2d.drawImage(warning[flipped>500?1:0], xx+104, yy+200, null);
            if ((flipped+=30)>1000)
            {
                flipped = 0;
            }
            g2d.setColor(Color.red);
        }
        else
        {
            g2d.setColor(Color.green);
        }
        g2d.drawString(warnings[warningSignal], xx+104+lengths[warningSignal], yy+300);
        visionBuf = imWatchingYou.getVisionImage();
        if (visionBuf != null) g2d.drawImage(visionBuf, 5, 5, null);
    }

    @Override
    public void run() {
        ServerSocket ss;
        try 
        {
            ss = new ServerSocket(1180);
            while (true)
            {
                try
                {
                    Socket connect = ss.accept();
                    DataInputStream dis = new DataInputStream(connect.getInputStream());
                    DataOutputStream dos = new DataOutputStream(connect.getOutputStream());
                    while (true)
                    {
                        cocked = dis.readBoolean()?0:1;
                        rolled = dis.readBoolean()?0:1;
                        shifted = dis.readBoolean()?1:0;
                        bluealliance = dis.readBoolean()?0:1;
                        warningSignal = dis.read();
                        dos.writeBoolean(imWatchingYou.isLeftHot());
                        dos.writeBoolean(imWatchingYou.isRightHot());
                        repaint();

                        Thread.sleep(100);
                    }
                } 
                catch (IOException ex) {} catch (InterruptedException ex) {
                }
            }
        } 
        catch (IOException ex) {}
    } 
    
    
    
    public final void loadPictures()
    {
        rollers = new Polygon();
        rollers.addPoint(6, -16);
        rollers.addPoint(6, 60);
        rollers.addPoint(-40, 80);
        rollers.addPoint(-60, 80);
        rollers.addPoint(-60, 72);
        rollers.addPoint(-40, 72);
        rollers.addPoint(-6, 58);
        rollers.addPoint(-6, -16);
        redFire = new Polygon();
        redFire.addPoint(-28,0);
        redFire.addPoint(-40,-70);
        redFire.addPoint(-35,-40);
        redFire.addPoint(-27,-80);
        redFire.addPoint(-19,-35);
        redFire.addPoint(-10,-60);
        redFire.addPoint(0,-40);
        redFire.addPoint(0,-40);
        redFire.addPoint(10,-60);
        redFire.addPoint(19,-35);
        redFire.addPoint(27,-80);
        redFire.addPoint(35,-40);
        redFire.addPoint(40,-70);
        redFire.addPoint(29,0);
        redFire.translate(0, 10);
        frame = new BufferedImage(300,300,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = frame.createGraphics();
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(140,100,126,28);
        g2d.setColor(Color.black);
        g2d.drawRect(140,100,126,28);
        bumper[0] = new BufferedImage(300,300,BufferedImage.TYPE_INT_ARGB);
        g2d = bumper[0].createGraphics();
        g2d.setColor(Color.red);
        g2d.fillRect(128,128,150,24);
        g2d.setColor(Color.black);
        g2d.drawRect(128,128,150,24);
        
        bumper[1] = new BufferedImage(300,300,BufferedImage.TYPE_INT_ARGB);
        g2d = bumper[1].createGraphics();
        g2d.setColor(Color.blue);
        g2d.fillRect(128,128,150,24);
        g2d.setColor(Color.black);
        g2d.drawRect(128,128,150,24);
        
        shifter[0] = new BufferedImage(300,300,BufferedImage.TYPE_INT_ARGB);
        g2d = shifter[0].createGraphics();
        g2d.setColor(Color.red);
        g2d.translate(160, 138);
        g2d.fillPolygon(redFire);
        g2d.translate(86, 0);
        g2d.fillPolygon(redFire);
        g2d.translate(-246, -138);
        g2d.setColor(Color.orange);
        g2d.scale(.8, .8);
        g2d.translate(200, 168);
        g2d.fillPolygon(redFire);
        g2d.translate(108, 0);
        g2d.fillPolygon(redFire);
        g2d.translate(-308, -168);
        g2d.scale(1.25, 1.25);
        g2d.setColor(Color.yellow);
        g2d.scale(.5, .5);
        g2d.translate(320, 258);
        g2d.fillPolygon(redFire);
        g2d.translate(174, 0);
        g2d.fillPolygon(redFire);
        g2d.translate(-494, -258);
        g2d.scale(2, 2);
        
        g2d.setColor(Color.gray);
        g2d.fillArc(140,118,40,40,0,360);
        g2d.fillArc(226,118,40,40,0,360);
       
        g2d.setColor(Color.black);
        g2d.drawArc(140,118,40,40,0,360);
        g2d.drawArc(226,118,40,40,0,360);
        
        shifter[1] = new BufferedImage(300,300,BufferedImage.TYPE_INT_ARGB);
        g2d = shifter[1].createGraphics();
        g2d.setColor(Color.gray);
        g2d.fillArc(140,118,40,40,0,360);
        g2d.fillArc(226,118,40,40,0,360);
       
        g2d.setColor(Color.black);
        g2d.drawArc(140,118,40,40,0,360);
        g2d.drawArc(226,118,40,40,0,360);
        
        roller[0] = new BufferedImage(400,300,BufferedImage.TYPE_INT_ARGB);
        g2d = roller[0].createGraphics();
        rolAngle = 3.14;
        g2d.translate(250, 100);
        g2d.rotate(rolAngle);
        g2d.setColor(Color.green);
        g2d.fillArc(-66, 63, 24, 24, 0, 360);
        g2d.setColor(Color.black);
        g2d.drawArc(-66, 63, 24, 24, 0, 360);
        g2d.fillArc(-63, 66, 18, 18, 0, 360);
        g2d.setColor(Color.LIGHT_GRAY);
        //g2d.fillRect(0,0,16,50);
        g2d.fillPolygon(rollers);
        g2d.setColor(Color.black);
        //g2d.drawRect(0,0,16,50);
        g2d.drawPolygon(rollers);
        g2d.rotate(-rolAngle);
        g2d.translate(-250, -100);
        
        roller[1] = new BufferedImage(400,300,BufferedImage.TYPE_INT_ARGB);
        g2d = roller[1].createGraphics();
        rolAngle = 2.14;
        g2d.translate(250, 100);
        g2d.rotate(rolAngle);
        g2d.setColor(Color.green);
        g2d.fillArc(-66, 63, 24, 24, 0, 360);
        g2d.setColor(Color.black);
        g2d.drawArc(-66, 63, 24, 24, 0, 360);
        g2d.fillArc(-63, 66, 18, 18, 0, 360);
        g2d.setColor(Color.LIGHT_GRAY);
        //g2d.fillRect(0,0,16,50);
        g2d.fillPolygon(rollers);
        g2d.setColor(Color.black);
        //g2d.drawRect(0,0,16,50);
        g2d.drawPolygon(rollers);
        g2d.rotate(-rolAngle);
        g2d.translate(-250, -100);
        
        catapult[0] = new BufferedImage(300,300,BufferedImage.TYPE_INT_ARGB);
        g2d = catapult[0].createGraphics();
        catAngle = 0;
               g2d.translate(150, 98);
        g2d.rotate(catAngle);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0,0,120,4);
        g2d.fillRect(0,-60,4,60);
        g2d.setColor(Color.black);
        g2d.drawRect(0,0,120,4);
        g2d.drawRect(0,-60,4,60);
        g2d.fillRect(90,-16,4,17);
        g2d.fillRect(20,-32,4,33);
        g2d.rotate(-catAngle);
        g2d.translate(-150, -98);
        
        catapult[1] = new BufferedImage(300,300,BufferedImage.TYPE_INT_ARGB);
        g2d = catapult[1].createGraphics();
        catAngle = -1.2;
               g2d.translate(150, 98);
        g2d.rotate(catAngle);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0,0,120,4);
        g2d.fillRect(0,-60,4,60);
        g2d.setColor(Color.black);
        g2d.drawRect(0,0,120,4);
        g2d.drawRect(0,-60,4,60);
        g2d.fillRect(90,-16,4,17);
        g2d.fillRect(20,-32,4,33);
        g2d.rotate(-catAngle);
        g2d.translate(-150, -98);
        
        warning[0] = new BufferedImage(300,300,BufferedImage.TYPE_INT_ARGB);
        g2d = warning[0].createGraphics();
        g2d.setPaint(new GradientPaint(0,0,Color.red.brighter(),200,200,Color.red.darker()));
        g2d.fillRect(0, 0, 200, 16);
        g2d.setFont(new Font("Arial",Font.BOLD,32));
        g2d.drawString("WARNING!", 22, 52);
        g2d.setPaint(new GradientPaint(0,0,Color.yellow.brighter(),200,200,Color.yellow.darker()));
        g2d.fillRect(0, 64, 200, 16);
        g2d.setColor(Color.black);
        g2d.drawRect(0, 0, 200, 16);
        g2d.drawRect(0, 64, 200, 16);
        
        
        warning[1] = new BufferedImage(300,300,BufferedImage.TYPE_INT_ARGB);
        g2d = warning[1].createGraphics();
        g2d.setPaint(new GradientPaint(0,0,Color.yellow.brighter(),200,200,Color.yellow.darker()));
        g2d.fillRect(0, 0, 200, 16);
        g2d.setPaint(new GradientPaint(0,0,Color.red.brighter(),200,200,Color.red.darker()));
        g2d.fillRect(0, 64, 200, 16);
        g2d.setFont(new Font("Arial",Font.BOLD,32));
        g2d.drawString("WARNING!", 22, 52);
        g2d.setColor(Color.black);
        g2d.drawRect(0, 0, 200, 16);
        g2d.drawRect(0, 64, 200, 16);
        
    }
}
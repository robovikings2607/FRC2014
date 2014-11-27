/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package therobotdrawing;

import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.IPCameraFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core;
import com.jhlabs.image.ContrastFilter;
import com.jhlabs.image.GammaFilter;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author rossron
 */
public class visionTrackerFTW implements Runnable {
    private final String propFileName = "driverStationVision.properties";
    private String testFileName, savedFramePath;
    private boolean hotLeft, hotRight, calibMode, saveFrames;
    private int targetCheckCount, savedFrameCount, width, height,
                lowBlue, lowGreen, lowRed, highBlue, highGreen, highRed,
                targetSize;
    private float gamma, contrast, brightness;
    private ContrastFilter contrastFilter;
    private GammaFilter gammaFilter;
    private BufferedImage bufSourceImage, bufDisplayImage;
    private Rect leftTarget, rightTarget;
    private Scalar lowRange, highRange;
    private DecimalFormat floatFmt, intFmt;
    private Properties props;
    private boolean useSavedImages = false, pausePlayback = true;
    
    private Object mutex = new Object(), 
                   imageMutex = new Object();
    
    private JSlider lowBlueSlider, lowGreenSlider, lowRedSlider,
                    highBlueSlider, highGreenSlider, highRedSlider,
                    gammaSlider, contrastSlider, brightnessSlider, targetSizeSlider;
    private JFrame filteredFrame, binFrame;
    private ImageIcon filteredIcon, binIcon; 
    
    private class savedImageStreamer extends Thread {
        private volatile opencv_core.IplImage img;
        private ArrayList<Path> il1 = new ArrayList(), 
                                il2 = new ArrayList(),
                                il3 = new ArrayList(),
                                il4 = new ArrayList();
        
        private opencv_core.IplImage grab() {
            return img;
        }
        
        public savedImageStreamer() {
            try {           
                for (Path p : Files.newDirectoryStream(FileSystems.getDefault().getPath("./replayImages"), "*.?.jpg"))
                    il1.add(p);
                for (Path p : Files.newDirectoryStream(FileSystems.getDefault().getPath("./replayImages"), "*.??.jpg"))
                    il2.add(p);
                for (Path p : Files.newDirectoryStream(FileSystems.getDefault().getPath("./replayImages"), "*.???.jpg"))
                    il3.add(p);
                for (Path p : Files.newDirectoryStream(FileSystems.getDefault().getPath("./replayImages"), "*.????.jpg"))
                    il4.add(p);
                Collections.sort(il1);
                Collections.sort(il2);
                Collections.sort(il3);
                Collections.sort(il4);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        private void imgReadLoop(Iterator<Path> i) {
            img = com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage(i.next().toString());
            while (i.hasNext()) {
                if (!pausePlayback) img = com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage(i.next().toString());
                try { Thread.sleep(45); } catch (Exception e) {}
            }
        }
        
        public void run() {
            while (true) {
                Iterator<Path> it1 = il1.iterator(), 
                               it2 = il2.iterator(), 
                               it3 = il3.iterator(),
                               it4 = il4.iterator();
                if (it1.hasNext()) imgReadLoop(it1);
                if (it2.hasNext()) imgReadLoop(it2);
                if (it3.hasNext()) imgReadLoop(it3);
                if (it4.hasNext()) imgReadLoop(it4);
            }            
        }
    }
    
    private class imageSaver extends Thread {
        
        private BufferedImage imageToSave;
        private int imageCount;
        private String imageInfo;
        
        public imageSaver(BufferedImage image, String imageType, int count) {
            imageToSave = image;
            imageCount = count;
            imageInfo = imageType;
        }
        
        public void run() {
            StringBuffer sb = new StringBuffer(savedFramePath);
            sb.append(File.separatorChar).append(imageInfo).append(".");
            sb.append(imageCount).append(".jpg");
            try {
                ImageIO.write(imageToSave, "jpg", new File(sb.toString()));
            } catch (Exception e) {}
        }
    }

    private class calibrationMode implements ActionListener {
        private JFrame minBGRFrame, maxBGRFrame, exposureFrame, targetSizeFrame;
        
        public void actionPerformed(ActionEvent e) {
                if (!calibMode) {
                    setupCalibMode();
                    ((JButton)e.getSource()).setText("Calib Off");
                    calibMode = true;
                } else {
                    filteredFrame.dispose();
                    binFrame.dispose();
                    minBGRFrame.dispose();
                    maxBGRFrame.dispose();
                    exposureFrame.dispose();
                    targetSizeFrame.dispose();
                    try {
                        File propFile = new File(propFileName);
                        if (propFile.exists()) {
                            propFile.renameTo(new File(propFileName + System.currentTimeMillis()));
                        }
                        props.setProperty("gamma", floatFmt.format(gamma));
                        props.setProperty("contrast", floatFmt.format(contrast));
                        props.setProperty("brightness", floatFmt.format(brightness));
                        props.setProperty("lowBlue", intFmt.format(lowBlue));
                        props.setProperty("lowGreen", intFmt.format(lowGreen));
                        props.setProperty("lowRed", intFmt.format(lowRed));
                        props.setProperty("highBlue", intFmt.format(highBlue));
                        props.setProperty("highGreen", intFmt.format(highGreen));
                        props.setProperty("highRed", intFmt.format(highRed));
                        props.setProperty("targetSize",intFmt.format(targetSize));
                        props.store(new FileOutputStream(propFileName), null);
                    } catch (Exception ex) {
                        
                    }
                    ((JButton)e.getSource()).setText("Calib On");
                    calibMode = false;
                }            
        }
        
        private void setupCalibMode() {
                filteredFrame = new JFrame("filteredImage");
                binFrame = new JFrame("binImage");
                filteredFrame.setSize(width, height);
                binFrame.setSize(width, height);
                JLabel filteredLabel = new JLabel();
                JLabel binLabel = new JLabel();
                filteredIcon = new ImageIcon();
                binIcon = new ImageIcon();
                filteredLabel.setIcon(filteredIcon);
                binLabel.setIcon(binIcon);
                filteredFrame.getContentPane().add(filteredLabel);
                binFrame.getContentPane().add(binLabel);
                createSliders();
                filteredFrame.setVisible(true);
                binFrame.setVisible(true);            
        }

        private void createSliders() {
            minBGRFrame = new JFrame("minBGR");
            minBGRFrame.setLayout(new FlowLayout());
            lowBlueSlider =  new JSlider(JSlider.VERTICAL, 0, 255, lowBlue);
            lowGreenSlider = new JSlider(JSlider.VERTICAL, 0, 255, lowGreen);
            lowRedSlider = new JSlider(JSlider.VERTICAL, 0, 255, lowRed);
            minBGRFrame.add(lowBlueSlider);
            minBGRFrame.add(lowGreenSlider);
            minBGRFrame.add(lowRedSlider);
            minBGRFrame.pack();
            minBGRFrame.setVisible(true);
            maxBGRFrame = new JFrame("maxBGR");
            maxBGRFrame.setLayout(new FlowLayout());
            highBlueSlider = new JSlider(JSlider.VERTICAL, 0, 255, highBlue);
            highGreenSlider = new JSlider(JSlider.VERTICAL, 0, 255, highGreen);
            highRedSlider = new JSlider(JSlider.VERTICAL, 0, 255, highRed);
            maxBGRFrame.add(highBlueSlider);
            maxBGRFrame.add(highGreenSlider);
            maxBGRFrame.add(highRedSlider);
            maxBGRFrame.pack();
            maxBGRFrame.setVisible(true);
            targetSizeFrame = new JFrame("targetSize");
            targetSizeFrame.setLayout(new FlowLayout());
            targetSizeSlider = new JSlider(JSlider.HORIZONTAL, 5, 40, targetSize);
            targetSizeFrame.add(targetSizeSlider);
            targetSizeFrame.pack();
            targetSizeFrame.setVisible(true);
            exposureFrame = new JFrame("Gamma, Contrast, Brightness");
            exposureFrame.setLayout(new FlowLayout());
            int gammaInt = (int)(gamma * 100.0),
                contrastInt = (int)(contrast * 100.0),
                brightnessInt = (int)(brightness * 100.0);
            gammaSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, gammaInt);
            contrastSlider = new JSlider(JSlider.HORIZONTAL, 50, 400, contrastInt);
            brightnessSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, brightnessInt);
            exposureFrame.add(gammaSlider);
            exposureFrame.add(contrastSlider);
            exposureFrame.add(brightnessSlider);
            exposureFrame.pack();
            exposureFrame.setVisible(true);
        }    
    }    

    private final void initProperties() {
        props = new Properties();
        try {
            props.load(new FileInputStream(propFileName));
            gamma = Float.valueOf(props.getProperty("gamma"));
            contrast = Float.valueOf(props.getProperty("contrast"));
            brightness = Float.valueOf(props.getProperty("brightness"));
            lowBlue = Integer.valueOf(props.getProperty("lowBlue"));
            lowGreen = Integer.valueOf(props.getProperty("lowGreen"));
            lowRed = Integer.valueOf(props.getProperty("lowRed"));
            highBlue = Integer.valueOf(props.getProperty("highBlue"));
            highGreen = Integer.valueOf(props.getProperty("highGreen"));
            highRed = Integer.valueOf(props.getProperty("highRed"));
            targetSize = Integer.valueOf(props.getProperty("targetSize"));
        } catch (Exception e) {
            gamma = (float)0.4;
            contrast = (float)1.96;
            brightness = (float)0.98;
            lowBlue = 60;
            lowGreen = 60;
            lowRed = 0;
            highBlue = 255;
            highGreen = 255;
            highRed = 20;
            targetSize = 14;
        }
    }
    
    private void setupButtons() {
        JFrame btnFrame = new JFrame("Control Panel");
        btnFrame.setLayout(new FlowLayout());
        btnFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JButton btnSaveFrames = new JButton("Save Frames");
        btnSaveFrames.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (!saveFrames) {
                    ((JButton)ev.getSource()).setText("Stop Saving");
                    savedFramePath = "savedImages-" + System.currentTimeMillis();
                    new File(savedFramePath).mkdir();
                    saveFrames = true;
                } else {
                    ((JButton)ev.getSource()).setText("Save Frames");
                    saveFrames = false;
                }
            }
        }); 
        btnFrame.add(btnSaveFrames);
        
        JButton btnCalibMode = new JButton("Calib On");
        btnCalibMode.addActionListener(new calibrationMode());
        btnFrame.add(btnCalibMode);
        
        if (useSavedImages) {
            JButton btnPause = new JButton("Play");
            btnPause.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    pausePlayback = !pausePlayback;
                    if (pausePlayback) ((JButton)ae.getSource()).setText("Play");
                    else ((JButton)ae.getSource()).setText("Pause");
                }                
            });
            btnFrame.add(btnPause);
        }
        
        btnFrame.pack();
        btnFrame.setVisible(true);
    }
    
    public visionTrackerFTW() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        initProperties();
        if (System.getProperty("useFile") != null) {
            useSavedImages = true;
        }
        setupButtons();
        testFileName = "image4.jpg";
        floatFmt = new DecimalFormat("#.00");
        intFmt = new DecimalFormat("###");
        targetCheckCount = 0;
        savedFrameCount = 0;
        calibMode = false;
        saveFrames = false;
        width = 0;
        height = 0;
        contrastFilter = new ContrastFilter();
        gammaFilter = new GammaFilter();
        gammaFilter.setGamma(gamma);
        contrastFilter.setContrast(contrast);
        contrastFilter.setBrightness(brightness);
        leftTarget = null;
        rightTarget = null;
        lowRange = new Scalar(lowBlue,lowGreen,lowRed);
        highRange = new Scalar(highBlue,highGreen,highRed);        
    }
    
    public void run() {
        if (useSavedImages) {
            loadFromFile();
        } else {
            while (true) {
                loadFromJavaCV();
                try { Thread.sleep(1000); } catch (Exception e) {}
            }
        }
    }
    
    private void loadFromFile() {
        savedImageStreamer strm = new savedImageStreamer();
        strm.start();
        while (strm.grab() == null) try {Thread.sleep(5);} catch (Exception e) {}
        opencv_core.IplImage cameraFrame = strm.grab();
        bufSourceImage = cameraFrame.getBufferedImage();
        width = bufSourceImage.getWidth();
        height = bufSourceImage.getHeight();
        while (true) {
            findTargets();
            bufSourceImage = strm.grab().getBufferedImage();
        }
    }
    
    private void loadFromJavaCV() {
        FrameGrabber camGrabber = null;
        
        System.err.println("opening stream...");
        
        try {
            camGrabber = new IPCameraFrameGrabber("http://10.26.7.12/mjpg/video.mjpg");
        } catch (Exception e) {
            System.err.printf("OpenCVGrameGrabber ctor: %s\n", e.getMessage());
        }
        
        boolean keepTrying = true;
        while (keepTrying) {
            String stepName = "null";
            try {
                stepName = "camGrabber.start()";
                camGrabber.start();
                stepName = "camGrabber.grab()";
                camGrabber.grab();
                keepTrying = false;
            } catch (Exception e) {
                System.err.printf("%s: %s ... retrying... \n", stepName, e.getMessage());               
                try { Thread.sleep(1500); } catch (Exception e1) {}
            }
        }        
        
        try {
            System.err.println("starting capture");
            opencv_core.IplImage img = camGrabber.grab();
            bufSourceImage = img.getBufferedImage();
            width = bufSourceImage.getWidth();
            height = bufSourceImage.getHeight();
            while(true) {
                findTargets();
                img = camGrabber.grab();
                bufSourceImage = img.getBufferedImage();        
            }
        } catch (Exception e) {
            System.out.printf("camGrabber.grab(): %s\n", e.getMessage());
            try { Thread.sleep(1500); } catch (Exception e1) {}
        }

        try {
            camGrabber.stop();
            camGrabber.release();
        } catch (Exception e) {}   
    }

    public BufferedImage getVisionImage() {
        synchronized(imageMutex) {
            return bufDisplayImage;
        }
    }
    
    public boolean isLeftHot() {
        synchronized(mutex) {
            return hotLeft;
        }
    }
    
    public boolean isRightHot() {
        synchronized(mutex) {
            return hotRight;
        }
    }
    
    private void findTargets() {
        leftTarget = null;
        rightTarget = null;
        
        if (calibMode) {
            lowRed = lowRedSlider.getValue();
            lowGreen = lowGreenSlider.getValue();
            lowBlue = lowBlueSlider.getValue();
            highRed = highRedSlider.getValue();
            highGreen = highGreenSlider.getValue();
            highBlue =  highBlueSlider.getValue();
            lowRange = new Scalar(lowBlue, lowGreen, lowRed);
            highRange = new Scalar(highBlue, highGreen, highRed);
            targetSize = targetSizeSlider.getValue();
            gamma = gammaSlider.getValue() / (float)100.0;
            contrast = contrastSlider.getValue() / (float)100.0;
            brightness = brightnessSlider.getValue() / (float)100.0;
            gammaFilter.setGamma(gamma);
            contrastFilter.setContrast(contrast);
            contrastFilter.setBrightness(brightness);
        }
        
        if (saveFrames) {
            new imageSaver(bufSourceImage, "bufSourceImage", savedFrameCount).start();
        }

        // filter the source image
        ColorModel cm = bufSourceImage.getColorModel();
        BufferedImage filteredImage = new BufferedImage(cm, bufSourceImage.copyData(null), cm.isAlphaPremultiplied(), null);
        contrastFilter.filter(filteredImage, filteredImage);
        gammaFilter.filter(filteredImage, filteredImage);
        
        Mat img = bufToMat(filteredImage);       
        Mat bin = new Mat(img.size(), img.type());
        Core.inRange(img, lowRange, highRange, bin);
        
        if (calibMode) {
            Graphics2D fg = filteredImage.createGraphics();
            BufferedImage binImage = matToBuf(bin);
            Graphics2D bg = binImage.createGraphics();
            fg.setStroke(new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));                        
            fg.setFont(new Font("Arial", Font.PLAIN, 16));
            fg.setColor(Color.RED);
            StringBuilder sb = new StringBuilder("G: ");
            sb.append(floatFmt.format(gamma)).append(" C: ");
            sb.append(floatFmt.format(contrast)).append(" B: ");
            sb.append(floatFmt.format(brightness));
            fg.drawString(sb.toString(), 10, 30);
            bg.setStroke(new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));                        
            bg.setFont(new Font("Arial", Font.PLAIN, 16));
            bg.setColor(Color.WHITE);
            sb.delete(0,sb.length());
            sb.append("Min BGR: (");
            sb.append(intFmt.format(lowBlue)).append(",").append(intFmt.format(lowGreen)).append(",");
            sb.append(intFmt.format(lowRed)).append(")");
            bg.drawString(sb.toString(), 10, 35);
            sb.delete(0,sb.length());
            sb.append("Max BGR: (").append(intFmt.format(highBlue)).append(",");
            sb.append(intFmt.format(highGreen)).append(",").append(intFmt.format(highRed));
            sb.append(")");
            bg.drawString(sb.toString(), 10, 50);
            sb.delete(0,sb.length());
            sb.append("Size: ").append(intFmt.format(targetSize));
            bg.drawString(sb.toString(), 190, 35);
            filteredIcon.setImage(filteredImage);
            binIcon.setImage(binImage);
            if (saveFrames) {
                new imageSaver(filteredImage, "filteredImage", savedFrameCount).start();
                new imageSaver(binImage, "binImage", savedFrameCount).start();
            }
        }

        ArrayList<MatOfPoint> contours = new ArrayList();
        Imgproc.findContours(bin, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        
        // sort contours into horizontal rectangles and vertical rectangles; eliminate everything else
        ArrayList<Rect> horizRects = new ArrayList();
        ArrayList<Rect> vertRects = new ArrayList();
        for (MatOfPoint mp : contours) {
            Rect r = Imgproc.boundingRect(mp);
            int diff = r.width - r.height;
            if (diff > targetSize) horizRects.add(r);               // 20 for 640x480 frames; 14 for 320x240
            else if (diff < -targetSize) vertRects.add(r);
        }
        
        synchronized(mutex) {
            hotLeft = false;
            hotRight = false;
            for (Rect r : horizRects) {
                Rect r2 = closestVertRect(r, vertRects);
                if (r2 == null) {                               // also check if closest is "too far away" to be a target
                    continue;
                }
                if (r2.x > r.x) { // vertRect is on the right, ergo horizRect is left
                    hotLeft = true;
                    int vertRight = r2.x + r2.width;
                    int vertBottom = r2.y + r2.height;
                    leftTarget = new Rect(r.x, r.y, vertRight - r.x, vertBottom - r.y);
                } else if (r2.x < r.x) {
                    hotRight = true;
                    int horizRight = r.x + r.width;
                    int vertBottom = r2.y + r2.height;
                    rightTarget = new Rect(r2.x, r.y, horizRight - r2.x, vertBottom - r.y);
                }
            }
        }
        
        Graphics2D origGraphics = bufSourceImage.createGraphics();
        origGraphics.setStroke(new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));                        
        origGraphics.setFont(new Font("Arial", Font.PLAIN, 24));
            if (hotLeft) {
                origGraphics.setColor(Color.PINK);
                origGraphics.drawString("Hot Left", 10, 40);
                origGraphics.drawString(floatFmt.format(calcDistance(leftTarget, bin.size().width)), 10, 70);
                origGraphics.setColor(Color.RED);
                origGraphics.drawRect(leftTarget.x, leftTarget.y, leftTarget.width, leftTarget.height);
            }
        
            if (hotRight) {
                origGraphics.setBackground(Color.PINK);
                origGraphics.drawString("Hot Right", 150, 40);
                origGraphics.drawString(floatFmt.format(calcDistance(rightTarget, bin.size().width)), 150, 70);
                origGraphics.setColor(Color.RED);
                origGraphics.drawRect(rightTarget.x, rightTarget.y, rightTarget.width, rightTarget.height);
            }        
        
        synchronized (imageMutex) {
            bufDisplayImage = new BufferedImage(cm, bufSourceImage.copyData(null), cm.isAlphaPremultiplied(), null);
        }    
        
        if (saveFrames) {
            new imageSaver(bufDisplayImage, "results", savedFrameCount).start();
            savedFrameCount += 1;
        }
        
        if (calibMode) {
            filteredFrame.repaint();
            binFrame.repaint();
        }
    }

    private double calcDistance(Rect target, double fovPixels) {
        double fovFt = 2.718333 * (fovPixels/target.width);     // 32.62in
        double distFt = (fovFt/2) / Math.tan(Math.toRadians(33.5));
        return distFt;
    }

    private Rect closestVertRect(Rect hRect, ArrayList<Rect> vRects) {
        Rect result = null; 
        int dist = 0;
        for (Rect r : vRects) {
            int tempDist = Math.abs(hRect.x - r.x) + Math.abs(hRect.y - r.y);
            if (dist == 0 || tempDist < dist) {
                dist = tempDist;
                result = r;
            }
        }
        return result;        
    }

    private BufferedImage matToBuf(Mat m) {
        MatOfByte mByte = new MatOfByte();
        Highgui.imencode(".jpg", m, mByte);
        try {
            return ImageIO.read(new ByteArrayInputStream(mByte.toArray()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }        
    }
        
    private Mat bufToMat(BufferedImage image) {
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, data);
        return mat;
    }
    

}

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.sound.sampled.*;
import javax.swing.*;
import org.jtransforms.fft.*;
import java.awt.image.*;
import javax.imageio.*;

public class Playlist extends JFrame {
    public static void main(String[] args) throws IOException {
        Playlist w = new Playlist("AndrewBotics Playlist");
    }
    Font DEFAULTFONT = new Font("Comic Sans Ms", Font.PLAIN, 15);
    ArrayList<Song> SONGS;
    ArrayList<Song> ACTIVESONGS = new ArrayList<>();

    int SONGINDEX = -1;
    // -6 to halve, +6 to double
    int SONGVOLUME = 0;
    Clip CURRENTSONG;
    long LASTPOSITION;

    JLabel IMAGELABEL;
    JLabel NOWPLAYING;
    JLabel VOLUMEMETER;

    JButton PLAY;
    JButton NEXTSONG;
    JButton PREVSONG;
    JButton RESHUFFLE;
    JCheckBox[] STIMBOXES;
    JButton DECVOLUME;
    JButton INCVOLUME;
    String[] STIMS;

    int IMAGESIZE = 225;
    byte[] TITLEBYTES;

    VisualizerPanel VISPANEL;
    javax.swing.Timer VISTIMER;

    Color GREEN = new Color(1, 169, 130);

    public Playlist(String name) throws IOException {
        super(name);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(450, 600);
        setLayout(null);

        populateSongs();
        //System.out.println(ACTIVESONGS);

        NOWPLAYING = new JLabel();
        NOWPLAYING.setFont(DEFAULTFONT);
        add(NOWPLAYING);

        IMAGELABEL = new JLabel();
        IMAGELABEL.setBounds(25, 50, IMAGESIZE, IMAGESIZE);
        add(IMAGELABEL);
        
        int BUTTONHEIGHT = 40;
        int BUTTONY = 290;
        PLAY = createButton("PAUSE", 100, BUTTONY, 75, BUTTONHEIGHT);
        PLAY.addActionListener(e -> pausePlay());

        NEXTSONG = createButton(">", 185, BUTTONY, 50, BUTTONHEIGHT);
        NEXTSONG.addActionListener(e -> nextSong());

        PREVSONG = createButton("<", 40, BUTTONY, 50, BUTTONHEIGHT);
        PREVSONG.addActionListener(e -> backSong());

        RESHUFFLE = createButton("RESHUFFLE", 282, 50, 125, BUTTONHEIGHT);
        RESHUFFLE.addActionListener(e -> shuffleAndStart());

        STIMBOXES = createCheckGroup(STIMS, 263, 100);

        DECVOLUME = createButton("-", 268, BUTTONY, 50, BUTTONHEIGHT);
        DECVOLUME.addActionListener(e -> decreaseVolume());

        INCVOLUME = createButton("+", 368, BUTTONY, 50, BUTTONHEIGHT);
        INCVOLUME.addActionListener(e -> increaseVolume());

        VOLUMEMETER = new JLabel("0");
        VOLUMEMETER.setFont(DEFAULTFONT);
        VOLUMEMETER.setBounds(325, BUTTONY, 50, BUTTONHEIGHT);
        add(VOLUMEMETER);
        
        VISPANEL = new VisualizerPanel();
        add(VISPANEL);
        VISTIMER = new javax.swing.Timer(10, e -> {
            if (CURRENTSONG != null && CURRENTSONG.isRunning()) {
                VISPANEL.repaint();
            }
        });
        VISTIMER.start();

        shuffleAndStart();

        setVisible(true);
    }
    JButton createButton(String text, int x, int y, int w, int h){
        JButton button = new JButton(text);
        add(button);
        button.setBounds(x, y, w, h);
        button.setForeground(Color.BLACK);
        button.setBackground(Color.WHITE);
        button.setFont(DEFAULTFONT);
        button.setBorder(BorderFactory.createLineBorder(GREEN, 5));
        button.setFocusPainted(false);
        button.setFocusable(false); 
        return button;
    }
    JCheckBox[] createCheckGroup(String[] options, int x, int y){
        JCheckBox[] boxes = new JCheckBox[options.length];
        for (int i = 0; i<options.length; i++){
            boxes[i] = createCheckBox(options[i], x, y+(i*25));
        }
        return boxes;
    }
    JCheckBox createCheckBox(String text, int x, int y){
        JCheckBox jcb = new JCheckBox(text);
        jcb.setBounds(x, y, 300, 30);
        jcb.setFont(DEFAULTFONT);
        jcb.setForeground(Color.BLACK);
        jcb.setFocusPainted(false);
        jcb.setFocusable(false); 
        add(jcb);
        return jcb;
    }
    void populateSongs() throws IOException{
        Scanner s = new Scanner(new File("database.in"));
        SONGS = new ArrayList<>();
        ArrayList<String> stims = new ArrayList<>();
        while (s.hasNextLine()){
            String[] data = s.nextLine().split(",");
            SONGS.add(new Song(data[0], data[1], data[2], data[3]));
            if (!stims.contains(SONGS.getLast().genre)) stims.add(SONGS.getLast().genre);
        }
        STIMS = new String[stims.size()];
        for (int i = 0; i<stims.size(); i++){
            STIMS[i] = stims.get(i);
        }
        s.close();
    }
    void shuffleAndStart(){
        ACTIVESONGS.clear();
        HashSet<String> validset = new HashSet<>();
        for (JCheckBox jcb : STIMBOXES) if (jcb.isSelected()) validset.add(jcb.getText());
        // System.out.println(validset);
        for (Song s : SONGS){
            if (validset.isEmpty() || validset.contains(s.genre)) ACTIVESONGS.add(s);
        }
        Collections.shuffle(ACTIVESONGS);
        if (CURRENTSONG != null){
            CURRENTSONG.stop();
            CURRENTSONG.close();
        }
        SONGINDEX = -1;
        playNextSong();
        PLAY.setText("PAUSE");
    }
    void nextSong(){
        CURRENTSONG.stop();
        CURRENTSONG.close();
        playNextSong();
        PLAY.setText("PAUSE");
    }
    void backSong(){
        CURRENTSONG.stop();
        CURRENTSONG.close();
        SONGINDEX = (SONGINDEX+ACTIVESONGS.size()-2)%ACTIVESONGS.size();
        playNextSong();
        PLAY.setText("PAUSE");
    }
    void pausePlay(){
        if (CURRENTSONG!=null) {
            if (CURRENTSONG.isRunning()) {
                LASTPOSITION = CURRENTSONG.getMicrosecondPosition();
                CURRENTSONG.stop();
                PLAY.setText("PLAY");
            }
            else {
                CURRENTSONG.setMicrosecondPosition(LASTPOSITION);
                CURRENTSONG.start();
                PLAY.setText("PAUSE");
            }
        }
    }
    void playNextSong() {
        try {
            SONGINDEX = (SONGINDEX+1)%ACTIVESONGS.size();
            CURRENTSONG = ACTIVESONGS.get(SONGINDEX).generateClip();
            setAudioBytes(ACTIVESONGS.get(SONGINDEX));
            CURRENTSONG.start();
            updateVolume();
            updateUI(ACTIVESONGS.get(SONGINDEX));
            CURRENTSONG.addLineListener(e->{
                if (e.getType()==LineEvent.Type.STOP && e.getFramePosition()==CURRENTSONG.getFrameLength()){
                    //System.out.println("FINISHED");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    nextSong();
                }
            });
        }
        catch (Exception e){
            System.out.println(Arrays.toString(AudioSystem.getAudioFileTypes()));
            e.printStackTrace();
        }
    }
    void increaseVolume(){
        SONGVOLUME = Math.min(6, SONGVOLUME+3);
        updateVolume();
    }
    void decreaseVolume(){
        SONGVOLUME = Math.max(-24, SONGVOLUME-3);
        updateVolume();
    }
    void updateVolume(){
        if (CURRENTSONG==null) return;

        if (CURRENTSONG.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) CURRENTSONG.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(SONGVOLUME);
            VOLUMEMETER.setText((SONGVOLUME>0?"+":"")+SONGVOLUME);
            
            double val = Math.pow(2, SONGVOLUME/6.0);
            VOLUMEMETER.setText(String.format("%.2fx", val));
        } else {
            System.out.println("Master control is not supported.");
        }
    }
    void updateUI(Song song) throws IOException{
        int TARGET_SIZE = 225;
        File imgFile = new File(song.getImagePath());
        BufferedImage image = ImageIO.read(imgFile);
        
        int iw = image.getWidth();
        int ih = image.getHeight();

        int size = Math.min(iw, ih);
        BufferedImage crop = image.getSubimage((iw-size)/2, (ih-size)/2, size, size);
        Image finalImage = crop.getScaledInstance(TARGET_SIZE, TARGET_SIZE, Image.SCALE_SMOOTH);

        IMAGELABEL.setIcon(new ImageIcon(finalImage));
        NOWPLAYING.setText("Now Playing: "+song.songName);
        NOWPLAYING.setBounds(10, 10, 500, 30);
        repaint();
        revalidate();
    }
    public void setAudioBytes(Song song) throws Exception {
        File curr = new File(song.getMediaPath());
        AudioInputStream ais = AudioSystem.getAudioInputStream(curr);
        TITLEBYTES = ais.readAllBytes();
        //System.out.println(TITLEBYTES.length);
        ais.close();
    }

    class VisualizerPanel extends JPanel {
        public VisualizerPanel() {
            setBounds(0, 350, 450, 200);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics G) {
            super.paintComponent(G);
            Graphics2D g = (Graphics2D) G;
            g.setColor(GREEN);
            g.setStroke(new BasicStroke(5.0f));
            
            if (TITLEBYTES == null || CURRENTSONG == null || !CURRENTSONG.isRunning()) return;
            long currentFrame = CURRENTSONG.getFramePosition();

            if (CURRENTSONG.getFrameLength()<=0) return;
            int currFrame = (int) ((960+currentFrame)*TITLEBYTES.length/CURRENTSONG.getFrameLength());
            if (currFrame%2==1) currFrame++;
            int nextAmt = 1024;
            //System.out.println(currentFrame + "/"+CURRENTTITLE.getFrameLength());
            // System.out.println(currFrame+"/"+TITLEBYTES.length);

            double[] samples = new double[nextAmt/4];
            for (int i = 0; i<nextAmt; i+=4){
                if (currFrame+i>=TITLEBYTES.length) continue;
                samples[i/4] = (short)((TITLEBYTES[currFrame+i+1] & 0xFF)<<8 | (TITLEBYTES[currFrame+i] & 0xFF))/32768.0;
            }

            //System.out.println(Arrays.toString(samples));

            DoubleFFT_1D fft = new DoubleFFT_1D(nextAmt/4);
            fft.realForward(samples);
            double[] mags = new double[nextAmt/8];
            for (int i = 0; i<nextAmt/4; i+=2){
                double re = samples[i];
                double im = samples[i + 1];
                double magnitude = Math.sqrt(re*re+im*im);
                mags[i/2] = magnitude;
            }
            //System.out.println(mags.length);
            //System.out.println(Arrays.toString(mags));
            //System.out.println();

            int margin = 20;
            int usableWidth = getWidth() - (margin * 2);
            int totalBars = 25;
            
            int spacePerBar = usableWidth / totalBars;
            int width = spacePerBar - 6; 
            
            int currX = margin;
            int currY = getHeight() - 5; 
            
            for (int i=1; i<=totalBars; i++) {
                double percentage = (double) i / totalBars;
                double curve = (percentage*0.2) + (Math.pow(percentage, 2.5)*0.8);
                int index = 2 + (int) (curve*(mags.length*0.5)); 
                
                if (index >= mags.length) index = mags.length-1;
                
                double mag = mags[index];
                if (index+1 < mags.length) {
                     mag = (mags[index] + mags[index+1])/2.0;
                }
            
                int apply = (int)(120*Math.log10(mag+1));
                
                if (apply>0) g.drawRect(currX, currY-apply, width, apply);
                
                currX += spacePerBar; 
            }
        }
    }
}


import java.io.*;
import javax.sound.sampled.*;

public class Song {
    public String songName;
    public String mediaFile;
    public String imageFile;
    public String genre;
    public Clip clip;

    public Song(String songName, String mediaFile, String imageFile, String genre){
        this.songName = songName.trim();
        this.mediaFile = mediaFile.trim()+".wav";
        if (imageFile.trim().equals("INHERIT")) this.imageFile = mediaFile.trim()+".png";
        else this.imageFile = imageFile.trim()+".png";
        this.genre = genre.trim();
    }

    public Clip generateClip() throws Exception{
        File soundFile = new File("Songs\\"+mediaFile);
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
        clip = AudioSystem.getClip();
        clip.open(audioIn);
        return clip;
    }

    public String getImagePath() {
        return "Images\\"+imageFile;
    }
    public String getMediaPath() {
        return "Songs\\"+mediaFile;
    }
    public String toString(){
        return mediaFile;
    }
}
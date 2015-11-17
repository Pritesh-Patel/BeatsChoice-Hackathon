/**
* Created by Pritesh on 22/03/2015.
*/


package Services;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by AdilInsider on 21/03/2015.
 */
public class Mp3Helper {




    public static List<Music> proccesAllMusic()
    {
        ArrayList<Music> list = new ArrayList<Music>();
        File dir = new File("public");
        for( File f: dir.listFiles())
        {
            if(f.getPath().endsWith(".mp3")){
                Music m = getMeta(f);
                if(m != null)
                {
                    list.add(m);
                }

            }

        }

        return list;
    }



    public static Music getMeta(File f2){

        Music m = new Music();

        Mp3File song = null;
        try {
            song = new Mp3File(f2);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedTagException e) {
            e.printStackTrace();
        } catch (InvalidDataException e) {
            e.printStackTrace();
        }
        if (song.hasId3v2Tag()){
            ID3v2 id3v2tag = song.getId3v2Tag();
            byte[] imageData = id3v2tag.getAlbumImage();
            //converting the bytes to an image
            try {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));
                File f = new File("public/"+ f2.getName()+".png");
                m.imageFile = f.getName();
                ImageIO.write(img, "png", f);

            } catch (Exception e) {
                System.out.println("cannot get image");

            } finally {
                m.name =id3v2tag.getTitle();
                m.artist = id3v2tag.getArtist();
                m.mp3URL = f2.getName();
                if(m.imageFile==null){
                    m.imageFile = "download.jpg";
                }
            }


        }

        return m;


    }

}



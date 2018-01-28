package jp.picpie.scaviewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifThumbnailDirectory;

import java.io.File;
import java.io.IOException;

/**
 * Created by pie on 2017/04/01.
 */
public class BookPics {
    public static  Bitmap bmpLoad(String path,int width,int height){
        Bitmap bmp = null;
        BitmapFactory.Options opt = new BitmapFactory.Options();

        opt.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, opt);

        int scaleW = opt.outWidth / width + 1;
        int scaleH = opt.outHeight / height + 1;
        int scale = Math.max(scaleW, scaleH);

        opt.inJustDecodeBounds = false;
        opt.inSampleSize = scale;

        bmp = BitmapFactory.decodeFile(path, opt);

        return bmp;
    }

    public static Bitmap getThum( String picpath ){
        File jpegFile = new File( picpath );
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(jpegFile);
            ExifThumbnailDirectory etd = metadata.getDirectory(ExifThumbnailDirectory.class);
            if( etd == null) {
                return bmpLoad(picpath, 320, 240);
            }
            byte[] d = etd.getThumbnailData();
            Bitmap bmp = BitmapFactory.decodeByteArray(d,0,d.length);
            return bmp;
        } catch (ImageProcessingException e) {
            ThumbnailUtils tu = new ThumbnailUtils();
            Bitmap bmp = tu.createVideoThumbnail(picpath, 0);

            if(bmp!=null){
                return bmp;
            }
            return bmpLoad(picpath, 320, 240);
        } catch (IOException e) {
            return null;
        }
    }
}

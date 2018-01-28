package jp.picpie.scaviewer;

import android.graphics.RectF;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pie on 2017/03/19.
 */
public class BookPage {
    File file;
    String name;
    String annotation;
    String property;
    boolean focus;
    List<ViewArea>  pdivs;

    long getId(){
        return file.lastModified();
    }
    BookPage( File _file ){
        file = _file;
        name = file.getName();
        pdivs = new ArrayList<ViewArea>();
    }
    public File getFile(){
        return file;
    }

    public void setFocus( boolean _focus ){
        focus = _focus;
    }
    public boolean isFocussed(){
        return focus;
    }

    public String getName(){
        return name;
    }
    public void setName( String _name ){
        name = _name;
    }
    public String getAnnotation(){
        return annotation;
    }
    public void setAnnotation( String _annotation ){
        annotation = _annotation;
    }
    public String getProperty(){
        return property;
    }
    public void setProperty( String _property ){
        property = _property;
    }
}

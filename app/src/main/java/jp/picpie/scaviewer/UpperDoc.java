package jp.picpie.scaviewer;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.support.v4.util.SimpleArrayMap;
import android.util.ArrayMap;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by pie on 2017/03/20.
 */
public class UpperDoc {
    private static final int IDITEM = 0;
    private static final int TITLEITEM = 1;
    private static final int BODYITEM = 2;
    private static final int PARENTID = 3;
    private static final int SEQ = 4;
    private static final int CREATETIME = 5;
    private static final int MODIFYTIME = 6;

    private static final int MAXver = 3;
    private static final int CONSTREF = -1;

    private static SQLiteDatabase udDB;
    private static Context context;
    private static final String UDOCDB = UdHelper.DB_TABLE;
    private static final String ROOTCOND = "parentid=0";

    public UpperDoc( Context _context ){
        context = _context;
        udDB = new UdHelper( context ).getDB();
    }

    public int getFileID( String path ){
        Cursor cur = udDB.query(UDOCDB, null, "title='"+path+"'", null, null, null, "title, id desc",  "1");
        int count = cur.getCount();
        if( count == 0 ){
            return -1;
        }
        cur.moveToFirst();
        return cur.getInt(IDITEM);
    }
    public int setFileID( String path ){
        Cursor cur = udDB.query(UDOCDB, null, "title='"+path+"'", null, null, null, "title, id desc",  "1");
        int count = cur.getCount();
        if( count == 0 ){
            ContentValues insData = new ContentValues();
            insData.put("title", path);
            insData.put("parentid", 0);
            udDB.insert(UDOCDB, null, insData);
            cur = udDB.query(UDOCDB, null, "title='"+path+"'", null, null, null, "title, id desc", "1");
        }
        cur.moveToFirst();
        return cur.getInt(IDITEM);
    }
    public void setFileAnnotation( String fpath, String anno ){
        setItem(0,fpath,anno);
    }
    public int delFileID( String path ){
        Cursor cur = udDB.query(UDOCDB, null, "title='"+path+"'", null, null, null, "title, id desc",  "1");
        int count = cur.getCount();
        if( count == 0 ){
            ContentValues insData = new ContentValues();
            insData.put("title", path);
            insData.put("parentid", 0);
            udDB.insert(UDOCDB, null, insData);
            cur = udDB.query(UDOCDB, null, "title='"+path+"'", null, null, null, "title, id desc", "1");
        }
        cur.moveToFirst();
        return cur.getInt(IDITEM);
    }

    ///////// WebStrage ///////////////////////////////////////////////////////////////////////

    public String getItem(  int dirid, String key ){
        Cursor cur = udDB.query(UDOCDB, null, "title='"+key+"' and parentid="+String.valueOf(dirid), null, null, null, "title, id desc", "1");
        String rtv = "";
        if( cur.getCount()!=0 ){
            if(cur.moveToFirst()){
                rtv = cur.getString(BODYITEM);
            }
        }
        cur.close();
        return rtv;
    }

    public void setItem( int dirid, String title, String body ){
        ContentValues insData = new ContentValues();
        Cursor cur = udDB.query(UDOCDB, null, "title='"+title+"' and parentid="+String.valueOf(dirid), null, null, null, "title, id desc", "1");
        int id=0;
        if( cur.moveToFirst() ){
            id = cur.getInt(IDITEM);
        }
        insData.put("title", title);
        insData.put("body", body);
        insData.put("parentid", dirid);

        long ct = new Date().getTime();
        insData.put("mtime", ct);
        if( id > 0 ){
            udDB.update(UDOCDB, insData, "id=" + id, null);
        }else{
            insData.put("ctime", ct);
            udDB.insert(UDOCDB, null, insData);
        }
    }

    public SimpleArrayMap<String,String> listItem( int dirid ){
        Cursor cur = udDB.query(UDOCDB, null, "title not like '//%' and parentid="+String.valueOf(dirid), null, null, null, "title", null);
        SimpleArrayMap<String,String> map = new SimpleArrayMap<String,String>();
        cur.moveToFirst();
        for(int n=0; n < cur.getCount(); ++n ){
            map.put(cur.getString(TITLEITEM), cur.getString(BODYITEM));
            cur.moveToNext();
        }
        return map;
    }
    public List<UdocItem> listAllItem(  ){
        Cursor cur = udDB.query(UDOCDB, null, null, null, null, null, "id", null);
        ArrayList<UdocItem> map = new ArrayList<UdocItem>();
        cur.moveToFirst();
        for(int n=0; n < cur.getCount(); ++n ){
            map.add(new UdocItem(cur.getInt(IDITEM), cur.getString(PARENTID) + ":" + cur.getString(TITLEITEM), cur.getString(BODYITEM)));
            cur.moveToNext();
        }
        return map;
    }

    public List<UdocItem> searchItem( int dirid, String tstr ){
        Cursor cur = udDB.query(UDOCDB, null, "body like '%"+tstr+"%' and parentid="+String.valueOf(dirid), null, null, null, "title, id desc");
        ArrayList<UdocItem> map = new ArrayList<UdocItem>();
        cur.moveToFirst();
        for(int n=0; n < cur.getCount(); ++n ){
            map.add(new UdocItem(cur.getInt(IDITEM), cur.getString(PARENTID) + ":" + cur.getString(TITLEITEM), cur.getString(BODYITEM)));
            cur.moveToNext();
        }
        return map;
    }

    public void removeItem( int dirid, String key ){
        udDB.delete(UDOCDB, "title='" + key + "' and parentid="+String.valueOf(dirid), null);
    }
    public void removeItemById( int id ){
        udDB.delete(UDOCDB, "id="+String.valueOf(id), null);
    }
    public void removeAllItem( int dirid ){
        udDB.delete(UDOCDB, "parentid="+String.valueOf(dirid), null);
    }

    ///////////////////////////////////////////////////////////////////////

    public String getConst( String key ){
        return getItem( CONSTREF, key );
    }
    public void setConst( String key, String value ){
        setItem( CONSTREF, key, value );
    }

    public void clear(){
        udDB.execSQL("DELETE FROM "+UDOCDB+";");
    }

    //////////////////////////////////////////////////////////////////////////


}

class UdHelper extends SQLiteOpenHelper {
    public static final String TAG = "PCamroid";
    public final static String DB_TABLE = "udoc";
    private final static String DB_NAME = "udoc.db";
    private final static int DB_VERSION = 1;

    public UdHelper( Context context ){
        super(context, DB_NAME, null, DB_VERSION );
    }

    public SQLiteDatabase getDB(){
        try {
            return getWritableDatabase();
        }catch( SQLiteException e ){
            return getReadableDatabase();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE " + DB_TABLE +
                " (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, body TEXT, parentid INTEGER, seq TEXT, ctime INTEGER, mtime INTEGER )");
        ContentValues insData = new ContentValues();

        write( db, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath(), "", 0 );
        write( db, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath(), "", 0 );
        write( db, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath(), "", 0 );
    }
    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ){
    }

    void write( SQLiteDatabase db, String title, String body, int parentid ){
        ContentValues insData = new ContentValues();
        insData.put("title", title );
        insData.put("body", body );
        insData.put("parentid", parentid);
        db.insert(DB_TABLE, null, insData);
    }
}

class UdocItem {
    public int id;
    public String key;
    public String value;
    public boolean flag;
    UdocItem( int i, String k, String v ){
        id=i; key = k; value = v; flag = false;
    }
}
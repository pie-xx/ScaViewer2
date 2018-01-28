package jp.picpie.scaviewer;

import android.content.Context;
import android.os.Environment;
import android.support.v4.util.SimpleArrayMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Created by pie on 2017/03/19.
 */
public class BookFolder implements Iterable, Iterator<BookPage>  {
    static UpperDoc    mUDoc;
    final static String CurFolder = "CurFolder";       // 現在のホルダ
    final static String CurPage = "//CurPage";            // 現在見ているファイル (ホルダごとに設定)
    final static String CurArea = "//CurArea";            // 現在のエリア (ホルダごとに設定)
    final static String CurMark = "//CurMark";            // 現在のブックマーク (ホルダごとに設定)
    public static final String PropPreStr = "//prop:";
    final static int ROOTID = 0;

    static Stack<String> mHistory = new Stack<String>();

    static String mCurPath;
    static String mCurPage;

    BookFolder( Context cont){
        mUDoc = new UpperDoc( cont );
    }

    public void pushHistory( String path ){
        mHistory.push( path );
    }
    public String popHistory(){
        try {
            return mHistory.pop();
        }catch (EmptyStackException e){
            return null;
        }
    }

    public  int getBookID(String path){
        return mUDoc.getFileID(path);
    }


    public  int getCurBookID(){
        return getBookID(getCurFolderFullpath());
    }
    public  String getCurFolderFullpath(){
        if( mCurPath != null ){
            return mCurPath;
        }
        mCurPath = mUDoc.getConst(CurFolder);
        if( mCurPath.isEmpty() ){
            mCurPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return mCurPath;
    }
    public String getCurFname( ){
        return getFileName( getCurPageFullpath() );
    }

    public void setCurFullpath(String path){
        mCurPage = null;
        mCurPath = path;
        mUDoc.setConst(CurFolder, path);
    }

    public void setCurPageFullPath( String path ){
        mCurPage = path;
        mUDoc.setItem( getBookID( getParentDir( path ) ), CurPage, getFileName(path));
    }

    public  String getCurPageFullpath(){
        if( mCurPage != null ){
            if( mCurPage.indexOf("/")!=-1){
                return mCurPage;
            }
            return getCurFolderFullpath()+"/"+mCurPage;
        }
        mCurPage = mUDoc.getItem(getCurBookID(), CurPage );

        return getCurFolderFullpath()+"/"+mCurPage;
    }

    static class FileSort implements Comparator<File> {
        public int compare(File src, File target){
            int diff = src.getName().compareTo(target.getName());
            return diff;
        }
    }

    List<BookPage> mFileList;
    int mPos = -1;

    @Override
    public Iterator iterator() {
        String dpath = getCurFolderFullpath();
        File dir = new File( dpath );
        final File[] files = dir.listFiles();

        mFileList = new ArrayList<BookPage>();
        BookPage parentPage = new BookPage(new File( getCurParentDir() ));
        parentPage.setName("..");
        parentPage.setAnnotation( mUDoc.getItem( BookFolder.ROOTID, dpath ) );
        mFileList.add( parentPage );

        int bookid = mUDoc.getFileID(dpath);

        if(files!=null){
            Arrays.sort(files, new FileSort());
            for(int n=0; n < files.length; ++n){
                if(!files[n].isFile()){
                    BookPage bp = new BookPage(files[n]);
                    bp.setAnnotation( mUDoc.getItem( BookFolder.ROOTID, files[n].getPath()) );
                    mFileList.add( bp );
                }
            }
            for(int n=0; n < files.length; ++n){
                if(files[n].isFile()){
                    BookPage bp = new BookPage(files[n]);
                    bp.setAnnotation( mUDoc.getItem( bookid, files[n].getName()) );
                    bp.setProperty( getProperty(files[n].getName()) );
                    mFileList.add( bp );
                }
            }
        }
        mPos = 0;
        return this;
    }

    public  void saveAnnotation( String fname, String annotation ){
        String dir = getCurFolderFullpath();

        int bookid = getBookID(dir);
        if( bookid == -1 ){
            bookid = mUDoc.setFileID( dir );
        }
        if( annotation.isEmpty() ){
            mUDoc.removeItem( bookid, fname );
        }else {
            mUDoc.setItem( bookid, fname, annotation );
        }
    }
    public SimpleArrayMap<String,String> getListAnnotations(){
        int bookid = getCurBookID();
        if( bookid == -1 ){
            return null;
        }
        SimpleArrayMap<String,String> annos = mUDoc.listItem(bookid);
        return annos;
    }

    public  String getAnnotatiion( String fname ){
        return mUDoc.getItem( getCurBookID(), fname );
    }
    public  String getDirAnnotatiion(String path){
        return mUDoc.getItem(ROOTID, path);
    //    return mUDoc.getItem( getBookID(getParentDir(path)), getFileName( path ) );
    }
    public  void saveDirAnnotatiion(String path, String annotation) {
        mUDoc.setItem( ROOTID, path ,annotation );
        if( annotation.isEmpty() ){
            int bookid = mUDoc.getFileID( path );
            if(mUDoc.listItem(bookid).size() < 1 ) {
                mUDoc.removeItem(ROOTID, path);
            }
        }
    }
    public String getProperty( String fname ){
        return mUDoc.getItem( getCurBookID(), PropPreStr + fname );
    }

    public String getCurMark(){
        return mUDoc.getItem( getCurBookID(), CurMark );
    }
    public void setCurMark( String fname ){
        mUDoc.setItem( getCurBookID(), CurMark, fname );
    }

    //
    public  SimpleArrayMap<String, String> getListFile(){
        return mUDoc.listItem(ROOTID);
    }

    String getParentDir( String path ){
        int p = path.lastIndexOf("/");
        if( p > 0 ){
            return path.substring(0, p);
        }
        return "/";
    }
    String getCurParentDir(){
        return getParentDir(getCurFolderFullpath());
    }
    static public  String getFileName( String fullpath ) {
        int p = fullpath.lastIndexOf("/");
        if( p != -1 ){
            return fullpath.substring(p + 1);
        }
        return "/";
    }
    public  String getCurFolderName(){
        return getFileName(getCurFolderFullpath());
    }

    @Override
    public boolean hasNext() {
        return mPos < mFileList.size() ;
    }

    @Override
    public BookPage next() {
        return mFileList.get(mPos++);
    }

    @Override
    public void remove() {
        mFileList.clear();
    }
}

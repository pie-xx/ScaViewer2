package jp.picpie.scaviewer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.SimpleArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class PageListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    BookFolder  mBookFolder;

    ListView mListView;
    NavigationView mNavigationView;
    List<String> mAnnos;

    PageListAdapter  mAdapter;

    @Override
    protected void onResume() {
        super.onResume();

        String focussedName = mAdapter.getFocussedName();
        String curpageFullpath = mBookFolder.getCurPageFullpath();
        if( !focussedName.equals(curpageFullpath)){
            mAdapter.setFocus(mBookFolder.getCurPageFullpath());
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(mAdapter.getFocusNo());
        }else {
            mListView.setSelection(mAdapter.getFocusNo());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_list);

        mBookFolder = new BookFolder(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    //    setPageListTitle();
        setTitle(mBookFolder.getCurFolderName());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File mediaStorageDir = new File( mBookFolder.getCurFolderFullpath() );
                //カメラ画像を保存するディレクトリ
                if(!mediaStorageDir.exists()){
                    if(!mediaStorageDir.mkdirs())
                        return;
                }
                String timeStamp = new SimpleDateFormat(
                        "yyyy_MM_dd__HH_mm_ss").format(new Date());
                //画像作成日時を表すタイムスタンプ
                File mediaFile = new File(mediaStorageDir.getPath() + File.separator
                        + timeStamp + ".jpg");
                //カメラを起動して実際に保存される画像ファイル名
                Uri cameraImageUri = Uri.fromFile(mediaFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mediaFile));

                startActivityForResult(cameraIntent, 0);

         //       Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
           //             .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        setAnno2Menu();
        setFile2Page();
    }

    void setPageListTitle( ){
        String curfolderAnnotation = mBookFolder.getDirAnnotatiion(mBookFolder.getCurFolderFullpath());
        if( curfolderAnnotation!=null && !curfolderAnnotation.isEmpty() ){
            setTitle(curfolderAnnotation+" ("+mBookFolder.getCurFolderName()+")");
        }else {
            setTitle(mBookFolder.getCurFolderName());
        }
    }

    public void setAnno2Menu(){
        Menu navmenu = mNavigationView.getMenu();
        navmenu.removeGroup(0);

        SimpleArrayMap<String,String> snos = mBookFolder.getListAnnotations();
        if( snos == null ){
            return;
        }

        mAnnos = new ArrayList<String>();
        for( int n=0; n < snos.size(); ++ n ) {
            String key = snos.keyAt(n);
            mAnnos.add(key);
        }
        Collections.sort(mAnnos);
        for( int n=0; n < mAnnos.size(); ++n ){
            MenuItem mi = navmenu.add(Menu.NONE, n, n, snos.get(mAnnos.get(n)));
        }
    }

    void setFile2Page(){
        mListView = (ListView)findViewById(R.id.listView);
        ArrayList<BookPage> list = new ArrayList<BookPage>();
        mAdapter = new PageListAdapter(PageListActivity.this);
        mAdapter.setTweetList(list);
        mListView.setAdapter(mAdapter);

        int bookid = mBookFolder.getBookID(mBookFolder.getCurFolderName());
        for(Iterator<BookPage> i = mBookFolder.iterator(); i.hasNext();){
            BookPage bp = i.next();
            list.add(bp);
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BookPage bp = (BookPage) parent.getItemAtPosition(position);
                if (bp.getFile().isDirectory()) {
                    changeBook(bp.getFile().getPath());
                    return;
                }
                mBookFolder.setCurPageFullPath(bp.getFile().getPath());
                mAdapter.setFocus(position);
                mAdapter.notifyDataSetChanged();

                startActivity(PieViewFullscreenActivity.class);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, View view, int i, long l) {
                mListView.setSelection(i);
                BookPage bp = (BookPage) adapterView.getAdapter().getItem(i);
                editAnnotation(bp);
                return true; // このあとonItemClick()が呼ばれないようにするためtrueを返す
            }
        });

        mAdapter.setFocus(mBookFolder.getCurPageFullpath());
    }

    void editAnnotation( final BookPage bp ){
        //テキスト入力を受け付けるビューを作成します。
        final EditText editView = new EditText(PageListActivity.this);

        final String targetFile = bp.getName();
        String targetAnno = "";
        if (targetFile.equals("..")) {
            targetAnno = mBookFolder.getDirAnnotatiion(mBookFolder.getCurFolderFullpath());
        } else {
            if( bp.getFile().isDirectory()){
                targetAnno = mBookFolder.getDirAnnotatiion(bp.getFile().getPath());
            }else {
                targetAnno = mBookFolder.getAnnotatiion(targetFile);
            }
        }
        editView.setText(targetAnno);

        new AlertDialog.Builder(PageListActivity.this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(editView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (bp.getFile().isDirectory()) {
                            if (targetFile.equals("..")) {
                                mBookFolder.saveDirAnnotatiion(mBookFolder.getCurFolderFullpath(), editView.getText().toString());
                                //setPageListTitle();
                            } else {
                                mBookFolder.saveDirAnnotatiion(bp.getFile().getPath(), editView.getText().toString());
                                bp.setAnnotation(editView.getText().toString());
                            }
                        } else {
                            mBookFolder.saveAnnotation(bp.getName(), editView.getText().toString());
                            bp.setAnnotation(editView.getText().toString());
                        }
                        setAnno2Menu();
                        mAdapter.notifyDataSetChanged();
                    }
                })
                .show();

    }

    void changeBook( String bookpath ){
        mBookFolder.pushHistory( mBookFolder.getCurFolderFullpath() );
        mBookFolder.setCurFullpath(bookpath);
        startActivity(PageListActivity.class);
    }

    public String getExt( String fname ) {
        int p = fname.lastIndexOf(".");
        if( p == -1 )
            return "";
        return fname.substring(p+1);
    }
    String ext2ctype( String fname ){
        String exp = getExt(fname);
        if( exp.equalsIgnoreCase("jpg")|| exp.equalsIgnoreCase("png") ){
            return "image/*";
        }
        if( exp.equalsIgnoreCase("pdf") ){
            return "application/pdf";
        }
        if( exp.equalsIgnoreCase("html") ){
            return "text/html";
        }
        if( exp.equalsIgnoreCase("mp4") ){
            return "video/mp4";
        }
        if( exp.equalsIgnoreCase("mpg") ){
            return "video/mpeg";
        }
        if( exp.equalsIgnoreCase("flv") ){
            return "video/x-flv";
        }
        if( exp.equalsIgnoreCase("wmv") ){
            return "video/x-ms-wmv";
        }
        if( exp.equalsIgnoreCase("avi") ){
            return "video/avi";
        }
        if( exp.equalsIgnoreCase("mov") ){
            return "video/quicktime";
        }
        if( exp.equalsIgnoreCase("mp3") ){
            return "audio/mpeg";
        }
        return "text/plain";
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            String b4path = mBookFolder.popHistory();
            if( b4path != null ){
                mBookFolder.setCurFullpath(b4path);
                startActivity(PageListActivity.class);
                return;
            }
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.page_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_upper ){
            changeBook( mBookFolder.getCurParentDir() );
            return true;
        }
        if (id == R.id.action_top) {
            mListView.setSelection(0);
            return true;
        }
        if (id == R.id.action_middle) {
            int itemCount = mListView.getCount();
            mListView.setSelection(itemCount / 2);
            return true;
        }
        if (id == R.id.action_bottom) {
            int itemCount = mListView.getCount();
            mListView.setSelection(itemCount - 1);
            return true;
        }
        if (id == R.id.action_edit_title) {
            editAnnotation( (BookPage)mAdapter.getItem( 0 ) );
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_folder) {
            startActivity(FolderSettingActivity.class);
        } else if (id == R.id.nav_gallery) {
            startActivity(ScrollingActivity.class);
        } else if (id == R.id.nav_focussed) {
            mListView.setSelection(mAdapter.getFocusNo());
        } else if (id == R.id.nav_picview) {
            startActivity(DBdebugActivity.class);
        } else if (id == R.id.nav_bookmarked){
            mListView.setSelection(mAdapter.getFocusNo());
            String targetfile = mBookFolder.getCurMark();
            jumpToFname( targetfile );
        }else {
            /*
            Uri uri = Uri.parse("file://" + mBookFolder.getCurFolderFullpath() + "/" + mAnnos.get(id));
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.putExtra(Intent.EXTRA_TEXT, mBookFolder.getCurFolderFullpath() + "/" + mAnnos.get(id));
            intent.setDataAndType(uri, ext2ctype(mAnnos.get(id)));
            startActivity(intent);
            */
            String targetfile = mAnnos.get(id);
            jumpToFname( targetfile );
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void jumpToFname( String fname ){
        int seq = 0;
        for(Iterator<BookPage> i = mBookFolder.iterator(); i.hasNext();){
            BookPage bp = i.next();
            if( fname.equals(bp.getName()) ){
                mListView.setSelection(seq);
                break;
            }
            ++seq;
        }
    }

    void startActivity( Class classname ){
        Intent intent = new Intent(getApplicationContext(), classname );
        intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "PageList Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://jp.picpie.scaviewer/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "PageList Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://jp.picpie.scaviewer/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
/**
 * Created by pie on 2017/03/18.
 */
class PageListAdapter extends BaseAdapter {

    Context context;
    LayoutInflater layoutInflater = null;
    ArrayList<BookPage> pageList;
    int focuspos;

    public PageListAdapter(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.focuspos = -1;
    }

    public void setTweetList(ArrayList<BookPage> pList) {
        this.pageList = pList;
    }

    @Override
    public int getCount() {
        return pageList.size();
    }

    @Override
    public Object getItem(int position) {
        return pageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return pageList.get(position).getId();
    }

    public int getFocusNo(){
        return focuspos;
    }
    public void setFocus( int focusno ){
        focuspos = focusno;
    }
    public void setFocus( String focuspath ){
        String focusfile = BookFolder.getFileName( focuspath );
        for( int n=0; n < pageList.size(); ++n ){
            if( pageList.get(n).getName().equals( focusfile )){
                focuspos = n;
                break;
            }
        }
    }
    public String getFocussedName(){
        if( focuspos < 0 || focuspos >= pageList.size()){
            return "";
        }
        return pageList.get( focuspos ).getFile().getPath();
    }
    public boolean isFocused( int itemno ){
        return itemno==focuspos;
    }
    /*
        public void setEditable( boolean _editable ){
            editable =  _editable;
            notifyDataSetChanged();
        }
    */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.page_prop,parent,false);

        File pagefile = pageList.get(position).getFile();
        ImageView picview = (ImageView) convertView.findViewById(R.id.picture);
        ImageView pageicon = (ImageView) convertView.findViewById(R.id.pageIcon);
        ((TextView)convertView.findViewById(R.id.name)).setText( pageList.get(position).getName() );

        if( pagefile.isDirectory() ) {
            ((TextView) convertView.findViewById(R.id.property)).setVisibility(View.GONE);
            picview.setVisibility(View.GONE);
            pageicon.setVisibility(View.VISIBLE);
            ((TextView) convertView.findViewById(R.id.annotation)).setText(  pageList.get(position).getAnnotation() );
            if( pageList.get( position ).getName().equals("..") ) {
                pageicon.setVisibility(View.GONE);
                ((TextView)convertView.findViewById(R.id.name)).setVisibility(View.GONE);
            }
        }else{
            pageicon.setVisibility(View.GONE);
            picview.setVisibility(View.GONE);
            try {
                Bitmap bmp = BookPics.getThum(pageList.get(position).getFile().getPath());
                if( bmp != null ) {
                    picview.setImageBitmap( bmp );
                    picview.setVisibility(View.VISIBLE);
                }
            }catch (OutOfMemoryError e){
            }
        }

        if( position==focuspos ){
            convertView.setBackgroundColor(Color.LTGRAY);
        }else{
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        String ano = pageList.get(position).getAnnotation();
        if( ano != null ) {
            if( ano.isEmpty() ){
                ((TextView) convertView.findViewById(R.id.annotation)).setVisibility(View.GONE);
            }else {
                ((TextView) convertView.findViewById(R.id.annotation)).setText(ano);
            }
        }else{
            ((TextView) convertView.findViewById(R.id.annotation)).setVisibility(View.GONE);
        }
        String prop = pageList.get(position).getProperty();
        if( prop != null ) {
            if( prop.isEmpty() ){
                ((TextView) convertView.findViewById(R.id.property)).setVisibility(View.GONE);
            }else {
                ((TextView) convertView.findViewById(R.id.property)).setText("*");
                ((TextView) convertView.findViewById(R.id.property)).setVisibility(View.VISIBLE);
            }
        }else{
            ((TextView) convertView.findViewById(R.id.property)).setVisibility(View.GONE);
        }

        return convertView;
    }
}
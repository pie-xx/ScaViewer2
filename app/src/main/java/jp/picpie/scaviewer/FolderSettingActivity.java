package jp.picpie.scaviewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class FolderSettingActivity extends AppCompatActivity  {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    BookFolder  mBookFolder;
    private GoogleApiClient client;
    TextView mCurPathView;
    ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_view);

        mCurPathView = (TextView)findViewById(R.id.docpath);

        mBookFolder = new BookFolder(getApplicationContext());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String docpath = mBookFolder.getCurFolderFullpath();
        mCurPathView.setText(docpath);

        Button movetobtn = (Button) findViewById(R.id.movedirbtn);
        movetobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newpath = mCurPathView.getText().toString();
                moveTo(newpath);
            }
        });

        mListView = (ListView) findViewById(R.id.dirlist);
        SimpleArrayMap<String, String> dirlist = mBookFolder.getListFile();
        final DirListAdapter  adapter = new DirListAdapter(FolderSettingActivity.this, mBookFolder );
        adapter.setList(dirlist);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String newpath = (String) adapterView.getItemAtPosition(i);
                moveTo( newpath );
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fab.setVisibility(View.INVISIBLE);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void moveTo(String newpath){
        mBookFolder.setCurFullpath(newpath);
        startActivity( PageListActivity.class );
    }
    void startActivity( Class classname ){
        Intent intent = new Intent(getApplicationContext(), classname );
        intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.page_list, menu);
        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch( event.getKeyCode() ) {
            case KeyEvent.KEYCODE_ENTER:
                String newpath = mCurPathView.getText().toString();
                moveTo( newpath );
                return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "FolderSetting Page", // TODO: Define a title for the content shown.
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
                "FolderSetting Page", // TODO: Define a title for the content shown.
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
 * Created by pie on 2017/04/29.
 */
class  DirListAdapter  extends BaseAdapter {
    SimpleArrayMap<String, String> dirlist;
    Context context;
    BookFolder  mBookFolder;

    void addList( String dir ){
        dirlist.put( dir, "" );
        notifyDataSetChanged();
    }

    DirListAdapter( Context cont, BookFolder folder ){
        context = cont;
        mBookFolder = folder;
        dirlist = new SimpleArrayMap<String, String>();
    }

    void setList( SimpleArrayMap<String, String> list ){
        dirlist.clear();
        for( int n=0; n < list.size(); ++n ){
            String  keyfname = list.keyAt( n );
            String  annotation = list.get( keyfname );
            if( annotation!=null && !annotation.isEmpty() ) {
                dirlist.put( keyfname, annotation );
            }
        }
    }

    @Override
    public int getCount() {
        return dirlist.size();
    }

    @Override
    public Object getItem(int i) {
        return dirlist.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);;

        view = layoutInflater.inflate(R.layout.dirlist,viewGroup,false);
        TextView dirnameView = (TextView) view.findViewById(R.id.dir_name);

        final String dirname =  dirlist.keyAt(i);
        dirnameView.setText(dirname);

        TextView annoView = (TextView) view.findViewById(R.id.dir_annotation);
        annoView.setText( dirlist.get( dirname ) );

        View itemView = (View) view.findViewById(R.id.dir_item);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBookFolder.setCurFullpath(dirname);
                startActivity(PageListActivity.class);
            }
        });

        return view;
    }

    void startActivity( Class classname ){
        Intent intent = new Intent(context, classname );
        intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
/*
    public void delList( String path ){
        mBookFolder.delFolder(path);
        dirlist.remove(path);
        notifyDataSetChanged();
    }
*/
}


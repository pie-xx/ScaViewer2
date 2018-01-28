package jp.picpie.scaviewer;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DBdebugActivity extends AppCompatActivity {
    UpperDoc    mUDoc;
    ListView mListView;
    DBListAdapter   mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbdebug);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mUDoc = new UpperDoc( this );;

        mListView = (ListView) findViewById(R.id.dblist);
        mAdapter = new DBListAdapter( this );

        setList();

    }

    void setList(){
        List<UdocItem> dblist = mUDoc.listAllItem();
        mAdapter.setList(dblist);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getTitle().equals("Delete")){
            for( int n=0; n < mAdapter.getCount(); ++n ){
                if(mAdapter.getItem( n ).flag ) {
                    mUDoc.removeItemById(mAdapter.getItem(n).id);
                }
            }
            setList();
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // メニューの要素を追加して取得
        MenuItem actionItem = menu.add("Delete");
        // アイコンを設定
        actionItem.setIcon(android.R.drawable.ic_menu_delete);

        return true;
    }


}

class  DBListAdapter  extends BaseAdapter {
    Context mContext;
    List<UdocItem> dblist;

    DBListAdapter( Context context ){
        mContext = context;
        dblist = new ArrayList();
    }

    void setList( List<UdocItem> list ){
        dblist.clear();
        dblist.addAll(list);
    }

    @Override
    public int getCount() {
        return dblist.size();
    }

    @Override
    public UdocItem getItem(int i) {
        return dblist.get(i);
    }

    @Override
    public long getItemId(int i) {
        return dblist.get(i).id;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);;

        view = layoutInflater.inflate(R.layout.dbdebuglist,viewGroup,false);

        TextView idView = (TextView) view.findViewById(R.id.dbparent);
        TextView dbkeyView = (TextView) view.findViewById(R.id.dbkey);
        TextView dbvalueView = (TextView) view.findViewById(R.id.dbValue);
        final CheckBox    cb = (CheckBox) view.findViewById(R.id.checkBox);

        UdocItem item = dblist.get( i );

        idView.setText(String.format("%03d ", item.id));
        dbkeyView.setText(item.key);
        dbvalueView.setText(item.value);
        cb.setChecked(item.flag);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                dblist.get(i).flag = b;
            }
        });




        return view;
    }

}


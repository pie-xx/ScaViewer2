package jp.picpie.scaviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class PieViewFullscreenActivity extends AppCompatActivity {
//    public static final String PropPreStr = "//prop:";

    private static final boolean AUTO_HIDE = true;
    BookFolder mBookFolder;
    PieView mPieView;
    ContrastMenu    mContrastMenu;

    ArrayList<BookPage> mList = new ArrayList<BookPage>();
    int mFocusNo;

    float mEx, mEy;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "PieViewFullscreen Page", // TODO: Define a title for the content shown.
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
                "PieViewFullscreen Page", // TODO: Define a title for the content shown.
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

    enum FStat {NON, Fire, UP};
    FStat mFire;
    long mEvtime;

    public BookFolder getBookFolder() {
        return mBookFolder;
    }

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    //    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {

        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBookFolder = new BookFolder(getApplicationContext());
        setTitle(mBookFolder.getCurFolderName());
        mBookFolder = new BookFolder(getApplicationContext());
        String focussedfile = mBookFolder.getCurPageFullpath();
        int focusno = 0;
        for (Iterator<BookPage> i = mBookFolder.iterator(); i.hasNext(); ) {
            BookPage bp = i.next();
            mList.add(bp);
            if (bp.getFile().getPath().equals(focussedfile)) {
                mFocusNo = focusno;
            }
            ++focusno;
        }

        setContentView(R.layout.activity_pie_view_fullscreen);
        mVisible = true;

        Point dsize = new Point();
        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getSize(dsize);
        mPieView = new PieView(this, dsize.x, dsize.y);

        addContentView(mPieView, new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT));

        mEvtime = 0;
        mFire = FStat.UP;

        mPieView.loadFile(mBookFolder.getCurPageFullpath());

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        mContrastMenu = new ContrastMenu(this, dsize.x);
        mContrastMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContrastMenu.erase();
            }
        });
        mContrastMenu.setOnBlightnessBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        mPieView.setBrightness(progress2blightness(progress));
                        mPieView.invalidate();
                        return;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mPieView.saveProperty();
                    }
                }
        );
        mContrastMenu.setOnContrastBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        mPieView.setContrast(progress2contrast(progress));
                        mPieView.invalidate();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mPieView.saveProperty();
                    }
                }
        );

        addContentView(mContrastMenu.mBody, new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT));

        mAnimeCount=0;

        imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.ic_menu_black_24dp);
        imageView.setVisibility(View.INVISIBLE);
        addContentView(imageView, new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT));
    }
    // @param brightness -255..255 0 is default
    float progress2blightness(int progress){
        return 255f*(progress - 50)/50f;
    }
    // @param contrast 0..10 1 is default
    float progress2contrast(int progress){
        return (float)Math.pow(10.0, (float)(progress/50.0f) - 1.0f);
        //	return (float)Math.log10((float)progress)/50 +1;
    }

    @Override
    public void onPause() {
        super.onPause();
        mPieView.recycle();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Point dsize = new Point();
        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getSize(dsize);

        mPieView.setViewSize(dsize.x, dsize.y);
        mPieView.invalidate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            toggle();
        } else if (id == R.id.action_before) {
            before();
        } else if (id == R.id.action_next) {
            next();
        } else if (id == R.id.action_editarea) {
            if (mPieView.getVmode() == PieView.ViewMode.Normal) {
                mPieView.setVmode(PieView.ViewMode.EditArea);
            } else {
                mPieView.saveProperty();
                mPieView.setVmode(PieView.ViewMode.Normal);
            }
            getFragmentManager().invalidateOptionsMenu();

        } else if (id == R.id.action_add_area) {
            ViewArea va = mPieView.getViewAreaProc().getFocussedViewArea();
            if (va == null) {
                mPieView.getViewAreaProc().addViewArea(new RectF(0, 0, mPieView.getOrgWidth(), mPieView.getOrgHeight()));
            } else {
                mPieView.getViewAreaProc().addViewArea(va);
                mPieView.getViewAreaProc().getFocussedViewArea().move(200.0f, 200.0f);
            }
            saveToUndo();
            mPieView.invalidate();
        } else if (id == R.id.action_rm_area) {
            saveToUndo();
            mPieView.getViewAreaProc().delViewArea();
            mPieView.invalidate();
        } else if (id == R.id.action_undoarea) {
            undoArea();
        } else if (id == R.id.action_fit) {
            ViewArea va = mPieView.getViewAreaProc().getFocussedViewArea();
            RectF rect = va.getArea();
            mPieView.fitArea(rect.top, rect.left, rect.right, rect.bottom, va.mFitTo);
            mPieView.invalidate();
            Toast.makeText(this, "LoadScale = " + String.valueOf(mPieView.mLoadScale), Toast.LENGTH_LONG).show();
        }else if( id == R.id.action_pictureset){
            mContrastMenu.disp(mPieView.getBrightness(),mPieView.getContrast());
        }else if( id == R.id.action_bookmark){
            mBookFolder.setCurMark( mBookFolder.getCurFname() );
            Toast.makeText(this, "Bookmark " + mBookFolder.getCurFname(), Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }

    void before() {
        if (mPieView.before()) {
            mPieView.invalidate();
        } else {
            mFocusNo = mFocusNo - 1;
            if (mFocusNo < 0) {
                mFocusNo = 0;
            }
            mPieView.loadFile(mList.get(mFocusNo).getFile().getPath());
            mBookFolder.setCurPageFullPath(mList.get(mFocusNo).getFile().getPath());
            mPieView.mViewAreaProc.mTetFocus = mPieView.mViewAreaProc.mTA.size() - 1;
        }
        mPieView.fitFocussedArea(true);
    }

    void next() {
        if (mPieView.next()) {
            mPieView.invalidate();
        } else {
            mFocusNo = mFocusNo + 1;
            if (mFocusNo > mList.size() - 1) {
                mFocusNo = mList.size() - 1;
            }
            mPieView.loadFile(mList.get(mFocusNo).getFile().getPath());
            mBookFolder.setCurPageFullPath(mList.get(mFocusNo).getFile().getPath());
            mPieView.mViewAreaProc.mTetFocus = 0;
        }
        mPieView.fitFocussedArea(false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mPieView.getVmode() == PieView.ViewMode.EditArea) {
                    mPieView.setVmode(PieView.ViewMode.Normal);
                    getFragmentManager().invalidateOptionsMenu();
                } else {
                    finish();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                before();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                next();
                return true;
        }
        return false;
    }

    String mUndoBuffer = "";

    void saveToUndo() {
        mUndoBuffer = mPieView.getViewAreaProc().getProperty();
        mPieView.saveProperty();
    }

    void undoArea() {
        mPieView.getViewAreaProc().setProperty(mUndoBuffer);
        mPieView.invalidate();
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        /*
        // メニューの要素を追加して取得
        MenuItem actionItem = menu.add("Action Button Help Icon");
        // アイコンを設定
        actionItem.setIcon(android.R.drawable.ic_menu_help);

        // SHOW_AS_ACTION_ALWAYS:常に表示
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        */
        getMenuInflater().inflate(R.menu.menu_pic_view, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        show();
        menu.clear();
        if (mPieView.getVmode() == PieView.ViewMode.Normal) {
            setTitle(mBookFolder.getCurFolderName());
            getMenuInflater().inflate(R.menu.menu_pic_view, menu);
            setActionBarColor(R.color.background_material_dark);
        } else {
            setTitle("--Edit Area--");
            getMenuInflater().inflate(R.menu.menu_area_edit, menu);
            setActionBarColor(R.color.start_color);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    void setActionBarColor(int color) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            Drawable backgroundDrawable = getApplicationContext().getResources().getDrawable(color);
            actionBar.setBackgroundDrawable(backgroundDrawable);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    int mAnimeCount;
    Handler mHander = null;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        long curtime = event.getEventTime();
        float cX = event.getX();
        float cY = event.getY();
        float dist = (cX - mEx) * (cX - mEx) + (cY - mEy) * (cY - mEy);

        if (event.getPointerCount() > 1) {
            mFire = FStat.Fire;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (mFire == FStat.UP) {
                    mEx = event.getX();
                    mEy = event.getY();
                    mEvtime = curtime;
                    mFire = FStat.NON;

                    startAnimation();
                } else
                if (dist > 1000) {
                    clearAnimation();
                    mFire = FStat.Fire;
                }
                break;
            default:
                clearAnimation();
                mEvtime = 0;
                mFire = FStat.UP;
                imageView.setVisibility( View.INVISIBLE );
                break;
        }

        return false;
    }

    void startAnimation(){
        mAnimeCount = 0;
        if( mHander==null) {
            mHander = new Handler();
        }
        mHander.postDelayed(new Runnable() {
            @Override
            public void run() {
                if( mAnimeCount > 12 ){
                    toggle();
                    mFire = FStat.Fire;
                    clearAnimation();
                    return;
                }
        //        mPieView.setAnimeCount(mAnimeCount);
        //        mPieView.invalidate();
                mHander.postDelayed(this, 100);
                if( mAnimeCount== 0) {
                    startRotation();
                }
                mAnimeCount++;
            }
        }, 500);
        int top = (int)mEy - 200;
        if( mVisible ) {
            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                top = top - actionBar.getHeight();
            }
        }

        imageView.layout((int)mEx-100,top,(int)mEx+100,top+200);
    }

    void clearAnimation(){
        stopRotation();

        mAnimeCount = 0;
        mPieView.setAnimeCount(mAnimeCount);
        mPieView.invalidate();
        if( mHander!= null ) {
            mHander.removeCallbacksAndMessages(null);
            mHander = null;
        }
    }

    private ImageView imageView;
    private RotateAnimation rotate;

    private void startRotation() {

        // RotateAnimation(float fromDegrees, float toDegrees, int pivotXType, float pivotXValue, int pivotYType,float pivotYValue)
        rotate = new RotateAnimation(0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        // animation時間 msec
        rotate.setDuration(3000);
        // 繰り返し回数
        rotate.setRepeatCount(10);
        // animationが終わったら消去
        rotate.setFillAfter(false);


        //アニメーションの開始
        imageView.setVisibility(View.VISIBLE);
        imageView.startAnimation(rotate);

    }
    private void stopRotation(){
        if( rotate != null) {
            imageView.clearAnimation();
            rotate.cancel();
            rotate = null;
        }
        imageView.setVisibility(View.INVISIBLE);
    }
}

class ContrastMenu {
    Context		    mContext;
    LinearLayout mBody;

    LinearLayout    mTitleLayout;
    Button mSaveButton;
    Button			mCanButton;

    SeekBar mBlightnessBar;
    TextView mBlightnessText;
    LinearLayout    mBlightnessLayout;

    TextView        mContrastText;
    SeekBar         mContrastBar;
    LinearLayout    mContrastLayout;

    LinearLayout    mBarlLayout;
    LinearLayout    mPanellLayout;

    Button          mResetBtn;

    float mContrast=1.0f;	// 0..10 1is default.
    float mBrightness=0;	// -255..255 0 is default

    public ContrastMenu(Context context, int dwidth){
        mContext = context;
        int lsize = dwidth / 5;

        mBody = new LinearLayout(context);
        mBody.setOrientation(LinearLayout.VERTICAL);
        mBody.setTop(300);
        mBody.setMinimumHeight(40);
        mBody.setBottom(100);

// Titlebar
        mTitleLayout =  new LinearLayout(context);
        mTitleLayout.setGravity(Gravity.RIGHT);

//        mSaveButton = new Button(context);
//        mSaveButton.setText("✔");
//        mTitleLayout.addView(mSaveButton);
/*
        mCanButton = new Button(context);
        mCanButton.setBackgroundDrawable(context.getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel));
        mTitleLayout.addView(mCanButton);
        mCanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                erase();
            }
        });
*/
        mBarlLayout = new LinearLayout(context);
        mBarlLayout.setOrientation(LinearLayout.VERTICAL);

// Blightness
        mBlightnessLayout = new LinearLayout(context);
        mBlightnessText = new TextView(context);
        mBlightnessText.setText("Blightness");
        mBlightnessText.setTextColor(Color.WHITE);
        mBlightnessText.setWidth(lsize);
        mBlightnessBar = new SeekBar(context);

        mBlightnessLayout.addView(mBlightnessText);
        mBlightnessLayout.addView(mBlightnessBar,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

        mBarlLayout.addView(mBlightnessLayout,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

// Contrast
        mContrastLayout = new LinearLayout(context);

        mContrastBar = new SeekBar(context);
        mContrastBar.setMax(100);

        mContrastText = new TextView(context);
        mContrastText.setText("Contrast");
        mContrastText.setTextColor(Color.WHITE);
        mContrastText.setWidth(lsize);

        mContrastLayout.addView(mContrastText);
        mContrastLayout.addView(mContrastBar,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

        mBarlLayout.addView(mContrastLayout,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

        mResetBtn = new Button(context);
    //    mResetBtn.setBackgroundDrawable(context.getResources().getDrawable(android.R.drawable.ic_menu_revert));
        mResetBtn.setText("x");

        mPanellLayout = new LinearLayout(context);
        mPanellLayout.setBackgroundColor(Color.argb(128, 0, 0, 0));
        mPanellLayout.setPadding(4,8,4,4);

        mPanellLayout.addView(mResetBtn);

        mPanellLayout.addView(mBarlLayout,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

        mBody.addView(mTitleLayout,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
        mBody.addView(mPanellLayout,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));


        mBody.setVisibility(View.INVISIBLE);

    }
    public LinearLayout getLayout(){
        return mBody;
    }

    public void setOnClickListener(View.OnClickListener onclicked){
        mResetBtn.setOnClickListener(onclicked);
    }
    public void setOnContrastBarChangeListener(SeekBar.OnSeekBarChangeListener onclicked){
        mContrastBar.setOnSeekBarChangeListener(onclicked);
    }
    public void setOnBlightnessBarChangeListener(SeekBar.OnSeekBarChangeListener onclicked){
        mBlightnessBar.setOnSeekBarChangeListener(onclicked);
    }

    public void erase(){
        mBody.setVisibility(View.INVISIBLE);
    }

    public void disp(float blightness, float contrast) {
        mBlightnessBar.setProgress(blightness2progress(blightness));
        mContrastBar.setProgress(contrast2progress(contrast));

        mBody.setVisibility(View.VISIBLE);

    }
    // @param brightness -255..255 0 is default
    int blightness2progress(float blightness){
        return (int)(100 * ( blightness + 255 )/512);
    }
    // @param contrast 0..10 1 is default
    int contrast2progress(float contrast){
        return (int)((Math.log10(contrast)+1)*50);
        //    return (int)(Math.pow(10.0, (contrast-1))*50);
    }

    public boolean isAlive(){
        return mBody.isShown();
    }

}

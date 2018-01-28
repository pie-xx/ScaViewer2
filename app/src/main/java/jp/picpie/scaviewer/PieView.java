package jp.picpie.scaviewer;

/**
 * Created by pie on 2017/04/04.
 */

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PieView extends View {

    float mContrast=1.0f;	// 0..10 1is default.
    float mBrightness=0;	// -255..255 0 is default

    PieViewFullscreenActivity mPieViewFullscreenActivity;

    String	mSrcFilename;		// 表示中のファイル
    Bitmap	mOrgBmp = null;		// 元画像
    Bitmap	mViewBmp = null;	// 画面用
    int	mLoadScale;				// メモリ不足時は縮小して読み込む、その倍率

    Point mViewSize;	// 表示スクリーンの縦横サイズ
    float	mScale ;	// 表示倍率
    FPoint	mOrgPos;	// 表示窓の中心

    public enum ViewMode {Normal, EditArea};
    ViewMode mVmode;

    public ViewMode getVmode(){
        return mVmode;
    }
    public void setVmode(ViewMode vm ){
        mVmode = vm;
        invalidate();
    }

    Rect	mOrgCopyArea;	// コピー元領域　元画像
    Rect	mViewCopyArea;	// コピー先領域　表示

    ViewAreaProc mViewAreaProc;

    ViewAreaProc    getViewAreaProc(){
        return mViewAreaProc;
    }

    int mAnimeCount;
    ////////////////////////////////////////////////////////////////////////////

    public PieView(PieViewFullscreenActivity context, int width, int height) {
        super(context);
        mPieViewFullscreenActivity = context;
        init(width, height);
        mAnimeCount = 0;
    }
    void init(int width, int height){

        mOrgPos = new FPoint(0,0);

        mViewBmp = Bitmap.createBitmap( Math.max(width, height), Math.max(width, height), Bitmap.Config.ARGB_8888 );

        mOrgCopyArea = new Rect();
        mViewCopyArea = new Rect();

        mViewSize = new Point(width, height);

        mViewAreaProc = new ViewAreaProc( this );
        mVmode = ViewMode.Normal;
    }

    public void recycle(){
        if(mOrgBmp!=null){
            mOrgBmp.recycle();
        }
        mOrgBmp = null;
    }

    public void setViewSize(int width, int height){
        mViewSize = new Point(width, height);
//        mViewMenu.reSize(width, height);
//        mTetraEditMenu.setSize(width, height);
    }

    void fitArea(float top, float left, float right, float bottom, ViewArea.FitTo ft) {
        float width = right - left;
        float height = bottom - top ;

        mOrgPos.x = (right + left) / 2;
        mOrgPos.y = (top + bottom) / 2;

        float viewHeight = mViewSize.y;
        android.support.v7.app.ActionBar actionBar = mPieViewFullscreenActivity.getSupportActionBar();
        if( actionBar != null ){
            viewHeight = viewHeight - actionBar.getHeight();
        }

        switch( ft ){
            case Up:
                mScale = (float) (mViewSize.x / width);
                mOrgPos.y = top + viewHeight /( mScale * 2);
                break;
            case Left:
                mScale = (float) (viewHeight / height);
                mOrgPos.x = left + mViewSize.x /( mScale * 2);
                break;
            case Right:
                mScale = (float) (viewHeight / height);
                mOrgPos.x = right - mViewSize.x /( mScale * 2);
                break;
            case Down:
                mScale = (float) (mViewSize.x / width);
                mOrgPos.y = bottom - viewHeight /( mScale * 2);
                break;
            case Width:
                mScale = (float) (mViewSize.x / width);
                break;
            case Height:
                mScale = (float) (viewHeight / height);
                break;
            case Center:
            default:
                mScale = (float) (viewHeight / height);
                float tmpWidth = mScale * width;
                if((int)tmpWidth > mViewSize.x ){
                    mScale = (float) (mViewSize.x / width);
                }
        }
    }
    void setDefPos(){
        fitArea( 0, 0, mOrgBmp.getWidth(), mOrgBmp.getHeight(), ViewArea.FitTo.Center );
    }

    public boolean loadImageFile(String bmpfile){
        for( mLoadScale=1; mLoadScale < 16; mLoadScale=mLoadScale*2 ){
            try{
                recycle();
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inSampleSize = mLoadScale;
                mOrgBmp = BitmapFactory.decodeFile(bmpfile, opt);
                if( mOrgBmp!=null ) {
                    invalidate();
                    return true;
                }
            }catch(java.lang.OutOfMemoryError e){
                recycle();
            }
        }
        return false;
    }

    public Bitmap getBitmap(){
        return mOrgBmp;
    }
    public int getOrgWidth(){
        if(mOrgBmp==null)
            return 0;
        return mOrgBmp.getWidth();
    }
    public int getOrgHeight(){
        if(mOrgBmp==null)
            return 0;
        return mOrgBmp.getHeight();
    }

    public void clearOrgBitmap(){
        if(mOrgBmp != null){
            mOrgBmp.recycle();
        }
        if(mViewBmp != null ){
            mViewBmp.recycle();
        }
    }
    public boolean isRecycled(){
        return mOrgBmp.isRecycled() || mOrgBmp==null;
    }
    public void drawBitmap( Bitmap bmp ) {
        mOrgBmp = bmp.copy(bmp.getConfig(), false);
    }

    @Override
    public void onDraw(Canvas canvas){
        //	super.onDraw(canvas);
        if( mOrgBmp == null ){
            return;
        }

        drawBackScreen(canvas);
        drawCopyArea(canvas);
        if( mVmode == ViewMode.EditArea ) {
            mViewAreaProc.showAllViewArea(canvas);
        }
        if( mAnimeCount > 0 ){
            showLongClickCounter( canvas );
        }
    }
    public void setAnimeCount( int count ){
        mAnimeCount = count;
    }
    void showLongClickCounter(Canvas canvas){
        Paint tpaint = new Paint();
        tpaint.setColor(Color.RED);
        tpaint.setTextSize(28);
        canvas.drawCircle((float) mCurTouch.x, (float) mCurTouch.y, (float) mAnimeCount*10.0f, tpaint);
    }

    public void setContrast(float contrast){
        mContrast=contrast;	// 0..10 1is default.
        mViewAreaProc.setContrast( contrast );
    }
    public float getContrast(){
        return mContrast;
    }
    public void setBrightness(float brightness){
        mBrightness=brightness;	// -255..255 0 is default
        mViewAreaProc.setBrightness( brightness );
    }
    public float getBrightness(){
        return mBrightness;
    }

    void drawBackScreen(Canvas canvas){
        mViewCopyArea.top = 0;
        mViewCopyArea.left = 0;
        mViewCopyArea.right = (int) mViewSize.x;
        mViewCopyArea.bottom = (int) (mViewSize.y);

        Paint paint = new Paint();
        paint.setColor(Color.DKGRAY);

        canvas.drawRect(mViewCopyArea, paint);
    }

    void drawCopyArea(Canvas canvas){
        calcCopyArea();
        // @param contrast 0..10 1 is default
        // @param brightness -255..255 0 is default
        Paint paint = null;
        paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(new float[]
                        {
                                mContrast, 0, 0, 0, mBrightness,
                                0, mContrast, 0, 0, mBrightness,
                                0, 0, mContrast, 0, mBrightness,
                                0, 0, 0, 1, 0
                        }
                )
        );
        canvas.drawBitmap(mOrgBmp, mOrgCopyArea, mViewCopyArea, paint);
    }

    void calcCopyArea(){
        int tmpOrgWidth = (int) (mViewSize.x / mScale);
        int tmpOrgHeight = (int) (mViewSize.y / mScale);

        mOrgCopyArea.left = (int)mOrgPos.x - tmpOrgWidth / 2;
        mOrgCopyArea.top = (int)mOrgPos.y - tmpOrgHeight /2;
        mOrgCopyArea.right = mOrgCopyArea.left + tmpOrgWidth;
        mOrgCopyArea.bottom = mOrgCopyArea.top + tmpOrgHeight;

        if(mOrgCopyArea.left < 0){
            mViewCopyArea.left = (int) ((-mOrgCopyArea.left) * mScale);
            mOrgCopyArea.left = 0;
        }

        if( mOrgCopyArea.right > mOrgBmp.getWidth() ){
            mViewCopyArea.right = (int) (mViewSize.x - (mOrgCopyArea.right - mOrgBmp.getWidth())*mScale);
            mOrgCopyArea.right = mOrgBmp.getWidth();
        }

        if(mOrgCopyArea.top < 0){
            mViewCopyArea.top = (int) ((-mOrgCopyArea.top) * mScale);
            mOrgCopyArea.top = 0;
        }

        if( mOrgCopyArea.bottom > mOrgBmp.getHeight() ){
            mViewCopyArea.bottom = (int) (mViewSize.y - (mOrgCopyArea.bottom - mOrgBmp.getHeight())*mScale);
            mOrgCopyArea.bottom = mOrgBmp.getHeight();
        }

    }

    float view2orgX(float x){
        return (float) ((x - mViewSize.x/2) / mScale + mOrgPos.x);
    }
    float view2orgY(float y){
        return (float) ((y - mViewSize.y/2) / mScale + mOrgPos.y);
    }

    float	org2viewX(float x){
        return (float) (( x - mOrgPos.x )* mScale + mViewSize.x / 2 );
    }
    float	org2viewY(float y){
        return (float) (( y - mOrgPos.y )* mScale + mViewSize.y / 2 );
    }

///////////////////////////////////////////////////////////////////////

    enum TMODE {T_None, T_Ext, T_Scroll, MI_Ap0, MI_Ap1, MI_Ap2, MI_Ap3, MI_Ln0, MI_Ln1, MI_Ln2, MI_Ln3, MI_Rect  }
    TMODE	mTmode = TMODE.T_None;

    FPoint	mTouch = new FPoint();      // タッチ開始の座標
    FPoint	mCurTouch = new FPoint();   // 移動中の座標

    FPoint	mExt1stCenter = new FPoint();
    double	mExt1stDist;
    double	mNewDist;
    float	mExt1stScale;
    FPoint	mExt1stPos = new FPoint();
    FPoint  mEvPos0 = new FPoint();
    FPoint  mEvPos1 = new FPoint();
    void	setEvPos(float x0, float y0, float x1, float y1){
        mEvPos0.x = x0;
        mEvPos0.y = y0;
        mEvPos1.x = x1;
        mEvPos1.y = y1;
    }

    double getPointDist(float x0,float y0,float x1,float y1){
        setEvPos(x0, y0, x1, y1);

        return (x0-x1)*(x0-x1)+(y0-y1)*(y0-y1);
    }
    void setExt1st(){
        mExt1stCenter.x = (mEvPos0.x + mEvPos1.x) / 2;
        mExt1stCenter.y = (mEvPos0.y + mEvPos1.y) / 2;
        mExt1stScale = mScale;
        mExt1stPos.x = mOrgPos.x;
        mExt1stPos.y = mOrgPos.y;
    }
    void	resetMovingPoint(){
        mTmode = TMODE.T_None;
    }
/////////////////////////////////////////
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        mCurTouch.x = event.getX();
        mCurTouch.y = event.getY();
        boolean	rtn = false;

        switch( mTmode ){
        case T_None:
            rtn = switchNone(event);
            break;
        case T_Scroll:
            rtn = switchScroll(event);
            break;
        case T_Ext:
            rtn = switchExt(event);
            break;
        case MI_Rect:
            rtn = switchRect( event );
            break;
        }

        invalidate();
        return rtn;
    }

// まだタッチしてない状態からのスタート
    boolean switchNone(MotionEvent event){
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(event.getPointerCount() > 1){
                    // ２本指でタッチしたら拡大モード
                    mExt1stDist = getPointDist(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    setExt1st();
                    mTmode = TMODE.T_Ext;
                    return true;
                }
                mTouch.x = event.getX();
                mTouch.y = event.getY();
                mTmode = TMODE.T_Scroll;
                if( mVmode == ViewMode.EditArea ) {
                    if( mViewAreaProc.checkPoint( new FPoint(view2orgX(mTouch.x), view2orgY(mTouch.y)) )) {
                        mTmode = TMODE.MI_Rect;
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:
                resetMovingPoint();
                invalidate();
                break;
            default:
        }
        return false;
    }

// スクロール処理
    boolean switchScroll(MotionEvent event){
        switch(event.getAction()){
            case MotionEvent.ACTION_MOVE:
                if(event.getPointerCount() > 1){
                    mExt1stDist = getPointDist(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    setExt1st();

                    mTmode = TMODE.T_Ext;
                    return true;
                }
                mOrgPos.x = mOrgPos.x + (mTouch.x - mCurTouch.x)/mScale;
                mOrgPos.y = mOrgPos.y + (mTouch.y - mCurTouch.y)/mScale;

                mTouch.x = mCurTouch.x;
                mTouch.y = mCurTouch.y;

                invalidate();
                return false;
            case MotionEvent.ACTION_UP:
                resetMovingPoint();
                invalidate();
                return false;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                if(event.getPointerCount() > 1){
                    mExt1stDist = getPointDist(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    setExt1st();

                    mTmode = TMODE.T_Ext;
                }
        }
        return false;
    }
// 拡大処理
    boolean switchExt(MotionEvent event){
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                mNewDist = getPointDist(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                if(mExt1stDist/mNewDist < 0.1 ){
                    mNewDist = mExt1stDist * 10;
                }
                float sx = view2orgX((float)mExt1stCenter.x);
                float sy = view2orgY((float)mExt1stCenter.y);
                mScale = (float) (mExt1stScale * mNewDist / mExt1stDist);
                float ex = view2orgX((float)mExt1stCenter.x);
                float ey = view2orgY((float)mExt1stCenter.y);

                mOrgPos.x = mOrgPos.x + (sx - ex);
                mOrgPos.y = mOrgPos.y + (sy - ey);

                invalidate(); // �ʒm
                return false;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            default:
                resetMovingPoint();
                return false;
        }
    }

    boolean switchRect(MotionEvent event){
        switch(event.getAction()){
            case MotionEvent.ACTION_MOVE:
                if(event.getPointerCount() > 1){
                    mExt1stDist = getPointDist(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    setExt1st();

                    mTmode = TMODE.T_Ext;
                    return true;
                }

                mViewAreaProc.moveViewArea( (mCurTouch.x - mTouch.x )/mScale, ( mCurTouch.y - mTouch.y )/mScale );

                mTouch.x = mCurTouch.x;
                mTouch.y = mCurTouch.y;

                invalidate();
                return false;
            case MotionEvent.ACTION_UP:
                resetMovingPoint();
                invalidate();
                return false;
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                if(event.getPointerCount() > 1){
                    mExt1stDist = getPointDist(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    setExt1st();

                    mTmode = TMODE.T_Ext;
                }
        }
        return false;
    }
/////////////////////////////////////////////////////
    public boolean loadFile(String bmpfile){
        mSrcFilename = bmpfile;

        if( !loadImageFile(bmpfile) ){
            return false;
        }
        setDefPos();
        for( int n = mPieViewFullscreenActivity.mFocusNo; n >= 0; --n) {
            String fname = mPieViewFullscreenActivity.mList.get(n).getName();
            String prop = mPieViewFullscreenActivity.mBookFolder.getAnnotatiion( BookFolder.PropPreStr + fname );
            if( !prop.isEmpty() ) {
                mViewAreaProc.setProperty(prop);
                if( mViewAreaProc.mTA.size()!=0 ) {
                    fitFocussedArea(false);
                    return true;
                }
            }
        }
        return true;
    }

    public boolean next(){
        if( mViewAreaProc.mTetFocus >= mViewAreaProc.mTA.size() - 1 ){
            return false;
        }
        mViewAreaProc.mTetFocus++;
        return true;
    }
    public boolean before(){
        if( mViewAreaProc.mTetFocus == 0 ){
            return false;
        }
        mViewAreaProc.mTetFocus--;
        return true;
    }

    public void saveProperty(){
        try {
            BookFolder bf = mPieViewFullscreenActivity.getBookFolder();
            bf.saveAnnotation(BookFolder.PropPreStr + BookFolder.getFileName(bf.getCurPageFullpath()), getViewAreaProc().getProperty());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    void fitFocussedArea(boolean isBefore){
        ViewArea va = getViewAreaProc().getFocussedViewArea();
        if( va != null ) {
            RectF rect = va.getArea();
            if( va.mBrightness != ViewArea.DefaultValue ) {
                mBrightness = va.mBrightness;
                mContrast = va.mContrast;
            }
            ViewArea.FitTo ft = va.mFitTo;
            if( isBefore ){
                switch ( ft ){
                    case Up: ft = ViewArea.FitTo.Down; break;
                    case Down: ft = ViewArea.FitTo.Up; break;
                    case Left: ft = ViewArea.FitTo.Right; break;
                    case Right: ft = ViewArea.FitTo.Left; break;
                }
            }
            fitArea(rect.top, rect.left, rect.right, rect.bottom, ft);
        }
    }
}

class ViewArea {
    RectF area;
    String  annotation;
    float mContrast;	// 0..10 1is default.
    float mBrightness;	// -255..255 0 is default
    final static float DefaultValue = 10000;

    public enum FitTo {Undef, Center, Up, Left, Right, Down, Width, Height,  } ;
    FitTo	mFitTo;

    ViewArea(){
        area = new RectF();
        annotation = "";
        mContrast = DefaultValue; //1.0f;
        mBrightness = DefaultValue; // 0;
        mFitTo = FitTo.Up;
    }

    ViewArea( String json ){
        set( json );
    }

    ViewArea( ViewArea va ){
        area = new RectF( va.getArea() );
        annotation = "";
        mContrast = va.mContrast;
        mBrightness = va.mBrightness;
        mFitTo = va.mFitTo;
    }

    public void adjustLoadScale( int loadscale ){
        if( loadscale==0){
            return;
        }
        area.left = area.left / loadscale;
        area.right = area.right / loadscale;
        area.top = area.top / loadscale;
        area.bottom = area.bottom / loadscale;
    }
    public void adjustSaveScale( int savescale ){
        if( savescale==0){
            return;
        }
        area.left = area.left * savescale;
        area.right = area.right * savescale;
        area.top = area.top * savescale;
        area.bottom = area.bottom * savescale;
    }

    public void set( String json ){
        try {
            JSONObject jObj = new JSONObject(json);
            String rectstr =(String) jObj.get("area");
            setArea( rectstr );
            annotation = (String) jObj.get("annotation");
            mContrast = getJsonFloat( jObj, "contrast");
            mBrightness = getJsonFloat( jObj, "brightness");
            String sFitTo = (String)jObj.get("fitto");
            if( sFitTo.equals("Up")){
                mFitTo = FitTo.Up;
            }else
            if( sFitTo.equals("Down")){
                mFitTo = FitTo.Down;
            }else
            if( sFitTo.equals("Left")){
                mFitTo = FitTo.Left;
            }else
            if( sFitTo.equals("Right")){
                mFitTo = FitTo.Right;
            }else
            if( sFitTo.equals("Center")){
                mFitTo = FitTo.Center;
            }
        }catch (JSONException e){
            annotation = e.toString();
        }
    }

    Float getJsonFloat( JSONObject jobj, String key ){
        Float fval=1.0f;
        try{
            Object obj = jobj.get(key);
            Class objclass = obj.getClass();
            String classname = objclass.getName();
            if( classname.contains("Double")){
                fval = ((Double) obj).floatValue();
            }else
            if( classname.contains("Integer")){
                fval = ((Integer)obj).floatValue();
            }

        }catch (JSONException e){
            annotation = e.toString();
        }
        return fval;
    }

    public FitTo getFitTo(){
        return mFitTo;
    }
    public void setFitTo( FitTo fitto ){
        mFitTo = fitto;
    }
    public void setFitTo( int fno ){
        if( fno==1 ){
            mFitTo = FitTo.Up;
        }
        if( fno==3){
            mFitTo = FitTo.Down;
        }
        if( fno==0 ){
            mFitTo = FitTo.Left;
        }
        if( fno==2){
            mFitTo = FitTo.Right;
        }
    }

    public FPoint[] getSrcPoints(){
        FPoint[] dpos = new FPoint[4];
        for(int n=0; n<dpos.length; n=n+1 ){
            dpos[n] = new FPoint();
        }
        dpos[0].y = area.top;
        dpos[0].x = area.left;
        dpos[1].y = area.top;
        dpos[1].x = area.right;
        dpos[2].y = area.bottom;
        dpos[2].x = area.right;
        dpos[3].y = area.bottom;
        dpos[3].x = area.left;
        return dpos;
    }

    public RectF   getArea(){
        return area;
    }
    public void    setArea( RectF a ){
        area.set(a);
    }
    public void    setArea( String s ){
        String[] fnums = s.split("\\(")[1].split("\\)")[0].split(",");
        RectF rect =  new RectF(Float.parseFloat(fnums[0]), Float.parseFloat(fnums[1]), Float.parseFloat(fnums[2]), Float.parseFloat(fnums[3]));
        area = new RectF();
        area.set( rect );
    }

    public void move( float mx, float my){
        area.top = area.top + my;
        area.bottom = area.bottom + my;
        area.left = area.left + mx;
        area.right = area.right + mx;
    }

    public String  getAnnotation(){
        return annotation;
    }
    public void    setAnnotation( String str ){
        annotation = str;
    }

    public float   getContrast(){
        return mContrast;
    }
    public void setContrast( float c ){
        mContrast = c;
    }

    public float   getBrightness(  ){
        return mBrightness;
    }
    public void setBrightness( float b ){
        mBrightness = b;
    }

    public String  toString(){
        JSONObject  json = new JSONObject();
        try {
            json.accumulate("area", area);
            json.accumulate("annotation", annotation);
            json.accumulate("contrast",mContrast);
            json.accumulate("brightness",mBrightness);
            json.accumulate("fitto", mFitTo);
        }catch (org.json.JSONException e){
            return e.toString();
        }
        return json.toString();
    }
}

class ViewAreaProc {
    final float NearLimit = 64.0f;
    List<ViewArea> mTA;
    int mTetFocus;
    PieView mPieView;
    PieView.TMODE   mTmode;

    ViewAreaProc(PieView pv) {
        mPieView = pv;
        mTA = new ArrayList<ViewArea>();
        mTetFocus = -1;
        mTmode = PieView.TMODE.T_None;
    }

    public void addViewArea( ViewArea va ){
        ViewArea pd = new ViewArea( va );
        mTA.add(pd);
        mTetFocus = mTA.size() - 1;
    }
    public void addViewArea( RectF rect ){
        ViewArea pd = new ViewArea();
        pd.setArea(rect);
        mTA.add(pd);
        mTetFocus = mTA.size() - 1;
    }
    public void delViewArea(){
        if( !isValidIndex(mTetFocus) ){
            return;
        }
        mTA.remove(mTetFocus );
        if( mTetFocus >= mTA.size() ){
            mTetFocus = mTA.size() - 1;
        }
    }
    public ViewArea getFocussedViewArea(){
        if( isValidIndex(mTetFocus) ){
            return mTA.get(mTetFocus);
        }
        return null;
    }

    boolean isValidIndex( int n ){
        return ! (n < 0 || mTA.size() <= n);
    }

    public String getProperty(){
        StringBuffer bp = new StringBuffer();
        bp.append("{props:[");
        for(int n=0; n < mTA.size(); ++n){
            if(n > 0){
                bp.append(",");
            }
            ViewArea va = new ViewArea( mTA.get(n) );
            va.adjustSaveScale( mPieView.mLoadScale );
            bp.append( va.toString() );
        }
        bp.append("],focus:"+String.valueOf(mTetFocus)+
                ",loadscale:"+String.valueOf(mPieView.mLoadScale)+
                "}");
        return bp.toString();
    }

    public void setProperty( String prop ){
        try {
            JSONObject json = new JSONObject( prop );
            JSONArray props = json.getJSONArray("props");
            mTetFocus = json.getInt("focus");
            mTA.clear();
            for( int n=0; n < props.length(); ++n ){
                ViewArea va = new ViewArea( props.get(n).toString() );
                va.adjustLoadScale( mPieView.mLoadScale );
                mTA.add( va );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////
    void showAllViewArea(Canvas canvas){
        Paint spaint = new Paint();
        spaint.setColor(Color.argb(255, 180, 32, 32));
        Paint sFpaint = new Paint();
        sFpaint.setColor(Color.argb(255, 255, 0, 0));

        for( int i = 0; i < mTA.size(); ++i) {
            showViewArea(mTA.get(i), canvas, spaint, Integer.toString(i));
        }

        try{
            showViewArea(mTA.get(mTetFocus), canvas, sFpaint, Integer.toString(mTetFocus));
        }catch(IndexOutOfBoundsException e){}

    }

    // 領域編集時の領域表示
    void showViewArea(ViewArea tetra, Canvas canvas, Paint paint, String tetno){
        // 領域座標の取得
        FPoint[] tpos = tetra.getSrcPoints();
        // 元座標からスクリーン座標に変換　および領域の中心を計算
        FPoint cp = new FPoint(0,0);
        FPoint[] dpos = new FPoint[4];
        for(int n=0; n<dpos.length; n=n+1 ){
            dpos[n] = new FPoint();
            dpos[n].x = mPieView.org2viewX((float) tpos[n].x);
            dpos[n].y = mPieView.org2viewY((float) tpos[n].y);
            cp.x = cp.x + dpos[n].x;
            cp.y = cp.y + dpos[n].y;
        }

        for(int n=0; n<dpos.length; n=n+1 ){
            canvas.drawRect((float)(dpos[n].x-12), dpos[n].y-12, dpos[n].x+12, dpos[n].y+12, paint);
        }

        // フォーカス変更用の中央の大きな丸の描画
        // テトラの各頂点の描画
        Paint tpaint = new Paint();
        tpaint.setColor(Color.WHITE);
        tpaint.setTextSize(28);
        canvas.drawCircle((float)cp.x/4, (float)cp.y/4, (float)30.0, paint);
        canvas.drawText(tetno, (float)(cp.x/4-10), (float)cp.y/4+6, tpaint);

        // 頂点をつなぐ辺の描画
        for(int n=0; n<dpos.length-1; n=n+1  ){
            canvas.drawLine(dpos[n].x, dpos[n].y, dpos[n+1].x, dpos[n+1].y, paint);
            canvas.drawRect((dpos[n].x+dpos[n+1].x)/2-8, (dpos[n].y+dpos[n+1].y)/2-8,
                            (dpos[n].x+dpos[n+1].x)/2+8, (dpos[n].y+dpos[n+1].y)/2+8, paint);
        }
        canvas.drawLine(dpos[dpos.length-1].x, dpos[dpos.length-1].y, dpos[0].x, dpos[0].y, paint);
        canvas.drawRect((dpos[0].x+dpos[dpos.length-1].x)/2-8, (dpos[0].y+dpos[dpos.length-1].y)/2-8,
                        (dpos[0].x+dpos[dpos.length-1].x)/2+8, (dpos[0].y+dpos[dpos.length-1].y)/2+8, paint);

        // 寄せる辺を強調
        paint.setStrokeWidth(3);
        switch( tetra.getFitTo() ){
            case Up:
                canvas.drawLine((float)dpos[0].x+4, (float)dpos[0].y+4, (float)dpos[1].x+4, (float)dpos[1].y+4, paint);
                canvas.drawLine((float)dpos[0].x  , (float)dpos[0].y  , (float)dpos[1].x  , (float)dpos[1].y  , paint);
                canvas.drawLine((float)dpos[0].x-4, (float)dpos[0].y-4, (float)dpos[1].x-4, (float)dpos[1].y-4, paint);
                break;
            case Right:
                canvas.drawLine((float)dpos[1].x+4, (float)dpos[1].y+4, (float)dpos[2].x+4, (float)dpos[2].y+4, paint);
                canvas.drawLine((float)dpos[1].x  , (float)dpos[1].y  , (float)dpos[2].x  , (float)dpos[2].y  , paint);
                canvas.drawLine((float)dpos[1].x-4, (float)dpos[1].y-4, (float)dpos[2].x-4, (float)dpos[2].y-4, paint);
                break;
            case Down:
                canvas.drawLine((float)dpos[2].x+4, (float)dpos[2].y+4, (float)dpos[3].x+4, (float)dpos[3].y+4, paint);
                canvas.drawLine((float)dpos[2].x  , (float)dpos[2].y  , (float)dpos[3].x  , (float)dpos[3].y  , paint);
                canvas.drawLine((float)dpos[2].x-4, (float)dpos[2].y-4, (float)dpos[3].x-4, (float)dpos[3].y-4, paint);
                break;
            case Left:
                canvas.drawLine((float)dpos[3].x+4, (float)dpos[3].y+4, (float)dpos[0].x+4, (float)dpos[0].y+4, paint);
                canvas.drawLine((float)dpos[3].x  , (float)dpos[3].y  , (float)dpos[0].x  , (float)dpos[0].y  , paint);
                canvas.drawLine((float)dpos[3].x-4, (float)dpos[3].y-4, (float)dpos[0].x-4, (float)dpos[0].y-4, paint);
                break;
            default:
                break;
        }
    }

    void setBrightness( float brightness ){
        mTA.get(mTetFocus).mBrightness = brightness;
    }

    void setContrast( float contrast ){
        mTA.get(mTetFocus).mContrast = contrast;
    }

    void moveViewArea( float mx, float my ){
        switch( mTmode ) {
        case MI_Ap0:
            mTA.get(mTetFocus).area.left += mx;
            mTA.get(mTetFocus).area.top += my;
            break;
        case MI_Ap1:
            mTA.get(mTetFocus).area.right += mx;
            mTA.get(mTetFocus).area.top += my;
            break;
        case MI_Ap2:
            mTA.get(mTetFocus).area.right += mx;
            mTA.get(mTetFocus).area.bottom += my;
            break;
        case MI_Ap3:
            mTA.get(mTetFocus).area.left += mx;
            mTA.get(mTetFocus).area.bottom += my;
            break;
        case MI_Ln0:
            mTA.get(mTetFocus).area.left += mx;
            break;
        case MI_Ln1:
            mTA.get(mTetFocus).area.top += my;
            break;
        case MI_Ln2:
            mTA.get(mTetFocus).area.right += mx;
            break;
        case MI_Ln3:
            mTA.get(mTetFocus).area.bottom += my;
            break;
        case MI_Rect:
            mTA.get(mTetFocus).move(mx, my);
        }
    }

    boolean checkPoint( FPoint touch ){
        if( isValidIndex( mTetFocus ) ){
            ViewArea va = mTA.get(mTetFocus);
            FPoint[] tpos = va.getSrcPoints();
            FPoint lpos = new FPoint();
            for( int n=0; n < tpos.length; ++n ) {
                if (isTouchPoint(tpos[n], touch, NearLimit)) {
                    mTmode = PieView.TMODE.valueOf("MI_Ap" + Integer.toString(n));;
                    return true;
                }
                if (n == 0){
                    lpos.x = (tpos[n].x + tpos[tpos.length-1].x)/2;
                    lpos.y = (tpos[n].y + tpos[tpos.length-1].y)/2;
                }else{
                    lpos.x = (tpos[n].x + tpos[n-1].x)/2;
                    lpos.y = (tpos[n].y + tpos[n-1].y)/2;
                }
                if (isTouchPoint(lpos, touch, NearLimit)) {
                    mTmode = PieView.TMODE.valueOf("MI_Ln" + Integer.toString(n));;
                    va.setFitTo( n );
                    return true;
                }
            }

            for( int n=0; n < mTA.size(); ++n ){
                va = mTA.get(n);
                tpos = va.getSrcPoints();
                lpos.x = (tpos[0].x+tpos[1].x)/2;
                lpos.y = (tpos[0].y+tpos[3].y)/2;
                if (isTouchPoint(lpos, touch, NearLimit)) {
                    mTmode = PieView.TMODE.MI_Rect;
                    mTetFocus = n;
                    return true;
                }
            }
        }

        return false;
    }

    boolean	isTouchPoint(FPoint target, FPoint touch, float a ){
        return inRangeCheck(touch.x, target.x, a ) &&
                inRangeCheck(touch.y, target.y, a );
    }
    // 範囲チェック
    static boolean inRangeCheck( float v, float cv, float ev ){
        float lv = cv - ev;
        float hv = cv + ev;
        return ( lv < v && v < hv );
    }
    static float inAreaValue( Point pos, Point[] area ){
        Point grp = new Point();
        for( int n=0; n < 4; ++n ){
            grp.x = grp.x + (area[n].x - pos.x);
            grp.y = grp.y + (area[n].y - pos.y);
        }

        return (float)( grp.x * grp.x + grp.y * grp.y);
    }
    static boolean inAreaCheck( Point pos, Point[] area ){
        Point grp = new Point();
        for( int n=0; n < 4; ++n ){
            grp.x = grp.x + (area[n].x - pos.x);
            grp.y = grp.y + (area[n].y - pos.y);
        }

        return ( grp.x * grp.x + grp.y * grp.y) < 180000 ;
    }

}

class FPoint {
    float   x;
    float   y;

    FPoint(){
        x = 0.0f;
        y = 0.0f;
    }
    FPoint( int _x, int _y ){
        x = (float)_x;
        y = (float)_y;
    }
    FPoint( float _x, float _y ){
        x = (float)_x;
        y = (float)_y;
    }
}
package Xinyue.all.activity;




import java.io.File;
import java.util.ArrayList;





import music.dreamer.useful.BaseActivity;
import music.dreamer.useful.InfoHelper;
import music.dreamer.useful.MusicService;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**播放选择的3D旋转页面*/
public class RotateActivity extends BaseActivity implements OnTouchListener  {
	
	
	protected static ArrayList<Activity> All_activityList = new ArrayList<Activity>();
	public static  int REQUEST_CODE_GETIMAGE_BYCAMERA = 1;
    public static  String thisLarge = null, theSmall = null;
	private ViewGroup layoutmain;
	private ViewGroup layoutnext;
	private ViewGroup layoutlast;
	
	private Rotate3D rotate3d;
	private Rotate3D rotate3d2;
	private Rotate3D rotate3d3;
	private int mCenterX ;		
	private int mCenterY ;		
	private float degree = (float) 0.0;
	private int currentTab = 0;
	private float perDegree;
	private VelocityTracker mVelocityTracker;
	
	private boolean areButtonsShowing;
	private RelativeLayout composerButtonsWrapper;
	private ImageView composerButtonsShowHideButtonIcon;
	private RelativeLayout composerButtonsShowHideButton;
	
	//界面隐藏控件对象
	private ImageButton ZuozheButton;
	private ImageButton AboutButton;
	private ImageButton CameraButton;
	private ImageButton ExitButton;
	private ImageButton music;
	private ImageButton video;
	private ImageView MusicButton;
	private ImageView VideoButton;

	public Intent intent_SERVICE;
	public Intent video_intent;
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	All_activityList.add(this);
        initMain();
        MyAnimations.initOffset(this);
 
		// 加号的动画
		composerButtonsShowHideButton.startAnimation(MyAnimations.getRotateAnimation(0, 360, 200));
        DisplayMetrics dm = new DisplayMetrics();
		dm = getResources().getDisplayMetrics();
		mCenterX = dm.widthPixels / 2;
		mCenterY = dm.heightPixels / 2;
		perDegree = (float) (90.0 / dm.widthPixels);
		
		//为ImageButton控件添加事件监听
		MusicButton.setOnClickListener(new MusicButtonListener());
		VideoButton.setOnClickListener(new VideoButtonListener());
		//ImageButton的初始化以及事件监听的赋予
		CameraButton = (ImageButton)findViewById(R.id.composer_button_photo);
		ZuozheButton = (ImageButton)findViewById(R.id.composer_button_people);
		AboutButton = (ImageButton)findViewById(R.id.composer_button_place);
		music= (ImageButton)findViewById(R.id.composer_button_music);
		video=(ImageButton)findViewById(R.id.composer_button_thought);
		ExitButton = (ImageButton)findViewById(R.id.composer_button_sleep);
		CameraButton.setOnClickListener(new CameraButtonListener());
		ZuozheButton.setOnClickListener(new ZuozheButtonListener());
		AboutButton.setOnClickListener(new AboutButtonListener());
		music.setOnClickListener(new MusicButtonListener());
		video.setOnClickListener(new VideoButtonListener());
		ExitButton.setOnClickListener(new ExitButtonListener());
	}
	

	class MusicButtonListener implements OnClickListener{
		
		public void onClick(View v) {
			// TODO Auto-generated method stub
			 //Intent一个音乐后台服务，并开启这个服务
	        intent_SERVICE = new Intent();
	        intent_SERVICE.setAction("music.dreamer.media.MUSIC_SERVICE");
	        intent_SERVICE.putExtra("list", 1);
	        startService(intent_SERVICE);
			
			Intent music1 = new Intent();
		    music1.setClass(RotateActivity.this, MainActivity.class);
			startActivity(music1);			
			overridePendingTransition(R.anim.s,R.anim.a); 
			Toast.makeText(RotateActivity.this,"已切换至音乐播放区！",Toast.LENGTH_SHORT).show();	
		}
	}
	class VideoButtonListener implements OnClickListener{

		public void onClick(View v) {
			// TODO Auto-generated method stub
			//Intent一个后台服务，并开启这个服务
			video_intent = new Intent();
	        video_intent.setAction("music.dreamer.media.VIDEO_SERVICE");
	        startService(video_intent);
			
			Intent video1 = new Intent();
			video1.setClass(RotateActivity.this, VideoPlayerActivity.class);
			startActivity(video1);
		    Toast.makeText(RotateActivity.this,"已切换至影视播放区！",Toast.LENGTH_SHORT).show();		
		}
	}
	
	class ZuozheButtonListener implements OnClickListener{

		public void onClick(View v) {
			// TODO Auto-generated method stub
			openDialog();				
			Toast.makeText(RotateActivity.this,"Dreamer团队简介",Toast.LENGTH_SHORT).show();
           
		}
	}
	class CameraButtonListener implements OnClickListener{

		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent takephoto = new Intent("android.media.action.IMAGE_CAPTURE");	
			
			String camerName = InfoHelper.getFileName();
			String fileName = "Share" + camerName + ".tmp";	
			
			File camerFile = new File( InfoHelper.getCamerPath(), fileName );
					
			theSmall = InfoHelper.getCamerPath() + fileName;
			thisLarge = getLatestImage();
			
			Uri originalUri = Uri.fromFile( camerFile );
			takephoto.putExtra(MediaStore.EXTRA_OUTPUT, originalUri); 	
			startActivityForResult(takephoto, REQUEST_CODE_GETIMAGE_BYCAMERA);
           
		}
	}
	class AboutButtonListener implements OnClickListener{

		public void onClick(View v) {
			// TODO Auto-generated method stub
			openDatilDialog();
			Toast.makeText(RotateActivity.this,"欣悦影音介绍",Toast.LENGTH_SHORT).show();
		}
	}
	class ExitButtonListener implements OnClickListener{

		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (VideoPlayerActivity.uri != null) {
				RotateActivity.this.stopService(video_intent); 
			}
			if(MusicService.mp!=null){
				MusicService.nm.cancelAll();
				RotateActivity.this.stopService(intent_SERVICE); 
	 			onDestroy();		 			
				System.exit(0);
        	}
			else{
				Toast.makeText(RotateActivity.this,"亲，下次再见咯！",Toast.LENGTH_SHORT).show();
				RotateActivity.this.finish();
				onDestroy();
				System.exit(0);
			}
			
		}
	}
	private void setListener() {
		// 给大按钮设置点击事件
		composerButtonsShowHideButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!areButtonsShowing) {
					// 图标的动画
					MyAnimations.startAnimationsIn(composerButtonsWrapper, 300);
					// 加号的动画
					composerButtonsShowHideButtonIcon.startAnimation(MyAnimations.getRotateAnimation(0, -225, 300));
				} else {
					// 图标的动画
					MyAnimations.startAnimationsOut(composerButtonsWrapper, 300);
					// 加号的动画
					composerButtonsShowHideButtonIcon.startAnimation(MyAnimations.getRotateAnimation(-225, 0, 300));
				}
				areButtonsShowing = !areButtonsShowing;
			}
		});

		// 给小图标设置点击事件
		for (int i = 0; i < composerButtonsWrapper.getChildCount(); i++) {
			final ImageView smallIcon = (ImageView) composerButtonsWrapper.getChildAt(i);
			final int position = i;
			smallIcon.setOnClickListener(new View.OnClickListener() {
				public void onClick(View arg0) {
					// 这里写各个item的点击事件
					// 1.加号按钮缩小后消失 缩小的animation
					// 2.其他按钮缩小后消失 缩小的animation
					// 3.被点击按钮放大后消失 透明度渐变 放大渐变的animation
					if(areButtonsShowing){
						composerButtonsShowHideButtonIcon.startAnimation(MyAnimations.getRotateAnimation(-225, 0, 300));
						smallIcon.startAnimation(MyAnimations.getMaxAnimation(400));
						for (int j = 0; j < composerButtonsWrapper.getChildCount(); j++) {
							if (j != position) {
								final ImageView smallIcon = (ImageView) composerButtonsWrapper.getChildAt(j);
								smallIcon.startAnimation(MyAnimations.getMiniAnimation(300));
								//MyAnimations.getMiniAnimation(300).setFillAfter(true);
							}
						}
						areButtonsShowing = !areButtonsShowing;
					}
					
					
				}
			});
		}
	}
	//初始3D页面的显示布局
	private void initMain(){
        setContentView(R.layout.rotate);
        
        composerButtonsWrapper = (RelativeLayout) findViewById(R.id.composer_buttons_wrapper);
		composerButtonsShowHideButton = (RelativeLayout) findViewById(R.id.composer_buttons_show_hide_button);
		composerButtonsShowHideButtonIcon = (ImageView) findViewById(R.id.composer_buttons_show_hide_button_icon);
		
        layoutnext = (ViewGroup) findViewById(R.id.layout_next);
        layoutnext.setOnTouchListener(this);
		MusicButton = (ImageView)findViewById(R.id.image);
		
		
        layoutlast = (ViewGroup) findViewById(R.id.layout_last);
        layoutlast.setOnTouchListener(this);
        VideoButton= (ImageView)findViewById(R.id.image1);
        
		layoutmain = (ViewGroup)findViewById(R.id.layout_main);
		layoutmain.setOnTouchListener(this);
		 setListener();
	}
	
	/*打开软件介绍*/
	private  void openDatilDialog(){
		LayoutInflater in = RotateActivity.this.getLayoutInflater();
		final View datil = in.inflate(R.layout.datil, null);
		
		new AlertDialog.Builder(this)
				.setTitle("清远职业技术学院")
				.setIcon(R.drawable.alarm)
				.setView(datil)
				.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {

							
							public void onClick(DialogInterface dialog,
									int which) {														
									Toast.makeText(RotateActivity.this,
											"阅览完毕，感谢您对本软件的使用！", Toast.LENGTH_SHORT)
											.show();
							} 
					
						}).setNegativeButton("取消", null).show();

	                            }
	
	/*打开Dreamer团队介绍*/
	private  void openDialog(){
		LayoutInflater in = RotateActivity.this.getLayoutInflater();
		final View alarm = in.inflate(R.layout.alarm, null);
		
		new AlertDialog.Builder(this)
				.setTitle("清远职业技术学院")
				.setIcon(R.drawable.alarm)
				.setView(alarm)
				.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {

							
							public void onClick(DialogInterface dialog,
									int which) {														
									Toast.makeText(RotateActivity.this,
											"阅览完毕，感谢您对本团队的支持！", Toast.LENGTH_SHORT)
											.show();
							} 
					
						}).setNegativeButton("取消", null).show();

	                            }
	


	private int mLastMotionX;
	public boolean onTouch(View arg0, MotionEvent event) {
		int x = (int) event.getX();
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();//获得VelocityTracker类实例
			}
			mVelocityTracker.addMovement(event);//将事件加入到VelocityTracker类实例中
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			break;
		case MotionEvent.ACTION_MOVE:
			mVelocityTracker.computeCurrentVelocity(1000, 1000); 
			Log.i("test","velocityTraker :"+mVelocityTracker.getXVelocity());
			int dx = x - mLastMotionX;
			if(dx != 0){
				doRotate(dx);
				if(degree > 90){			
					degree = 0;		
					break;
				}
			}else{
				return false;
			}
			mLastMotionX = x;
			break;
		case MotionEvent.ACTION_UP:
			//设置units的值为1000，意思为一秒时间内运动了多少个像素
			mVelocityTracker.computeCurrentVelocity(1000); 
			float VelocityX = mVelocityTracker.getXVelocity();
			Log.i("test","velocityTraker2:"+mVelocityTracker.getXVelocity());
			if(VelocityX > 500 || VelocityX < -500 ){
				endRotateByVelocity();
			}else{
				endRotate();
			}
			   releaseVelocityTracker();  
               break;  
 
           case MotionEvent.ACTION_CANCEL:  
               releaseVelocityTracker();  
               break;  
		}
		return true;
	}
	private void releaseVelocityTracker() {
		if(null != mVelocityTracker) {  
            mVelocityTracker.clear();  
            mVelocityTracker.recycle();  
            mVelocityTracker = null;  
        }  
		
	}
	//3D页面的循环
	private void endRotateByVelocity(){
		if(degree > 0){
			rotate3d = new Rotate3D(degree , 90 , 0, mCenterX, mCenterY);
			rotate3d3 = new Rotate3D( - 90 + degree,0,0, mCenterX, mCenterY);
			rotate3d.setDuration(300);
			rotate3d3.setDuration(300);
			if(currentTab == 0){
				layoutmain.startAnimation(rotate3d);
				layoutlast.startAnimation(rotate3d3);	
			}else if(currentTab == 2){
				layoutlast.startAnimation(rotate3d);
				layoutnext.startAnimation(rotate3d3);
			}else if(currentTab == 1){
				layoutnext.startAnimation(rotate3d);
				layoutmain.startAnimation(rotate3d3);
			}
			
			currentTab =(currentTab - 1)%3;
			if(currentTab < 0){
				currentTab = 2;
			}
		}else if(degree < 0){
			rotate3d = new Rotate3D(degree , -90 , 0, mCenterX, mCenterY);
			rotate3d2 = new Rotate3D( 90 + degree,0,0, mCenterX, mCenterY);
			rotate3d.setDuration(300);
			rotate3d2.setDuration(300);
			if(currentTab == 0){
				layoutmain.startAnimation(rotate3d);
				layoutnext.startAnimation(rotate3d2);	
			}else if(currentTab == 1){
				layoutnext.startAnimation(rotate3d);
				layoutlast.startAnimation(rotate3d2);
			}else if(currentTab == 2){
				layoutlast.startAnimation(rotate3d);
				layoutmain.startAnimation(rotate3d2);
			}
			
			currentTab = (currentTab + 1)%3;
		}
		
		
		System.out.println(">>>>>>>>degree:"+degree +" currentTab:" + currentTab);
		setViewVisibile();
		
		degree = 0;
	
	}
	private void endRotate() {
		if(degree > 45){
			rotate3d = new Rotate3D(degree , 90 , 0, mCenterX, mCenterY);
			rotate3d3 = new Rotate3D( - 90 + degree,0,0, mCenterX, mCenterY);
			rotate3d.setDuration(300);
			rotate3d3.setDuration(300);
			if(currentTab == 0){
				layoutmain.startAnimation(rotate3d);
				layoutlast.startAnimation(rotate3d3);	
			}else if(currentTab == 2){
				layoutlast.startAnimation(rotate3d);
				layoutnext.startAnimation(rotate3d3);
			}else if(currentTab == 1){
				layoutnext.startAnimation(rotate3d);
				layoutmain.startAnimation(rotate3d3);
			}
			
			currentTab =(currentTab - 1)%3;
			if(currentTab < 0){
				currentTab = 2;
			}
		}else if(degree < -45){
			rotate3d = new Rotate3D(degree , -90 , 0, mCenterX, mCenterY);
			rotate3d2 = new Rotate3D( 90 + degree,0,0, mCenterX, mCenterY);
			rotate3d.setDuration(300);
			rotate3d2.setDuration(300);
			if(currentTab == 0){
				layoutmain.startAnimation(rotate3d);
				layoutnext.startAnimation(rotate3d2);	
			}else if(currentTab == 1){
				layoutnext.startAnimation(rotate3d);
				layoutlast.startAnimation(rotate3d2);
			}else if(currentTab == 2){
				layoutlast.startAnimation(rotate3d);
				layoutmain.startAnimation(rotate3d2);
			}
			
			currentTab = (currentTab + 1)%3;
		}else{
			rotate3d = new Rotate3D( degree , 0 , 0, mCenterX, mCenterY);
			rotate3d2 = new Rotate3D(  90 + degree,90,0, mCenterX, mCenterY);
			rotate3d3 = new Rotate3D(  - 90 + degree,- 90,0, mCenterX, mCenterY);
			rotate3d.setDuration(500);
			rotate3d2.setDuration(500);
			rotate3d3.setDuration(500);
			if(currentTab == 0){
				layoutmain.startAnimation(rotate3d);
				layoutnext.startAnimation(rotate3d2);
				layoutlast.startAnimation(rotate3d3);
			}else if(currentTab == 1){
				layoutnext.startAnimation(rotate3d);
				layoutlast.startAnimation(rotate3d2);
				layoutmain.startAnimation(rotate3d3);
			}else if(currentTab == 2){
				layoutlast.startAnimation(rotate3d);
				layoutmain.startAnimation(rotate3d2);
				layoutnext.startAnimation(rotate3d3);
			}
		}
		
		
		System.out.println(">>>>>>>>degree:"+degree +" currentTab:" + currentTab);
		setViewVisibile();
		
		degree = 0;
	}
	private void setViewVisibile() {
		if(currentTab == 0){
			layoutmain.setVisibility(View.VISIBLE);
			layoutnext.setVisibility(View.GONE);
			layoutlast.setVisibility(View.GONE);
		}else if(currentTab == 1){
			layoutmain.setVisibility(View.GONE);
			layoutnext.setVisibility(View.VISIBLE);
			layoutlast.setVisibility(View.GONE);
		}else if(currentTab == 2){
			layoutmain.setVisibility(View.GONE);
			layoutnext.setVisibility(View.GONE);
			layoutlast.setVisibility(View.VISIBLE);
		}
	}
	private void doRotate(int dx) {
		float xd = degree;
		layoutnext.setVisibility(View.VISIBLE);
		layoutmain.setVisibility(View.VISIBLE);
		layoutlast.setVisibility(View.VISIBLE);
		
		degree += perDegree*dx;
		System.out.println(">>>>>>>>>degree:" + degree );
		rotate3d = new Rotate3D(xd , degree , 0, mCenterX, mCenterY);
		rotate3d2 = new Rotate3D( 90 + xd,  90+degree,0, mCenterX, mCenterY);
		rotate3d3 = new Rotate3D(-90+xd, -90+degree,0, mCenterX, mCenterY);	
		if(currentTab == 0){
			layoutmain.startAnimation(rotate3d);
			layoutnext.startAnimation(rotate3d2);
			layoutlast.startAnimation(rotate3d3);
		}else if(currentTab == 1){
			layoutmain.startAnimation(rotate3d3);
			layoutnext.startAnimation(rotate3d);
			layoutlast.startAnimation(rotate3d2);
		}else if(currentTab == 2){
			layoutmain.startAnimation(rotate3d2);
			layoutnext.startAnimation(rotate3d3);
			layoutlast.startAnimation(rotate3d);
		}
		rotate3d.setFillAfter(true);
		rotate3d2.setFillAfter(true);
		rotate3d3.setFillAfter(true);
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("温馨提示");
			builder.setIcon(R.drawable.dialog_alert_icon);
			builder.setMessage("您确定要关闭欣悦影音播放器吗？")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog,int which) {
					if (All_activityList.size() > 0) { 
				         for (Activity activity : All_activityList) { 
				               System.gc();
				               activity.finish(); 
				               } 
				         try {
				        	 if (VideoPlayerActivity.uri != null) {
				 				RotateActivity.this.stopService(video_intent); 
				 			}
				        	if(MusicService.mp!=null){
				        		MusicService.nm.cancelAll();
				        		RotateActivity.this.stopService(intent_SERVICE); 

					 			onDestroy();		 			
								System.exit(0);
								
				        	}
				        	else{
				        		onDestroy();
								System.exit(0);		
				        	}
				        } catch (Exception e) {e.printStackTrace();}
				 		}	
					}
							}).setNegativeButton("取消", null).show();

		}
		return false;
	}

    @Override 
    protected void onDestroy() { 
               super.onDestroy(); 
               All_activityList.remove(this); 	                
           }
}

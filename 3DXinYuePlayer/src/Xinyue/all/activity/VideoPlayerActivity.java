package Xinyue.all.activity;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import video.dreamer.views.SoundView;
import video.dreamer.views.SoundView.OnVolumeChangedListener;
import video.dreamer.views.VideoView;
import video.dreamer.views.VideoView.MySizeChangeLinstener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;

/**实现视频播放**/
/*This work comes from Dreamer丶Team. The main programmer is LinShaoHan.
 * QQ:752280466   Welcome to join with us.
 */

public class VideoPlayerActivity extends Activity {
	
	private final static String TAG = "VideoPlayerActivity";
	
	public static LinkedList<MovieInfo> playList = new LinkedList<MovieInfo>();
	public class MovieInfo{
		String displayName;  
		String path;
	}
	private Uri videoListUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	private static int position ;
	private static boolean backFromAD = false;
	private int playedTime;
	
	public static Uri uri=null;
	public static VideoView vv = null;
	private SeekBar seekBar = null;  
	private TextView durationTextView = null;
	private TextView playedTextView = null;
	private GestureDetector mGestureDetector = null;
	private AudioManager mAudioManager = null;  
	
	private int maxVolume = 0;
	private int currentVolume = 0;  
	
	private ImageButton bn1 = null;
	private ImageButton bn2 = null;
	private ImageButton bn3 = null;
	private ImageButton bn4 = null;
	private ImageButton bn5 = null;
	
	private View controlView = null;
	private PopupWindow controler = null;
	
	private SoundView mSoundView = null;
	private PopupWindow mSoundWindow = null;

	
	private static int screenWidth = 0;
	private static int screenHeight = 0;
	private static int controlHeight = 0;  
	protected final static int MENU_MUSIC = Menu.FIRST ;
	protected final static int MENU_NEW = Menu.FIRST + 1;
	protected final static int MENU_DAIL = Menu.FIRST + 2;
	protected final static int MENU_EXIT = Menu.FIRST+3;
	private final static int TIME = 6868;  
	private boolean isControllerShow = true;
	private boolean isPaused = false;
	private boolean isFullScreen = false;
	private boolean isSilent = false;
	private boolean isSoundShow = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState); 
        requestWindowFeature(Window.FEATURE_NO_TITLE);   
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,   
        WindowManager.LayoutParams. FLAG_FULLSCREEN); 
        //设置为横屏模式
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
        setContentView(R.layout.main);
        
        Log.d("OnCreate", getIntent().toString());
        Intent intent = new Intent();
		intent.setClass(VideoPlayerActivity.this, VideoChooseActivity.class);
		VideoPlayerActivity.this.startActivityForResult(intent, 0);
		cancelDelayHide();
        
        Looper.myQueue().addIdleHandler(new IdleHandler(){

			public boolean queueIdle() {
				
				// TODO Auto-generated method stub
				if(controler != null && vv.isShown()){
					controler.showAtLocation(vv, Gravity.BOTTOM, 0, 0);
					controler.update(0, 0, screenWidth, controlHeight);
				}
						

				return false;  
			}
        });
        
        
        controlView = getLayoutInflater().inflate(R.layout.control_views, null);
        controler = new PopupWindow(controlView);
        durationTextView = (TextView) controlView.findViewById(R.id.duration);
        playedTextView = (TextView) controlView.findViewById(R.id.has_played);
        
        mSoundView = new SoundView(this);
        mSoundView.setOnVolumeChangeListener(new OnVolumeChangedListener(){

			public void setYourVolume(int index) {
				 
				cancelDelayHide();
				updateVolume(index);
				hideControllerDelay();
			}
        });
        
        mSoundWindow = new PopupWindow(mSoundView);
        position = -1;
        
        bn1 = (ImageButton) controlView.findViewById(R.id.button1);
        bn2 = (ImageButton) controlView.findViewById(R.id.button2);
        bn3 = (ImageButton) controlView.findViewById(R.id.button3);
        bn4 = (ImageButton) controlView.findViewById(R.id.button4);
        bn5 = (ImageButton) controlView.findViewById(R.id.button5);
        vv = (VideoView) findViewById(R.id.vv);
        
         uri = getIntent().getData();
        if(uri!=null){
        	if(vv.getVideoHeight()==0){
        		vv.setVideoURI(uri);
        	}
        	bn3.setImageResource(R.drawable.pause);
        }else{
        	bn3.setImageResource(R.drawable.play);
        }

        getVideoFile(playList, new File("/sdcard/"));
        Cursor cursor = getContentResolver().query(videoListUri, new String[]{"_display_name","_data"}, null, null, null);
        int n = cursor.getCount();
        cursor.moveToFirst();
        LinkedList<MovieInfo> playList2 = new LinkedList<MovieInfo>();
        for(int i = 0 ; i != n ; ++i){
        	MovieInfo mInfo = new MovieInfo();
        	mInfo.displayName = cursor.getString(cursor.getColumnIndex("_display_name"));
        	mInfo.path = cursor.getString(cursor.getColumnIndex("_data"));
        	playList2.add(mInfo);
        	cursor.moveToNext();
        }
        
        if(playList2.size() > playList.size()){
        	playList = playList2;
        }
        
        vv.setMySizeChangeLinstener(new MySizeChangeLinstener(){

			public void doMyThings() {
				// TODO Auto-generated method stub
				setVideoScale(SCREEN_DEFAULT);
			}
        	
        });
              
        bn1.setAlpha(0xBB);
        bn2.setAlpha(0xBB);  
        bn3.setAlpha(0xBB);
        bn4.setAlpha(0xBB);
        
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        bn5.setAlpha(findAlphaFromSound());
        
        bn1.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(VideoPlayerActivity.this, VideoChooseActivity.class);
				VideoPlayerActivity.this.startActivityForResult(intent, 0);
				cancelDelayHide();
			}
        	
        });
        
        bn4.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				// TODO Auto-generated method stub
				int n = playList.size();
				if(++position < n){
					vv.setVideoPath(playList.get(position).path);
					cancelDelayHide();
					hideControllerDelay();
				}else{
					Toast.makeText(VideoPlayerActivity.this,
							"很抱歉，已找不到相关的播放文件！", Toast.LENGTH_SHORT)
							.show();
					VideoPlayerActivity.this.finish();
				}
			}
        	
        });
        
        bn3.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				// TODO Auto-generated method stub
				cancelDelayHide();
				if(isPaused){
					vv.start();
					bn3.setImageResource(R.drawable.pause);
					hideControllerDelay();
				}else{
					vv.pause();
					bn3.setImageResource(R.drawable.play);
				}
				isPaused = !isPaused;
				
			}
        	
        });
        
        bn2.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(--position>=0){
					vv.setVideoPath(playList.get(position).path);
					cancelDelayHide();
					hideControllerDelay();
				}else{
					Toast.makeText(VideoPlayerActivity.this,
							"很抱歉，已找不到相关的播放文件！", Toast.LENGTH_SHORT)
							.show();
					VideoPlayerActivity.this.finish();
				}
			}
        	
        });
        
        bn5.setOnClickListener(new OnClickListener(){

		public void onClick(View v) {
			// TODO Auto-generated method stub
			cancelDelayHide();
			if(isSoundShow){
				mSoundWindow.dismiss();
			}else{
				if(mSoundWindow.isShowing()){
					mSoundWindow.update(15,0,SoundView.MY_WIDTH,SoundView.MY_HEIGHT);
				}else{
					mSoundWindow.showAtLocation(vv, Gravity.RIGHT|Gravity.CENTER_VERTICAL, 15, 0);
					mSoundWindow.update(15,0,SoundView.MY_WIDTH,SoundView.MY_HEIGHT);
				}
			}
			isSoundShow = !isSoundShow;
			hideControllerDelay();
		}   
       });
        
        bn5.setOnLongClickListener(new OnLongClickListener(){

			public boolean onLongClick(View arg0) {
				// TODO Auto-generated method stub
				if(isSilent){
					bn5.setImageResource(R.drawable.soundenable);
				}else{
					bn5.setImageResource(R.drawable.sounddisable);
				}
				isSilent = !isSilent;
				updateVolume(currentVolume);
				cancelDelayHide();
				hideControllerDelay();
				return true;
			}
        	
        });
        
        seekBar = (SeekBar) controlView.findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

				public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
					// TODO Auto-generated method stub
					
					if(fromUser){
						
						vv.seekTo(progress);
					}
					
				}
	
				public void onStartTrackingTouch(SeekBar arg0) {
					// TODO Auto-generated method stub
					myHandler.removeMessages(HIDE_CONTROLER);
				}
	
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					myHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
				}
        	});
        
        getScreenSize();
       
        mGestureDetector = new GestureDetector(new SimpleOnGestureListener(){

			@Override
			public boolean onDoubleTap(MotionEvent e) {
				// TODO Auto-generated method stub
				if(isFullScreen){
					setVideoScale(SCREEN_DEFAULT);
				}else{
					setVideoScale(SCREEN_FULL);
				}
				isFullScreen = !isFullScreen;
				Log.d(TAG, "onDoubleTap");
				
				if(isControllerShow){
					showController();
				}
				//return super.onDoubleTap(e);
				return true;
			}

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				// TODO Auto-generated method stub
				if(!isControllerShow){
					showController();
					hideControllerDelay();
				}else {
					cancelDelayHide();
					hideController();
				}
				//return super.onSingleTapConfirmed(e);
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				// TODO Auto-generated method stub
				if(isPaused){
					vv.start();
					bn3.setImageResource(R.drawable.pause);
					cancelDelayHide();
					hideControllerDelay();
				}else{
					vv.pause();
					bn3.setImageResource(R.drawable.play);
					cancelDelayHide();
					showController();
				}
				isPaused = !isPaused;
				//super.onLongPress(e);
			}	
        });
                
        vv.setOnPreparedListener(new OnPreparedListener(){

				public void onPrepared(MediaPlayer arg0) {
					// TODO Auto-generated method stub
					
					setVideoScale(SCREEN_DEFAULT);
					isFullScreen = false; 
					if(isControllerShow){
						showController();  
					}
					
					int i = vv.getDuration();
					Log.d("onCompletion", ""+i);
					seekBar.setMax(i);
					i/=1000;
					int minute = i/60;
					int hour = minute/60;
					int second = i%60;
					minute %= 60;
					durationTextView.setText(String.format("%02d:%02d:%02d", hour,minute,second));
					vv.start();  
					bn3.setImageResource(R.drawable.pause);
					hideControllerDelay();
					myHandler.sendEmptyMessage(PROGRESS_CHANGED);
				}	
	        });
        
        vv.setOnCompletionListener(new OnCompletionListener(){

				public void onCompletion(MediaPlayer arg0) {
					// TODO Auto-generated method stub
					int n = playList.size();
					if(++position < n){
						vv.setVideoPath(playList.get(position).path);
					}else{
						VideoPlayerActivity.this.finish();
					}
				}
        	});
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
    	if(requestCode==0&&resultCode==Activity.RESULT_OK){
    		int result = data.getIntExtra("CHOOSE", -1);
    		Log.d("RESULT", ""+result);
    		if(result!=-1){
    			vv.setVideoPath(playList.get(result).path);
    			position = result;
    		}
    		
    		return ;
    	}
		super.onActivityResult(requestCode, resultCode, data);
	
			if (resultCode != RESULT_OK)
				return;

			try {
				AssetFileDescriptor videoAsset = getContentResolver()
						.openAssetFileDescriptor(data.getData(), "r");
				FileInputStream fis = videoAsset.createInputStream();
				File tmpFile = new File(Environment.getExternalStorageDirectory(),"" );
				FileOutputStream fos = new FileOutputStream(tmpFile);
				byte[] buf = new byte[1024];
				int len;
				while ((len = fis.read(buf)) > 0) {
					fos.write(buf, 0, len);
				}
				fis.close();
				fos.close();
			} catch (IOException io_e) {
				// TODO: handle error
			}
		
	}

	private final static int PROGRESS_CHANGED = 0;
    private final static int HIDE_CONTROLER = 1;
    
    
    /*Handler信息传递，用来监听和实现播放视频区域的实时变化*/
    Handler myHandler = new Handler(){
    
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			
			switch(msg.what){
			
				case PROGRESS_CHANGED:
					
					int i = vv.getCurrentPosition();
					seekBar.setProgress(i);
					
					i/=1000;
					int minute = i/60;
					int hour = minute/60;
					int second = i%60;
					minute %= 60;
					playedTextView.setText(String.format("%02d:%02d:%02d", hour,minute,second));
					
					sendEmptyMessage(PROGRESS_CHANGED);
					break;
					
				case HIDE_CONTROLER:
					hideController();
					break;
			}
			
			super.handleMessage(msg);
		}	
    };

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		
		boolean result = mGestureDetector.onTouchEvent(event);
		
		if(!result){
			if(event.getAction()==MotionEvent.ACTION_UP){
				
			}
			result = super.onTouchEvent(event);
		}
		
		return result;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		
		getScreenSize();
		if(isControllerShow){
			
			cancelDelayHide();
			hideController();
			showController();
			hideControllerDelay();
		}
		
		super.onConfigurationChanged(newConfig);
	}

	//暂停播放
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		playedTime = vv.getCurrentPosition();
		vv.pause();
		bn3.setImageResource(R.drawable.play);
		super.onPause();   
	}

	
	//播放的就绪
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		vv.seekTo(playedTime);
		vv.start();  
		if(vv.getVideoHeight()!=0){
			bn3.setImageResource(R.drawable.pause);//显示为暂停按钮
			hideControllerDelay();
		}
		Log.d("REQUEST", "NEW AD !");
		
		super.onResume();
	}

	//双击触碰以及视频显示、视频列表回收等的销毁
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		
		if(controler.isShowing()){
			controler.dismiss();
		
		}
		if(mSoundWindow.isShowing()){
			mSoundWindow.dismiss();
		}
		
		myHandler.removeMessages(PROGRESS_CHANGED);
		myHandler.removeMessages(HIDE_CONTROLER);
		
		playList.clear();
		
		super.onDestroy();
	}     

	//获取手机屏幕的分辨率
	private void getScreenSize()
	{
		Display display = getWindowManager().getDefaultDisplay();
        screenHeight = display.getHeight();
        screenWidth = display.getWidth();
        controlHeight = screenHeight/4;
        
	}
	
	//看视频区的置后控制方法
	private void hideController(){
		if(controler.isShowing()){
			controler.update(0,0,0, 0);
		
			isControllerShow = false;
		}
		if(mSoundWindow.isShowing()){
			mSoundWindow.dismiss();
			isSoundShow = false;
		}
	}
	
	private void hideControllerDelay(){
		myHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
	}
	
	private void showController(){
		controler.update(0,0,screenWidth, controlHeight);
		if(isFullScreen){
		
		}else{
		
		}
		
		isControllerShow = true;
	}
	
	private void cancelDelayHide(){
		myHandler.removeMessages(HIDE_CONTROLER);
	}

    private final static int SCREEN_FULL = 0;
    private final static int SCREEN_DEFAULT = 1;
    
    private void setVideoScale(int flag){
    	
    	LayoutParams lp = vv.getLayoutParams();
    	
    	switch(flag){
    		case SCREEN_FULL:
    			
    			Log.d(TAG, "screenWidth: "+screenWidth+" screenHeight: "+screenHeight);
    			vv.setVideoScale(screenWidth, screenHeight);
    			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    			
    			break;
    			
    		case SCREEN_DEFAULT:
    			
    			int videoWidth = vv.getVideoWidth();
    			int videoHeight = vv.getVideoHeight();
    			int mWidth = screenWidth;
    			int mHeight = screenHeight - 25;
    			
    			if (videoWidth > 0 && videoHeight > 0) {
    	            if ( videoWidth * mHeight  > mWidth * videoHeight ) {
    	                //Log.i("@@@", "image too tall, correcting");
    	            	mHeight = mWidth * videoHeight / videoWidth;
    	            } else if ( videoWidth * mHeight  < mWidth * videoHeight ) {
    	                //Log.i("@@@", "image too wide, correcting");
    	            	mWidth = mHeight * videoWidth / videoHeight;
    	            } else {
    	                
    	            }
    	        }
    			
    			vv.setVideoScale(mWidth, mHeight);

    			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    			
    			break;
    	}
    }

    private int findAlphaFromSound(){
    	if(mAudioManager!=null){
    		int alpha = currentVolume * (0xCC-0x55) / maxVolume + 0x55;
    		return alpha;
    	}else{
    		return 0xCC;
    	}
    }

    private void updateVolume(int index){
    	if(mAudioManager!=null){
    		if(isSilent){
    			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
    		}else{
    			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
    		}
    		currentVolume = index;
    		bn5.setAlpha(findAlphaFromSound());
    	}
    }

    private void getVideoFile(final LinkedList<MovieInfo> list,File file){
    	
    	file.listFiles(new FileFilter(){

			public boolean accept(File file) {
				// TODO Auto-generated method stub
				String name = file.getName();
				int i = name.indexOf('.');
				if(i != -1){
					name = name.substring(i);
					if(name.equalsIgnoreCase(".mp4")||name.equalsIgnoreCase(".3gp")
							||name.equalsIgnoreCase(".avi")||name.equalsIgnoreCase(".fvl")
							||name.equalsIgnoreCase(".rm")||name.equalsIgnoreCase(".rmvb"))
					{	
						MovieInfo mi = new MovieInfo();
						mi.displayName = file.getName();
						mi.path = file.getAbsolutePath();
						list.add(mi);
						return true;
					}
				}else if(file.isDirectory()){
					getVideoFile(list, file);
				}
				return false;
			}
    	});
    }
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case MENU_MUSIC://音乐播放区
			    Intent music = new Intent();
			    music.setClass(VideoPlayerActivity.this, MainActivity.class);
				startActivity(music);			
				Toast.makeText(VideoPlayerActivity.this,"已从影视区切换至音乐播放区！",Toast.LENGTH_SHORT).show();
				finish();
			break;	
			
		case MENU_NEW://视频录制
			    System.gc();
			    vv.pause();
			    Intent mIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
			    mIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);//画质1    
			    startActivityForResult(mIntent, 1);
			break;
			
		case MENU_DAIL://操作说明
			vv.pause();
			openControlDialog();
			
			break;	
			
		case MENU_EXIT://退出
			Toast.makeText(VideoPlayerActivity.this,"已退出影视播放区！",Toast.LENGTH_LONG).show();
			onDestroy();
			System.gc();
	        finish();
	        
			
			break;
		}
		return true;
	}
	
	/*打开操作说明*/
	private  void openControlDialog(){
		new AlertDialog.Builder(this).setTitle(R.string.about_control).setIcon(R.drawable.logo)
		.setMessage(R.string.control_msg)
		.setPositiveButton(R.string.exit_button, new DialogInterface.OnClickListener(){ 
			public void onClick( DialogInterface diagloginterface , int i){
				vv.start();
			}	
		}).show();
	} 
	/*界面底部的menu菜单实现*/
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, MENU_MUSIC, 0, "播放音乐").setIcon(R.drawable.menu_music);
		menu.add(Menu.NONE, MENU_NEW, 0, "录制视频").setIcon(R.drawable.menu_video);
		menu.add(Menu.NONE, MENU_DAIL, 0, "操作说明").setIcon(R.drawable.help_book);
		menu.add(Menu.NONE, MENU_EXIT, 0, "退  出").setIcon(R.drawable.menu_exit);
		//return true;
		return super.onCreateOptionsMenu(menu); 
	}
}
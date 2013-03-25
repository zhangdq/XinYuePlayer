package Xinyue.all.activity;



import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import music.dreamer.adapter.AlbumListAdapter;
import music.dreamer.adapter.ArtistListAdapter;
import music.dreamer.adapter.MusicListAdapter;
import music.dreamer.useful.DBHelper;
import music.dreamer.useful.MusicService;
import music.dreamer.useful.ScanSdReceiver;
import music.dreamer.useful.SetTimeDialog;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;


/**所有歌曲显示类**/
/*This work comes from Dreamer丶Team. The main programmer is LinShaoHan.
 * QQ:752280466   Welcome to join with us.
 */

public class MainActivity extends TabActivity implements TabHost.TabContentFactory{


	protected void onStart() {
		IntentFilter filter = new IntentFilter();
        filter.addAction(MUSIC_LIST);
		registerReceiver(changeItem, filter);
		super.onStart();
	}
	
	static SharedPreferences sharedpreferences;
	private static final String MUSIC_LIST = "music.dreamer.list";
	private ListView listview;
	private int[] _ids;
	private String[] _titles;
	private String[] _artists;
	private String[] albums;
	private String[] artists;
	private String[] _path;	   //音乐文件的路径
	private int pos;		   //正在播放音乐的位置
	private int num;		   //选择的歌曲的位置
	private  MusicListAdapter adapter;
	public  static int skin=0;
	private static final String MEMORIES_SKIN="MEMORIES_SKIN";
	protected final static int MENU_ABOUT = Menu.FIRST ;
	protected final static int MENU_MORE = Menu.FIRST + 1;
	protected final static int MENU_EXIT = Menu.FIRST+2;
	public static final int RingtongButton=0;
	//private ScanSdReceiver scanSdReceiver = null;
	public static final String TAG = "TestMain" ; 
	public static ArrayList<Activity> activityList = new ArrayList<Activity>();
    public Intent intent_SERVICE;
	static LinearLayout list;
	private AlertDialog ad = null;
	private AlertDialog.Builder  builder = null;
	private Cursor c;
	private String tag;
	/*上下文菜单项*/
	private static final int PLAY_ITEM = Menu.FIRST;
	private static final int DELETE_ITEM = Menu.FIRST+1;
	private static final int SONG_SHARE = Menu.FIRST+2;
	private static final int SET_AS = Menu.FIRST+3;
	//关于音量的变量
	private AudioManager mAudioManager = null;
	private int maxVolume;//最大音量
	private int currentVolume;//当前音量
	public static int time;
	public static String search_music;
	public int id;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activityList.add(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		TabHost th = getTabHost();
		th.addTab(th.newTabSpec("list").setIndicator(
				"音乐列表",getResources().getDrawable(R.drawable.item))
				.setContent(this));
		th.addTab(th.newTabSpec("artists").setIndicator(
				"艺术家",getResources().getDrawable(R.drawable.artist))
				.setContent(this));
		th.addTab(th.newTabSpec("albums").setIndicator(
				"专  辑",getResources().getDrawable(R.drawable.album))
				.setContent(this));
        //Intent一个音乐后台服务，并开启这个服务
       /* intent_SERVICE = new Intent();
        intent_SERVICE.setAction("music.dreamer.media.MUSIC_SERVICE");
        intent_SERVICE.putExtra("list", 1);
        startService(intent_SERVICE);*/
      
	}
	//TabHost标签切片按钮的监听与内容实现
	public View createTabContent(String tag) {
		this.tag = tag;
		if (tag.equals("list")){
			listview = new ListView(this);
			setListData();
		    listview.setOnItemClickListener(new ListItemClickListener());
		    listview.setOnCreateContextMenuListener(new ContextMenuListener());
		} else if (tag.equals("artists")){
			c.moveToFirst();
			int num = c.getCount();
			HashSet set = new HashSet();
			for (int i = 0; i < num; i++){
				String szArtist = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));
				if (szArtist.equals("<unknown>")){
					set.add("未知艺术家");
				}else{
					set.add(szArtist);
				}
				c.moveToNext();
			}
			num = set.size();
			Iterator it = set.iterator();
			artists = new String[num];
			int i = 0;
			while(it.hasNext()){
				artists[i] = it.next().toString();
				i++;
			}
			/*计算每个歌手拥有的歌曲数*/
			int counts[] = new int[num];

			for(int j = 0; j<num; j++){
				c.moveToFirst();
     		for(int k = 0; k < c.getCount(); k++){
					String szArtist = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST)); 
					if (szArtist.equals("<unknown>"))
					{
						szArtist = "未知艺术家";
					}
					if (artists[j].equals(szArtist)){
						counts[j]++;
						//n++;
					}
					c.moveToNext();
				}
			}
			
			listview = new ListView(this);
			listview.setAdapter(new ArtistListAdapter(this, artists,counts));
			listview.setOnItemClickListener(new ArtistsItemClickListener());
		} else if (tag.equals("albums")){
			Cursor c = this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					new String[]{MediaStore.Audio.Media.TITLE,
					MediaStore.Audio.Media.DURATION,
					MediaStore.Audio.Media.ARTIST,
					MediaStore.Audio.Media.ALBUM,
					MediaStore.Audio.Media._ID,
					MediaStore.Audio.Media.DISPLAY_NAME}, 
					null, 
					null,
					MediaStore.Audio.Media.ALBUM);
			c.moveToFirst();
			int num = c.getCount();
			HashSet set = new HashSet();
			for (int i = 0; i < num; i++){
				String szAlbum = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM));
				set.add(szAlbum);
				c.moveToNext();
			}
			num = set.size();
			Iterator it = set.iterator();
			albums = new String[num];
			int i = 0;
			while(it.hasNext()){
				albums[i] = it.next().toString();
				i++;
			}
			String album="";
			for (int j=0;j<num; j++){
				if (j<num-1){
					album = album + "'" + albums[j] + "',"; 
				} else{
					album = album + "'" + albums[j] + "'";
				}
			}
			
			Cursor c1 = this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					new String[]{
					MediaStore.Audio.Media.TITLE,
					MediaStore.Audio.Media.DURATION,
					MediaStore.Audio.Media.ARTIST,
					MediaStore.Audio.Media.ALBUM,
					MediaStore.Audio.Media._ID,
					MediaStore.Audio.Media.DISPLAY_NAME,
					}, 
					null,
					null,
					MediaStore.Audio.Media.ALBUM);
			c1.moveToFirst();
			HashMap<String, String> map = new HashMap<String, String>();
			int num1 = c1.getCount();
			for (int j=0;j<num1;j++){
				map.put(c1.getString(3), c1.getString(2));
				c1.moveToNext();
			}
			listview = new ListView(this);
			listview.setAdapter(new AlbumListAdapter(this, albums,map));
			listview.setOnItemClickListener(new AlbumsItemClickListener());
		}else if (tag.equals("recent")){
			DBHelper dbHelper = new DBHelper(this, "music.db", null, 2);
			Cursor cursor = dbHelper.queryRecently();
			cursor.moveToFirst();
			int num = 0;
			int[] music_id;
			if (cursor!=null){
				num = cursor.getCount();
			} else{
				return null;
			}
			String idString ="";
			if (num>=10){
				for(int i=0;i<10;i++){
					music_id = new int[10];
					music_id[i]=cursor.getInt(cursor.getColumnIndex("music_id"));
					if (i<9){
						idString = idString+music_id[i]+",";
					} else{
						idString = idString+music_id[i];
					}
					cursor.moveToNext();
				} 
			}else if(num>0){
				for(int i=0;i<num;i++){
					music_id = new int[num];
					music_id[i]=cursor.getInt(cursor.getColumnIndex("music_id"));
					if (i<num-1){
						idString = idString+music_id[i]+",";
					} else{
						idString = idString+music_id[i];
					}
					cursor.moveToNext();
				}
			}
			if (cursor!=null){
				cursor.close();
				cursor=null;
			}
			if (dbHelper!=null){
				dbHelper.close();
				dbHelper = null;
			}
			listview = new ListView(this);
			listview.setCacheColorHint(00000000);
			Cursor c = this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
	        			new String[]{MediaStore.Audio.Media.TITLE,
						MediaStore.Audio.Media.DURATION,
						MediaStore.Audio.Media.ARTIST,
						MediaStore.Audio.Media._ID,
						MediaStore.Audio.Media.DISPLAY_NAME,
						MediaStore.Audio.Media.ALBUM_ID,} , MediaStore.Audio.Media._ID + " in ("+ idString + ")", null,null);
			
			  c.moveToFirst();
		      _ids = new int[c.getCount()];
		      _titles = new String[c.getCount()];
		      for(int i=0;i<c.getCount();i++){
		    	  _ids[i] = c.getInt(3);
		          _titles[i] = c.getString(0);
		        	
		          c.moveToNext();
		      }
		      listview.setAdapter(new MusicListAdapter(this, c));
		      listview.setOnItemClickListener(new ListItemClickListener());
		}
		listview.setCacheColorHint(00000000);
        
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE); 
		maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//获得最大音量  
		currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);//获得当前音量 
	    list = new LinearLayout(this);

		  sharedpreferences=getSharedPreferences(MEMORIES_SKIN,0);	
			skin=sharedpreferences.getInt("skin_value",0);
			try{
				//皮肤记录  
				 if(skin==0){
				    	System.gc();
				    	list.setBackgroundResource(R.drawable.listbg);  
				    }
				 else if(skin==1){
				    	System.gc();	
				    	
				    	list.setBackgroundResource(R.drawable.back);  
				    }
				    else if(skin==2){
				    	System.gc();
				    	list.setBackgroundResource(R.drawable.back0);  
				    					
				    }
				    else if(skin==3){
				    	System.gc();		  
				    	list.setBackgroundResource(R.drawable.back1);  
				    }
				    else if(skin==4){
				    	System.gc();
				    	list.setBackgroundResource(R.drawable.back2); 	    	
				    }
				
				    else if(skin==5){
				    	System.gc();
				    	list.setBackgroundResource(R.drawable.back3);  				   
				    }

			      }catch(Exception e){System.out.println(e);}
			
	        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
        LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        list.removeAllViews();
        list.addView(listview,params);
		return list;
	}
	//更换主题的实现方法
	public static void Theme(){
		System.gc();
		skin++;
		if(skin>=6){skin=0;}
	 
		sharedpreferences.edit().putInt("skin_value",skin).commit();	
		skin=sharedpreferences.getInt("skin_value",0);
		 if(skin==0){
		    	System.gc();		    	
		    	list.setBackgroundResource(R.drawable.listbg);  			  
		    }
		 else if(skin==1){
		    	System.gc();
		    	list.setBackgroundResource(R.drawable.back);  
		    }
		    else if(skin==2){
		    	System.gc();
		    	list.setBackgroundResource(R.drawable.back0);  

		    }
		    else if(skin==3){
		    	System.gc();
		    	list.setBackgroundResource(R.drawable.back1);     
		    }
		    else if(skin==4){
		    	System.gc();
		    	list.setBackgroundResource(R.drawable.back2);     
		    }
		
		    else if(skin==5){
		    	System.gc();
		    	list.setBackgroundResource(R.drawable.back3);     
		    }
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case MENU_ABOUT://更多设置内容
			 MoreSetting();						
			break;	
			
		case MENU_MORE://更多相关操作	
			setMore();
			break;
			
		case MENU_EXIT://退出
			/*android.os.Process.killProcess(android.os.Process.myPid());*/
			/*这里被注释掉的语句为系统默认的活动组件销毁语句*/
			System.gc();
			exitApp();
			
			break;
		}
		return true;
	}
	/*界面底部的menu菜单实现*/
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		setMenuBackground();
		menu.add(Menu.NONE, MENU_ABOUT, 0, "设  置").setIcon(R.drawable.common_account);
		menu.add(Menu.NONE, MENU_MORE, 0, "更  多").setIcon(R.drawable.menu_more);
		menu.add(Menu.NONE, MENU_EXIT, 0, "退  出").setIcon(R.drawable.menu_exit);
		//return true;
		return super.onCreateOptionsMenu(menu); 
	}
	
	protected void setMenuBackground() {
		this.getLayoutInflater().setFactory(
		new android.view.LayoutInflater.Factory() {
		    public View onCreateView(String name, Context context,AttributeSet attrs)
		    {
		// 指定自定义inflate的对象
	 	           if (name.equalsIgnoreCase("com.android.internal.view.menu.IconMenuItemView")
		          | name.equalsIgnoreCase("com.android.internal.view.menu.ListMenuItemView")) {
		        try {
		              LayoutInflater f = getLayoutInflater();
	                  final View view = f.createView(name, null,attrs);
		              new Handler().post(new Runnable() {
		            public void run() {
		           // 设置背景图片
	                	view.setBackgroundResource(R.drawable.button);	}});
		                return view;
		              } catch (InflateException e){e.printStackTrace();
		           } catch (ClassNotFoundException e) {
		          e.printStackTrace();
	 	          }
		    }
		return null;
		          }
                              });
		                              }
	/*定义和实现设置睡眠时间的Handler*/
	static Handler handler = new Handler() {
		public void handleMessage(Message msg) {
		//要做的事情
		super.handleMessage(msg);
		android.os.Process.killProcess(android.os.Process.myPid());
		   }
		};
		
	public static class MyThread implements Runnable{
		
		public void run() {
		// TODO Auto-generated method stub
		while (true) {
		try {
		Thread.sleep(time*60000);//线程暂停时间XX分钟
		Message message=new Message();
		message.what=1;
		handler.sendMessage(message);//发送消息
		} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		      }
		    }
		  }
		}

	
		/*更多设置内容*/
	private void MoreSetting() {
			
			 String[] items={
					 "睡眠定时",
					 "设置肤色",
					 "摇甩换曲"
				}; 
			 
			 AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)  
			           .setIcon(R.drawable.music)  
			           .setTitle("相关设置")
			           .setItems(items, onMoreSetting).create();  
			 dialog.show();  
		}
 
	  OnClickListener onMoreSetting = new OnClickListener() {  
		    public void onClick(DialogInterface dialog, int which) {  
		          
		      switch(which)
		      {
		        /*睡眠定时*/
		        case 0:
		        	
		        	SetTimeDialog.getCreatePlaylistDialog(
		        			MainActivity.this).create().show();     	
	        	
					break;
			  case 1://换肤
					Intent intent = new Intent();
		    		intent.setClass(MainActivity.this, ChangeNew.class);
		    		startActivity(intent);
		    		finish();	
					break;
					
			  case 2://摇一摇换曲子
				  moveDialog();			
					break;
		      }
		    
		   }
	    };	
		
		
	//更多的相关操作
	private void setMore() {
		
		 String[] items={			
				 "音乐搜索",
				 "刷新歌曲",
				 "转至影视播放" 
			}; 
		 
		 AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)  
		           .setIcon(R.drawable.music)  
		           .setTitle("更多操作")
		           .setItems(items, onSetMoreSelect).create();  
		 dialog.show();  
	}

 
	OnClickListener onSetMoreSelect = new OnClickListener() {  
	    public void onClick(DialogInterface dialog, int which) {  
	          
	      switch(which)
	      {
	       
				
	        case 0:/*音乐搜索*/
	        	Intent search = new Intent();
				search.setClass(MainActivity.this, SearchActivity.class);
				startActivity(search);
				finish();
				break;
				
	        case 1:/*歌曲刷新*/
	        	ScanSdReceiver scanSdReceiver = null;
	        	IntentFilter intentfilter = new IntentFilter( Intent.ACTION_MEDIA_SCANNER_STARTED);
		    	intentfilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		    	intentfilter.addDataScheme("file");
		    	scanSdReceiver = new ScanSdReceiver();
		    	registerReceiver(scanSdReceiver, intentfilter);
		    	sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
		    	Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath())));

	        	Intent song = new Intent();
	 			song.setClass(MainActivity.this, NewSongs.class);
	 			startActivity(song);
	 			finish();
	    		break;
	    		
	        case 2:/*转至影视播放*/
	        	
	     /*在转至视频播放区之前我们需要判断播放内容是否为空，不为空则将已在后台进行播放的歌曲进行强制停止*/
	        	if (MusicService.mp != null) {
	        		MusicService.mp.stop();	        		
	    		}	        	
	        	Intent intent = new Intent();
				intent.setClass(MainActivity.this, VideoPlayerActivity.class);
				startActivity(intent);			
				Toast.makeText(MainActivity.this,"已切换至影视播放区！",Toast.LENGTH_SHORT).show();
				finish();				
	        	break;

	      }
	    
	   }
    };
    
	/*播放音乐*/
	private void playMusic(int position){
		Intent intent = new Intent(MainActivity.this,MusicActivity.class);
		intent.putExtra("_ids", _ids);
		intent.putExtra("_titles", _titles);
		intent.putExtra("_artists", _artists);
		intent.putExtra("position", position);
		startActivity(intent);
		finish();
	}
	
	/*从列表中删除选中的音乐*/
	private void deleteMusic(int position){
		this.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
				MediaStore.Audio.Media._ID + "=" + _ids[position], 
				null);
	}
	
	/*从sdcard中删除选中的音乐*/
	private void deleteMusicFile(int position){
		File file = new File(_path[position]);
		System.out.println(_path[position]);
		try {
			deleteFile(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//删除歌曲文件的方法
	public static void deleteFile(File f) throws Exception {
		if (f.isFile()) {
			if (f.canWrite()) {
				f.delete();
			} else {
				throw new Exception("文件：" + f.getName() + "只读，无法删除，请手动删除");
			}
		} else {
			File[] fs = f.listFiles();
			if (fs.length != 0) {
				for (int i = 0; i < fs.length; i++) {
					deleteFile(fs[i]);
				}
			}
		}
	}
	class ListItemClickListener implements OnItemClickListener{

    	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
    		// TODO Auto-generated method stub
    		playMusic(position);
    	}
    	
    }
	/*启动艺术家音乐列表的intent对象的实现*/
	class ArtistsItemClickListener implements OnItemClickListener{

    	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
    		Intent intent = new Intent();
    		intent.setClass(MainActivity.this, ArtistActivity.class);
    		intent.putExtra("artist", artists[position]);
    		startActivity(intent);
    	}
    }
	
	/*启动专辑音乐列表的intent对象的实现*/
	class AlbumsItemClickListener implements OnItemClickListener{
	
    	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
    		Intent intent = new Intent();
    		intent.setClass(MainActivity.this, AlbumActivity.class);
    		intent.putExtra("albums", albums[position]);
    		startActivity(intent);
    	}
    }
	/*摇一摇换曲子设置*/
	private  void moveDialog(){
		LayoutInflater in = MainActivity.this.getLayoutInflater();
		final View alarm = in.inflate(R.layout.share, null);
		
		new AlertDialog.Builder(this)
				.setTitle("摇甩换歌曲")
				.setIcon(R.drawable.moving)
				.setView(alarm)
				.setPositiveButton("开启",
						new DialogInterface.OnClickListener() {

						
							public void onClick(DialogInterface dialog,
									int which) {	
								  MusicService.moveshare=true;
									Toast.makeText(MainActivity.this,
											"已开启摇甩换歌曲功能,请跳转至播放页面进行激活！", Toast.LENGTH_LONG)
											.show();
							} 
					
						}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

							
							public void onClick(DialogInterface dialog,
									int which) {		
								    MusicService.moveshare=false;
								    MusicService.sd.stop();
									Toast.makeText(MainActivity.this,
											"已取消摇甩换歌曲功能！", Toast.LENGTH_LONG)
											.show();
							} 
					
						}).show();

	                            }

	 /*完全退出应用程序 */
	public void exitApp() { 
		AlertDialog.Builder builder = new AlertDialog.Builder(this); 
		builder.setTitle("温馨提示"); 
		builder.setIcon(R.drawable.dialog_alert_icon);
		builder.setMessage("您确定要退出欣悦影音中的音乐播放模式吗?"); 
		builder.setPositiveButton("确定", new OnClickListener() {
		    public void onClick(DialogInterface dialog, int which) { 
		         if (activityList.size() > 0) { 
		         for (Activity activity : activityList) { 
		               System.gc();
		               activity.finish(); 
		              
		               } 
		         try {
		        	 if(MusicService.mp!=null){
		        		 MusicService.nm.cancelAll();
				 		 MainActivity.this.stopService(intent_SERVICE); 
		        	 }
		        	
		 		} catch (Exception e) {
		 			// TODO Auto-generated catch block
		 			e.printStackTrace();
		 		}
		      android.os.Process.killProcess(android.os.Process.myPid()); 
		          } 
		      } 
		 });
		builder.setNegativeButton("取消", null); 
		builder.show(); 
	}

	@Override 
	protected void onDestroy() { 
	        super.onDestroy(); 
	        activityList.remove(this); 
	       Log.i(TAG, activityList.toString()) ; 
	}
     /*音量的调节实现*/
	public boolean dispatchKeyEvent(KeyEvent event) { 
		int action = event.getAction(); 
		int keyCode = event.getKeyCode(); 
		switch (keyCode) { 
			case KeyEvent.KEYCODE_VOLUME_UP: 
			if (action == KeyEvent.ACTION_UP) {
				if (currentVolume<maxVolume){
					currentVolume = currentVolume + 1;
					mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
				} else {
					mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
				}
			} 
			return false; 
			case KeyEvent.KEYCODE_VOLUME_DOWN: 
			if (action == KeyEvent.ACTION_UP) { 
				if (currentVolume>0){
					currentVolume = currentVolume - 1;
					mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume , 0);
				} else {
					mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
				}
			} 
			return false; 
			default: 
			return super.dispatchKeyEvent(event); 
		} 
	}
    private BroadcastReceiver changeItem = new BroadcastReceiver() {
		
		
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(MUSIC_LIST)){
				pos = intent.getExtras().getInt("position");
				adapter.setItemIcon(pos);//改变列表项图标
				adapter.notifyDataSetChanged();//通知UI更新
				System.out.println("List Update...");
			}
			
		}
	};
	
	private void setListData(){
		c = this.getContentResolver()
		.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[]{
				MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.DURATION,
				MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.DISPLAY_NAME,
				MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.ALBUM_ID},
				null, null, null);
	    if (c==null || c.getCount()==0){
	    	builder = new AlertDialog.Builder(this);
			builder.setMessage("很抱歉，音乐库内容为空！").setPositiveButton("确定", null);
			ad = builder.create();
			ad.show();
			return;
	    }
	    c.moveToFirst();
	    _ids = new int[c.getCount()];
	    _titles = new String[c.getCount()];
	    _artists = new String[c.getCount()];
	    _path = new String[c.getCount()];
	    for(int i=0;i<c.getCount();i++){
	    	_ids[i] = c.getInt(3);
	    	_titles[i] = c.getString(0);
	    	_artists[i] = c.getString(2);
	    	_path[i] = c.getString(5).substring(4);
	    	c.moveToNext();
	    }
	    adapter = new MusicListAdapter(this, c);
	    listview.setAdapter(adapter);
	}
	
	
	protected void onStop() {
		super.onStop();
		unregisterReceiver(changeItem);
	}
	
	/*创建上下文菜单监听器*/
	class ContextMenuListener implements OnCreateContextMenuListener{
		
		public void onCreateContextMenu(ContextMenu menu, View view,
				ContextMenuInfo info) {
			if (tag.equals("list")){
				menu.setHeaderTitle("相关操作");
				menu.setHeaderIcon(R.drawable.music);
				menu.add(0, PLAY_ITEM, 0, "播放");
				menu.add(0, DELETE_ITEM, 0, "删除");
				menu.add(0, SONG_SHARE, 0, "分享");
				menu.add(0, SET_AS, 0, "音乐设定操作");
				final AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) info;
				num = menuInfo.position;
			}
		}
	}
	
	//指定音乐设置操作
	private void setEffects() {
		
		String[] items = { "设置为来电铃声", "设置为通知铃声", "设置为闹钟铃声" };
		 AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)  
		           .setIcon(R.drawable.music)  
		           .setTitle("音乐设定操作")
		           .setItems(items, onSetEffectsSelect).create();  
		 dialog.show();  
	}
	
	OnClickListener onSetEffectsSelect = new OnClickListener() {  
	    public void onClick(DialogInterface dialog, int which) {  
	          
	      switch(which)
	      {
	        /*设置---铃声*/
	        case 0:
	        	try{	
	        		setMyRingtone(num);
	        	}
	        	catch(Exception e)
	        	{
	        		Log.v(TAG, "This is my ringset!");
	        	}
	    		break;	    	
	    		/*设置---提示音*/
	        case 1:
	        	
	        	setMyNotification(num);
				break;
             /*设置---闹铃音*/
	        case 2:
	        	setMyAlarm(num);
	    		break;
	      }
	    
	   }
    };
	//设置--铃声的具体方法
    public void setMyRingtone(int position)  
    {  

      File sdfile = new File(_path[position]);
      ContentValues values = new ContentValues();  
      values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());  
      values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
      values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");   
      values.put(MediaStore.Audio.Media.IS_RINGTONE, true);  
      values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);  
      values.put(MediaStore.Audio.Media.IS_ALARM, false);  
      values.put(MediaStore.Audio.Media.IS_MUSIC, false);  
      
      Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile.getAbsolutePath());
      //Uri uri = MediaStore.Audio.Media.getContentUriForPath(_path[position]);
      Uri newUri = this.getContentResolver().insert(uri, values);
      RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, newUri); 
      Toast.makeText( getApplicationContext (),"设置来电铃声成功！", Toast.LENGTH_SHORT ).show(); 
      System.out.println("setMyRingtone()-----铃声");
    }  

  //设置--提示音的具体实现方法
    public void setMyNotification(int position)  
    { 
   
      File sdfile = new File(_path[position]);   
      ContentValues values = new ContentValues();  
      values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());  
      values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
      values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");   
      values.put(MediaStore.Audio.Media.IS_RINGTONE, false);  
      values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);  
      values.put(MediaStore.Audio.Media.IS_ALARM, false);  
      values.put(MediaStore.Audio.Media.IS_MUSIC, false);  
      
      Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile.getAbsolutePath());
      Uri newUri = this.getContentResolver().insert(uri, values);
      
      RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION, newUri); 
      Toast.makeText( getApplicationContext (),"设置通知铃声成功！", Toast.LENGTH_SHORT ).show();
      System.out.println("setMyNOTIFICATION-----提示音");
    } 
  //设置--闹铃音的具体实现方法
  public void setMyAlarm(int position)  
    {  
      File sdfile = new File(_path[position]);   
      ContentValues values = new ContentValues();  
      values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());  
      values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
      values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");   
      values.put(MediaStore.Audio.Media.IS_RINGTONE, false);  
      values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);  
      values.put(MediaStore.Audio.Media.IS_ALARM, true);  
      values.put(MediaStore.Audio.Media.IS_MUSIC, false);  
      
      Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile.getAbsolutePath());
      Uri newUri = this.getContentResolver().insert(uri, values);
      RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM, newUri); 
      Toast.makeText( getApplicationContext (),"设置闹钟铃声成功！", Toast.LENGTH_SHORT ).show();
      System.out.println("setMyNOTIFICATION------闹铃音");
    }  

	/*分享选中的音乐*/
	private void ShareMusicFile(int position){

		 Intent intent=new Intent(Intent.ACTION_SEND);  
			//intent.setType("text/plain");  
			 intent.setType("audio/*");
			 File file = new File(_path[position]);
			 Uri u = Uri.fromFile(file);
			 intent.putExtra(Intent.EXTRA_STREAM, u);
			 System.out.println(_path[position]);
			 intent.putExtra(Intent.EXTRA_SUBJECT, "分享");  
			 intent.putExtra(Intent.EXTRA_TEXT, "歌曲分享    (来自Dreamer开发小组，欢迎使用欣悦影音播放器)");  
			 startActivity(Intent.createChooser(intent, getTitle()));  
	   
	}
	/*上下文菜单的某一项被点击时回调该方法*/
	
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case PLAY_ITEM:									//开始播放
			playMusic(num);
			break;

		case DELETE_ITEM:								//删除一首歌曲
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("您确定要从音乐库中删除这首歌曲吗?")
			.setPositiveButton("是", new DialogInterface.OnClickListener() {
				
				
				public void onClick(DialogInterface dialog, int which) {
					deleteMusic(num);					//从列表中删除音乐
					deleteMusicFile(num);				//从sdcard中删除音乐
					setListData();						//从新获得列表中药显示的数据
					adapter.notifyDataSetChanged();		//更新列表UI
				}
			})
			.setNegativeButton("否", null);
			AlertDialog ad = builder.create();
			ad.show();
			break;
			
		case SONG_SHARE://分享被选中的歌曲
			ShareMusicFile(num);
			break;
		case SET_AS: //将选中的歌曲设置为....
			setEffects();
			break;
		}
		return true;
	}

}

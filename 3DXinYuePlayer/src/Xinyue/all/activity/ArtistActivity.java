package Xinyue.all.activity;

import java.io.File;

import music.dreamer.adapter.MusicListAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
/**艺术家类*/
/*This work comes from Dreamer丶Team. The main programmer is LinShaoHan.
 * QQ:752280466   Welcome to join with us.
 */
public class ArtistActivity extends Activity {
	private int[] _ids;
	private String[]_titles;
	private String[]_artists;
	private String[] _path;									//音乐文件的路径
	private ListView listview;
	private int pos;
	private String artistName;
	private  MusicListAdapter adapter;
	
	/*上下文菜单项*/
	private static final int PLAY_ITEM = Menu.FIRST;
	private static final int DELETE_ITEM = Menu.FIRST+1;
	private static final int SONG_SHARE = Menu.FIRST+2;
	private static final int SET_AS = Menu.FIRST+3;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = this.getIntent();
		if (intent.getExtras().getString("artist").equals("未知艺术家")){
			artistName = "<unknown>";
		} else{
			artistName = intent.getExtras().getString("artist");
		}
		listview = new ListView(this);
		setListData();
	    listview.setOnItemClickListener(new ListItemClickListener());
	    listview.setOnCreateContextMenuListener(new ContextMenuListener());
	    LinearLayout list = new LinearLayout(this);
	    //list.setBackgroundResource(R.drawable.back0);
	    try{
			//皮肤记录 
	    if(MainActivity.skin==0){
	    	System.gc();
	    	list.setBackgroundResource(R.drawable.listbg);  
	    }
	    else if(MainActivity.skin==1){
	    	System.gc();	
	    	list.setBackgroundResource(R.drawable.back);  
	
	    }
	    else if(MainActivity.skin==2){
	    	System.gc();
	    	list.setBackgroundResource(R.drawable.back0);  
		
	    }
	    else if(MainActivity.skin==3){
	    	System.gc();		  
	    	list.setBackgroundResource(R.drawable.back1);  
		
	    }
	    else if(MainActivity.skin==4){
	    	System.gc();
	    	list.setBackgroundResource(R.drawable.back2);     
	    }
	
	    else if(MainActivity.skin==5){
	    	System.gc();
	    	list.setBackgroundResource(R.drawable.back3);     
	      }

        }catch(Exception e){System.out.println(e);}

	    /*这里的setBackgroundResource设置的是显示指定艺术家的所有歌曲显示列表的背景*/
	    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
	    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
	    list.addView(listview,params);
	    setContentView(list);
	}
	
	/*播放选中的音乐*/
	private void playMusic(int position){
		Intent intent = new Intent(ArtistActivity.this,MusicActivity.class);
		intent.putExtra("_ids", _ids);
		intent.putExtra("_titles", _titles);
		intent.putExtra("position", position);
		intent.putExtra("_artists", _artists);
		
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
		File file = new File(_path[pos]);
		try {
			MainActivity.deleteFile(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	 class ListItemClickListener implements OnItemClickListener{

	 
	    	public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
	    		// TODO Auto-generated method stub
	    		playMusic(position);
	    	}
	 }
	 /*创建上下文菜单监听器*/
		class ContextMenuListener implements OnCreateContextMenuListener{
		
			public void onCreateContextMenu(ContextMenu menu, View view,
					ContextMenuInfo info) {
				menu.setHeaderTitle("相关操作");
				menu.setHeaderIcon(R.drawable.music);
				menu.add(0, PLAY_ITEM, 0, "播放");
				menu.add(0, DELETE_ITEM, 0, "删除");
				menu.add(0, SONG_SHARE, 0, "分享");
				menu.add(0, SET_AS, 0, "音乐设定操作");
				final AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) info;
				pos = menuInfo.position;
			}
		}
		/*分享选中的音乐*/
		private void ShareMusicFile(int position){

			 Intent intent=new Intent(Intent.ACTION_SEND);  
				//intent.setType("text/plain");  
				 intent.setType("audio/*");
				 File file = new File(_path[pos]);
				 Uri u = Uri.fromFile(file);
				 intent.putExtra(Intent.EXTRA_STREAM, u);
				 System.out.println(_path[position]);
				 intent.putExtra(Intent.EXTRA_SUBJECT, "分享");  
				 intent.putExtra(Intent.EXTRA_TEXT, "歌曲分享    (来自Dreamer开发小组，欢迎使用欣悦影音播放器)");  
				 startActivity(Intent.createChooser(intent, getTitle()));  
		   
		}
		//指定音乐设置操作
		private void setEffects() {
			
			String[] items = { "设置为来电铃声", "设置为通知铃声", "设置为闹钟铃声" };
			 	
			 AlertDialog dialog = new AlertDialog.Builder(ArtistActivity.this)  
			           .setIcon(R.drawable.isplaying)  
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
		        	
		        		setMyRingtone(pos);
		        
		    		break;	    	
		    		/*设置---提示音*/
		        case 1:
		        	
		        	setMyNotification(pos);
					break;
					
	             /*设置---闹铃音*/
		        case 2:
		        	setMyAlarm(pos);
		    		break;
		      }
		    
		   }
	    };
		//设置--铃声的具体方法
	    public void setMyRingtone(int position)  
	    {  

	      File sdfile = new File(_path[pos]);
	      ContentValues values = new ContentValues();  
	      values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());  
	      values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
	      values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");   
	      values.put(MediaStore.Audio.Media.IS_RINGTONE, true);  
	      values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);  
	      values.put(MediaStore.Audio.Media.IS_ALARM, false);  
	      values.put(MediaStore.Audio.Media.IS_MUSIC, false);  
	      
	      Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile.getAbsolutePath());
	      Uri newUri = this.getContentResolver().insert(uri, values);
	      RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, newUri); 
	      Toast.makeText( getApplicationContext (),"设置来电铃声成功！", Toast.LENGTH_SHORT ).show(); 
	      System.out.println("setMyRingtone()-----铃声");
	    }  

	  //设置--提示音的具体实现方法
	    public void setMyNotification(int position)  
	    { 
	   
	      File sdfile = new File(_path[pos]);   
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
	    
	      File sdfile = new File(_path[pos]);   
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
	    
		
		/*上下文菜单的某一项被点击时回调该方法*/
		@Override
		public boolean onContextItemSelected(MenuItem item) {
			switch (item.getItemId()) {
			case PLAY_ITEM:									//开始播放
				playMusic(pos);
				break;

			case DELETE_ITEM:								//删除一首歌曲
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("您确定要从音乐库中删除这首歌曲吗？")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
			
					public void onClick(DialogInterface dialog, int which) {
						deleteMusic(pos);					//从列表中删除音乐
						deleteMusicFile(pos);				//从sdcard中删除音乐
						setListData();						//从新获得列表中药显示的数据
						adapter.notifyDataSetChanged();		//更新列表UI
					}
				})
				.setNegativeButton("否", null);
				AlertDialog ad = builder.create();
				ad.show();
				break;
			case SONG_SHARE://分享艺术家列表里被选中的歌曲
				ShareMusicFile(pos);
				break;
				
			case SET_AS://将艺术家列表中的被选中歌曲设置为...
				setEffects();
				break;
			}
			return true;
		}
	 
		private void setListData(){
			Cursor c = this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					new String[]{MediaStore.Audio.Media.TITLE,
					MediaStore.Audio.Media.DURATION,
					MediaStore.Audio.Media.ARTIST,
					MediaStore.Audio.Media._ID,
					MediaStore.Audio.Media.DISPLAY_NAME,
					MediaStore.Audio.Media.DATA,
					MediaStore.Audio.Media.ALBUM_ID,}, 
					MediaStore.Audio.Media.ARTIST + "='" + artistName+"'", 
					null,
					null);
		   c.moveToFirst();
		   _ids = new int[c.getCount()];
		   _titles = new String[c.getCount()];
		   _artists = new String[c.getCount()];
		   _path = new String[c.getCount()];
		   for(int i=0;i<c.getCount();i++){
		       _ids[i] = c.getInt(3);
		       _artists[i] = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));
		       _titles[i] = c.getString(0);
		       _path[i] = c.getString(5).substring(4);
		       c.moveToNext();
		  }
		   adapter = new MusicListAdapter(this, c);
		   listview.setAdapter(adapter);
		}
}

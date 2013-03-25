package music.dreamer.useful;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import video.dreamer.views.VideoView;
import music.dreamer.useful.ShakeDetector;
import music.dreamer.useful.ShakeDetector.OnShakeListener;
import Xinyue.all.activity.MusicActivity;
import Xinyue.all.activity.R;
import Xinyue.all.activity.VideoPlayerActivity;
import android.telephony.TelephonyManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;

/** 支持后台音乐的声音播放提供服务类 */
/*
 * This work comes from Dreamer丶Team. The main programmer is LinShaoHan.
 * QQ:752280466 Welcome to join with us.
 */
public class MusicService extends Service implements
		MediaPlayer.OnCompletionListener {
	public static  TelephonyManager tm;
	private static final String MUSIC_CURRENT = "music.dreamer.currentTime";
	private static final String MUSIC_DURATION = "music.dreamer.duration";
	private static final String MUSIC_NEXT = "music.dreamer.next";
	private static final String MUSIC_UPDATE = "music.dreamer.update";
	private static final String MUSIC_LIST = "music.dreamer.list";
	private static final int MUSIC_PLAY = 1;
	private static final int MUSIC_PAUSE = 2;
	private static final int MUSIC_STOP = 3;
	private static final int PROGRESS_CHANGE = 4;
	private static final int MUSIC_REWIND = 5;
	private static final int MUSIC_FORWARD = 6;
	public static  MediaPlayer mp = null;
	
	int progress;
	private Uri uri = null;
	private int id = 10000;
	private Handler handler = null;
	private Handler rHandler = null;
	private Handler fHandler = null;
	private int currentTime;
	private int duration;
	private DBHelper dbHelper = null;
	private int flag;
	private int position;
	private int _ids[];
	private String _titles[];
	private String _artists[];
	private int _id;
	private String _title;
	public static Notification notification;
	public static NotificationManager nm;
	public static ShakeDetector sd;
	public static boolean moveshare=false;

	
	public void onCreate() {
		super.onCreate();
		tm = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
		if (mp != null) {
			mp.reset();
			mp.release();
			mp = null;
		}
		mp = new MediaPlayer();
		mp.setOnCompletionListener(this);
		nm = (NotificationManager) getApplicationContext().getSystemService(
				Context.NOTIFICATION_SERVICE);

		rHandler = new Handler();
		fHandler = new Handler();
		rHandler.removeCallbacks(rewind);
		fHandler.removeCallbacks(forward);

		IntentFilter filter1 = new IntentFilter();
		filter1.addAction("music.dreamer.playmusic");
		filter1.addAction("music.dreamer.nextone");
		filter1.addAction("music.dreamer.lastone");
		filter1.addAction("music.dreamer.startapp");
		registerReceiver(appWidgetReceiver, filter1);
		
		IntentFilter phone = new IntentFilter();
		phone.addAction("android.intent.action.NEW_OUTGOING_CALL");
		phone.addAction("android.intent.action.PHONE_STATE");
		registerReceiver(phoneReceiver, phone);

		sd = new ShakeDetector(getApplicationContext());

	}

	/* Handler线程及一切正处运行的播放媒体的运行销毁 */
	public void onDestroy() {
		super.onDestroy();
		nm.cancelAll();
		System.out.println("Service destroy!");
		if (mp != null) {
			mp.stop();
			mp = null;
		}
		if (dbHelper != null) {
			dbHelper.close();
			dbHelper = null;
		}
		if (handler != null) {
			handler.removeMessages(1);
			handler = null;
		}

	}

	/* 启动后台播放 */
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	    
		if ((flag == 0) && (intent.getExtras().getInt("list") == 1)) {
			//System.out.println("Service flag=0");
			return;
		}
		if (intent.getIntArrayExtra("_ids") != null) {
			_ids = intent.getIntArrayExtra("_ids");
			_titles = intent.getStringArrayExtra("_titles");
			_artists = intent.getStringArrayExtra("_artists");
		}
		int position1 = intent.getIntExtra("position", -1);
		if (position1 != -1) {
			position = position1;
			_id = _ids[position];
			_title = _titles[position];
		}
		int length = intent.getIntExtra("length", -1);
		if (_id != -1) {
			if (id != _id) {
				id = _id;
				
				uri = Uri.withAppendedPath(
						MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + _id);
				DBOperate(_id);
				try {
					mp.reset();
					mp.setDataSource(this, uri);

				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (length == 1) {

				try {
					mp.reset();
					mp.setDataSource(this, uri);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		setup();
		init();
		if (position != -1) {
			Intent intent1 = new Intent();
			intent1.setAction(MUSIC_LIST);
			intent1.putExtra("position", position);
			sendBroadcast(intent1);
			Intent intent2 = new Intent("music.dreamer.musictitle");
			intent2.putExtra("title", _title);
			sendBroadcast(intent2);
			Intent playIntent = new Intent("music.dreamer.play");
			sendBroadcast(playIntent);
		}

		int op = intent.getIntExtra("op", -1);
		if (op != -1) {
			switch (op) {
			case MUSIC_PLAY:// 播放
				if (!mp.isPlaying()) {
					play();
				}
				break;
			case MUSIC_PAUSE:// 暂停播放
				if (mp.isPlaying()) {
					pause();
				}
				break;
			case MUSIC_STOP:// 音乐的停止播放
				stop();
				break;
			case PROGRESS_CHANGE:
				currentTime = intent.getExtras().getInt("progress");
				mp.seekTo(currentTime);

				break;
			case MUSIC_REWIND:
				rewind();
				break;
			case MUSIC_FORWARD:
				forward();
				break;
			}
		}
		int pos = intent.getIntExtra("position", -1);
		// 手机摇晃监听
		sd.registerOnShakeListener(new OnShakeListener() {

			public void onShake() {
				    Random random = new Random();
				    position = random.nextInt(_ids.length - 1);
					Intent inte = new Intent("music.dreamer.lastone");
					inte.putExtra("position", position);
					inte.putExtra("_titles", _titles);
					sendBroadcast(inte);			
					Intent playIntent = new Intent("music.dreamer.play");
					sendBroadcast(playIntent);
			}
		});
		if(moveshare==true){
			sd.start();
		}
		else {
			moveshare=false;
			return;
		}
		mp.setOnCompletionListener(new OnCompletionListener() {

			public void onCompletion(MediaPlayer mp) {
				
				Random random = new Random();
				position = random.nextInt(_ids.length - 1);
				Intent inte = new Intent("music.dreamer.lastone");
				inte.putExtra("position", position);
				inte.putExtra("_titles", _titles);
				sendBroadcast(inte);
				Intent playIntent = new Intent("music.dreamer.play");
				sendBroadcast(playIntent);
			}
		});
		position=pos;
	    
	}

	public IBinder onBind(Intent intent) {
		return null;
	}

	// 播放
	private void play() {
		if (mp != null) {
			notification = new Notification();
			notification.icon = R.drawable.music;
			notification.defaults = Notification.DEFAULT_LIGHTS;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.when = System.currentTimeMillis();

			notification.tickerText = _title;
			Intent intent2 = new Intent(getApplicationContext(),
					MusicActivity.class);
			intent2.putExtra("_ids", _ids);
			intent2.putExtra("_titles", _titles);
			intent2.putExtra("position", position);
			intent2.putExtra("_artists", _artists);
			PendingIntent contentIntent = PendingIntent.getActivity(
					getApplicationContext(), 0, intent2,
					PendingIntent.FLAG_UPDATE_CURRENT);
			notification.setLatestEventInfo(getApplicationContext(), 
					"欣悦影音   酷乐随行",_title, contentIntent);
			nm.notify(0, notification);

			mp.start();
			
		}
		flag = 1;
		rHandler.removeCallbacks(rewind);
		fHandler.removeCallbacks(forward);
		
	}
	

	// 暂停
	private void pause() {
		if (mp != null) {
			mp.pause();
		}
		flag = 1;
	}

	private void stop() {
		if (mp != null) {
			mp.stop();
			try {
				mp.prepare();
				mp.seekTo(0);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			handler.removeMessages(1);
			rHandler.removeCallbacks(rewind);
			fHandler.removeCallbacks(forward);
		}
	}

	private void init() {
		final Intent intent = new Intent();
		intent.setAction(MUSIC_CURRENT);
		if (handler == null) {
			handler = new Handler() {

				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					if (msg.what == 1) {
						if (flag == 1) {
							currentTime = mp.getCurrentPosition();
							intent.putExtra("currentTime", currentTime);
							sendBroadcast(intent);
						}
						handler.sendEmptyMessageDelayed(1, 600);
					}
				}
			};
		}
	}

	private void setup() {
		final Intent intent = new Intent();
		intent.setAction(MUSIC_DURATION);
		try {
			if (!mp.isPlaying()) {
				mp.prepare();
			}
			mp.setOnPreparedListener(new OnPreparedListener() {

				public void onPrepared(MediaPlayer mp) {
					handler.sendEmptyMessage(1);
				}
			});
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		duration = mp.getDuration();
		intent.putExtra("duration", duration);
		sendBroadcast(intent);
	}

	private void rewind() {
		rHandler.post(rewind);
	}

	private void forward() {
		fHandler.post(forward);
	}

	Runnable rewind = new Runnable() {

		public void run() {
			if (currentTime >= 0) {
				currentTime = currentTime - 5000;
				mp.seekTo(currentTime);
				rHandler.postDelayed(rewind, 500);
			}

		}
	};

	Runnable forward = new Runnable() {

		public void run() {
			if (currentTime <= duration) {
				currentTime = currentTime + 5000;
				mp.seekTo(currentTime);
				fHandler.postDelayed(forward, 500);
			}
		}
	};

	// 获取歌曲的随机播放位置
	private int getRandomPostion(boolean loopAll) {
		int ret = -1;

		if (MusicActivity.randomNum < _ids.length - 1) {
			MusicActivity.randomIDs[MusicActivity.randomNum] = position;
			ret = MusicActivity.findRandomSound(_ids.length);
			MusicActivity.randomNum++;// 对播放页面中的随机数进行追加

		} else if (loopAll == true) {
			MusicActivity.randomNum = 0;
			for (int i = 0; i < _ids.length; i++) {
				MusicActivity.randomIDs[i] = -1;
			}
			MusicActivity.randomIDs[MusicActivity.randomNum] = position;
			ret = MusicActivity.findRandomSound(_ids.length);
			MusicActivity.randomNum++;
		}

		return ret;
	}

	// 下一首歌曲
	private void nextOne() {
		if (_ids.length == 1
				|| MusicActivity.loop_flag == MusicActivity.LOOP_ONE) {
			position = position;

		} else if (MusicActivity.loop_flag == MusicActivity.LOOP_ALL) {
			if (MusicActivity.random_flag == true) {
				int i = getRandomPostion(true);
				if (i == -1) {
					stop();
					return;
				} else {
					position = i;
				}
			} else {
				if (position == _ids.length - 1) {
					position = 0;
				} else if (position < _ids.length - 1) {
					position++;
				}
			}

		} else if (MusicActivity.loop_flag == MusicActivity.LOOP_NONE) {
			if (MusicActivity.random_flag == true) {// 设定歌曲的随机出现值
				int i = getRandomPostion(false);
				if (i == -1) {
					stop();
					return;
				} else {
					position = i;
				}
			} else {
				if (position == _ids.length - 1) {
					stop();
					return;
				} else if (position < _ids.length - 1) {
					position++;
				}
			}
		}
		uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				"" + _ids[position]);
		DBOperate(_ids[position]);
		id = _ids[position];
		_title = _titles[position];
		_id = id;
		try {
			mp.reset();
			mp.setDataSource(this, uri);

		} catch (Exception e) {
			e.printStackTrace();
		}
		handler.removeMessages(1);
		rHandler.removeCallbacks(rewind);
		fHandler.removeCallbacks(forward);
		setup();
		init();
		play();

		Intent intent = new Intent();
		intent.setAction(MUSIC_LIST);
		intent.putExtra("position", position);
		sendBroadcast(intent);

		Intent intent1 = new Intent();
		intent1.setAction(MUSIC_UPDATE);
		intent1.putExtra("position", position);
		sendBroadcast(intent1);

		Intent intent2 = new Intent("music.dreamer.musictitle");
		intent2.putExtra("title", _title);
		sendBroadcast(intent2);

	}

	// 上一首歌曲
	private void lastOne() {
		if (_ids.length == 1) {
			position = position;

		} else if (position == 0) {
			position = _ids.length - 1;
		} else if (position > 0) {
			position--;
		}
		uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				"" + _ids[position]);
		DBOperate(_ids[position]);
		id = _ids[position];
		_title = _titles[position];
		_id = id;
		try {
			mp.reset();
			mp.setDataSource(this, uri);

		} catch (Exception e) {
			e.printStackTrace();
		}
		handler.removeMessages(1);
		rHandler.removeCallbacks(rewind);
		fHandler.removeCallbacks(forward);
		setup();
		init();
		play();

		Intent intent = new Intent();
		intent.setAction(MUSIC_LIST);
		intent.putExtra("position", position);
		sendBroadcast(intent);

		Intent intent1 = new Intent();
		intent1.setAction(MUSIC_UPDATE);
		intent1.putExtra("position", position);
		sendBroadcast(intent1);

		Intent intent2 = new Intent("music.dreamer.musictitle");
		intent2.putExtra("title", _title);
		sendBroadcast(intent2);
	}

	
	public void onCompletion(MediaPlayer mp) {
		nextOne();
	}

	private void DBOperate(int pos) {
		dbHelper = new DBHelper(this, "music.db", null, 2);
		Cursor c = dbHelper.query(pos);
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		if (c == null || c.getCount() == 0) {// 如果获取的内容为空则重新获取歌曲的详情内容
			ContentValues values = new ContentValues();
			values.put("music_id", pos);
			values.put("clicks", 1);
			values.put("latest", dateString);
			dbHelper.insert(values);
		} else {
			c.moveToNext();
			int clicks = c.getInt(2);
			clicks++;
			ContentValues values = new ContentValues();
			values.put("clicks", clicks);
			values.put("latest", dateString);
			dbHelper.update(values, pos);
		}
		if (c != null) {
			c.close();
			c = null;
		}
		if (dbHelper != null) {
			dbHelper.close();
			dbHelper = null;
		}
	}


	/* 实例化一个广播接受者对象phoneReceiver，并实现对电话通话状态的事件处理 */
	private BroadcastReceiver phoneReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			if (VideoPlayerActivity.uri != null) {
				VideoPlayerActivity.vv.pause();
		
			}
			if (mp != null) {
				mp.pause();
		       
			}
			tm.listen(new PhoneStateListener() {

				@Override
				public void onCallStateChanged(int state, String incomingNumber) {

					switch (state) {
					case TelephonyManager.CALL_STATE_OFFHOOK:
						// 接听
				
						break;

					case TelephonyManager.CALL_STATE_IDLE:
						//play();
						// 挂断
						break;
			
					}
				}
			}, PhoneStateListener.LISTEN_CALL_STATE);
		}

	};

	

	// 实例化一个广播接收对象appWidgetReceiver，并实现其相应的歌曲状态运行
	protected BroadcastReceiver appWidgetReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals("music.dreamer.playmusic")) {
				if (mp.isPlaying()) {
					pause();
					Intent pauseIntent = new Intent("music.dreamer.pause");
					sendBroadcast(pauseIntent);
				} else {
					play();
					Intent playIntent = new Intent("music.dreamer.play");
					sendBroadcast(playIntent);
				}
			} else if (intent.getAction().equals("music.dreamer.nextone")) {
				nextOne();
				Intent playIntent = new Intent("music.dreamer.play");
				sendBroadcast(playIntent);
			} else if (intent.getAction().equals("music.dreamer.lastone")) {
				lastOne();
				Intent playIntent = new Intent("music.dreamer.play");
				sendBroadcast(playIntent);
			} else if (intent.getAction().equals("music.dreamer.startapp")) {
				Intent intent1 = new Intent("music.dreamer.musictitle");
				intent1.putExtra("title", _title);
				sendBroadcast(intent1);
			} 
		}
	};

}

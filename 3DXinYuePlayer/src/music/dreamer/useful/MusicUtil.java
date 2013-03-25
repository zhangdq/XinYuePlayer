package music.dreamer.useful;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;




import Xinyue.all.activity.R;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;

public class MusicUtil {
	private Context context;

	public MusicUtil(Context context) {
		super();
		this.context = context;

	}

	// 获取音乐文件
	public List<MusicSet> getMusic() {
		List<MusicSet> musics = new ArrayList<MusicSet>();
		ContentResolver cr = context.getContentResolver();
		Cursor c = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
				null, null, null);
		if (c != null) {
			while (c.moveToNext()) {
				MusicSet music = new MusicSet();
				int id = c.getInt(c
						.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
				String name = c.getString(c
						.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
				String path = c.getString(c
						.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
				String special = c.getString(c
						.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));

				String artist = c.getString(c
						.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
				int totaltime = c
						.getInt(c
								.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
				int size = c.getInt(c
						.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
				int album_id = c
						.getInt(c
								.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
				music.setAlbum_id(album_id);
				music.setArtist(artist);
				music.setId(id);
				music.setName(name);
				music.setPath(path);
				music.setSpecial(special);
				music.setTotaltime(totaltime);
				music.setSize(size);
				musics.add(music);

			}

		}

		return musics;

	}

	// 进度转换成时间
	public static String PostionToTime(int time) {
		time /= 1000;
		int minute = time / 60;
		int hour = minute / 60;
		int sencond = time % 60;
		return String.format("%02d:%02d", minute, sencond);

	}

	// 获取音乐图片路径

	private String getAlbumArt(int album_id) {
		String mUriAlbums = "content://media/external/audio/albums";
		String[] projection = new String[] { "album_art" };
		Cursor cur = context.getContentResolver().query(
				Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)),
				projection, null, null, null);
		String album_art = null;
		if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
			cur.moveToNext();
			album_art = cur.getString(0);
		}
		cur.close();
		cur = null;
		return album_art;
	}

	// 获取音乐图片
	public BitmapDrawable getImage(int id) {
		String albumArt = getAlbumArt(id);
		Bitmap bm = null;
		BitmapDrawable bmpDraw = null;
		if (albumArt == null) {
			bm = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.media_music);

			bmpDraw = new BitmapDrawable(bm);
		} else {
			bm = BitmapFactory.decodeFile(albumArt);
			bmpDraw = new BitmapDrawable(bm);

		}
		return bmpDraw;
	}

	public static String getCurrentDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date d = new Date();
		String currentdate = sdf.format(d);

		return currentdate;

	}
}

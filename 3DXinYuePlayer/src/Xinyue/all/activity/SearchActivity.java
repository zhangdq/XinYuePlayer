package Xinyue.all.activity;

import java.util.List;

import music.dreamer.useful.MusicSet;
import music.dreamer.useful.MusicUtil;




import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
/**搜索歌曲的活动单元组件**/
public class SearchActivity extends Activity {
	private AutoCompleteTextView actv;
	private String[] names;
	private List<MusicSet> musics;
	private Button search;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		actv = (AutoCompleteTextView) findViewById(R.id.actv);
		search = (Button) findViewById(R.id.search);
		MusicUtil mu = new MusicUtil(this); 
		musics = mu.getMusic();
		names = new String[musics.size()];
		for (int i = 0; i < musics.size(); i++) {
			names[i] = musics.get(i).getName();
		}
    
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, names);
		actv.setAdapter(adapter);
		search.setOnClickListener(new OnClickListener() {

			
			public void onClick(View v) {
				String select = actv.getText().toString();
				Intent intent = new Intent(SearchActivity.this,
						AlbumActivity.class);
				intent.putExtra("select", select);
				intent.putExtra("isselect", true);
				startActivity(intent);
				finish();
				

			}
		});
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == event.KEYCODE_BACK) {
			Intent intent = new Intent();
			intent.setClass(this, MainActivity.class);
			startActivity(intent);
			finish();
		}
		return true;
    }
}

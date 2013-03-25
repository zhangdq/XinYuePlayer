package Xinyue.all.activity;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;


public class NewSongs extends Activity {
	private Button StarButton;
	@Override    
	protected void onCreate(Bundle savedInstanceState) {                    
		// TODO Auto-generated method stub   
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.newsong
				);  
		StarButton = (Button)findViewById(R.id.image);
		StarButton.setOnClickListener(new PhotoButtonListener());
	}
	class PhotoButtonListener implements OnClickListener{

		public void onClick(View v) {
			
			
		    Intent intent = new Intent();
			intent.setClass(NewSongs.this, MainActivity.class);
			startActivity(intent);
			finish();
			Toast.makeText(NewSongs.this,"已成功刷新歌曲！",Toast.LENGTH_SHORT).show();
			
		}
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
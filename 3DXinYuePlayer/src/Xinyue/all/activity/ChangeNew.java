package Xinyue.all.activity;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


public class ChangeNew extends Activity {
	private Button PhotoButton;
	@Override    
	protected void onCreate(Bundle savedInstanceState) {                    
		// TODO Auto-generated method stub   
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.change
				);  
		PhotoButton = (Button)findViewById(R.id.button1);
		PhotoButton.setOnClickListener(new PhotoButtonListener());
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
	class PhotoButtonListener implements OnClickListener{

		public void onClick(View v) {
			MainActivity.Theme();
		    Intent intent = new Intent();
			intent.setClass(ChangeNew.this, MainActivity.class);
			startActivity(intent);
			finish();
			
		}
	}
}

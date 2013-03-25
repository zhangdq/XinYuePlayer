package Xinyue.all.activity;

import java.util.LinkedList;
import Xinyue.all.activity.VideoPlayerActivity.MovieInfo;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**选择播放视频文件**/
/*This work comes from Dreamer丶Team. The main programmer is LinShaoHan.
 * QQ:752280466   Welcome to join with us.
 */
public class VideoChooseActivity extends Activity{

	//private static int height , width;
	private LinkedList<MovieInfo> mLinkedList;
	private LayoutInflater mInflater;
	View root;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.video_list);
		
		mLinkedList = VideoPlayerActivity.playList;
		
		mInflater = getLayoutInflater();
		ImageButton iButton = (ImageButton) findViewById(R.id.cancel);
		iButton.setOnClickListener(new OnClickListener(){

			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				VideoChooseActivity.this.finish();
			}
			
		});
		
		ListView myListView = (ListView) findViewById(R.id.list);
		myListView.setAdapter(new BaseAdapter(){

			public int getCount() {
				// TODO Auto-generated method stub
				return mLinkedList.size();
			}

			public Object getItem(int arg0) {
				// TODO Auto-generated method stub
				return arg0;
			}

			public long getItemId(int arg0) {
				// TODO Auto-generated method stub
				return arg0;
			}

			public View getView(int arg0, View convertView, ViewGroup arg2) {
				// TODO Auto-generated method stub
				if(convertView==null){
					convertView = mInflater.inflate(R.layout.list, null);
				}
				TextView text = (TextView) convertView.findViewById(R.id.text);
				text.setText(mLinkedList.get(arg0).displayName);
				
				return convertView;   
			}
			
		});
		
		myListView.setOnItemClickListener(new OnItemClickListener(){

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra("CHOOSE", arg2);
				VideoChooseActivity.this.setResult(Activity.RESULT_OK, intent);
				VideoChooseActivity.this.finish();
			}
		});
	}
}

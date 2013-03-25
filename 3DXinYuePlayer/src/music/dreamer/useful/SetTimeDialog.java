package music.dreamer.useful;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import Xinyue.all.activity.MainActivity;
import Xinyue.all.activity.MainActivity.MyThread;
import Xinyue.all.activity.R;

/**睡眠定时类**/
/*This work comes from Dreamer丶Team. The main programmer is LinShaoHan.
 * QQ:752280466   Welcome to join with us.
 */

public class SetTimeDialog extends DialogBuilder {

	public final static int RUNNING_BG = 0;
	public final static int EXIT_APP = 1;
	public final static int DIALOG_CANCEL = 2;
    public static EditText edittext1; 
  
	public static Builder getCreatePlaylistDialog(final Context context) {

		AlertDialog.Builder builder = getInstance(context);
		edittext1 = new EditText(context);
		edittext1.setText(context
				.getString(R.string.set_time));
		edittext1.setSelectAllOnFocus(true);
		builder.setView(edittext1);
		builder.setPositiveButton(context.getString(R.string.Yes),
				new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				
				/*将文本框输入的String类型的分钟数转换为int类型并赋值给time*/
				if(SetTimeDialog.edittext1.getText().toString()!=null)
				{
				MainActivity.time= Integer.valueOf(SetTimeDialog.edittext1.getText().toString()).intValue();
				dialog.cancel();
				}
				else {
				Toast.makeText(context, "很抱歉，设置的时间不能为空！", Toast.LENGTH_LONG).show();
				}
				new Thread(new MyThread()).start();
				if(MainActivity.time>=120){
					Toast.makeText(context, "很抱歉，设置的时间不能超过120分钟", Toast.LENGTH_LONG)
							.show();
				}
				else {
					Toast.makeText(context, "设置睡眠时间成功，"+edittext1.getText().toString()
						+"分钟后关闭音乐播放程序！", Toast.LENGTH_LONG)
						.show();}
			}
		});			 
				
		builder.setNeutralButton(context.getString(R.string.cancel),
				null);
		builder.setIcon(ImageScale.getImage(context));
		builder.setTitle("睡眠定时");
		return builder;
		  
	}
			
}

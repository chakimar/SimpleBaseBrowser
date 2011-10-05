package name.chakimar.sbb;

import android.app.DownloadManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DownloadNotificationClickReceiver extends BroadcastReceiver {

	private static final String TAG = "DownloadNotificationClickReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Intent newIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);;
			PendingIntent pIntent = PendingIntent.getActivity(context, 0, newIntent, 0);
			pIntent.send();
		} catch (CanceledException e) {
			Log.e(TAG, e.getMessage());
		}
	}

}

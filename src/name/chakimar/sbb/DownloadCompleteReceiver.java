package name.chakimar.sbb;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class DownloadCompleteReceiver extends BroadcastReceiver {

	private static final String TAG = "DownloadReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive:"+intent);
		Bundle bundle = intent.getExtras();
		long downloadId = bundle.getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
		if (downloadId == 0L) {
			//error
			return;
		}
		
		Query query = new Query();
		query.setFilterById(downloadId);
		DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		Cursor c = dm.query(query);
		
		int idUri = c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
		c.moveToFirst();
		String localUri = c.getString(idUri);
		int idMimetype = c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE);
		c.moveToFirst();
		String mimetype = c.getString(idMimetype);
		int idTitle = c.getColumnIndex(DownloadManager.COLUMN_TITLE);
		c.moveToFirst();
		String title = c.getString(idTitle);
		
		notifyDownloadCompleted(context, downloadId, localUri, mimetype, title);
	}

	private void notifyDownloadCompleted(Context context, long downloadId,
			String localUri, String mimetype, String title) {
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		Notification notification = new Notification();
		notification.when = System.currentTimeMillis();
		notification.tickerText = "download completed";
		notification.icon = android.R.drawable.stat_sys_download_done;
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		
		Intent newIntent = new Intent(Intent.ACTION_VIEW);
		newIntent.setDataAndType(Uri.parse(localUri), mimetype);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, newIntent , 0);
		notification.setLatestEventInfo(context, title, "download completed", contentIntent);
		nm.notify((int)downloadId, notification);
	}

}

package name.chakimar.sbb;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Browser;
import android.widget.ListAdapter;
import android.widget.ListView;

public class BookmarkListActivity extends ListActivity {
	private String[] projection = new String[]{
			Browser.BookmarkColumns.BOOKMARK,
			Browser.BookmarkColumns.CREATED,
			Browser.BookmarkColumns.DATE,
			Browser.BookmarkColumns.FAVICON,
			Browser.BookmarkColumns.TITLE,
			Browser.BookmarkColumns.URL,
			Browser.BookmarkColumns.VISITS
	};
	private ListView lv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		lv = getListView();

		String selection = Browser.BookmarkColumns.BOOKMARK + "=1";
		Cursor c = getContentResolver().query(Browser.BOOKMARKS_URI, projection, selection , null, null);
		List<Bookmark> bookmarks = new ArrayList<Bookmark>();
		if (c.moveToFirst()) {
			int idTitle = c.getColumnIndex(Browser.BookmarkColumns.TITLE);
			int idUrl = c.getColumnIndex(Browser.BookmarkColumns.URL);
			int idFavicon = c.getColumnIndex(Browser.BookmarkColumns.FAVICON);
			do {
				String title = c.getString(idTitle);
				String url = c.getString(idUrl);
				byte[] faviconData = c.getBlob(idFavicon);
				Bitmap favicon = null;
				if (faviconData != null && faviconData.length > 0) {
					favicon = BitmapFactory.decodeByteArray(faviconData, 0, faviconData.length);
				} else {
					favicon = BitmapFactory.decodeResource(getResources(), R.drawable.bookmark);
				}
				Bookmark bookmark = new Bookmark();
				bookmark.setTitle(title);
				bookmark.setUrl(url);
				bookmark.setFavicon(favicon);
				bookmarks.add(bookmark);
			} while(c.moveToNext());
		}
		if (c != null) {
			c.close();
		}

		ListAdapter adapter = new BookmarkListAdapter(this, R.layout.row_bookmark, bookmarks);
		lv.setAdapter(adapter);
	}


}

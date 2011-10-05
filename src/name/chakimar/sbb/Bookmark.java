package name.chakimar.sbb;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class Bookmark {

	private String title;
	private String url;
	private Bitmap favicon;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public void setFavicon(byte[] data) {
		if (data != null && data.length > 0) {
			this.favicon = BitmapFactory.decodeByteArray(data, 0, data.length);
		}
	}
	public void setFavicon(Bitmap favicon) {
		this.favicon = favicon;
	}
	public Bitmap getFavicon() {
		return favicon;
	}
}

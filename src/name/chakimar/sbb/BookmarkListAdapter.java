package name.chakimar.sbb;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.ClipboardManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BookmarkListAdapter extends ArrayAdapter<Bookmark> {

	protected static final int ITEM_ORDER_OPEN = 0;
	protected static final int ITEM_ORDER_EDIT = ITEM_ORDER_OPEN + 1;
	protected static final int ITEM_ORDER_CREATE_SHORTCUT = ITEM_ORDER_EDIT + 1;
	protected static final int ITEM_ORDER_SHARE_LINK = ITEM_ORDER_CREATE_SHORTCUT + 1;
	protected static final int ITEM_ORDER_COPY_URL = ITEM_ORDER_SHARE_LINK + 1;
	protected static final int ITEM_ORDER_DELETE = ITEM_ORDER_COPY_URL + 1;
	protected static final int ITEM_ORDER_SET_AS_HOMEPAGE = ITEM_ORDER_DELETE + 1;
	private Context context;
	private List<Bookmark> items;
	private LayoutInflater inflater;

	public BookmarkListAdapter(Context context, int textViewResourceId,
			List<Bookmark> items) {
		super(context, textViewResourceId, items);
		this.context = context;
		this.items = items;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if(v == null){  
			//1行分layoutからViewの塊を生成
			v = inflater.inflate(R.layout.row_bookmark, null);
		}
		final Bookmark bookmark = items.get(position);

		v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadUrl(bookmark.getUrl());
			}
		});
		v.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				openBookmarkMenuDialog(bookmark);
				return true;
			}
		});

		ImageView ivFavicon = (ImageView) v.findViewById(R.id.bookmark_image);
		TextView tvTitle = (TextView) v.findViewById(R.id.bookmark_title);
		TextView tvUrl = (TextView) v.findViewById(R.id.bookmark_url);
		
		Bitmap favicon = bookmark.getFavicon();
		ivFavicon.setImageBitmap(favicon);
		tvTitle.setText(bookmark.getTitle());
		tvUrl.setText(bookmark.getUrl());

		return v;
	}

	protected void openBookmarkMenuDialog(final Bookmark bookmark) {
		AlertDialog bookmarkMenuDialog = new AlertDialog.Builder(context)
		.setTitle(bookmark.getTitle())
		.setItems(R.array.bookmark_menu, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == ITEM_ORDER_OPEN) {
					loadUrl(bookmark.getUrl());
				}
				if (which == ITEM_ORDER_EDIT) {
					editBookmark(bookmark);
				}
				if (which == ITEM_ORDER_CREATE_SHORTCUT) {
					createShortcut(bookmark);
				}
				if (which == ITEM_ORDER_SHARE_LINK) {
					shareLink(bookmark);
				}
				if (which == ITEM_ORDER_COPY_URL) {
					copyUrl(bookmark.getUrl());
				}
				if (which == ITEM_ORDER_DELETE) {
					deleteBookmark(bookmark);
				}
				if (which == ITEM_ORDER_SET_AS_HOMEPAGE) {
					setAsHomepage(bookmark.getUrl());
				}
			}
		})
		.create();

		bookmarkMenuDialog.show();
	}

	protected void deleteBookmark(Bookmark bookmark) {
		//TODO ブックマーク追加機能を実装する際に実装する。
	}

	protected void shareLink(Bookmark bookmark) {
		//TODO 履歴機能を作ってから作る
	}

	protected void editBookmark(Bookmark bookmark) {
		//TODO ブックマーク追加機能を実装する際に実装する。
		
	}

	protected void createShortcut(Bookmark bookmark) {
		//TODO 履歴機能を作ってから作る
	}

	private void loadUrl(String url) {
		Intent data = new Intent(context, MainActivity.class);
		data.putExtra("url", url);
		((Activity) context).setResult(Activity.RESULT_OK, data);
		((Activity) context).finish();
	}
	
	private void copyUrl(String url) {
		ClipboardManager cbm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		cbm.setText(url);
		
	}

	private void setAsHomepage(String url) {
		SimpleBaseBrowser app = (SimpleBaseBrowser) context.getApplicationContext();
		app.setHomePage(url);
	}

}

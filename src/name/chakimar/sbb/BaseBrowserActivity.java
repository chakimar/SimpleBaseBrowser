package name.chakimar.sbb;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.HttpAuthHandler;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public abstract class BaseBrowserActivity extends Activity implements DownloadListener{
	private static final String SEARCH_QUERY = "http://www.google.co.jp?q=";
	private static final int ITEM_ID_GO_BACK = 0;
	private static final int ITEM_ID_GO_FOWARD = ITEM_ID_GO_BACK + 1;
	private static final int ITEM_ID_RELOAD_OR_STOP = ITEM_ID_GO_FOWARD + 1;
	private static final int ITEM_ID_SEARCH = ITEM_ID_RELOAD_OR_STOP + 1;
	private static final int ITEM_ID_ADD_BOOKMARK = ITEM_ID_SEARCH + 1;
	private static final int ITEM_ID_READ_BOOKMARKS = ITEM_ID_ADD_BOOKMARK + 1;
	private static final int ITEM_ID_HOMEPAGE = ITEM_ID_READ_BOOKMARKS + 1;
	private static final int ITEM_ID_DOWNLOAD_HISTORY = ITEM_ID_HOMEPAGE + 1;
	private static final int ITEM_ID_SETTINGS = ITEM_ID_DOWNLOAD_HISTORY + 1;
	private static final int REQUEST_CODE_READ_BOOKMARKS = 0;
    private final static String EXTRA_SHARE_FAVICON = "share_favicon";
	private static final String EXTRA_SHARE_SCREENSHOT = "share_screenshot";
	protected SimpleBaseBrowser app;
	protected WebView webview;
	public boolean nowloading;
	protected WebViewClient webviewClient = new WebViewClient() {

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			BaseBrowserActivity.this.nowloading = true;
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			BaseBrowserActivity.this.nowloading = false;
		}
		
		@Override
		public void onReceivedHttpAuthRequest(WebView view,
				HttpAuthHandler handler, String host, String realm) {
			String[] up = view.getHttpAuthUsernamePassword(host, realm);
			if( up != null && up.length == 2 && handler.useHttpAuthUsernamePassword()) {
				String username = up[0];
				String password = up[1];
				if (username!= null && password != null) {
					handler.proceed(up[0], up[1]);
				} else {
					handler.cancel();
				}
			}
			else{
				showHttpAuthenication(handler, host, realm, null, null, 0);
			}
		}
		//TODO フォームのリサブミットに対応する
		//TODO SSLエラーに対応する。（オレオレ証明書）
		//TODO マーケットのリンク等、WebViewで開けないリンクに対応する
		
	};
	protected WebChromeClient webChromeClient = new WebChromeClient() {
		@Override
		public void onReceivedTitle(WebView view, String title) {
			setTitle(title);
		}

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			setProgress(newProgress * 100);
		}
	};


    public static final void sharePage(Context c, String title, String url,
            Bitmap favicon, Bitmap screenshot) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, url);
        send.putExtra(Intent.EXTRA_SUBJECT, title);
        send.putExtra(EXTRA_SHARE_FAVICON, favicon);
        send.putExtra(EXTRA_SHARE_SCREENSHOT, screenshot);
        try {
            c.startActivity(Intent.createChooser(send, c.getString(
                    R.string.choosertitle_sharevia)));
        } catch(android.content.ActivityNotFoundException ex) {
            // if no app handles it, do nothing
        }
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		//プログレスバーがしましま模様になる。
		//		setProgressBarIndeterminate(true);
		this.app = (SimpleBaseBrowser) getApplication();
		initWebView();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		finalizeWebView();
	}

	/**
	 * WebViewの初期化
	 */
	protected void initWebView() {
		webview = new WebView(this);
		webview.setWebViewClient(webviewClient);
		webview.setWebChromeClient(webChromeClient);
		webview.setDownloadListener(this);

		WebSettings settings = webview.getSettings();
		settings.setJavaScriptEnabled(true);
		//ズームコントロールを追加
		settings.setBuiltInZoomControls(true);
		//ダブルタップズームに必要
		settings.setUseWideViewPort(true);
		//ズームアウトして画面全体を表示させる。
		settings.setLoadWithOverviewMode(true);
		//Flash再生に対応
		settings.setPluginsEnabled(true);

	}

	/**
	 * WebViewの最終処理
	 */
	protected void finalizeWebView() {
		if (webview == null) return;
		webview.stopLoading();
		webview.clearCache(true);
		webview.setWebViewClient(null);
		webview.setWebChromeClient(null);
		webview.destroy();
		webview = null;
	}

	protected void loadUrl(final String url) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				webview.loadUrl(url);
			}
		});
	}

	protected void searchUrl(final String keyword) {
		String url = SEARCH_QUERY + keyword;
		loadUrl(url);
	}

	/**
	 * BACKキーが押された場合は、履歴があれば戻り、無ければ終了する。
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (webview.canGoBack()) {
				webview.goBack();
				return true;
			} else {
				finish();
				return true;
			}
		} else if (event.getAction() == KeyEvent.ACTION_DOWN
				&& event.getKeyCode() == KeyEvent.KEYCODE_SEARCH) {
			openSearchDialog();
		}

		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ITEM_ID_GO_BACK, 0, R.string.go_back);
		menu.add(0, ITEM_ID_GO_FOWARD, 0, R.string.go_foward);
		menu.add(0, ITEM_ID_RELOAD_OR_STOP, 0, R.string.reload);
		menu.add(0, ITEM_ID_SEARCH, 0, R.string.search);
		menu.add(0, ITEM_ID_ADD_BOOKMARK, 0, R.string.add_bookmark);
		menu.add(0, ITEM_ID_READ_BOOKMARKS, 0, R.string.bookmark);
		menu.add(0, ITEM_ID_HOMEPAGE, 0, R.string.homepage);
		menu.add(0, ITEM_ID_DOWNLOAD_HISTORY, 0, R.string.download_history).setIntent(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
		menu.add(0, ITEM_ID_SETTINGS, 0, R.string.settings).setIntent(new Intent(this, BaseSettingsActivity.class));
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(ITEM_ID_GO_BACK).setEnabled(webview.canGoBack());
		menu.findItem(ITEM_ID_GO_FOWARD).setEnabled(webview.canGoForward());
		if (nowloading) {
			menu.findItem(ITEM_ID_RELOAD_OR_STOP).setTitle(R.string.stop);
		} else {
			menu.findItem(ITEM_ID_RELOAD_OR_STOP).setTitle(R.string.reload);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == ITEM_ID_GO_BACK) {
			if (webview.canGoBack()) webview.goBack();
			return true;
		}
		if (itemId == ITEM_ID_GO_FOWARD) {
			if (webview.canGoForward()) webview.goForward();
			return true;
		}
		if (itemId == ITEM_ID_RELOAD_OR_STOP) {
			if (nowloading) {
				showToast("stop loading...");
				webview.stopLoading();
			} else {
				webview.reload();
			}
			return true;
		}
		if (itemId == ITEM_ID_SEARCH) {
			openSearchDialog();
			return true;
		}
		if (itemId == ITEM_ID_ADD_BOOKMARK) {
			startAddBookmarkActivity();
			return true;
		}
		if (itemId == ITEM_ID_READ_BOOKMARKS) {
			startActivityForResult(new Intent(this, BookmarkListActivity.class), REQUEST_CODE_READ_BOOKMARKS);
			return true;
		}
		if (itemId == ITEM_ID_HOMEPAGE) {
			loadUrl(app.getHomepage());
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void startAddBookmarkActivity() {
		Intent intent = new Intent(this, AddBookmarkActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_READ_BOOKMARKS && resultCode == RESULT_OK) {
			String url = data.getStringExtra("url");
			loadUrl(url);
		}
	}

	protected void openSearchDialog() {
		LayoutInflater inflater = getLayoutInflater();
		final TextView view = (TextView) inflater.inflate(R.layout.move_or_search_dialog, null);
		view.setText(webview.getUrl());
		AlertDialog dialog = new AlertDialog.Builder(this)
		.setView(view)
		.setPositiveButton(R.string.move, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String url = view.getText().toString();
				if (!URLUtil.isValidUrl(url)) {
					url = "http://" + url;
				}
				loadUrl(url);
			}
		})
		.setNeutralButton(R.string.search, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				searchUrl(view.getText().toString());
			}
		})
		.setNegativeButton(R.string.cancel, null)
		.create();

		//ダイアログ表示時にキーボードを表示させる
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		view.requestFocus();
		dialog.show();
	}

	@Override
	public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
		Toast.makeText(this, "download started", Toast.LENGTH_SHORT).show();

		try {
			downloadByDownloadManager(url, contentDisposition, mimetype);
		} catch (NoClassDefFoundError e) {
			//TODO 通常のダウンロード処理を書く
		}
	}

	private void downloadByDownloadManager(String url,
			String contentDisposition, String mimetype) {
		DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
		String filename = guessUniqueFileName(url, contentDisposition, mimetype);
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
		//		request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
		request.setMimeType(mimetype);
		dm.enqueue(request);
	}

	private String guessUniqueFileName(String url, String contentDisposition,
			String mimetype) {
		String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);
		File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		String[] files = downloadDir.list();
		FileName fn = new FileName(filename);
		filename = fn.getUniqueFileName(files);
		
		return filename;
	}


	private void showHttpAuthenication(final HttpAuthHandler handler, final String host, final String realm, final String name, final String password, int focusId)
	{
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.http_auth_dialog, null);
		EditText et_user = (EditText)textEntryView.findViewById(R.id.http_auth_username_edit);
		EditText et_pass = (EditText)textEntryView.findViewById(R.id.http_auth_password_edit);
		
		if(name!=null) {
			et_user.setText(name);
		}
		if(password!=null) {
			et_pass.setText(password);
		}
		((TextView)textEntryView.findViewById(R.id.host_view)).setText(host);
		AlertDialog dialog = new AlertDialog.Builder(this)
		.setTitle(realm)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setView(textEntryView)
		.setPositiveButton(R.string.auth_dialog_ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				EditText etAuthUserName = (EditText)textEntryView.findViewById(R.id.http_auth_username_edit);
				EditText etAuthPassword = (EditText)textEntryView.findViewById(R.id.http_auth_password_edit);
				String user = etAuthUserName.getText().toString();
				String pass = etAuthPassword.getText().toString();
				webview.setHttpAuthUsernamePassword(host, realm, user, pass);
				handler.proceed(user, pass);
			}
		})
		.setNegativeButton(R.string.auth_dialog_cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				handler.cancel();
			}
		})
		.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				handler.cancel();

			}
		})
		.create();

		if(focusId!=0) {
			//    		dialog.findViewById(focusId).requestFocus();//error code!!
			textEntryView.findViewById(focusId).requestFocus();
		} else {
			textEntryView.findViewById(R.id.http_auth_username_edit).requestFocus();
		}

		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.show();
	}
	
	void showToast(int resId) {
		Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
	}
	
	void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
}

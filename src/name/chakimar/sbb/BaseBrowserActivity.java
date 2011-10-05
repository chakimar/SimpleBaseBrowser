package name.chakimar.sbb;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

public abstract class BaseBrowserActivity extends Activity implements DownloadListener{
	private static final String SEARCH_QUERY = "http://www.google.co.jp?q=";
	private static final int ITEM_ID_SEARCH = 0;
	private static final int ITEM_ID_DOWNLOAD_HISTORY = ITEM_ID_SEARCH + 1;
	protected WebView webview;
	protected WebViewClient webviewClient = new WebViewClient() {
		//TODO ベーシック認証に対応する
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		//プログレスバーがしましま模様になる。
		//		setProgressBarIndeterminate(true);
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
		menu.add(0, ITEM_ID_SEARCH, 0, R.string.search);
		menu.add(0, ITEM_ID_DOWNLOAD_HISTORY, 1, R.string.download_history);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == ITEM_ID_SEARCH) {
			openSearchDialog();
			return true;
		} else if (itemId == ITEM_ID_DOWNLOAD_HISTORY) {
			startDownloadHistoryActivity();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void startDownloadHistoryActivity() {
		Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
		startActivity(intent);
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
				if (!url.startsWith("http://")) {
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
}

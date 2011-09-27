package name.chakimar.sbb;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public abstract class BaseBrowserActivity extends Activity {
	private static final String SEARCH_QUERY = "http://www.google.co.jp?q=";
	private static final int DIALOG_ID_SEARCH = 0;
	private static final int ITEM_ID_SEARCH = 0;
	protected WebView webview;
	protected WebViewClient webviewClient = new WebViewClient() {
		//TODO ベーシック認証に対応する
		//TODO フォームのリサブミットに対応する
		//TODO SSLエラーに対応する。（オレオレ証明書）
		
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
		
		WebSettings settings = webview.getSettings();
		settings.setJavaScriptEnabled(true);
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
		}
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_ID_SEARCH) {
			LayoutInflater inflater = getLayoutInflater();
			final TextView view = (TextView) inflater.inflate(R.layout.move_or_search_dialog, null);
			view.setText(webview.getUrl());
			AlertDialog dialog = new AlertDialog.Builder(this)
			.setView(view)
			.setPositiveButton(R.string.move, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					loadUrl(view.getText().toString());
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
			
			return dialog;
		}
		return super.onCreateDialog(id);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ITEM_ID_SEARCH, 0, R.string.search);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (featureId == ITEM_ID_SEARCH) {
			openSearchDialog();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	protected void openSearchDialog() {
		showDialog(DIALOG_ID_SEARCH);
	}
	
}
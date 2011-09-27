package name.chakimar.sbb;

import android.os.Bundle;

public class MainActivity extends BaseBrowserActivity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(webview);
        loadUrl("http://www.google.co.jp");
    }
}
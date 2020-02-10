package com.example.task.ui.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.example.task.R;

public class WebViewActivity extends AppCompatActivity {
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        getSupportActionBar().setTitle(getResources().getString(R.string.webview_activity_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String url = getIntent().getExtras().getString("url");
        mWebView = (WebView) findViewById(R.id.webBrowser);

        try {
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.setWebViewClient(new WebViewClient()
            {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url)
                {
                    view.loadUrl(url);
                    return true;
                }
            });
            mWebView.loadUrl(url);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

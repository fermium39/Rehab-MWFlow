package com.fitflow.go;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.graphics.drawable.Drawable;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.graphics.Bitmap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;

import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;

import java.io.OutputStream;

public class MainActivity extends Activity {
    private static final String APP_URL = "https://appassets.androidplatform.net/assets/index.html";
    private static final int EXPORT_REQUEST = 1001;
    private WebView webView;
    private FrameLayout splashOverlay;
    private final Handler splashHandler = new Handler(Looper.getMainLooper());
    private boolean splashPageReady = false;
    private byte[] pendingExport;

    public final class ExportBridge {
        @JavascriptInterface
        public void saveHistory(String base64Json) {
            try {
                final byte[] data = Base64.decode(base64Json, Base64.DEFAULT);
                runOnUiThread(() -> {
                    pendingExport = data;
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/json");
                    intent.putExtra(Intent.EXTRA_TITLE, "fitflow-go-history.json");
                    startActivityForResult(intent, EXPORT_REQUEST);
                });
            } catch (IllegalArgumentException ignored) { }
        }
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(Color.rgb(5, 24, 39));
        rootLayout.addView(webView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        splashOverlay = new FrameLayout(this);
        splashOverlay.setBackgroundColor(Color.rgb(5, 24, 39));
        ImageView background = new ImageView(this);
        background.setImageResource(com.fitflow.go.R.drawable.fitflow_go_splash_background);
        background.setScaleType(ImageView.ScaleType.CENTER_CROP);
        splashOverlay.addView(background, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        ImageView logo = new ImageView(this);
        logo.setImageResource(com.fitflow.go.R.drawable.fitflow_go_splash_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int padding = (int) (32 * getResources().getDisplayMetrics().density);
        logo.setPadding(padding, 0, padding, 0);
        splashOverlay.addView(logo, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        rootLayout.addView(splashOverlay, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(rootLayout);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.addJavascriptInterface(new ExportBridge(), "FitFlowGo");
        webView.setWebChromeClient(new WebChromeClient());
        WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
            .build();
        webView.setWebViewClient(new WebViewClientCompat() {
            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (APP_URL.equals(url) && !splashPageReady) {
                    splashPageReady = true;
                    splashHandler.postDelayed(() -> {
                        if (splashOverlay != null) {
                            splashOverlay.setVisibility(View.GONE);
                            splashOverlay = null;
                        }
                    }, 1400);
                }
            }
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return loader.shouldInterceptRequest(request.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                return !"appassets.androidplatform.net".equals(uri.getHost());
            }
        });
        webView.loadUrl(APP_URL);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EXPORT_REQUEST) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null && pendingExport != null) {
                try (OutputStream output = getContentResolver().openOutputStream(data.getData())) {
                    if (output != null) output.write(pendingExport);
                } catch (Exception ignored) { }
            }
            pendingExport = null;
        }
    }

    @Override public void onBackPressed() {
        if (webView == null) { super.onBackPressed(); return; }
        webView.evaluateJavascript(
            "(function(){return window.fitFlowGoBack ? window.fitFlowGoBack() : false})()",
            value -> { if (!"true".equals(value)) finish(); }
        );
    }

    @Override public void onDestroy() {
        splashHandler.removeCallbacksAndMessages(null);
        if (webView != null) {
            webView.removeJavascriptInterface("FitFlowGo");
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}

package br.radiosnetto.iptv;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

// Abre o site da lista dentro do app. Os canais vêm sempre de /lista.m3u,
// então atualizar a lista no site atualiza o app sem precisar de um APK novo.
public class MainActivity extends Activity {
    static final String HOST = "radiosnettoiptv.netlify.app";
    static final String HOME = "https://" + HOST + "/";

    WebView web;
    FrameLayout root;
    View fullscreen;
    WebChromeClient.CustomViewCallback fullscreenCallback;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#0B0F1A"));
        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#0B0F1A"));
        root.addView(web, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setUserAgentString(s.getUserAgentString() + " RadiosNettoApp/1.0");

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                Uri uri = req.getUrl();
                if (HOST.equals(uri.getHost()) && !uri.getPath().startsWith("/lista.m3u")) return false;
                openExternal(uri);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
                if (req.isForMainFrame()) {
                    view.loadDataWithBaseURL(null,
                        "<body style='background:#0b0f1a;color:#e8ecf6;font-family:sans-serif;text-align:center;padding-top:30vh'>"
                        + "<h2>Sem conexão</h2><p>Verifique sua internet.</p>"
                        + "<button style='padding:12px 20px;border:0;border-radius:10px;background:#ffb21e;font-weight:700' "
                        + "onclick=\"location.href='" + HOME + "'\" autofocus>Tentar novamente</button></body>",
                        "text/html", "utf-8", null);
                }
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (fullscreen != null) { callback.onCustomViewHidden(); return; }
                fullscreen = view;
                fullscreenCallback = callback;
                root.addView(view, new FrameLayout.LayoutParams(-1, -1));
                web.setVisibility(View.GONE);
                setFullscreenUi(true);
            }

            @Override
            public void onHideCustomView() {
                exitFullscreen();
            }
        });

        web.setDownloadListener((url, ua, disposition, mime, length) -> openExternal(Uri.parse(url)));

        if (state != null) web.restoreState(state);
        else web.loadUrl(HOME);
    }

    void openExternal(Uri uri) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Exception ignored) {
        }
    }

    void exitFullscreen() {
        if (fullscreen == null) return;
        root.removeView(fullscreen);
        fullscreen = null;
        web.setVisibility(View.VISIBLE);
        setFullscreenUi(false);
        if (fullscreenCallback != null) fullscreenCallback.onCustomViewHidden();
        fullscreenCallback = null;
    }

    void setFullscreenUi(boolean on) {
        getWindow().getDecorView().setSystemUiVisibility(on
            ? View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            : 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (fullscreen != null) { exitFullscreen(); return true; }
            // Fecha o player aberto antes de voltar ou sair do app.
            web.evaluateJavascript(
                "(function(){var d=document.getElementById('player');if(d&&d.open){d.close();return 1}return 0})()",
                closed -> {
                    if ("1".equals(closed)) return;
                    if (web.canGoBack()) web.goBack();
                    else finish();
                });
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    protected void onPause() {
        super.onPause();
        web.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.onResume();
    }

    @Override
    protected void onDestroy() {
        ((ViewGroup) web.getParent()).removeView(web);
        web.destroy();
        super.onDestroy();
    }
}

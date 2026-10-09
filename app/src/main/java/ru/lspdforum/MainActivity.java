package ru.lspdforum;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ActivityNotFoundException;

public class MainActivity extends Activity {
    private static final String DEFAULT_FORUM_URL = "https://whg124570.samp.date/";
    private static final int INK = Color.rgb(29, 39, 34);
    private static final int MUTED = Color.rgb(105, 113, 106);
    private static final int PAPER = Color.rgb(245, 242, 234);
    private static final int ACCENT = Color.rgb(182, 77, 56);

    private WebView webView;
    private ProgressBar progressBar;
    private TextView welcomeTitle;
    private TextView welcomeMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(PAPER);
        getWindow().setNavigationBarColor(PAPER);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        setContentView(createLayout());
        configureWebView();

        if (savedInstanceState != null && webView.restoreState(savedInstanceState) != null) {
            showForum();
        } else {
            openForum();
        }
    }

    private View createLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PAPER);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(0, insets.getSystemWindowInsetTop(), 0, insets.getSystemWindowInsetBottom());
            return insets;
        });

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(22), dp(12), dp(18), dp(12));

        TextView brand = new TextView(this);
        brand.setText("LSPD Forum");
        brand.setTextColor(INK);
        brand.setTextSize(15);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        brand.setLetterSpacing(0.08f);
        header.addView(brand, new LinearLayout.LayoutParams(0, dp(40), 1));

        Button backButton = iconButton("‹", "Назад");
        backButton.setOnClickListener(view -> {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                showWelcome("Вы уже на главной странице форума.");
            }
        });
        header.addView(backButton);

        Button reloadButton = iconButton("↻", "Обновить страницу");
        reloadButton.setOnClickListener(view -> {
            if (webView.getVisibility() == View.VISIBLE) {
                webView.reload();
            }
        });
        header.addView(reloadButton);
        root.addView(header);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(ACCENT));
        progressBar.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(ACCENT));
        progressBar.setVisibility(View.GONE);
        root.addView(progressBar, new LinearLayout.LayoutParams(-1, dp(3)));

        FrameLayout content = new FrameLayout(this);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(-1, 0, 1);
        root.addView(content, contentParams);

        LinearLayout welcome = new LinearLayout(this);
        welcome.setOrientation(LinearLayout.VERTICAL);
        welcome.setGravity(Gravity.CENTER);
        welcome.setPadding(dp(30), dp(24), dp(30), dp(36));
        welcomeTitle = new TextView(this);
        welcomeTitle.setText("Ваше сообщество");
        welcomeTitle.setTextColor(INK);
        welcomeTitle.setTextSize(30);
        welcomeTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        welcomeTitle.setGravity(Gravity.CENTER);
        welcome.addView(welcomeTitle);

        welcomeMessage = new TextView(this);
        welcomeMessage.setText("Темы, люди и разговоры в одном месте.");
        welcomeMessage.setTextColor(MUTED);
        welcomeMessage.setTextSize(16);
        welcomeMessage.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(-1, -2);
        messageParams.setMargins(0, dp(12), 0, 0);
        welcome.addView(welcomeMessage, messageParams);
        content.addView(welcome, new FrameLayout.LayoutParams(-1, -1));

        webView = new WebView(this);
        webView.setBackgroundColor(PAPER);
        webView.setVisibility(View.GONE);
        content.addView(webView, new FrameLayout.LayoutParams(-1, -1));

        return root;
    }

    private void configureWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAllowContentAccess(false);
        webView.getSettings().setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
                progressBar.setVisibility(progress < 100 ? View.VISIBLE : View.GONE);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();
                if ("https".equalsIgnoreCase(scheme)) {
                    return false;
                }
                if ("http".equalsIgnoreCase(scheme)) {
                    showWelcome("Небезопасная HTTP-ссылка заблокирована.");
                    return true;
                }
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (ActivityNotFoundException ignored) {
                    Toast.makeText(MainActivity.this, "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                showForum();
                progressBar.setVisibility(View.VISIBLE);
            }

        });
        android.webkit.CookieManager.getInstance().setAcceptCookie(true);
    }

    private void openForum() {
        webView.loadUrl(DEFAULT_FORUM_URL);
    }

    private void showForum() {
        webView.setVisibility(View.VISIBLE);
        View welcome = (View) welcomeTitle.getParent();
        welcome.setVisibility(View.GONE);
    }

    private void showWelcome(String message) {
        webView.stopLoading();
        webView.setVisibility(View.GONE);
        View welcome = (View) welcomeTitle.getParent();
        welcome.setVisibility(View.VISIBLE);
        welcomeTitle.setText("Ваше сообщество");
        welcomeMessage.setText(message);
        progressBar.setVisibility(View.GONE);
    }

    private Button iconButton(String icon, String description) {
        Button button = new Button(this);
        button.setText(icon);
        button.setContentDescription(description);
        button.setTextSize(25);
        button.setTextColor(INK);
        button.setAllCaps(false);
        button.setMinWidth(0);
        button.setPadding(0, 0, 0, dp(3));
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setLayoutParams(new LinearLayout.LayoutParams(dp(46), dp(44)));
        return button;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (webView != null) {
            webView.saveState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
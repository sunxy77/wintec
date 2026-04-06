package cn.szscinfo.wintec;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private ConfigManager configManager;
    private boolean isMenuVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 隐藏系统UI
        hideSystemUI();

        // 初始化配置管理器
        configManager = new ConfigManager(this);

        // 初始化视图
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);

        // 设置WebView
        setupWebView();

        // 强制设置WebView背景色为白色，防止黑屏
        webView.setBackgroundColor(0); // 设置透明
        if (webView.getParent() instanceof View) {
            ((View) webView.getParent()).setBackgroundColor(getResources().getColor(android.R.color.white));
        }

        // 加载保存的看板或默认看板
        String currentUrl = configManager.getCurrentUrl();
        loadDashboard(currentUrl);

        // 显示当前看板名称
        String currentName = configManager.getCurrentDashboardName();
        Toast.makeText(this, "当前看板: " + currentName, Toast.LENGTH_SHORT).show();
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();

        // 允许混合内容加载（处理 HTTPS 页面加载 HTTP 资源的情况）
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // 允许访问本地文件系统（如果需要）
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);

        // 启用JavaScript（重要：ERP看板需要）
        webSettings.setJavaScriptEnabled(true);

        // 启用DOM存储
        webSettings.setDomStorageEnabled(true);

        // 支持数据库
        webSettings.setDatabaseEnabled(true);

        // 自适应屏幕
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        // 强制开启硬件加速以解决部分 TV 黑屏问题
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // 禁止垂直/水平滚动条显示，减少黑边干扰
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        // 支持缩放（可选，电视端建议关闭）
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);

        // 设置缓存模式
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // 设置WebViewClient处理页面跳转
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 所有链接都在WebView内打开
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 页面加载完成，隐藏进度条
                progressBar.setVisibility(View.GONE);
                // 打印加载完成日志
                android.util.Log.d("WebView", "Page loaded: " + url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                // 加载出错时的处理
                String errorMessage = "加载失败 (" + errorCode + "): " + description;
                if (errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT) {
                    errorMessage = "网络连接失败，请检查电视网络状态。";
                }
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                android.util.Log.e("WebView", "Error: " + description + " at " + failingUrl);
                // 发生错误时，将背景色设为灰色以便区分
                view.setBackgroundColor(android.graphics.Color.GRAY);
            }
        });

        // 设置WebChromeClient处理进度
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        // 让WebView获取焦点
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.requestFocus();
    }

    private void loadDashboard(String url) {
        if (url != null && !url.isEmpty()) {
            // 如果URL不是http开头，添加https://
            if (!url.startsWith("http")) {
                url = "https://" + url;
            }
            webView.loadUrl(url);
        } else {
            // 加载错误页面
            webView.loadUrl("about:blank");
            Toast.makeText(this, "请先配置看板地址", Toast.LENGTH_LONG).show();
        }
    }

    private void showDashboardSelector() {
        final String[] names = configManager.getDashboardNames();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择看板")
                .setItems(names, (dialog, which) -> {
                    // 保存选择的看板
                    configManager.saveCurrentDashboard(which);
                    // 加载新看板
                    String url = configManager.getDashboardUrl(which);
                    loadDashboard(url);
                    // 显示提示
                    Toast.makeText(MainActivity.this,
                            "切换到: " + names[which], Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // 让对话框的按钮可聚焦（电视遥控器可用）
        dialog.getWindow().getDecorView().setFocusable(true);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();

        // 当按键按下时处理
        if (action == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_MENU:
                case KeyEvent.KEYCODE_SETTINGS:
                    showDashboardSelector();
                    return true;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    // 记录起始按键时间（由系统处理长按逻辑，但在这里通过 event.isLongPress 判断）
                    if (event.isLongPress()) {
                        showDashboardSelector();
                        return true;
                    }
                    break;
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        webView.goBack();
                        return true;
                    }
                    break;
            }
        } else if (action == KeyEvent.ACTION_UP) {
            // 在松开确认键时，如果没有被消费（长按），则执行刷新
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                if (!event.isCanceled() && !event.isLongPress()) {
                    webView.reload();
                    Toast.makeText(this, "正在刷新面板...", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                // 系统UI重新出现，再次隐藏
                new Handler().postDelayed(this::hideSystemUI, 1000);
            }
        });

        // 沉浸模式
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }
}
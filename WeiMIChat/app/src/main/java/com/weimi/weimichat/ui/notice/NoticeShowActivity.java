package com.weimi.weimichat.ui.notice;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.weimi.weimichat.R;
import com.weimi.weimichat.bean.BaseResp;
import com.weimi.weimichat.bean.notice.NoticeResp;
import com.weimi.weimichat.biz.home.NoticePresenter;
import com.weimi.weimichat.biz.home.NoticeView;
import com.weimi.weimichat.ui.base.BaseActivity;
import com.weimi.weimichat.util.GeneralUtils;
import com.weimi.weimichat.util.LoadingDialog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by yxl on 2018/9/10.
 */

public class NoticeShowActivity extends BaseActivity implements NoticeView{

    private WebView webView;

    private int noticeId;

    private NoticePresenter noticePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        noticeId = getIntent().getExtras().getInt("noticeId");
        presenter = noticePresenter = new NoticePresenter();
        noticePresenter.attachView(this);
        setContentView(R.layout.show_notice);
        super.onCreate(savedInstanceState);
    }
    @Override
       public void onError(String errorMsg, String code) {        if (!GeneralUtils.isNetworkConnected(NoticeShowActivity.this)) {            showToast(getString(R.string.network_not_available));            return;        }
        showToast(errorMsg);
    }

    @Override
    public void onSuccess(String code) {

    }

    @Override
    public void onSuccess(BaseResp resp, String code) {
        NoticeResp notice = (NoticeResp) resp;
        webView.loadDataWithBaseURL(null, getNewContent(notice.getContent()), "text/html", "UTF-8", "");
    }
    private LoadingDialog dialog;
    @Override
    public void showLoading() {
        LoadingDialog.Builder loadBuilde = new LoadingDialog.Builder(this)
                .setMessage("加载中...")
                .setCancelable(true)
                .setCancelOutside(true);
        dialog = loadBuilde.create();
        dialog.show();
    }

    @Override
    public void hideLoading() {
        dialog.dismiss();
    }
    @Override
    public void setHeader() {
        super.setHeader();
        title.setText("资讯详情");
    }
    @Override
    public void initViews() {
        webView = (WebView) findViewById((R.id.contentInfo));
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY); //取消滚动条白边效果
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            webView.getSettings().setMixedContentMode(webView.getSettings()
                    .MIXED_CONTENT_ALWAYS_ALLOW);  //注意安卓5.0以上的权限
        }
    }

    @Override
    public void initListeners() {

    }

    @Override
    public void initData() {
        noticePresenter.showNoticeContent(noticeId);
    }
    private String getNewContent(String htmlText) {
        Document doc = Jsoup.parse(htmlText);
        Elements elements = doc.getElementsByTag("img");
        for (Element element : elements) {
            if (element.className() != null && element.className().length() > 0)
                element.attr("width", "100%").attr("height", "auto");
        }
        return doc.toString();

    }
}

package com.mossco.za.mvpapp.article.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.mossco.za.mvpapp.R;
import com.mossco.za.mvpapp.article.presenter.ArticlePresenter;
import com.mossco.za.mvpapp.article.presenter.ArticlesContract;
import com.mossco.za.mvpapp.databinding.ActivityArticleDetailsBinding;
import com.mossco.za.mvpapp.news.model.NewsArticle;
import com.mossco.za.mvpapp.utilities.DrawableUtils;
import com.mossco.za.mvpapp.utilities.StringsUtils;

public class ArticleDetailsActivity extends AppCompatActivity implements ArticlesContract.ArticleView {

    public static final String NEWS_ARTICLE_KEY = "news_article";
    ActivityArticleDetailsBinding binding;
    private ArticlePresenter articlePresenter;
    private ProgressDialog newsProgressDialog;

    public static Intent getStartIntent(Context context, NewsArticle newsArticle) {
        Intent intent = new Intent(context, ArticleDetailsActivity.class);
        intent.putExtra(NEWS_ARTICLE_KEY, newsArticle);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_article_details);
        newsProgressDialog = new ProgressDialog(this);

        setSupportActionBar(binding.mainToolbar);
        getSupportActionBar().setTitle(getString(R.string.news_article));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        onArticleScreenCreated();
    }

    @Override
    public void showArticle(NewsArticle newsArticle) {
        binding.articleDateTextView.setText(StringsUtils.getFormattedDateWithTime(newsArticle.getDateCreated()));
        binding.articleTitleTextView.setText(newsArticle.getHeadline());
        binding.largeImageAltTextView.setText(newsArticle.getLargeImageAlt());
        binding.articleDescription.setText(newsArticle.getStoryBody());
        binding.articleImageView.setImageResource(R.drawable.beast);

        Glide.with(getApplicationContext()).load(StringsUtils.REMOTE_IMAGE_URL.concat(newsArticle.getLargeImageName()))
                .dontAnimate().fitCenter().placeholder(DrawableUtils.getCircularProgressDrawable(this))
                .error(R.drawable.ic_image_not_availabe).into(binding.articleImageView);
    }

    @Override
    public void showProgressDialog() {
        newsProgressDialog.setTitle(getString(R.string.places_loading));
        newsProgressDialog.setMessage(getString(R.string.please_wait_message));
        newsProgressDialog.setIndeterminate(true);
        newsProgressDialog.show();
    }

    @Override
    public void dismissProgressDialog() {
        newsProgressDialog.dismiss();
        finish();
    }

    @Override
    public void showFailedToLoadLatestNewsErrorMessage() {
        showSnackBar(getString(R.string.failed_to_load_error));
    }

    private void showSnackBar(String message) {
        newsProgressDialog.dismiss();
        Snackbar snackbar =
                Snackbar.make(binding.articleImageView, message, Snackbar.LENGTH_LONG).setAction(getString(R.string.retry), v -> {
                    onArticleScreenCreated();
                });
        snackbar.show();
    }

    private void onArticleScreenCreated() {
        articlePresenter = new ArticlePresenter(this);
        if (getIntent() != null && getIntent().hasExtra(NEWS_ARTICLE_KEY)) {
            NewsArticle newsArticle = (NewsArticle) getIntent().getSerializableExtra(NEWS_ARTICLE_KEY);
            if (isNetworkConnectionAvailable()) {
                articlePresenter.loadArticle(newsArticle);
            }else {
                showSnackBar(getString(R.string.network_error));
            }
        }
    }

    boolean isNetworkConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) {
            return false;
        }
        NetworkInfo.State network = info.getState();
        return (network == NetworkInfo.State.CONNECTED || network == NetworkInfo.State.CONNECTING);
    }
}

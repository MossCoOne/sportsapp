package com.mossco.za.mvpapp.news.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.snackbar.Snackbar;
import com.mossco.za.mvpapp.R;
import com.mossco.za.mvpapp.article.view.ArticleDetailsActivity;
import com.mossco.za.mvpapp.databinding.ActivityMainBinding;
import com.mossco.za.mvpapp.news.model.NewsArticle;
import com.mossco.za.mvpapp.news.presenter.NewsContract;
import com.mossco.za.mvpapp.news.presenter.NewsPresenter;
import com.mossco.za.mvpapp.utilities.StringsUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NewsContract.NewsView, NewsArticleAdapter.OnItemClickListener {

    ActivityMainBinding binding;
    private NewsPresenter newsPresenter;
    private NewsArticle mainStory;
    private ProgressDialog newsProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        newsProgressDialog = new ProgressDialog(this);
        setSupportActionBar(binding.mainToolbar);
        getSupportActionBar().setTitle(getString(R.string.news_title));

        onNewsScreenCreated();
        binding.mainStoryContainer
                .setOnClickListener(view -> startActivity(ArticleDetailsActivity.getStartIntent(view.getContext(), mainStory)));
    }

    private void onNewsScreenCreated() {
        newsPresenter = new NewsPresenter(this);
        if (isNetworkConnectionAvailable()) {
            newsPresenter.loadLatestNews();
        } else {
            showSnackBar(getString(R.string.network_error));
        }
    }

    @Override
    public void displayLatestNews(List<NewsArticle> newsArticles) {

        mainStory = getMainStory(newsArticles);

        if (mainStory != null) {
            populateMainStory(mainStory);
        } else {
            ratherDontShowMainStoryView();
        }

        binding.newsArticleRecyclerView.setAdapter(new NewsArticleAdapter(newsArticles, this::onItemClicked));
        binding.newsArticleRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.contentScrollView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showProgressDialog() {
        if (newsProgressDialog != null) {

            newsProgressDialog.setTitle(getString(R.string.places_loading));
            newsProgressDialog.show();
            newsProgressDialog.setMessage(getString(R.string.please_wait_message));
            newsProgressDialog.setIndeterminate(true);
        }
    }

    @Override
    public void dismissProgressDialog() {
        newsProgressDialog.dismiss();
    }

    @Override
    public void showFailedToLoadLatestNewsErrorMessage() {
        showSnackBar(getString(R.string.failed_to_load_error));
    }

    private void showSnackBar(String message) {
        newsProgressDialog.dismiss();
        Snackbar snackbar = Snackbar.make(binding.contentScrollView, message, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.retry), v -> {
                    //reload
                    onNewsScreenCreated();
                });
        snackbar.show();
    }

    @Override
    public void onItemClicked(NewsArticle extraItem) {
        startActivity(ArticleDetailsActivity.getStartIntent(this, extraItem));
    }

    private void ratherDontShowMainStoryView() {
        binding.mainStoryContainer.setVisibility(View.GONE);
    }

    private void populateMainStory(NewsArticle mainStory) {
        binding.categoryChip.setText(mainStory.getCategory());
        binding.headlineStoryTitle.setText(mainStory.getHeadline());
        binding.headlineStoryDateTextView.setText(StringsUtils.getFormattedDate(mainStory.getDateCreated()));
        binding.headlineStoryImageView.setImageResource(R.drawable.beast);
    }

    private NewsArticle getMainStory(List<NewsArticle> newsArticleList) {
        for (NewsArticle newsArticle : newsArticleList) {
            if (newsArticle.isMainStory()) {
                newsArticleList.remove(newsArticle);
                return newsArticle;
            }
        }
        return null;
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

package com.mossco.za.mvpapp.news;

import com.mossco.za.mvpapp.model.NewsRepository;
import com.mossco.za.mvpapp.model.NewsRepositoryImplementation;

import java.util.List;

public class NewsPresenter implements NewsContract.UserActionsListener {

    private NewsRepository newsRepository;
    private NewsContract.View view;

    public NewsPresenter(NewsContract.View view) {
        newsRepository = new NewsRepositoryImplementation();
        this.view = view;
    }

    @Override
    public void loadLatestNews() {
        view.showProgressDialog();
        newsRepository.loadLatestNews(new NewsRepository.LatestNewsCallback() {
            @Override
            public void onLatestNewsLoaded(List<NewsArticle> newsArticles) {
                view.displayLatestNews(newsArticles);
                view.dismissProgressDialog();
            }

            @Override
            public void onErrorOccurred(String errorMessage) {
                view.dismissProgressDialog();
                view.showFailedToLoadLatestNewsErrorMessage();
            }
        });
    }
}

package com.example.mvvmsdemo.moudle;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import com.example.mvvmsdemo.moudle.bean.NewsBean;

import java.util.ArrayList;
import java.util.List;

//负责加载数据
public class NewsModel {
    private List<NewsBean> newsBeans = new ArrayList<>();

    //回调是同步的
    public void load(final Handler handler, final IModel listence) {
        //模拟去网络请求数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 10; i++) {
                        NewsBean newsBean = new NewsBean(System.currentTimeMillis() + "", "内容");
                        newsBeans.add(newsBean);
                    }

                    Thread.sleep(2000);

                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            listence.success();
                        }
                    };

                    handler.post(r);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //回调是异步的
    public void load(final Context context, final IModel listence) {
        //模拟去网络请求数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 10; i++) {
                        NewsBean newsBean = new NewsBean(System.currentTimeMillis() + "", "内容");
                        newsBeans.add(newsBean);
                    }

                    Thread.sleep(2000);


                    listence.success();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public List<NewsBean> get() {
        return newsBeans;
    }
}

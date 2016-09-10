package com.example.rakesh.betaf;

import android.os.AsyncTask;
import android.widget.Toast;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EventsTask extends AsyncTask<String, Void, String> {
    Retrofit retrofit;
    Observable<Data> eventsObservable;


    @Override
    protected void onPreExecute() {
    }

    @Override
    protected String doInBackground(String... strings) {
        final String error = null;
        retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Utilities.base_url)
                .build();

        EventsInterface eventsInterface = retrofit.create(EventsInterface.class);

        eventsObservable = eventsInterface.getEvents(token);

        eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(events -> {
                    if (events.getStatus()==0) {
                        //TODO Add to db
                        //db.addEvent(events.getEvents().get(0));
                    }
                    //else Toast.makeText(LoginActivity.this, "Error, Try Again", Toast.LENGTH_SHORT).show();
                });

        return error;
    }

    @Override
    protected void onPostExecute(String error) {
        super.onPostExecute(error);

       //TODO Redirect to MainActivity if logged in
    }
}

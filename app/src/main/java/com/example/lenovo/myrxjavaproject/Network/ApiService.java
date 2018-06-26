package com.example.lenovo.myrxjavaproject.Network;

import android.database.Observable;

import com.example.lenovo.myrxjavaproject.model.Note;
import com.example.lenovo.myrxjavaproject.model.User;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * This class holds the interface methods of every endpoint by defining the endpoint,
 * request and response Observable.
 */

public interface ApiService {
    //Register new user
    @FormUrlEncoded
    @POST("/notes/user/register")
    Single<User> registerUser(@Field("device_id") String deviceId);

    //create note
    @FormUrlEncoded
    @POST("/notes/new")
    Single<Note> createNote(@Field("note") String note);

    //fetch all notes
    @GET("/notes/all")
    Single<List<Note>> fetchAllNotes();


    /**
     * update single note
     * Id of the note to be updated(Replace the {id} with actual value in the URL)
     **/
    @FormUrlEncoded
    @PUT("notes/{id}")
    Completable updateNote(@Path("id") int noteId, @Field("note") String note);

    // Delete note
    @DELETE("notes/{id}")
    Completable deleteNote(@Path("id") int noteId);
}

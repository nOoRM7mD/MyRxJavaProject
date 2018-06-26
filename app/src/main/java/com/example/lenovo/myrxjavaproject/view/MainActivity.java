package com.example.lenovo.myrxjavaproject.view;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.MainThread;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.myrxjavaproject.MyDividerItemDecoration;
import com.example.lenovo.myrxjavaproject.Network.ApiService;
import com.example.lenovo.myrxjavaproject.R;
import com.example.lenovo.myrxjavaproject.RecyclerTouchListener;
import com.example.lenovo.myrxjavaproject.app.MyApplication;
import com.example.lenovo.myrxjavaproject.model.Note;
import com.example.lenovo.myrxjavaproject.model.User;
import com.example.lenovo.myrxjavaproject.utils.PrefUtil;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private NotesAdapter notesAdapter;
    private List<Note> noteList = new ArrayList<>();
    private ApiService apiService;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.txt_empty_notes_view)
    TextView emptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.activity_title_home));
        setSupportActionBar(toolbar);
        notesAdapter = new NotesAdapter(this, noteList);
        apiService = ((MyApplication) getApplication()).getApiService();

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(notesAdapter);

        /**
         * On long press on RecyclerView item, open alert dialog
         * with options to choose
         * Edit and Delete
         * */
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showNoteDialog(false, null, -1);
            }
        });
        // white background notification bar
        whiteNotificationBar(fab);

        if (TextUtils.isEmpty(PrefUtil.getApiKey(getApplicationContext()))) {
            registerUser();
        } else {
            fetchAllNotes();
        }
    }

    private void showNoteDialog(final boolean shouldUpdate, final Note note, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.note_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputNote = view.findViewById(R.id.note);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!shouldUpdate ? getString(R.string.lbl_new_note_title) : getString(R.string.lbl_edit_note_title));
        if (shouldUpdate && note != null) {
            inputNote.setText(note.getNote());
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(shouldUpdate ? "update" : "save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });
        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(inputNote.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Enter note!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }

                if (shouldUpdate && note != null) {
                    // update note by it's id
                    updateNote(note.getId(), inputNote.getText().toString(), position);
                } else {
                    createNote(inputNote.getText().toString());
                }
            }
        });
    }

    private void createNote(String note) {
        compositeDisposable.add(
                apiService.createNote(note)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<Note>() {

                            @Override
                            public void onSuccess(Note note) {
                                if (!TextUtils.isEmpty(note.getError())) {
                                    Toast.makeText(getApplicationContext(), note.getError(), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                Log.d("CreateNoteSuccess", "new note created: " + note.getId() + ", " + note.getNote() + ", " + note.getTimestamp());

                                // Add new item and notify adapter
                                noteList.add(0, note);
                                notesAdapter.notifyItemInserted(0);

                                toggleEmptyNotes();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("CreateNoteError", "onError: " + e.getMessage());
                                showError(e);
                            }
                        }));
    }


    private void updateNote(final int noteId, final String note, final int position) {
        compositeDisposable.add(
                apiService
                        .updateNote(noteId, note)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                Note currentNote = noteList.get(position);
                                currentNote.setNote(note);
                                //update note and notify adapter
                                noteList.set(position, currentNote);
                                notesAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("UpdateNoteError", "onError: " + e.getMessage());
                                showError(e);
                            }
                        })

        );

    }

    /**
     * Deleting a note
     */
    private void deleteNote(final int noteId, final int position) {
        Log.e("DeleteNote", "deleteNote: " + noteId + ", " + position);
        compositeDisposable.add(
                apiService.deleteNote(noteId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                Log.d("NoteDeleted", "Note deleted! " + noteId);

                                // Remove and notify adapter about item deletion
                                noteList.remove(position);
                                notesAdapter.notifyItemRemoved(position);
                                notesAdapter.notifyDataSetChanged();
                                Toast.makeText(MainActivity.this, "Note deleted!", Toast.LENGTH_SHORT).show();

                                toggleEmptyNotes();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("Delete Note", "onError: " + e.getMessage());
                                showError(e);
                            }
                        })
        );
    }

    private void registerUser() {
        // unique id to identify the device
        String uniqueId = UUID.randomUUID().toString();

        compositeDisposable.add(
                apiService
                        .registerUser(uniqueId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        //subscribe with observer
                        .subscribeWith(new DisposableSingleObserver<User>() {
                            @Override
                            public void onSuccess(User user) {
                                PrefUtil.storeApiKey(getApplicationContext(), user.getApiKey());
                                Toast.makeText(getApplicationContext(),
                                        "Device is registered successfully! ApiKey: " + PrefUtil.getApiKey(getApplicationContext()),
                                        Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("Registration Error", "onError: " + e.getMessage());
                                showError(e);
                            }
                        }));
    }

    private void fetchAllNotes() {
        compositeDisposable.add(
                apiService.fetchAllNotes()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(new Function<List<Note>, List<Note>>() {

                            @Override
                            public List<Note> apply(List<Note> notes) throws Exception {
                                Collections.sort(notes, new Comparator<Note>() {
                                    @Override
                                    public int compare(Note n1, Note n2) {
                                        return n2.getId() - n1.getId();
                                    }
                                });
                                return notes;
                            }
                        }).subscribeWith(new DisposableSingleObserver<List<Note>>() {
                    @Override
                    public void onSuccess(List<Note> notes) {
                        noteList.clear();
                        noteList.addAll(notes);
                        notesAdapter.notifyDataSetChanged();
                        toggleEmptyNotes();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("FetchNoteError", "onError: " + e.getMessage());
                        showError(e);
                    }
                })
        );
    }

    /**
     * Opens dialog with Edit - Delete options
     * Edit - 0
     * Delete - 0
     */
    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                if (index == 0) {
                    showNoteDialog(true, noteList.get(position), position);
                } else {
                    deleteNote(noteList.get(position).getId(), position);
                }
            }
        });
        builder.show();
    }

    private void toggleEmptyNotes() {
        if (noteList.size() > 0) {
            emptyTextView.setVisibility(View.GONE);
        } else {
            emptyTextView.setVisibility(View.VISIBLE);
        }

    }

    private void showError(Throwable e) {
        String message = "";
        try {
            if (e instanceof IOException) {
                message = "No internet connection!";
            } else if (e instanceof HttpException) {
                HttpException error = (HttpException) e;
                String errorBody = error.response().errorBody().string();
                JSONObject jObj = new JSONObject(errorBody);

                message = jObj.getString("error");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (JSONException e1) {
            e1.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        if (TextUtils.isEmpty(message)) {
            message = "Unknown error occurred! Check LogCat.";
        }

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    private void whiteNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}

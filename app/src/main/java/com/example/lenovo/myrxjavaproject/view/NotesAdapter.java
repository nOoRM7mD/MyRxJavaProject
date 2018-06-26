package com.example.lenovo.myrxjavaproject.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lenovo.myrxjavaproject.R;
import com.example.lenovo.myrxjavaproject.model.Note;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * This adapter class renders the RecyclerView with defined layout and data.
 */

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.MyViewHolder> {
    private List<Note> allNotes;
    private Context context;

    public NotesAdapter(Context context, List<Note> allNotes) {
        this.allNotes = allNotes;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_list_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Note currentNotes = allNotes.get(position);
        holder.note.setText(currentNotes.getNote());
        // Displaying dot from HTML character code
        holder.dot.setText(Html.fromHtml("&#8226;"));

        // Changing dot color to random color
        holder.dot.setTextColor(getRandomMaterialColor("400"));

        // Formatting and displaying timestamp
        holder.timeStamp.setText(currentNotes.getTimestamp());
    }

    @Override
    public int getItemCount() {
        int size = allNotes == null ? 0 : allNotes.size();
        return allNotes.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.note)
        TextView note;
        @BindView(R.id.dot)
        TextView dot;
        @BindView(R.id.timestamp)
        TextView timeStamp;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setData(List<Note> data) {
        if (allNotes != null) {
            allNotes.clear();
            allNotes.addAll(data);
            notifyDataSetChanged();
        }
    }

    /**
     * Chooses random color defined in res/array.xml
     */
    private int getRandomMaterialColor(String typeColor) {
        int returnColor = Color.GRAY;
        int arrayId = context.getResources().getIdentifier("mdcolor_" + typeColor, "array", context.getPackageName());

        if (arrayId != 0) {
            TypedArray colors = context.getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.GRAY);
            colors.recycle();
        }
        return returnColor;
    }

    /**
     * Formatting timestamp to `MMM d` format
     * Input: 2018-02-21 00:15:42
     * Output: Feb 21
     */
    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = fmt.parse(dateStr);
            SimpleDateFormat fmtOut = new SimpleDateFormat("MMM d");
            return fmtOut.format(date);
        } catch (ParseException e) {

        }

        return "";
    }
}

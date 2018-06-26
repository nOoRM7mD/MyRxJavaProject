package com.example.lenovo.myrxjavaproject.model;

/**
 * POJO class necessary to serialize the JSON.
 * Defines the note object with id, note and timestamp fields
 */

public class Note extends BaseResponse {
    int id;
    String note;
    String timestamp;

    public int getId() {
        return id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTimestamp() {
        return timestamp;
    }
}

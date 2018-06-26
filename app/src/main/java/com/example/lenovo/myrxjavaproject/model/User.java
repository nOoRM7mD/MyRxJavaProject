package com.example.lenovo.myrxjavaproject.model;

import com.google.gson.annotations.SerializedName;

/**
 * POJO classes necessary to serialize the JSON.
 * User response once the device is registered.
 */

public class User extends BaseResponse {

    @SerializedName("api_key")
    String apiKey;

    public String getApiKey() {
        return apiKey;
    }
}

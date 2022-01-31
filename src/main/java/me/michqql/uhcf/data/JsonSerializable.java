package me.michqql.uhcf.data;

import com.google.gson.JsonElement;

public interface JsonSerializable {

    void read(JsonElement element);
    JsonElement write();
}

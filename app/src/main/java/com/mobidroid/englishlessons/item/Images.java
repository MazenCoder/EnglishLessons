package com.mobidroid.englishlessons.item;

import android.net.Uri;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Images {

//    private Uri image_uri;
    private Map<String, String> images_uri_map = new HashMap<>();
//    private List<String> uriList = new ArrayList<>();

    public Images() { }


//    public List<String> getUriList() {
//        return uriList;
//    }
//
//    public void setUriList(List<String> uriList) {
//        this.uriList = uriList;
//    }

    public Map<String, String> getImages_uri_map() {
        return images_uri_map;
    }

    public void setImages_uri_map(Map<String, String> images_uri_map) {
        this.images_uri_map = images_uri_map;
    }
//
//    public Images(Uri image_uri) {
//        this.image_uri = image_uri;
//    }
//
//    public Uri getImage_uri() {
//        return image_uri;
//    }
//
//    public void setImage_uri(Uri image_uri) {
//        this.image_uri = image_uri;
//    }

}

package com.mobidroid.englishlessons.item;

import java.util.ArrayList;
import java.util.List;

public class KEY {


    public static final String COURSE = "course";
    public static final String IMAGES = "images";
    public static final String IMAGES_MAP = "images_uri_map";
    public static final String VIDEO = "video";
    public static final String ID_COURSE = "course_id";
    public static List<Course> courseList;

    static {
        courseList = new ArrayList<>();

        addCourse();
    }

    private static void addCourse() {
        courseList.add(new Course("https://www.google.com/url?sa=i&source=images&cd=&cad=rja&uact=8&ved=2ahUKEwiJta3wtMTcAhXDhKYKHeKeCsgQjRx6BAgBEAU&url=https%3A%2F%2Fcssauthor.com%2Fmobile-app-ui-psd%2F&psig=AOvVaw3H1m0jCmQJ9dgmwk0rTM2F&ust=1532799265974442", "test"));
        courseList.add(new Course("https://www.google.com/url?sa=i&source=images&cd=&cad=rja&uact=8&ved=2ahUKEwiJta3wtMTcAhXDhKYKHeKeCsgQjRx6BAgBEAU&url=https%3A%2F%2Fcssauthor.com%2Fmobile-app-ui-psd%2F&psig=AOvVaw3H1m0jCmQJ9dgmwk0rTM2F&ust=1532799265974442", "test"));
        courseList.add(new Course("https://www.google.com/url?sa=i&source=images&cd=&cad=rja&uact=8&ved=2ahUKEwiJta3wtMTcAhXDhKYKHeKeCsgQjRx6BAgBEAU&url=https%3A%2F%2Fcssauthor.com%2Fmobile-app-ui-psd%2F&psig=AOvVaw3H1m0jCmQJ9dgmwk0rTM2F&ust=1532799265974442", "test"));
        courseList.add(new Course("https://www.google.com/url?sa=i&source=images&cd=&cad=rja&uact=8&ved=2ahUKEwiJta3wtMTcAhXDhKYKHeKeCsgQjRx6BAgBEAU&url=https%3A%2F%2Fcssauthor.com%2Fmobile-app-ui-psd%2F&psig=AOvVaw3H1m0jCmQJ9dgmwk0rTM2F&ust=1532799265974442", "test"));
        courseList.add(new Course("https://www.google.com/url?sa=i&source=images&cd=&cad=rja&uact=8&ved=2ahUKEwiJta3wtMTcAhXDhKYKHeKeCsgQjRx6BAgBEAU&url=https%3A%2F%2Fcssauthor.com%2Fmobile-app-ui-psd%2F&psig=AOvVaw3H1m0jCmQJ9dgmwk0rTM2F&ust=1532799265974442", "test"));
        courseList.add(new Course("https://www.google.com/url?sa=i&source=images&cd=&cad=rja&uact=8&ved=2ahUKEwiJta3wtMTcAhXDhKYKHeKeCsgQjRx6BAgBEAU&url=https%3A%2F%2Fcssauthor.com%2Fmobile-app-ui-psd%2F&psig=AOvVaw3H1m0jCmQJ9dgmwk0rTM2F&ust=1532799265974442", "test"));
    }
}

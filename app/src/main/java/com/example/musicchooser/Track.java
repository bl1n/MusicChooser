package com.example.musicchooser;

public class Track {
    private String mName;
    private String mPath;

    public Track(String aName, String aPath) {
        mName = aName;
        mPath = aPath;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String aPath) {
        mPath = aPath;
    }

    public String getName() {
        return mName;
    }

    public void setName(String aName) {
        mName = aName;
    }
}

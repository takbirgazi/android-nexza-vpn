package com.data;

import androidx.annotation.Nullable;

public class VPNData {

    public String config;
    public String name;
    @Nullable
    public String username;
    @Nullable
    public String password;

    public VPNData(String config, String name, @Nullable String username,@Nullable String password){
        this.config = config;
        this.name = name;
        this.username = username;
        this.password = password;
    }
}

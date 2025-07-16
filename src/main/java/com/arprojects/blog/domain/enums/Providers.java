package com.arprojects.blog.domain.enums;

public enum Providers {

    BASIC("BASIC"),
    GOOGLE("GOOGLE");

    private final String label;

    Providers(String label){
        this.label = label;
    }

    public String getLabel(){
        return this.label;
    }

    public static Providers fromLabel(String label){
        for(Providers provider: values()){
            if(provider.label.equalsIgnoreCase(label)){
                return provider;
            }
        }

        throw new IllegalArgumentException("Unknown provider: "+label);
    }
}

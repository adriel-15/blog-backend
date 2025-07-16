package com.arprojects.blog.domain.enums;

public enum Authorities {

    ADMIN("ADMIN"),
    WRITER("WRITER"),
    READER("READER");

    private final String label;

    Authorities(String label){
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }

    public static Authorities fromLabel(String label){
        for(Authorities authority: values()){
            if(authority.label.equalsIgnoreCase(label)){
                return authority;
            }
        }

        throw new IllegalArgumentException("Unknown authority: "+label);
    }
}

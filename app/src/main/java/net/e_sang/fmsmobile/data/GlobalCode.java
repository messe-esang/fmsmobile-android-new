package net.e_sang.fmsmobile.data;

public class GlobalCode {
    private String name;       // key가 seq를 의미
    private String value;

    public GlobalCode(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name;
    }
}
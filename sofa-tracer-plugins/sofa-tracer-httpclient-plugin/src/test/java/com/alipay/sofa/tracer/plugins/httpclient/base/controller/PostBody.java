package com.alipay.sofa.tracer.plugins.httpclient.base.controller;

/**
 * PostBody
 *
 * @author yangguanchao
 * @since 2018/08/09
 */
public class PostBody {

    private String name;

    private int age;

    private boolean female;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isFemale() {
        return female;
    }

    public void setFemale(boolean female) {
        this.female = female;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PostBody)) return false;

        PostBody postBody = (PostBody) o;

        if (age != postBody.age) return false;
        if (female != postBody.female) return false;
        return name != null ? name.equals(postBody.name) : postBody.name == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + age;
        result = 31 * result + (female ? 1 : 0);
        return result;
    }
}

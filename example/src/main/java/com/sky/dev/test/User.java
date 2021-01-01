package com.sky.dev.test;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User  {
    private Integer id;
    private String name;
    private Integer age;

    public int compareTo(User o) {
        return o.getAge().compareTo(this.getAge());
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
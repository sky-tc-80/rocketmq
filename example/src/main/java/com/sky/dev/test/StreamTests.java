package com.sky.dev.test;

import com.sky.dev.util.Arrays;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StreamTests {

    public static void main(String[] args) {
        User u1 = new User(1, "a", 21);
        User u2 = new User(2, "b", 22);
        User u3 = new User(3, "c", 23);
        User u4 = new User(4, "d", 24);
        User u5 = new User(5, "e", 25);
        User u6 = new User(6, "f", 26);

        List<User> users = Arrays.asList(u1, u2, u3, u4, u5, u6);
        users.stream()
                .filter(u -> u.getId() % 2 == 0)
                .filter(u -> u.getAge() > 23)
                //.sorted(Comparator.comparing(User::getAge).reversed())
                .map(u -> u.getName().toUpperCase())
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList())
                .forEach(System.out::println);

    }


}

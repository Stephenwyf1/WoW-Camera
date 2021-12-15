package com.wyj.service;

import com.wyj.domain.User;

import java.util.List;

public interface UserService {

    User queryById(Long id);

    User queryByPhoneNumber(User user);

    User login(User user);

    List<User> queryAll();

    void save(User user);

    String sendSMS(String phoneNumber);

//    void saveOperation(Long id, String operation);
//
//    String getUserOperation(Long id);
}

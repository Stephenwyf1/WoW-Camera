package com.wyj.service.impl;

import com.wyj.domain.User;
import com.wyj.mapper.UserMapper;
import com.wyj.service.UserService;
import com.wyj.utils.SendSms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service("userService")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;


    @Override
    public User queryById(Long id) {
        User user = userMapper.selectByPrimaryKey(id);
        return user;
    }

    @Override
    public User queryByPhoneNumber(User user) {
        Example example = new Example(User.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("phoneNumber",user.getPhoneNumber());
        User query_user = userMapper.selectOneByExample(example);
        return query_user;
    }

    @Override
    public User login(User user) {
        User query_user = this.queryByPhoneNumber(user);
        //如果用户不存在，直接注册
        if (query_user == null){
            this.save(user);
            return user;
        }
        return query_user;
    }

    @Override
    public List<User> queryAll() {
        List<User> userList = userMapper.selectAll();
        return userList;
    }

    @Override
    public void save(User user) {
        userMapper.insertSelective(user);
    }

    @Override
    public String sendSMS(String phoneNumber) {
        return SendSms.send(phoneNumber);
    }

//    @Override
//    public void saveOperation(Long id, String operation) {
//        User user = userMapper.selectByPrimaryKey(id);
//        user.setOperation(operation);
//        userMapper.updateByPrimaryKey(user);
//    }
//
//    @Override
//    public String getUserOperation(Long id) {
//        User user = userMapper.selectByPrimaryKey(id);
//        return user.getOperation();
//    }
}

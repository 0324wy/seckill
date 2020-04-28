package com.rockTechnology.miaosha.service;

import com.rockTechnology.miaosha.dao.UserDao;
import com.rockTechnology.miaosha.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service //说明是Service
public class UserService {

    @Autowired
    UserDao userDao; //注解引入userDao
    public User getById(int id){
        return userDao.getById(id);
    }

    public boolean tx(){
        User xiaomajia = new User();
        xiaomajia.setId(2);
        xiaomajia.setName("xiaomajia");
        userDao.Insert(xiaomajia);

        User heiheihei = new User();
        heiheihei.setId(1);
        heiheihei.setName("heiheihei");
        userDao.Insert(heiheihei);
        return true;
    }
}

package com.rockTechnology.miaosha.dao;


import com.rockTechnology.miaosha.domain.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserDao {
    @Select("select * from user where id = #{id}") //用注解就不用xml配置文件了
    public User getById(@Param("id")int id);

    @Insert("insert into user(id,name)values(#{id},#{name})")
    public int Insert(User user);
}

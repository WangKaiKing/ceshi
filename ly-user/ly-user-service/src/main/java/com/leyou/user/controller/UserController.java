package com.leyou.user.controller;

import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("check/{data}/{type}")
    public ResponseEntity<Boolean> check(@PathVariable("type") Integer type, @PathVariable("data") String data){

        Boolean flag = this.userService.check(type,data);
        if (null!=flag && flag){
            return ResponseEntity.ok(flag);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @PostMapping("code")
    public ResponseEntity<Void> sendCode(@RequestParam("phone") String phone){

        try {
            this.userService.sendCode(phone);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    /**
     * 注册
     * @param user
     * @param code
     * @return
     */
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, @RequestParam("code") String code) {
        Boolean boo = this.userService.register(user, code);
        if (boo == null || !boo) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 根据用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
    @PostMapping("query")
    public ResponseEntity<User> login(@RequestParam("username") String username,@RequestParam("password") String password) {
        User user = this.userService.login(username, password);
        if (user != null) {
            return ResponseEntity.ok(user);
        }else{
            return null;
        }
    }

}

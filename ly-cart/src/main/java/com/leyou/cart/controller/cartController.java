package com.leyou.cart.controller;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class cartController {

    @Autowired
    private CartService cartService;

    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart) {

        cartService.addCart(cart);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("modify/addCart2/{carts}")
    public ResponseEntity<List<Cart>> addCart2(@PathVariable("carts") List<Cart> carts,@RequestBody Cart cart) {

        System.out.println("萨达是");


        /*cartService.addCart(cart);

        List<Cart> carts = cartService.queryCarts();*/

        return ResponseEntity.ok(carts);

    }

    @GetMapping
    public ResponseEntity<List<Cart>> queryCarts() {

        List<Cart> carts = cartService.queryCarts();

        if (carts != null) {
            return ResponseEntity.ok(carts);
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("modify/decrement")
    public ResponseEntity<Void> decrementNum(@RequestParam Long skuId) {

        this.cartService.decrementNum(skuId);

        return ResponseEntity.ok().build();
    }

    @PutMapping("modify/increment")
    public ResponseEntity<Void> incrementNum(@RequestParam Long skuId) {

        this.cartService.incrementNum(skuId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable("skuId") String skuId) {
        this.cartService.deleteCart(skuId);
        return ResponseEntity.ok().build();
    }
}

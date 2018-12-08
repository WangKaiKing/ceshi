package com.leyou.item.controller;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @RequestMapping("list")
    public ResponseEntity<List<Category>> queryByParentId(@RequestParam(value = "pid", defaultValue = "0") Long pid) {
        List<Category> categories = this.categoryService.queryByParentId(pid);

        if (categories != null && 0 != categories.size()) {
            return ResponseEntity.ok(categories); //200
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); //204
    }

    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryByBrandId(@PathVariable("bid")Long bid) {
        List<Category> categories = this.categoryService.queryByBrandId(bid);
        if (categories != null && categories.size() != 0) {
            return ResponseEntity.ok(categories);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("names")
    public ResponseEntity<List<String>> queryNameByIds(@RequestParam("ids") List<Long> ids){
        List<String > list = this.categoryService.queryNamesByIds(ids);
        if (list == null || list.size() < 1) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(list);
    }
}

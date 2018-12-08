package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sun.rmi.runtime.Log;

import java.util.List;

@RestController
@RequestMapping("brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> queryBrandByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "desc", defaultValue = "false") Boolean desc,
            @RequestParam(value = "key", required = false) String key) {
        PageResult<Brand> result = this.brandService.queryBrandByPage(page, rows, sortBy, desc, key);
        if (result == null || result.getItems().size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Void> saveBrand(Brand brand, @RequestParam("cids") List<Long> cids) {
        this.brandService.saveBrand(brand, cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping
    public ResponseEntity<Void> updataBrand(Brand brand, @RequestParam("cids") List<Long> cids) {
        this.brandService.updataBrand(brand, cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandByCategory(@PathVariable("cid") Long cid) {
        List<Brand> brands = this.brandService.queryBrandByCategory(cid);
        if (brands != null && 0 != brands.size()) {
            return ResponseEntity.ok(brands);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("bid/{bid}")
    public ResponseEntity<Brand> queryBrandById(@PathVariable("bid") Long bid) {
        Brand brand = this.brandService.queryBrandById(bid);
        if (brand != null) {
            return ResponseEntity.ok(brand);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

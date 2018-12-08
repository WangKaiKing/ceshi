package com.leyou.item.api;

import com.leyou.item.pojo.Brand;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


public interface BrandApi {

    @GetMapping("brand/bid/{bid}")
    Brand queryBrandById(@PathVariable("bid") Long bid);
}

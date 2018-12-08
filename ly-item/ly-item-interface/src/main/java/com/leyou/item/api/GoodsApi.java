package com.leyou.item.api;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface GoodsApi {

    @GetMapping("spu/page")
    PageResult<SpuBo> querySpuByPage(
            @RequestParam(value = "key",required = false) String key,
            @RequestParam(value = "saleable",required = false) Boolean saleable,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows);

    @GetMapping("sku/list")
    List<Sku> querySkuBySpuId(@RequestParam("id") Long id);

    @GetMapping("/spu/detail/{spuid}")
    SpuDetail querySpuDetailById(@PathVariable("spuid") Long spuid);

    @GetMapping("spu/{spuId}")
    Spu querySpuById(@PathVariable("spuId") Long spuId);
}

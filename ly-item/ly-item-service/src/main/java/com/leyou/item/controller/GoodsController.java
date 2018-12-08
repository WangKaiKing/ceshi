package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpuByPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows
    ) {
        PageResult<SpuBo> spuPageResult = this.goodsService.querySpuByPage(key, saleable, page, rows);
        if (spuPageResult != null && 0 != spuPageResult.getItems().size()) {
            return ResponseEntity.ok(spuPageResult);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 新增商品
     *
     * @param spuBo
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spuBo) {
        this.goodsService.saveGoods(spuBo);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo) {
        this.goodsService.updateGoods(spuBo);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/spu/detail/{spuid}")
    public ResponseEntity<SpuDetail> querySpuDetailById(@PathVariable("spuid") Long spuid) {
        SpuDetail detail = this.goodsService.querySpuDetailById(spuid);
        if (detail != null) {
            return ResponseEntity.ok(detail);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@RequestParam("id") Long id) {
        List<Sku> skus = this.goodsService.querySkuBySpuId(id);
        if (skus != null && 0 != skus.size()) {
            return ResponseEntity.ok(skus);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("spu/{spuId}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("spuId") Long spuId) {
        Spu spu = this.goodsService.querySpuById(spuId);
        if (spu != null) {
            return ResponseEntity.ok(spu);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

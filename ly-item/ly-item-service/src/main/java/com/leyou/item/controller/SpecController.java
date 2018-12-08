package com.leyou.item.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecController {

    @Autowired
    private SpecService specService;

    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupByCid(@PathVariable("cid")Long cid) {
        List<SpecGroup> specGroups = this.specService.queryGroupByCid(cid);
        if (specGroups != null && 0 != specGroups.size()) {
            return ResponseEntity.ok(specGroups);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> querySpecParam(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid",required = false) Long cid,
            @RequestParam(value="searching", required = false) Boolean searching,
            @RequestParam(value="generic", required = false) Boolean generic) {
        List<SpecParam> specParams = this.specService.querySpecParam(gid,cid,searching,generic);
        if (specParams != null && 0 != specParams.size()) {
            return ResponseEntity.ok(specParams);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}

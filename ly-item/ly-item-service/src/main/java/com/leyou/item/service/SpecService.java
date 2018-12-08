package com.leyou.item.service;

import com.leyou.item.mapper.SpecMapper;
import com.leyou.item.mapper.SpectParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecService {

    @Autowired
    private SpecMapper specMapper;

    @Autowired
    private SpectParamMapper spectParamMapper;

    public List<SpecGroup> queryGroupByCid(Long cid) {

        //先查询组
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        List<SpecGroup> specGroupList = this.specMapper.select(specGroup);
        //根据每个组的id查询当前组内对应的规格参数
        specGroupList.forEach(group->{

            SpecParam specParam = new SpecParam();
            specParam.setGroupId(group.getId());
            group.setSpecParams(spectParamMapper.select(specParam));
        });
        return specGroupList;
    }

    public List<SpecParam> querySpecParam(Long gid,Long cid) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        return this.spectParamMapper.select(specParam);
    }

    public List<SpecParam> querySpecParam(Long gid, Long cid, Boolean searching, Boolean generic) {

        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);
        specParam.setGeneric(generic);
        return this.spectParamMapper.select(specParam);
    }
}

package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 品牌接口
 */
public interface BrandService {

    public List<TbBrand> findAll();

    /**
     * 品牌分页
     * @param pageNum 当前页面
     * @param pageSize 每页记录数
     * @return
     */
    public PageResult findPage(int pageNum,int pageSize);

    /**
     * 品牌增加
     * @param brand
     */
    public void add(TbBrand brand);

    /**
     * 根据id查询实体
     * @param id
     * @return
     */
    public TbBrand findOne(long id);

    /**
     * 修改品牌信息
     * @param brand
     */
    public void update(TbBrand brand);

    /**
     * 根据复选框中所选id进行删除操作
     * @param ids
     */
    public void delete(Long[] ids);

    /**
     * 根据条件查询，所得结果进行分页
     * @param brand
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageResult findPage(TbBrand brand,int pageNum,int pageSize);

    /**
     * 返回下拉列表数据
     * @return
     */
    public List<Map> selectOptionList();
}

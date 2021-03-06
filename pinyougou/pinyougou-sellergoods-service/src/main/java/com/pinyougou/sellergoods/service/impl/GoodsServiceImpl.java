package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbSellerMapper sellerMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		goods.getGoods().setAuditStatus("0");//设置状态为未审核
		goodsMapper.insert(goods.getGoods());//插入商品基本信息
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());//将商品基本表的id给商品扩展表
		goodsDescMapper.insert(goods.getGoodsDesc());//插入商品扩展表数据

		//设置新增商品的上下架状态为上架"1"
		goods.getGoods().setIsMarketable("1");
		//插入SKU商品数据
		saveItemList(goods);


	}

	//插入列表数据
	private void saveItemList(Goods goods) {
		if ("1".equals(goods.getGoods().getIsEnableSpec())) {

			for (TbItem item:goods.getItemList()){
				//构建标题SPU名称+规格选项值
				String title = goods.getGoods().getGoodsName();//spu全名
				Map<String,Object> map = JSON.parseObject(item.getSpec());
				for (String key : map.keySet()) {
					title += ""+map.get(key);
				}
				item.setTitle(title);

				setItemValues(item,goods);

				itemMapper.insert(item);
			}
		} else {

			//没有启用规格
			TbItem item = new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());//标题
			item.setPrice(goods.getGoods().getPrice());//价格
			item.setNum(77777);//库存数量
			item.setStatus("1");//状态
			item.setIsDefault("1");//默认
			item.setSpec("{}");//规格

			setItemValues(item,goods);

			itemMapper.insert(item);
		}
	}


	private void setItemValues(TbItem item,Goods goods) {

		//商品分类
		item.setCategoryid(goods.getGoods().getCategory3Id());
		item.setCreateTime(new Date());//创建日期
		item.setUpdateTime(new Date());//更新日期

		item.setGoodsId(goods.getGoods().getId());//商品id
		item.setSellerId(goods.getGoods().getSellerId());//商家id

		//分类名称
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());

		//品牌名称
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());

		//商家名称(店铺名称)
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(seller.getNickName());

		//图片
		List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(),Map.class);
		if (imageList.size()>0){
			item.setImage((String)imageList.get(0).get("url"));
		}

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//商品数据发生变化时，修改商品状态为"0"，则solr索引库的数据同时也会被删除,这样就不会出现数据库数据发生变化，而索引库数据未发生变化的情况
		goods.getGoods().setAuditStatus("0");
		//更新基本表数据
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		//更新扩展表数据
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());

		//删除原有的SKU商品数据
		TbItemExample example=new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);

		//插入SKU商品数据
		saveItemList(goods);

	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){

		Goods goods = new Goods();
		//商品基本表
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		//商品扩展表
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(tbGoodsDesc);
		//读取SKU列表
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> itemList = itemMapper.selectByExample(example);
		goods.setItemList(itemList);

		return goods;

		//return goodsMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//goodsMapper.deleteByPrimaryKey(id);
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            goods.setIsDelete("1");//表示逻辑删除
            goodsMapper.updateByPrimaryKey(goods);
        }
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		//指定条件为未逻辑删除
        criteria.andIsDeleteIsNull();
		
		if(goods!=null){			
			if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				//criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateStatus(Long[] ids, String status) {
		for (Long id : ids) {
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(goods);
		}
	}

	@Override
	public void updateIsMarketable(Long[] ids,String isMarketable) {
		for (Long id : ids) {
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsMarketable(isMarketable);
			goodsMapper.updateByPrimaryKey(goods);
		}
	}

	/**
	 * 根据spu的id集合查询sku列表
	 * @param goodsIds
	 * @param status
	 * @return
	 */
	@Override
	public List<TbItem> findItemListByGoodsIdListAndStatus(Long[] goodsIds,String status){
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo(status);//状态
		criteria.andGoodsIdIn(Arrays.asList(goodsIds));//指定条件：spuid集合
		return itemMapper.selectByExample(example);
	}

}

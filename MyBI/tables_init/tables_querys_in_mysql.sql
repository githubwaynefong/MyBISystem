# 查询所有订单信息
select od.id
	  ,case when od.order_status = '00' then '新订单未付款'
            when od.order_status = '01' then '已付款配送中'
            when od.order_status = '02' then '已收货'
            when od.order_status = '99' then '订单取消'
	   end order_status
      ,od.order_date
      ,cm.name customer_name
      ,od.shipping_handling
      ,od.ship_to_name
      ,ad.address ship_to_address
      ,od.subtotal
      ,im.name item_name
      ,odts.unitprice
      ,odts.quantity
      ,odts.subtotal
      ,imtp.name item_type
      ,ct.name catery_name
from order_items odts
join orders od on od.id = odts.order_id
join customer cm on cm.id = od.customer_id
join items im on im.id = odts.item_id
join item_catery_lnk lnk on lnk.item_id = im.id
join cateries ct on ct.id = lnk.catery_id
join item_type imtp on imtp.id = im.item_type_id
join address ad on ad.id = od.ship_to_address_id;
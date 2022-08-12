package com.zc.gulimall.order.web;

import com.zc.common.constant.GlobalUrlConstant;
import com.zc.common.exception.NoStockException;
import com.zc.gulimall.order.entity.vo.OrderConfirmVo;
import com.zc.gulimall.order.entity.vo.OrderSubmitVo;
import com.zc.gulimall.order.entity.vo.SubmitOrderResponseVo;
import com.zc.gulimall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderWebController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) {
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();

        model.addAttribute("orderConfirmData", orderConfirmVo);
        //展示订单确认的数据
        return "confirm";
    }

    /**
     * 下单
     * @param orderSubmitVo
     * @return
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes) {

        try {
            SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);

            System.out.println("订单提交的数据..." + vo);
            if(responseVo.getCode() == 0) {
                //下单成功来到支付页面
                model.addAttribute("SubmitOrderResponseVo", responseVo);
                return "pay";
            } else {
                //下单失败回到订单确认页重新确认订单信息
                String msg = "下单失败";
                switch (responseVo.getCode()) {
                    case 1: msg += "订单信息过期，请刷新再次提交"; break;
                    case 2: msg += "订单商品价格发生变化，请确认后再次提交"; break;
                    case 3: msg += "库存锁定失败，商品库存不足"; break;
                }
                redirectAttributes.addFlashAttribute("msg", msg);
                return "redirect:" + GlobalUrlConstant.ORDER_SERVER_URL + "/toTrade";
            }
        } catch (NoStockException e) {
            redirectAttributes.addFlashAttribute("msg", e.getMessage());
            return "redirect:" + GlobalUrlConstant.ORDER_SERVER_URL + "/toTrade";
        }

    }
}

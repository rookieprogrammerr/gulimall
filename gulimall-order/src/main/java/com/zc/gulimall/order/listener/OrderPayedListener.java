package com.zc.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.zc.gulimall.order.entity.vo.PayAsyncVo;
import com.zc.gulimall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class OrderPayedListener {
    @Autowired
    private OrderService orderService;

    @PostMapping("/payed/notify")
    public String handleAlipayed(PayAsyncVo vo, HttpServletRequest request) {
        //只要我们收到了支付宝给我们异步的通知，告诉我们订单支付成功。返回success。支付宝就再也不通知
        //验签
        try {
            if(orderService.rasCheckV1(request)) {
                System.out.println("签名验证成功...");
                return orderService.handlePayResult(vo);
            } else {
                System.out.println("签名验证失败...");
                return "error";
            }
        } catch (AlipayApiException e) {
            System.out.println("签名验证异常...");
            return "error";
        }
    }
}

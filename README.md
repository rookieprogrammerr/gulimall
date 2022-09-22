# 环境信息

| 软件名        | 版本号     | 描述                   |
| ------------- |---------| ---------------------- |
| nginx         | 1.21.5  | 反向代理服务器         |
| elasticsearch | 7.4.2   | 搜索                   |
| kibana        | 7.4.2   |                        |
| nacos         | 1.2.1   | 服务注册中心和配置中心 |
| redis         | 6.2.6   | 缓存等                 |
| mysql         | 8.0.27  | 数据库                 |

# 模块介绍

| 模块名              | 模块中文名   | 描述                                          |
|------------------|---------|---------------------------------------------|
| mall-auth-server | 认证服务    | 社交登录、Oauth2.0、单点登录                          |
| mall-common      | 公共模块    | 保存常量、异常码、工具类、通用实体等                          |
| mall-coupon      | 优惠券模块   | 优惠券服务                                       |
| mall-gateway     | 网关模块    | 网关接收前端请求做统一转发                               |
| mall-member      | 会员模块    | 会员服务                                        |
| mall-order       | 订单模块    | 订单服务                                        |
| mall-product     | 商品模块    | 商品服务                                        |
| mall-search      | 搜索服务    | Elasticsearch 操作服务                          |
| mall-seckill     | 秒杀模块    | 秒杀服务                                        |
| mall-third-party | 第三方整合服务 | 第三方服务、阿里云OSS等                               |
| mall-ware        | 库存模块    | 仓储服务                                        |
| renren-fast      | 人人后台生成  | [人人开源](https://gitee.com/renrenio) 后端快速开发平台 |
| renren-fast-vue  | 后台管理前端  | [人人开源](https://gitee.com/renrenio) 前端快速开发平台 |

# 域名配置
C:\Windows\System32\drivers\etc\hosts 文件
```text
# guli mall #
192.168.124.129		gulimall.com
192.168.124.129		search.gulimall.com
192.168.124.129		item.gulimall.com
192.168.124.129		auth.gulimall.com
192.168.124.129		cart.gulimall.com
192.168.124.129		order.gulimall.com
192.168.124.129		member.gulimall.com
192.168.124.129		seckill.gulimall.com
```

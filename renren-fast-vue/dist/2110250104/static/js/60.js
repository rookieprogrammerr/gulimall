webpackJsonp([60],{yZ7W:function(e,a,t){"use strict";Object.defineProperty(a,"__esModule",{value:!0});var r={data:function(){return{visible:!1,dataForm:{id:0,assigneeId:"",assigneeName:"",phone:"",priority:"",status:0,wareId:"",amount:"",createTime:"",updateTime:""},dataRule:{assigneeId:[{required:!0,message:"采购人id不能为空",trigger:"blur"}],assigneeName:[{required:!0,message:"采购人名不能为空",trigger:"blur"}],phone:[{required:!0,message:"联系方式不能为空",trigger:"blur"}],priority:[{required:!0,message:"优先级不能为空",trigger:"blur"}],status:[{required:!0,message:"状态不能为空",trigger:"blur"}],wareId:[{required:!0,message:"仓库id不能为空",trigger:"blur"}],amount:[{required:!0,message:"总金额不能为空",trigger:"blur"}],createTime:[{required:!0,message:"创建日期不能为空",trigger:"blur"}],updateTime:[{required:!0,message:"更新日期不能为空",trigger:"blur"}]}}},methods:{init:function(e){var a=this;this.dataForm.id=e||0,this.visible=!0,this.$nextTick(function(){a.$refs.dataForm.resetFields(),a.dataForm.id&&a.$http({url:a.$http.adornUrl("/ware/purchase/info/"+a.dataForm.id),method:"get",params:a.$http.adornParams()}).then(function(e){var t=e.data;t&&0===t.code&&(a.dataForm.assigneeId=t.purchase.assigneeId,a.dataForm.assigneeName=t.purchase.assigneeName,a.dataForm.phone=t.purchase.phone,a.dataForm.priority=t.purchase.priority,a.dataForm.status=t.purchase.status,a.dataForm.wareId=t.purchase.wareId,a.dataForm.amount=t.purchase.amount,a.dataForm.createTime=t.purchase.createTime,a.dataForm.updateTime=t.purchase.updateTime)})})},dataFormSubmit:function(){var e=this;this.$refs.dataForm.validate(function(a){a&&e.$http({url:e.$http.adornUrl("/ware/purchase/"+(e.dataForm.id?"update":"save")),method:"post",data:e.$http.adornData({id:e.dataForm.id||void 0,assigneeId:e.dataForm.assigneeId,assigneeName:e.dataForm.assigneeName,phone:e.dataForm.phone,priority:e.dataForm.priority,status:e.dataForm.status,wareId:e.dataForm.wareId,amount:e.dataForm.amount,createTime:e.dataForm.createTime,updateTime:e.dataForm.updateTime})}).then(function(a){var t=a.data;t&&0===t.code?e.$message({message:"操作成功",type:"success",duration:1500,onClose:function(){e.visible=!1,e.$emit("refreshDataList")}}):e.$message.error(t.msg)})})}}},i={render:function(){var e=this,a=e.$createElement,t=e._self._c||a;return t("el-dialog",{attrs:{title:e.dataForm.id?"修改":"新增","close-on-click-modal":!1,visible:e.visible},on:{"update:visible":function(a){e.visible=a}}},[t("el-form",{ref:"dataForm",attrs:{model:e.dataForm,rules:e.dataRule,"label-width":"120px"},nativeOn:{keyup:function(a){if(!("button"in a)&&e._k(a.keyCode,"enter",13,a.key,"Enter"))return null;e.dataFormSubmit()}}},[t("el-form-item",{attrs:{label:"优先级",prop:"priority"}},[t("el-input",{attrs:{placeholder:"优先级"},model:{value:e.dataForm.priority,callback:function(a){e.$set(e.dataForm,"priority",a)},expression:"dataForm.priority"}})],1)],1),e._v(" "),t("span",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[t("el-button",{on:{click:function(a){e.visible=!1}}},[e._v("取消")]),e._v(" "),t("el-button",{attrs:{type:"primary"},on:{click:function(a){e.dataFormSubmit()}}},[e._v("确定")])],1)],1)},staticRenderFns:[]},s=t("VU/8")(r,i,!1,null,null,null);a.default=s.exports}});
webpackJsonp([58],{LQYY:function(a,e,t){"use strict";Object.defineProperty(e,"__esModule",{value:!0});var r={data:function(){return{visible:!1,dataForm:{id:0,name:"",address:"",areacode:""},dataRule:{name:[{required:!0,message:"仓库名不能为空",trigger:"blur"}],address:[{required:!0,message:"仓库地址不能为空",trigger:"blur"}],areacode:[{required:!0,message:"区域编码不能为空",trigger:"blur"}]}}},methods:{init:function(a){var e=this;this.dataForm.id=a||0,this.visible=!0,this.$nextTick(function(){e.$refs.dataForm.resetFields(),e.dataForm.id&&e.$http({url:e.$http.adornUrl("/ware/wareinfo/info/"+e.dataForm.id),method:"get",params:e.$http.adornParams()}).then(function(a){var t=a.data;t&&0===t.code&&(e.dataForm.name=t.wareInfo.name,e.dataForm.address=t.wareInfo.address,e.dataForm.areacode=t.wareInfo.areacode)})})},dataFormSubmit:function(){var a=this;this.$refs.dataForm.validate(function(e){e&&a.$http({url:a.$http.adornUrl("/ware/wareinfo/"+(a.dataForm.id?"update":"save")),method:"post",data:a.$http.adornData({id:a.dataForm.id||void 0,name:a.dataForm.name,address:a.dataForm.address,areacode:a.dataForm.areacode})}).then(function(e){var t=e.data;t&&0===t.code?a.$message({message:"操作成功",type:"success",duration:1500,onClose:function(){a.visible=!1,a.$emit("refreshDataList")}}):a.$message.error(t.msg)})})}}},o={render:function(){var a=this,e=a.$createElement,t=a._self._c||e;return t("el-dialog",{attrs:{title:a.dataForm.id?"修改":"新增","close-on-click-modal":!1,visible:a.visible},on:{"update:visible":function(e){a.visible=e}}},[t("el-form",{ref:"dataForm",attrs:{model:a.dataForm,rules:a.dataRule,"label-width":"120px"},nativeOn:{keyup:function(e){if(!("button"in e)&&a._k(e.keyCode,"enter",13,e.key,"Enter"))return null;a.dataFormSubmit()}}},[t("el-form-item",{attrs:{label:"仓库名",prop:"name"}},[t("el-input",{attrs:{placeholder:"仓库名"},model:{value:a.dataForm.name,callback:function(e){a.$set(a.dataForm,"name",e)},expression:"dataForm.name"}})],1),a._v(" "),t("el-form-item",{attrs:{label:"仓库地址",prop:"address"}},[t("el-input",{attrs:{placeholder:"仓库地址"},model:{value:a.dataForm.address,callback:function(e){a.$set(a.dataForm,"address",e)},expression:"dataForm.address"}})],1),a._v(" "),t("el-form-item",{attrs:{label:"区域编码",prop:"areacode"}},[t("el-input",{attrs:{placeholder:"区域编码"},model:{value:a.dataForm.areacode,callback:function(e){a.$set(a.dataForm,"areacode",e)},expression:"dataForm.areacode"}})],1)],1),a._v(" "),t("span",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[t("el-button",{on:{click:function(e){a.visible=!1}}},[a._v("取消")]),a._v(" "),t("el-button",{attrs:{type:"primary"},on:{click:function(e){a.dataFormSubmit()}}},[a._v("确定")])],1)],1)},staticRenderFns:[]},d=t("VU/8")(r,o,!1,null,null,null);e.default=d.exports}});
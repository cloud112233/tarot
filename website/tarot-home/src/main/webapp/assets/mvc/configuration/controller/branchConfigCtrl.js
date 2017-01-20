angular.module('myee', [])
    .controller('branchConfigCtrl', branchConfigCtrl);

/**
 * branchConfigCtrl - controller
 */
branchConfigCtrl.$inject = ['$scope','$resource', 'cResource', '$filter', 'cfromly', 'Constants', 'cAlerts', 'toaster', '$rootScope', '$timeout', '$q','cTables','NgTableParams'];
function branchConfigCtrl($scope,$resource, cResource, $filter, cfromly, Constants, cAlerts, toaster, $rootScope, $timeout, $q,cTables, NgTableParams) {
    var lang = $rootScope.lang_zh;
    var iDatatable = 0, iConfig = 1;
    $scope.activeTab = iDatatable;

//升级配置------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    var mgrData = $scope.mgrData =  {
        fields: [
            {key: 'name', type: 'c_input',className:'c_formly_line', templateOptions: {'label': '分支主标题', required: true, placeholder: '100字以内',maxlength: 100,isSearch:true}},
            {key: 'subName', type: 'c_input',className:'c_formly_line', templateOptions: {'label': '分支副标题', required: true, placeholder: '100字以内',maxlength: 100,isSearch:true}},
            {key: 'manager', type: 'c_input',className:'c_formly_line', templateOptions: {'label': '负责人', required: true, placeholder: '负责人姓名,100字以内',maxlength: 100,isSearch:true}},
            {
                key: 'description',
                type: 'c_textarea',
                ngModelAttrs: {style: {attribute: 'style'}},
                templateOptions: {label: '描述', placeholder: '描述,255字以内', rows: 10, style: 'max-width:500px',maxlength:255,isSearch:false}
            }
        ],
        api: {
            read: './configuration/branch/paging',
            update: './configuration/branch/update',
            delete: './configuration/branch/delete',
        },
    };

    cTables.initNgMgrCtrl(mgrData, $scope);
    cTables.cfromlyData(mgrData.fields);

    //formly提交
    $scope.processSubmit = function () {
        var formly = $scope.formData;
        if (formly.form.$valid) {
            $scope.disableSubmit = true;
            formly.options.updateInitialValue();
            var data = {
                id:formly.model.id,
                name:formly.model.name,
                subName:formly.model.subName,
                manager:formly.model.manager,
                description:formly.model.description,
                createTime:new Date().getTime(),
            }
            cResource.save(mgrData.api.update,{},data).then($scope.saveSuccess);
        }
    };

}
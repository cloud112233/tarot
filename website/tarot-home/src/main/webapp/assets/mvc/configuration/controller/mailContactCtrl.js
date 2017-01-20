angular.module('myee', [])
    .controller('mailContactConfigCtrl', mailContactConfigCtrl);

mailContactConfigCtrl.$inject = ['$scope', 'Constants', 'cTables', 'cfromly', 'cAlerts'];
function mailContactConfigCtrl($scope, Constants, cTables, cfromly, cAlerts) {
    var mgrData = $scope.mgrData = {
        fields: [
            {
                key: 'name',
                type: 'c_input',
                templateOptions: {label: '名称', required: true, placeholder: '名称,40字以内', maxlength: 40,isSearch:true}
            },
            {
                key: 'mailAddress',
                type: 'c_input',
                templateOptions: {type: 'email', label: '电子邮件', required: false, placeholder: '电子邮件,60字以内', maxlength: 60,isSearch:true}
            }
        ],
        api: {
            read: '../admin/configuration/mailcontact/paging',
            update: '../admin/configuration/mailcontact/save',
            delete: '../admin/configuration/mailcontact/delete'
        }
    };
    cTables.initNgMgrCtrl(mgrData, $scope);
    cTables.cfromlyData(mgrData.fields);
}
Ext.define('sphinx.view.Header', {
    extend: 'Ext.Container',
    xtype: 'appHeader',
    id: 'app-header',
    height: 52,
    layout: {
        type: 'hbox',
        align: 'middle'
    },
    
    defaults: { // defaults are applied to items, not the container
    	margins: '5 5 5 5'
    },
    
    margins: '5 5 5 5',
    padding:'5 5 5 5',
    initComponent: function() {
        this.items = [{
            xtype: 'component',
            html: '<span style="font-size: 32px;font-weight: bolder;color:#FFF">新农村合作医疗管理系统</span>',
            flex: 1
        },{
            xtype: 'component',
            html: '<span style="font-size: 12px;font-weight: bolder;color:#FFF">'+Ext.util.Cookies.get('username')+'</span>'
        },{
        	xtype: 'tool',
            type: 'gear'
        },{
        	xtype: 'tool',
            type: 'close'
        },{
        	xtype: 'tool',
            type: 'help'
        }];


        this.callParent();
    }
});

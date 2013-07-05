Ext.define('sphinx.view.search.SearchForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.searchForm',

    layout: 'fit',
    autoShow: false,
    title: '查询',
    initComponent: function() {
    	autoHeight: true,
        this.callParent(arguments);
    },
    
    defaults: {
		anchor: '100%',
		labelWidth: 100
	},
	
    buttons: [{
     	 text   : 'Search',
     	 action: 'search'
      }, {
     	 text   : 'Reset',
     	 handler: function() {
     		 this.up('form').getForm().reset();
     	 }
      }],
      items   : [	{
		    xtype: 'textarea',
		    style: 'margin:5',
		    hideLabel: true,
		    name: 'msg',
		    anchor: '-5 -5' ,
		    value: 'select * from cmis:document'
		}]
      
      
});

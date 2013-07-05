Ext.define('sphinx.controller.App', {
    extend: 'Ext.app.Controller',
    
    views: ['Header'],
    
    init: function() {
    	this.control({
            'appHeader tool[type=close]': {
            	click: this.onLogout,
            }
        });
    },

    onLogout : function( tool ){
    	Ext.util.Cookies.clear('Authorization');
    	Ext.util.Cookies.clear('username');
    	window.location.href = 'login.html';
    }
    
    
});
Ext.Loader.setConfig({disableCaching:false});

Ext.view.BoundList.override( {
	getInnerTpl: function(displayField) {
        return '{[values["' + displayField + '"]]}';
    }
});

Ext.Ajax.on("beforerequest", function(conn, options, eOpts){
	if(!Ext.util.Cookies.get('Authorization')){
		window.location.href = 'login.html';
	}
	options = options || {};
	options.headers = options.headers || {};
    options.headers['Authorization']=Ext.util.Cookies.get('Authorization');
});

Ext.Ajax.on("requestexception", function(conn, response, options, eOpts){
	//options = options || {};
});



Ext.form.action.Submit.override({
  processResponse : function(response){
      this.response = response;
      return true;
  }
});

Ext.application({
    requires: ['Ext.container.Viewport'],
    name: 'sphinx',

    appFolder: 'app',
    controllers: [
                  'App',
                  'FolderTree',
                  'ContentGrid',
                  'ContentGridContextmenu',
                  'SystemTree',                  
                  'Search',
                  'RepositoryInfo',
                  'system.Users',
                  'system.Groups',
                  'system.Types',
                  'object.ACL'
              ],

    launch: function() {
    	if(!Ext.util.Cookies.get('Authorization')){
    		window.location.href = 'login.html';
    		return;
    	}
    	
    	
        Ext.app = function(){
            var msgCt;

            function createBox(t, s){
               return '<div class="msg"><h3>' + t + '</h3><p>' + s + '</p></div>';
            }
            return {
                msg : function(title, format){
                    if(!msgCt){
                        msgCt = Ext.DomHelper.insertFirst(document.body, {id:'msg-div'}, true);
                    }
                    var s = Ext.String.format.apply(String, Array.prototype.slice.call(arguments, 1));
                    var m = Ext.DomHelper.append(msgCt, createBox(title, s), true);
                    m.hide();
                    m.slideIn('t').ghost("t", { delay: 1000, remove: true});
                },

                init : function(){
                    if(!msgCt){
                        msgCt = Ext.DomHelper.insertFirst(document.body, {id:'msg-div'}, true);
                    }

                }
            };
        }();
    	
    	
    	Ext.onReady(function() {
    		
    		Ext.state.Manager.setProvider(Ext.create('Ext.state.CookieProvider'));
    	    Ext.QuickTips.init();
    	    Ext.app.init();
    	    Ext.state.Manager.setProvider(Ext.create('Ext.state.CookieProvider'));

    	    var viewport = Ext.create('Ext.Viewport', {
    	        id: 'border-example',
    	        layout: 'border',
    	        items: [
    	        Ext.create('sphinx.view.Header', {
    	            region: 'north',
    	            height: 50
    	        }), {
    	            region: 'west',
    	            id: 'west-panel',
    	            split: true,
    	            width: 200,
    	            minWidth: 175,
    	            maxWidth: 400,
    	            collapsible: true,
    	            animCollapse: true,
    	            margins: '0 0 0 5',
    	            layout: 'accordion',
    	            items: [
    	                Ext.create('sphinx.view.document.FolderTree',{title:'文档管理',iconCls: 'info',stateId: 'navigation-panel2',stateful: true}),
    	                Ext.create('sphinx.view.system.SystemTree',{title:'系统管理',iconCls: 'info',stateId: 'navigation-panel1', stateful: true}),
    	                {xtype:'searchForm',title:'查询管理',iconCls: 'info',stateId: 'navigation-panel1', stateful: true }
    	            
    	          ]
    	        },
    	        Ext.create('Ext.tab.Panel', {
    	        	id:'centerPanel',
    	            region: 'center',
    	            deferredRender: false,
    	            activeTab: 0, 
    	            items: [{
    	                contentEl: 'center1',
    	                title: '公告',
    	                autoScroll: true
    	            }
    	            
    	            ]
    	        })]
    	    });
    	});
    }

});




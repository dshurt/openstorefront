/* 
 * Copyright 2017 Space Dynamics Laboratory - Utah State University Research Foundation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * See NOTICE.txt for more information.
 */

/* global Ext, CoreService */

Ext.define('OSF.landing.DefaultDisclaimer', {
	extend: 'Ext.panel.Panel',
    alias: 'widget.osf-defaultdisclaimer',
    
    bodyCls: 'home-footer',
	layout: 'center',
    width: '100%',
    // hidden:true,
    
	items: [
		{
			xtype: 'panel',
            itemId: 'disclaimer',
            //css classes are only effective when applied here
            bodyCls: 'home-footer home-footer-disclaimer-text',
			width: '100%',
            html: '<b>Disclaimer</b>',
		}
    ],

	initComponent: function () {

        console.log("pleas work")
		this.callParent();	
        var disclaimerPanel = this;
		
		// CoreService.brandingservice.getCurrentBranding().then(function(branding){
		// 	disclaimerPanel.getComponent('disclaimer').update(
        //         '<i>' + branding.disclaimerMessage + '</i>'
        //         );	
        //     disclaimerPanel.ownerCt.bodyCls = disclaimerPanel.ownerCt.bodyCls + "home-footer-disclaimer";
            
        //     if(branding.disclaimerMessage){
        //         disclaimerPanel.ownerCt.hidden = false;
        //     }

		// });
		
	}
	
});


/* 
 * Copyright 2016 Space Dynamics Laboratory - Utah State University Research Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* global Ext, CoreService, CoreUtil */

Ext.define('OSF.component.RootEvaluationPanel', {
	extend: 'Ext.panel.Panel',
	alias: 'osf.widget.RootEvaluationPanel',
	readOnly: false,
	initComponent: function () {
		this.callParent();
		var rootEvalPanel = this;

		var changeHistory = Ext.create('OSF.component.ChangeLogWindow', {									
		});

		rootEvalPanel.contentPanel = Ext.create('Ext.panel.Panel', {

			region: 'center',			
			layout: 'fit',
			itemId: 'contentPanel',
			dockedItems: [
				{
					xtype: 'toolbar',
					dock: 'top',
					itemId: 'tools',					
					cls: 'eval-form-title',
					items: [
						{
							xtype: 'tbfill'
						},
						{
							xtype: 'panel',
							itemId: 'title',							
							tpl: '<h1 style="color: white;">{title}</h1>'					
						},
						{
							xtype: 'tbfill'
						},
						{
							text: 'Change History',
							itemId: 'changeHistoryBtn',
							iconCls: 'fa fa-2x fa-history icon-button-color-default icon-vertical-correction',
							scale: 'medium',
							handler: function() {									
								changeHistory.show();
								
								changeHistory.load({
									entity: 'Component',												
									entityId: rootEvalPanel.componentId,
									includeChildren: true,
									addtionalLoad: function(data, changeWindow) {
										changeWindow.setLoading(true);
										Ext.Ajax.request({
											url: 'api/v1/resource/changelogs/Evaluation/' + rootEvalPanel.evaluationId + '?includeChildren=true',
											callback: function() {
												changeWindow.setLoading(false);
											},
											success: function(response, opts) {
												var extraData = Ext.decode(response.responseText);												
												Ext.Array.push(data, extraData);
												data.sort(function(a, b){
													return Ext.Date.parse(b.createDts, 'C') - Ext.Date.parse(a.createDts, 'C');
												});												
												changeWindow.updateData(data);
											}
										});
									}
								});
							}
						}
					]
				}
			]			
		});

		rootEvalPanel.commentPanel = Ext.create('Ext.panel.Panel', {
			title: 'Comments',
			iconCls: 'fa fa-lg fa-comment',
			region: 'east',			
			collapsed: true,
			collapsible: true,
			animCollapse: false,
			floatable: false,
			titleCollapse: true,
			width: 375,
			minWidth: 250,
			split: true,			
			bodyStyle: 'background: white;',
			layout: 'fit',
			items: [
				{
					xtype: 'panel',
					itemId: 'comments',
					bodyStyle: 'padding: 10px;',
					scrollable: true,
					items: [						
					],
					dockedItems: [
						{
							xtype: 'form',
							itemId: 'form',
							dock: 'bottom',
							layout: 'anchor',
							items: [
								{
									xtype: 'hidden',
									name: 'commentId'
								},
								{
									xtype: 'hidden',
									name: 'replyCommentId'
								},
								{
									xtype: 'htmleditor',
									name: 'comment',									
									width: '100%',
									fieldBodyCls: 'form-comp-htmleditor-border',
									maxLength: 4000
								}
							],
							dockedItems: [
								{
									xtype: 'toolbar',
									dock: 'bottom',
									items: [
										{
											text: 'Comment',
											iconCls: 'fa fa-lg fa-comment icon-button-color-save',
											handler: function(){
												var form = this.up('form');
												var data = form.getValues();
												data.acknowledge = false;
												
												var method = 'POST';
												var update = '';		
												if (data.commentId) {
													method = 'PUT',
													update = '/' + data.commentId;		
												}
												var evaluationId = rootEvalPanel.commentPanel.lastLoadOpt.evaluationId;
												var entity = rootEvalPanel.commentPanel.lastLoadOpt.entity;
												var entityId = rootEvalPanel.commentPanel.lastLoadOpt.entityId;
												if (!entity) {
													data.entity = 'Evaluation';
													data.entityId = evaluationId;	
												} else {
													data.entity = entity;
													data.entityId = entityId;
												}
												
												CoreUtil.submitForm({
													url: 'api/v1/resource/evaluations/' + evaluationId + '/comments' + update,
													method: method,
													data: data,
													form: form,
													success: function(){
														rootEvalPanel.commentPanel.loadComments(evaluationId, entity, entityId);														
														form.reset();
														
														if (rootEvalPanel.commentPanel.getComponent('comments').replyMessage) {
															rootEvalPanel.commentPanel.getComponent('comments').removeDocked(rootEvalPanel.commentPanel.getComponent('comments').replyMessage, true);
															rootEvalPanel.commentPanel.getComponent('comments').replyMessage = null;
														}
														if (rootEvalPanel.commentPanel.getComponent('comments').editMessage) {
															rootEvalPanel.commentPanel.getComponent('comments').removeDocked(rootEvalPanel.commentPanel.getComponent('comments').editMessage, true);
															rootEvalPanel.commentPanel.getComponent('comments').editMessage = null;
														}														
													}
												});												
												
											}
										},
										{
											xtype: 'tbfill'
										},
										{
											text: 'Cancel',
											itemId: 'cancel',											
											iconCls: 'fa fa-lg fa-close icon-button-color-warning',
											handler: function(){										
												var form = this.up('form');
												form.reset();
												if (rootEvalPanel.commentPanel.getComponent('comments').replyMessage) {
													rootEvalPanel.commentPanel.getComponent('comments').removeDocked(rootEvalPanel.commentPanel.getComponent('comments').replyMessage, true);
													rootEvalPanel.commentPanel.getComponent('comments').replyMessage = null;
												}
												if (rootEvalPanel.commentPanel.getComponent('comments').editMessage) {
													rootEvalPanel.commentPanel.getComponent('comments').removeDocked(rootEvalPanel.commentPanel.getComponent('comments').editMessage, true);
													rootEvalPanel.commentPanel.getComponent('comments').editMessage = null;
												}												
											}
										}
									]
								}
							]
						}
					]
				}				
			],
			listeners: {
				afterrender: function () {

					if (rootEvalPanel.readOnly) {
						var subCommentPanel = rootEvalPanel.query('[itemId=comments]')[0];

						Ext.Array.forEach(subCommentPanel.query('panel'), function (el) {
							el.setStyle('pointer-events', 'none');
						});
						Ext.Array.forEach(subCommentPanel.query('htmleditor'), function (el) {
							el.setDisabled(true);
							el.setVisible(false);
						});
						Ext.Array.forEach(subCommentPanel.query('button'), function (el) {
							el.setDisabled(true);
							el.setVisible(false);
						});
					}
				}
			}
		});
		rootEvalPanel.commentPanel.loadComments = function(evaluationId, entity, entityId){
			
			if (evaluationId) {			
				rootEvalPanel.commentPanel.lastLoadOpt = {
					evaluationId: evaluationId,
					entity: entity,
					entityId: entityId
				};
			} else {
				evaluationId = rootEvalPanel.commentPanel.lastLoadOpt.evaluationId;
				entity = rootEvalPanel.commentPanel.lastLoadOpt.entity;
				entityId = rootEvalPanel.commentPanel.lastLoadOpt.entityId;				
			}
			
			rootEvalPanel.commentPanel.getComponent('comments').removeAll(true);
			rootEvalPanel.commentPanel.setLoading(true);
			Ext.Ajax.request({
				url: 'api/v1/resource/evaluations/' + evaluationId + '/comments',
				method: 'GET',
				params: {
					entity: entity,
					entityId: entityId
				},
				callback: function(){
					rootEvalPanel.commentPanel.setLoading(false);
				},
				success: function(response, opts) {
					var data = Ext.decode(response.responseText);
					var comments = [];
					var commentMap = {					
					};
					//build hiearchy
					Ext.Array.each(data, function(comment){
						comment.replies = [];
						if (!comment.replyCommentId) {							
							commentMap[comment.commentId] = comment;							
							comments.push(comment);
						}
					});
					
					var mapReplies = function(replies) {
						var missingBucket = [];
						Ext.Array.each(replies, function(comment){
							if (comment.replyCommentId) {
								var existing = commentMap[comment.replyCommentId];
								if (existing) {
									existing.replies.push(comment);
									commentMap[comment.commentId] = comment;
								} else {
									missingBucket.push(comment);
								}
							}
						});		
						return missingBucket;
					};
					var missingBucket = data;
					do {
						missingBucket = mapReplies(missingBucket);
					} while (missingBucket.length > 0);
										
					
					comments.sort(function(a,b){
						return a.createDts > b.createDts;
					});
					
					var commentPanels = [];
					var createComments = function(comment, parent, messageMenu) {
						var closeable=true;
						messageMenu.queryById('edit').setHidden(true);
						
						//var closeable = false;						
						// if (rootEvalPanel.user.admin || rootEvalPanel.user.username === comment.createUser) {
						// 	closeable = true;
						// 	messageMenu.queryById('edit').setHidden(false);
						// }
						var iconCls = '';
						var headerStyle = 'background: olive;';
						if (comment.replyCommentId) {
							iconCls = 'fa fa-reply';
							headerStyle = 'background: darkolivegreen;';
						}
																		
						var panel = Ext.create('Ext.panel.Panel', {	
							iconCls: iconCls,
							header: {
								style: headerStyle
							},
							title: 	comment.createUser + ' - ' + 
									Ext.Date.format(Ext.Date.parse(comment.createDts, 'c'), 'm-d-Y H:i:s'),
							listeners: {
								beforeclose: function(panel, opts) {
									if (panel.finishClose) {
										return true;
									} else {
										Ext.Msg.show({
											title:'Delete Comment',
											message: 'Are you sure you want DELETE this comment?',
											buttons: Ext.Msg.YESNO,
											icon: Ext.Msg.QUESTION,
											fn: function(btn) {
												if (btn === 'yes') {
													panel.setLoading('Deleting...');
													Ext.Ajax.request({
														url: 'api/v1/resource/evaluations/' + evaluationId + '/comments/' + comment.commentId,
														method: 'DELETE',
														callback: function() {
															panel.setLoading(false);
														},
														success: function() {
															panel.finishClose = true;
															panel.close();
														}
													});
												} 
											}
										});
									}
									return false;
								}
							},							
							tools: [								
								{
									type: 'gear',
									tooltip: 'Actions',																	
									callback: function(panel, tool, event) {
										
										messageMenu.showAt(event.getXY());
										
										messageMenu.handlerEdit = function() {
											var form = rootEvalPanel.commentPanel.getComponent('comments').getComponent('form');

											var record = Ext.create('Ext.data.Model', {												
											});
											record.set(comment);
											form.loadRecord(record);

											if (rootEvalPanel.commentPanel.getComponent('comments').editMessage) {
												rootEvalPanel.commentPanel.getComponent('comments').removeDocked(rootEvalPanel.commentPanel.getComponent('comments').editMessage, true);
												rootEvalPanel.commentPanel.getComponent('comments').editMessage = null;
											}

											var editMessage = Ext.create('Ext.panel.Panel', {
												dock: 'bottom',
												html: 'Editing ' + panel.getTitle(),
												bodyStyle: 'background: #00d400; color: white; padding-left: 3px;'
											});
											rootEvalPanel.commentPanel.getComponent('comments').addDocked(editMessage);
											rootEvalPanel.commentPanel.getComponent('comments').editMessage = editMessage;										
										};
										
										messageMenu.handlerReply = function() {
											

											if (rootEvalPanel.commentPanel.getComponent('comments').replyMessage) {
												rootEvalPanel.commentPanel.getComponent('comments').removeDocked(rootEvalPanel.commentPanel.getComponent('comments').replyMessage, true);
												rootEvalPanel.commentPanel.getComponent('comments').replyMessage = null;
											}

											var replyMessage = Ext.create('Ext.panel.Panel', {
												dock: 'bottom',
												html: 'Replying to ' + panel.getTitle(),
												bodyStyle: 'background: #00d400; color: white; padding-left: 3px;'
											});
											rootEvalPanel.commentPanel.getComponent('comments').addDocked(replyMessage);
											rootEvalPanel.commentPanel.getComponent('comments').replyMessage = replyMessage;
											var form = rootEvalPanel.commentPanel.getComponent('comments').getComponent('form');

											var record = Ext.create('Ext.data.Model', {												
											});
											record.set('replyCommentId', comment.commentId);
											form.loadRecord(record);											
										};										
										
										messageMenu.handlerAcknowledge = function() {
											panel.setLoading('Updating record...');								
											Ext.Ajax.request({
												url: 'api/v1/resource/evaluations/' + evaluationId + '/comments/' + panel.data.commentId + '/acknowlege',
												method: 'PUT',
												callback: function() {
													panel.setLoading(false);
												},
												success: function(response, opts) {
													rootEvalPanel.commentPanel.loadComments();
												}
											});											
										};
										
									}
								}
							],
							collapsible: true,
							titleCollapse: true,
							requiredPermissions: ['ADMIN-EVALUATION-DELETE-COMMENT'],
							permissionCheckSuccess: function(){
								closeable=true;
								messageMenu.queryById('edit').setHidden(false);
                                
							},
							permissionCheckFailure: function(){
								if(rootEvalPanel.user.username === comment.createUser)
								{
									this.closeable=true;
									this.setClosable(true);
									messageMenu.queryById('edit').setHidden(false);		
								}
								else
								{
									this.closeable=false;
									this.setClosable(false);
								    messageMenu.queryById('edit').setHidden(true);
								}
								this.setHidden(false);
							},
							closable: closeable,
							closeToolText: 'Delete',
							margin: '0 0 0 0', 
							bodyStyle: 'padding: 5px;',
							data: comment,
							tpl: new Ext.XTemplate(	
								'<tpl if="acknowledge"><span class="fa fa-lg fa-check text-success" title="acknowledged"></span></tpl>{comment}'
							)
						});	
						
						if (parent) {
							parent.add(panel);
						} else {
							commentPanels.push(panel);
						}
						return panel;
					};
					var messageMenu = Ext.create('Ext.menu.Menu', {											
						margin: '0 0 10 0',
						items: [
							{
								text: 'Reply',
								handler: function() {
									messageMenu.handlerReply();
								}
							},
							{
								xtype: 'menuseparator'								
							},
							{
								text: 'Edit',
								itemId: 'edit',
								hidden: true,
								handler: function() {
									messageMenu.handlerEdit();
								}
							},
							{
								text: 'Toggle Acknowledge',
								handler: function() {
									messageMenu.handlerAcknowledge();
								}													
							}
						]
					});
					
					var processCommentPanel = function (comments, parent) {
						Ext.Array.each(comments, function(comment) {
							createComments(comment, parent, messageMenu);
							processCommentPanel(comment.replies);
						});
					};
					processCommentPanel(comments);						
					
					rootEvalPanel.commentPanel.getComponent('comments').add(commentPanels);
				}
			});		
		};
		
		CoreService.brandingservice.getCurrentBranding().then(function(branding){			
			rootEvalPanel.branding = branding;
		});
	},
	loadContentForm: function(page) {
		var rootEvalPanel = this;
		this.checkFormSaveStatus(null, function () {

			rootEvalPanel.pageStatus = page;
			
			if (rootEvalPanel.currentContentForm && rootEvalPanel.currentContentForm.unsavedChanges) {
				rootEvalPanel.currentContentForm.saveData();
			}
			
			rootEvalPanel.contentPanel.removeAll(true);
			rootEvalPanel.contentPanel.getComponent('tools').getComponent('title').update({
				title: page.title
			});
			
			var hideSecurityMarking = true;
			if (rootEvalPanel.branding) {
				hideSecurityMarking = !rootEvalPanel.branding.allowSecurityMarkingsFlg;
			}
			
			var contentForm = Ext.create('OSF.form.' + page.form, Ext.apply({	
				hideSecurityMarking: hideSecurityMarking,
				hideToggleStatus: true
			}, page.options)
			);
			
			rootEvalPanel.contentPanel.add(contentForm);
			rootEvalPanel.currentContentForm = contentForm;

			if (contentForm.loadData) {
				if (page.refreshCallback) {
					rootEvalPanel.refreshCallback = page.refreshCallback;
				}
				
				contentForm.loadData(rootEvalPanel.evaluationId, rootEvalPanel.componentId, page.data, {
					commentPanel: rootEvalPanel.commentPanel,
					user: rootEvalPanel.user,
					mainForm: rootEvalPanel
				}, function () {

					// if readOnly, disable/hide the appropriate fields for the content form
					if (rootEvalPanel.readOnly) {

						Ext.Array.forEach(rootEvalPanel.contentPanel.query('button'), function (field, index) {
							field.setVisible(false);
							field.setDisabled(true);
						});
						Ext.Array.forEach(rootEvalPanel.contentPanel.query('grid'), function (grid, index) {
							grid.setStyle('opacity', '0.6');
							grid.events = {};
						});
						Ext.Array.forEach(rootEvalPanel.contentPanel.query('field'), function (field) {
							if (field.xtype !== 'tinymce_textarea') {
								field.setReadOnly(true);
							}
							field.setStyle('opacity', '0.6');
						});

						// Unfortunately there are some underlying issues with tinymce. In short, there is a
						//	very brief timing issue. Thus push this back on the stack a bit...
						//	NOTE: when attempting to "setReadOnly" before this delay will force the tinymce
						//		in such a state, where the method "setReadOnly()" does not operate as expected.
						Ext.Function.createDelayed(function () {
							Ext.Array.forEach(rootEvalPanel.contentPanel.query('tinymce_textarea'), function (field) {
								field.setReadOnly(true);
							});
						}, 100)();
					}
				});
			}
		});
	},
	checkFormSaveStatus: function (evalWin, cb) {
		var rootEvalPanel = this;
		
		if (rootEvalPanel.down('form') !== null && rootEvalPanel.down('form').unsavedChanges) {
			// ask if they would like to save before closing...
			Ext.Msg.show({
				title: 'Discard Changes?',
				message: 'You have unsaved changes.<br />Would you like to continue and <b>discard</b> all changes?',
				buttons: Ext.Msg.YESNO,
				icon: Ext.Msg.WARNING,
				fn: function (btn) {
					if (btn === 'yes') {
						if (evalWin) {
							evalWin.doClose();
						}
						else if (cb) {
							cb();
						}
					} else if (btn === 'no') {
						// don't do anything...
					}
				}
			});
			return false;
		}
		else if (cb) {
			cb();
		}
		return true;
	}
});

Ext.define('OSF.component.EvaluationEntryPanel', {
	extend: 'OSF.component.RootEvaluationPanel',
	alias: 'osf.widget.EvaluationEntryPanel',
	requires: [
		'OSF.form.EntrySummary',
		'OSF.form.Attributes',
		'OSF.form.Relationships',
		'OSF.form.Contacts',
		'OSF.form.Resources',
		'OSF.form.Media',
		'OSF.form.Dependencies',
		'OSF.form.Tags'
	],
	layout: 'border',
	initComponent: function () {
		this.callParent();

		var entryPanel = this;

		entryPanel.navigation = Ext.create('Ext.panel.Panel', {
			itemId: 'entrymenu',
			title: 'Entry Navigation',	
			titleCollapse: true,
			collapsible: true,
			layout: 'anchor',
			animCollapse: false,
			split: true,
			width: 250,
			minWidth: 250,
			scrollable: true,
			iconCls: 'fa fa-navicon',
			region: 'west',
			bodyStyle: 'padding: 10px; background: white;',
			defaultType: 'button',
			defaults: {
				width: '100%',
				cls: 'evaluation-nav-button',							
				overCls: 'evaluation-nav-button-over',
				focusCls: 'evaluation-nav-button',
				margin: '5 0 0 0'
			},
			items: [
				{							
					text: 'Summary',							
					handler: function(){
						entryPanel.loadContentForm({
							form: 'EntrySummary',
							title: 'Entry Summary',
							refreshCallback: entryPanel.externalRefreshCallback
						});								
					}							
				}
			]
		});

		entryPanel.add(entryPanel.navigation);
		entryPanel.add(entryPanel.contentPanel);
		entryPanel.add(entryPanel.commentPanel);

		CoreService.userservice.getCurrentUser().then(function(user){
			entryPanel.user = user;	
			
			entryPanel.loadContentForm({
				form: 'EntrySummary',
				title: 'Entry Summary',
				refreshCallback: entryPanel.externalRefreshCallback
			});				
		});
	},
	loadEval: function(evaluationId, componentId){
		var entryPanel = this;

		entryPanel.setLoading('Loading Entry...');
		entryPanel.evaluationId = evaluationId;
		entryPanel.componentId = componentId;
		
		Ext.Ajax.request({
			url: 'api/v1/resource/components/' + entryPanel.componentId,
			callback: function() {				
				entryPanel.setLoading(false);
			},
			success: function(response, opts) {
				var componentFull = Ext.decode(response.responseText);
				
				entryPanel.setLoading('Loading Entry Type...');				
				Ext.Ajax.request({
				url: 'api/v1/resource/componenttypes/'+ componentFull.componentType,
				callback: function() {	
					entryPanel.setLoading(false);
				},
				success: function(response, opts) {
						var entryType = Ext.decode(response.responseText);
						var menuItems = [];
						menuItems.push(
							{							
								text: 'Summary',							
								handler: function(){
									entryPanel.loadContentForm({
										form: 'EntrySummary',
										title: 'Entry Summary',
										refreshCallback: entryPanel.externalRefreshCallback
									});								
								}							
							}					
						);
						if (entryType.dataEntryAttributes){
							menuItems.push({						
								text: 'Attributes',							
								handler: function(){
									entryPanel.loadContentForm({
										form: 'Attributes',
										title: 'Entry Attributes'
									});
								}
							});
						}
						if (entryType.dataEntryRelationships){
							menuItems.push({						
								text: 'Relationships',							
								handler: function(){
									entryPanel.loadContentForm({
										form: 'Relationships',
										title: 'Entry Relationships'
									});
								}
							});					
						}
						if (entryType.dataEntryContacts){
							menuItems.push({						
								text: 'Contacts',							
								handler: function(){
									entryPanel.loadContentForm({
										form: 'Contacts',
										title: 'Entry Contacts'
									});
								}
							});					
						}
						if (entryType.dataEntryResources){
							menuItems.push({						
								text: 'Resources',							
								handler: function(){
									entryPanel.loadContentForm({
										form: 'Resources',
										title: 'Entry Resources'
									});	
								}
							});					
						}
						if (entryType.dataEntryMedia){
							menuItems.push({						
								text: 'Media',							
								handler: function(){
									entryPanel.loadContentForm({
										form: 'Media',
										title: 'Entry Media'
									});
								}
							});						
						}
						if (entryType.dataEntryDependencies){
							menuItems.push({						
								text: 'Dependencies',							
								handler: function(){
									entryPanel.loadContentForm({
										form: 'Dependencies',
										title: 'Entry Dependencies'
									});
								}
							});					
						}
						menuItems.push({						
							text: 'Tags',							
							handler: function(){
								entryPanel.loadContentForm({
									form: 'Tags',
									title: 'Tags'
								});
							}
						});

						entryPanel.navigation.removeAll();
						entryPanel.navigation.add(menuItems);
					
					}
				});
			}
		});
	}
});

Ext.define('OSF.component.EvaluationEvalPanel', {
	extend: 'OSF.component.RootEvaluationPanel',
	alias: 'osf.widget.EvaluationEvalPanel',
	requires: [
		'OSF.form.EvaluationInfo',
		'OSF.form.ChecklistSummary',
		'OSF.form.ChecklistQuestion',
		'OSF.form.ChecklistAll',
		'OSF.form.Section',
		'OSF.form.Review',
		'OSF.form.ManageEvalQuestions'
	],
	layout: 'border',

	initComponent: function () {
		this.callParent();
		
		var evalPanel = this;

		evalPanel.navigation = Ext.create('Ext.panel.Panel', {
			title: 'Evaluation Navigation',
			iconCls: 'fa fa-navicon',
			region: 'west',
			itemId: 'evalmenu',
			collapsible: true,
			animCollapse: false,
			titleCollapse: true,
			width: 250,
			minWidth: 250,
			split: true,
			scrollable: true,			
			layout: 'anchor',
			bodyStyle: 'background: white;',
			defaults: {
				width: '100%'
			},
			items: [
				{
					xtype: 'panel',
					title: 'Evaluation',
					titleCollapse: true,
					collapsible: true,
					margin: '10 0 0',
					bodyStyle: 'padding: 10px;',
					defaultType: 'button',
					defaults: {
						width: '100%',
						cls: 'evaluation-nav-button',							
						overCls: 'evaluation-nav-button-over',
						focusCls: 'evaluation-nav-button',
						margin: '5 0 0 0'
					},
					items: [
						{							
							text: 'Info',							
							handler: function(){
								evalPanel.loadContentForm({
									form: 'EvaluationInfo',
									title: 'Evaluation Info',
									refreshCallback: evalPanel.externalRefreshCallback
								});
								evalPanel.commentPanel.setHidden(false);
							}
						},
						{						
							text: 'Review',														
							handler: function(){
								evalPanel.loadContentForm({
									form: 'Review',
									title: 'Review'
								});
								evalPanel.commentPanel.setHidden(true);
							}
						}						
					]
				},
				{
					xtype: 'panel',
					itemId: 'sectionmenu',
					title: 'Sections',
					collapsible: true,
					bodyStyle: 'padding: 10px;',
					margin: '0 0 0 0',
					defaultType: 'button',
					defaults: {
						width: '100%',
						cls: 'evaluation-nav-button',							
						overCls: 'evaluation-nav-button-over',
						focusCls: 'evaluation-nav-button',
						margin: '5 0 0 0'
					},					
					items: [
					]
				},	
				{
					xtype: 'panel',
					itemId: 'checklistmenu',
					title: 'Checklist',
					titleCollapse: true,
					collapsible: true,
					bodyStyle: 'padding: 10px;',
					margin: '0 0 0 0',
					defaultType: 'button',
					defaults: {
						width: '100%',
						cls: 'evaluation-nav-button',							
						overCls: 'evaluation-nav-button-over',
						focusCls: 'evaluation-nav-button',
						margin: '5 0 0 0'
					},					
					items: [																
					]
				}
			]
		});

		evalPanel.add(evalPanel.navigation);
		evalPanel.add(evalPanel.contentPanel);
		evalPanel.add(evalPanel.commentPanel);

		CoreService.userservice.getCurrentUser().then(function(user){
			evalPanel.user = user;	
			
			evalPanel.loadContentForm({
				form: 'EvaluationInfo',
				title: 'Evaluation Info',
				refreshCallback: evalPanel.externalRefreshCallback
			});	
			evalPanel.commentPanel.setHidden(false);
		});
	},
	loadEval: function (evaluationId, componentId) {

		var evalPanel = this;

		evalPanel.setLoading(true);
		evalPanel.evaluationId = evaluationId;
		evalPanel.componentId = componentId;

		Ext.Ajax.request({
			url: 'api/v1/resource/evaluations/' + evaluationId +'/details',
			callback: function() {
				evalPanel.setLoading(false);	
			},
			success: function(response, opt) {
				
				evalPanel.evaluationAll = Ext.decode(response.responseText);

				var questions = [];
				
				questions.push({							
					text: 'Summary',							
					handler: function(){
						evalPanel.loadContentForm({
							form: 'ChecklistSummary',
							title: 'Checklist Summary',
							data: evalPanel.evaluationAll.checkListAll
						});
						evalPanel.commentPanel.setHidden(false);
					}							
				});
				
				var allQuestionButtonType = 'button';
				var allQuestionMenu = null;
				if (evalPanel.evaluationAll.evaluation.allowQuestionManagement) {
					allQuestionButtonType = 'splitbutton';
					allQuestionMenu = {
						items: [
							{
								text: 'Manage Questions',
								iconCls: 'fa fa-lg fa-edit icon-small-vertical-correction',
								handler: function() {
									
									var manageWin = Ext.create('OSF.form.ManageEvalQuestions', {
										evaluationAll: evalPanel.evaluationAll,
										successCallback: function() {
											evalPanel.loadEval(evalPanel.evaluationId, evalPanel.componentId);
											allQuestionLoadAction();
										}
									});											
									manageWin.show();
								}
							}
						],
						listeners: {
							beforerender: function () {
							 this.setWidth(this.up('button').getWidth());
							}					
						}								
					};
				}
				
				var allQuestionLoadAction = function() {
					evalPanel.loadContentForm({
						form: 'ChecklistAll',
						title: 'Checklist Questions',
						data: evalPanel.evaluationAll.checkListAll,
						refreshCallback: function(updatedResponse) {
							var newStatusIcon = questionStatusIcon(updatedResponse.workflowStatus);

							var checklistMenu = evalPanel.navigation.getComponent('checklistmenu');
							Ext.Array.each(checklistMenu.items.items, function(item){
								if (item.questionId && updatedResponse.questionId === item.questionId) {
									var itemStatus = item.getComponent('status');
									itemStatus.setText(newStatusIcon);	
									itemStatus.setTooltip(updatedResponse.workflowStatusDescription);
								}
							});
						}									
					});
					evalPanel.commentPanel.setHidden(false);
				};
				
				questions.push({		
					xtype: allQuestionButtonType,
					text: 'All Questions',
					menu: allQuestionMenu,
					handler: function(){
						allQuestionLoadAction();
					}							
				});
				
				var questionStatusIcon = function(workflowStatus) {
					var statusIcon = '';
					if (workflowStatus === 'COMPLETE') {
						statusIcon = '<span class="fa fa-2x fa-check text-success"></span>';
					} else if (workflowStatus === 'INPROGRESS') {
						statusIcon = '<span class="fa fa-2x fa-refresh text-info"></span> ';
					} else if (workflowStatus === 'HOLD') {
						statusIcon = '<span class="fa fa-2x fa-close text-danger"></span> ';
					} else if (workflowStatus === 'WAIT') {
						statusIcon = ' - <span class="fa fa-2x fa-minus text-warning"></span> ';
					}
					return statusIcon;
				};
				
				
				Ext.Array.each(evalPanel.evaluationAll.checkListAll.responses, function(chkresponse) {
												
					var statusIcon = questionStatusIcon(chkresponse.workflowStatus);
					
					var questionHandler = function(btn) {
						evalPanel.loadContentForm({
							form: 'ChecklistQuestion',
							title: 'Checklist Question',
							data: chkresponse,
							refreshCallback: function(updatedResponse) {
								var newStatusIcon = questionStatusIcon(updatedResponse.workflowStatus);
																		
								var checklistMenu = evalPanel.navigation.getComponent('checklistmenu');
								Ext.Array.each(checklistMenu.items.items, function(item){
									if (item.questionId && updatedResponse.questionId === item.questionId) {
										var itemStatus = item.getComponent('status');
										itemStatus.setText(newStatusIcon);
										itemStatus.setTooltip(updatedResponse.workflowStatusDescription);
									}
								});
							}
						});
						evalPanel.commentPanel.setHidden(false);
					};
					
					questions.push({
						xtype: 'segmentedbutton',
						allowMultiple: false,
						allowToggle: false,
						allowDepress: false,
						qid: chkresponse.question.qid,
						questionId: chkresponse.question.questionId,
						items: [
							{
								text: chkresponse.question.qid,
								width: 50,
								tooltip: chkresponse.question.question,
								handler: questionHandler
							},
							{
								text: chkresponse.question.evaluationSectionDescription,
								tooltip: chkresponse.question.evaluationSectionDescription,
								handler: questionHandler
							},
							{
								text: statusIcon,
								itemId: 'status',
								tooltip: chkresponse.workflowStatusDescription,
								cls: 'evaluation-nav-question-status',
								width: 50,
								handler: questionHandler
							}
						]							
					});
				});
				evalPanel.navigation.getComponent('checklistmenu').removeAll();
				evalPanel.navigation.getComponent('checklistmenu').add(questions);
				
				var sections = [];
				Ext.Array.each(evalPanel.evaluationAll.contentSections, function(sectionAll) {
					
					var menu = null;
					var buttonType = 'button';
					if (evalPanel.evaluationAll.evaluation.allowNewSections && !evalPanel.readOnly) {
						
						buttonType = 'splitbutton';
						menu = {
							items: [
								{
									text: 'Delete Section',
									iconCls: 'fa fa-lg fa-trash-o icon-button-color-warning icon-small-vertical-correction-book',
									handler: function(){
										Ext.Msg.show({
											title:'Delete: ' + sectionAll.section.title + '?',													
											message: 'Are you sure you want to remove this section?',
											buttons: Ext.Msg.YESNO,
											icon: Ext.Msg.QUESTION,
											fn: function(btn) {
												if (btn === 'yes') {
													
													evalPanel.setLoading('Deleting Section: ' + sectionAll.section.title);
													Ext.Ajax.request({
														url: 'api/v1/resource/evaluations/' + evalPanel.evaluationId + '/sections/' + sectionAll.section.contentSectionId,
														method: 'DELETE',
														callback: function() {
															evalPanel.setLoading(false);
														},
														success: function(response, opts) {
															evalPanel.loadContentForm({
																form: 'EvaluationInfo',
																title: 'Evaluation Info'
															});
															evalPanel.commentPanel.setHidden(false);
															evalPanel.loadEval(evalPanel.evaluationId, evalPanel.componentId);																		
														}
													});
												}
											}
										});												
									}
								}
							]
						};
					}
					
					sections.push({	
						xtype: buttonType,
						text: sectionAll.section.title,
						menu: menu,
						handler: function(){
							evalPanel.loadContentForm({
								form: 'Section',
								title: sectionAll.section.title,
								data: sectionAll										
							});
							evalPanel.commentPanel.setHidden(false);
						}							
					});							
				});
				
				evalPanel.navigation.getComponent('sectionmenu').removeAll();
				evalPanel.navigation.getComponent('sectionmenu').add(sections);
				
				if (evalPanel.evaluationAll.evaluation.allowNewSections && !evalPanel.readOnly) {

					var dockedTools = evalPanel.navigation.getComponent('sectionmenu').getDockedComponent('tools');
					if (!dockedTools) {							
						evalPanel.navigation.getComponent('sectionmenu').addDocked({
							xtype: 'toolbar',
							itemId: 'tools',
							dock: 'top',
							items: [
								{
									iconCls: 'fa fa-lg fa-plus icon-button-color-save',
									text: 'Add Section',
									itemId: 'addSectionButton',
									handler: function() {

										var sectionWindow = Ext.create('Ext.window.Window', {
											title: 'Add Section',
											modal: true,
											closeAction: 'destroy',
											width: 400,
											height: 175,
											layout: 'fit',
											items: [
												{
													xtype: 'form',
													bodyStyle: 'padding: 10px;',
													items: [
														{
															xtype: 'combobox',
															name: 'templateId',
															fieldLabel: 'Section Template',
															displayField: 'name',
															valueField: 'templateId',								
															emptyText: 'Select',
															labelAlign: 'top',
															width: '100%',
															editable: false,
															forceSelection: true,
															allowBlank: false,
															store: {									
																autoLoad: true,
																proxy: {
																	type: 'ajax',
																	url: 'api/v1/resource/contentsectiontemplates'
																},
																listeners: {
																	load: function(store, records, opts) {
																		store.filterBy(function(record){
																			var keep = true;
																			Ext.Array.each(evalPanel.evaluationAll.contentSections, function(sectionAll) {
																				if (record.get('templateId') === sectionAll.section.templateId) {
																					keep = false;
																				}
																			});
																			return keep;
																		});
																	}
																}
															}
														}
													],
													dockedItems: [
														{
															xtype: 'toolbar',
															dock: 'bottom',
															items: [
																{
																	text: 'Add',
																	iconCls: 'fa fa-lg fa-plus icon-button-color-save',
																	formBind: true,
																	handler: function() {
																		var win = this.up('window');
																		var form = this.up('form');
																		var sectionData = form.getValues();

																		evalPanel.setLoading('Adding Section...');
																		Ext.Ajax.request({
																			url: 'api/v1/resource/evaluations/' + evalPanel.evaluationId + '/sections/' + sectionData.templateId,
																			method: 'POST',
																			callback: function(response, opts) {
																				evalPanel.setLoading(false);
																			},
																			success: function(response, opts) {
																				evalPanel.loadEval(evalPanel.evaluationId, evalPanel.componentId);
																				win.close();
																			}																		
																		});

																	}
																},
																{
																	xtype: 'tbfill'
																},
																{
																	text: 'Cancel',
																	iconCls: 'fa fa-lg fa-close icon-button-color-warning',
																	handler: function() {
																		this.up('window').close();
																	}
																}																	
															]
														}
													]
												}
											]
										});
										sectionWindow.show();
									}
								}
							]
						});
					}
				}
				
			}
		});				
	}
});

Ext.define('OSF.component.EvaluationFormWindow', {
	extend: 'Ext.window.Window',
	alias: 'osf.widget.EvaluationFormWindow',
	title: 'Evaluation Form',
	iconCls: 'fa fa-clipboard',
	width: '85%',
	height: '85%',
	modal: true,
	maximizable: true,
	layout: 'fit',
	isPublishedEvaluation: false,
	listeners: {
		show: function() {        

			this.removeCls("x-unselectable");    
		},
		beforeClose: function () {

			var entryPanel = this.query('[itemId=entryPanel]')[0];
			var evalPanel = this.query('[itemId=evalPanel]')[0];

			return evalPanel ? evalPanel.checkFormSaveStatus(this) : entryPanel.checkFormSaveStatus(this);
		}
	},	
	initComponent: function () {

		this.callParent();
		var evalWin = this;

		var initialTabPanels = [
			Ext.create('OSF.component.EvaluationEntryPanel', {
				itemId: 'entryPanel',
				title: 'Entry View',
				readOnly: evalWin.isPublishedEvaluation,
				tabConfig: {
					margin: '0 3 0 3'
				}
			})
		];
		if (!evalWin.isPublishedEvaluation) {

			initialTabPanels.push(Ext.create('OSF.component.EvaluationEvalPanel', {
				itemId: 'evalPanel',
				title: 'Current Evaluation View',
				tabConfig: {
					margin: '0 30 0 3'
				}
			}));

		}

		evalWin.evalTabPanel = Ext.create('Ext.TabPanel', {
		    fullscreen: true,
		    items: initialTabPanels
		});
		
		evalWin.add(evalWin.evalTabPanel);

	},
	loadEval: function(record, refreshCallback) {

		var evalWin = this;

		// setup entry panel
		Ext.Array.forEach(evalWin.query('[itemId=entryPanel],[itemId=evalPanel]'), function (el) {
			var panel = evalWin.query('[itemId=' + el.itemId + ']')[0];
			panel.loadEval(record.get('evaluationId'), record.get('componentId'));

			if (refreshCallback) {
				panel.externalRefreshCallback = refreshCallback;
			}
		});

		// dynamically create published evaluation tabs
		Ext.Ajax.request({
			url: 'api/v1/resource/components/' + record.get('originComponentId') + '/detail/',
			success: function (response) {

				response = Ext.decode(response.responseText);

				Ext.Array.forEach(response.fullEvaluations, function (el, index) {
					if (el.evaluation.published) {

						var newEvalPanel = Ext.create('OSF.component.EvaluationEvalPanel', {
							title: 'Evaluation - ' + el.evaluation.version,
							readOnly: true
						});
						newEvalPanel.loadEval(el.evaluation.evaluationId, el.evaluation.componentId);

						evalWin.evalTabPanel.add(newEvalPanel);
					}
				});
			}
		});
	}
	 
});

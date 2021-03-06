/* 
 * Copyright 2018 Space Dynamics Laboratory - Utah State University Research Foundation.
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

/* global Ext */

Ext.define('OSF.customSubmission.field.ResourceSimple', {
	extend: 'Ext.panel.Panel',
	xtype: 'osf-submissionform-resourcesimple',
	
	layout: 'hbox',
	width: '100%',
	maxWidth: 800,
	
	fieldType: 'RESOURCE_SIMPLE',
	uploadedFile: false,
	
	initComponent: function () {
		this.callParent();
		
		var resourcePanel = this;
		
		if (resourcePanel.fieldTemplate.allowPrivateResource) {
			resourcePanel.setMaxWidth(860);
		}

		resourcePanel.labelData = {
			question:  resourcePanel.createQuestionLabel(),
			uploadedFile: ''
		};
		
		resourcePanel.label = Ext.create('Ext.panel.Panel', {		
			flex: 1,
			margin: '0 10 0 0',
			data: resourcePanel.labelData,
			tpl: new Ext.XTemplate(
				'{question}',
				'<div>Uploaded: {uploadedFile}</div>'
			)
		});	

		resourcePanel.add([
			resourcePanel.label,
			{
				xtype: 'panel',
				layout: 'hbox',
				items: [
					{
						xtype: 'button',
						text: 'Upload',
						width: 100,
						handler: function() {
							resourcePanel.uploadWindow();
						}
					},
					{
						xtype: 'checkbox',
						itemId: 'private',
						margin: '0 0 0 5',						
						hidden: resourcePanel.fieldTemplate.allowPrivateResource ? false : true,
						boxLabel: 'Private'						
					}
				]
			}
			
		]);
		
		//on edit hold on to the meta record
		
		var initialData = resourcePanel.section.submissionForm.getFieldData(resourcePanel.fieldTemplate.fieldId);
		if (initialData) {
			var data = Ext.decode(initialData);
			
			if (data.privateFlag) {
				resourcePanel.queryById('private').setValue(data.privateFlag);
			}
			
			resourcePanel.uploadedFile = data.file;		
			if (data.file) {
				resourcePanel.labelData.uploadedFile = data.file.originalName;														
				resourcePanel.label.update(resourcePanel.labelData);
			}
		}		
		
		if (resourcePanel.section) {
			if (!resourcePanel.section.submissionForm.userSubmission) {
				resourcePanel.previewMode = true;			
			} else {
				resourcePanel.userSubmissionId = resourcePanel.section.submissionForm.userSubmission.userSubmissionId;
			}
		} else {
			resourcePanel.previewMode = true;
		}		
	
	},
	
	uploadWindow: function() {
		var resourcePanel = this;
		
		//prompt for upload
		
		var uploadWindow = Ext.create('Ext.window.Window', {
			title: 'Upload File',
			modal: true,
			width: 500,
			height: 200,
			layout: 'fit',
			closeAction: 'destroy',
			items: [
				{
					xtype: 'form',
					scrollable: true,
					layout: 'anchor',
					bodyStyle: 'padding: 10px;',
					items: [
						{
							xtype: 'fileFieldMaxLabel',
							itemId: 'upload',
							width: '100%',
							allowBlank: false,
							name: 'file',
							labelAlign: 'top'
						}						
					],
					dockedItems: [
						{
							xtype: 'toolbar',
							dock: 'bottom',
							items: [
								{
									text: 'Upload',
									formBind: true,
									iconCls: 'fa fa-lg fa-edit icon-button-color-edit',
									handler: function () {
										
										//on success field.uploadedFile = true
										if (resourcePanel.previewMode) {
											Ext.Msg.show({
												title:'Preview Mode',
												message: 'Unable to upload file in preview mode.',
												buttons: Ext.Msg.OK,
												icon: Ext.Msg.ERROR,
												fn: function(btn) {
												}
											});	
											uploadWindow.close();
										} else {
											//upload
											var form = this.up('form');
											
											var progressMsg = Ext.MessageBox.show({
												title: 'Media Upload',
												msg: 'Uploading media please wait...',
												width: 300,
												height: 150,
												closable: false,
												progressText: 'Uploading...',
												wait: true,
												waitConfig: {interval: 300}
											});

											form.submit({
												url: 'Media.action?UploadSubmissionMedia',
												params: {
													'userSubmissionId': resourcePanel.userSubmissionId,
													'submissionTemplateFieldId': resourcePanel.fieldTemplate.fieldId
												},
												method: 'POST',
												submitEmptyText: false,
												success: function (form, action, opt) {
													progressMsg.hide();
													uploadWindow.close();
												},
												failure: function (form, action, opt) {
													var data = Ext.decode(action.response.responseText);
													if (data.success && data.success === false){
														Ext.Msg.show({
															title: 'Upload Failed',
															msg: 'The file upload was not successful. Check that the file meets the requirements and try again.',
															buttons: Ext.Msg.OK
														});																										
													} else {
														//false positive the return object doesn't have success																
														Ext.toast('Uploaded Successfully', '', 'tr');													
														
														//update media record
														resourcePanel.uploadedFile = {
															mediaFileId: data.file.mediaFileId,
															originalName: data.file.originalName
														};
														
														resourcePanel.labelData.uploadedFile = data.file.originalName;														
														resourcePanel.label.update(resourcePanel.labelData);
														uploadWindow.close();
													}
													progressMsg.hide();
												}
											});
											
											
										}
										
									}
								},
								{
									xtype: 'tbfill'
								},
								{
									text: 'Cancel',
									iconCls: 'fa fa-lg fa-close icon-button-color-warning',
									handler: function () {
										this.up('window').close();												
									}
								}								
							]
						}
					]
				}
			]
		});
		
		uploadWindow.show();
		
	},
	
	reviewDisplayValue: function() {
		var field = this;
		var value = field.uploadedFile;
		return (value && value !== '') ? value : '(No Data Entered)';	
	},
	
	isValid: function() {
		var field = this;
		
		if (field.fieldTemplate.required) {
			return field.uploadedFile;
		} else {
			return true;
		}
	},
	getUserData: function() {
		var field = this;
		
		if (field.uploadedFile){
			
			//get the file upload info (mediaFile)
			
			var resource = {
				resourceType: field.fieldTemplate.resourceType,
				privateFlag: field.queryById('private').getValue(),
				file: field.uploadedFile
			};			
			
			var userSubmissionField = {			
				templateFieldId: field.fieldTemplate.fieldId,
				rawValue: Ext.encode(resource)
			};	
			return userSubmissionField;		
		} else {
			return null;		
		}
		
	}	
	
});



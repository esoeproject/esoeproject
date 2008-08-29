package com.qut.middleware.esoemanager.client.ui.policy.operators;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.qut.gwtuilib.client.RegexConstants;
import com.qut.gwtuilib.client.display.FlexibleTable;
import com.qut.gwtuilib.client.display.IntegratedMultiValueTextBox;
import com.qut.gwtuilib.client.display.IntegratedTextBox;
import com.qut.gwtuilib.client.display.Loader;
import com.qut.gwtuilib.client.eventdriven.eventmgr.BaseEvent;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventController;
import com.qut.gwtuilib.client.eventdriven.eventmgr.EventListener;
import com.qut.gwtuilib.client.eventdriven.events.DisplayWidgetEvent;
import com.qut.gwtuilib.client.eventdriven.events.MessageEvent;
import com.qut.gwtuilib.client.exceptions.InvalidContentException;
import com.qut.gwtuilib.client.input.StyledButton;
import com.qut.middleware.esoemanager.client.CSSConstants;
import com.qut.middleware.esoemanager.client.EsoeManager;
import com.qut.middleware.esoemanager.client.EsoeManagerConstants;
import com.qut.middleware.esoemanager.client.PolicyConstants;
import com.qut.middleware.esoemanager.client.events.EventConstants;
import com.qut.middleware.esoemanager.client.exceptions.PolicyComponentValidationException;
import com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator;
import com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.StringOperator;
import com.qut.middleware.esoemanager.client.rpc.handler.AttributeHandler;

public abstract class StringOperatorBaseUI extends OperatorUI implements EventListener
{
	static List<String> attributeContainer;

	ListBox configuredAttributeNames;
	IntegratedTextBox manualAttributeName;
	IntegratedMultiValueTextBox attributeValues;

	private String operatorTitle;
	private String equalsStatement;

	private Label operatorTitleLbl;
	private Label equalsStatementLbl;

	private VerticalPanel childPanel;

	private boolean manualAttribute;

	public StringOperatorBaseUI(OperatorUI parent, boolean editMode, String operatorTitle, String equalsStatement)
	{
		super(parent, editMode, true);
		this.operatorTitle = operatorTitle;
		this.equalsStatement = equalsStatement;
		this.manualAttribute = false;

		init();

		/* These have already been created in createLocalInterface called by our parent) */
		this.operatorTitleLbl.setText(operatorTitle);
		this.equalsStatementLbl.setText(equalsStatement);

		EventController.registerListener(this);
	}

	private void init()
	{
		this.validChildOperators.add(PolicyConstants.STRINGNORMLOWERCASE);
		this.validChildOperators.add(PolicyConstants.STRINGNORMSPACE);
	}

	@Override
	public void createLocalInterface()
	{
		this.localContent.clear();

		this.manualAttributeName = new IntegratedTextBox(this, 1, RegexConstants.matchAll, "Attribute name is invalid",
				"Attribute", EsoeManagerConstants.areaID, "manualattribute");
		this.manualAttributeName.getContent().setVisible(false);
		this.configuredAttributeNames = new ListBox();
		this.attributeValues = new IntegratedMultiValueTextBox(this, 1, null,
				"Attribute value is invalid", "Attribute value", EsoeManagerConstants.areaID, "attributevalue");

		if (this.newOperator)
		{
			this.attributeValues.addValue();
		}

		if (attributeContainer == null || attributeContainer.size() == 0)
		{
			this.attributeContainer = new ArrayList<String>();
			Loader attribLoader = new Loader();
			EsoeManager.contentService.retrieveConfiguredAttributeList(new AttributeHandler(attribLoader,
					this.attributeContainer, this.configuredAttributeNames, EsoeManagerConstants.areaID));
		}
		else
		{
			for (String attribute : attributeContainer)
				this.configuredAttributeNames.addItem(attribute);
			this.configuredAttributeNames.setVisible(true);
		}

		StyledButton manualOverride = new StyledButton("toggle", "");
		manualOverride.addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				StringOperatorBaseUI.this.manualAttribute = !StringOperatorBaseUI.this.manualAttribute;
				StringOperatorBaseUI.this.configuredAttributeNames
						.setVisible(!StringOperatorBaseUI.this.configuredAttributeNames.isVisible());
				StringOperatorBaseUI.this.manualAttributeName.getContent().setVisible(
						!StringOperatorBaseUI.this.manualAttributeName.getContent().isVisible());
			}
		});

		this.operatorTitleLbl = new Label();
		this.equalsStatementLbl = new Label();
		this.equalsStatementLbl.addStyleName(CSSConstants.serviceValueTitle);

		HorizontalPanel attributeSelectionPanel = new HorizontalPanel();
		attributeSelectionPanel.add(this.configuredAttributeNames);
		attributeSelectionPanel.add(this.manualAttributeName.getContent());
		attributeSelectionPanel.add(manualOverride);

		FlexibleTable editableContent = new FlexibleTable(2, 2);
		editableContent.insertWidget(this.manualAttributeName.getTitle());
		editableContent.insertWidget(attributeSelectionPanel);
		editableContent.nextRow();
		editableContent.insertWidget(this.equalsStatementLbl);
		editableContent.nextRow();
		editableContent.insertWidget(this.attributeValues.getContent());

		this.localContent.add(editableContent);

		this.childPanel = new VerticalPanel();
		renderChildren();
		this.localContent.add(this.childPanel);
	}

	private void renderChildren()
	{
		if (children != null && children.size() > 0)
		{
			Label stringOperators = new Label("and the following operations are applied to the comparison");
			this.childPanel.add(stringOperators);
			for (int i = 0; i < children.size(); i++)
			{
				OperatorUI child = children.get(i);
				this.childPanel.add(child);
			}
		}
	}

	private void refreshLocalInterface()
	{
		if (this.attributeValues.attributeCount() > 1)
			this.equalsStatementLbl.setText(this.equalsStatement + " one of");
		else
			this.equalsStatementLbl.setText(this.equalsStatement);

		this.childPanel.clear();
		renderChildren();
	}

	public boolean isManualAttribute()
	{
		return this.manualAttribute;
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.client.ui.data.policy.Operator#validateOperator()
	 */
	@Override
	public void validate(List<String> idList) throws PolicyComponentValidationException
	{
		if (this.manualAttributeName.getContent().isVisible() && !this.manualAttributeName.isValid())
		{
			this.showParentContent();
			this.localMessages.errorMsg("String operators must have a valid attribute name");
			throw new PolicyComponentValidationException("String operators must have a valid attribute name");
		}

		try
		{
			List<String> values = this.attributeValues.getValues();

			if (values == null || values.size() == 0)
			{
				this.showParentContent();
				this.localMessages.errorMsg("String operators must have at least one attribute value");
				throw new PolicyComponentValidationException("String operators must have at least one attribute value");
			}
		}
		catch (InvalidContentException e)
		{
			this.showParentContent();
			throw new PolicyComponentValidationException("A string operator attribute value is invalid");
		}

		super.validate(idList);
	}

	@Override
	public void addChild(OperatorUI child, String childType)
	{
		if (this.validChildOperators.contains(childType))
		{
			this.children.add(child);
			this.refreshLocalInterface();
			return;
		}

		EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
				MessageEvent.error, "Unable to add " + childType + " to a string operator"));
	}

	@Override
	public void deleteChild(OperatorUI child)
	{
		if (this.children.contains(child))
		{
			this.children.remove(child);
			this.refreshLocalInterface();
			return;
		}

		EventController.executeEvent(new MessageEvent(EventConstants.userMessage, EsoeManagerConstants.areaID,
				MessageEvent.error, "No such child found"));
	}

	/* (non-Javadoc)
	 * @see com.qut.middleware.esoemanager.client.ui.policy.operators.Operator#extractContent()
	 */
	@Override
	public com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.Operator extractContent()
			throws PolicyComponentValidationException
	{
		try
		{

			com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.StringOperator operator = (com.qut.middleware.esoemanager.client.rpc.bean.policy.operator.StringOperator) super
					.extractContent();

			if (this.isManualAttribute())
				operator.setAttributeName(this.manualAttributeName.getText());
			else
				operator.setAttributeName(this.configuredAttributeNames.getItemText(this.configuredAttributeNames
						.getSelectedIndex()));

			operator.setAttributeValues(this.attributeValues.getValues());

			return operator;
		}
		catch (InvalidContentException e)
		{
			throw new PolicyComponentValidationException(e);
		}
	}

	public void populateContent(Operator operator) throws PolicyComponentValidationException
	{
		try
		{
			super.populateContent(operator);

			StringOperator stringOperator = (StringOperator) operator;
			this.manualAttribute = true;
			this.manualAttributeName.setText(stringOperator.getAttributeName());
			this.manualAttributeName.getContent().setVisible(true);
			this.configuredAttributeNames.setVisible(false);

			for (String value : stringOperator.getAttributeValues())
				this.attributeValues.addValue(value);
		}
		catch (InvalidContentException e)
		{
			throw new PolicyComponentValidationException(e);
		}
	}

	@Override
	public void executeEvent(BaseEvent event)
	{
		super.executeEvent(event);
		if (event instanceof DisplayWidgetEvent)
		{
			DisplayWidgetEvent dwe = (DisplayWidgetEvent) event;
			if (dwe.getParent() == this)
			{
				this.refreshLocalInterface();
			}
		}
	}

	@Override
	public String[] getRegisteredEvents()
	{
		/* All events this class will respond to */
		String[] registeredEvents = { EventConstants.integratedMultiValueTextBoxValueAdded,
				EventConstants.integratedMultiValueTextBoxValueRemoved };

		String[] combinedEvents = new String[registeredEvents.length + super.getRegisteredEvents().length];
		System.arraycopy(registeredEvents, 0, combinedEvents, 0, registeredEvents.length);
		System.arraycopy(super.getRegisteredEvents(), 0, combinedEvents, registeredEvents.length, super
				.getRegisteredEvents().length);

		return combinedEvents;
	}

}

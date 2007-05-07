/*
* Copyright 2004,2005 The Apache Software Foundation.
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

/*
 * Modifications by Shaun Mangelsdorf
 */


package com.qut.middleware.spep.ws.receiver;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;

import java.lang.reflect.Method;

/**
 * Modified from RawXMLINOutMessageReceiver to allow support for signed XML documents in
 * the payload of a SOAP envelope. Previously a namespace would be added to the root
 * element, and so that namespace declaration code was removed from this modified version.
 * 
 * The RawXMLINOutMessageReceiver MessageReceiver hands over the raw request received to
 * the service implementation class as an OMElement. The implementation class is expected
 * to return back the OMElement to be returned to the caller. This is a synchronous
 * MessageReceiver, and finds the service implementation class to invoke by referring to
 * the "ServiceClass" parameter value specified in the service.xml and looking at the
 * methods of the form OMElement <<methodName>>(OMElement request)
 *
 * @see RawXMLINOutMessageReceiver
 */
public class BareXMLInOutReceiver extends AbstractInOutSyncMessageReceiver
        implements MessageReceiver {

    private Method findOperation(AxisOperation op, Class<?> ImplClass) {
        String methodName = op.getName().getLocalPart();
        Method[] methods = ImplClass.getMethods();

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodName) &&
                    methods[i].getParameterTypes().length == 1 &&
                    OMElement.class.getName().equals(
                            methods[i].getParameterTypes()[0].getName()) &&
                    OMElement.class.getName().equals(methods[i].getReturnType().getName())) {
                return methods[i];
            }
        }

        return null;
    }

    /**
     * Invokes the business logic invocation on the service implementation class.
     *
     * @param msgContext    the incoming message context
     * @param newmsgContext the response message context
     * @throws AxisFault on invalid method (wrong signature) or behaviour (return null)
     */
    @Override
    public void invokeBusinessLogic(MessageContext msgContext, MessageContext newmsgContext)
            throws AxisFault {
        try {

            // get the implementation class for the Web Service
            Object obj = getTheImplementationObject(msgContext);

            // find the WebService method
            Class<?> ImplClass = obj.getClass();

            AxisOperation opDesc = msgContext.getOperationContext().getAxisOperation();
            Method method = findOperation(opDesc, ImplClass);

            if (method != null) {
                OMElement result = (OMElement) method.invoke(
                        obj, new Object[]{msgContext.getEnvelope().getBody().getFirstElement()});
                SOAPFactory fac = getSOAPFactory(msgContext);
                SOAPEnvelope envelope = fac.getDefaultEnvelope();

                if (result != null) {
                    envelope.getBody().addChild(result);
                }

                newmsgContext.setEnvelope(envelope);

            } else {
                throw new AxisFault(Messages.getMessage("methodDoesNotExistInOut", opDesc.getName().toString())); //$NON-NLS-1$
            }
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }
}

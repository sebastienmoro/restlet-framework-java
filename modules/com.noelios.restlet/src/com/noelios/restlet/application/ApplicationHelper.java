/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.application;

import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.noelios.restlet.ContextHelper;

/**
 * Application implementation.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ApplicationHelper extends ContextHelper<Application> {
    /**
     * Constructor.
     * 
     * @param application
     *                The application to help.
     * @param parentContext
     *                The parent context, typically the component's context.
     */
    public ApplicationHelper(Application application, Context parentContext) {
        super(application, parentContext);
    }

    @Override
    public Context createContext(String loggerName) {
        return new ApplicationContext(getHelped(), getParentContext(), Logger
                .getLogger(loggerName));
    }

    /**
     * Creates a new decoder filter. Allows overriding.
     * 
     * @param application
     *                The parent application.
     * @return The new decoder filter.
     */
    protected Filter createDecoderFilter(Application application) {
        return new Decoder(application.getContext(), true, false);
    }

    /**
     * Creates a new status filter. Allows overriding.
     * 
     * @param application
     *                The parent application.
     * @return The new status filter.
     */
    protected Filter createStatusFilter(Application application) {
        return new ApplicationStatusFilter(application);
    }

    /**
     * Creates a new tunnel filter. Allows overriding.
     * 
     * @param application
     *                The parent application.
     * @return The new tunnel filter.
     */
    protected Filter createTunnelFilter(Application application) {
        return new TunnelFilter(application);
    }

    /**
     * In addition to the default behavior, it sets the request and response
     * {@link Application#KEY} attributes with the application instance.
     * 
     * @param request
     *                The request to handle.
     * @param response
     *                The response to update.
     */
    @Override
    public void handle(Request request, Response response) {
        // Add the application in request and response attributes
        request.getAttributes().put(Application.KEY, getHelped());
        response.getAttributes().put(Application.KEY, getHelped());

        // Actually handle call
        super.handle(request, response);
    }

    /** Start hook. */
    @Override
    public synchronized void start() throws Exception {
        // Addition of tunnel filter
        if (getHelped().getTunnelService().isEnabled()) {
            addFilter(createTunnelFilter(getHelped()));
        }

        // Addition of status pages
        if (getHelped().getStatusService().isEnabled()) {
            addFilter(createStatusFilter(getHelped()));
        }

        // Addition of decoder filter
        if (getHelped().getDecoderService().isEnabled()) {
            addFilter(createDecoderFilter(getHelped()));
        }

        // Attach the Application's root Restlet
        setNext(getHelped().getRoot());
    }

    @Override
    public synchronized void stop() throws Exception {
        clear();
    }

    @Override
    public void update() throws Exception {
    }

}

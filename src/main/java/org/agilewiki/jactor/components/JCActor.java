/*
 * Copyright 2011 Bill La Forge
 *
 * This file is part of AgileWiki and is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (LGPL) as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * or navigate to the following url http://www.gnu.org/licenses/lgpl-2.1.txt
 *
 * Note however that only Scala, Java and JavaScript files are being covered by LGPL.
 * All other files are covered by the Common Public License (CPL).
 * A copy of this license is also included and can be
 * found as well at http://www.opensource.org/licenses/cpl1.0.txt
 */
package org.agilewiki.jactor.components;

import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.bind.Internals;
import org.agilewiki.jactor.bind.JBActor;
import org.agilewiki.jactor.bind.VoidInitializationMethodBinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * <p>
 * Application logic is added to a JCActor by including components rather than by subclassing.
 * </p>
 * <p>
 * JCActors are fully interoperable with JLPCActors and JBActors.
 * </p>
 */
final public class JCActor extends JBActor {
    private ArrayList<Component> components = new ArrayList<Component>();

    /**
     * Create a JCActor.
     *
     * @param mailbox A mailbox which may be shared with other actors.
     */
    public JCActor(final Mailbox mailbox) {
        super(mailbox);

        bind(Include.class.getName(), new VoidInitializationMethodBinding<Include>() {
            @Override
            public void initializationProcessRequest(Internals internals, Include request)
                    throws Exception {
                processInclude(request);
            }
        });
    }

    /**
     * Process an include.
     *
     * @param include The include request.
     * @throws Exception Any uncaught exceptions from calls to the component bindery methods.
     */
    private void processInclude(Include include)
            throws Exception {
        Class clazz = include.getClazz();
        final String className = clazz.getName();
        ConcurrentSkipListMap<String, Object> data = getData();
        if (data.containsKey(className))
            return;
        Object o = clazz.newInstance();
        data.put(className, o);
        if (!(o instanceof Component))
            return;
        final Component c = (Component) o;
        ArrayList<Include> includes = c.includes();
        if (includes == null) {
            c.thisActor = this;
            c.bindery();
            components.add(c);
            return;
        }
        final Iterator<Include> it = includes.iterator();
        while (it.hasNext()) {
            processInclude(it.next());
        }
        c.thisActor = JCActor.this;
        c.bindery();
        components.add(c);
    }

    /**
     * Calls open on each component, marks the actor as active,
     * and then calls opened on each component.
     *
     * @param internals The actor's internals.
     * @throws Exception Any uncaught exceptions raised while processing the open.
     */
    @Override
    protected void open(Internals internals) throws Exception {
        Iterator<Component> it = components.iterator();
        while (it.hasNext()) {
            Component c = it.next();
            c.opening(internals);
        }
        super.open(internals);
        it = components.iterator();
        while (it.hasNext()) {
            Component c = it.next();
            c.opened(internals);
        }
    }

    /**
     * Call close on all the components, ignoring any exceptions that are thrown.
     * Components are closed in reverse dependency order, the root component being the first.
     */
    public void close() {
        int i = components.size();
        while (i > 0) {
            i -= 1;
            Component c = components.get(i);
            try {
                c.close();
            } catch (Exception e) {
            }
        }
    }
}

/*
 *   Copyright (C) 2010  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 *
 */

package org.concord.energy2d.event;

import java.util.EventObject;

/**
 * @author Charles Xie
 * 
 */
public class GraphEvent extends EventObject {

	public final static byte GRAPH_OPENED = 0;
	public final static byte GRAPH_CLOSED = 1;

	public GraphEvent(Object source) {
		super(source);
	}

}

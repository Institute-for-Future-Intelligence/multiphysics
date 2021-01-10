package org.energy2d.event;

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

/*
 * Copyright (C) 2007 Unicon, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this distribution.  It is also available here:
 * http://www.fsf.org/licensing/licenses/gpl.html
 *
 * As a special exception to the terms and conditions of version 
 * 2 of the GPL, you may redistribute this Program in connection 
 * with Free/Libre and Open Source Software ("FLOSS") applications 
 * as described in the GPL FLOSS exception.  You should have received
 * a copy of the text describing the FLOSS exception along with this
 * distribution.
 */
package net.unicon.portal.groups.framework.eve.graph;

import org._3pq.jgrapht.DirectedGraph;
import org._3pq.jgrapht.Edge;
import org._3pq.jgrapht.edge.EdgeFactories;
import org._3pq.jgrapht.EdgeFactory;
import org._3pq.jgrapht.Graph;
import org._3pq.jgrapht.graph.DefaultDirectedGraph;
import org._3pq.jgrapht.graph.UnmodifiableDirectedGraph;

/**
 * @author nbolton
 *
 * This will produce the correct graph implementation to the Eve group
 * service and store.
 */
public class GraphFactory {

    private static GraphFactory __instance = new GraphFactory();
    private EdgeFactory edgeFactory = 
        new EdgeFactories.DirectedEdgeFactory();
    
    public static GraphFactory instance() {
        return __instance;
    }
    
    public DefaultDirectedGraph newGraph() {
        return new DefaultDirectedGraph();
    }
    
    public DirectedGraph cloneGraph(Graph graph) {
        DefaultDirectedGraph newGraph = newGraph(); 
        
        // clone graph

        // Setting to allow multiple edges will
        // improve performance. The group service
        // will prevent this.
        newGraph.setAllowMultipleEdges(true);
        newGraph.addAllVertices(graph.vertexSet());
        newGraph.addAllEdges(graph.edgeSet());
        newGraph.setAllowMultipleEdges(false);
        
        return newGraph;
    }
    
    public Edge createEdge(Object source, Object target) {
        return edgeFactory.createEdge(source, target);
    }
    
    public DirectedGraph unmodifiableClone(Graph g) {
        return new UnmodifiableDirectedGraph(cloneGraph(g));
    }
    
    private GraphFactory() {}
}

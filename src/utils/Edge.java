package utils;

import java.awt.Color;
import uchicago.src.sim.gui.DrawableEdge;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.network.DefaultEdge;
import uchicago.src.sim.network.Node;

public class Edge extends DefaultEdge implements DrawableEdge {
    private Color color = Color.WHITE;
    private static final float DEFAULT_STRENGTH = 1;

    public Edge(Node from, Node to, String message) {
        super(from, to, message, DEFAULT_STRENGTH);
    }

    public void setColor(Color c) {
        color = c;
    }

    public void draw(SimGraphics g, int fromX, int toX, int fromY, int toY) {
        g.drawDirectedLink(color, fromX, toX, fromY, toY);
    }
}
